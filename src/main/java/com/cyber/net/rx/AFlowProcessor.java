/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
abstract class AFlowProcessor<I,O> implements IFlowProcessor<I,O>{

    protected final Subject<O> flow = PublishSubject.create();
    
    @Override public void onSubscribe(Disposable d) { flow.onSubscribe(d); }

    @Override public void onError(Throwable e) { flow.onError(e); }

    @Override public void onComplete() { flow.onComplete(); }

    @Override public Observable<O> getFlow() { return flow; };

    

}
