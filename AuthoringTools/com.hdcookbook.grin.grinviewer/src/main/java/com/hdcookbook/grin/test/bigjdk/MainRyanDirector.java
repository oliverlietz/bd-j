
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

package com.hdcookbook.grin.test.bigjdk;

import com.hdcookbook.grin.test.RyanDirector;
import com.hdcookbook.grin.Segment;

/**
 * This is part of the "Ryan's life" test show.  It's mostly of
 * historical interest; it still works, but some of the ways of
 * structuring and using a show are passe.
 *
 * @author Bill Foote (http://jovial.com)
 */
public class MainRyanDirector extends RyanDirector {

    private boolean videoCanBeStarted = false;
    private int frame;

    // We fake time-triggered events by just recording a frame # when
    // we want it to fire.  This simulates what you would do with a
    // real trigger on a disc.
    private Runnable nextTrigger;
    private int nextTriggerFrame;
    
    Segment sCommentaryMenuActivation;
    Segment sCommentaryMenuCountDown;

    public MainRyanDirector() {
    }

    public void init() {
        super.init();
        sCommentaryMenuActivation 
            = getShow().getSegment("commentary_menu_activation");
        sCommentaryMenuCountDown
            = getShow().getSegment("commentary_menu_count_down");
    }

    public synchronized void startVideo() {
        videoCanBeStarted = true;
        notifyAll();
    }

    public void setInteractiveMode(boolean on) {
        System.out.println("Interactive mode set to " + on);
        if (on) {
            synchronized(this) {
                nextTriggerFrame = frame + 24*5;        // 5s
                nextTrigger = new Runnable() {
                    public void run() {
                        getShow().activateSegment(sCommentaryMenuActivation);
                    }
                };
            }
        }
    }

    protected void startCommentary() {
        setDirectorNumber(1);
        setupNextDirector(2);
    }

    private void setupNextDirector(final int num) {
        synchronized(this) {
            nextTriggerFrame = frame + 24*3;
            if (num == 1) {
                nextTriggerFrame += 24*6;
            }
            nextTrigger = new Runnable() {
                public void run() {
                    if (num == 5) {
                        getShow().activateSegment(sCommentaryMenuCountDown);
                    }
                    if (num < 6) {
                        setupNextDirector(num + 1);
                    }
                    setDirectorNumber(num);
                }
            };
        }
    }

    public synchronized void waitForVideoStartOK() throws InterruptedException {
        while (!videoCanBeStarted) {
            wait();
        }
    }

    public void setFrame(int f) {
        Runnable trigger = null;
        synchronized (this)  {
            if (nextTrigger != null && f >= nextTriggerFrame) {
                trigger = nextTrigger;
                nextTrigger = null;
            }
            frame = f;
        }
        if (trigger != null) {
            trigger.run();
        }
    }
    
}
