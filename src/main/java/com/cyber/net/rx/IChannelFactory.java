/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.rx;

import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public interface IChannelFactory<T> extends IFlowSource<T>{
    
    T get(SocketAddress remoteAddress);
    
}