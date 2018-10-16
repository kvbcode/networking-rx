/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net;

import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public interface ISession<T> extends IRxIOElement<T>{
    
    public int getId();
    
    public boolean isAlive();
        
}
