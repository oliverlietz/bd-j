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
import java.awt.Rectangle;
import java.io.IOException;

public class SEBox extends Box implements SENode, SEScalableNode {
    
    public SEBox(SEShow show) {
        super(show);
    }
    
    public SEBox(SEShow show, String name, int x, int y, int width, int height,
               int outlineWidth, Color outlineColor, Color fillColor)
    {
        super(show);
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.outlineWidthX = outlineWidth;
        this.outlineWidthY = outlineWidth;
        this.outlineColor = outlineColor;
        this.fillColor = fillColor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public int getOutlineWidthX() {
       return outlineWidthX;
    }
    
    public int getOutlineWidthY() {
       return outlineWidthY;
    }
    
    public Color getOutlineColor() {
       return outlineColor;
    }
    
    public Color getFillColor() {
       return fillColor;
    }

    public void setScalingModel(InterpolatedModel model) {
        this.scalingModel = model;
        if (model != null) {
            scaledBounds = new Rectangle();
        }
    }

    public InterpolatedModel getScalingModel() {
        return scalingModel;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public void setWidth(int w) {
        this.width = w;
    }
    
    public void setHeight(int h) {
        this.height = h;
    }
    
    public void setOutlineWidthX(int w) {
        this.outlineWidthX = w;
    }
    
    public void setOutlineWidthY(int w) {
        this.outlineWidthY = w;
    }
    
    public void setOutlineColor(Color color) {
        this.outlineColor = color;
    }
    
    public void setFillColor(Color color) {
        this.fillColor = color;
    }
    
    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException 
    {
       out.writeSuperClassData(this);
       out.writeInt(getX());
       out.writeInt(getY());
       out.writeInt(getWidth());
       out.writeInt(getHeight());
       out.writeInt(getOutlineWidthX());
       out.writeInt(getOutlineWidthY());
       out.writeColor(getOutlineColor());
       out.writeColor(getFillColor());
       out.writeBoolean(scalingModel != null);
       if (scalingModel != null) {
            out.writeFeatureReference(getScalingModel());
       } 
    }

    public String getRuntimeClassName() {
        return Box.class.getName();
    }

    public void accept(SEShowVisitor visitor) {
        visitor.visitBox(this);
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
        x = xOffset + Show.scale(x, xScale);
        y = yOffset + Show.scale(y, yScale);
        width = Show.scale(width, xScale);
        height = Show.scale(height, yScale);
        outlineWidthX = Show.scale(outlineWidthX, xScale);
        outlineWidthY = Show.scale(outlineWidthY, yScale);
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        if (name == null) {
            return "box @" + Integer.toHexString(hashCode());
        } else {
            return "box " + name;
        }
    }
}
