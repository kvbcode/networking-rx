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
public class UdpConnectionFactory implements IFlowSource<UdpConnection>{

    private final PublishSubject flow = PublishSubject.create();
    private final UdpSocketWriter writer;
    
    public UdpConnectionFactory(UdpSocketWriter writer){
        this.writer = writer;
    }

    public UdpConnection get(SocketAddress remoteAddress){
        UdpConnection conn = new UdpConnection(writer, remoteAddress);
        flow.onNext(conn);
        return conn;
    }

    @Override
    public Observable<UdpConnection> getFlow() {
        return flow;
    }
    
    
    
}
