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

package com.hdcookbook.grin.fontstrip;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.Node;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.animator.DrawRecord;
import com.hdcookbook.grin.animator.RenderContext;
import com.hdcookbook.grin.io.binary.GrinDataInputStream;
import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.ImageManager;
import com.hdcookbook.grin.util.ManagedImage;
import com.hdcookbook.grin.util.SetupClient;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;

/**
 * A feature for drawing font strip text.
 * <p>
 * See the file READM.TXT in the font strip extension base directory
 * for more details.
 **/
public class FontStripText extends Feature implements Node, SetupClient {
    
     /**
     * Value for alignment indicating that x refers to the left side
     * of the text.
     **/
    public final static int LEFT = 0x01;

    /**
     * Value for alignment indicating that x refers to the middle
     * of the text.
     **/
    public final static int MIDDLE = 0x02;

    /**
     * Value for alignment indicating that x refers to the right side
     * of the text.
     **/
    public final static int RIGHT = 0x03;

    /**
     * Value for alignment indicating that y refers to the top side
     * of the text.
     **/
    public final static int TOP = 0x04;

    /**
     * Value for alignment indicating that y refers to the baseline
     * of the text.
     **/
    public final static int BASELINE = 0x08;

    /**
     * Value for alignment indicating that y refers to the baseline
     * of the text.
     **/
    public final static int BOTTOM = 0x0c;

    /**
     * The alignment to apply to x and y.  The value is obtained by or-ing
     * (or by adding) a horizontal value (LEFT, MIDDLE or RIGHT) with
     * a vertical value (TOP, BASELINE or BOTTOM).
     **/
    protected int alignment;

    /**
     * The name of the fontstrip information file.
     */
    public final static String INFOFILE ="fontstrp.inf";

    protected int xArg;
    protected int yArg;
    protected String[] strings;
    private CharImageInfo[][] bakedStrings = null;
    protected String fontImageFileName;
    private FontImageFileInfo fontInfo;
    
    protected int hspace;
    protected int vspace;
    protected Color background;

    private boolean isActivated = false;
    private int alignedX;
    private int alignedY;
    private int drawX;
    private int drawY;
    private int drawWidth;
    private int drawHeight;
    private boolean changed = false;
    private DrawRecord drawRecord = new DrawRecord();
    private Object     setupMonitor = new Object();
    private boolean    setupMode    = false;    
    private boolean imageSetup = false;

    private ManagedImage fontImage = null;

    private static boolean loadingFailed = false;
    
    public FontStripText(Show show) {
        super(show);
    }

    /**
     * {@inheritDoc}
     **/
    protected Feature createClone(HashMap clones) {
        if (!setupMode || !imageSetup || isActivated) {
            throw new IllegalStateException();
        }
        FontStripText result = new FontStripText(show);
        result.alignment = alignment;
        result.xArg = xArg;
        result.yArg = yArg;
        result.strings = strings;
        result.bakedStrings = bakedStrings;
        result.fontImageFileName = fontImageFileName;
        result.fontInfo = fontInfo;
        result.hspace = hspace;
        result.vspace = vspace;
        result.background = background;
        result.isActivated = isActivated;
        result.alignedX = alignedX;
        result.alignedY = alignedY;
        result.drawX = drawX;
        result.drawY = drawY;
        result.drawWidth = drawWidth;
        result.drawHeight = drawHeight;
        result.changed = changed;
        result.setupMode = setupMode;
        result.imageSetup = imageSetup;
        result.fontImage = fontImage;
        result.loadingFailed = loadingFailed;

        if (!loadingFailed) {
            ImageManager.getImage(result.fontImage);
                // This increments the reference count of this ManagedImage.
                // See FixedImage.createClone() for details.
            result.fontImage.prepare();
                // Balanced by an unprepare in destroy()
        }
        return result;
    }
    
    public int getX() {
        return alignedX;
    }

    public int getY() {
        return alignedY;
    }

    public void initialize() {
        if (!FontImageFileInfo.initialized) {
            try {
                FontImageFileInfo.initFontImageFileInfo(INFOFILE);
            } catch (IOException e) {
                Debug.printStackTrace(e);
                loadingFailed = true;
            }
        }
        
        if (loadingFailed) {
            return;
        }
     
        fontInfo = FontImageFileInfo.getFontInfo(fontImageFileName);
        if (fontInfo == null) {
            if (Debug.ASSERT) {
                Debug.println("ERROR: entry for " + fontImageFileName + " not found in the info file.");
                Debug.assertFail();
            }
            loadingFailed = true;
            return;
        }        
        fontImage = ImageManager.getImage(fontImageFileName);

        bakeStrings();
    }

