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

import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.util.Debug;

import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents an individual command that is defined in-line in a show file.
 * An SEShowCommand participates in generating the Xlet subclass of Command
 * for a show.  It also runs something reasonable in SE emulation mode.
 * 
 * @see SEShowCommands
 *
 *  @author     Bill Foote (http://jovial.com)
 **/
public class SEShowCommand extends Command implements SENode {
    
    private SEShowCommands container;
    int commandNumber;
    private String originalSource;
    private String grinviewMethodBody;
    private String xletMethodBody;
    private ArrayList<Command> subCommands = new ArrayList<Command>();
    private GrinXHelper seCommand = null;
    private boolean triedSeCommand = false;
    
    SEShowCommand(SEShow show, SEShowCommands container, int commandNumber) {
        super(show);
        this.container = container;
        this.commandNumber = commandNumber;
    }

    /**
     * Adds a sub-command to this command object.  To execute it, run 
     * the java expression 
     * "runSubCommand(number);"
     * 
     * @param cmd   The command to add
     * @return The java source code to invoke this command in the xlet or in
     *              griview.
     */
    public String addSubCommand(Command cmd) {
        int num = subCommands.size();
        subCommands.add(cmd);
        return "runSubCommand(" + num + ", grinCaller);";
    }
    
    public int getCommandNumber() {
        return commandNumber;
    }
    
    public Command[] getSubCommands() {
        return subCommands.toArray(new Command[subCommands.size()]);
    }

    /**
     * Sets the java source of the body of the method for the GrinView version.
     * 
     * @param methodBody
     * @throws java.io.IOException 
     */
    public void setGrinviewMethodBody(String methodBody) throws IOException {
        this.grinviewMethodBody = methodBody.trim();
    }

    /**
     * Returns Grinview method body for this command.
     */
    public String getGrinviewMethodBody() {
        return grinviewMethodBody;
    }

        
    /**
     * Sets the java source of the body of the method for the JavaSE version.  
     * The special string JAVA_COMMAND_BODY must occur somewhere in the
     * string.  This will be replaced by the automatically-generated methods.
     * 
     * @param methodBody
     */
    public void setXletMethodBody(String methodBody) throws IOException {
        this.xletMethodBody = methodBody.trim();
    }
    
    /**
     * Returns Xlet method body for this command.
     */
    public String getXletMethodBody() {
        return xletMethodBody;
    }

    public String getJavaSource(boolean xlet) {
        if (xlet) {
            return xletMethodBody;
        } else {
            return grinviewMethodBody;
        }
    }

    /**
     * Returns the original source as specified in Show script file.
     */
    public String getOriginalSource() {
         return originalSource;
    }

    /**
     * Sets the original source as specified in Show file.
     */
    public void setOriginalSource(String originalSource) {
         this.originalSource = originalSource;
    }
   
    /**
     * {@inheritDoc}
     **/
    public void execute(Show caller) {
        if (!triedSeCommand) {
            triedSeCommand = true;
            Class cl = container.getCommandClass();
            if (cl != null) {
                Class[] paramType = { Show.class };
                Object[] params = { show };
                try {
                    seCommand = (GrinXHelper) 
                            cl.getConstructor(paramType).newInstance(params);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (seCommand != null) {
                seCommand.setCommandNumber(commandNumber);
                seCommand.setSubCommands(getSubCommands());
            }
        }
        if (seCommand != null) {
            try {
                seCommand.execute(caller);
            } catch (Throwable t) {
                System.out.println("***  Error executing command:  ");
                t.printStackTrace();
                System.out.println();
            }
        } else {
            System.out.println("executing command:  " + xletMethodBody);
        }
    }

    /**
     * {@inheritDoc}
     **/
    public void execute() {
        if (Debug.ASSERT) {
            Debug.assertFail();
        }
    }

    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException 
    {
        out.writeSuperClassData(this);
        out.writeInt(getCommandNumber());
        out.writeCommands(getSubCommands());
    }

    public String getRuntimeClassName() {
        return container.getClassName();  // Need special care
    }
    
    public void accept(SEShowVisitor visitor) {
        visitor.visitShowCommand(this);
    }

    /**
     * {@inheritDoc}
     **/
    public void postProcess(ShowBuilder builder) throws IOException {
    }

    /**
     * {@inheritDoc}
     **/
    public void changeFeatureReference(Feature from, Feature to)
        throws IOException 
    {
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        return "java_command number " + commandNumber;
    }
}
