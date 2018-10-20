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
public class ConnectionStorage<T>{

    private final ConcurrentHashMap<T, IConnection> map;
    
    public ConnectionStorage(){
        map = new ConcurrentHashMap<>();
    }

    public IConnection get(T key){
        return map.get(key);
    }

    public void put(T key, IConnection connection){
        map.put(key, connection);
    }
    
    public void remove(T key){
        IConnection conn = map.remove(key);
        conn.onError(new ConnectException(conn + " timeout"));
    }

    public Observable<Entry<T, IConnection>> iterate(){
        return Observable.fromIterable(map.entrySet());
    }
    
    public void close(){
        iterate()
            .subscribe(e -> e.getValue().onComplete());
    }
}
