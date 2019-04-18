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

import com.cyber.net.rx.UdpChannel;
import com.cyber.net.rx.protocol.EchoProtocol;
import com.cyber.net.rx.protocol.ProtocolFactory;
import io.reactivex.observers.TestObserver;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kirill Bereznyakov
 */
public class UdpServerTest {
    String TEST_STRING = "THIS_IS_TEST_STRING";
    int port = 19999;
    
    public UdpServerTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testServerEchoProtocol() throws Exception{
        System.out.println("\ntestSimpleEcho()");
        
        TestObserver<String> testObs = new TestObserver<>();
        
        UdpServer server = new UdpServer(port);
        server
            .observeConnection()
            .subscribeWith( ProtocolFactory.from( EchoProtocol::new ) );        
        
        UdpTransport client = UdpTransport.connect("127.0.0.1", port);
        client
            .getFlow()
            .map(p -> new String(p.getData()))
            .doOnNext(System.out::println)
            .subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
                
        TimeUnit.MILLISECONDS.sleep(50);

        server.close();
        
        testObs.assertValueCount(3);
        testObs.assertValueAt(2, TEST_STRING);

        client.close();
    }
    
    @Test
    public void testConnectionTimeout() throws Exception{
        System.out.println("\ntestConnectionTimeout()");
     
        TestObserver<String> testObs = new TestObserver<>();
        final AtomicInteger completedConnectionCounter = new AtomicInteger(0);
        
        UdpServer server = new UdpServer(port);
        server
            .setTimeout(50)
            .observeConnection()
                .doOnNext(ch -> ch.getFlow()
                    .doOnComplete( completedConnectionCounter::incrementAndGet )
                    .subscribe()
                )
                .subscribeWith( ProtocolFactory.from( EchoProtocol::new ) );
        
        UdpTransport client = UdpTransport.connect("127.0.0.1", port);
        client
            .getFlow()
            .map(p -> new String(p.getData()))
            .doOnNext(System.out::println)
            .subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
                
        TimeUnit.MILLISECONDS.sleep(150);

        client.send(TEST_STRING.getBytes());

        TimeUnit.MILLISECONDS.sleep(150);

        server.close();
        
        testObs.assertValueCount(3);
        testObs.assertValueAt(2, TEST_STRING);        
                
        System.out.println("completedConnectionCounter: " + completedConnectionCounter.get());
        
        assertTrue( completedConnectionCounter.get() == 2 );
    }
    
    @Test
    public void testClientServerEcho() throws Exception{
        System.out.println("\ntestClientServerEcho()");

        TestObserver<String> testObs = new TestObserver<>();
        
        UdpServer server = new UdpServer(port);
        server.observeConnection()
            .doOnNext(data -> System.out.println("server.in: " + data))
            .subscribe( conn -> conn.getFlow().subscribeWith( conn ) );        
        
        UdpChannel ch = UdpClient.connect("127.0.0.1", port);
        ch.getFlow().subscribe(data -> System.out.println("client.in: " + data));
        ch.getFlow().map(b -> new String(b)).subscribeWith(testObs);
        
        ch.accept(TEST_STRING.getBytes());
        ch.accept(TEST_STRING.getBytes());
        ch.accept(TEST_STRING.getBytes());
        
        TimeUnit.MILLISECONDS.sleep(50);
        server.close();

        testObs.awaitCount(3);
        testObs.assertValueAt(0, TEST_STRING);
    }
    
}
