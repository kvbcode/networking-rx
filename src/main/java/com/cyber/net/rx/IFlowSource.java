/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx;

import io.reactivex.Observable;

/**
 *
 * @author CyberManic
 */
public interface IFlowSource<T> {
    
    public Observable<T> getFlow();
    
}
