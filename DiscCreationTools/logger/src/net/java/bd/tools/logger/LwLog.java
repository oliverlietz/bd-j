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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Vector;

public class LwLog extends Component {

    private static final long serialVersionUID = 3314375418166592663L;
    private Color back = Color.white;
    private Color front = Color.black;
    private Color selected = Color.gray;
    private Vector data = null;  // vector of byte[]
    private int firstDisplayedItem = 0;
    private int numberOfLinesDisplayed = 0;
    private int horizontalScrollingStep = Screen.getVisibleWidth() / 2;
    private int xPos = 0;
    private int maxRecordLength = 0; // on the rendered area
    private int position;
    private static final int littleGap = 6;
    private static final int scrollPaneWidth = 20;
    private boolean followEnd = true;

    /**
     * @param data Vector of Strings 
     * @param numberOfLinesDisplayed
     * @param position
     */
    public LwLog(Vector data, int numberOfLinesDisplayed, int position) {
        this.data = data;
        if (data == null) {
            throw new RuntimeException("Can't instantiate LwLog when data is null");
        }

        this.numberOfLinesDisplayed = numberOfLinesDisplayed;
        if (numberOfLinesDisplayed <= 0) {
            throw new RuntimeException(
                    "Can't instantiate LwLog when number of lines displayed is <= 0");
        }

        this.position = position;
    }

    public void setBackgroundColor(Color b) {
        this.back = b;
    }

    public void setForegroundColor(Color f) {
        this.front = f;
    }

    public void setSelectedColor(Color s) {
        this.selected = s;
    }

    public void initPosition() {
        switch (position) {
            case BaseLogDialog.BOTTOM_POSITION:
                firstDisplayedItem = data.size() - numberOfLinesDisplayed + 1;
                if (firstDisplayedItem < 0) {
                    firstDisplayedItem = 0;
                }
                break;
            case BaseLogDialog.TOP_POSITION:
                firstDisplayedItem = 0;
                break;
            case BaseLogDialog.KEEP_POSITION:
                // don't change
                break;
        }

        repaint();
    }

    public synchronized void paint(Graphics g) {
        if (followEnd) {
            firstDisplayedItem = data.size() - numberOfLinesDisplayed;
            if (firstDisplayedItem < 0) {
                firstDisplayedItem = 0;
            }
        }
        maxRecordLength = 0;
        FontMetrics fm = g.getFontMetrics(Screen.getDefaultFont());

        // background
        g.setColor(back);
        g.fillRect(0, 0, getWidth(), getHeight());

        // item list
        for (int i = 0; (i < numberOfLinesDisplayed) && (i + firstDisplayedItem < data.size()); i++) {
            g.setColor(front);

            String s = new String((byte[]) data.elementAt(i + firstDisplayedItem));
            g.drawString(s,
                    xPos + fm.getHeight() / 3,
                    i * fm.getHeight() + fm.getMaxAscent());

            int len = fm.stringWidth(s);
            if (len > maxRecordLength) {
                maxRecordLength = len;
            }
        }

        // draw the scroll stripe
        if (data.size() > numberOfLinesDisplayed) {
            g.setColor(front);
            g.fillRect(getWidth() - littleGap - scrollPaneWidth, 0, littleGap, getHeight());

            g.setColor(back);
            g.fillRect(getWidth() - scrollPaneWidth, 0, scrollPaneWidth, getHeight());

            int markTop = getHeight() * firstDisplayedItem / data.size();
            int markHeight = getHeight() * numberOfLinesDisplayed / data.size();
            if (markHeight > getHeight()) {
                markHeight = getHeight();
            }

            g.setColor(selected);
            g.fillRect(getWidth() - scrollPaneWidth, markTop, scrollPaneWidth, markHeight);
        }
    }

    /**
     * Move the screen one line up.
     *
     */
    public synchronized void moveUp() {
        if (firstDisplayedItem > 0) {
            followEnd = false;
            firstDisplayedItem--;
            repaint();
        }
    }

    /**
     * Move the screen one line down
     *
     */
    public synchronized void moveDown() {
        if (firstDisplayedItem < data.size() - numberOfLinesDisplayed) {
            firstDisplayedItem++;
            followEnd = firstDisplayedItem 
                            >= data.size() - numberOfLinesDisplayed;
            repaint();
        }
    }

    /**
     * Move the selection pointer one page up
     *
     */
    public synchronized void movePageUp() {
        if (firstDisplayedItem > 0) {
            followEnd = false;
            firstDisplayedItem -= numberOfLinesDisplayed;
            if (firstDisplayedItem < 0) {
                firstDisplayedItem = 0;
            }
            repaint();
        }
    }

    /**
     * Move the selection pointer one page down
     *
     */
    public synchronized void movePageDown() {
        if (firstDisplayedItem < data.size() - numberOfLinesDisplayed) {
            firstDisplayedItem = Math.min(
                    data.size() - numberOfLinesDisplayed,
                    firstDisplayedItem + numberOfLinesDisplayed);
            followEnd = firstDisplayedItem 
                            >= data.size() - numberOfLinesDisplayed;

            repaint();
        }
    }

    /**
     * Move the screen half screen left 
     */
    public synchronized void moveLeft() {
        if (xPos < 0) {
            xPos += horizontalScrollingStep;
            repaint();
        }
    }

    /**
     * Move the screen half screen right
     */
    public synchronized void moveRight() {
        if (maxRecordLength + xPos >
                Screen.getVisibleWidth() - littleGap - scrollPaneWidth) {
            xPos -= horizontalScrollingStep;
            repaint();
        }
    }
}
