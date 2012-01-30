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

import com.hdcookbook.grin.fontstrip.xml.FontDescription;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Represents intermediate font image file that the designer can possibly edit.
 */
public class FontDesignImage {

    static final int DEFAULT_WIDTH = 1600;
    static final int DEFAULT_HEIGHT = 4000;
    
    BufferedImage image = null;
    int maxPixelsX      = 0; 
    int maxPixelsY      = 0;    
    int currBaseline    = 0;
    int currAscent      = 0;
    int fontSize        = 0;
    int whitespace      = 0; // how many pixels to leave around each char
    
    Color backgroundColor = Color.WHITE;
    Color pixRectColor    = Color.RED;
    Color guidelineColor  = new Color(0x22ff00);
    Color fontColor       = Color.BLACK;
    CharInfo charInfo     = null;
    
    char[]  chars          = null;  // a list of characters that this file will hold.
    HashMap charMetricsMap = new HashMap();  // CharMetrics corresponding to char[].
    File    outputFile     = null;  // physical image file to output.
    
    public FontDesignImage(FontDescription description, int width, int height) {
        maxPixelsX = width;
        maxPixelsY = height;       
        setFont(description.getFont());
        setWhitespace(description.getMargin());
        setColors(description.getBackgroundColor(), description.getPixelRectColor(),
                description.getBoundingRectColor(), description.getCharactorColor());
    }
    
    public void setColors(Color backgroundColor,
            Color pixRectColor, Color guidelineColor, Color fontColor) {
        this.backgroundColor = backgroundColor;
        this.pixRectColor    = pixRectColor;
        this.guidelineColor  = guidelineColor;
        this.fontColor       = fontColor;  
    }
    
    public void setWhitespace(int pixels) {
        this.whitespace = pixels;
    }
    
