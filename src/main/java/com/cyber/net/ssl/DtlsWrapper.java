/* 
 * The MIT License
 *
 * Copyright 2019 Kirill Bereznyakov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cyber.net.ssl;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_TASK;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_WRAP;
import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
import javax.net.ssl.SSLEngineResult.Status;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsWrapper{

    private final SSLEngine ssl;
    private String mode;
    private boolean debug = false;
    
    private ByteBuffer outputBuffer = null;

    private boolean isHandshakeFinished = false;
    private int handshakeStepCounter = 0;
    private Consumer<byte[]> outputHandler;
    
    public DtlsWrapper(SSLContext sslContext) throws SSLException{
        ssl = sslContext.createSSLEngine();        
    }    

    public void useServerMode() throws SSLException{
        mode = "server";        
        ssl.setUseClientMode(false);
        ssl.setNeedClientAuth(true);
        ssl.beginHandshake();        
    }
    
    public void useClientMode() throws SSLException{
        mode = "client";
        ssl.setUseClientMode(true);
        ssl.beginHandshake();        
    }
    
    /**
     * Запуск подпрограмм. Можно выполнять в других потоках, тогда wrap/unwrap
     * будут приостановлены если обработка еще не завершена.
     */
    private void runDelegatedTasks(){
        Runnable runnable;
        while ((runnable = ssl.getDelegatedTask()) != null) {
            runnable.run();
            if (debug) System.out.println(mode + " task: HandshakeStatus = " + ssl.getHandshakeStatus());
        }
    }    

    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException{
        SSLEngineResult result = ssl.wrap(src, dst);
        if (debug) System.out.println(mode + " wrap: " + result.toString().replace("\n", ", "));
        return result;
    }
    
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException{
        SSLEngineResult result = ssl.unwrap(src, dst);
        if (debug) System.out.println(mode + " unwrap: " + result.toString().replace("\n", ", "));        
        return result;
    }    

    /**
     * Упрощенный метод для шифрования данных. Использует общий внутренний буфер
     * для вывода. Для большей эффективности и доступа к SSLEngineResult
     * рекомендуется {@link #wrap(java.nio.ByteBuffer,java.nio.ByteBuffer)}.
     * @param input byte[]
     * @return output byte[]
     * @throws SSLException 
     */
    public byte[] crypt(byte[] input) throws SSLException{
        ByteBuffer in = ByteBuffer.wrap(input);
        ByteBuffer out = getOutputBuffer().clear();

        SSLEngineResult result = wrap(in, out);
        while(result.getStatus()==Status.BUFFER_OVERFLOW){
            result = wrap(in, growUpOutputBuffer());
        }

        byte[] buf = new byte[out.flip().limit()];
        out.get(buf);
        return buf;
    }
    
    /**
     * Упрощенный метод для расшифровки данных. Использует общий внутренний буфер
     * для вывода. Для большей эффективности и доступа к SSLEngineResult
     * рекомендуется {@link #unwrap(java.nio.ByteBuffer,java.nio.ByteBuffer)}.
     * @param input byte[]
     * @return output byte[]
     * @throws SSLException 
     */
    public byte[] decrypt(byte[] input) throws SSLException{
        ByteBuffer in = ByteBuffer.wrap(input);
        ByteBuffer out = getOutputBuffer().clear();
        
        SSLEngineResult result = unwrap(in, out);                
        while(result.getStatus()==Status.BUFFER_OVERFLOW){
            result = unwrap(in, growUpOutputBuffer());
        }
        
        byte[] buf = new byte[out.flip().limit()];
        out.get(buf);        
        return buf;
    }
    
    public int getMaximumPacketSize(){
        return ssl.getSSLParameters().getMaximumPacketSize();
    }
    
    /**
     * Возвращает общий внутренний буфер для вывода.
     * Обычный размер 16Кб.
     * @return internal output ByteBuffer
     */
    private ByteBuffer getOutputBuffer(){
        if (outputBuffer==null) outputBuffer = ByteBuffer.allocateDirect(getMaximumPacketSize());                
        return outputBuffer;
    }

    /**
     * Увеличивает размер {@link #getOutputBuffer()} на {@link #getMaximumPacketSize()}
     * @return new ByteBuffer
     */
    private ByteBuffer growUpOutputBuffer(){
        if (outputBuffer==null){
            outputBuffer = ByteBuffer.allocateDirect(getMaximumPacketSize()*2);
        }else{
            outputBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity() + getMaximumPacketSize());
        }
        if (debug) System.out.println(mode + "growUpOutputBuffer() to " + outputBuffer.capacity());
        return outputBuffer;
    }
    
    /**
     * Истина, когда завершена последовательность Handshake и есть готовность
     * работы с пользовательскими данными.
     * @return boolean
     */
    public boolean isHandshakeFinished(){
        return isHandshakeFinished;
    }
    
    public SSLEngine getSSLEngine(){
        return ssl;
    }

    public void setDebug(boolean value){
        this.debug = value;
    }
    
    /**
     * Метод обработки данных при согласовании (Handshake). Вызывается каждый раз
     * при поступлении новой порции данных.
     * @param input
     * @param output
     * @return
     * @throws SSLException 
     */
    public HandshakeStatus doHandshakeStep(ByteBuffer input, ByteBuffer output) throws SSLException{
        SSLEngineResult result = null;
        HandshakeStatus hs;
        handshakeStepCounter++;
        
        if (debug) System.out.println( "\t" + mode + " handshake step: " + handshakeStepCounter + ", " + ssl.getHandshakeSession());
        
        do{
            hs = ssl.getHandshakeStatus();
            switch(hs){
                case NEED_UNWRAP:
                case NEED_UNWRAP_AGAIN:
                    result = unwrap(input, output);
                    break;
                case NEED_WRAP:
                    result = wrap(input, output);                
                    break;
                case NEED_TASK:
                    runDelegatedTasks();
                    break;
                case NOT_HANDSHAKING:
                    if (!isHandshakeFinished && result!=null && result.getHandshakeStatus()==FINISHED){
                        isHandshakeFinished = true;
                        handleOutput(output);
                    };
                    return hs;
            }
            
        }while(result!=null && result.getStatus()==Status.OK);                
        
        handleOutput(output);
        
        return hs;
    }
    
    private void handleOutput(ByteBuffer output){
        if (outputHandler!=null && output.position()>0){
            if (debug) System.out.println(mode + " handle output: " + output);
            byte[] buf = new byte[output.flip().limit()];
            output.get(buf);
            outputHandler.accept(buf);
            output.clear();
        }
    }
    
    public void setOutputHandler(Consumer<byte[]> outputHandler){
        this.outputHandler = outputHandler;
    }
    
}
