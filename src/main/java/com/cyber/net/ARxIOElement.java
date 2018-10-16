/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public abstract class ARxIOElement<T> implements IRxIOElement<T>{

    private final Subject<T> inputSlot = PublishSubject.create();    
    private final Subject<T> outputSlot = PublishSubject.create();    
    
    @Override
    public Subject<T> inputSlot() {
        return inputSlot;
    }

    @Override
    public Subject<T> outputSlot() {
        return outputSlot;
    }

    public ARxIOElement<T> bind(ARxIOElement<T> parentElement){
        bind( parentElement.inputSlot(), parentElement.outputSlot() );
        return parentElement;
    }
    
    public void bind(Observable<T> input, Observer<T> output){
        input.subscribe( inputSlot() );
        outputSlot().subscribe( output );        
    }
    
}
