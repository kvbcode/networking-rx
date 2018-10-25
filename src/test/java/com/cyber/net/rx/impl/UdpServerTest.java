/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx.impl;

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
    public void testSimple() throws Exception{
        System.out.println("\ntestSimple()");
        
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
        final AtomicInteger timeoutEventCounter = new AtomicInteger(0);
        
        UdpServer server = new UdpServer(port);
        server
            .setTimeout(250)
            .observeConnection()
                .subscribeWith( ProtocolFactory.from( EchoProtocol::new, err -> timeoutEventCounter.incrementAndGet() ) );
        
        UdpTransport client = UdpTransport.connect("127.0.0.1", port);
        client.getFlow().map(p -> new String(p.getData())).subscribe(System.out::println);
        client.getFlow().map(p -> new String(p.getData())).subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
                
        TimeUnit.MILLISECONDS.sleep(1000);

        client.send(TEST_STRING.getBytes());

        TimeUnit.MILLISECONDS.sleep(1000);

        server.close();
        
        testObs.assertValueCount(3);
        testObs.assertValueAt(2, TEST_STRING);        
                
        System.out.println("timeoutEventCounter: " + timeoutEventCounter.get());
        
        assertTrue( timeoutEventCounter.get() == 2 );
    }
    
}
