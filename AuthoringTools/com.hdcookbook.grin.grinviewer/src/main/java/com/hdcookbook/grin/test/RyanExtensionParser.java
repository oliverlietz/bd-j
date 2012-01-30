
/*  
 * Copyright (c) 2007, Sun Microsystems, Inc.
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

package com.hdcookbook.grin.test;

import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.SEShowVisitor;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.io.binary.GrinDataInputStream;
import com.hdcookbook.grin.io.text.Lexer;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

/**
 * This is part of the "Ryan's life" test show.  It's mostly of
 * historical interest; it still works, but some of the ways of
 * structuring and using a show are passe.
 *
 * @author Bill Foote (http://jovial.com)
 */
public class RyanExtensionParser 
        implements ExtensionParser {
   
    RyanDirector director;

    private static abstract class RyanCommand extends Command implements SENode
    {
        public RyanCommand(Show show) {
            super(show);
        }
        public void writeInstanceData(GrinDataOutputStream out) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public String getRuntimeClassName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        public void accept(SEShowVisitor visitor) {
            visitor.visitUserDefinedCommand(this);
        }
        public void postProcess(ShowBuilder builder) {
        }
        public void changeFeatureReference(Feature from, Feature to) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public RyanExtensionParser() {
        this.director = null;   // used with GrinView
    }
    public RyanExtensionParser(RyanDirector director) {
        this.director = director;
    }


    public Feature getFeature(Show show, String typeName, 
            String name, Lexer lexer) 
            throws IOException 
    {
        return null;
    }

    public Modifier getModifier(Show show, String typeName, 
                                String name, Lexer lexer)
                                throws IOException
    {
        return null;
    }

    //public Command parseCommand(Show show, String typeName, Lexer lex,
    //                          ShowParser parser) 
    
    public Command getCommand(Show show, String typeName, 
            Lexer lexer)
                        throws IOException
    {
        Command result = null;
        if ("ryan:start_video".equals(typeName)) {
            result = new RyanCommand(show) {
               public void execute() { 
                    if (director == null) {
                        System.out.println("Pretending to start video");
                    } else {
                        director.startVideo();
                    }
               }
               public void readInstanceData(GrinDataInputStream in) throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
               }
            };
        } else if ("ryan:play_mode_interactive".equals(typeName)) {
            final boolean val = lexer.getBoolean();
            result = new RyanCommand(show) {
               public void execute() { 
                    if (director == null) {
                        System.out.println("Pretending to set interactive mode"
                                           + val);
                    } else {
                        director.setInteractiveMode(val);
                    }
               }
               public void readInstanceData(GrinDataInputStream in) throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
               }
            };
        } else if ("ryan:toggle_commentary".equals(typeName)) {
            result = new RyanCommand(show) {
               public void execute() { 
                    if (director == null) {
                        System.out.println("Pretending to toggle commentary");
                    } else {
                        director.toggleCommentary();
                    }
               }
               public void readInstanceData(GrinDataInputStream in) throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
               }
            };
        } else if ("ryan:commentary_start".equals(typeName)) {
            result = new RyanCommand(show) {
               public void execute() { 
                    if (director == null) {
                        System.out.println("Pretending to start commentary");
                    } else {
                        director.startCommentary();
                    }
               }
               public void readInstanceData(GrinDataInputStream in) throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
               }
            };
        } else {
            throw new IOException("Unrecognized command type \"" + typeName + "\"");
        }
        
        lexer.parseExpected(";");
        
        return result;
    }
}
