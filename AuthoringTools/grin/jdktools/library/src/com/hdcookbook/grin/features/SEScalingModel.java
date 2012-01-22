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
import com.hdcookbook.grin.features.InterpolatedModel;
import com.hdcookbook.grin.commands.Command;

/** 
 * A scaling_model node.  This scales other features, with the anchor of
 * the scaling operation specified by an absolute x,y point.
 **/
public class SEScalingModel extends SEInterpolatedModel 
                            implements SEScalableNode 
{
    public SEScalingModel() {
    }
    
    public int[] getXs() {
        return getArray(InterpolatedModel.SCALE_X_FIELD);
    }
 
    public int[] getYs() {
        return getArray(InterpolatedModel.SCALE_Y_FIELD);
    }

    public int[] getScaleXs() {
        return getArray(InterpolatedModel.SCALE_X_FACTOR_FIELD);
    }
 
    public int[] getScaleYs() {
        return getArray(InterpolatedModel.SCALE_Y_FACTOR_FIELD);
    }

    public void accept(SEShowVisitor visitor) {
        visitor.visitScalingModel(this);
    }

    private int[] getArray(int field) {
        int[] result = values[field];
        if (result == null) {
            result = new int[frames.length];
            int value = currValues[field];
            for (int i =0; i < result.length; i++) {
                result[i] = value;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     **/
    public void scaleBy(int xScale, int yScale, int xOffset, int yOffset) {
        doScale(currValues, xScale, yScale, xOffset, yOffset);
        for (int i = 0; i < values.length; i++)  {
            doScale(values[i], xScale, yScale, xOffset, yOffset);
        }
    }

    private void doScale(int[] values, int xScale, int yScale, 
                                       int xOffset, int yOffset) 
    {
        if (values == null) {
            return;
        }
        values[SCALE_X_FIELD] 
            = xOffset + Show.scale(values[SCALE_X_FIELD], xScale);
        values[SCALE_Y_FIELD] 
            = yOffset + Show.scale(values[SCALE_Y_FIELD], yScale);
        values[SCALE_X_FACTOR_FIELD]
            = Show.scale(values[SCALE_X_FACTOR_FIELD], xScale);
        values[SCALE_Y_FACTOR_FIELD]
            = Show.scale(values[SCALE_Y_FACTOR_FIELD], yScale);
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        if (name == null) {
            return "scaling_model @" + Integer.toHexString(hashCode());
        } else {
            return "scaling_model " + name;
        }
    }
}
