/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import com.cyber.net.dto.RawPacket;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author CyberManic
 */
public class UdpSocketReader implements IFlowSource, Runnable{

    private final PublishSubject<RawPacket> flow = PublishSubject.create();
    private final DatagramSocket localSocket;
    private final byte[] netbuf = new byte[8200];
    private final DatagramPacket dg = new DatagramPacket(netbuf, netbuf.length);
    private volatile boolean shutdown = false;
    
    public UdpSocketReader(DatagramSocket localSocket){
        this.localSocket = localSocket;        
    }
    
    @Override
    public Observable<RawPacket> getFlow(){
        return flow;
    }
           
    private RawPacket receiveRawPacket() throws IOException{
        localSocket.receive(dg);                                                // block thread and wait for data        
        return RawPacket.from( dg );
    }
   
    @Override
    public void run(){
        try{
            while(!shutdown && !Thread.currentThread().isInterrupted()){
                flow.onNext( receiveRawPacket() );
            }
        }catch(SocketException ex){
            // используется прерывание через закрытие сокета,
            // поэтому игнорируем SocketException
            flow.onComplete();
        }catch(IOException ex){   
            flow.onError(ex);
        }finally{
            flow.onComplete();
        }
    }

    
    public void close(){
        shutdown = true;
        flow.onComplete();
    }
    
}
