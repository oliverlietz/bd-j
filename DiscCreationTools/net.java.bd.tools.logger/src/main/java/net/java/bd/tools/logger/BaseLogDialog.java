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
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import org.havi.ui.event.HRcEvent;

/**
 * Abstract class serves as a base class for all text style display
 * screens.
 *
 */
public abstract class BaseLogDialog extends Container
        implements KeyListener, FocusListener {

    private static final long serialVersionUID = 1935060115870298485L;
    public static final int KEEP_POSITION = 0;
    public static final int BOTTOM_POSITION = 1;
    public static final int TOP_POSITION = 2;
    protected String logTitle;
    protected String logLegend;
    protected int position;
    protected Vector data;
    protected Font font;
    protected LwText legendText;
    protected LwLog lLog;
    // whether the Log Dialog is visible or pseudo-hidden
    protected boolean visible = true;
    private final boolean remoteControlEnabled;


    /** 
     * Constructor for the screen called from a prev component
     * @param prev
     * @param lp
     */
    public BaseLogDialog(String logTitle, String logLegend, int position) {
        this(logTitle, logLegend, position, true);
    }


    /** 
     * Constructor for the screen called from a prev component
     * @param prev
     * @param lp
     */
    public BaseLogDialog(String logTitle, String logLegend, int position,
                         boolean remoteControlEnabled)
    {
        this.remoteControlEnabled = remoteControlEnabled;
        this.logTitle = logTitle;
        this.logLegend = logLegend;
        this.position = position;
        data = new Vector();
    }

    public void compose() {
        setSize(Screen.getVisibleWidth(), Screen.getVisibleHeight());

        // set the font size for the resolution
        font = Screen.getDefaultFont();
        setFont(font);
        FontMetrics fm = getFontMetrics(font);
        int stringHeight = fm.getHeight();

        LwText lTitle = new LwText(logTitle, Color.blue, Color.white);
        add(lTitle);
        lTitle.setLocation(0, 0);
        lTitle.setSize(Screen.getVisibleWidth(), stringHeight);

        lLog = new LwLog(data, (Screen.getVisibleHeight() - 2 * stringHeight) / fm.getHeight(), position);
        add(lLog);
        lLog.setLocation(0, stringHeight);
        lLog.setSize(Screen.getVisibleWidth(), Screen.getVisibleHeight() - 2 * stringHeight);

        legendText = new LwText(logLegend, Color.black, Color.white);
        add(legendText);
        legendText.setLocation(0, Screen.getVisibleHeight() - stringHeight);
        legendText.setSize(Screen.getVisibleWidth(), stringHeight);

        if (remoteControlEnabled) {
            addKeyListener(this);
        }
        addFocusListener(this);
    }

    /**
     * Loads text strings into the data Vector
     */
    protected abstract void loadData();

    /**
     * 
     * @param f
     */
    protected void loadFromFile(File f) {
        String fileName = f.getName();

        // try to read the specified files
        String s = null;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            while ((s = br.readLine()) != null) {
                addString(s);
            }
        } catch (Exception e) {
            s = "Error reading " + fileName + "\n" + e.getMessage();
            Logger.log(s, e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                Logger.log("Exception when closing BufferedReader.", e);
            }
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e) {
                Logger.log("Exception when closing FileReader.", e);
            }
        }

        addString(s);
    }

    /**
     * Adds a string argument to the data vector
     * @param s
     */
    public void addString(String s) {
        if (s != null) {
            data.add(s.getBytes());
        }
    }

    // -------- KeyListener interface methods ------------
    /**
     * Invoked when a key has been typed.
     * This event occurs when a key press is followed by a key release.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed.
     */
    public synchronized void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_0:
            case KeyEvent.VK_NUMPAD0: // show/hide xlet container toggle
                visible = !visible;
                Screen.setVisible(visible);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
                lLog.moveDown();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
                lLog.moveUp();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                lLog.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                lLog.moveRight();
                break;
            case KeyEvent.VK_PAGE_DOWN:
            case HRcEvent.VK_TRACK_NEXT:
                lLog.movePageDown();
                break;
            case KeyEvent.VK_PAGE_UP:
            case HRcEvent.VK_TRACK_PREV:
                lLog.movePageUp();
                break;
        }
        notifyAll();
    }

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e) {
    }

    // --------------------------- FocusListener interface -----------------
    public void focusLost(FocusEvent e) {
        //data.clear();
    }

    public void focusGained(FocusEvent e) {
        //loadData();
        //lLog.initPosition();
    }
}
