/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.UdpSocketReader;
import com.cyber.net.rx.UdpSocketWriter;
import com.cyber.net.dto.RawPacket;
import com.cyber.net.rx.IFlowConsumer;
import com.cyber.net.rx.IFlowSource;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author CyberManic
 */
public class UdpTransport implements IFlowSource<RawPacket>, IFlowConsumer<RawPacket>{

    private final DatagramSocket localSocket;
    private final UdpSocketReader reader;
    private final UdpSocketWriter writer;

    private SocketAddress remoteSocketAddress;
    
    private UdpTransport(DatagramSocket localSocket, SocketAddress remoteSocketAddress) throws SocketException{

        this.localSocket = localSocket;
            
        reader = new UdpSocketReader( this.localSocket );
        writer = new UdpSocketWriter( this.localSocket );
                
        if (remoteSocketAddress!=null){
            this.remoteSocketAddress = remoteSocketAddress;
            this.localSocket.connect(remoteSocketAddress);        
        }

    }

    public static UdpTransport listen(DatagramSocket localSocket) throws SocketException{
        return new UdpTransport( localSocket, null );
    }

    public static UdpTransport listen(int port) throws SocketException{
        return new UdpTransport( new DatagramSocket(port), null);
    }
    
    public static UdpTransport connect(SocketAddress remoteSocketAddress) throws SocketException{
        return new UdpTransport( new DatagramSocket(), remoteSocketAddress );
    }

    public static UdpTransport connect(String host, int port) throws SocketException{
        return new UdpTransport( new DatagramSocket(), new InetSocketAddress( host, port ) );
    }
            
    public UdpSocketWriter getWriter(){
        return writer;
    }
    
    /**
     * Возвращает SocketAddress клиента или null
     * @return SocketAddress клиента
     */
    public SocketAddress getRemoteSocketAddress(){
        return remoteSocketAddress;
    }
        
    public void close(){   
        reader.close();
        if (!localSocket.isClosed()) localSocket.close();
    }
        
    @Override
    public String toString(){
        return "UdpTransport[" + localSocket.getLocalSocketAddress() + ", " + remoteSocketAddress + "]";
    }

    /**
     * Отправляет data на удаленный адрес, указанный при создании
     * @param data данные для отправки клиенту
     */
    public void send(byte[] data){
        if (remoteSocketAddress!=null) onNext( new RawPacket( remoteSocketAddress, data ) );
    }    
    
    /**
     * Возвращает поток данных от SocketReader
     * @return Observable
     */
    @Override
    public Observable<RawPacket> getFlow() {
        return reader.getFlow();
    }

    /**
     * Получает пакет для отправки через SocketWriter
     * @param p RawPacket 
     */
    @Override public void onNext(RawPacket p) { writer.onNext(p); }
   
    @Override public void onSubscribe(Disposable d) { }

    @Override public void onError(Throwable e) { e.printStackTrace(); }

    @Override public void onComplete() { }
    
}
