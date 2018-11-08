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

    private static final Consumer<Throwable> DEFAULT_PROTOCOL_ERROR_HANDLER = System.err::println;
    
    private final Supplier<IProtocol> protocolSupplier;
    private final Consumer<Throwable> protocolErrorHandler;    
    
    private ProtocolFactory( Supplier<IProtocol> protocolSupplier, Consumer<Throwable> protocolErrorHandler ){
        this.protocolSupplier = protocolSupplier;
        this.protocolErrorHandler = protocolErrorHandler;
    }

    public static ProtocolFactory from( Supplier<IProtocol> protocolSupplier, Consumer<Throwable> protocolErrorHandler ){
        return new ProtocolFactory(protocolSupplier, protocolErrorHandler);
    }
    
    public static ProtocolFactory from( Supplier<IProtocol> protocolSupplier ){
        return new ProtocolFactory( protocolSupplier, DEFAULT_PROTOCOL_ERROR_HANDLER );
    }
    
    public IProtocol get(){
        return protocolSupplier.get();
    }

    @Override
    public void onNext(IChannel ch) {        
        setupChannel( get(), ch);        
    }
    
    public void setupChannel(IProtocol proto, IChannel ch){
        ch.getDownstream().subscribeWith( proto.getDownstream() );
        proto.getDownstream().subscribeWith( ch.getUpstream() );
    }
        
    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) {}

    @Override public void onComplete() {}
        
}
