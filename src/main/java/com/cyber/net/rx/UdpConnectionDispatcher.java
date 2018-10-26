/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import com.cyber.net.dto.RawPacket;
import io.reactivex.disposables.Disposable;
import java.net.SocketAddress;
import java.util.Optional;

/**
 *
 * @author CyberManic
 */
public class UdpConnectionDispatcher implements IFlowConsumer<RawPacket>{

    private final ConnectionStorage<SocketAddress> storage;
    private final UdpConnectionFactory factory;
    
    public UdpConnectionDispatcher(ConnectionStorage<SocketAddress> connectionStorage, UdpConnectionFactory connectionFactory){
        this.storage = connectionStorage;
        this.factory = connectionFactory;
    }

    @Override
    public void onNext(RawPacket p) {
        final SocketAddress remoteAddress = p.getRemoteSocketAddress();
        
        IConnection conn = Optional
            .ofNullable(storage.get(remoteAddress))
            .orElseGet(() -> {
                    IConnection c = factory.get(remoteAddress);
                    storage.put(remoteAddress, c);
                    return c;
            });        
                
        conn.getDownstream().onNext(p.getData());
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) {}

    @Override public void onComplete() {}

}
