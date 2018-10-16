/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net;

import io.reactivex.Observable;

/**
 *
 * @author CyberManic
 */
public interface IRxProducer<T> {
    
    public Observable<T> outputSlot();
    
}
