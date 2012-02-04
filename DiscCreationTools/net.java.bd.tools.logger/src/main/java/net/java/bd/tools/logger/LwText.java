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

package net.java.bd.tools.logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * A lightweight text box.
 */
public class LwText extends Component {
    public static final String lineSep = System.getProperty("line.separator"); 
    
    
    public LwText(String[] text) {
        this.text = text;
        this.fm = getFontMetrics(Screen.getDefaultFont());
    }

    public LwText(String text) {
        this(new String[] {text});
    }

    public LwText(String text, Color back, Color front) {
        this(text);
        this.back = back;
        this.front = front;
    }

    public LwText(String[] text, Color back, Color front) {
        this(text);
        this.back = back;
        this.front = front;
    }

    public void paint(Graphics g) {
        maxLineLength = 0;
        g.setColor(back);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (numberOfLinesDisplayed == -1) {
            numberOfLinesDisplayed = getHeight() / fm.getHeight();
        }
        g.setColor(front);
        if (text != null && text.length > 0) {
            for (int i = 0; i < text.length && i < numberOfLinesDisplayed; i++) {
                if (text[i] != null) {
                    g.drawString(text[i],
                        textStartPos + fm.getHeight()/3, 
                        i * fm.getHeight() + fm.getMaxAscent());
                    
     
                    // compute the max line length
                    int len = fm.stringWidth(text[i]);
                    if (len > maxLineLength) {
                        maxLineLength = len;
                    }
                }
            }
        }
    }

    public void setText(String[] text) {
        this.text = text;
        repaint();
    }

    public void setText(String text) {
        setText(new String[] {text});
    }

    /**
     * Appends s to the text array.  
     * 
     * @param s
     */
    public void append(String s) {
        if (s==null || s.length()==0) {
            return;
        }

        int startPos = 0, delimPos = 0;
        
        while( (delimPos = s.indexOf(lineSep, startPos)) != -1) {
            appendAsLine(s.substring(startPos, delimPos));
            startPos = delimPos + lineSep.length();
        }
        
        if (startPos < s.length()) {
            appendAsLine(s.substring(startPos));
        }
        
        repaint();
    }
    
    
    /**
     * Appends s to the text array. If the array gets too long, more than
     * number of lines visible, the old elements are discarded. 
     * 
     * @param s
     */
    private void appendAsLine(String s) {
        if (numberOfLinesDisplayed == -1) {
            numberOfLinesDisplayed = getHeight() / fm.getHeight();
        }

        if (text.length < numberOfLinesDisplayed) {
            // extend array
            String[] t = new String[text.length + 1];
            System.arraycopy(text, 0, t, 0, text.length);
            text = t;
            text[text.length-1] = s;
        }
        else {
            for(int i=0; i<numberOfLinesDisplayed-1; i++) {
                text[i] = text[i+1];
            }
            text[numberOfLinesDisplayed-1] = s;
        }
    }
    
    
    public void scrollLeft() {
        if (textStartPos != 0) {
            textStartPos += getWidth()/2;
            repaint();
        }
    }
    
    public void scrollRight() {
        // note: textStartPos is always 0 or negative
        if (getWidth() - textStartPos < maxLineLength) {
            textStartPos -= getWidth()/2;
            repaint();
        }
    }
    
    public int getTextStartPos() {
        return textStartPos;
    }
    
    
    
    public void setBackColor(Color c) {
        back = c;
        repaint();
    }

    // We need this methods in order to use this component in BorderLayout
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public Dimension getMinimumSize() {
        FontMetrics fm = getFontMetrics(Screen.getDefaultFont());
        return new Dimension(
                /* doesn't matter yet */ 0,
                getMinimumHeight(fm));
    }

    private int getMinimumHeight(FontMetrics fm) {
        return fm.getHeight() * text.length;
    }


    int numberOfLinesDisplayed = -1;
    FontMetrics fm = null;
    int maxLineLength = 0;
    int textStartPos = 0;  // is zero or negative!
    String[] text;
    Color back = Color.white;
    Color front = Color.black;
}
