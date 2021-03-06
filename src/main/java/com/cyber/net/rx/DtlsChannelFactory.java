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
import java.net.SocketAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsChannelFactory extends UdpChannelFactory{

    protected final SSLContext context;

    public DtlsChannelFactory(SSLContext context, UdpSocketWriter writer){
        super(writer);
        this.context = context;
    }

    @Override
    public DtlsChannel get(SocketAddress remoteAddress){
        DtlsChannel conn = new DtlsChannel(context, writer.getWriterFor(remoteAddress));
        try{
            conn.getDtlsWrapper().useServerMode();
            flow.onNext(conn);
        }catch(SSLException e){
            flow.onError(e);
        }
        return conn;
    }    

    @Override
    public Observable<UdpChannel> getFlow() {
        return flow;
    }
    
}
