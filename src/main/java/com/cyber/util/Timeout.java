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
    
    private Timeout(long timeoutNanos){
        if (timeoutNanos < 0) throw new IllegalArgumentException("timeout value must be positive");
        this.timeoutNanos = timeoutNanos;
    }

    public static Timeout fromNanos(long timeoutNanos){
        return new Timeout(timeoutNanos);
    }

    public static Timeout fromMillis(long timeoutMillis){
        return new Timeout(TimeUnit.MILLISECONDS.toNanos(timeoutMillis));
    }

    public static Timeout fromSeconds(long timeoutSeconds){
        return new Timeout(TimeUnit.SECONDS.toNanos(timeoutSeconds));
    }
    
    /**
     * @return timeout value in milliseconds
     */
    public long getTimeoutValueNanos(){
        return timeoutNanos;
    }

    /**
     * @return timeout value in milliseconds
     */
    public long getTimeoutValueMillis(){
        return TimeUnit.NANOSECONDS.toMillis(timeoutNanos);
    }

    /**
     * @return timeout value in milliseconds
     */
    public long getTimeoutValueSeconds(){
        return TimeUnit.NANOSECONDS.toSeconds(timeoutNanos);
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
