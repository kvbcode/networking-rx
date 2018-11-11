/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.protocol;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 *
 * @author CyberManic
 */
public class EchoProtocol<T> implements IProtocol<T>{

    protected Observable<T> flow;
    
    public EchoProtocol(){
        System.out.println(this + " created");                
    }

    public IProtocol<T> bind(Observable<T> downstream){
        
        flow = downstream
            .doOnNext( data ->  System.out.println("proto.echo.in: " + data.toString()))
            .doOnSubscribe( sub -> System.out.println("proto.echo.in.subscribe: " + sub ) )
            .doOnError( err -> System.out.println("proto.echo.in.error: " + err.toString()))
            .doOnTerminate( () -> System.out.println("proto.echo.in: completed") );
        
        return this;
    }
    
    @Override
    public String toString(){
        return "EchoProtocol";
    }

    @Override
    public Observable<T> getFlow() {
        return flow;
    }
    
}
