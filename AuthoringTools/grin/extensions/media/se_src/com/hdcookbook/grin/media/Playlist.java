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


/** 
 * This is a little tricky.  This is an SE version of the base class for
 * SEPlaylist.  It's also a "stubbed out" playlist.  The real playlist
 * class is in the xlet_src directory, but it can't be used with grinview,
 * because it depends on GEM APIs to do the media playback.
 * This SE version doesn't play any media, but it does trigger the
 * right commands, and it sends some debug messages out to say what it's
 * doing.
 * <p>
 * It is important to keep the public methods of this class in sync with
 * the xlet version of Playlist.
 * <p>
 * Please see the xlet version of this class for more complete documentation
 * of its methods.
 */

package com.hdcookbook.grin.media;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.Node;
import com.hdcookbook.grin.animator.DrawRecord;
import com.hdcookbook.grin.animator.RenderContext;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.io.binary.GrinDataInputStream;
import com.hdcookbook.grin.util.Debug;

import java.awt.Graphics2D;
import java.io.IOException;


public class Playlist extends Feature implements Node {

    private String locator;
    protected Command[] onActivate;
    protected Command[] onMediaStart;
    protected Command[] onMediaEnd;
    protected Command[] onDeactivate;
    protected boolean autoStart;
    protected boolean autoStop;

        // If the show uses mark times to send commands when we enter
        // into a segment of video, markTimes and onEntryCommands will
        // both be non-null.  Otherwise, they will both be null.
        // See also the assertions about these data members in initialize().
    protected int[] markTimes;
    protected Command[][] onEntryCommands = null;

    private boolean activated;
    private int startCountdown = 0;
    private int stopCountdown = 0;
    
    public Playlist(Show show) {
        super(show);
    }

    protected void setLocator(String locator) {
        this.locator = locator;
    }

    protected String getLocator() {
        return locator;
    }

    /**
     * Start video playback.  Please see the xlet version of this class
     * for more complete documentation.
     **/
    public void start() {
        Debug.println("Pretending to start video:  " + locator);
        startCountdown = 10;
        stopCountdown = 0;
    }

    /**
     * Stop video playback.  Please see the xlet version of this class
     * for more complete documentation.
     **/
    public void stop() {
        Debug.println("Pretending to stop video:  " + locator);
        startCountdown = 0;
        stopCountdown = 0;
    }

    /**
     * Reset the locator.  Please see the xlet version of this class
     * for more complete documentation.
     **/
    public void resetLocator(String locator) {
        this.locator = locator;
    }

    /** 
     * {@inheritDoc}
     **/
    public int getX() {
        return 0;
    }

    /** 
     * {@inheritDoc}
     **/
    public int getY() {
        return 0;
    }

    /** 
     * {@inheritDoc}
     **/
    public void initialize() {
            // Assert the invariant for our mark times:  The list must
            // be sorted, it must start with Integer.MIN_VALUE, and it
            // must end with Integer.MAX_VALUE.  Ensuring this invariant
            // makes writing the binary search through mark times easier.
        if (Debug.ASSERT) {
            if (markTimes == null) {
                if (onEntryCommands != null) {
                    Debug.assertFail();
                }
            } else {
                if (onEntryCommands == null) {
                    Debug.assertFail();
                }
                if (markTimes[0] != Integer.MIN_VALUE) {
                    Debug.assertFail();
                } else if (markTimes[markTimes.length-1] != Integer.MAX_VALUE) {
                    Debug.assertFail();
                } 
                int last = markTimes[0];
                for (int i = 1; i < markTimes.length; i++) {
                    if (last >= markTimes[i]) {
                        Debug.assertFail();
                    }
                    last = markTimes[i];
                }
            }
        }
    }

    /** 
     * {@inheritDoc}
     **/
    public void destroy() {
    }

    /** 
     * {@inheritDoc}
     **/
    protected int setSetupMode(boolean mode) {
        return 0;
    }

    /** 
     * {@inheritDoc}
     **/
    protected void setActivateMode(boolean mode) {
        activated = mode;
        if (mode) {
            show.runCommands(onActivate);
            if (autoStart) {
                start();
            }
        } else {
            show.runCommands(onDeactivate);
            if (autoStop) {
                stop();
            }
        }
    }

    /** 
     * {@inheritDoc}
     **/
    public boolean needsMoreSetup() {
        return false;
    }

    /** 
     * {@inheritDoc}
     **/
    public void addDisplayAreas(RenderContext context) {
    }

    /** 
     * {@inheritDoc}
     **/
    public void markDisplayAreasChanged() {
    }

    /** 
     * {@inheritDoc}
     **/
    public void paintFrame(Graphics2D gr) {
    }

    /** 
     * {@inheritDoc}
     **/
    public void nextFrame() {
        if (startCountdown > 0) {
            startCountdown--;
            if (startCountdown == 0) {
                Debug.println("Pretending video started:  " + locator);
                show.runCommands(onMediaStart);
                stopCountdown = 10 * 24;
            }
        } else if (stopCountdown > 0) {
            stopCountdown--;
            if (stopCountdown == 0) {
                Debug.println("Pretending end of media reached:  " + locator);
                show.runCommands(onMediaEnd);
            }
        }
    }

    public void readInstanceData(GrinDataInputStream in, int length)
                throws IOException
    {
        in.readSuperClassData(this);

        locator = in.readString();
        onActivate = in.readCommands();
        onMediaStart = in.readCommands();
        onMediaEnd = in.readCommands();
        onDeactivate = in.readCommands();
        autoStart = in.readBoolean();
        autoStop = in.readBoolean();
        markTimes = in.readSharedIntArray();
        if (in.readByte() != 0)  {
            int len = in.readInt();
            onEntryCommands = new Command[len][];
            for (int i = 0; i < len; i++) {
                onEntryCommands[i] = in.readCommands();
            }
        }
    }
}
