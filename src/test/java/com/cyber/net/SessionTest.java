/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net;

import com.cyber.net.dto.RawPacket;
import com.cyber.net.proto.EchoProtocol;
import com.cyber.net.proto.NullProtocol;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author CyberManic
 */
public class SessionTest {
    
    Session session;
    
    Observable<String> source;
    TestObserver<String> testObs;
    
    final String[] TEST_VALUES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    
    public SessionTest() {
    }
    
    @Before
    public void setUp() {
        session = new Session(0, new NullProtocol());
        source = Observable.fromArray(TEST_VALUES)
            .doOnNext(s -> System.out.println("source.emit: " + s));
        testObs = new TestObserver<>();
    }
    
    @After
    public void tearDown() {
        session = null;
    }

    @Test
    public void testIsAliveOnStart() {
        System.out.println("testIsAliveOnStart()");
        assertTrue(session.isAlive());
    }
        
    //@Test
    public void testIsAlive() throws InterruptedException{
        System.out.println("testIsAlive()");
        System.out.println("wait 11 sec (default timeout=10sec)");
        TimeUnit.SECONDS.sleep(11);
        assertFalse(session.isAlive());
    }
    
    @Test
    public void testInput() throws InterruptedException{
        System.out.println("testInput()");
        
        session.inputSlot()
            .doOnNext(s -> {
                System.out.println("received for local consumer: " + s);                
            }).subscribeWith(testObs);
        
        source.subscribeWith(session.inputSlot());

        testObs
            .awaitCount(TEST_VALUES.length)
            .assertValues( TEST_VALUES );
    }
    
    @Test
    public void testOutput() throws InterruptedException{
        System.out.println("testOutput()");
        
        session.outputSlot()
            .doOnNext(s -> {
                System.out.println("send to remote client: " + s);                
            }).subscribeWith(testObs);
        
        source.subscribeWith(session.outputSlot());

        testObs
            .awaitCount(TEST_VALUES.length)
            .assertValues( TEST_VALUES );
    }
    
    @Test
    public void testEcho() throws InterruptedException{
        System.out.println("testEcho()");
        
        session.inputSlot()
            .doOnNext(s -> {
                System.out.println("(input) received for local consume: " + s);                
            }).subscribeWith( session.outputSlot() );

        session.outputSlot()
            .doOnNext(s -> {
                System.out.println("(output) send to remote client: " + s);                
            }).subscribeWith(testObs);
                
        source.subscribeWith(session.inputSlot());

        testObs
            .awaitCount(TEST_VALUES.length)
            .assertValues( TEST_VALUES );
    }
    
    
}
