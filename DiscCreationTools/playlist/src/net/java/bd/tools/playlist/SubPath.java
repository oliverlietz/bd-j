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
import javax.xml.bind.annotation.XmlElement;

/**
 * BD-ROM Part 3-1 5.3.5 SubPath
 */
public class SubPath {

    private int id;
    private SubPathType type;
    private boolean isRepeatSubPath;
    private SubPlayItem[] subPlayItems;
    
    public SubPath() {}
    public SubPath(int id) {
        this.id = id;
    }
    
    public void readObject(DataInputStream din) throws IOException {
        // 32 bit length
        // 8 bit reserve
        // 8 bit subpath type
        // 15 bit reserve
        // 1 bit isRepeatSubPath
        // 8 bit number of SubPlayItems
        // SubPlayItems[]
        
        din.skipBytes(5);
        int stype = din.readUnsignedByte();
        Enum[] subPathTypes = SubPathType.values();  
        for (int i = 0; i < subPathTypes.length; i++ ) {
           if (subPathTypes[i].ordinal() == stype) {
                setSubPathType((SubPathType)subPathTypes[i]);
                break;
           }   
        }          
        din.skipBytes(1);
        byte b = din.readByte();
        setIsRepeatSubPath( (b & 0x01) != 0);
        din.skipBytes(1);
        int length = din.readUnsignedByte();
        SubPlayItem[] items = new SubPlayItem[length];
        for (int i = 0; i < items.length; i++) {
            items[i] = new SubPlayItem(i);
            items[i].readObject(din);
        }
        setSubPlayItems(items);
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);
        
        substream.writeByte(0);       
        substream.writeByte(getSubPathType().ordinal());
        substream.writeByte(0);
        substream.writeByte(getIsRepeatSubPath() ? 0x01 : 0x00);
        substream.writeByte(0);
        SubPlayItem[] items = getSubPlayItems();
        substream.writeByte(items.length);
        for (int i = 0; i < items.length; i++) {
            items[i].writeObject(substream);
        }
        substream.flush();
        substream.close();
        
        byte[] data = baos.toByteArray();
        dout.writeInt(data.length);
        dout.write(data);        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SubPathType getSubPathType() {
        return type;
    }

    public void setSubPathType(SubPathType type) {
        this.type = type;
    }

    public boolean getIsRepeatSubPath() {
        return isRepeatSubPath;
    }

    public void setIsRepeatSubPath(boolean isRepeatSubPath) {
        this.isRepeatSubPath = isRepeatSubPath;
    }
    
    @XmlElement(name="SubPlayItem")  
    public SubPlayItem[] getSubPlayItems() {
        return subPlayItems;
    }

    public void setSubPlayItems(SubPlayItem[] subPlayItems) {
        this.subPlayItems = subPlayItems;
    }
    
    public enum SubPathType {

        RESERVED_0,
        RESERVED_1,
        PRIMARY_AUDIO_PRESENTATION,
        INTERACTIVE_GRAPHICS_PRESENTATION,
        TEXT_SUBTITLE_PRESENTATION,
        OUT_OF_MUX_AND_SYNC,
        OUT_OF_MUX_AND_ASYNC,
        IN_MUX_AND_SYNC,
        RESERVED_8;
        
        public byte getEncoding() {
            return (byte) ordinal();
        }
    }     
}
