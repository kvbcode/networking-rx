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

import com.cyber.net.ssl.DtlsAdapter;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.subjects.PublishSubject;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsChannel extends UdpChannel{
    protected static final int OUTPUT_BUFFER_SIZE = 65000;

    protected final DtlsAdapter dtls;
    protected final ByteBuffer channelOutputBuffer;
    protected final PublishSubject<DtlsChannel> obsConnectionStatus = PublishSubject.create();
    
    public DtlsChannel(SSLContext context, IOutputWriter outputWriter) {
        super(outputWriter);
        
        try{
            dtls = new DtlsAdapter(context);
        }catch(SSLException e){
            throw new RuntimeException("DtlsChannel SSL error: " + e.getMessage());
        }
            
        channelOutputBuffer = ByteBuffer.allocateDirect(OUTPUT_BUFFER_SIZE);
        dtls.setOutputWriter(super::onNext);
        dtls.setOnConnectedListener( () -> obsConnectionStatus.onNext(this) );
        dtls.setOnClosedListener( () -> obsConnectionStatus.onComplete() );
        
        flow = flow.compose(getDtlsConnectionHandler(dtls, channelOutputBuffer) );
    }

    public static ObservableTransformer<byte[], byte[]> getDtlsConnectionHandler(DtlsAdapter dtls, ByteBuffer channelOutputBuffer){
        return obs -> obs
            .flatMap(input -> {
                if (dtls.isHandshaking()){
                    dtls.handleConnectionData(ByteBuffer.wrap(input), channelOutputBuffer.clear());
                    return Observable.empty();
                }
                if (input.length==0) return Observable.empty();
                
                SSLEngineResult result = dtls.unwrap(ByteBuffer.wrap(input), channelOutputBuffer.clear());
                if (result.bytesProduced()==0) return Observable.empty();
                
                byte[] decryptedData = new byte[channelOutputBuffer.flip().limit()];
                channelOutputBuffer.get(decryptedData);
                
                return Observable.just(decryptedData);                
            });
    }
    
    public DtlsAdapter getDtlsWrapper(){
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
    
    @Override
    public void close(){
        dtls.getSSLEngine().closeOutbound();
        try{
            dtls.sendServiceData();
        }catch(SSLException e){
            onError(e);
        }
        obsConnectionStatus.onComplete();
    }
    
    /**
     * Возвращает DtlsChannel в случае подключения и успешного завершения рукопожатия.
     * При закрытии канала вернет onComplete()
     * @return 
     */
    public Observable<DtlsChannel> observeStatus(){
        return obsConnectionStatus;
    }

    public Observable<DtlsChannel> openConnection() throws SSLException{
        dtls.sendServiceData();
        return observeStatus();
    }

    
}
