/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net;

import com.cyber.net.rx.UdpSocketWriter;
import com.cyber.net.rx.UdpSocketReader;
import com.cyber.net.dto.RawPacket;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author CyberManic
 */
public class UdpSocketReaderTest {
    
    final int PORT = 19999;
    final String TEST_STRING = "THIS_IS_TEST_STRING";
    DatagramSocket serverSocket;
    UdpSocketReader reader;
    TestObserver<String> testObs;
    
    public UdpSocketReaderTest() {
    }
    
    @Before
    public void setUp() throws IOException{
        serverSocket = new DatagramSocket(PORT);
        reader = new UdpSocketReader(serverSocket);
        new Thread(reader).start();
        testObs = new TestObserver<>();        
    }
    
    @After
    public void tearDown() {
        reader.close();
        serverSocket.close();
    }

    @Test
    public void testPacket() throws IOException{
        System.out.println("testPacket()");
        
        reader.getFlow()
            .map(p -> new String(p.getData()))
            .doOnNext(s -> System.out.println( "received: " + s ))
            .subscribeWith(testObs);
                
        UdpSocketWriter client = new UdpSocketWriter(new DatagramSocket());
        client.onNext(new RawPacket(new InetSocketAddress("127.0.0.1", PORT), TEST_STRING.getBytes()));
        
        System.out.println( "test.in: " + testObs.awaitCount(1).values() );
        testObs.assertValue(TEST_STRING);
    }
    
}
