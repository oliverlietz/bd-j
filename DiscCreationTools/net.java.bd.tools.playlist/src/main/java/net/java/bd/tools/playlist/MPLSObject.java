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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * BD-ROM Part 3-1 5.3 Movie PlayList file.
 * This is the xml "root" object that includes all the data for the PlayList file.
 */
@XmlRootElement
@XmlType(propOrder={"version", "appInfoPlayList", "playList", 
"playListMark", "extensionData", "paddingN1", "paddingN2", "paddingN3", "paddingN4"})
public class MPLSObject {

    public static final String TYPE = "MPLS";
    
    String version = null;
    AppInfoPlayList appInfo = new AppInfoPlayList();
    PlayList playList = new PlayList();
    PlayListMark playListMark = new PlayListMark();
    ExtensionData extensionData = null;  // ExtensionData might not be present.
    
    int paddingN1, paddingN2, paddingN3, paddingN4;

    public MPLSObject() {}
    
    public void setVersion(String s) {
        this.version = s;
    }

    public String getVersion() {
        return version;
    }

    public void setAppInfoPlayList(AppInfoPlayList appInfo) {
        this.appInfo = appInfo;
    }
  
    public AppInfoPlayList getAppInfoPlayList() {
        return this.appInfo;
    }
    public PlayList getPlayList() {
        return playList;
    }
    
    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }
  
    public PlayListMark getPlayListMark() {
        return playListMark;
    }
    
    public void setPlayListMark(PlayListMark playListMark) {
        this.playListMark = playListMark;
    }
    
    public void setExtensionData(ExtensionData extensionData) {
        this.extensionData = extensionData;
    }
  
    public ExtensionData getExtensionData() {
        return extensionData;
    }   
    
    public void setPaddingN1(int i) {
        paddingN1 = i;
    }
  
    public int getPaddingN1() {
        return paddingN1;
    }   
    public void setPaddingN2(int i) {
        paddingN2 = i;
    }
  
    public int getPaddingN2() {
        return paddingN2;
    }    
    
    public void setPaddingN3(int i) {
        paddingN3 = i;
    }
  
    public int getPaddingN3() {
        return paddingN3;
    } 
    
    public void setPaddingN4(int i) {
        paddingN4 = i;
    }
  
    public int getPaddingN4() {
        return paddingN4;
    }    
    
    public void readObject(DataInputStream din) throws IOException {
        // 8*4 bit type indicator
        // 8*4 bit version number
        // 32 bit PlayList_start_address
        // 32 bit PlayListMark_start_address
        // 32 bit ExtensionData_start_address
        // 160 reserved
        // AppInfoPlayList
        // Padding N1 * 16 bits
        // PlayList
        // Padding N2 * 16 bits
        // PlayListMark
        // padding N3 * 16 bits
        // ExtensionData
        // padding N4 * 16 bits
        
        String typeIndicator         = StringIOHelper.readISO646String(din, 4);
        String versionNumber         = StringIOHelper.readISO646String(din, 4);
        int playListStartAddress     = din.readInt();
        int playListMarkStartAddress = din.readInt();
        int extensionStartAddress         = din.readInt();
        DataInputStream substream;
        
        if (!TYPE.equals(typeIndicator)) {
            throw new RuntimeException("TypeIndicator error " + typeIndicator);
        }
        setVersion(versionNumber);
        din.skipBytes(20);        
        
        // AppInfoPlayList
        byte[] appInfoBytes = new byte[playListStartAddress - (4*5 + 20)];
        din.read(appInfoBytes);
        substream = new DataInputStream(new ByteArrayInputStream(appInfoBytes));
        appInfo.readObject(substream);     
        setPaddingN1(seekPaddings(substream));  // check for padding_words
        substream.close();
        
        // PlayList
        byte[] playListBytes = new byte[playListMarkStartAddress - playListStartAddress];
        din.read(playListBytes);
        substream = new DataInputStream(new ByteArrayInputStream(playListBytes));
        playList.readObject(substream);     
        setPaddingN2(seekPaddings(substream));  // check for padding_words
        substream.close();        
        
        if (extensionStartAddress == 0) {
            // no extension data, just read playListMark and be done.
            playListMark.readObject(din);
            setPaddingN3(seekPaddings(din));
            setPaddingN4(0);
            return;
        } 
            
        // PlayListMark and ExtensionData
        byte[] playListMarkBytes = new byte[extensionStartAddress - playListMarkStartAddress];
        din.read(playListMarkBytes);
        substream = new DataInputStream(new ByteArrayInputStream(playListMarkBytes));
        playListMark.readObject(substream);
        setPaddingN3(seekPaddings(substream));        
        substream.close();
            
        extensionData = new ExtensionData();
        extensionData.readObject(din);       
        setPaddingN4(seekPaddings(din));
    }
    
    public void writeObject(DataOutputStream out) throws IOException {
               
        int playListStartAddress;
        int playListMarkStartAddress;
        int extensionDataStartAddress;
        byte[] reserved = new byte[20];
        
        // Write out AppInfoPlayList, PlayList and PlaylistMark to 
        // a byte array first, to calcurate data size.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream appInfoStream = new DataOutputStream(baos);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DataOutputStream playListStream = new DataOutputStream(baos2); 
        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        DataOutputStream playListMarkStream = new DataOutputStream(baos3); 
        
        appInfo.writeObject(appInfoStream);
        for (int i = 0; i < paddingN1; i++) {
            appInfoStream.writeShort((short)0);
        }
      
        playList.writeObject(playListStream);
        for (int i = 0; i < paddingN2; i++) {
           playListStream.writeShort((short)0);
        }       
        playListMark.writeObject(playListMarkStream);
        for (int i = 0; i < paddingN3; i++) {
           playListMarkStream.writeShort((short)0);
        } 
        appInfoStream.flush();
        playListStream.flush();
        playListMarkStream.flush();

        appInfoStream.close();
        playListStream.close();
        playListMarkStream.close();        
        
        playListStartAddress = ((4*5 + 20) + appInfoStream.size());
        playListMarkStartAddress = playListStartAddress + playListStream.size();
        if (extensionData == null) {
            extensionDataStartAddress = 0;
        } else {
            extensionDataStartAddress = playListMarkStartAddress + playListMarkStream.size();
        }
        
        // Now write out the entire dataset to the file.
        out.write(StringIOHelper.getISO646Bytes(TYPE));
        out.write(StringIOHelper.getISO646Bytes(getVersion()));
        out.writeInt(playListStartAddress);
        out.writeInt(playListMarkStartAddress);
        out.writeInt(extensionDataStartAddress);
        out.write(reserved);
        out.write(baos.toByteArray());  // appInfoPlayList
        out.write(baos2.toByteArray()); // PlayList
        out.write(baos3.toByteArray()); // PlayListMark
        if (extensionData != null) {
            extensionData.writeObject(out);  // extensionsData
            for (int i = 0; i < paddingN4; i++) {
               out.writeShort((short) 0);
            }        
        }
    }
     
    // Find out how many 16-bit paddings are at the end of the stream.
    private int seekPaddings(DataInputStream in) throws IOException {
        int i = 0;
        try {
            while (true) {
                in.readUnsignedShort();
                i++;
            }
        } catch (EOFException e) {
        }
        
        return i;
    }
}
