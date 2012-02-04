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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * BD-ROM Part 3-1 5.3.7 PlayListMark Mark item.
 */
public class Mark {
    private int id;
    private int type;
    private int playItemIdRef;
    private long markTimeStamp;
    private int entryEsPid;
    private long duration;
    
    public Mark() {}
    public Mark(int id) {
        this.id = id;
    }
    
    public void readObject(DataInputStream din) throws IOException {
        // 8 bit reserved        1
        // 8 bit markType        1
        // 16 bit playItemIdRef  2
        // 32 bit markTimeStamp  4 unsigned
        // 16 bit entryESPID     2
        // 32 bit duration       4 unsigned

        byte[] timeStampBytes = new byte[4];
        byte[] durationBytes  = new byte[4];

        din.skipBytes(1);
        setType(din.readUnsignedByte());
        setPlayItemIdRef(din.readUnsignedShort());
        din.readFully(timeStampBytes);
        setMarkTimeStamp(UnsignedIntHelper.convertToLong(timeStampBytes));
        setEntryEsPid(din.readUnsignedShort());
        din.readFully(durationBytes);
        setDuration(UnsignedIntHelper.convertToLong(durationBytes));
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        dout.writeByte(0);
        dout.writeByte(getType());
        dout.writeShort(getPlayItemIdRef());
        dout.write(UnsignedIntHelper.convertToBytes(getMarkTimeStamp()));
        dout.writeShort(getEntryEsPid());
        dout.write(UnsignedIntHelper.convertToBytes(getDuration()));
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)    
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        if (type != 0x01 && type != 0x02) {
            throw new IllegalArgumentException("Unexpected type " + type);
        }
        this.type = type;
    }

    public int getPlayItemIdRef() {
        return playItemIdRef;
    }

    public void setPlayItemIdRef(int playItemIdRef) {
        this.playItemIdRef = playItemIdRef;
    }

    public long getMarkTimeStamp() {
        return markTimeStamp;
    }

    public void setMarkTimeStamp(long markTimeStamp) {
        this.markTimeStamp = markTimeStamp;
    }
    
    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)  
    public Integer getEntryEsPid() {
        return entryEsPid;
    }

    public void setEntryEsPid(Integer entryEsPid) {
        this.entryEsPid = entryEsPid;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
