/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx;

import com.cyber.net.rx.IFlowSource;
import io.reactivex.Observable;

/**
 *
 * @author CyberManic
 */
public interface IFlowProcessor<T> extends IFlowSource<T>{
    
    public IFlowProcessor<T> bind( Observable<T> flow );
    
}
