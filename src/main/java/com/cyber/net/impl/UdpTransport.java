/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.impl;

import com.cyber.net.ARxIOElement;
import com.cyber.net.UdpSocketReader;
import com.cyber.net.UdpSocketWriter;
import com.cyber.net.dto.RawPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author CyberManic
 */
public class UdpTransport extends ARxIOElement<RawPacket>{

    private final DatagramSocket localSocket;
    private final UdpSocketReader reader;
    private UdpSocketWriter writer;

    private SocketAddress remoteSocketAddress;
    private Thread readerThread;
    
    private UdpTransport(DatagramSocket localSocket, SocketAddress remoteSocketAddress) throws SocketException{

        if (localSocket!=null){
            this.localSocket = localSocket;
        }else{
            this.localSocket = new DatagramSocket();
        }
            
        reader = new UdpSocketReader( this.localSocket );
        writer = new UdpSocketWriter( this.localSocket );

        bind(reader.outputSlot(), writer);
        
        readerThread = new Thread(reader, "UdpTransport");        
        readerThread.start();        
        
        if (remoteSocketAddress!=null){
            this.remoteSocketAddress = remoteSocketAddress;
            this.localSocket.connect(remoteSocketAddress);        
        }else{
            inputSlot()
                .firstOrError()
                .subscribe(p -> this.remoteSocketAddress = p.getRemoteSocketAddress() );
        }
    }

    public static UdpTransport listen(DatagramSocket localSocket) throws SocketException{
        return new UdpTransport(localSocket, null);
    }

    public static UdpTransport listen(int port) throws SocketException{
        return new UdpTransport(new DatagramSocket(port), null);
    }
    
    public static UdpTransport connect(SocketAddress remoteSocketAddress) throws SocketException{
        return new UdpTransport(null, remoteSocketAddress);
    }

    public static UdpTransport connect(String host, int port) throws SocketException{
        return new UdpTransport( null, new InetSocketAddress( host, port ) );
    }
        
    /**
     * Возвращает SocketAddress клиента или null
     * @return SocketAddress клиента
     */
    public SocketAddress getRemoteSocketAddress(){
        return remoteSocketAddress;
    }
        
    public void close(){        
        inputSlot().onComplete();
        outputSlot().onComplete();
        
        reader.shutdown();
        
        localSocket.close();
    }
    
    public boolean send(byte[] data){
        if (remoteSocketAddress==null) return false;
        
        outputSlot().onNext(new RawPacket(remoteSocketAddress, data));
        return true;
    }
    
    @Override
    public String toString(){
        return "UdpTransport[" + localSocket.getLocalSocketAddress() + ", " + remoteSocketAddress + "]";
    }
    
}
