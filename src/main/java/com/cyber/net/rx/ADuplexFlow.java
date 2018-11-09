/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public class ADuplexFlow<T> implements IDuplexFlow<T>{

    protected Subject<T> downstream = PublishSubject.create();
    protected Subject<T> upstream = PublishSubject.create();

    @Override
    public Subject<T> getDownstream() {
        return downstream;
    }

    @Override
    public Subject<T> getUpstream() {
        return upstream;
    }        
    
}
