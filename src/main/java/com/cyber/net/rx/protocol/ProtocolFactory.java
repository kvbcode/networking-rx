/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.protocol;

import com.cyber.net.rx.IFlowConsumer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.util.function.Supplier;
import com.cyber.net.rx.IChannel;

/**
 *
 * @author CyberManic
 */
public class ProtocolFactory implements IFlowConsumer<IChannel>{

    private final Supplier<IProtocol> protocolSupplier;
    
    private ProtocolFactory( Supplier<IProtocol> protocolSupplier ){
        this.protocolSupplier = protocolSupplier;
    }
    
    public static ProtocolFactory from( Supplier<IProtocol> protocolSupplier ){
        return new ProtocolFactory( protocolSupplier );
    }
    
    public IProtocol get(){
        return protocolSupplier.get();
    }

    @Override
    public void onNext(IChannel ch) {        
        setupChannel( ch, get());        
    }
    
    public void setupChannel(IChannel ch, IProtocol proto){
        ch.getDownstream().subscribeWith( proto.getDownstream() );
        proto.getDownstream().subscribeWith( ch.getUpstream() );
    }
        
    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) {}

    @Override public void onComplete() {}
        
}
