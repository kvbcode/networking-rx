/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.UdpConnection;
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

    public static UdpConnection connect(SocketAddress remoteSocketAddress) throws SocketException{
        UdpTransport udp = UdpTransport.connect( remoteSocketAddress );        
        UdpConnection conn = new UdpConnection( udp.getWriter(), remoteSocketAddress );
        
        return conn;
    }

    public static UdpConnection connect(String host, int port) throws SocketException{
        return UdpClient.connect( new InetSocketAddress( host, port ) );        
    }
    
    
}
