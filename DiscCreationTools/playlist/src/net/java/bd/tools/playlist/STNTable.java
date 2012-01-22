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
 * BD-ROM Part 3-1 5.3.4.5.1 STN_table
 * STN == STream Number
 */
public class STNTable {
    
    private PrimaryVideoStreamEntry[] primaryVideoStreams;
    private PrimaryAudioStreamEntry[] primaryAudioStreams;
    private PGTextSTStreamEntry[] pgTextSTStreams;  
    private IGStreamEntry[] igStreams;
    private SecondaryVideoStreamEntry[] secondaryVideoStreams;
    private SecondaryAudioStreamEntry[] secondaryAudioStreams;
    private Integer pipPGTextSTStreamEntriesPlus;
    
    public void readObject(DataInputStream din) throws IOException {
        
        int length = din.readUnsignedShort();
        din.skipBytes(2);
        int primaryVideoStreamLength   = din.readUnsignedByte();
        int primaryAudioStreamLength   = din.readUnsignedByte();
        int pgTextStStreamLength       = din.readUnsignedByte();
        int igStreamLength             = din.readUnsignedByte();
        int secondaryAudioStreamLength = din.readUnsignedByte();
        int secondaryVideoStreamLength = din.readUnsignedByte();
        int pipPGTextStStreamLength    = din.readUnsignedByte();
        din.skipBytes(5);
        
        if (primaryVideoStreamLength > 0) {
            setPrimaryVideoStreams(new PrimaryVideoStreamEntry[primaryVideoStreamLength]);
            for (int i = 0; i < primaryVideoStreamLength; i++) {
                getPrimaryVideoStreams()[i] = new PrimaryVideoStreamEntry(i);
                getPrimaryVideoStreams()[i].readObject(din);
            }
        }
        if (primaryAudioStreamLength > 0)  {
            setPrimaryAudioStreams(new PrimaryAudioStreamEntry[primaryAudioStreamLength]);
            for (int i = 0; i < primaryAudioStreamLength; i++) {
                getPrimaryAudioStreams()[i] = new PrimaryAudioStreamEntry(i);
                getPrimaryAudioStreams()[i].readObject(din);
            }
        }    
        if (pgTextStStreamLength > 0 ||
            pipPGTextStStreamLength > 0) {
            
            setPipPGTextSTStreamEntriesPlus(pipPGTextStStreamLength);
            length = pgTextStStreamLength + pipPGTextStStreamLength;            
            setPGTextSTStreams(new PGTextSTStreamEntry[length]);
            
            for (int i = 0; i < length; i++) {
                getPGTextSTStreams()[i] = new PGTextSTStreamEntry(i);
                getPGTextSTStreams()[i].readObject(din);
            }            
        }
        if (igStreamLength > 0) {
            setIGStreams(new IGStreamEntry[igStreamLength]);
            for (int i = 0; i < igStreamLength; i++) {
                getIGStreams()[i] = new IGStreamEntry(i);
                getIGStreams()[i].readObject(din);
            }            
        } 
        if (secondaryAudioStreamLength > 0) {
            setSecondaryAudioStreams(new SecondaryAudioStreamEntry[secondaryAudioStreamLength]);
            for (int i = 0; i < secondaryAudioStreamLength; i++) {
                getSecondaryAudioStreams()[i] = new SecondaryAudioStreamEntry(i);
                getSecondaryAudioStreams()[i].readObject(din);
            }            
        }         
        if (secondaryVideoStreamLength > 0) {
            setSecondaryVideoStreams(new SecondaryVideoStreamEntry[secondaryVideoStreamLength]);
            for (int i = 0; i < secondaryVideoStreamLength; i++) {
                getSecondaryVideoStreams()[i] = new SecondaryVideoStreamEntry(i);
                getSecondaryVideoStreams()[i].readObject(din);
            }            
        }           
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        
        int primaryVideoStreamLength   = 
                (getPrimaryVideoStreams() == null ? 0 : getPrimaryVideoStreams().length);
        int primaryAudioStreamLength   = 
                (getPrimaryAudioStreams() == null ? 0 : getPrimaryAudioStreams().length);            
        int pgTextStStreamLength       = 
                (getPGTextSTStreams() == null ? 0 : getPGTextSTStreams().length);
        int igStreamLength             = 
                (getIGStreams() == null ? 0 : getIGStreams().length);
        int secondaryAudioStreamLength = 
                (getSecondaryAudioStreams() == null ? 0 : getSecondaryAudioStreams().length);
        int secondaryVideoStreamLength = 
                (getSecondaryVideoStreams() == null ? 0 : getSecondaryVideoStreams().length);
        int pipPGTextStStreamLength    = 
                (getPipPGTextSTStreamEntriesPlus() == null ? 0 : getPipPGTextSTStreamEntriesPlus());  
    
        // Adjust the count for the Primary audio stream entry, according to the spec.
        primaryAudioStreamLength = primaryAudioStreamLength - pipPGTextStStreamLength;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);
        
