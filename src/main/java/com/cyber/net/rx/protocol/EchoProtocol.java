/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.protocol;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public class EchoProtocol implements IProtocol{

    final Subject<byte[]> flow = PublishSubject.create();
    
    public EchoProtocol(){
        System.out.println(this + " created");        
    }

    @Override
    public Observable<byte[]> getFlow(){
        return flow;
    }
    
    @Override
    public String toString(){
        return "EchoProtocol";
    }

    @Override public void onNext(byte[] data) {
        System.out.println("proto.echo.input: " + data + " [" + data.length + "]");
        flow.onNext(data);
    }

    @Override public void onSubscribe(Disposable d) {
        System.out.println("proto.echo.subscribe: " + d);
    }

    @Override public void onError(Throwable ex) {
        System.out.println("proto.echo.input.error: " + ex.toString());
        flow.onError(ex);
    }

    @Override public void onComplete() {
        System.out.println("proto.echo.input: completed");
        flow.onComplete();
    }
    
}
