/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.UdpChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 *
 * @author CyberManic
 */
public class UdpClient {

    private UdpClient(){

    }

    public static UdpChannel connect(SocketAddress remoteSocketAddress) throws SocketException{
        UdpTransport udp = UdpTransport.connect( remoteSocketAddress );        
        UdpChannel conn = new UdpChannel( udp.getWriter(), remoteSocketAddress );

        udp.getFlow()
            .map(p -> p.getData())
            .subscribeWith(conn.getDownstream());
        
        return conn;
    }

    public static UdpChannel connect(String host, int port) throws SocketException{
        return UdpClient.connect( new InetSocketAddress( host, port ) );        
    }
    
    
}
