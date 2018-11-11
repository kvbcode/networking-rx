/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public class UdpChannel implements IChannel{

    private final UdpSocketWriter writer;    
    private final SocketAddress remoteSocketAddress;
    private volatile long lastActivityNanos;
        
    protected final PublishSubject<byte[]> downstream = PublishSubject.create();
    protected Observable<byte[]> flow = downstream;
    
    public UdpChannel(UdpSocketWriter writer, SocketAddress remoteSocketAddress){
        this.writer = writer;
        this.remoteSocketAddress = remoteSocketAddress; 
        
        downstream.subscribe((data) -> this.updateActivityNanos());
    }

    protected void updateActivityNanos(){
        this.lastActivityNanos = System.nanoTime();
    }

    @Override
    public long getLastActivityNanos() {
        return lastActivityNanos;
    }        
    
    @Override
    public void close(){
        downstream.onComplete();
    }
    
    protected void send(byte[] data) {
        writer.send(data, remoteSocketAddress);
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "[" + remoteSocketAddress + "]";
    }

    @Override public void accept(byte[] dataIn){ downstream.onNext(dataIn); }

    @Override public void onNext(byte[] dataOut) { send(dataOut); }

    @Override public Observable<byte[]> getFlow() { return flow; }

    @Override public void onSubscribe(Disposable d) { }

    @Override public void onError(Throwable e) { }

    @Override public void onComplete() { }

}
