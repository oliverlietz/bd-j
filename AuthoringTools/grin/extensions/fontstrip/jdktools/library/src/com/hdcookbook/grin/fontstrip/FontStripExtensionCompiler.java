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

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.io.text.Lexer;
import com.hdcookbook.grin.util.AssetFinder;
import java.awt.Color;
import java.io.IOException;
import java.util.Vector;

/** 
 * Grin text parser for FontStrip extension feature.
 */
public class FontStripExtensionCompiler implements ExtensionParser {

    public Feature getFeature(Show show, String typeName, String name, Lexer lexer)
            throws IOException {
        
        if ("fontstrip:text".equals(typeName)) {

            // arguments are - font_image_filename 
            //  text_pos text_strings
            //  ["hspace" integer] 
            //  [ "background" color_entry ] ;

            String fileName = lexer.getString();
            
            String tok = lexer.getString();
            int xAlign = SEFontStripText.LEFT;
            if ("left".equals(tok)) {
                xAlign = SEFontStripText.LEFT;
                tok = lexer.getString();
            } else if ("middle".equals(tok)) {
                xAlign = SEFontStripText.MIDDLE;
                tok = lexer.getString();
            } else if ("right".equals(tok)) {
                xAlign = SEFontStripText.RIGHT;
                tok = lexer.getString();
            }
            int x = lexer.convertToInt(tok);
            tok = lexer.getString();
            int yAlign = SEFontStripText.TOP;
            if ("top".equals(tok)) {
                yAlign = SEFontStripText.TOP;
                tok = lexer.getString();
            } else if ("baseline".equals(tok)) {
                yAlign = SEFontStripText.BASELINE;
                tok = lexer.getString();
            } else if ("bottom".equals(tok)) {
                yAlign = SEFontStripText.BOTTOM;
                tok = lexer.getString();
            }
            int y = lexer.convertToInt(tok);
            int alignment = xAlign | yAlign;

            tok = lexer.getString();
            String[] textStrings;

            if ("{".equals(tok)) {
                textStrings = parseStringsWithOpenBraceRead(lexer);
            } else {
                textStrings = new String[]{tok};
            }

            int vspace = 0;
            int hspace = 0;
            tok = lexer.getString();
            
            if ("vspace".equals(tok)) {
                vspace = lexer.getInt();
                tok = lexer.getString();
            }            
            if ("hspace".equals(tok)) {
                hspace = lexer.getInt();
                tok = lexer.getString();
            }
            
            Color backgroundColor = null;

            if ("background".equals(tok)) {
                lexer.parseExpected("{");
                int r = lexer.getInt();
                int g = lexer.getInt();
                int b = lexer.getInt();
                int a = lexer.getInt();
                lexer.parseExpected("}");

                backgroundColor = AssetFinder.getColor(r, g, b, a);
            }

            if (!";".equals(tok)) {
               lexer.parseExpected(";");
            }
            
            SEFontStripText text = new SEFontStripText(show, x, y, alignment, textStrings, 
                    fileName, hspace, vspace, backgroundColor);
            text.setName(name);
            
            return text;
            
        }
        return null;
    }

    public Modifier getModifier(Show arg0, String arg1, String arg2, Lexer arg3) 
            throws IOException {
        return null;
    }

    public Command getCommand(Show arg0, String arg1, Lexer arg2) 
            throws IOException {
        return null;
    }
    
    private String[] parseStringsWithOpenBraceRead(Lexer lexer) throws IOException {
        Vector v = new Vector();
        for (;;) {
            String tok = lexer.getString();
            if (tok == null) {
                lexer.reportError("EOF unexpected in string list");
            } else if ("}".equals(tok)) {
                break;
            } else {
                v.addElement(tok);
            }
        }
        int num = v.size();
        String[] result = new String[num];
        for (int i = 0; i < num; i++) {
            result[i] = (String) v.elementAt(i);
        }
        return result;
    }
}
