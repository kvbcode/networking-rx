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
import java.net.SocketException;

/**
 *
 * @author CyberManic
 */
public class UdpSocketReader implements IFlowSource<RawPacket>, Runnable{

    private final PublishSubject<RawPacket> flow = PublishSubject.create();
    private final Observable<RawPacket> sharedFlow;
    
    private final DatagramSocket localSocket;
    private final byte[] netbuf = new byte[8200];
    private final DatagramPacket dg = new DatagramPacket(netbuf, netbuf.length);
    private volatile boolean shutdown = false;

    private final Thread readerThread;
    
    public UdpSocketReader(DatagramSocket localSocket){
        this.localSocket = localSocket;

        readerThread = new Thread(this, "UdpTransportSocketReader");                
        
        sharedFlow = flow
            .doOnSubscribe(d -> readerThread.start())
            .share();        
    }
    
    @Override
    public Observable<RawPacket> getFlow(){
        return sharedFlow;
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
            if (!flow.hasComplete()) flow.onError(ex);
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
