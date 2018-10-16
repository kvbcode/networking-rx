/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import com.cyber.net.dto.RawPacket;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author CyberManic
 */
public class UdpSocketWriter implements IRxConsumer<RawPacket>{

    private final DatagramSocket localSocket;
    
    public UdpSocketWriter(DatagramSocket localSocket){
        this.localSocket = localSocket;
    }    
        
    public DatagramSocket getLocalSocket() {
        return localSocket;
    }
        
    public void send(RawPacket packet) throws IOException{
        DatagramPacket p = new DatagramPacket(packet.getData(), packet.getData().length, packet.getRemoteSocketAddress());
        localSocket.send(p);
    }
        
    @Override
    public void onNext(RawPacket t) {
        try{
            send(t);
        }catch(IOException e){
            onError(e);
        }
    }

    @Override public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onComplete() {}
    
}
