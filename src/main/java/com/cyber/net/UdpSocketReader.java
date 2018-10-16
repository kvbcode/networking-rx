/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import com.cyber.net.dto.RawPacket;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author CyberManic
 */
public class UdpSocketReader implements IRxProducer, Runnable{

    private final PublishSubject<RawPacket> producerSlot = PublishSubject.create();
    final DatagramSocket localSocket;
    final byte[] netbuf = new byte[4096];
    final DatagramPacket dg = new DatagramPacket(netbuf, netbuf.length);
    private volatile boolean shutdown = false;
    
    public UdpSocketReader(DatagramSocket localSocket){
        this.localSocket = localSocket;        
    }
    
    @Override
    public Observable<RawPacket> outputSlot(){
        return producerSlot;
    }
        
    public SocketAddress getLocalSocketAddress(){
        return localSocket.getLocalSocketAddress();
    }
    
    public int getPort(){
        return localSocket.getPort();
    }
    
    private RawPacket receiveRawPacket() throws IOException{
        localSocket.receive(dg);                                                // block thread and wait for data        
        return RawPacket.from( dg );
    }
        
    public void shutdown(){
        shutdown = true;
    }
    
    @Override
    public void run(){
        shutdown = false;
        try{
            while(!shutdown && !Thread.currentThread().isInterrupted()){
                producerSlot.onNext( receiveRawPacket() );
            }
        }catch(SocketException ex){
            // используется прерывание через закрытие сокета,
            // поэтому игнорируем SocketException
            producerSlot.onComplete();
        }catch(IOException ex){   
            producerSlot.onError(ex);
        }finally{
            producerSlot.onComplete();
        }
    }


}
