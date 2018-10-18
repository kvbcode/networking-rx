/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author CyberManic
 */
public class ConnectionStorage<T> {

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
        map.remove(key);
    }
    
}
