/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.ConnectionStorage;
import com.cyber.net.rx.UdpConnection;
import com.cyber.net.rx.UdpConnectionDispatcher;
import com.cyber.net.rx.UdpConnectionFactory;
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
    private final static long DEFAULT_TIMEOUT_VALUE = 5000;

    private final DatagramSocket localSocket;
    private final ConnectionStorage<SocketAddress> connectionStorage;

    private final UdpTransport udp;
    private final UdpConnectionDispatcher connectionDispatcher;
    private final UdpConnectionFactory connectionFactory;
    private Timeout connectionTimeout;
    
    private Disposable timeoutSub;
    
    
    public UdpServer(DatagramSocket localSocket) throws SocketException{
        this.localSocket = localSocket;
        connectionStorage = new ConnectionStorage<>();        
        connectionTimeout = new Timeout(DEFAULT_TIMEOUT_VALUE);

        udp = UdpTransport.listen(localSocket);
        connectionFactory = new UdpConnectionFactory( udp.getWriter() );        
        connectionDispatcher = new UdpConnectionDispatcher( connectionStorage, connectionFactory );        
        
        udp.getFlow().subscribeWith( connectionDispatcher );

        setTimeout(DEFAULT_TIMEOUT_VALUE);
    }

    public UdpServer(int port) throws SocketException{
        this(new DatagramSocket(port));
    }
        
    public UdpServer setTimeout(long milliseconds){
        connectionTimeout = new Timeout(milliseconds);        

        if (timeoutSub!=null && !timeoutSub.isDisposed()) timeoutSub.dispose();
        
        timeoutSub = Observable.interval( connectionTimeout.getTimeoutValue(), TimeUnit.MILLISECONDS )
            .flatMap(i -> connectionStorage.iterate())
            .filter(e -> connectionTimeout.isTimeout( e.getValue().getLastActivityTime() ) )
            .subscribe( e -> connectionStorage.remove( e.getKey() ) );
        
        return this;
    }
        
    public void close(){
        connectionStorage.close();        
        udp.close();
        localSocket.close();
    }
    
    /**
     * Возвращает поток новых соединений
     * @return 
     */
    public Observable<UdpConnection> observeConnection() {
        return connectionFactory.getFlow();
    }
    
}
