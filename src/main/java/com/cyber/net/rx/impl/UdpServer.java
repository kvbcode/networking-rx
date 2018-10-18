/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.ConnectionStorage;
import com.cyber.net.rx.UdpConnectionDispatcher;
import com.cyber.net.rx.UdpConnectionFactory;
import com.cyber.net.rx.protocol.EchoProtocol;
import com.cyber.net.rx.protocol.IProtocol;
import com.cyber.net.rx.protocol.ProtocolFactory;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.function.Supplier;

/**
 *
 * @author CyberManic
 */
public class UdpServer {

    private final DatagramSocket localSocket;
    private final UdpTransport udp;
    private ProtocolFactory protocolFactory;
    
    public UdpServer(DatagramSocket localSocket) throws SocketException{
        this.localSocket = localSocket;
        udp = UdpTransport.listen(localSocket);
    }

    public UdpServer(int port) throws SocketException{
        this(new DatagramSocket(port));
    }
    
    public UdpServer setProtocol(Supplier<IProtocol> protocolSupplier){
        protocolFactory = new ProtocolFactory(protocolSupplier);
        return this;
    }
    
    public void start(){         
        UdpConnectionFactory connectionFactory = new UdpConnectionFactory( udp.getWriter() );        
        connectionFactory.getFlow().subscribeWith( protocolFactory );
        
        ConnectionStorage<SocketAddress> connectionStorage = new ConnectionStorage<>();
        UdpConnectionDispatcher connectionDispatcher = new UdpConnectionDispatcher(connectionStorage, connectionFactory);
        
        udp.getFlow().subscribeWith( connectionDispatcher );        
    }
    
    public void close(){
        udp.close();
        localSocket.close();
    }
    
}
