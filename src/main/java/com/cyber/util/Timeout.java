/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.lang.IllegalArgumentException;

/**
 *
 * @author CyberManic
 */
public class Timeout implements Predicate<Long>{

    private final long timeoutNanos;
    
    public Timeout(long milliseconds){
        if (milliseconds < 0) throw new IllegalArgumentException("timeout value must be positive");
        timeoutNanos = TimeUnit.MILLISECONDS.toNanos(milliseconds);
    }

    /**
     * @return timeout value in milliseconds
     */
    public long getTimeoutValue(){
        return TimeUnit.NANOSECONDS.toMillis(timeoutNanos);
    }
    
    /**
     * @param lastActivityNanos last activity time
     * @return true is timeout
     */
    public boolean isTimeout(long lastActivityNanos){
        return System.nanoTime() - lastActivityNanos > timeoutNanos;
    };

    /**
     * @param lastActivityNanos last activity time
     * @return true if timeout
     */
    @Override
    public boolean test(Long lastActivityNanos) {
        return isTimeout(lastActivityNanos);
    }
    
}
