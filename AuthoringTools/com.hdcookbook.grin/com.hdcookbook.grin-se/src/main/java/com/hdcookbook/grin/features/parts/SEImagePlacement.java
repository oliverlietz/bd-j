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

package com.hdcookbook.grin.features.parts;

import com.hdcookbook.grin.util.AssetFinder;
import com.hdcookbook.grin.util.ImageManager;
import com.hdcookbook.grin.util.ManagedImage;
import com.hdcookbook.grin.util.ManagedSubImage;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class represents an image_placement structure.  This structure
 * determines the placement (x,y position, width and height) of an image,
 * or of an image sequence, or an image within an image sequence.
 **/
public class SEImagePlacement implements SEImageSeqPlacement {


    private int x;
    private HorizontalAlignment xAlign;
    private int y;
    private VerticalAlignment yAlign;
    private double scaleX;      // may be negative
    private double scaleY;      // may be negative

    public SEImagePlacement() {
        x = 0;
        xAlign = HorizontalAlignment.LEFT;
        y = 0;
        yAlign = VerticalAlignment.TOP;
        scaleX = 1.0;
        scaleY = 1.0;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleX() {
        return this.scaleX;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    public double getScaleY() {
        return this.scaleY;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public void setXAlign(HorizontalAlignment xAlign) {
        this.xAlign = xAlign;
    }

    public HorizontalAlignment getXAlign() {
        return this.xAlign;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return this.y;
    }

    public void setYAlign(VerticalAlignment yAlign) {
        this.yAlign = yAlign;
    }
    
    public VerticalAlignment getYAlign() {
        return this.yAlign;
    }

    
    /**
     * {@inheritDoc}
     **/
    public Rectangle[] getImageSeqPlacementRects(String[] images) 
                throws IOException
    {
        Rectangle[] result = new Rectangle[images.length];
        for (int i = 0; i < images.length; i++) {
            String im = images[i];
            if (im == null) {
                result[i] = null;
            } else {
                result[i] = getImagePlacementRect(images[i]);
            }
        }
        return result;
    }

    /**
     * Get the rectangle for this placement when used in a fixed_image
     * feature.  This reqires reading and decoding the image passed in,
     * to get the width and height.
     * <p>
     * In keeping with the Graphics.draw() method that takes dx1, dx2 etc., 
     * the resulting rectangle's x position will be one past the right edge
     * if the width is negative, and the y position will be one past the
     * bottom edge if the height is negative.  That is, it will be set up
     * so you can call 
     * <code>g.draw(..., r.x, r.y, r.x+r.width, r.y+r.height, ...)</code>.
     *
     * @throws  IOException     If there's a problem reading the image file
     **/
    public Rectangle getImagePlacementRect(String imageFileName) 
                throws IOException 
    {
        Rectangle result = new Rectangle();

        // 
        // First, we see if this image exists in a mosaic.  This can
        // happen with GrinView, and in this case the original image
        // file isn't available.  If we don't find a mosaic tile, then
        // we use ImageIO, because that works even if we're running
        // in a program that has no GUI.
        //
        ManagedImage mi = ImageManager.getImage(imageFileName);
        try {
            if (mi instanceof ManagedSubImage) {
                result.width = (int) Math.round(mi.getWidth() * scaleX);
                result.height = (int) Math.round(mi.getHeight() * scaleY);
            } else {
                BufferedImage im = ImageIO.read(
                                        AssetFinder.getURL(imageFileName));
                result.width = (int) Math.round(im.getWidth() * scaleX);
                result.height = (int) Math.round(im.getHeight() * scaleY);
            }
        } finally {
            ImageManager.ungetImage(mi);
        }

        if (xAlign == HorizontalAlignment.LEFT) {
            result.x = x;
        } else if (xAlign == HorizontalAlignment.MIDDLE) {
            result.x = x - (int) Math.round(0.5 * Math.abs(result.width));
        } else {
            assert xAlign == HorizontalAlignment.RIGHT;
            result.x = x - Math.abs(result.width);
        }
        if (yAlign == VerticalAlignment.TOP) {
            result.y = y;
        } else if (yAlign == VerticalAlignment.MIDDLE) {
            result.y = y - (int) Math.round(0.5 * Math.abs(result.height));
        } else {
            assert yAlign == VerticalAlignment.BOTTOM;
            result.y = y - Math.abs(result.height);
        }
        if (result.width < 0) {
            result.x -= result.width;   // Set up for Graphics.draw(...)
        }
        if (result.height < 0) {
            result.y -= result.height;
        }
        return result;
    }
}
