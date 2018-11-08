/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyber.net.impl;

import com.cyber.net.rx.impl.TcpServer;
import com.cyber.net.rx.TcpSocketReader;
import com.cyber.net.dto.RawPacket;
import io.reactivex.Observable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.TestObserver;
import java.util.Objects;

/**
 *
 * @author CyberManic
 */
public class TcpServerIT {
    final int PORT = 9999;
    TcpServer server;
    Thread serverThread;
    final String TEST_STRING = "THIS_IS_TEST_STRING";
    CompositeDisposable disp = new CompositeDisposable();
    
    public TcpServerIT(){
    }

    @Before
    public void setUp() throws IOException {
        System.out.println("new TcpServer():" + PORT);       
        server = new TcpServer(PORT);
        serverThread = new Thread(server, server.getClass().getSimpleName());
        serverThread.start();        
    }
    
    @After
    public void shutDown() throws InterruptedException{
        System.out.println("shutdown TcpServer()");
        
        server.shutdown();
        serverThread.interrupt();        
        disp.clear();        
    }
    
    
    @Test
    public void connectionNoData() throws IOException, InterruptedException {        
        System.out.println("connectionNoData()");
        
        disp.add(
            server.observeConnection().subscribe((s) -> System.out.println("new connect: " + s.toString()))
        );
        
        try (Socket socket = new Socket("127.0.0.1", PORT)) {
            assertTrue(socket.isConnected());
        }
    }
    
    @Test
    public void connectionWithData() throws IOException, InterruptedException {        
        System.out.println("connectionWithData()");
        

        // читаем данные из входного потока через Scanner
        Observable<String> reader = server.observeConnection()
                .map( socket -> socket.getInputStream() )
                .map( inStream -> new Scanner(inStream).next() );
                        
        // подписываем тестового наблюдателя
        TestObserver<String> testObs = new TestObserver<>();
        disp.add( reader.subscribe( testObs::onNext ) );
        
        // соединяемся с сервером и отсылаем байты тестовой строки
        try (
            Socket socket = new Socket("127.0.0.1", PORT);
            OutputStream out = socket.getOutputStream()
        ){
            out.write(TEST_STRING.getBytes());
            out.flush();
        }
        
        // синхронизируемся со средой выполнения теста и проверяем значение        
        testObs.awaitCount(1).assertValue(TEST_STRING);
    }

    @Test
    public void connectionReaderWithData() throws IOException, InterruptedException {        
        System.out.println("connectionReaderWithData()");
        
        TestObserver<String> testObs = new TestObserver<>();
        
        disp.add( server.observeConnection()
            .subscribe( socket  -> {
                TcpSocketReader reader = new TcpSocketReader(socket);
                reader.inputSlot()
                    .map(p -> new String(p.getData()))
                    .subscribeWith( testObs );
                new Thread(reader).start();
            })
        );
        
        // соединяемся с сервером и отсылаем байты тестовой строки
        try (
            Socket socket = new Socket("127.0.0.1", PORT);
            OutputStream out = socket.getOutputStream()
        ){
            out.write(TEST_STRING.getBytes());
            out.flush();
        }
        
        // синхронизируемся со средой выполнения теста и проверяем значение        
        testObs.awaitCount(1).assertValue(TEST_STRING);
    
    }
}
