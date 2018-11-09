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
public class UdpChannelDispatcher implements IFlowConsumer<RawPacket>{

    private final ChannelStorage<SocketAddress> storage;
    private final UdpChannelFactory factory;
    
    public UdpChannelDispatcher(ChannelStorage<SocketAddress> channelStorage, UdpChannelFactory channelFactory){
        this.storage = channelStorage;
        this.factory = channelFactory;
    }

    @Override
    public void onNext(RawPacket p) {
        final SocketAddress remoteAddress = p.getRemoteSocketAddress();
        
        IChannel conn = Optional
            .ofNullable(storage.get(remoteAddress))
            .orElseGet(() -> {
                    IChannel c = factory.get(remoteAddress);
                    storage.put(remoteAddress, c);
                    return c;
            });        
                
        conn.getDownstream().onNext(p.getData());
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) { e.printStackTrace(System.err); }

    @Override public void onComplete() {}

}
