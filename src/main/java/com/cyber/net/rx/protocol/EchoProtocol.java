/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.protocol;

import com.cyber.net.rx.ADuplexFlow;

/**
 *
 * @author CyberManic
 */
public class EchoProtocol<T> extends ADuplexFlow<T> implements IProtocol<T>{

    public EchoProtocol(){
        System.out.println(this + " created");        
        
        getDownstream()
            .doOnNext( data ->  System.out.println("proto.echo.in: " + data.toString()))
            .doOnSubscribe( sub -> System.out.println("proto.echo.in.subscribe: " + sub ) )
            .doOnError( err -> System.out.println("proto.echo.in.error: " + err.toString()))
            .doOnTerminate( () -> System.out.println("proto.echo.in: completed") )
            .subscribe();
        
        getDownstream().subscribeWith( getUpstream() );        
    }

    @Override
    public String toString(){
        return "EchoProtocol";
    }
    
}
