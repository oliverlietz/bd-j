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
package com.hdcookbook.grin;

import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowVisitor;
import com.hdcookbook.grin.GrinXHelper;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.commands.SERunNamedCommand;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

/**
 * This is an abstract superclass of the built-in GRIN commands that
 * are implemented at runtime as direct instances of GrinXHelper.  Note
 * that the java_command structure produces commands that are
 * instances of a subclass of GrinXHelper, but on the SE side these
 * are represented using ShowCommand.
 **/
public abstract class SEGrinXHelper extends GrinXHelper implements SENode {

    public SEGrinXHelper(SEShow show) {
        super(show);
    }
    
    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException 
    {
        out.writeSuperClassData(this);
        out.writeInt(commandNumber);
        out.writeCommands(subCommands);
    }

    /**
     * Override of equals and hashCode to make canonicalization work
     **/
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        SEGrinXHelper o = (SEGrinXHelper) other;
        if (this.show != o.show || this.commandNumber != o.commandNumber) {
            return false;
        }
        if (this.subCommands == o.subCommands) {
            return true;
        } else if (this.subCommands == null || o.subCommands == null) {
            return false;
        } else if (this.subCommands.length != o.subCommands.length) {
            return false;
        }
        for (int i = 0; i < subCommands.length; i++) {
            if (!resolve(this.subCommands[i]).equals(resolve(o.subCommands[i])))
            {
                return false;
            }
        }
        return true;
    }

    private Command resolve(Command command) {
        while (command instanceof SERunNamedCommand) {
            command = ((SERunNamedCommand) command).getTarget();
        }
        return command;
    }

    /**
     * Override of equals and hashCode to make canonicalization work
     **/
    public int hashCode() {
        int result = show.hashCode() + (commandNumber * 11);
        if (subCommands != null) {
            for (int i = 0; i < subCommands.length; i++) {
                result = result ^ subCommands[i].hashCode();  // ^ is xor
            }
        }
        return result;
    }


    public String getRuntimeClassName() {
        return GrinXHelper.class.getName();
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
}
