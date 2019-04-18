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
package com.cyber.net.rx;

import com.cyber.net.ssl.DtlsWrapper;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsChannel extends UdpChannel{

    protected final DtlsWrapper dtls;
    protected final ByteBuffer outputBuffer;
    protected final int OUTPUT_BUFFER_SIZE = 65000;
    
    public DtlsChannel(SSLContext context, UdpSocketWriter writer, SocketAddress remoteSocketAddress) {
        super(writer, remoteSocketAddress);
        
        try{
            dtls = new DtlsWrapper(context);
        }catch(SSLException e){
            throw new RuntimeException("DtlsChannel SSL error: " + e.getMessage());
        }
            
        outputBuffer = ByteBuffer.allocateDirect(OUTPUT_BUFFER_SIZE);
        dtls.setOutputHandler(dataOut -> super.onNext(dataOut));        
        
        flow = flow.flatMap(input -> {
                if (!dtls.isHandshakeFinished()){
                    dtls.doHandshakeStep(ByteBuffer.wrap(input), outputBuffer.clear());
                    return Observable.empty();
                }                
                if (input.length==0) return Observable.empty();
                return Observable.just(dtls.decrypt(input));
            });
    }
    
    public DtlsWrapper getDtlsWrapper(){
        return dtls;
    }
    
    @Override
    public void onNext(byte[] dataOut) {
        try{
            super.onNext(dtls.crypt(dataOut));
        }catch(SSLException e){
            super.onError(e);
        }
    }        
    
}
