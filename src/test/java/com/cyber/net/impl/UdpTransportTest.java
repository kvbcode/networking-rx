/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.impl;

import com.cyber.net.proto.EchoProtocol;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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

        server.inputSlot()
            .subscribe(p -> System.out.println("server.in: " + new String(p.getData()) + " from " + p.getRemoteSocketAddress()));
        
        client.inputSlot()
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
        
        server.inputSlot()
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
        proto.bind( server );
        
        System.out.println("server: " + server);
        System.out.println("client: " + client);
        System.out.println("proto: " + proto);        
                
        client.inputSlot()
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
        proto.bind( server );        
        
        System.out.println("server: " + server);
        System.out.println("client: " + client);
        System.out.println("proto: " + proto);        
        
        client.send(TEST_STRING.getBytes());
        client.close();
        
        // ReConnect
        client = UdpTransport.connect(client.getRemoteSocketAddress());
        client.inputSlot()
            .map(p -> new String(p.getData()))
            .subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        testObs.awaitCount(1).assertValueAt(0, TEST_STRING);        
    }
}
