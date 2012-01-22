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

import javax.media.ClockStartedError;
import javax.media.Control;
import javax.media.Player;
import javax.media.Time;
import javax.media.Manager;
import javax.media.ControllerListener;
import javax.media.ControllerEvent;
import javax.media.RestartingEvent;
import javax.media.StartEvent;
import javax.media.StopEvent;
import javax.media.protocol.DataSource;
import javax.tv.media.AWTVideoSizeControl;
import javax.tv.media.AWTVideoSize;
import javax.tv.locator.InvalidLocatorException;

import org.davic.media.MediaLocator;
import org.davic.media.MediaTimePositionControl;
import org.havi.ui.HSound;

import org.bluray.net.BDLocator;
import org.bluray.media.InvalidPlayListException;
import org.bluray.media.PlayListChangeControl;
import org.bluray.media.PlaybackControl;
import org.bluray.media.PlaybackListener;
import org.bluray.media.PlaybackMarkEvent;
import org.bluray.media.PlaybackPlayItemEvent;
import org.bluray.media.PrimaryAudioControl;
import org.bluray.media.PrimaryGainControl;
import org.bluray.media.StreamNotAvailableException;
import org.bluray.media.SubtitlingControl;




/** 
 * This is a singleton class that's used to control A/V playback on the
 * primary JMF player.
 **/

public class PlayerWrangler implements PlaybackListener, ControllerListener

{
    private static PlayerWrangler theWrangler = new PlayerWrangler();

    private AnimationEngine engine;
    private Playlist currentPlaylist;
    private Player thePlayer;
    private PlayListChangeControl playlistControl;
    private PlaybackControl playbackControl;
    private AWTVideoSizeControl sizeControl;
    private BDLocator locatorRequest = null;
        // If locatorRequest is set, that means that we want the playlist
        // to stop whatever it's doing, and play that locator.
    private boolean newPlayer = false;
    private boolean playerRunning = false;
    private boolean playPending = false;

    private PlayerWrangler() {
    }

    public static PlayerWrangler getInstance() {
        return theWrangler;
    }

    /**
     * Initialize the playback engine.  This must be called after the xlet
     * starts, and before playback of video clips is attempted.
     **/
    public void initialize(AnimationEngine engine) {
        this.engine = engine;
    }

    /**
     * Destroy the playback engine.  This must be called on xlet termination.
     * Once destroyed, any attempt at media control fill fail, because
     * PlayerWrangler.getInstance() will return null.
     **/
    public void destroy() {
        if (Debug.ASSERT && engine == null) {
            Debug.assertFail();
        }
        PlaybackControl c = null;
        Player p = null;
        synchronized(this) {
            theWrangler = null;         // Now, getInstance gives null
            c = playbackControl;
            p = thePlayer;
        }
        if (c != null) {
            c.removePlaybackControlListener(this);
        }
        if (p != null) {
            p.removeControllerListener(this);
            p.stop();          // MHP 11.7.1.2
        }
    }

    /**
     * Create a BD locator. 
     **/
    public BDLocator createLocator(String str) {
        try {
            return new BDLocator(str);
        } catch (Exception ex) {
            if (Debug.LEVEL > 0) {
                Debug.printStackTrace(ex);
            }
            if (Debug.ASSERT) {
                Debug.assertFail();
            }
            return null;
        }
    }

    /**
     * Create a MediaLocator, given a BD locator string
     **/
    public MediaLocator createMediaLocator(String loc) {
        return new MediaLocator(createLocator(loc));
    }


