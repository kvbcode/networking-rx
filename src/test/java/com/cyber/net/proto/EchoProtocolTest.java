/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.proto;

import com.cyber.net.dto.RawPacket;
import io.reactivex.observers.TestObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author CyberManic
 */
public class EchoProtocolTest {
    
    EchoProtocol proto;
    String TEST_STRING = "THIS_IS_TEST_STRING";
    TestObserver<String> testObs = new TestObserver<>();
    
    public EchoProtocolTest() {
    }
    
    @Before
    public void setUp() {
        proto = new EchoProtocol<RawPacket>();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testInputOutput(){
        System.out.println("testInputOutput()");
        
        proto.outputSlot().subscribeWith(testObs);
        
        proto.inputSlot().onNext(TEST_STRING);
        
        System.out.println("return: " + testObs.values());
        testObs.awaitCount(1).assertValue(TEST_STRING);
    }
    
}
