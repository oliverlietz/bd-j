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

import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.input.RCHandler;
import com.hdcookbook.grin.input.SERCHandler;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

public class SESegment extends Segment implements SENode {

    public SESegment(String name, Feature[] active, Feature[] setup,
                 RCHandler[] rcHandlers, Command[] onEntryCommands,
                 boolean nextOnSetupDone, Command[] nextCommands) 
            throws IOException 
    {
        super();
        this.name = name;       // for debugging
        this.activeFeatures = active;
        this.settingUpFeatures = setup;
        this.onEntryCommands = onEntryCommands;
        this.nextOnSetupDone = nextOnSetupDone;
        this.nextCommands = nextCommands;
        this.setRCHandlers(rcHandlers);
    }
    
   public void setName(String name) {
        this.name = name;
    }
    
    public void setActiveFeatures(Feature[] features) {
        this.activeFeatures = features;
    }
    
    public void setSettingUpFeatures(Feature[] features) {
        this.settingUpFeatures = features;
    }
    
    public void setNextOnSetupDone(boolean nextOnSetupDone) {
        this.nextOnSetupDone = nextOnSetupDone;
    }
    
    public void setNextCommands(Command[] commands) {
        this.nextCommands = commands;
    }
    
    public void setRCHandlers(RCHandler[] rcHandlers) {
        this.rcHandlers = rcHandlers;
        int pressedMask = 0;
        int releasedMask = 0;
        int typedMask = 0;
        for (int i = 0; i < rcHandlers.length; i++) {
            SERCHandler h = (SERCHandler) rcHandlers[i];
            pressedMask = pressedMask | h.getKeyPressedInterestMask();
            releasedMask = releasedMask | h.getKeyReleasedInterestMask();
            typedMask = typedMask | h.getKeyTypedInterestMask();
        }
        this.rcPressedInterest = pressedMask;
        this.rcReleasedInterest = releasedMask;
        this.keyTypedInterest = typedMask;
    }

    
    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException 
    {
        out.writeSuperClassData(this);
        out.writeFeaturesArrayReference(getActiveFeatures());
        out.writeFeaturesArrayReference(getSetupFeatures());
        out.writeRCHandlersArrayReference(getRCHandlers());
        out.writeCommands(getOnEntryCommands());
        out.writeBoolean(getNextOnSetupDone());
        out.writeCommands(getNextCommands());
        out.writeInt(rcPressedInterest);
        out.writeInt(rcReleasedInterest);
        out.writeInt(keyTypedInterest);
    }

    public String getRuntimeClassName() {
        return Segment.class.getName();
    }
   
    public void accept(SEShowVisitor visitor) {
        visitor.visitSegment(this);
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
        for (int i = 0; i < activeFeatures.length; i++) {
            if (activeFeatures[i] == from) {
                activeFeatures[i] = to;
            }
        }
        for (int i = 0; i < settingUpFeatures.length; i++) {
            if (settingUpFeatures[i] == from) {
                settingUpFeatures[i] = to;
            }
        }
    }

}
