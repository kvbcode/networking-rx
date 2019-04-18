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
package com.cyber.net.proto;

import com.cyber.net.rx.protocol.EchoProtocol;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Kirill Bereznyakov
 */
public class EchoProtocolTest {
    
    EchoProtocol<String> proto;
    String TEST_STRING = "THIS_IS_TEST_STRING";
    TestObserver<String> testObs = new TestObserver<>();
    
    public EchoProtocolTest() {
    }
    
    @Before
    public void setUp() {
        proto = new EchoProtocol<>();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testInputOutput(){
        System.out.println("testInputOutput()");
                       
        proto.bind(Observable.just(TEST_STRING))
            .getFlow().subscribeWith( testObs );
        
        System.out.println("return: " + testObs.values());
        testObs.awaitCount(1).assertValue(TEST_STRING);
    }
    
}
