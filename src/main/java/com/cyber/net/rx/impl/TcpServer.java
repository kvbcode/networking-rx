/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.rx.impl;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ServerSocketFactory;


/**
 *
 * @author CyberManic
 */
public class TcpServer implements Runnable{
    
    private final ServerSocket serverSocket;
    private final int port;

    private final PublishSubject<Socket> publishSocket = PublishSubject.create();
    private volatile boolean shutdown = false;
    
    public TcpServer(int port) throws IOException{
        this.port = port;
        this.serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);  
        this.serverSocket.setReuseAddress(true);
    }

    /**
     * Выдает Socket нового подключения
     * @return Observable(Socket)
     */
    public Observable<Socket> observeConnection(){
        return publishSocket;
    }
    
    public void shutdown(){
        shutdown = true;
        try{
            serverSocket.close();
        }catch(IOException ex){            
        }
    }
    
    @Override
    public void run() {
        shutdown = false;
        try{
            while(!shutdown && !Thread.currentThread().isInterrupted()){
                publishSocket.onNext( serverSocket.accept() );
            }
            serverSocket.close();
        }catch(IOException ex){
            publishSocket.onError(ex);
        }finally{
            publishSocket.onComplete();
        }
        System.out.flush();
    }

}
