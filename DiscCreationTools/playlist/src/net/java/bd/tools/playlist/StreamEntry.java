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

/**
 * BD-ROM Part 3-1 5.3.4.5.2.1 stream_entry() for the STN_Table
 */
public class StreamEntry {
    
    private StreamType streamType;
    private int refToStreamPIDOfMainClip;
    private int refToSubPathId;
    private int refToSubClipEntryId;
    private int refToStreamPIDOfSubClip;
    
    public void readObject(DataInputStream din) throws IOException {
        din.skipBytes(1);
        int stype = din.readUnsignedByte();
        Enum[] streamTypes = StreamType.values();  
        for (int i = 0; i < streamTypes.length; i++ ) {
           if (streamTypes[i].ordinal() == stype) {
                setStreamType((StreamType)streamTypes[i]);
                break;
           }   
        } 
        StreamType type = getStreamType();
        if (type == StreamType.STREAM_FOR_PLAYITEM) {
            setRefToStreamPIDOfMainClip(din.readUnsignedShort());
            din.skipBytes(6);
        } else if (type == StreamType.STREAM_FOR_SUBPATH) {
            setRefToSubPathId(din.readUnsignedByte());
            setRefToSubClipEntryId(din.readUnsignedByte());
            setRefToStreamPIDOfSubClip(din.readUnsignedShort());
            din.skipBytes(4);
        } else if (type == StreamType.STREAM_FOR_IN_MUX_SUBPATH) {
            setRefToSubPathId(din.readUnsignedByte());
            setRefToStreamPIDOfMainClip(din.readUnsignedShort());
            din.skipBytes(5);
        } else {
            System.out.println("Warning, Unknown StreamEntry type " + type);
        }
        
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        
        StreamType type = getStreamType();
        
        if (type == StreamType.STREAM_FOR_PLAYITEM) {
            dout.writeByte(9); // length
            dout.writeByte(type.ordinal());            
            dout.writeShort(getRefToStreamPIDOfMainClip());
            dout.write(new byte[6]);
        } else if (type == StreamType.STREAM_FOR_SUBPATH) {
            dout.writeByte(9); // length
            dout.writeByte(type.ordinal());            
            dout.writeByte(getRefToSubPathId());
            dout.writeByte(getRefToSubClipEntryId());
            dout.writeShort(getRefToStreamPIDOfSubClip());
            dout.write(new byte[4]);
        } else if (type == StreamType.STREAM_FOR_IN_MUX_SUBPATH) {
            dout.writeByte(9); // length
            dout.writeByte(type.ordinal());  
            dout.writeByte(getRefToSubPathId());
            dout.writeShort(getRefToStreamPIDOfMainClip());
            dout.write(new byte[5]);
        } else {
            dout.writeByte(1); // length
            dout.writeByte(type.ordinal());
        }
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    public int getRefToStreamPIDOfMainClip() {
        return refToStreamPIDOfMainClip;
    }

    public void setRefToStreamPIDOfMainClip(int refToStreamPIDOfMainClip) {
        this.refToStreamPIDOfMainClip = refToStreamPIDOfMainClip;
    }

    public int getRefToSubPathId() {
        return refToSubPathId;
    }

    public void setRefToSubPathId(int refToSubPathId) {
        this.refToSubPathId = refToSubPathId;
    }

    public int getRefToSubClipEntryId() {
        return refToSubClipEntryId;
    }

    public void setRefToSubClipEntryId(int refToSubClipEntryId) {
        this.refToSubClipEntryId = refToSubClipEntryId;
    }

    public int getRefToStreamPIDOfSubClip() {
        return refToStreamPIDOfSubClip;
    }

    public void setRefToStreamPIDOfSubClip(int refToStreamPIDOfSubClip) {
        this.refToStreamPIDOfSubClip = refToStreamPIDOfSubClip;
    }
    
    public enum StreamType {

        RESERVED,
        STREAM_FOR_PLAYITEM,
        STREAM_FOR_SUBPATH,
        STREAM_FOR_IN_MUX_SUBPATH;

        public byte getEncoding() {
            return (byte) ordinal();
        }
    }     
}