    public void setFont(Font font) {
        fontSize = font.getSize();
        charInfo = new CharInfo(font);
    }
    
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }    
    
    public void setCharactorArray(char[] chars) {
        if (this.chars != null) {
            System.err.println("Character list already set");
            return;
        }
        this.chars = chars;
        image = new BufferedImage(maxPixelsX, maxPixelsY, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        charInfo.setFontRenderContext(g2.getFontRenderContext()); 
        currAscent = fontSize + whitespace;
        currBaseline = fontSize + whitespace;
        for (char ch : chars) {
             CharMetrics metrics = calculateCharacterMetrics(ch);
             charMetricsMap.put(ch, metrics);
        }
        g2.dispose();
    }
    
    /**
     * Writes out the content of the BufferedImage to a file.
     */
    public void writeOutImageFile() throws IOException {                    
        BufferedOutputStream out =
                new BufferedOutputStream(
                new FileOutputStream(outputFile));
        drawCharacters(); // this draws all chars to the BufferedImage
        Iterator writers = ImageIO.getImageWritersByFormatName(
                FontStripImageGenerator.getFormatName(outputFile));
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        writer.setOutput(ios);
        IIOImage iioImage = new IIOImage(getCurrentImage(), null, null);
        writer.write(null, iioImage, null);
        System.out.println("Writing: " + outputFile);
        out.close();
    }
    
    private void drawCharacters() {
        
        Graphics2D g2 = image.createGraphics();
        for (char ch : chars) {
            CharMetrics charMetrics = (CharMetrics) charMetricsMap.get(ch);          
            Rectangle boundRect = charMetrics.getBoundingRect();
            Rectangle pixRect = charMetrics.getPixRect();
            int baseline = charMetrics.getBaseline();
            Rectangle union = boundRect.union(pixRect);
            union.grow(whitespace, whitespace);

            if (backgroundColor != null) {
              g2.setColor(backgroundColor);
              g2.fillRect(union.x, union.y, union.width, union.height);
            } 
            
            g2.setColor(guidelineColor);
            
            // draw the char's boundry
            g2.drawRect(boundRect.x, boundRect.y, boundRect.width + 1, boundRect.height + 1);

            // draw the max area that can be changed
            g2.drawRect(union.x, union.y, union.width, union.height);
            
            // draw the baseline
            g2.drawLine(union.x, baseline, union.width+union.x, baseline);

            // draw the pix rect
            g2.setColor(pixRectColor);
            g2.drawRect(pixRect.x, pixRect.y, pixRect.width + 1, pixRect.height + 1);

            // draw the char
            g2.setColor(fontColor);
            charInfo.setChar(ch);
            TextLayout layout = charInfo.getLayout();            
            layout.draw((Graphics2D) g2, boundRect.x, baseline);
        }
        g2.dispose();
    }
    
    private BufferedImage getCurrentImage() {
        if (currBaseline + fontSize/2 > image.getHeight()) {
            System.err.println("Warning: Generated image is larger than buffer for file " + outputFile);
            return image.getSubimage(0,0,image.getWidth(), image.getHeight());
        }
        return image.getSubimage(0,0,maxPixelsX,currBaseline+fontSize/2+whitespace);
    }
    
    public void discardCurrentImage() {
        image.flush();
        image = null;
    }

    private CharMetrics calculateCharacterMetrics(char ch) {
        
        if (charInfo == null) {
            System.err.println("Font not set.");
            return null;
        }

        charInfo.setChar(ch);        
        Rectangle pixRect = charInfo.getPixRect(currAscent, currBaseline);
        int diff = currAscent - pixRect.x; 
        int adjustment = 0;
        if (diff > 0) {
            adjustment = diff;
            currAscent += adjustment;
            pixRect.translate(adjustment, 0);
        } 
        
        Rectangle boundRect = charInfo.getBoundingRect(currAscent, currBaseline);       
        Rectangle union = pixRect.union(boundRect);
        union.grow(whitespace, whitespace);    
        
        if ((union.x + union.width) >= maxPixelsX) {  // next line
            currBaseline += fontSize*2;
            currAscent    = fontSize+whitespace;
            pixRect = charInfo.getPixRect(currAscent, currBaseline);
            boundRect = charInfo.getBoundingRect(currAscent, currBaseline);
            union = pixRect.union(boundRect);
            union.grow(whitespace, whitespace);
        }
              
        //System.out.println("currBaseline    =" + currBaseline);
        //System.out.println("currAscent      =" + currAscent);
        //System.out.println("PixRect         =" + pixRect);
        //System.out.println("boundingRect    =" + boundRect);
        //System.out.println("Union + mergin  =" + union + ", " + whitespace + "\n");
        
        CharMetrics charMetrics = new CharMetrics(ch, currBaseline, 
                pixRect, boundRect, charInfo.getLeading());
        
        // calculate the next char's starting position
        currAscent += union.width; 
        
        return charMetrics;
        
    }

    /**
     * A convenience class to measure font metrics needed for
     * generating FontDesignImage.  Data from this class is recorded
     * into CharMetrics.
     */
    class CharInfo {
        Font font = null;
        FontRenderContext frc = null;
        TextLayout layout = null;
        public CharInfo(Font font) {
            this.font = font;
        }        
        public void setFontRenderContext(FontRenderContext frc) {
            this.frc = frc;
        }
        public void setChar(char ch) {
            layout = new TextLayout("" + ch, font, frc);
        }
        public Rectangle getPixRect(float x, float y) {
            if (layout == null) {
                System.err.println("Character for CharInfo not set.");
                return null;
            }
            return layout.getPixelBounds(frc, x, y);
        }
        public Rectangle getBoundingRect(float x, float y) {
            if (layout == null) {
                System.err.println("Character for CharInfo not set.");
                return null;
            }    
            return new Rectangle(Math.round(x),
                    Math.round(y - layout.getAscent()),
                    Math.round(layout.getAdvance()),
                    Math.round(layout.getAscent() + layout.getDescent()));
        }

        public int getLeading() {
            return (int) layout.getLeading();
        }

        public int getAscent() {
            return (int) Math.round(layout.getAscent());
        }
         
        private Font getFont() {
            return font;
        }

        private TextLayout getLayout() {
            return layout;
        }
    }
}
