
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
package com.hdcookbook.grinxlet;

import com.hdcookbook.grin.GrinXHelper;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.test.bigjdk.GenericMain;     // that's GrinView
import com.hdcookbook.grin.animator.AnimationClient;
import com.hdcookbook.grin.animator.AnimationEngine;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.input.RCKeyEvent;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This is a facade to the main controller in GrinView.  It allows GrinView
 * emulation of some of the features of the GrinXlet class found in
 * xlets/grin_samples/GenericGame.  This GrinView facade implements a subset
 * of those methods, so any public methods added here must be present in the
 * real xlet versions of GrinXlet.
 * <p>
 * In other words, the reason why a GrinView version of this class exists
 * is so that a Director subclass can refer to it, and still work under
 * GrinView.
 * <p>
 * If your project extends GameXlet, you'll probably want an se_src version
 * of your extension for use with GrinView.  This GrinView version of your
 * extension should look like this:
 * <pre>
 *    public class MyGameXlet extends GameXlet {
 *
 *         public MyGameXlet() {
 *         }
 *
 *         ...
 *    }
 * </pre>
 **/
public class GrinXlet implements KeyListener, MouseListener, MouseMotionListener {

    private static GrinXlet theInstance = null;
    private static GenericMain grinView;
    private Show[] keyInterestOrder;
    private boolean directorWantsKeyTyped = false;

    /**
     * This constructor is used by GrinView
     **/
    public GrinXlet(GenericMain grinView) {
        this.grinView = grinView;
        theInstance = this;
    }

    /**
     * This constructor is for a subclass of GrinXlet.
     **/
    protected GrinXlet() {
    }

    /**
     * Get the instance of this singleton.
     **/
    public static GrinXlet getInstance() {
        return theInstance;
    }

    /**
     * Get the list of animation clients
     **/
    public AnimationClient[] getAnimationClients() {
        return grinView.getAnimationClients();
    }

    /**
     * Reset the list of animation clients
     **/
    public void resetAnimationClients(AnimationClient[] clients) {
        grinView.resetAnimationClients(clients);
    }

    /**
     * Get the animation engine
     **/
    public AnimationEngine getAnimationEngine() {
        return grinView.getAnimationEngine();
    }

    /**
     * Inserts a new Show at the top of the key interest stack.
     * KeyEvents are delivered to the shows
     * starting from the top of the key interest stack.
     * If the show's currently active segment do not have any rc_handler
     * that uses the key, then the the event
     * is sent to the next show on the stack.
     * MouseEvents are sent to all the shows in the key interest list.
     */
    public synchronized void pushKeyInterest(Show show) {
        if (keyInterestOrder == null) {
	    // First time called.  We intentionally emulate the GrinXlet
	    // behavior of insisting that the initial director indicate that it
	    // wants key typed events if we are to ever deliver key typed
	    // events.  See GrinXlet if you're curious why.
	    directorWantsKeyTyped = show.getDirector().wantsKeyTyped();
            keyInterestOrder = new Show[] {show} ;
            return;
        }

        Show[] newList = new Show[keyInterestOrder.length+1];
        newList[0] = show;
        for (int i = 0; i < keyInterestOrder.length; i++) {
            newList[i+1] = keyInterestOrder[i];
        }
        keyInterestOrder = newList;
    }
    /**
     * Removes the show at the top of the key interest stack.
     * KeyEvents are delivered to a show in the order
     * starting from the top of the key interest stack.
     * If the show's currently active segment do not have any rc_handler
     * that uses the key, then the the event
     * is sent to the next show on the stack.
     * MouseEvents are sent to all the shows in the key interest list.
     */
    public synchronized Show popKeyInterest() {
        if (keyInterestOrder == null || keyInterestOrder.length == 0) {
            return null;
        }

        Show show = keyInterestOrder[0];
        Show[] newList = new Show[keyInterestOrder.length-1];
        for (int i = 1; i < keyInterestOrder.length; i++) {
            newList[i-1] = keyInterestOrder[i];
        }
        keyInterestOrder = newList;
        return show;
    }

    public void keyTyped(KeyEvent event) {
	if (!directorWantsKeyTyped) {
	    return;
	}
	char key = event.getKeyChar();
	synchronized(this) {
	    for (int i = 0; i < keyInterestOrder.length; i++) {
		keyInterestOrder[i].handleKeyTypedToDirector(key);
	    }
	}
	// Core GRIN never implemented a subclass of RCKeyEvent that would let
	// us call show.handleKeyTyped().
    }

    public void keyPressed(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        // Translate F1..F4 into red/green/yellow/blue
        if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F4) {
            code = code - KeyEvent.VK_F1 + RCKeyEvent.KEY_RED.getKeyCode();
        } else if (code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9) {
            code = code - KeyEvent.VK_NUMPAD0 + RCKeyEvent.KEY_0.getKeyCode();
        } else if (code == KeyEvent.VK_F5) {
            code = RCKeyEvent.KEY_POPUP_MENU.getKeyCode();
        }

        synchronized(this) {
            for (int i = 0; i < keyInterestOrder.length; i++) {
                boolean isHandled = keyInterestOrder[i].handleKeyPressed(code);
                if (isHandled) {
                    break;
                }
            }
        }
    }

    public void keyReleased(java.awt.event.KeyEvent e) {
        int code = e.getKeyCode();
        // Translate F1..F4 into red/green/yellow/blue
        if (code >= KeyEvent.VK_F1 && code <= KeyEvent.VK_F4) {
            code = code - KeyEvent.VK_F1 + RCKeyEvent.KEY_RED.getKeyCode();
        } else if (code >= KeyEvent.VK_NUMPAD0 && code <= KeyEvent.VK_NUMPAD9) {
            code = code - KeyEvent.VK_NUMPAD0 + RCKeyEvent.KEY_0.getKeyCode();
        } else if (code == KeyEvent.VK_F5) {
            code = RCKeyEvent.KEY_POPUP_MENU.getKeyCode();
        }
        synchronized(this) {
            for (int i = 0; i < keyInterestOrder.length; i++) {
                boolean isHandled = keyInterestOrder[i].handleKeyReleased(code);
                if (isHandled) {
                    break;
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        Insets insets       = grinView.getInsets();
        int    scaleDivisor = grinView.getScaleDivisor();
        final int x = (e.getX() - insets.left) * scaleDivisor;
        final int y = (e.getY() - insets.top) * scaleDivisor;
        synchronized(this) {
	    boolean consumed = false;
            for (int i = 0; i < keyInterestOrder.length; i++) {
                Show show = keyInterestOrder[i];
		if (show.handleMousePressed(x, y, consumed)) {
		    consumed = true;
		}
            }
        }
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mouseDragged(MouseEvent arg0) {
    }

    public void mouseMoved(MouseEvent e) {
        Insets insets = grinView.getInsets();
        int    scaleDivisor = grinView.getScaleDivisor();
        int x = (e.getX() - insets.left) * scaleDivisor;
        int y = (e.getY() - insets.top) * scaleDivisor;
        synchronized(this) {
	    boolean consumed = false;
            for (int i = 0; i < keyInterestOrder.length; i++) {
                Show show = keyInterestOrder[i];
		if (show.handleMouseMoved(x, y, consumed)) {
		    consumed = true;
		}
            }
        }
    }
}
