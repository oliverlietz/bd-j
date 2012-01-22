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


package com.hdcookbook.grin.fontstrip.xml;

import com.hdcookbook.grin.util.AssetFinder;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java representation of the xml configuration file which
 * the fontstrip image generator expects from the user.
 */
@XmlRootElement
public class InputData {
    private FontDescription[] fontDescription;
    private FontImageFile[]   fontImageFile;
 
    public FontImageFile[] getFontImageFile() {
        return fontImageFile;
    }

    public void setFontImageFile(FontImageFile[] fim) {
        this.fontImageFile = fim;
    }

    public FontDescription[] getFontDescription() {
        return fontDescription;
    }

    public void setFontDescription(FontDescription[] descriptions) {
        this.fontDescription = descriptions;
        
        for (int i = 0; i < descriptions.length; i++) {
            loadFont(descriptions[i]);
        }
    }
    
    public  FontDescription getFontDescription(FontImageFile file) {
        String descriptionName = file.getFontName();
        for (int i = 0; i < fontDescription.length; i++) {
            if (fontDescription[i].getName().equals(descriptionName)) {
                return fontDescription[i];
            }
        }
        return null;
    }
    
    private Font loadFont(FontDescription description) {
        Font font = null;
        String fontFileName = description.getFontFileName();
        int    fontSize     = description.getFontSize();
        
        if (fontFileName != null) {
            try {
               font = loadFont(fontFileName);   
               font = font.deriveFont((float) fontSize);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error in loading font file " + fontFileName);
            } catch (FontFormatException e) {
                e.printStackTrace();
                System.err.println("Error in loading font file " + fontFileName);
            }
        }
        
        if (font == null) {
            String fontName = description.getPhysicalName();
            font = new Font(fontName, Font.PLAIN, fontSize);
        }        
        
        description.font = font;
        
        return font;
    }
    
    private Font loadFont(String filename) 
            throws IOException, FontFormatException {   
        URL u = AssetFinder.tryURL(filename);
        InputStream in;
        if (u != null) {
            in = u.openStream();
        } else {
            in = new BufferedInputStream(new FileInputStream(filename));
        }
        Font f = Font.createFont(Font.TRUETYPE_FONT, in); 
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
        return f;
    }      
 
}
