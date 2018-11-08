/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx;

import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public interface IFlowDuplex<T> {
    
    Subject<T> getDownstream();
    
    Subject<T> getUpstream();
    
}
