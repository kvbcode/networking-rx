/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.DtlsChannel;
import com.cyber.net.rx.UdpChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

/**
 *
 * @author CyberManic
 */
public class DtlsClient {

    private DtlsClient(){

    }

    public static DtlsChannel connect(SSLContext context, SocketAddress remoteSocketAddress) throws SocketException, SSLException{
        UdpTransport udp = UdpTransport.connect( remoteSocketAddress );        
        DtlsChannel ch = new DtlsChannel( context, udp.getWriter(), remoteSocketAddress );
        ch.getDtlsWrapper().useClientMode();

        udp.getFlow()
            .map(p -> p.getData())
            .subscribe(dataIn -> ch.accept(dataIn));
        
        return ch;
    }

    public static DtlsChannel connect(SSLContext context, String host, int port) throws SocketException, SSLException{
        return DtlsClient.connect( context, new InetSocketAddress( host, port ) );        
    }
    
    
}
