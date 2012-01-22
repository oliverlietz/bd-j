
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

package com.hdcookbook.grin.mosaic;

import java.awt.Rectangle;

/**
 * Represents an arrangement of mosaic parts within a mosaic.
 * A Mosaic is compiled by creating several arrangements at different
 * widths, and picking the best one.  
 *
 *   @author     Bill Foote (http://jovial.com)
 **/
class Arrangement {

    private int maxHeight;
    private Rectangle[] pos;    // The positon of each part
    private int widthUsed = 0;
    private int heightUsed = 0;
    private int pixelsUsed;     // Integer.MAX_VALUE means "impossible"
    private int placed;         // # of rectangles fixed in place so far

    Arrangement(int maxHeight, MosaicPart[] parts) {
        this.maxHeight = maxHeight;
        pos = new Rectangle[parts.length];
        for (int i = 0; i < parts.length; i++) {
            pos[i] = new Rectangle(parts[i].getPlacement());
        }
    }

    void arrangeWithin(int maxWidth) {
        for (placed = 0; placed < pos.length; placed++) {
            Rectangle curr = pos[placed];
            curr.x = 0;
            curr.y = 0;
            if (curr.width > maxWidth) {        // Can't fit
                pixelsUsed = Integer.MAX_VALUE;
                return;
            }
            int nextY = maxHeight;
            boolean found = false;
            while (!found && curr.y + curr.height <= maxHeight) {
                found = true;
                for (int i = 0; found && i < placed; i++) {
                    Rectangle other = pos[i];
                    if (curr.intersects(other)) {
                        found = false;
                        curr.x = other.x + other.width;
                        int y = other.y + other.height;
                        if (y < nextY) {
                            nextY = y;
                        }
                        if (curr.x + curr.width > maxWidth) {
                            curr.x = 0;
                            assert curr.y < nextY;
                            curr.y = nextY;
                            nextY = maxHeight;
                        }
                    }
                }
            }
            if (!found) {
                pixelsUsed = Integer.MAX_VALUE;
                return;
            }
        }
        widthUsed = 0;
        heightUsed = 0;
        for (Rectangle r : pos) {
            int w = r.x + r.width;
            if (w > widthUsed) {
                widthUsed = w;
            }
            int h = r.y + r.height;
            if (h > heightUsed) {
                heightUsed = h;
            }
        }
        pixelsUsed = widthUsed * heightUsed;
    }

    //
    // Set the positions of the parts to correspond to the current
    // arrangement
    //
    void positionParts(MosaicPart[] parts) {
        for (int i = 0; i < parts.length; i++) {
            parts[i].setPosition(pos[i].x, pos[i].y);
        }
    }

    //
    // Return how many pixels current arrangement uses, Integer.MAX_VALUE
    // if no viable arrangement exists.
    //
    int getPixelsUsed() {
        return pixelsUsed;
    }

    //
    // Return the width used in the current arrangement
    //
    int getWidthUsed() {
        return widthUsed;
    }

    //
    // Return the height used in the current arrangement
    //
    int getHeightUsed() {
        return heightUsed;
    }
}
