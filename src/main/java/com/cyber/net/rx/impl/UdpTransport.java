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

import com.cyber.net.rx.UdpSocketReader;
import com.cyber.net.rx.UdpSocketWriter;
import com.cyber.net.dto.RawPacket;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import com.cyber.net.rx.IDuplex;

/**
 *
 * @author Kirill Bereznyakov
 */
public class UdpTransport implements IDuplex<RawPacket>{

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
