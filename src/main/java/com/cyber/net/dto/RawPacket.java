/* 
 * The MIT License
 *
 * Copyright 2019 Kirill Bereznyakov.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cyber.net.dto;

import java.net.DatagramPacket;
import java.net.SocketAddress;

/**
 *
 * @author Kirill Bereznyakov
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
