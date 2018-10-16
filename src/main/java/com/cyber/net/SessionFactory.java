/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import com.cyber.net.dto.RawPacket;
import com.cyber.net.proto.AProtocol;
import com.cyber.net.proto.EchoProtocol;
import io.reactivex.subjects.Subject;

/**
 *
 * @author CyberManic
 */
public class SessionFactory {

    public SessionFactory(){

    }

    public Session create(int sessionId){
        
        IProtocol<RawPacket> proto = new EchoProtocol<RawPacket>();
        
        return new Session(sessionId, proto);
    }
    
}
