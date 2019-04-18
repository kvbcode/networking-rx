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
