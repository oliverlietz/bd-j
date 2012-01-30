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
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.parts.SEImageSeqPlacement;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.io.IOException;

public class SEImageSequence extends ImageSequence 
                             implements SENode, SEScalableNode 
{
    private SEImageSeqPlacement sePlacement;

    public SEImageSequence(SEShow show) {
        super(show);
    }
    
    public SEImageSequence(SEShow show, String name, 
                           SEImageSeqPlacement sePlacement, String fileName,
                           String[] middle, String extension, boolean repeat,
                           int loopCount, Command[] endCommands) 
                throws IOException {
        super(show);
        this.name = name;
        this.sePlacement = sePlacement;
        this.repeat = repeat;
        this.model = null;
        this.loopCount = loopCount;
        this.endCommands = endCommands;
        
        fileNames = new String[middle.length];
        for (int i = 0; i < middle.length; i++) {
            if (middle[i] == null) {
                fileNames[i] = null;
            } else {
                fileNames[i] = (fileName + middle[i] + extension).intern();
            }
        }
        this.placements = sePlacement.getImageSeqPlacementRects(fileNames);
    }
    
    public SEImageSeqPlacement getPlacement() {
       return sePlacement;
    }

    public Dimension[] getImageSizes() {
        Dimension[] result = new Dimension[placements.length];
        for (int i = 0; i < result.length; i++) {
            Rectangle r = placements[i];
        if (r == null) {
           result[i] = null;
        } else {
               result[i] = new Dimension(r.width, r.height);
        }
        }
        return result;
    }

    public boolean getRepeat() {
       return repeat;
    }
    
    public Command[] getEndCommands() {
       return endCommands;
    }
    
    public ImageSequence getModel() {
        return model;
    }
    
    public String[] getFileNames() {
        return fileNames;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
    
    public void setEndCommands(Command[] endCommands) {
       this.endCommands = endCommands;
    }
    
    public void setModel(ImageSequence model) {
        if (model.fileNames.length != fileNames.length) { 
            throw new RuntimeException("Mismatched number of frames in model");
        }
        this.model = model;
    }
    
    public void setFileNames(String[] fileNames) {
        this.fileNames = fileNames;
    }
    
    public void setScalingModel(InterpolatedModel model) {
        this.scalingModel = model;
        if (model != null) {
            scaledBounds = new Rectangle();
        }
    }

    public void setPlacements(Rectangle[] placements) {
        this.placements = placements;
    }

    public InterpolatedModel getScalingModel() {
        return scalingModel;
    }
    
    public void writeInstanceData(GrinDataOutputStream out) throws IOException 
    {
       out.writeSuperClassData(this);
       out.writeSharedRectangleArray(placements);
       out.writeStringArray(getFileNames());
       out.writeBoolean(getRepeat());
       out.writeBoolean(model != null);
       if (model != null) {
           out.writeFeatureReference(model);
       }
       out.writeInt(loopCount);
       out.writeCommands(endCommands);
       out.writeBoolean(scalingModel != null);
       if (scalingModel != null) {
           out.writeFeatureReference(scalingModel);
       }  
    }

    public String getRuntimeClassName() {
        return ImageSequence.class.getName();
    }
    
    public void accept(SEShowVisitor visitor) {
        visitor.visitImageSequence(this);
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
    public void scaleBy(int xScale, int yScale, int xOffset, int yOffset) {
        Rectangle[] p = placements;
        placements = new Rectangle[p.length];
        for (int i = 0; i < placements.length; i++) {
            placements[i] = new Rectangle(xOffset + Show.scale(p[i].x, xScale),
                                          yOffset + Show.scale(p[i].y, yScale),
                                          Show.scale(p[i].width, xScale),
                                          Show.scale(p[i].height, yScale));
        }
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        if (name == null) {
            return "image_sequence @" + Integer.toHexString(hashCode());
        } else {
            return "image_sequence " + name;
        }
    }
}
