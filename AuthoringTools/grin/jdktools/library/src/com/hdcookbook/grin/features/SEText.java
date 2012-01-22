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

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.SEScalableNode;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowVisitor;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

public class SEText extends Text implements SENode, SEScalableNode {

    public SEText(SEShow show, String name, int x, int y, int alignment,
                String[] strings, int vspace, int fontIndex, Color[] colors, 
                int loopCount, Color background) 
    {
        super(show);
        this.name = name;
        this.xArg = x;
        this.yArg = y;
        this.alignment = alignment;
        this.strings = strings;
        this.vspace = vspace;
        this.fontIndex = fontIndex;
        this.colors = colors;
        this.loopCount = loopCount;
        this.background = background;
    }
    
    public SEText(SEShow show) {
        super(show);
    }
        
    public String[] getStrings() {
        return strings;
    }
    
    public int getVspace() {
        return vspace;
    }
    
    public int getFontIndex() {
        return fontIndex;
    }
    
    public Color[] getColors() {
        return colors;
    }
    
    public Color getBackground() {
        return background;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }
    
    public void setVspace(int vspace) {
        this.vspace = vspace;
    }
    
    public void setFontIndex(int index) {
        this.fontIndex = index;
    }
    
    public void setColors(Color[] colors) {
        this.colors = colors;
    }
    
    public void setBackground(Color background) {
        this.background = background;
    }    
   
    public int getXArg() {
        return xArg;
    }

    public void setXArg(int x) {
        this.xArg = x;
    }

    public int getYArg() {
        return yArg;
    }

    public void setYArg(int y) {
        this.yArg = y;
    }

    public int getAlignment() {
        return alignment;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException {
        
       out.writeSuperClassData(this);
       out.writeInt(xArg);
       out.writeInt(yArg);
       out.writeInt(alignment);
       out.writeStringArray(getStrings());
       out.writeInt(getVspace());
       out.writeInt(fontIndex);
       out.writeInt(colors.length);
       for (int i = 0; i < colors.length; i++) {
          out.writeColor(colors[i]);  
       }
       out.writeInt(loopCount);
       out.writeColor(getBackground());                 
    }

    public String getRuntimeClassName() {
        return Text.class.getName();
    }

    public void accept(SEShowVisitor visitor) {
        visitor.visitText(this);
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
    public void scaleBy(int xScale, int yScale, int xOffset, int yOffset) {
        xArg = xOffset + Show.scale(xArg, xScale);
        yArg = yOffset + Show.scale(yArg, yScale);
        vspace = Show.scale(vspace, yScale);
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        if (name == null) {
            return "text @" + Integer.toHexString(hashCode());
        } else {
            return "text " + name;
        }
    }
}
