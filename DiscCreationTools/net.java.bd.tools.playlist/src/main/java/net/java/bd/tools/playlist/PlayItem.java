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
 * BD-ROM Part 3-1 5.3.4 PlayItem
 */
public class PlayItem {
    private ClipInfo[] angles;
    private boolean isMultiAngle;
    private int connectionCondition;
    private long inTime;
    private long outTime;
    private UOMaskTable uoMaskTable = new UOMaskTable();
    private boolean playItemRandomAccessFlag;
    private int stillMode;
    private int stillTime;
    private boolean isDifferentAudios;
    private boolean isSeamlessAngleChange;
    private STNTable stnTable = new STNTable();
    private int id;
    
    public PlayItem() {}
    public PlayItem(int id) {
        this.id = id;
    }
    
    public void readObject(DataInputStream din) throws IOException {
        // 16 bit length
        // 8*5 ClipInfo[0] clipinfo filename
        // 8*4 ClipInfo[0] clip codec id
        // 11 bit reserved
        // 1 bit isMultiAngle
        // 4 bit connectionCondition
        // 8 bit ClipInfo[0] STC id ref
        // 32 bit unsigned inTime
        // 32 bit unsigned outTime
        // UOMaskTable
        // 1 bit playItemRandomAccessFlag
        // 7 bit reserve
        // 8 bit stillMode
        // 16 bit stillTime
        // if isMultiAngle=true, 1-N Angles
        // STNTAble
        
        ArrayList angleList = new ArrayList();
        String clipName;
        String codecId;
        int stcId;
        byte b;
        byte[] inTimeBytes  = new byte[4];
        byte[] outTimeBytes = new byte[4];
       
        din.skipBytes(2); // length
        clipName = StringIOHelper.readISO646String(din, 5);
        codecId = StringIOHelper.readISO646String(din, 4);
        din.skipBytes(1);
        b = din.readByte();
        setIsMultiAngle((b & 0x10) != 0);
        setConnectionCondition(b & 0x0f);
        stcId = din.readUnsignedByte();
        din.readFully(inTimeBytes);
        setInTime(UnsignedIntHelper.convertToLong(inTimeBytes));
        din.readFully(outTimeBytes);
        setOutTime(UnsignedIntHelper.convertToLong(outTimeBytes));
        uoMaskTable.readObject(din);
        b = din.readByte();
        setPlayItemRandomAccessFlag((b & 0x80) != 0);
        setStillMode(din.readUnsignedByte());
        if (getStillTime() == 1) {
            setStillTime(din.readUnsignedShort());
        } else {
            din.skipBytes(2);
        }
        
        ClipInfo entry = new ClipInfo(0, clipName, codecId, stcId);
        angleList.add(entry);
        if (getIsMultiAngle()) { // more ClipInfo data here
            int entries = din.readUnsignedByte();
            b = din.readByte();
            setIsDifferentAudios((b & 0x02) != 0);
            setIsSeamlessAngleChange((b & 0x01) != 0);
            for (int i = 1; i < entries; i++) {
               clipName = StringIOHelper.readISO646String(din, 5);
               codecId = StringIOHelper.readISO646String(din, 4);
               stcId = din.readUnsignedByte();
               entry = new ClipInfo(i, clipName, codecId, stcId);
               angleList.add(entry);
            }
        } 
        ClipInfo[] entries = 
             (ClipInfo[]) angleList.toArray(new ClipInfo[angleList.size()]);
        setAngles(entries);
        stnTable.readObject(din);
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);