    //
    // Start playback of a playlist
    //
    void start(Playlist playlist, BDLocator locator) {
        if (Debug.ASSERT && engine == null) {
            Debug.assertFail();
        }
        Player stopPlayer = null;
        BDLocator startLocator = null;
        Player startPlayer = null;
        synchronized(this) {
            playPending = true;
            currentPlaylist = playlist;
            if (thePlayer != null) {
                if (playerRunning) {
                    stopPlayer = thePlayer;
                    locatorRequest = locator;
                        // when we get the StopEvent, we'll position to
                        // this locator
                } else {
                    locatorRequest = null;      
                        // It should already be null, but it's just possible
                        // there's a race condition that might leave a previous
                        // request outstanding, so we cancel it.
                    startLocator = locator;
                }
            } else {
                try {
                    MediaLocator ml = new MediaLocator(locator);
                    thePlayer = Manager.createPlayer(ml);
                    newPlayer = true;
                    if (Debug.LEVEL > 1) {
                        Debug.println("Created player on " + locator);
                    }
                } catch (Exception ignored) {
                    if (Debug.LEVEL > 0) {
                        Debug.printStackTrace(ignored);
                    }
                    if (Debug.ASSERT) {
                        Debug.assertFail();
                    }
                }
                thePlayer.addControllerListener(this);
                Control[] controls = thePlayer.getControls();
                for (int i = 0; i < controls.length; i++) {
                    Control c = controls[i];
                    if (c instanceof PlayListChangeControl) {
                        playlistControl = (PlayListChangeControl) c;
                    } else if (c instanceof PlaybackControl) {
                        playbackControl = (PlaybackControl) c;
                    } else if (c instanceof AWTVideoSizeControl) {
                        sizeControl = (AWTVideoSizeControl) c;
                    }
                }
                if (Debug.ASSERT &&
                    (playbackControl == null || playlistControl == null
                     || sizeControl == null))
                {
                    Debug.assertFail();
                }
                playbackControl.addPlaybackControlListener(this);
                startPlayer = thePlayer;
            }
        }
        //
        // Out of general paranoia, we put any non-necessary interactions
        // with JMF outside of the synchronized part of the code.
        //
        if (stopPlayer != null) {
            stopPlayer.stop();
        }
        if (startLocator != null) {
            startExisting(locator);
        }
        if (startPlayer != null) {
            startPlayer.start();
        }
    }

    //
    // Start playback on the existing player, which must be in the
    // stopped state.
    //
    private void startExisting(BDLocator locator) {
        if (Debug.ASSERT && (playerRunning || locatorRequest != null)) {
            Debug.assertFail();
        }
        try {
            playlistControl.selectPlayList(locator);
            if (Debug.LEVEL > 1) {
                Debug.println("Selected locator for player with " + locator);
            }
            thePlayer.start();
        } catch (ClockStartedError ex) {
            // We are only called if playerStarted is false, so this must
            // represent a race condition, where start() was just called
            // a moment ago, perhaps for a different playlist.  In this
            // case, we're about to get a StartEvent, so we set locatorRequest
            // to what we want, and let the callback figure it out.
            locatorRequest = locator;
        } catch (Exception ignored) {
            if (Debug.LEVEL > 0) {
                Debug.printStackTrace(ignored);
            }
            if (Debug.ASSERT) {
                Debug.assertFail();
            }
        }
    }

    /**
     * Returns the current media time in ns, or -1 if no playlist has
     * started.
     **/
    public long getMediaTime() {
        Player p = thePlayer;
        if (p == null) {
            return -1;
        } else {
            return p.getMediaTime().getNanoseconds();
        }
    }

    /**
     * Returns the current media time in ms, or -1 if no playlist has
     * started.
     **/
    public int getMediaTimeMS() {
        Player p = thePlayer;
        if (p == null) {
            return -1;
        } else {
            return (int) (p.getMediaTime().getNanoseconds() / 1000000);
        }
    }

    /**
     * Sets the current media time in ns.
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public void setMediaTime(long time) {
        thePlayer.setMediaTime(new Time(time));
    }

    /**
     * Sets the current media time in ns.
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public void setMediaTimeMS(int time) {
        thePlayer.setMediaTime(new Time(((long) time) * 1000000));
    }

    /**
     * Sets the rate of playback, subject to the restrictions of the
     * BD spec.  This is just a pass-through to the JMF Player.setRate()
     * method.  
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public void setRate(float rate) {
        thePlayer.setRate(rate);
    }

    /**
     * Gets the rate of playback.  This is just a pass-through to the JMF
     * Player.getRate method.  
     * This method can only be called after the first playlist is
     * started, because that's the first time a JMF player is acquired.
     **/
    public float getRate() {
        return thePlayer.getRate();
    }


