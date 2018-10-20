/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 *
 * @author CyberManic
 */
public class Timeout implements Predicate<Long>{

    private final long timeoutNanos;
    
    public Timeout(long milliseconds){
        timeoutNanos = TimeUnit.MILLISECONDS.toNanos(milliseconds);
    }

    /**
     * Return timeout value in milliseconds
     * @return 
     */
    public long getTimeoutValue(){
        return TimeUnit.NANOSECONDS.toMillis(timeoutNanos);
    }
    
    /**
     * Return true is timeout
     * @param lastActivityNanos
     * @return 
     */
    public boolean isTimeout(long lastActivityNanos){
        return System.nanoTime() - lastActivityNanos > timeoutNanos;
    };

    /**
     * Return true if timeout
     * @param lastActivityNanos
     * @return 
     */
    @Override
    public boolean test(Long lastActivityNanos) {
        return isTimeout(lastActivityNanos);
    }
    
}