        ClipInfo angle = angles[0];
        substream.write(StringIOHelper.getISO646Bytes(angle.getClipName()));
        substream.write(StringIOHelper.getISO646Bytes("M2TS"));
        substream.writeByte(0);
        int value = getIsMultiAngle() ?  0x10 : 0 ;
        value |= getConnectionCondition();
        substream.writeByte(value);
        substream.writeByte(angle.getStcId());
        substream.write(UnsignedIntHelper.convertToBytes(getInTime()));
        substream.write(UnsignedIntHelper.convertToBytes(getOutTime()));
        uoMaskTable.writeObject(substream);
        substream.writeByte(getPlayItemRandomAccessFlag() ? 0x80 : 0);
        substream.writeByte(getStillMode());
        if (getStillMode() == 1) {
            substream.writeShort(getStillTime());
        } else {
            substream.write(new byte[2]);
        }
        if (getIsMultiAngle()) {
            substream.writeByte(angles.length); // Issue 205
            value = getIsDifferentAudios() ? 0x02 : 0;
            value |= getIsSeamlessAngleChange() ? 0x01 : 0; 
            substream.writeByte(value);
            for (int i = 1; i < angles.length; i++) { // skip angles[0]
                angle = angles[i];
                substream.write(StringIOHelper.getISO646Bytes(angle.getClipName()));
                substream.write(StringIOHelper.getISO646Bytes("M2TS"));     
                substream.writeByte(angle.getStcId());
            }
        }
        stnTable.writeObject(substream);
        substream.flush();
        substream.close();
        
        byte[] data = baos.toByteArray();
        dout.writeShort(data.length);
        dout.write(data);        
    }    

    @XmlElement(name="Angle")   
    public ClipInfo[] getAngles() {
        return angles;
    }

    public void setAngles(ClipInfo[] angles) {
        if ( (getIsMultiAngle() && (angles.length < 2 || angles.length > 9)) ||
             (!getIsMultiAngle() && angles.length != 1) ) {
            throw new IllegalArgumentException("Unexpected number of Clip Angle Information");
        }
        for (int i = 0; i < angles.length; i++) {
            if (i != angles[i].getId()) {
                throw new IllegalArgumentException("angle_id does not match the array order");
            }
        }
        this.angles = angles;
    }

    public boolean getIsMultiAngle() {
        return isMultiAngle;
    }

    public void setIsMultiAngle(boolean isMultiAngle) {
        this.isMultiAngle = isMultiAngle;
    }

    public int getConnectionCondition() {
        return connectionCondition;
    }

    public void setConnectionCondition(int connectionCondition) {
        
        if (connectionCondition != 1 &&
            connectionCondition != 5 &&
            connectionCondition != 6) {
            throw new IllegalArgumentException("Unexpected connectionCondition value "
                    + connectionCondition);
        }
                                
        this.connectionCondition = connectionCondition;
    }

    public long getInTime() {
        return inTime;
    }

    public void setInTime(long inTime) {
        this.inTime = inTime;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }

    public UOMaskTable getUoMaskTable() {
        return uoMaskTable;
    }

    public void setUoMaskTable(UOMaskTable uoMaskTable) {
        this.uoMaskTable = uoMaskTable;
    }

    public boolean getPlayItemRandomAccessFlag() {
        return playItemRandomAccessFlag;
    }

    public void setPlayItemRandomAccessFlag(boolean playItemRandomAccessFlag) {
        this.playItemRandomAccessFlag = playItemRandomAccessFlag;
    }

    public int getStillMode() {
        return stillMode;
    }

    public void setStillMode(int stillMode) {
        this.stillMode = stillMode;
    }

    public int getStillTime() {
        return stillTime;
    }

    public void setStillTime(int stillTime) {
        this.stillTime = stillTime;
    }

    public STNTable getStnTable() {
        return stnTable;
    }

    public void setStnTable(STNTable stnTable) {
        this.stnTable = stnTable;
    }

    public boolean getIsDifferentAudios() {
        return isDifferentAudios;
    }

    public void setIsDifferentAudios(boolean isDifferentAudios) {
        this.isDifferentAudios = isDifferentAudios;
    }

    public boolean getIsSeamlessAngleChange() {
        return isSeamlessAngleChange;
    }

    public void setIsSeamlessAngleChange(boolean isSeamlessAngleChange) {
        this.isSeamlessAngleChange = isSeamlessAngleChange;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
