/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.impl;

import com.cyber.net.rx.impl.UdpTransport;
import com.cyber.net.rx.protocol.EchoProtocol;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author CyberManic
 */
public class UdpTransportTest {
    final int PORT = 19999;
    final String TEST_STRING = "THIS_IS_TEST_STRING";

    UdpTransport server;
    UdpTransport client;
    TestObserver<String> testObs;
        
    public UdpTransportTest() {
    }
    
    @Before
    public void setUp() throws IOException{
        testObs = new TestObserver<>();
        server = UdpTransport.listen(PORT);        
        client = UdpTransport.connect("127.0.0.1", PORT);        

        server.getFlow()
            .subscribe(p -> System.out.println("server.in: " + new String(p.getData()) + " from " + p.getRemoteSocketAddress()));
        
        client.getFlow()
            .subscribe(p -> System.out.println("client.in: " + new String(p.getData()) + " from " + p.getRemoteSocketAddress()));
        
    }
    
    @After
    public void tearDown() {
        server.close();
        client.close();
    }

    @Test
    public void testServerInput() throws IOException{
        System.out.println("\ntestServerInput()");

        System.out.println("server: " + server);
        System.out.println("client: " + client);
        
        server.getFlow()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);

        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        
        testObs.awaitCount(1).assertValueAt(0, TEST_STRING);
    }
 
    @Test
    public void testEcho() throws Exception{
        System.out.println("\ntestEcho()");

        EchoProtocol proto = new EchoProtocol();
        server.getFlow().subscribe(p -> proto.onNext(p.getData()) );
        
        System.out.println("server: " + server);
        System.out.println("client: " + client);
        System.out.println("proto: " + proto);        
                
        client.getFlow()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);

        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        
        testObs.awaitCount(1).assertValueAt(0, TEST_STRING);
    }
    
    @Test
    public void testReConnect() throws Exception{
        System.out.println("\ntestReConnect()");
        
        EchoProtocol proto = new EchoProtocol();
        server.getFlow().subscribe(p -> proto.onNext(p.getData()) );
        
        System.out.println("server: " + server);
        System.out.println("client: " + client);
        System.out.println("proto: " + proto);        
        
        client.send(TEST_STRING.getBytes());
        client.close();
        
        // ReConnect
        client = UdpTransport.connect(client.getRemoteSocketAddress());
        client.getFlow()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        testObs.awaitCount(1).assertValueAt(0, TEST_STRING);        
    }
}
