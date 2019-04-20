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

package com.cyber.net.rx.impl;

import com.cyber.net.rx.DtlsChannel;
import com.cyber.net.rx.IChannel;
import com.cyber.net.rx.UdpChannel;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsChannelWrapper extends DtlsChannel{
    protected final IChannel channel;
    
    private DtlsChannelWrapper(SSLContext context, IChannel channel, boolean useClientMode) throws SSLException{
        super(context, data -> channel.onNext(data));        
        this.channel = channel;

        if (useClientMode){
            dtls.useClientMode();
        }else{
            dtls.useServerMode();
        }
        
        dtls.setOutputWriter(channel::onNext);

        flow = channel.getFlow()
                .compose(DtlsChannel.getDtlsConnectionHandler(dtls, channelOutputBuffer));
    }
        
    /**
     * Возвращает адаптер канала в режиме сервера. Ждет входящих подключений от клиентов.
     * Автоматически начнет процедуру рукопожатия.
     * @param context
     * @param channel
     * @return
     * @throws SSLException 
     */
    public static DtlsChannelWrapper asServer(SSLContext context, UdpChannel channel) throws SSLException{
        return new DtlsChannelWrapper(context, channel, false);
    }
    
    /**
     * Возвращает адаптер канала в режиме клиента. Для начала процедуры рукопожатия
     * следует отправить любые данные или {@link #openConnection() }
     * @param context
     * @param channel
     * @return
     * @throws SSLException 
     */
    public static DtlsChannelWrapper asClient(SSLContext context, UdpChannel channel) throws SSLException{
        return new DtlsChannelWrapper(context, channel, true);
    }        
    
            
    @Override
    public long getLastActivityNanos() {
        return channel.getLastActivityNanos();
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
    
    @Override
    public void accept(byte[] dataIn) {
        channel.accept(dataIn);
    }

    @Override
    public Observable<byte[]> getFlow() {
        return flow;
    }

    @Override
    public void onSubscribe(Disposable dspsbl) {
        channel.onSubscribe(dspsbl);
    }

    @Override
    public void onNext(byte[] dataOut) {
        try{
            channel.onNext(dtls.crypt(dataOut));
        }catch(SSLException e){
            onError(e);
        }        
    }

    @Override
    public void onError(Throwable thrwbl) {
        channel.onError(thrwbl);
    }

    @Override
    public void onComplete() {
        channel.onComplete();        
    }
        
}
