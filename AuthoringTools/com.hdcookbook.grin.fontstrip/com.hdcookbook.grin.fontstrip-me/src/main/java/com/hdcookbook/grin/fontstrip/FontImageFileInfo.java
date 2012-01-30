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

package com.hdcookbook.grin.fontstrip;

import com.hdcookbook.grin.util.AssetFinder;
import com.hdcookbook.grin.util.ManagedImage;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class FontImageFileInfo {
    static  String[]  fileNames = null;
    static  FontImageFileInfo[] fileInfos = null;    
    static  boolean initialized = false;
    
    // Version number for the font image info file.
    // The value should match the version number in 
    // com.hdcookbook.grin.fontstrip.FontImageMosaic class.
    private final static int VERSION_NUMBER = 2;
    
    int maxLeading;
    int maxAscent;
    int maxDescent;
        // maxAscent and maxDescent are based on the bound rects of
        // the characters during font construction.
    HashMap charMap;
    
    public static void initFontImageFileInfo(String infoFile) 
            throws IOException {
        
        initialized = true;
        URL u = AssetFinder.tryURL(infoFile);
        if (u == null) {
            throw new IOException("ERROR: FontStrip image info file not found " + infoFile);
        }
    
        DataInputStream dis = new DataInputStream(
                new BufferedInputStream(u.openStream()));
        int version = dis.readInt();
        if (version != VERSION_NUMBER) {
            throw new IOException("FontStrip image info file version mismatch, expects " 
                    + VERSION_NUMBER + ", got " + version);
        }
        int n = dis.readInt();
        fileNames = new String[n];        
        fileInfos = new FontImageFileInfo[n];

        for (int i = 0; i < n; i++) {
            fileNames[i] = dis.readUTF();
            FontImageFileInfo info = new FontImageFileInfo();
            info.maxLeading = dis.readInt();
            info.maxAscent = dis.readInt();
            info.maxDescent = dis.readInt();
            info.charMap = new HashMap();
            int count = dis.readInt();
            for (int j = 0; j < count; j++) {
                CharImageInfo charImage = new CharImageInfo();
                charImage.ch = dis.readChar();
                charImage.charRect = new Rectangle();
                charImage.charRect.x = dis.readInt();
                charImage.charRect.y = dis.readInt();
                charImage.charRect.width = dis.readInt();
                charImage.charRect.height = dis.readInt();
                charImage.ascent = dis.readInt();
                charImage.xOffset = dis.readInt();
                charImage.width = dis.readInt();

                info.charMap.put(new Character(charImage.ch), charImage);
            }
            fileInfos[i] = info;
        }
    }
    
    static FontImageFileInfo getFontInfo(String fileName) {
        for (int i = 0; i < fileNames.length; i++) {
            if (fileName.equals(fileNames[i])) {
                return fileInfos[i];
            }
        }
        return null;
    }
}
