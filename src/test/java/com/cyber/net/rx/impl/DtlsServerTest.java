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
package com.cyber.net.rx.impl;

import com.cyber.net.rx.DtlsChannel;
import com.cyber.net.rx.UdpChannel;
import com.cyber.net.ssl.SSLContextFactory;
import io.reactivex.observers.TestObserver;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsServerTest {
    
    public static final int PORT = 15555;
    
    public DtlsServerTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test    
    public void testDtlsConnection() throws Exception{
        System.out.println("testInstance()");
        
        String SERVER_HELLO = "server hello";
        String[] CLIENT_HELLO = {"client HELLO1", "client HELLO2", "client HELLO3"};
        TestObserver<String> clientTestObs = new TestObserver<>();
        TestObserver<String> serverTestObs = new TestObserver<>();
        
        // require java >=9 for DTLS protocol
        SSLContext context = new SSLContextFactory.Builder()
                .setProtocol("DTLS")
                .setKeyStoreResourceFile("localhost.jks")
                .setKeyStorePassPhrase("localhost")
                .build().get();
        
        DtlsServer server = new DtlsServer(context, PORT);        
        
        server.observeConnection()
                .subscribe(ch -> {
                    //((DtlsChannel)ch).getDtlsWrapper().setDebug(true);
                    System.out.println("server.new connection: " + ch);
                    ch.getFlow()
                        .map(String::new)
                        .doOnNext(s -> {
                            System.out.println("server.ch.in: " + s);
                            ch.onNext(SERVER_HELLO.getBytes());                            
                        })
                        .subscribe(serverTestObs);
                });
        
        DtlsChannel client = DtlsClient.connect(context, "127.0.0.1", PORT);
        //client.getDtlsWrapper().setDebug(true);
                        
        client.getFlow()
                .map(String::new)
                .doOnNext(s -> System.out.println("client.in: " + s))
                .subscribe(clientTestObs);
        
        client.onNext(new byte[0]);
        Thread.sleep(200);
        client.onNext(CLIENT_HELLO[0].getBytes());
        client.onNext(CLIENT_HELLO[1].getBytes());
        client.onNext(CLIENT_HELLO[2].getBytes());
        
        Thread.sleep(50);

        serverTestObs
                .assertValueCount(3)
                .assertValuesOnly(CLIENT_HELLO[0], CLIENT_HELLO[1], CLIENT_HELLO[2]);
                
        
        clientTestObs
                .assertValueCount(3)
                .assertValuesOnly(SERVER_HELLO, SERVER_HELLO, SERVER_HELLO);
                
    }
    
    
    
}
