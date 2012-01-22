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

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.EmptyStackException;
import java.util.Stack;


/**
 * Contains static methods to provide various settings about the
 * display features
 *
 * @author Vasily Kopyl
 *
 */
public class Screen {
    private static Container rootContainer = null;  // given by Xlet
    
    private static Stack stack = new Stack();
    private static Component logComponent;
    
    private static Font defaultFont = getFont(Screen.getDefaultFontSize());
    private static boolean showLogMode = false;
    
    /**
     * Sets the root container given by Xlet.
     * @param c
     */
    public static void setRootContainer(Container c) {
        rootContainer = c;
        rootContainer.setSize(
                Screen.getScreenWidth(), Screen.getScreenHeight());
        rootContainer.setLocation(0, 0);
    }

    /**
     * Sets the component that will be displayed when [pause] is pressed
     * @param c
     */
    public static void setLogComponent(Component c) {
        logComponent = c;
        if (showLogMode) {
            display(logComponent);
        }
    }

    /**
     * Make the root container visible or hidden
     * @param visible
     */
    public static void setVisible(boolean visible) {
        if (visible) {
            rootContainer.setSize(
                    Screen.getScreenWidth(), Screen.getScreenHeight());
        } else {
            // we don't hide the root component completely here,
            // in order to be able to receive keyboard events
            rootContainer.setSize(1, 1);
        }

        rootContainer.validate();
        rootContainer.repaint();
    }

    /**
     * Returns the current component (not the log one)
     * @return
     */
    public static Component getCurrentComponent() {
        try {
            return (Component)stack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    /**
     * Sets the current component
     * if null - set the previous component 
     * @param c
     */
    public static void setCurrentComponent(Component c) {
        try {
            if (c == null) {
                stack.pop();
            } else {
                stack.push(c);
            }

            if (!showLogMode) {
                display((Component)stack.peek());
            }
        } catch (EmptyStackException e) {
            // nothing to display
        }
    }


    /**
     * Display the specified component in the root container
     * @param c
     */
    private static void display(Component c) {
        if (c == null) {
            return;
        }

        rootContainer.setVisible(false);
        rootContainer.removeAll();

        // put the visible area to the center of the root container
        rootContainer.add(c);
        c.setSize(Screen.getVisibleWidth(), Screen.getVisibleHeight());
        c.setLocation((getScreenWidth() - getVisibleWidth())/2,
                (getScreenHeight() - getVisibleHeight())/2);

        rootContainer.validate();
        rootContainer.setVisible(true);

        c.requestFocus();
    }

    public static synchronized void toggleShowLogMode() {
        setShowLogMode(!showLogMode);
    }

    /**
     * Sets the showLogMode to the specified value and displays
     * the appropriate component.
     * @param b
     */
    public static synchronized void setShowLogMode(boolean b) {
        showLogMode = b;
        
        if (showLogMode) {
            display(logComponent);
        }
        else {
            try {
                display((Component)stack.peek());
            }
            catch (EmptyStackException e) {
                // nothing to display
            }
        }
    }
    
     /**
     * Repaints the log component if it's visible on the screen.
     */   
    public static synchronized void repaintLogScreen() {
        if (showLogMode) {
            logComponent.repaint();
        }
    } 
    
    // ========================= Screen sizes ==========================
    public static int getScreenWidth() {
        return 1920;
    }

    public static int getScreenHeight() {
        return 1080;
    }

    /**
     * Overscan area in percents.
     * 5% - action safe area
     * 10% - title safe area
     * @return % of invisible area near the screen borders
     */
    public static int getOverscanArea() {
        return 20;
    }

    public static int getVisibleWidth() {
        return getScreenWidth() * (100 - getOverscanArea())/100;
    }

    public static int getVisibleHeight() {
        return getScreenHeight() * (100 - getOverscanArea())/100;
    }

    /**
     * The font size that will be used for data output on the screen
     * @return
     */
    public static int getDefaultFontSize() {
        return 26;
    }

    /**
     * Get default font to be used for data output on the screen.
         * @return the default font object.
     */
    public static Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * Create a new font object of the specified size.
     */
    public static Font getFont(int size) {
        return new Font("SansSerif", Font.PLAIN, size);
    }
}
