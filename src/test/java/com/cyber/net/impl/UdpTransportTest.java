/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.impl;

import com.cyber.net.dto.RawPacket;
import com.cyber.net.rx.impl.UdpTransport;
import com.cyber.net.rx.protocol.EchoProtocol;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
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
        System.out.println("\nsetup()...");
        testObs = new TestObserver<>();
        server = UdpTransport.listen(PORT);        
        client = UdpTransport.connect("127.0.0.1", PORT);                
    }
    
    @After
    public void tearDown() {
        System.out.println("...tearDown()");
        server.close();
        client.close();
    }

    @Test
    public void testServerInput() throws IOException{
        System.out.println("testServerInput()");

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
        System.out.println("testEcho()");

        EchoProtocol<RawPacket> proto = new EchoProtocol<>();
        proto.bind(server.getFlow())
            .getFlow().subscribeWith(server);                
        
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
        System.out.println("testReConnect()");
        
        EchoProtocol proto = new EchoProtocol();
        proto.bind(server.getFlow())
            .getFlow().subscribeWith(server);                
        
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
