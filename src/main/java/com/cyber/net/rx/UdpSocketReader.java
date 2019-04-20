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
 * @author Kirill Bereznyakov
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
            if (!shutdown) flow.onError(ex);
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
