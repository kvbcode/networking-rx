/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author CyberManic
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
