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

package com.hdcookbook.grin.features;

import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.SEScalableNode;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowVisitor;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

public class SETranslator extends Translator implements SENode, SEScalableNode {
    
    public SETranslator(SEShow show) {
        super(show);
    }
    
    public SETranslator(SEShow show, String name) {
        super(show);
        setName(name);
    }    

    /**
     * Used by binary writer
     **/
    public int getAbsoluteXOffset() {
        return fx;
    }
    /**
     * Used by binary writer
     **/
    public int getAbsoluteYOffset() {
        return fy;
    }

    /**
     * Used by binary writer
     **/
    public boolean getModelIsRelative() {
        return modelIsRelative;
    }    
    
    public void setModel(InterpolatedModel model) {
        this.model = model;
    }

    public void setup(InterpolatedModel model, Feature part) {
        super.setup(part);
        this.model = model;
    }
    
    /**
     * Called from the parser for the x offset, useful
     * only when our model uses absolute coordinates
     **/
    public void setupAbsoluteXOffset(int x) {
        this.fx = x;
    }

    /**
     * Called from the parser for the y offset, useful
     * only when our model uses absolute coordinates
     **/
    public void setupAbsoluteYOffset(int y) {
        this.fy = y;
    }

    /**
     * Called from the parser and binary reader to determine if our model
     * uses absolute coordinates.  Absolute coordinates are deeprecated.
     **/
    public void setupModelIsRelative(boolean b) {
        modelIsRelative = b;
    }


    
    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException {
        
        out.writeSuperClassData(this);
        out.writeInt(getAbsoluteXOffset());
        out.writeInt(getAbsoluteYOffset());
        out.writeBoolean(getModelIsRelative());
        out.writeFeatureReference(getModel());   
    }

    public String getRuntimeClassName() {
        return Translator.class.getName();
    }

    public void accept(SEShowVisitor visitor) {
        visitor.visitTranslator(this);
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
        if (part == from) {
            part = to;
        }
    }

    /**
     * {@inheritDoc}
     **/
    public void scaleBy(int xScale, int yScale, int xOffset, int yOffset) {
        if (!modelIsRelative) {
            fx = xOffset + Show.scale(fx, xScale);
            fy = yOffset + Show.scale(fy, yScale);
        }
        // Our translation_model gets scaled, too
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        if (name == null) {
            return "translator @" + Integer.toHexString(hashCode());
        } else {
            return "translator " + name;
        }
    }
}