    /**
     * Returns an AWTVideoSizeControl that can be used to scale the video.
     * This returns null if a playlist hasn't been started yet.  That's
     * inconvenient, but it's a function of how the underlying API is
     * structured, where you can't get a JMF player until you start it
     * on something.
     * <p>
     * This method is only available on the Xlet version of this class,
     * because AWTVideoSizeControl is a JavaTV API.
     * <p>
     * In the future, it might be intersting to integrate InterpolatedModel
     * or some other way of declaratively controlling the display size
     * with the Playlist API.
     * Doing that is non-trivial, though, partly because of 
     * AWTVideoSizeControl.checkSize().
     * As of this writing, that has not been done, so scripting from Java is
     * needed to adjust the video size.
     **/
    public AWTVideoSizeControl getSizeControl() {
        return sizeControl;
    }


    /**
     * Stop playing a playlist.  
     **/
    void stop(Playlist playlist) {
        if (Debug.ASSERT && engine == null) {
            Debug.assertFail();
        }
        if (Debug.LEVEL > 1) {
            Debug.println("Stopping playlist " + playlist);
        }
        Player stopPlayer = null;
        synchronized(this) {
            if (currentPlaylist != playlist) {
                return;
            }
            playPending = false;
            currentPlaylist = null;
            if (thePlayer != null) {
                stopPlayer = thePlayer;   // NOP if it's already stopped
            }
            locatorRequest = null;
        }
        if (stopPlayer != null) {
            stopPlayer.stop();  // NOP if it's already stopped
        }
    }

    /**
     * Callback from PlaybackListener
     **/
    public void markReached(PlaybackMarkEvent event) {
        // We're not currently doing anything with these events
    }

    /**
     * Callback from PlaybackListener
     **/
    public void playItemReached(PlaybackPlayItemEvent event) {
        // We're not currently doing anything with these events
    }

    /**
     * Callback from ControllerListener
     **/
    public void controllerUpdate(ControllerEvent event) {
        if (Debug.LEVEL > 1) {
            Debug.println("Player gets controllerUpdate " + event);
        }
        if (event instanceof RestartingEvent) {
            // The restarting event is a subtype of StopEvent, and can
            // be generated for things like a rate change.  It's not
            // a StopEvent that we care about, because the player
            // is just going to Start again automatically, so we ignore
            // it.
            return;
        } else if (event instanceof StartEvent) {
            Player stopPlayer = null;
            synchronized(this)  {
                playerRunning = true;
                if (locatorRequest != null) {
                    // If there's a pending request to move to another locator,
                    // stop the player, so we can select it.
                    stopPlayer = thePlayer;
                } else if (playPending) {
                    playPending = false;
                    if (currentPlaylist != null) {
                        currentPlaylist.notifyMediaStart();
                            // This just enqueues commands, so it's safe here.
                    }
                }
                if (newPlayer) {
                    newPlayer = false;
                    engine.paintNextFrameFully();
                        // Some players, including at least one PC player, mess
                        // up the FrameBuffer display when the main player first
                        // starts showing video.
                        //
                        // This method just sets a flag, so it's safe here.
                }
            }
            if (stopPlayer != null) {
                stopPlayer.stop();
            }
        } else if (event instanceof StopEvent) {
                //
                // When end of media is reached, at least some players
                // give both a StopEvent, and an EndOfMediaEvent.
                // EndOfMediaEvent is a subtype of StopEvent.  Due
                // to the boolean playerRunning, only the first one
                // will have any effect.
                //
            BDLocator startLocator;
            boolean playerWasRunning;
            Playlist p;
            synchronized(this) {
                startLocator = locatorRequest;
                locatorRequest = null;
                playerWasRunning = playerRunning;
                playerRunning = false;
                p = currentPlaylist;
            }
            if (startLocator != null) {
                startExisting(startLocator);
            } else if (playerWasRunning && p != null) {
                if (Debug.LEVEL > 1) {
                    Debug.println("Notifying media end to playlist " + p);
                }
                p.notifyMediaEnd();
                    // In the case where a new playlist has taken over
                    // the player, startLocator will be non-null, so
                    // we correctly won't send the media end commands.
            }
        }
    }
}
