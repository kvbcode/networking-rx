/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net;

import com.cyber.net.dto.RawPacket;
import com.cyber.net.proto.AProtocol;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author CyberManic
 */
public class SessionManager {

    final SortedMap<Integer,ISession> sessions = Collections.synchronizedSortedMap( new TreeMap<>() );
    final SessionFactory sessionFactory;
    
    public SessionManager(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public ISession create( Integer sessionId ){
        Session session = sessionFactory.create( sessionId );
        this.put(session);
        return session;
    }
    
    public Integer put( ISession session ){
        final Integer sessionId = session.getId();
        sessions.put( sessionId, session );  
        return sessionId;
    }
        
    public ISession get( Integer sessionId ){
        return sessions.get( sessionId );
    }
        
    public ISession remove( Integer sessionId ){
        return sessions.remove( sessionId );
    }
    
    public Collection<ISession> getSessions(){
        return sessions.values();
    }
    
}
