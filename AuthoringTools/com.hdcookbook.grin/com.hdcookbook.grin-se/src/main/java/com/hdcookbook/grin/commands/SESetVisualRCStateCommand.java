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
import com.hdcookbook.grin.input.VisualRCHandler;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

public class SESetVisualRCStateCommand extends SetVisualRCStateCommand 
        implements SENode {
    
    public SESetVisualRCStateCommand(SEShow show) {
        super(show);
    }
    
    public SESetVisualRCStateCommand(SEShow show, boolean activated, int state,
            VisualRCHandler handler, boolean runCommands) 
    {
        super(show, activated, state, handler, runCommands);
    }
    
    public SESetVisualRCStateCommand(SEShow show, boolean activated, int state,
            VisualRCHandler handler, boolean runCommands, int gridNumber) 
    {
        super(show, activated, state, handler, runCommands, gridNumber);
    }
    
    /**
     * Called from parser 
     *  
     * @param state State number, -1 means "current state"
     **/
    public void setup(boolean activated, int state, 
            VisualRCHandler handler, boolean runCommands, int gridNumber)  {
        this.activated = activated;
        this.state = state;
        this.handler = handler;
        this.runCommands = runCommands;
        this.gridNumber = gridNumber;
    }
    
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public void setVisualRCHandler(VisualRCHandler handler) {
        this.handler = handler;
    }
    
    public void setRunCommands(boolean runCommands) {
        this.runCommands = runCommands;
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
        SESetVisualRCStateCommand o = (SESetVisualRCStateCommand) other;
        return this.show == o.show
               && this.activated == o.activated
               && this.state == o.state
               && this.handler == o.handler
               && this.runCommands == o.runCommands
               && this.gridNumber == o.gridNumber;
    }

    /**
     * Override of equals and hashCode to make canonicalization work
     **/
    public int hashCode() {
        int result = state << 8;
        if (runCommands) {
            result = result + 7;
        }
        if (activated) {
            result = result + 11;
        }
        result += gridNumber << 12;
        return show.hashCode() ^ handler.hashCode() ^ result;
    }

    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException {
        
        out.writeSuperClassData(this);
        out.writeBoolean(getActivated());
        out.writeInt(getState());
        out.writeRCHandlerReference(getVisualRCHandler());
        out.writeBoolean(getRunCommands());        
        out.writeInt(gridNumber);
    }

    public String getRuntimeClassName() {
        return SetVisualRCStateCommand.class.getName();
    }

    public void accept(SEShowVisitor visitor) {
        visitor.visitSetVisualRCStateCommand(this);
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
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        return "set_visual_rc " + handler 
                                + " (" + activated + ", " 
                                + handler.getStateName(state) + ", "
                                + runCommands + ", " + gridNumber + " )";
    }
}
