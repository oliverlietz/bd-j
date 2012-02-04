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
 * BD-ROM 3-1 5.3.3 PlayList
 */
public class PlayList {
    private PlayItem[] playItems;
    private SubPath[] subPaths;
    
    public void readObject(DataInputStream din) throws IOException {
        // 32 bit play list length
        // 
        // 16 bit reserve
        // 16 bit number of playitems
        // 16 bit number of subpaths
        // PlayItem[]
        // SubPath[]
        
        din.skipBytes(6);
        int playlength = din.readUnsignedShort();
        int sublength  = din.readUnsignedShort();
        PlayItem[] items = new PlayItem[playlength];
        for (int i = 0; i < playlength; i++) {
            PlayItem item = new PlayItem(i);
            item.readObject(din);
            items[i] = item;
        }
        setPlayItems(items);
        SubPath[] paths = new SubPath[sublength];
        for (int i = 0; i < sublength; i++) {
            SubPath path = new SubPath(i);
            path.readObject(din);
            paths[i] = path;
        }
        setSubPaths(paths);
    }
    public void writeObject(DataOutputStream dout) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);
        
        int playItemCount = getPlayItems() == null ? 0 : getPlayItems().length;
        int subPathCount = getSubPaths() == null ? 0 : getSubPaths().length;   
        
        substream.write(new byte[2]); // reserved
        substream.writeShort(playItemCount);
        substream.writeShort(subPathCount);        
        for (int i = 0; i < playItemCount; i++) {
            getPlayItems()[i].writeObject(substream);
        }
        for (int i = 0; i < subPathCount; i++) {
            getSubPaths()[i].writeObject(substream);
        }
        substream.flush();
        substream.close();
        
        byte[] data = baos.toByteArray();
        dout.writeInt(data.length);
        dout.write(data);
    }

    @XmlElement(name="PlayItem")   
    public PlayItem[] getPlayItems() {
        return playItems;
    }

    public void setPlayItems(PlayItem[] playItems) {
        for (int i = 0; i < playItems.length; i++) {
            if (i != playItems[i].getId()) {
                throw new IllegalArgumentException("PlayItem_id does not match the array order");
            }
        }     
        this.playItems = playItems;
    }

    public SubPath[] getSubPaths() {
        return subPaths;
    }

    public void setSubPaths(SubPath[] subPaths) {
        for (int i = 0; i < subPaths.length; i++) {
            if (i != subPaths[i].getId()) {
                throw new IllegalArgumentException("SubPath_id does not match the array order");
            }
        }            
        this.subPaths = subPaths;
    }

}
