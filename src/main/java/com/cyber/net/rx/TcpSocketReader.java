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
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public class TcpSocketReader implements Runnable{

    private static final int MIN_CHUNK_SIZE = 0;
    
    private final PublishSubject<RawPacket> publish = PublishSubject.create();    
    private final Socket localSocket;
    private volatile boolean shutdown = false;
    
    public TcpSocketReader(Socket localSocket){
        this.localSocket = localSocket;        
    }
    
    public Observable<RawPacket> inputSlot(){        
        return publish;
    }
        
    public SocketAddress getLocalSocketAddress(){
        return localSocket.getLocalSocketAddress();
    }
    
    public int getPort(){
        return localSocket.getPort();
    }
    
    
    public void shutdown(){
        shutdown = true;
    }
    
    public void run(){
        shutdown = false;
        try(InputStream in = localSocket.getInputStream()){
            
            byte[] dataBuf;
            int availableBytes, readedBytes;

            while(!shutdown && !Thread.currentThread().isInterrupted()){
                
                availableBytes = in.available();
                if (availableBytes > MIN_CHUNK_SIZE){
                    dataBuf = new byte[availableBytes];
                    readedBytes = in.read(dataBuf);

                    if (readedBytes==-1) break;                    

                    publish.onNext( new RawPacket( localSocket.getRemoteSocketAddress(), dataBuf ) );
                }
            }            
            localSocket.close();
        }catch(IOException ex){            
            publish.onError(ex);
        }finally{
            publish.onComplete();        
        }
    }

}
