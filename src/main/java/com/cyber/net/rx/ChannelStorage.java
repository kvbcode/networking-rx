/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import io.reactivex.Observable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author CyberManic
 */
public class ChannelStorage<T>{

    private final ConcurrentHashMap<T, IChannel> map;
    
    public ChannelStorage(){
        map = new ConcurrentHashMap<>();
    }

    public IChannel get(T key){
        return map.get(key);
    }

    public void put(T key, IChannel ch){
        map.put(key, ch);
    }
    
    public void remove(T key){
        IChannel ch = map.remove(key);
        ch.close();
    }

    public Observable<Entry<T, IChannel>> iterate(){
        return Observable.fromIterable(map.entrySet());
    }
    
    public void close(){
        iterate()
            .subscribe(e -> e.getValue().close());
    }
}
