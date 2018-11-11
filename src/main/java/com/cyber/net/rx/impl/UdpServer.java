/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.ChannelStorage;
import com.cyber.net.rx.UdpChannel;
import com.cyber.net.rx.UdpChannelDispatcher;
import com.cyber.net.rx.UdpChannelFactory;
import com.cyber.util.Timeout;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author CyberManic
 */
public class UdpServer{
    private final static long DEFAULT_CHANNEL_TIMEOUT_MILLIS = 5000;

    private final DatagramSocket localSocket;
    private final ChannelStorage<SocketAddress> channelStorage;

    private final UdpTransport udp;
    private final UdpChannelDispatcher channelDispatcher;
    private final UdpChannelFactory channelFactory;
    private Timeout channelTimeoutUtil;
    
    private Disposable timeoutSub;
    
    
    public UdpServer(DatagramSocket localSocket) throws SocketException{
        this.localSocket = localSocket;
        channelStorage = new ChannelStorage<>();        

        udp = UdpTransport.listen(localSocket);
        channelFactory = new UdpChannelFactory( udp.getWriter() );        
        channelDispatcher = new UdpChannelDispatcher( channelStorage, channelFactory );        
        
        udp.getFlow().subscribeWith( channelDispatcher );

        setTimeout(DEFAULT_CHANNEL_TIMEOUT_MILLIS);
    }

    public UdpServer(int port) throws SocketException{
        this(new DatagramSocket(port));
    }

    public ChannelStorage<SocketAddress> getChannels(){
        return channelStorage;
    }
    
    public UdpServer setTimeout(long milliseconds){
        channelTimeoutUtil = Timeout.fromMillis(milliseconds);        

        if (timeoutSub!=null && !timeoutSub.isDisposed()) timeoutSub.dispose();
        
        timeoutSub = Observable.interval( channelTimeoutUtil.getTimeoutValueMillis(), TimeUnit.MILLISECONDS )
            .flatMap(i -> channelStorage.iterate())
            .filter(e -> channelTimeoutUtil.isTimeout( e.getValue().getLastActivityNanos() ) )
            .subscribe( e -> channelStorage.remove( e.getKey() ) );
        
        return this;
    }
        
    public void close(){
        channelStorage.close();        
        udp.close();
        localSocket.close();
    }
    
    /**
     * Возвращает поток новых соединений
     * @return Observable
     * @see UdpChannelFactory#getFlow
     */
    public Observable<UdpChannel> observeConnection() {
        return channelFactory.getFlow();
    }
    
}
