/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.proto;

/**
 *
 * @author CyberManic
 */
public class EchoProtocol<T> extends AProtocol<T>{

    public EchoProtocol(){
        System.out.println("EchoProtocol() created");
        
        inputSlot()
            .doOnError(ex -> System.out.println("proto.echo.error: " + ex.toString()))
            .doOnTerminate(() -> System.out.println("proto.echo: terminated") )
            .doOnSubscribe( s -> System.out.println("proto.echo.subscribe: " + s) )
            .subscribe(p -> {
                System.out.println("proto.input: " + p.toString());
                outputSlot().onNext(p);
            });
    }

    @Override
    public String toString(){
        return "EchoProtocol";
    }
    
}
