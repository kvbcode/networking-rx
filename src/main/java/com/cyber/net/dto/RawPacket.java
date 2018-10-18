/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyber.net.dto;

import java.net.DatagramPacket;
import java.net.SocketAddress;

/**
 *
 * @author CyberManic
 */
public class RawPacket{
    private final SocketAddress remoteSocketAddress;
    private final byte[] data;
    
    /**
     * При создании производит копирование данных из dataBuf
     * @param remoteSocketAddress адрес клиента
     * @param dataBuf ссылка на буфер с данными клиента
     * @param dataBufOffset смещение начала буфера
     * @param dataLength размер данных
     */
    public RawPacket( SocketAddress remoteSocketAddress, byte[] dataBuf, int dataBufOffset, int dataLength){
        this.remoteSocketAddress = remoteSocketAddress;
        this.data = new byte[dataLength];
        System.arraycopy(dataBuf, dataBufOffset, this.data, 0, dataLength);
    }

    /**
     * При создании ссылается на dataBuf без собственной копии
     * @param remoteSocketAddress адрес клиента
     * @param dataBuf ссылка на буфер с данными клиента
     */
    public RawPacket( SocketAddress remoteSocketAddress, byte[] dataBuf ){
        this.remoteSocketAddress = remoteSocketAddress;
        this.data = dataBuf;
    }
    
        
    public static RawPacket from( DatagramPacket dg ){
        return new RawPacket( dg.getSocketAddress(), dg.getData(), dg.getOffset(), dg.getLength() );
    }
    
    public SocketAddress getRemoteSocketAddress(){        
        return remoteSocketAddress;
    }
    
    public byte[] getData(){
        return data;
    }
    
    @Override
    public String toString(){
        return "RawPacket@" + Integer.toHexString(hashCode()) + " [" + remoteSocketAddress + ", " + data.length + ")";
    }
    
}
