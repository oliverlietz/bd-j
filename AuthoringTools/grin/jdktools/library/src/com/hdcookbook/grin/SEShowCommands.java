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

package com.hdcookbook.grin;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Represents the commands that are defined in-line in a show file.  All of
 * the commands are combined together into one command class that is included
 * with the xlet.  There's also a facility to make a different version of the
 * commands class that can run under JavaSE during debugging.
 * <p>
 * For JavaSE, the SEShowCommands object emits a java source file.  Ideally,
 * you should generate this source file, compile it, then link GrinView against
 * it, even if you're reading a source text file with GrinView.  If you don't,
 * it's OK:  GrinView will provide a default command implementation that prints
 * out the source of what would have been executed, rather than trying to run
 * the command.
 * <p>
 * This class compiled from the source file that this class emits is pretty much 
 * a requirement to be present if your show includes any
 * custom extension and executing a binary file, meanwhile.  SEShowCommands 
 * generates some code to indicate how to instantiate custom extensions. 
 *
 * @author     Bill Foote (http://jovial.com)
 **/

public class SEShowCommands  {
    
    private ArrayList<SEShowCommand> commands = new ArrayList<SEShowCommand>();
    private String className;   
    private String grinviewClassBody;
    private String xletClassBody;
    private String originalSource;
    private Class showCommandsClass = null;
    private boolean triedShowCommandsClass = false;
    private SEShow show;
    
    //
    // package-private constructor
    //
    SEShowCommands(SEShow show) {
        this.show = show;
    }

    SEShow getShow() {
        return show;
    }
    
    /**
     * Set the fully-qualified name of the class to generate for the xlet
     * 
     * @param className The name
     */
    public void setClassName(String className) {
        this.className = className;
    }

    //
    // Get the command class, if it's in our classpath.  If not, return
    // null.
    //
    public synchronized Class getCommandClass() {
        if (!triedShowCommandsClass) {
            triedShowCommandsClass = true;
            try {
                showCommandsClass = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                System.out.println(className + " not found; java_command commands won't run.");
            } catch (Throwable t) {
                System.out.println("=======  Problem with " + className 
                                        + "  =====");
                t.printStackTrace();
            }
        }
        return showCommandsClass;
    }

    /**
     * 
     * @return the show class name, or null if not set.
     */
    public String getClassName() {
        return className;
    }
    
    public SEShowCommand addNewCommand() {
        SEShowCommand cmd = new SEShowCommand(show, this, commands.size());
        commands.add(cmd);
        return cmd;
    }

    /**
     * Adds to the java source of the body of the class for the GrinView version.  
     * The special string JAVA_COMMAND_BODY must occur somewhere in the
     * string.  This will be replaced by the automatically-generated methods.
     * 
     * @param classBody
     * @throws java.io.IOException  If there's an error, like the 
     *          absence of JAVA_COMMAND_BODY
     */
    public void setGrinviewClassBody(String classBody) throws IOException {
        checkForJavaCommandBody(classBody);
        this.grinviewClassBody = classBody;        
    }

    /**
     * Returns grinview class body string.
     */
    public String getGrinviewClassBody() {
        return this.grinviewClassBody;
    } 
        
    /**
     * Adds to the java source of the body of the class for the JavaSE version.  
     * The special string JAVA_COMMAND_BODY must occur somewhere in the
     * string.  This will be replaced by the automatically-generated methods.
     * 
     * @param classBody
     * @throws java.io.IOException  If there's an error, like the 
     *          absence of JAVA_COMMAND_BODY
     */
    public void setXletClassBody(String classBody) throws IOException {
        checkForJavaCommandBody(classBody);
        this.xletClassBody = classBody;
    }
    
    /**
     * Returns xlet class body string.
     */
    public String getXletClassBody() {
        return this.xletClassBody;
    } 

    public void setOriginalSource(String originalSource) {
        this.originalSource = originalSource;
    }

    public String getOriginalSource() {
        return originalSource;
    }

    private void checkForJavaCommandBody(String classBody) throws IOException {
        if (!classBody.contains("JAVA_COMMAND_BODY")) {
            throw new IOException("Command class needs to contain "
                                  + "JAVA_COMMAND_BODY macro");
        }
    }
    
    public void appendXletClassBody(String classBody) {
        
    }
    
    /** 
     * Get the java source for the class implementing this show's command.
     * 
     * @param forXlet true if the code to emit is for the xlet to use, false
     * if it's meant for GrinView on big jdk to use.
     * @param moreCode code to append to the end of the generated code.
     * 
     * @return  The java source, or null if there are no commands to be emitted.
     */
    public String getJavaSource(boolean forXlet, String moreCode) {
        if (className == null) {
            return null;
        }
        
        StringBuffer generated = new StringBuffer();
        if (commands != null) {
            // first, command switch statement\
            generated.append("    public void execute(com.hdcookbook.grin.Show caller) {\n");
            generated.append("        switch (commandNumber) {\n");
            for (int i = 0; i < commands.size(); i++) {
                generated.append("            case " + i + ": grinCommand" + i + "(caller);    break;\n");
            }
            generated.append("        }\n");
            generated.append("    }\n\n");

            for (int i = 0; i < commands.size(); i++) {
                generated.append("    private void grinCommand" + i + "(com.hdcookbook.grin.Show grinCaller) {\n");
                generated.append("        " + commands.get(i).getJavaSource(forXlet) + "\n");
                generated.append("    }\n\n");
            }
        }
        
        if (moreCode != null) {
           // append extra code at the end
           generated.append(moreCode);
        }
        
        String body = forXlet ? xletClassBody : grinviewClassBody;
              
        return body.replaceFirst("JAVA_COMMAND_BODY", generated.toString());
    }
    
}
