/*  
 * Copyright (c) 2008, Sun Microsystems, Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *  Note:  In order to comply with the binary form redistribution 
 *         requirement in the above license, the licensee may include 
 *         a URL reference to a copy of the required copyright notice, 
 *         the list of conditions and the disclaimer in a human readable 
 *         file with the binary form of the code that is subject to the
 *         above license.  For example, such file could be put on a 
 *         Blu-ray disc containing the binary form of the code or could 
 *         be put in a JAR file that is broadcast via a digital television 
 *         broadcast medium.  In any event, you must include in any end 
 *         user licenses governing any code that includes the code subject 
 *         to the above license (in source and/or binary form) a disclaimer 
 *         that is at least as protective of Sun as the disclaimers in the 
 *         above license.
 * 
 *         A copy of the required copyright notice, the list of conditions and
 *         the disclaimer will be maintained at 
 *         https://hdcookbook.dev.java.net/misc/license.html .
 *         Thus, licensees may comply with the binary form redistribution
 *         requirement with a text file that contains the following text:
 * 
 *             A copy of the license(s) governing this code is located
 *             at https://hdcookbook.dev.java.net/misc/license.html
 */


package net.java.bd.tools.playlist;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*
 * BD-ROM Part 3-1 5.3.2 AppInfoPlayList object. 
 */
public class AppInfoPlayList {

    PlaybackType playbackType;
    int playbackCount;
    UOMaskTable uoMaskTable = new UOMaskTable();
    boolean playListRandomAccessFlag;
    boolean audioMixAppFlag;
    boolean losslessFlag;
    
    public void setPlaybackType(PlaybackType type) {
        this.playbackType = type;
    }
    public PlaybackType getPlaybackType() {
        return playbackType;
    }
    public void setPlaybackCount(int count) {
        if (getPlaybackType() == PlaybackType.RANDOM || 
                getPlaybackType() == PlaybackType.SHUFFLE) {
            if (count < 1) { // make sure the value is correct
               throw new IllegalArgumentException("Playback should be >= 1");
            }
        }
        this.playbackCount = count;
    }
    public int getPlaybackCount() {
        return playbackCount;
    }
    public void setUOMaskTable(UOMaskTable table) {
        this.uoMaskTable = table;
    }
    public UOMaskTable getUOMaskTable() {
        return uoMaskTable;
    }
    public void setPlayListRandomAccessFlag(boolean flag) {
        this.playListRandomAccessFlag = flag;
    }
    public boolean getPlayListRandomAccessFlag() {
        return this.playListRandomAccessFlag;
    }
    public void setAudioMixAppFlag(boolean flag) {
        this.audioMixAppFlag = flag;
    }
    public boolean getAudioMixAppFlag() {
        return this.audioMixAppFlag;
    }
    public void setLosslessFlag(boolean flag) {
        this.losslessFlag = flag;
    }
    public boolean getLosslessFlag() {
        return this.losslessFlag;
    }
    
    public void readObject(DataInputStream din) throws IOException {
        // 32 bit length
        // 8 bit reserved
        // 8 bit playback_type
        // 16 bit playback_count
        // UOMaskTable
        // 1 bit PlayListRandomAccessFlag
        // 1 bit AudioMixAppFlag
        // 1 bit LosslessMayBybassMixerFlag
        // 13 bit reserved
        
        din.skipBytes(5);
        int type = din.readUnsignedByte();
        int count = din.readUnsignedShort();
        Enum[] playbackTypes = PlaybackType.values();  
        for (int i = 0; i < playbackTypes.length; i++ ) {
           if (playbackTypes[i].ordinal() == type) {
                setPlaybackType((PlaybackType)playbackTypes[i]);
                break;
           }   
        } 
        setPlaybackCount(count);         
        uoMaskTable.readObject(din);
        
        byte b = din.readByte();
        din.skipBytes(1);
        setPlayListRandomAccessFlag((b & 0x80) != 0);
        setAudioMixAppFlag((b & 0x40) != 0); 
        setLosslessFlag((b & 0x20) != 0);
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);
        
        // 32 bit length
        // 8 bit reserved
        // 8 bit playback_type
        // 16 bit playback_count
        // UOMaskTable
        // 1 bit PlayListRandomAccessFlag
        // 1 bit AudioMixAppFlag
        // 1 bit LosslessMayBybassMixerFlag
        // 13 bit reserved        
        
        substream.writeByte(0);
        substream.writeByte(getPlaybackType().ordinal());
        substream.writeShort(getPlaybackCount());
        uoMaskTable.writeObject(substream);        
        byte b = 0;
        if (getPlayListRandomAccessFlag()) {
            b |= 0x80;
        } 
        if (getAudioMixAppFlag()) {
            b |= 0x40;
        }
        if (getLosslessFlag() ) {
            b |= 0x20;
        }
        substream.writeByte(b);
        substream.writeByte(0);   
        
        substream.flush();
        substream.close();
        
        byte[] data = baos.toByteArray();
        dout.writeInt(data.length);
        dout.write(data);
    } 
    
    public enum PlaybackType {

        RESERVED,
        SEQUENTIAL,
        RANDOM,
        SHUFFLE;

        public byte getEncoding() {
            return (byte) ordinal();
        }
    }     
}
