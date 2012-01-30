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



package com.hdcookbook.grin.media;

import com.hdcookbook.grin.animator.AnimationEngine;
import com.hdcookbook.grin.util.Debug;

/** 
 * This is a singleton class that's used to control A/V playback on the
 * primary JMF player.  In the SE version, we don't control anything,
 * so this is a stub version that provides enough for a show to link
 * against for grinview, and run properly without presenting video.
 **/

public class PlayerWrangler 

{
    private static PlayerWrangler theWrangler = new PlayerWrangler();

    private PlayerWrangler() {
    }

    public static PlayerWrangler getInstance() {
        return theWrangler;
    }

    /**
     * Initialize the playback engine.  This must be called after the xlet
     * startes, and before playback of video clips is attempted.
     **/
    public void initialize(AnimationEngine engine) {
    }

    /**
     * Returns the current media time, or -1 if no playlist has
     * started.
     **/
    public long getMediaTime() {
        return -1;
    }

    /**
     * Returns the current media time in ms, or -1 if no playlist has
     * started.
     **/
    public int getMediaTimeMS() {
        return -1;
    }

    /**
     * Sets the current media time in ns.
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public void setMediaTime(long time) {
    }

    /**
     * Sets the current media time in ns.
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public void setMediaTimeMS(int time) {
    }

    /**
     * Sets the rate of playback, subject to the restrictions of the
     * BD spec.  This is just a pass-through to the JMF Player.setRate()
     * method.  
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public void setRate(float rate) {
    }

    /**
     * Gets the rate of playback.  This is just a pass-through to the JMF
     * Player.getRate method.  
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public float getRate() {
        return -1;
    }


    /**
     * Destroy the playback engine.  This must be called on xlet termination.
     * Once destroyed, any attempt at media control fill fail, because
     * PlayerWrangler.getInstance() will return null.
     **/
    public void destroy() {
        theWrangler = null;
    }
}
