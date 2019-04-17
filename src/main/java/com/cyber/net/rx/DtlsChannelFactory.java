/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx;

import java.net.SocketAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

/**
 *
 * @author CyberManic
 */
public class DtlsChannelFactory extends UdpChannelFactory{

    protected final SSLContext context;
        
    public DtlsChannelFactory(SSLContext context, UdpSocketWriter writer){
        super(writer);
        this.context = context;
    }

    @Override
    public DtlsChannel get(SocketAddress remoteAddress){
        DtlsChannel conn = new DtlsChannel(context, writer, remoteAddress);
        try{
            conn.getDtlsWrapper().useServerMode();
            flow.onNext(conn);
        }catch(SSLException e){
            flow.onError(e);
        }
        return conn;
    }
    
    
}
