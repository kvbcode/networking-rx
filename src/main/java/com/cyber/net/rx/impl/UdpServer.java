/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.ConnectionStorage;
import com.cyber.net.rx.IConnection;
import com.cyber.net.rx.UdpConnectionDispatcher;
import com.cyber.net.rx.UdpConnectionFactory;
import com.cyber.net.rx.protocol.IProtocol;
import com.cyber.net.rx.protocol.ProtocolFactory;
import com.cyber.util.Timeout;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author CyberManic
 */
public class UdpServer {

    private final DatagramSocket localSocket;
    private final ConnectionStorage<SocketAddress> connectionStorage;
    private final CompositeDisposable disposables;

    private UdpTransport udp;
    private UdpConnectionDispatcher connectionDispatcher;
    private UdpConnectionFactory connectionFactory;
    private Observer<IConnection> newConnectionListener;
    private Timeout connectionTimeout;
    
    public UdpServer(DatagramSocket localSocket){
        this.localSocket = localSocket;
        connectionStorage = new ConnectionStorage<>();        
        disposables = new CompositeDisposable();
        connectionTimeout = new Timeout(5_000);
    }

    public UdpServer(int port) throws SocketException{
        this(new DatagramSocket(port));
    }
    
    public UdpServer setOnConnectionListener(Observer<IConnection> newConnectionListener){
        this.newConnectionListener = newConnectionListener;
        return this;
    }
    
    public UdpServer setProtocol(Supplier<IProtocol> protocolSupplier){
        this.newConnectionListener = ProtocolFactory.from(protocolSupplier);
        return this;
    }
    
    public UdpServer setTimeout(long milliseconds){
        connectionTimeout = new Timeout(milliseconds);        
        return this;
    }

    public void start() throws SocketException{         
        udp = UdpTransport.listen(localSocket);

        connectionFactory = new UdpConnectionFactory( udp.getWriter() );        
        connectionFactory.getFlow().subscribeWith( newConnectionListener );
        
        connectionDispatcher = new UdpConnectionDispatcher( connectionStorage, connectionFactory );        
        udp.getFlow().subscribeWith( connectionDispatcher );
        
        bindSubscriptions();
    }

    private void bindSubscriptions(){
        //check connections timeout
        disposables.add(
            Observable.interval( connectionTimeout.getTimeoutValue(), TimeUnit.MILLISECONDS )
                .flatMap(i -> connectionStorage.iterate())
                .filter(e -> connectionTimeout.isTimeout( e.getValue().getLastActivityTime() ) )
                .subscribe( e -> connectionStorage.remove( e.getKey() ) )
        );
        
    }
    
    public void close(){
        disposables.clear();
        connectionStorage.close();        
        udp.close();
        localSocket.close();
    }
    
}
