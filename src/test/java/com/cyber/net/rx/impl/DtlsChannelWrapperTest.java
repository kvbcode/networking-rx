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
import com.cyber.net.ssl.SSLContextFactory;
import io.reactivex.observers.TestObserver;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kirill Bereznyakov
 */
public class DtlsChannelWrapperTest {
    public static final int PORT = 15555;
    static SSLContext context;
    static String SERVER_HELLO = "server hello";
    static String[] CLIENT_HELLO = {"client HELLO1", "client HELLO2", "client HELLO3"};
    
    static{
        try{
            // require java >=9 for DTLS protocol
            context = new SSLContextFactory.Builder()
                    .setProtocol("DTLS")
                    .setKeyStoreResourceFile("localhost.jks")
                    .setKeyStorePassPhrase("localhost")
                    .build().get();        
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
    
    public DtlsChannelWrapperTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDtlsChannelWrapper() throws Exception{
        System.out.println("testDtlsChannelWrapper()");
        
        UdpServer server = new UdpServer(PORT);
        
        server.observeConnection()
                .subscribe(udpCh -> {
                    DtlsChannel ch = DtlsChannelWrapper.asServer(context, udpCh);                    
                    ch.observeStatus()
                            .doOnComplete(() -> System.out.println("SERVER CONNECTION CLOSED") )
                            .subscribe(conn -> System.out.println("SERVER CONNECTED, HANDSHAKE FINISHED") );
                    
                    //ch.getDtlsWrapper().setDebug(true);
                    ch.getFlow()
                            .doOnNext(data -> System.out.println("server.in: " + new String(data)))
                            .doOnComplete(() -> System.out.println("server.in: channel closed"))
                            .subscribeWith(ch);
                    
                });        
        
        TestObserver<String> clientTestObs = new TestObserver<>();
                
        DtlsChannel client = DtlsChannelWrapper.asClient(context, UdpClient.connect("127.0.0.1", PORT));
        //client.getDtlsWrapper().setDebug(true);
        client.getFlow()
                .map(String::new)
                .doOnNext(s -> System.out.println("client.in: " + s))
                .subscribe(clientTestObs);
                
        client.observeStatus()
                .doOnComplete(() -> System.out.println("CLIENT CONNECTION CLOSED"))
                .subscribe(ch -> {
                    System.out.println("CLIENT CONNECTED, HANDSHAKE FINISHED");
                    ch.onNext(CLIENT_HELLO[0].getBytes());
                    ch.onNext(CLIENT_HELLO[1].getBytes());
                    ch.onNext(CLIENT_HELLO[2].getBytes());
                    ch.close();
                });
        
        clientTestObs
                .awaitCount(3)
                .assertValueCount(3)
                .assertValuesOnly(CLIENT_HELLO[0], CLIENT_HELLO[1], CLIENT_HELLO[2]);        
        
        server.close();
    }    
    
}
