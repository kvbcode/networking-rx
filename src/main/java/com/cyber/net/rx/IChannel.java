/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx;

/**
 *
 * @author CyberManic
 */
public interface IChannel extends IDuplexFlow<byte[]>{
    
    public long getLastActivityNanos();
    
    public void close();
    
}
