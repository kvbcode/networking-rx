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

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.net.SocketAddress;

/**
 *
 * @author Kirill Bereznyakov
 */
public class UdpChannel implements IChannel{

    private final UdpSocketWriter writer;    
    private final SocketAddress remoteSocketAddress;
    private volatile long lastActivityNanos;
        
    protected final PublishSubject<byte[]> downstream = PublishSubject.create();
    protected Observable<byte[]> flow = downstream;
    
    public UdpChannel(UdpSocketWriter writer, SocketAddress remoteSocketAddress){
        this.writer = writer;
        this.remoteSocketAddress = remoteSocketAddress; 
        
        downstream.subscribe((data) -> this.updateActivityNanos());
    }

    protected void updateActivityNanos(){
        this.lastActivityNanos = System.nanoTime();
    }

    @Override
    public long getLastActivityNanos() {
        return lastActivityNanos;
    }        
    
    @Override
    public void close(){
        downstream.onComplete();
    }
    
    protected void send(byte[] data) {
        writer.send(data, remoteSocketAddress);
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "[" + remoteSocketAddress + "]";
    }

    @Override public void accept(byte[] dataIn){ downstream.onNext(dataIn); }

    @Override public void onNext(byte[] dataOut) { send(dataOut); }

    @Override public Observable<byte[]> getFlow() { return flow; }

    @Override public void onSubscribe(Disposable d) { }

    @Override public void onError(Throwable e) { }

    @Override public void onComplete() { }

}
