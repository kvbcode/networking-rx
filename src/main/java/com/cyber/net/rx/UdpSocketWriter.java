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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 *
 * @author Kirill Bereznyakov
 */
public class UdpSocketWriter implements IFlowConsumer<RawPacket>{

    private final DatagramSocket localSocket;
    
    public UdpSocketWriter(DatagramSocket localSocket){
        this.localSocket = localSocket;
    }    
        
    public DatagramSocket getLocalSocket() {
        return localSocket;
    }
        
    public void send(RawPacket packet){
        send(packet.getData(), packet.getRemoteSocketAddress());
    }

    public void send(byte[] data, SocketAddress remoteSocketAddress){
        try{
            DatagramPacket p = new DatagramPacket(data, data.length, remoteSocketAddress);
            localSocket.send(p);
        }catch(IOException e){
            onError(e);
        }
    }
    
    @Override
    public void onNext(RawPacket p) {
        send(p);
    }

    @Override public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override public void onSubscribe(Disposable d) {}

    @Override public void onComplete() {}
    
}
