/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net;

import com.cyber.net.dto.RawPacket;
import com.cyber.net.impl.UdpTransport;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author CyberManic
 */
public class IntegrationTest {
    
    int PORT = 19999;
    String TEST_STRING = "THIS_IS_TEST_STRING";
    TestObserver<String> testObs = new TestObserver<>();
    SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", PORT);
    
    
    public IntegrationTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    //@Test
    public void testIntegrationNoNetwork() throws Exception{
        System.out.println("testIntegrationNoNetwork()");
        
        RawPacket packet = new RawPacket( remoteAddress, TEST_STRING.getBytes() );
                
        SessionFactory sessionFactory = new SessionFactory();
        SessionManager sessionManager = new SessionManager(sessionFactory);
        PacketDispatcher<RawPacket> dispatcher = new PacketDispatcher<>(sessionManager);        
        
        dispatcher.outputSlot()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);
        
        dispatcher.inputSlot().onNext(packet);
        dispatcher.inputSlot().onNext(packet);
        dispatcher.inputSlot().onNext(packet);
        dispatcher.inputSlot().onComplete();

        System.out.println("returned result: " + testObs.awaitCount(1).values() );
        
    }

    @Test
    public void testIntegrationWithNetwork() throws Exception{
        System.out.println("testIntegrationWithNetwork()");
        
        
        RawPacket packet = new RawPacket( remoteAddress, TEST_STRING.getBytes() );

        
        System.out.println("construct server...");
        
        DatagramSocket localServerSocket = new DatagramSocket(PORT);
        UdpSocketWriter serverSocketWriter = new UdpSocketWriter(localServerSocket);
        UdpSocketReader serverSocketReader = new UdpSocketReader(localServerSocket);        
        new Thread( serverSocketReader ).start();
        
        SessionFactory sessionFactory = new SessionFactory();
        SessionManager sessionManager = new SessionManager(sessionFactory);
        PacketDispatcher<RawPacket> dispatcher = new PacketDispatcher<>(sessionManager);        
        
        System.out.println("bind server components...");        
        
        dispatcher.outputSlot().subscribeWith( serverSocketWriter );
        serverSocketReader.outputSlot().subscribeWith( dispatcher.inputSlot() );
        
        /*
        dispatcher.outputSlot()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);
        
        System.out.println("construct client...");        
        UdpSocketWriter client = new UdpSocketWriter( "127.0.0.1", PORT );
        
        System.out.println("send data: " + TEST_STRING);        
        client.send(TEST_STRING.getBytes());
        */      
        
        Observable<String> testData = Observable.fromArray(TEST_STRING, TEST_STRING, TEST_STRING);
        
        UdpTransport client = UdpTransport.connect("127.0.0.1", PORT);
        client.inputSlot()
            .map(p -> new String(p.getData()))
            .subscribe(s -> System.out.println("client.in: " + s));
        
        client.inputSlot()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);
        
        testData
            .map(s -> s.getBytes())
            .map(data -> new RawPacket( new InetSocketAddress("127.0.0.1", PORT), data ))
            .subscribeWith( client.outputSlot() );
        
        
        System.out.println("returned result: " + testObs.awaitCount(1).values() );
        
        serverSocketReader.shutdown();
        //localServerSocket.close();
    }

    
}
