/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import com.cyber.net.rx.DtlsChannelFactory;
import java.net.DatagramSocket;
import java.net.SocketException;
import javax.net.ssl.SSLContext;

/**
 *
 * @author CyberManic
 */
public class DtlsServer extends UdpServer{

    protected final SSLContext context;
    
    public DtlsServer(SSLContext context, DatagramSocket localSocket) throws SocketException {
        super(localSocket);
        this.context = context;
        this.setChannelFactory( new DtlsChannelFactory( context, getWriter() ) );
        
    }

    public DtlsServer(SSLContext context, int port) throws SocketException {
        this(context, new DatagramSocket(port));
    }

    

}
