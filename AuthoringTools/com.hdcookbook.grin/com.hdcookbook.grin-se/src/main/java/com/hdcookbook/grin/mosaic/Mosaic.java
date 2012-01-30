
/*  
 * Copyright (c) 2007, Sun Microsystems, Inc.
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

package com.hdcookbook.grin.mosaic;

import com.hdcookbook.grin.MosaicSpec;
import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.ManagedImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import javax.imageio.ImageIO;

/**
 * Represents a single mosaic image, composed of a number of
 * parts, or tiles.
 *
 *   @author     Bill Foote (http://jovial.com)
 **/
public class Mosaic {

    private AbstractList<MosaicPart> partsList = new LinkedList<MosaicPart>();
    MosaicPart[] parts = null;

    private Component progressComponent = null;
    private int maxWidth;
    private int maxHeight;
    private int minWidth;
    private int numWidths;
    private int maxPixels;      
    private String outputName;

    private int position;
    private int currPixels = Integer.MAX_VALUE; // # pixels occupied
    private int currWidth = 0;
    private int currHeight = 0;

    private BufferedImage buffer;
    private Graphics2D graphics;        // into buffer

    /** 
     * Create a new mosaic, with the given parameters
     **/
    public Mosaic(MosaicSpec spec) {
        maxWidth = spec.maxWidth;
        maxHeight = spec.maxHeight;
        minWidth = spec.minWidth;
        numWidths = spec.numWidths;
        maxPixels = spec.maxPixels;
        outputName = spec.name;
    }

    public int getHeightUsed() {
        return currHeight;
    }

    public int getWidthUsed() {
        return currWidth;
    }

    /**
     * Put the image represented by mi and im into the mosaic.
     * This always works, and returns a MosaicPart.
     **/
    public MosaicPart putImage(ManagedImage mi, int maxWidth, int maxHeight) {
        MosaicPart part = new MosaicPart(mi, this, maxWidth, maxHeight);
        partsList.add(part);
        return part;
    }

    /**
     * Compile this mosaic into an optimal arrangement.
     *
     * @return true if this mosaic is used for images, false if it's empty
     **/
    public boolean compile(Component progressComponent) {
        this.progressComponent = progressComponent;
        parts = partsList.toArray(new MosaicPart[partsList.size()]);
        Arrangement arrangement = new Arrangement(maxHeight, parts);
        int lastWidth = -1;
        double widthFactor = Math.log(((double) maxWidth) / minWidth);
        for (int i = 0; i < numWidths; i++) {
            int width;
            if (numWidths > 1) {
                // Increase width geometrically from minWidth to maxWidth
                double f = widthFactor * ((double) i) / (numWidths - 1);
                f = Math.exp(f) * minWidth;
                width = (int) (f + 0.5);
            } else {
                width = maxWidth;
            }
            if (width == lastWidth) {
                continue;       // Don't try same width twice
            }
            lastWidth = width;
            arrangement.arrangeWithin(width);
            int pixels = arrangement.getPixelsUsed();
            if (pixels < currPixels) {
                setBestArrangement(arrangement);
            }
        }
        return currPixels > 0;
    }

    //
    // Sets the best arrangement seen so far to the argument.
    // Copies all needed data from arrangement, so that Arrangement
    // can be subsequently modified.
    //
    void setBestArrangement(Arrangement arrangement) {
        currPixels = arrangement.getPixelsUsed();
        synchronized(parts) {
            arrangement.positionParts(parts);
            currWidth = arrangement.getWidthUsed();
            currHeight = arrangement.getHeightUsed();
        }
        Component c = progressComponent;
        if (c != null) {
            c.repaint(100L);
        }
    }

    int getCurrPixels() {
        return currPixels;
    }

    void paintStatus(Graphics2D g) {
        if (parts == null) {
            return;
        }
        g.setComposite(AlphaComposite.Src);
        synchronized(parts) {
            if (currWidth == 0 || currHeight == 0 || progressComponent==null) {
                return;
            }
            float scaleX = ((float) progressComponent.getWidth()) / currWidth;
            float scaleY = ((float) progressComponent.getHeight()) / currHeight;
            if (scaleX < scaleY) {
                scaleY = scaleX;
            } else {
                scaleX = scaleY;
            }
            g.setColor(Color.yellow);
            g.fillRect(0, 0, (int) (currWidth * scaleX), 
                             (int) (currHeight * scaleY));
            Rectangle p = new Rectangle();
            for (MosaicPart part : parts) {
                Rectangle r = part.getPlacement();
                p.x = (int) (r.x * scaleX);
                p.y = (int) (r.y * scaleY);
                p.width = (int) (r.width * scaleX);
                p.height = (int) (r.height * scaleY);
                part.getImage().drawScaled(g, p, progressComponent);
            }
        }
    }


    /**
     * Get the name of the file we'll be written to.
     **/
    public String getOutputName() {
        return outputName;
    }

    /**
     * Set our position within the binary list of mosaics that's
     * written out for GRIN to consult at runtime.
     **/
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Get our position within the binary list of mosaics that's
     * written out for GRIN to consult at runtime.
     **/
    public int getPosition() {
        return position;
    }

    /**
     * Write out our image buffer as a PNG image.
     **/
    public void writeMosaicImage(File out) throws IOException {
        if (currPixels == Integer.MAX_VALUE) {
            throw new IOException("Unable to create arrange images in mosaic " 
                                   + out);
        }
        if (currPixels > maxPixels) {
            throw new IOException("Image too big (" + currPixels + " > "
                                  + maxPixels + ") -- see part 3.2 section G.5 "
                                  + "table G-4 \"Image size\".\n");
        }
        BufferedImage buffer = new BufferedImage(currWidth, currHeight, 
                                        BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics  = (Graphics2D)  buffer.getGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setColor(Color.yellow);
        graphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        for (MosaicPart part : parts) {
            Rectangle r = part.getPlacement();
            part.getImage().drawScaled(graphics, r, null);
        }
        boolean ok = ImageIO.write(buffer, "PNG", out);
        if (!ok) {
            throw new IOException("No writer found");
        }
    }
}
