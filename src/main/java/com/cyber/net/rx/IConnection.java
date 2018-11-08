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
public interface IConnection extends IFlowDuplex<byte[]>{
    
    public long getLastActivityTime();
    
    public void close();
    
}
