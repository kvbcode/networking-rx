/* 
 * The MIT License
 *
 * Copyright 2019 Kirill Bereznyakov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cyber.net.rx.protocol;

import com.cyber.net.rx.IFlowConsumer;
import io.reactivex.disposables.Disposable;
import java.util.function.Supplier;
import com.cyber.net.rx.IChannel;

/**
 *
 * @author Kirill Bereznyakov
 */
public class ProtocolFactory implements IFlowConsumer<IChannel>, Supplier<IProtocol>{

    private final Supplier<IProtocol> protocolSupplier;
    
    private ProtocolFactory( Supplier<IProtocol> protocolSupplier ){
        this.protocolSupplier = protocolSupplier;
    }
    
    public static ProtocolFactory from( Supplier<IProtocol> protocolSupplier ){
        return new ProtocolFactory( protocolSupplier );
    }
    
    @Override
    public IProtocol get(){
        return protocolSupplier.get();
    }

    @Override
    public void onNext(IChannel ch) {        
        get().bind( ch.getFlow() )
            .getFlow().subscribeWith( ch );
    }
            
    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) {}

    @Override public void onComplete() {}
        
}
