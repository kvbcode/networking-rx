/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import java.util.Objects;

/**
 *
 * @author CyberManic
 */
public class PacketDispatcher<T extends IPacket> extends ARxIOElement<T>{

    private final SessionManager sessionManager;
    
    public PacketDispatcher(SessionManager sessionManager){
        this.sessionManager = sessionManager;
        
        inputSlot().subscribe(this::onNextInput);
    }

    public static int getClientId(IPacket packet){
        return Objects.hash(packet.getRemoteSocketAddress());
    }
    
    private void onNextInput(T packet){
        int sessionId = getClientId(packet);
        
        ISession session = sessionManager.get(sessionId);
        if (session==null) session = sessionManager.create( sessionId );        
            
        session.inputSlot().onNext(packet);
        session.outputSlot().subscribeWith( this.outputSlot() );
    }
        
}
