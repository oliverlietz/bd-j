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

package com.hdcookbook.grin.input;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.SEScalableNode;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowVisitor;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Assembly;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import com.hdcookbook.grin.io.builders.VisualRCHandlerHelper;
import java.awt.Rectangle;
import java.io.IOException;

public class SEVisualRCHandler 
                extends VisualRCHandler 
                implements SENode, SEScalableNode, SERCHandler
{

    private VisualRCHandlerHelper helper;

    public SEVisualRCHandler(SEShow show, VisualRCHandlerHelper helper) {
        super();
        setShow(show);
        this.helper = helper;
    }

    public SEVisualRCHandler(String name, String[] gridAlternateNames,
                             String[] stateNames,
                             int[][] upDownAlternates, 
                             int[][] rightLeftAlternates,
                             Command[][] selectCommands, 
                             Command[][] activateCommands, 
                             Rectangle[] mouseRects, int[] mouseRectStates,
                             int timeout, Command[] timeoutCommands,
                             boolean startSelected,
                             VisualRCHandlerHelper helper) 
    {
        super();
        this.name = name;
        this.gridAlternateNames = gridAlternateNames;
        this.stateNames = stateNames;
        this.upDownAlternates = upDownAlternates;
        this.rightLeftAlternates = rightLeftAlternates;
        this.upDown = upDownAlternates[0];
        this.rightLeft = rightLeftAlternates[0];
        this.selectCommands = selectCommands;
        this.activateCommands = activateCommands;
        this.mouseRects = mouseRects;
        this.mouseRectStates = mouseRectStates;
        this.timeout = timeout;
        this.timeoutCommands = timeoutCommands;
        this.startSelected = startSelected;
        this.helper = helper;
    }

    /**
     * Called from the parser
     **/
    public void setup(Assembly assembly, Feature[] selectFeatures, 
                      Feature[] activateFeatures)
    {
        this.assembly = assembly;
        this.selectFeatures = selectFeatures;
        this.activateFeatures = activateFeatures;
        // activating this handler can change its state
    }

    /**
     * {@inheritDoc}
     **/
    public int getKeyPressedInterestMask() {
        return VisualRCHandler.MASK;
    }

    /**
     * {@inheritDoc}
     **/
    public int getKeyReleasedInterestMask() {
        return 0;
    }

    /**
     * {@inheritDoc}
     **/
    public int getKeyTypedInterestMask() {
        return 0;
    }

    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException {
        
        out.writeSuperClassData(this);
        
        out.writeStringArray(gridAlternateNames);
        out.writeInt(upDownAlternates.length);
        for (int i = 0; i < upDownAlternates.length; i++) {
            out.writeSharedIntArray(upDownAlternates[i]);
            out.writeSharedIntArray(rightLeftAlternates[i]);
        }
        out.writeStringArray(stateNames);
        if (selectCommands == null) {
            out.writeNull();
        } else {
            out.writeNonNull();
            out.writeInt(selectCommands.length);
            for (int i = 0; i < selectCommands.length; i++) {
                out.writeCommands(selectCommands[i]);
            }
        }
        if (activateCommands == null) {
            out.writeNull();
        } else {
            out.writeNonNull();
            out.writeInt(activateCommands.length);
            for (int i = 0; i < activateCommands.length; i++) {
                out.writeCommands(activateCommands[i]);
            }
        }
        
        out.writeRectangleArray(mouseRects);
        out.writeIntArray(mouseRectStates);
        out.writeInt(timeout);
        out.writeCommands(timeoutCommands);
       
        out.writeBoolean(assembly != null);
        if (assembly != null) {
            out.writeFeatureReference(assembly);
        }
        
        out.writeFeaturesArrayReference(selectFeatures);
        out.writeFeaturesArrayReference(activateFeatures);
        out.writeBoolean(startSelected);
    }

    public String getRuntimeClassName() {
        return VisualRCHandler.class.getName();
    }
    
    public void accept(SEShowVisitor visitor) {
        visitor.visitVisualRCHandler(this);
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
        for (int i = 0; i < selectFeatures.length; i++) {
            if (selectFeatures[i] == from) {
                selectFeatures[i] = to;
            }
        }
        for (int i = 0; i < activateFeatures.length; i++) {
            if (activateFeatures[i] == from) {
                activateFeatures[i] = to;
            }
        }
    }

    /**
     * {@inheritDoc}
     **/
    public void scaleBy(int xScale, int yScale, int xOffset, int yOffset) {
        if (mouseRects != null) {
            for (int i = 0; i < mouseRects.length; i++) {
                Rectangle r = mouseRects[i];
                r.x = xOffset + Show.scale(r.x, xScale);
                r.y = yOffset + Show.scale(r.y, yScale);
                r.width = Show.scale(r.width, xScale);
                r.height = Show.scale(r.height, yScale);
            }
        }
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        if (getName() == null) {
            return "rc_handler visual @" + Integer.toHexString(hashCode());
        } else {
            return "rc_handler visual " + getName();
        }
    }
}
