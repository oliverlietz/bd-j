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


/** 
 * The extensions parser for the media extension.
 */

package com.hdcookbook.grin.media;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.io.text.Lexer;
import com.hdcookbook.grin.io.text.ShowParser;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;


public class MediaExtensionParser implements ExtensionParser {

    private static class Mark {
        public int startTime;   // ms
        public String javaConstantName = null;          // can be null
        public Command[] onEntryCommands = null;        // can be null
    }
    
    /**
     * {@inheritDoc}
     **/
    public Feature getFeature(Show show, String typeName,
                              String name, Lexer lexer)
                   throws IOException
    {
        if (typeName == null) {
            return null;
        }
        
        if (typeName.equals("media:playlist")) {
            return parsePlaylist(show, name, lexer); // reserved for this parser
        } else {
            return null;
        } 
    }

    private Feature parsePlaylist(Show show, String name, Lexer lexer)
                throws IOException
    {
        String locator = lexer.getString();
        String tok = lexer.getString();
        boolean autoStart = true;
        boolean autoStop = true;
        Command[] onActivate = ShowParser.emptyCommandArray;
        Command[] onMediaStart = ShowParser.emptyCommandArray;
        Command[] onMediaEnd = ShowParser.emptyCommandArray;
        Command[] onDeactivate = ShowParser.emptyCommandArray;
        ArrayList<Mark> marks = new ArrayList<Mark>();
        if ("autostart:".equals(tok)) {
            autoStart = lexer.getBoolean();
            tok = lexer.getString();
        }
        if ("autostop:".equals(tok)) {
            autoStop = lexer.getBoolean();
            tok = lexer.getString();
        }
        if ("marks".equals(tok)) {
            parsePlaylistMarks(marks, lexer);
            tok = lexer.getString();
        }
        if ("on_activate".equals(tok)) {
            onActivate = lexer.getParser().parseCommands();
            tok = lexer.getString();
        }
        if ("on_media_start".equals(tok)) {
            onMediaStart = lexer.getParser().parseCommands();
            tok = lexer.getString();
        }
        if ("on_media_end".equals(tok)) {
            onMediaEnd = lexer.getParser().parseCommands();
            tok = lexer.getString();
        }
        if ("on_deactivate".equals(tok)) {
            onDeactivate = lexer.getParser().parseCommands();
            tok = lexer.getString();
        }
        lexer.expectString(";", tok);

        boolean hasMarkCommands = false;
        boolean hasJavaConstants = false;
        for (Mark m : marks) {
            if (m.javaConstantName != null) {
                if (!hasJavaConstants) {
                    String s;
                    s = "        // Constants for the beginning of playlist segments.\n";
                    ((SEShow) show).appendCommandClassCode(s);
                    s = "        // These can be used with PlayerWranger.setMediaTimeMS().\n";
                    ((SEShow) show).appendCommandClassCode(s);
                    hasJavaConstants= true;

                }
                String s = "    public final static int " + m.javaConstantName
                           + " = " + m.startTime + ";\n";
                ((SEShow) show).appendCommandClassCode(s);
                    // Yes, downcasts are evil.  The type of show really
                    // should be changed to SEShow throughout the parser, but
                    // that would mean breaking existing extension parsers.
            }
            hasMarkCommands = hasMarkCommands || m.onEntryCommands != null;
        }
        if (hasJavaConstants) {
            ((SEShow) show).appendCommandClassCode("\n");
        }
        int[] markTimes = null;
        Command[][] onEntryCommands = null;
        if (hasMarkCommands) {
            markTimes = new int[marks.size() + 2];
            onEntryCommands = new Command[markTimes.length][];
            markTimes[0] = Integer.MIN_VALUE;
            markTimes[markTimes.length-1] = Integer.MAX_VALUE;
            for (int i = 0; i < marks.size(); i++) {
                Mark m = marks.get(i);
                markTimes[i+1] = m.startTime;
                onEntryCommands[i+1] = m.onEntryCommands;
            }
        }
        return new SEPlaylist(show, name, locator, onActivate, onMediaStart,
                              onMediaEnd, onDeactivate, autoStart, autoStop,
                              markTimes, onEntryCommands);
    }

    private void parsePlaylistMarks(ArrayList<Mark> marks, Lexer lexer) 
            throws IOException 
    {
        lexer.parseExpected("{");
        String tok = lexer.getString();
        int lastTime = Integer.MIN_VALUE;
        for (;;) {
            if ("}".equals(tok)) {
                break;
            }
            Mark m = new Mark();
            m.startTime = lexer.convertToInt(tok);
            if (m.startTime <= lastTime) {
                lexer.reportError("Mark times are not sorted");
            } else if (m.startTime == Integer.MAX_VALUE) {
                lexer.reportError("Integer.MAX_VALUE is an illegal mark time");
            } else {
                lastTime = m.startTime;
            }
            lexer.parseExpected("ms");
            tok = lexer.getString();
            if ("java_constant".equals(tok)) {
                m.javaConstantName = lexer.getString();
                tok = lexer.getString();
            }
            if ("on_entry".equals(tok)) {
                m.onEntryCommands = lexer.getParser().parseCommands();
                tok = lexer.getString();
            }
            marks.add(m);
        }
    }

    /**
     * {@inheritDoc}
     **/
    public Modifier getModifier(Show show, String typeName,
                                String name, Lexer lexer) 
                throws IOException
    {   
        return null;
    }

    /**
     * {@inheritDoc}
     **/
    public Command getCommand(Show show, String typeName, Lexer lexer)
                           throws IOException
    {   
        return null;
    }

}
