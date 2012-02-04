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
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * BD-ROM Part 3-1 5.3.6 SubPlayItem
 */
public class SubPlayItem {
    
    private int id;
    private ClipInfo[] subClipEntries;
    private int spConnectionCondition;
    private boolean isMultiClipEntries;
    private long subPlayItemInTime;
    private long subPlayItemOutTime;
    private int syncPlayItemId;
    private long syncStartPtsOfPlayItem;
    
    public SubPlayItem() {}
    public SubPlayItem(int id) {
        this.id = id;
    }
    
    public void readObject(DataInputStream din) throws IOException {
        // 16 bit length
        // 8*5 clipinfo[0] filename
        // 8*4 clipinfo[0] codec id
        // 27 bit reserved
        // 4 bit sp_connection_condition
        // 1 bit isMultiClipEntries
        // 8 bit clipinfo[0] STC_id
        // 32 bit unsigned SubPlayItem in_time
        // 32 bit unsigned SubPlayItem out_time
        // 16 bit sync_playitem id
        // 32 bit unsigned sync_start_PTS
        // if (isMultiClipEntries)
        // 8 bit number of entries
        // 8 bit reserved
        // for i = 1...N
        // clipinfo[i] filename
        // clipinfo[i] codec id
        // clipinfo[i] STC_id
        
        ArrayList subClipList = new ArrayList();
        String name;
        String codecId;
        int    stdId;
        ClipInfo info;
        byte[] inTimeBytes = new byte[4];
        byte[] outTimeBytes = new byte[4];
        byte[] syncStartPtsBytes = new byte[4];
        
        din.skipBytes(2);        
        name = StringIOHelper.readISO646String(din, 5);
        codecId = StringIOHelper.readISO646String(din, 4);
        din.skipBytes(3);
        byte b = din.readByte();
        setSpConnectionCondition((b & 0x1e)>>1);
        setIsMultiClipEntries((b & 0x01) != 0);  
        stdId = din.readByte();
        din.readFully(inTimeBytes);
        setSubPlayItemInTime(UnsignedIntHelper.convertToLong(inTimeBytes));
        din.readFully(outTimeBytes);
        setSubPlayItemOutTime(UnsignedIntHelper.convertToLong(outTimeBytes));
        setSyncPlayItemId(din.readUnsignedShort());
        din.readFully(syncStartPtsBytes);
        setSyncStartPtsOfPlayItem(UnsignedIntHelper.convertToLong(syncStartPtsBytes));
        info = new ClipInfo(0, name, codecId, stdId);
        subClipList.add(info);
        if (getIsMultiClipEntries()) {
            int length = din.readByte();
            din.skipBytes(1);
            for (int i = 1; i < length; i++) {
                name = StringIOHelper.readISO646String(din, 5);
                codecId = StringIOHelper.readISO646String(din, 4);
                stdId = din.readByte();
                info = new ClipInfo(i, name, codecId, stdId);
                subClipList.add(info);
            }
        }
        ClipInfo[] entries = 
             (ClipInfo[]) subClipList.toArray(new ClipInfo[subClipList.size()]);
        setSubClipEntries(entries);                   
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);
        
        ClipInfo[] infos = getSubClipEntries();
        substream.write(StringIOHelper.getISO646Bytes(infos[0].getClipName()));
        substream.write(StringIOHelper.getISO646Bytes("M2TS"));
        substream.write(new byte[3]);
        int value = getSpConnectionCondition() << 1;
        value |= getIsMultiClipEntries() ? 0x01 : 0 ;
        substream.writeByte(value);
        substream.writeByte(infos[0].getStcId());
        substream.write(UnsignedIntHelper.convertToBytes(getSubPlayItemInTime()));
        substream.write(UnsignedIntHelper.convertToBytes(getSubPlayItemOutTime()));
        substream.writeShort(getSyncPlayItemId());
        substream.write(UnsignedIntHelper.convertToBytes(getSyncStartPtsOfPlayItem()));
        if (getIsMultiClipEntries()) {
            substream.writeByte(infos.length);
            substream.writeByte(0);
            for (int i = 1; i < infos.length; i++) {
                substream.write(StringIOHelper.getISO646Bytes(infos[i].getClipName()));
                substream.write(StringIOHelper.getISO646Bytes("M2TS"));
                substream.writeByte(infos[i].getStcId());
            }
        }
        
        substream.flush();
        substream.close();
        byte[] data = baos.toByteArray();
        
        dout.writeShort(data.length);
        dout.write(data);
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement(name="SubClipInfo")  
    public ClipInfo[] getSubClipEntries() {
        return subClipEntries;
    }

    public void setSubClipEntries(ClipInfo[] subClipEntries) {
        for (int i = 0; i < subClipEntries.length; i++) {
            if (i != subClipEntries[i].getId()) {
                throw new IllegalArgumentException("id does not match the array order");
            }
        }        
        this.subClipEntries = subClipEntries;
    }

    public int getSpConnectionCondition() {
        return spConnectionCondition;
    }

    public void setSpConnectionCondition(int spConnectionCondition) {
       
        if (spConnectionCondition != 1 &&
            spConnectionCondition != 5 &&
            spConnectionCondition != 6) {
           throw new IllegalArgumentException("sp_connection_condition should be either 1, 5 or 6 " + spConnectionCondition);
        }

        this.spConnectionCondition = spConnectionCondition;
    }

    public boolean getIsMultiClipEntries() {
        return isMultiClipEntries;
    }

    public void setIsMultiClipEntries(boolean isMultiClipEntries) {
        this.isMultiClipEntries = isMultiClipEntries;
    }

    public long getSubPlayItemInTime() {
        return subPlayItemInTime;
    }

    public void setSubPlayItemInTime(long subPlayItemInTime) {
        this.subPlayItemInTime = subPlayItemInTime;
    }

    public long getSubPlayItemOutTime() {
        return subPlayItemOutTime;
    }

    public void setSubPlayItemOutTime(long subPlayItemOutTime) {
        this.subPlayItemOutTime = subPlayItemOutTime;
    }

    public int getSyncPlayItemId() {
        return syncPlayItemId;
    }

    public void setSyncPlayItemId(int syncPlayItemId) {
        this.syncPlayItemId = syncPlayItemId;
    }

    public long getSyncStartPtsOfPlayItem() {
        return syncStartPtsOfPlayItem;
    }

    public void setSyncStartPtsOfPlayItem(long syncStartPtsOfPlayItem) {
        this.syncStartPtsOfPlayItem = syncStartPtsOfPlayItem;
    }
}
