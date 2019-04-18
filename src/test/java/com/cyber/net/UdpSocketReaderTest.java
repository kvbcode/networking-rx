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
package com.cyber.net;

import com.cyber.net.rx.UdpSocketWriter;
import com.cyber.net.rx.UdpSocketReader;
import io.reactivex.observers.TestObserver;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Kirill Bereznyakov
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
    public void testPacket() throws Exception{
        System.out.println("testPacket()");
        
        reader.getFlow()
            .map(p -> new String(p.getData()))
            .doOnNext(s -> System.out.println( "received: " + s ))
            .subscribeWith(testObs);
        
        UdpSocketWriter writer = new UdpSocketWriter(new DatagramSocket());
        writer.send(TEST_STRING.getBytes(), new InetSocketAddress("127.0.0.1", PORT));
        writer.send(TEST_STRING.getBytes(), new InetSocketAddress("127.0.0.1", PORT));
        
        System.out.println( "test.in: " + testObs.awaitCount(1).values() );
        testObs.assertValueAt(0, TEST_STRING);
    }
    
}
