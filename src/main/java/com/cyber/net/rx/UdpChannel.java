/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public class UdpChannel extends AFlowDuplex<byte[]> implements IChannel{

    private final UdpSocketWriter writer;    
    private final SocketAddress remoteSocketAddress;
    private volatile long lastActivityTime;
        
    public UdpChannel(UdpSocketWriter writer, SocketAddress remoteSocketAddress){
        this.writer = writer;
        this.remoteSocketAddress = remoteSocketAddress; 
        
        getDownstream().subscribe((data) -> this.updateActivityTime());
        getUpstream().subscribe(this::send);
    }

    protected void updateActivityTime(){
        this.lastActivityTime = System.nanoTime();
    }

    @Override
    public long getLastActivityTime() {
        return lastActivityTime;
    }        
    
    @Override
    public void close(){
        getDownstream().onComplete();
        getUpstream().onComplete();
    }
    
    protected void send(byte[] data) {
        writer.send(data, remoteSocketAddress);
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "[" + remoteSocketAddress + "]";
    }

}
