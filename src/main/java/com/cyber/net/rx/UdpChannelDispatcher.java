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
package com.cyber.net.rx;

import com.cyber.net.dto.RawPacket;
import io.reactivex.disposables.Disposable;
import java.net.SocketAddress;
import java.util.Optional;

/**
 *
 * @author Kirill Bereznyakov
 */
public class UdpChannelDispatcher implements IFlowConsumer<RawPacket>{

    protected final ChannelStorage<SocketAddress> storage;
    private IChannelFactory<? extends IChannel> factory;
    
    public UdpChannelDispatcher(ChannelStorage<SocketAddress> channelStorage, IChannelFactory<? extends IChannel> channelFactory){
        this.storage = channelStorage;
        this.factory = channelFactory;
    }

    public void setChannelFactory(IChannelFactory<? extends IChannel> factory){
        this.factory = factory;
    }
    
    @Override
    public void onNext(RawPacket p) {
        final SocketAddress remoteAddress = p.getRemoteSocketAddress();
        
        IChannel ch = Optional
            .ofNullable(storage.get(remoteAddress))
            .orElseGet(() -> {
                    IChannel c = factory.get(remoteAddress);
                    storage.put(remoteAddress, c);
                    return c;
            });        
        
        ch.accept(p.getData());
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onError(Throwable e) { e.printStackTrace(System.err); }

    @Override public void onComplete() {}

}
