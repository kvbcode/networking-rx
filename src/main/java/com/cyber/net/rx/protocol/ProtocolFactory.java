/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.protocol;

import com.cyber.net.rx.IConnection;
import com.cyber.net.rx.IFlowConsumer;
import com.cyber.net.rx.IFlowProcessor;
import io.reactivex.disposables.Disposable;
import java.util.function.Supplier;

/**
 *
 * @author CyberManic
 */
public class ProtocolFactory implements IFlowConsumer<IConnection>{

    private final Supplier<IProtocol> supplier;
    
    public ProtocolFactory(Supplier<IProtocol> protocolSupplier){
        this.supplier = protocolSupplier;
    }

    public IProtocol get(){
        return supplier.get();
    }

    @Override
    public void onNext(IConnection conn) {        
        IProtocol proto = get();
        
        conn.getFlow().subscribeWith(proto);
        proto.getFlow().subscribe(outData -> conn.send(outData));
        
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) {}

    @Override public void onComplete() {}
        
}
