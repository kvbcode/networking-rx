/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public class UdpChannelFactory implements IChannelFactory<UdpChannel>{

    protected final PublishSubject flow = PublishSubject.create();
    protected final UdpSocketWriter writer;
    
    public UdpChannelFactory(UdpSocketWriter writer){
        this.writer = writer;
    }
    
    @Override
    public UdpChannel get(SocketAddress remoteAddress){
        UdpChannel conn = new UdpChannel(writer, remoteAddress);
        flow.onNext(conn);
        return conn;
    }

    @Override
    public Observable<UdpChannel> getFlow() {
        return flow;
    }
    
    
    
}
