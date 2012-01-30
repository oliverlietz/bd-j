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
package com.hdcookbook.grin.commands;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowVisitor;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataInputStream;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

/**
 * This command is a reference to one of the shows named_command structures.
 * It a reference to either a single command, or to a GrinXHelper.COMMAND_LIST.
 * When the binary file is written out, SERunNamedCommand instances go away;
 * they are simply replaced by their target.
 **/

public class SERunNamedCommand extends Command implements SENode {

    private Command target;

    public SERunNamedCommand(SEShow show) {
        super(show);
    }

    public void setTarget(Command target) {
        this.target = target;
    }

    public Command getTarget() {
        return target;
    }

    public void execute(Show caller) {
        target.execute(caller);
    }

    public void execute() {
        if (Debug.ASSERT) {
            Debug.assertFail();
        }
    }

    /**
     * Override of equals and hashCode to make canonicalization work.  
     * This is included for completeness, but the binary writer and
     * GrinXHelper make sure that all SERunNamedCommand instances are
     * resolved to their target before any comparision.
     **/
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        SERunNamedCommand o = (SERunNamedCommand) other;
        return this.show == o.show && this.target == o.target;
    }

    /**
     * Override of equals and hashCode to make canonicalization work.
     *
     * @see #equals(Object)
     **/
    public int hashCode() {
        return target.hashCode() * 17;
    }

    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException 
    {
        if (Debug.ASSERT) {
            Debug.assertFail();
                // We're special-cased in GrinDataOutputStream
        }
        throw new IOException("Internal error");
    }

    public String getRuntimeClassName() {
        if (Debug.ASSERT) {
            Debug.assertFail();
                // We're special-cased in GrinDataOutputStream
        }
        return null;
    }

    public void accept(SEShowVisitor visitor) {
        visitor.visitRunNamedCommand(this);
    }

    /**
     * {@inheritDoc}
     **/
    public void postProcess(ShowBuilder builder) throws IOException {
    }

    /**
     * {@inheritDoc}
     **/
    public void changeFeatureReference(Feature from, Feature to) {
        // We don't call changeFeatureReference on target, because
        // SEShow calls changeFeatureReference on all named commands.
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        return "run_named_command " + target;
    }
}
