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
public class UdpConnection extends AFlowProcessor<byte[], byte[]> implements IConnection{

    private final UdpSocketWriter writer;    
    private final SocketAddress remoteSocketAddress;
    private volatile long lastActivityTime;
    
    public UdpConnection(UdpSocketWriter writer, SocketAddress remoteSocketAddress){
        this.writer = writer;
        this.remoteSocketAddress = remoteSocketAddress;
    }

    protected void updateActivityTime(){
        this.lastActivityTime = System.nanoTime();
    }

    @Override
    public long getLastActivityTime() {
        return lastActivityTime;
    }        
    
    @Override
    public void send(byte[] data) {
        writer.send(data, remoteSocketAddress);
    }

    @Override
    public void onNext(byte[] data) {
        updateActivityTime();
        flow.onNext(data);
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "[" + remoteSocketAddress + "]";
    }

}
