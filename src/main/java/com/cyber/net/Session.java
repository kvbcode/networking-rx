/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public class Session<T> extends ARxIOElement<T> implements ISession<T>{

    private final int id;
    private volatile IProtocol protocol;

    private volatile long lastActivityNanos = 0L;
    private final static long ACTIVITY_SHORT_TIMEOUT_NANOS = 2_000_000_000L;
    private final static long ACTIVITY_LONG_TIMEOUT_NANOS = 10_000_000_000L;
    private long activityTimeoutNanos = ACTIVITY_SHORT_TIMEOUT_NANOS;
    
    public Session(int sessionId, IProtocol<T> protocol){
        
        this.id = sessionId;
        this.protocol = protocol;
        
        System.out.println("new " + this);

        inputSlot()
            .doOnNext(v -> this.updateActivity())
            .doOnNext(p -> System.out.println("session.in: " + p) )
            .subscribeWith( protocol.inputSlot() );       
        
        protocol.outputSlot()
            .doOnNext(p -> System.out.println("session.out: " + p) )
            .subscribeWith( outputSlot() );
        
        updateActivity();
    }

    @Override
    public String toString(){
        return "Session(" + protocol + ")";
    }
    
    private void updateActivity(){
        this.lastActivityNanos = System.nanoTime();
    }
    
    @Override
    public int getId() {        
        return id;
    }

    @Override
    public boolean isAlive() {
        return System.nanoTime() - lastActivityNanos < activityTimeoutNanos;
    }
    
    public void close(){
        inputSlot().onComplete();
        outputSlot().onComplete();
    }
    
}
