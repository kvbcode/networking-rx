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
package com.cyber.net.impl;

import com.cyber.net.dto.RawPacket;
import com.cyber.net.rx.impl.UdpTransport;
import com.cyber.net.rx.protocol.EchoProtocol;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Kirill Bereznyakov
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
