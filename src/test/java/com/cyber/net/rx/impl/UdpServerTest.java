/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx.impl;

import com.cyber.net.rx.protocol.EchoProtocol;
import io.reactivex.observers.TestObserver;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testSomeMethod() throws Exception{
        TestObserver<String> testObs = new TestObserver<>();
        
        UdpServer server = new UdpServer(port);
        server.setProtocol( EchoProtocol::new ).start();
        
        UdpTransport client = UdpTransport.connect("127.0.0.1", port);
        client.getFlow().map(p -> new String(p.getData())).subscribe(System.out::println);
        client.getFlow().map(p -> new String(p.getData())).subscribeWith(testObs);
        
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
        client.send(TEST_STRING.getBytes());
                
        TimeUnit.SECONDS.sleep(1);

        server.close();
        
        testObs.assertValueCount(3);
        testObs.assertValueAt(2, TEST_STRING);
    }
    
}
