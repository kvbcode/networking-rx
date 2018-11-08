/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import io.reactivex.Observable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.net.ConnectException;

/**
 *
 * @author CyberManic
 */
public class ChannelsStorage<T>{

    private final ConcurrentHashMap<T, IChannel> map;
    
    public ChannelsStorage(){
        map = new ConcurrentHashMap<>();
    }

    public IChannel get(T key){
        return map.get(key);
    }

    public void put(T key, IChannel connection){
        map.put(key, connection);
    }
    
    public void remove(T key){
        IChannel conn = map.remove(key);
        conn.close();
    }

    public Observable<Entry<T, IChannel>> iterate(){
        return Observable.fromIterable(map.entrySet());
    }
    
    public void close(){
        iterate()
            .subscribe(e -> e.getValue().close());
    }
}