        substream.write(new byte[2]);
        substream.writeByte(primaryVideoStreamLength);
        substream.writeByte(primaryAudioStreamLength);
        substream.writeByte(pgTextStStreamLength);
        substream.writeByte(igStreamLength);
        substream.writeByte(secondaryAudioStreamLength);
        substream.writeByte(secondaryVideoStreamLength);
        substream.writeByte(pipPGTextStStreamLength);
        substream.write(new byte[5]);
        
        if (primaryVideoStreamLength > 0) {
            for (int i = 0; i < primaryVideoStreamLength; i++) {
                getPrimaryVideoStreams()[i].writeObject(substream);
            }
        }
        if (primaryAudioStreamLength > 0) {
            for (int i = 0; i < primaryAudioStreamLength; i++) {
                getPrimaryAudioStreams()[i].writeObject(substream);
            }
        }    
        if (pgTextStStreamLength > 0 ||
            pipPGTextStStreamLength > 0) {
            int length = pgTextStStreamLength + pipPGTextStStreamLength;            
            for (int i = 0; i < pgTextStStreamLength; i++) {
                getPGTextSTStreams()[i].writeObject(substream);
            }            
        }
        if (igStreamLength > 0) {
            for (int i = 0; i < igStreamLength; i++) {
                getIGStreams()[i].writeObject(substream);
            }            
        } 
        if (secondaryAudioStreamLength > 0) {
            for (int i = 0; i < secondaryAudioStreamLength; i++) {
                getSecondaryAudioStreams()[i].writeObject(substream);
            }            
        }         
        if (secondaryVideoStreamLength > 0) {
            for (int i = 0; i < secondaryVideoStreamLength; i++) {
                getSecondaryVideoStreams()[i].writeObject(substream);
            }            
        }           
        
        substream.flush();
        substream.close();
        byte[] data = baos.toByteArray();
        dout.writeShort(data.length);
        dout.write(data);
    }

    @XmlElement(name="PrimaryVideoStream")  
    public PrimaryVideoStreamEntry[] getPrimaryVideoStreams() {
        return primaryVideoStreams;
    }

    public void setPrimaryVideoStreams(PrimaryVideoStreamEntry[] primaryVideoStreams) {
        this.primaryVideoStreams = primaryVideoStreams;
    }

    @XmlElement(name="PrimaryAudioStream")  
    public PrimaryAudioStreamEntry[] getPrimaryAudioStreams() {
        return primaryAudioStreams;
    }

    public void setPrimaryAudioStreams(PrimaryAudioStreamEntry[] primaryAudioStreams) {
        this.primaryAudioStreams = primaryAudioStreams;
    }

    @XmlElement(name="PGTextSTStream")  
    public PGTextSTStreamEntry[] getPGTextSTStreams() {
        return pgTextSTStreams;
    }

    public void setPGTextSTStreams(PGTextSTStreamEntry[] pgTextStStreams) {
        this.pgTextSTStreams = pgTextStStreams;
    }
    
    @XmlElement(name="IGStream")  
    public IGStreamEntry[] getIGStreams() {
        return igStreams;
    }

    public void setIGStreams(IGStreamEntry[] igStreams) {
        this.igStreams = igStreams;
    }

    @XmlElement(name="SecondaryVideoStream")  
    public SecondaryVideoStreamEntry[] getSecondaryVideoStreams() {
        return secondaryVideoStreams;
    }

    public void setSecondaryVideoStreams(SecondaryVideoStreamEntry[] secondaryVideoStreams) {
        this.secondaryVideoStreams = secondaryVideoStreams;
    }
    
    @XmlElement(name="SecondaryAudioStream")  
    public SecondaryAudioStreamEntry[] getSecondaryAudioStreams() {
        return secondaryAudioStreams;
    }

    public void setSecondaryAudioStreams(SecondaryAudioStreamEntry[] secondaryAudioStreams) {
        this.secondaryAudioStreams = secondaryAudioStreams;
    }

    public Integer getPipPGTextSTStreamEntriesPlus() {
        return pipPGTextSTStreamEntriesPlus;
    }

    public void setPipPGTextSTStreamEntriesPlus(Integer pipPGTextSTStreamEntriesPlus) {
        this.pipPGTextSTStreamEntriesPlus = pipPGTextSTStreamEntriesPlus;
    }
}
