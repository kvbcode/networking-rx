/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx.impl;

import com.cyber.net.rx.UdpConnection;
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
 * @author CyberManic
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
    public void testSimpleEcho() throws Exception{
        System.out.println("\ntestSimpleEcho()");
        
        TestObserver<String> testObs = new TestObserver<>();
        
        UdpServer server = new UdpServer(port);
        server.observeConnection().subscribeWith( ProtocolFactory.from( EchoProtocol::new ) );        

        
        UdpTransport client = UdpTransport.connect("127.0.0.1", port);
        client.getFlow().map(p -> new String(p.getData())).subscribe(System.out::println);
        client.getFlow().map(p -> new String(p.getData())).subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
                
        TimeUnit.MILLISECONDS.sleep(50);

        server.close();
        
        testObs.assertValueCount(3);
        testObs.assertValueAt(2, TEST_STRING);
    }
    
    @Test
    public void testConnectionTimeout() throws Exception{
        System.out.println("\ntestConnectionTimeout()");
     
        TestObserver<String> testObs = new TestObserver<>();
        final AtomicInteger completedConnectionCounter = new AtomicInteger(0);
        
        UdpServer server = new UdpServer(port);
        server
            .setTimeout(100)
            .observeConnection()
                .doOnNext(conn -> conn.getDownstream()
                    .doOnComplete( completedConnectionCounter::incrementAndGet )
                    .subscribe()
                )
                .subscribeWith( ProtocolFactory.from( EchoProtocol::new ) );
        
        UdpTransport client = UdpTransport.connect("127.0.0.1", port);
        client.getFlow().map(p -> new String(p.getData())).subscribe(System.out::println);
        client.getFlow().map(p -> new String(p.getData())).subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
                
        TimeUnit.MILLISECONDS.sleep(250);

        client.send(TEST_STRING.getBytes());

        TimeUnit.MILLISECONDS.sleep(250);

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
            .subscribe( conn -> conn.getDownstream().subscribeWith( conn.getUpstream() ) );        
        
        UdpConnection conn = UdpClient.connect("127.0.0.1", port);
        conn.getDownstream().subscribe(data -> System.out.println("client.in: " + data));
        conn.getDownstream().map(b -> new String(b)).subscribeWith(testObs);
        
        conn.getUpstream().onNext(TEST_STRING.getBytes());
        conn.getUpstream().onNext(TEST_STRING.getBytes());
        conn.getUpstream().onNext(TEST_STRING.getBytes());
        
        TimeUnit.MILLISECONDS.sleep(50);
        server.close();

        testObs.awaitCount(3);
        testObs.assertValueAt(0, TEST_STRING);
    }
    
}
