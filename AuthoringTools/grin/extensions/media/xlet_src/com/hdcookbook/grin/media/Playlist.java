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
 * This is an exension feature for a Playlist.  Note that this class
 * depends on GEM APIs to start and stop video.  For GrinView use,
 * there's an SE version of this class, with the same classname.  It's
 * important that the public methods of this class be kept in sync with
 * the SE version.
 * <p>
 * This feature is designed to be used in conjunction with java_command
 * commands.  There are no built-in commands to start and stop video,
 * but you can make a java_command that calls the relevant public
 * methods.  GrinBunny demonstrates this technique - see
 * F:BackgroundVideo.
 * <p>
 * Generally speaking, you call methods on a playlist, and the right
 * thing happens.  So, if you <code>start()</code> a playlist, then
 * it will find the global JMF player, stop any currently playing
 * video, start the new video, and register a listener for notification
 * when that video actually starts.  Callbacks when the state of playing
 * changes are done via GRIN commands sent to the show.
 * <p>
 * In order to use a playlist, you must initialize and destroy the
 * PlayerWrangler singleton.  
 *
 * @see PlayerWrangler
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

import org.bluray.net.BDLocator;

import java.awt.Graphics2D;
import java.io.IOException;


public class Playlist extends Feature implements Node {

    private BDLocator locator;
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
    private int currentVideoSegment;

    public Playlist(Show show) {
        super(show);
    }

    protected void setLocator(String locator) {
        PlayerWrangler wrangler = PlayerWrangler.getInstance();
        this.locator = wrangler.createLocator(locator);
    }

    protected String getLocator() {
        return locator.toString();
    }

    /**
     * Start video playback.  The playlist finds the main player, stops
     * anything else that's playing, and starts video playback.  When the
     * video actually starts, the onMediaStart commands are triggered.
     * Starting any other playlist will grab playback away from this
     * playlist, and cease the triggering of any commands from this
     * playlist.
     * <p>
     * This may be called on a deactivated player.  A deactivated
     * player won't react to media events (by posting the onXXX commands).
     **/
    public void start() {
        PlayerWrangler wrangler = PlayerWrangler.getInstance();
        wrangler.start(this, locator);
    }

    //
    // Called from PlayerWrangler, with a lock held.  We mustn't do
    // anything here that requires a non-local lock.
    //
    void notifyMediaStart() {
        synchronized(this) {
            if (!activated) {
                return;
            }
        }
        show.runCommands(onMediaStart);
    }

    /**
     * Stop video playback.  If this playlist is currently playing, this
     * halts playback, and does not replace the currently playing video with
     * anything else.  The onMediaEnd commands are not triggered when this
     * method is called, and will not be triggered under any circumstances
     * after stop() has been called, unless playback is first re-started.
     * <p>
     * This may be called on a deactivated player.  A deactivated
     * player won't react to media events (by posting the onXXX commands).
     **/
    public void stop() {
        PlayerWrangler wrangler = PlayerWrangler.getInstance();
        wrangler.stop(this);
    }

    //
    // Called from PlayerWrangler, with a lock held.  We mustn't do
    // anything here that requires a non-local lock.
    //
    void notifyMediaEnd() {
        synchronized(this) {
            if (!activated) {
                return;
            }
        }
        show.runCommands(onMediaEnd);
    }

    /**
     * Reset the locator.  Re-setting the locator does not change any
     * running playback, but does change the video that will be shown
     * when video is next started.
     **/
    public void resetLocator(String locator) {
        setLocator(locator);
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
        synchronized(this) {
            activated = mode;
        }
        if (mode) {
            show.runCommands(onActivate);
            if (autoStart) {
                start();
            }
            currentVideoSegment = 0;
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
        //
        // Each frame, we poll the media time, and figure out which 
        // video segment we're in.  We solve for currentVideoSegment s
        // such that:
        //
        //    markTimes[s] <= time < markTimes[s+1]
        //
        // We take advantage of the fact that markTimes is guaranteed to
        // be sorted, to start with Integer.MIN_VALUE, and to end with
        // Integer.MAX_VALUE.  initialize() even has an assert to make sure
        // this is true.
        //
        // This provides the same functionality as playlist marks, but using
        // polling for each animation frame instead.  Using this mechanism,
        // we're guaranteed to see marks even if trick play is happening.

        if (markTimes != null) {
            int time = PlayerWrangler.getInstance().getMediaTimeMS();
            if (time < markTimes[currentVideoSegment]) {
                if (time == Integer.MIN_VALUE) {        // Unlikely!
                    time++;
                }
                currentVideoSegment = findSegment(time);
                show.runCommands(onEntryCommands[currentVideoSegment]);
            } else if (time < markTimes[currentVideoSegment + 1]) {
                // Do nothing -- we're still in the same segment
            } else if (time < markTimes[currentVideoSegment + 2]) {
                // We moved to the next segment chronologically
                currentVideoSegment++;
                show.runCommands(onEntryCommands[currentVideoSegment]);
            } else {
                if (time == Integer.MAX_VALUE) {        // Unlikely!
                    time--;
                }
                currentVideoSegment = findSegment(time);
                show.runCommands(onEntryCommands[currentVideoSegment]);
            }
        }
    }

    //
    // Find the video segment a given time is in, using a binary search.
    // This is called from nextFrame().  Solves for segment s such that:
    //
    //    markTimes[s] <= time < markTimes[s+1]
    //
    private int findSegment(int time) {
        if (Debug.ASSERT) {
            if (time == Integer.MIN_VALUE || time == Integer.MAX_VALUE) {
                Debug.assertFail();
            }
        }
        int min = 0;                            // minimum value of s
        int max = markTimes.length - 2;         // maximum value of s
        while (max > min) {
            int mid = (min + max + 1) / 2;      // That +1 is important!
            if (time < markTimes[mid]) {
                max = mid-1;
            } else {    // markTimes[mid] <= time
                min = mid;
            }
        }
        return min;
    }

    public void readInstanceData(GrinDataInputStream in, int length)
                throws IOException
    {
        in.readSuperClassData(this);

        setLocator(in.readString());
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