    private void bakeStrings() {
        changed = true;
        
        bakedStrings = new CharImageInfo[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            bakedStrings[i] = bakeString(strings[i]);
        }

        // We know the character size without loading the actual font image,
        // since the size is recorded in the fontstrip info size.
        int a = (alignment & 0x03);
        int width = 0;
        if (a == MIDDLE || a == RIGHT) {
            for (int i = 0; i < strings.length; i++) {
                CharImageInfo[] string = bakedStrings[i];
                int w = 0;
                for (int j=0; j < string.length; j++) {
                    CharImageInfo charInfo = string[j];
                    w += charInfo.width + hspace;
                }
                if (w > width) {
                    width = w;
                }
            }
        }
        if (a == MIDDLE) {
            alignedX = xArg - (width / 2);
        } else if (a == RIGHT) {
            alignedX = xArg - width;
        } else {
            alignedX = xArg;
        }
        a = (alignment & 0x0c);
        if (a == BASELINE) {
            alignedY = yArg - fontInfo.maxAscent;
        } else if (a == BOTTOM) {
            int lineHeight = fontInfo.maxAscent + fontInfo.maxDescent;
            int h = (vspace + fontInfo.maxLeading) * (strings.length - 1)
                     + (strings.length * lineHeight);
            alignedY = yArg - h;
        } else {
            alignedY = yArg;
        }

        // Now we calculate the bounding box.  For this, we look at the
        // actual drawing position of each character.
        // The character positioning is based on
        // the original font use to make the font strip (the "bound rect"
        // obtained from java.awt.font.TextLayout in SE), but the actual
        // drawing for any character can go outside those bounds.  For
        // example, the character glyphs might have been edited by
        // a graphics designer.

        drawX = alignedX;
        drawY = alignedY;
        int drawMaxX = alignedX;        // One pixel to right of pixel drawn
        int drawMaxY = alignedY;        // One pixel below pixel drawn

        int thisY = alignedY;
        for (int i = 0; i < strings.length; i++) {
            CharImageInfo[] string = bakedStrings[i];
            int thisX = alignedX;
            for (int j=0; j < string.length; j++) {
                CharImageInfo charInfo = string[j];
                if (charInfo.charRect.height != 0) {
                    int y = thisY + fontInfo.maxAscent - charInfo.ascent;
                    if (y < drawY) {
                        drawY = y;
                    }
                    int x = thisX + charInfo.xOffset;
                    if (x < drawX) {    // xOffset can be negative
                        drawX = x;
                    }
                    y += charInfo.charRect.height;
                    if (y > drawMaxY) {
                        drawMaxY = y;
                    }
                    x += charInfo.charRect.width;
                    if (x > drawMaxX) {
                        drawMaxX = x;
                    }
                }
                thisX += charInfo.width + hspace;
            }
            thisY += getLineHeight();
        }
        drawWidth = drawMaxX - drawX;
        drawHeight = drawMaxY - drawY;
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() {
        if (setupMode) {
            // That is, if this is a cloned feature.  See FixedImage.destroy().
            if (Debug.ASSERT && !imageSetup) {
                Debug.assertFail();
            }
            fontImage.unprepare();
        }
        if (!loadingFailed) {
            ImageManager.ungetImage(fontImage);
        } else if (Debug.ASSERT && fontImage != null) {
            Debug.assertFail();
        }
    }

    /**
     * {@inheritDoc}
     **/
    protected int setSetupMode(boolean mode) {
        if (loadingFailed) {
            return 0;
        }
        
        synchronized(setupMonitor) {
            setupMode = mode;
            if (setupMode) {
                fontImage.prepare();
                if (fontImage.isLoaded()) {
                    imageSetup = true;
                    return 0;
                } else {
                    show.setupManager.scheduleSetup(this);
                    return 1;
                }
            } else {
                fontImage.unprepare();
                imageSetup = false;
                return 0;
            }
        }
    }

    /**
     * {@inheritDoc}
     **/
    public void doSomeSetup() {
        synchronized(setupMonitor) {
            if (!setupMode) {
                return;
            }
        }
        fontImage.load(show.component);
        synchronized(setupMonitor) {
            if (!setupMode) {
                return;
            }
            imageSetup = true;
        }
        sendFeatureSetup();
    }

    /**
     * {@inheritDoc}
     **/
    public boolean needsMoreSetup() {
        synchronized (setupMonitor) {
            return setupMode && (!imageSetup);
        }
    }
    protected void setActivateMode(boolean mode) {
        this.isActivated = mode;
    }

    public void addDisplayAreas(RenderContext context) {
        drawRecord.setArea(drawX, drawY, drawWidth, drawHeight);
        if (changed) {
            drawRecord.setChanged();
        }
        drawRecord.setSemiTransparent();
        context.addArea(drawRecord);
        changed = false;
    }

    public void paintFrame(Graphics2D gr) {
        if (!isActivated || loadingFailed) {
            return;
        }
        if (background != null) {
            gr.setColor(background);
            gr.fillRect(drawX, drawY, drawWidth, drawHeight);
        }
        int y2 = alignedY + fontInfo.maxAscent;

        Composite old = gr.getComposite();
        boolean keepAlpha 
            = (old instanceof AlphaComposite) 
               && (((AlphaComposite) old).getRule() == AlphaComposite.SRC_OVER);
        if (!keepAlpha) {
            gr.setComposite(AlphaComposite.SrcOver);        
        }
        for (int i = 0; i < bakedStrings.length; i++) {
            drawString(gr, bakedStrings[i], alignedX, y2);
            y2 += getLineHeight();
        }
        if (!keepAlpha) {
            gr.setComposite(old);            
        }
    }

    public void nextFrame() {
        // nothing to do.
    }

    public void markDisplayAreasChanged() {
        drawRecord.setChanged();
    }

    public void readInstanceData(GrinDataInputStream in, int length) 
            throws IOException {
        in.readSuperClassData(this);
        this.xArg = in.readInt();
        this.yArg = in.readInt();
        this.alignment = in.readInt();
        this.strings = in.readStringArray();
        this.fontImageFileName = in.readString();
        this.hspace = in.readInt();
        this.vspace = in.readInt();
        this.background = in.readColor();     
        initialize();
    }


    // Get the character info for the given string
    private CharImageInfo[] bakeString(String string) {
        CharImageInfo[] infos = new CharImageInfo[string.length()];
        
        for (int i = 0; i < string.length(); i++) {
            Character ch = new Character(string.charAt(i));
            CharImageInfo charInfo = (CharImageInfo) fontInfo.charMap.get(ch);
            if (charInfo == null) {
               if (Debug.LEVEL > 0) {
		   Debug.println("No charInfo found for " + ch + " in " 
			   + fontImageFileName);
               }
               charInfo = new CharImageInfo();
               charInfo.ascent = 0;
               charInfo.xOffset = 0;
               charInfo.width = 0;
               charInfo.charRect = new Rectangle(0,0,0,0);       
               fontInfo.charMap.put(ch, charInfo);
           }
           infos[i] = charInfo;
        }
        return infos;
    }

   
    //
    // Draw string with starting at x with baseline at y
    //
    private void drawString(Graphics2D g2, CharImageInfo[] string, int x, int y)
    {
        for (int i = 0; i < string.length; i++) {
            CharImageInfo charInfo = string[i];
            fontImage.drawClipped(g2,
                    x + charInfo.xOffset, y - charInfo.ascent,
                    charInfo.charRect,
                    show.component);            
            x += charInfo.width + hspace;
        }        
    }
    
    /**
     * Get the text that's being displayed.
     **/
    public String[] getText() {
        return strings;
    }

    /**
     * Get the height of a line, including any vertical padding to take it
     * to the next line.
     **/
    public int getLineHeight() {
        return fontInfo.maxAscent + fontInfo.maxDescent + fontInfo.maxLeading 
               + vspace;
    }

    /**
     * Give the width of the string when drawn.  This method can be used
     * to format text.  If possible, this is better done off-line.
     * <p>
     * The actual bounding
     * box might be a bit larger, if the character's bounding box extends
     * to the left or to the right of the character starting position plus
     * its width.
     **/
    public int getStringWidth(String string) {
        int width = 0;
        int len = string.length();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Character ch = new Character(string.charAt(i));
                CharImageInfo charInfo 
                    = (CharImageInfo) fontInfo.charMap.get(ch);
                if (charInfo != null) {
                    width += charInfo.width;
                }
                width += hspace;
            }
            width -= hspace;
        }
        return width;
    }


    /** 
     * Change the text to display.
     * This should only be called with the show lock held, at an
     * appropriate time in the frame pump loop.  A good time to call
     * this is from within a command.
     * <p>
     * A good way to write this command that calls this is by using
     * the java_command structure.  There's an example of this in the
     * cookbook menu.
     **/
    public void setText(String[] newText) {
        synchronized(show) {    // Shouldn't be necessary, but doesn't hurt
            strings = newText;
            bakeStrings();
            // We don't want to do a full initialize() here, because that's
            // extra work, and because initialize() increments the reference
            // count for fontImageFile.
        }
    }
    
}
