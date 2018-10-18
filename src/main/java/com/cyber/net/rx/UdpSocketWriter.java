/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import com.cyber.net.dto.RawPacket;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public class UdpSocketWriter implements IFlowConsumer<RawPacket>{

    private final DatagramSocket localSocket;
    
    public UdpSocketWriter(DatagramSocket localSocket){
        this.localSocket = localSocket;
    }    
        
    public DatagramSocket getLocalSocket() {
        return localSocket;
    }
        
    public void send(RawPacket packet){
        send(packet.getData(), packet.getRemoteSocketAddress());
    }

    public void send(byte[] data, SocketAddress remoteSocketAddress){
        try{
            DatagramPacket p = new DatagramPacket(data, data.length, remoteSocketAddress);
            localSocket.send(p);
        }catch(IOException e){
            onError(e);
        }
    }
    
    @Override
    public void onNext(RawPacket p) {
        send(p);
    }

    @Override public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onComplete() {}
    
}
