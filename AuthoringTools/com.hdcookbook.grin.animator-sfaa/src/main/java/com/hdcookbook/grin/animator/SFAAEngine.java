
/*  
 * Copyright (c) 2009, Sun Microsystems, Inc.
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

package com.hdcookbook.grin.animator;

import com.hdcookbook.grin.util.AssetFinder;
import com.hdcookbook.grin.util.Debug;
import java.awt.AlphaComposite;
import java.awt.Container;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Image;

import javax.media.Time;

import org.bluray.ui.SyncFrameAccurateAnimation;
import org.bluray.ui.AnimationParameters;

/**
 * An animation engine that draws into an instance of
 * <code>org.bluray.ui.SyncFrameAccurateAnimation</code>.
 * <p>
 * If you're thinking of using this style of animation, you might want to
 * consider using direct draw, and 
 * <code>javax.media.Player.getMediaTime()</code> instead.
 * In some of SFAA using off-the-shelf players in April 2009, the
 * execution of SFAA was found somewhat wanting, and no player seemed to do
 * better in keeping the animation registered to the video than the sort of
 * "best effort" result you'd get from direct draw.  Additionally, SFAA makes
 * optimized drawing impossible, because some implementations clear the
 * SFAA buffer before each frame.  This limits the on-screen size that can be
 * covered by an SFAA if you want to maintain 24fps.  In most cases you can probably
 * achieve better results using direect draw and 
 * <code>Player.getMediaTime</code>.
 * <p>
 * If you do use SFAA Animator, be aware that when used with a start and
 * stop media time, SFAA will stall the animation thread, which prevents
 * GRIN commands from being executed.  It also makes it impossible to
 * destroy the SFAA, so you need to be sure video is playing in the time
 * range when trying to destroy the SFAA animator instance.
 **/
public class SFAAEngine extends AnimationEngine {

    private boolean paused = false;
    private boolean skipModel;
    private int framesBehind = 0;
    private int skippedFrames = 0;
    private Container container;
    private SyncFrameAccurateAnimation sfaa = null;
    private Graphics2D currGraphics = null;
        // Set to a non-null value once per frame in runAnimationLoop,
        // and set back to null in finishedFrame.
    private long frameNumber;
    private Rectangle bounds;

    /**
     * Create a new SFAAEngine.  It needs to be initialized with
     * the various initXXX methods (including the inherited ones).
     * <p>
     * An SFAAEngine has a public method that other engine types don't:
     * getAnimationFrameTime().  This gives the media time of the frame
     * of animation currently being drawn.  With SFAA, it would make sense
     * to drive all model updates from the media time, and not by counting
     * frames (as is often done with direct draw in GRIN).
     * <p>
     * For this reason, an SFAAEngine can be in "skip model" mode.  In this
     * mode, whenver an animation frame is skipped, the model update will
     * be skipped, too.  This way, a model that us based solely off of the
     * media time won't waste computational resources calculating an update
     * for a frame that's not going to be displayed anyway.
     * <p>
     * An SFAAEngine that is not in "skip model" mode will always update the
     * model for every frame, unless animation falls behind by a substantial
     * amount.
     * <p>
     * Note that SFAAEngine does not support optimized drawing, due to
     * limitations in BD's SFAA API.  For each frame that's displayed, the
     * entire scene is drawn.  For this reason, there's no reason to use
     * more than one draw target in a show that's designed for SFAA.
     * 
     *
     * @param frameNumber       The frame number to start with.  If the
     *                          SFAA instance was just created, 0 would
     *                          be a reasonable value.
     *
     * @param skipModel         Set true if this engine should be in
     *                          "skip model" mode; see above.  A value of true
     *                          is recommended.
     * 
     * @see #getAnimationFrameTime()
     **/
    public SFAAEngine(long frameNumber, boolean skipModel) {
        this.frameNumber = frameNumber;
        this.skipModel = skipModel;
    }

    /**
     * Sets the SFAA instance to be used by this engine.  Calling this method
     * is optional.  If you call it, then the animation engine should still
     * be subsequently initialized by calling initContainer().  The SFAA
     * instance you pass in to the present method will not have any initial
     * call to start() on it, but it will have start() and stop()
     * called on it from the engine if the engine is paused and unpaused.
     * The sfaa will be destroyed when 
     * the SFAAEngine is destroyed, and it will be removed from the container
     * passed into initContainer().
     * <p>
     * This is important:  SFAAEngine will not call sfaa.start() for you.
     * If you set up the sfaa with a start and stop media time, then all
     * is well; if you don't, you should call sfaa.start() yourself the
     * first time.
     *
     * @throws IllegalStateException if initContainer has already been called,
     *          or if the SFAA has already been set.
     **/
    public void setSFAA(SyncFrameAccurateAnimation sfaa)  {
        if (this.sfaa != null) {
            throw new IllegalStateException();
        }
        this.sfaa = sfaa;
    }

    /**
     * {@inheritDoc}
     *
     * @param container  The container that our SFAA instance should be put in.
     *
     * @param bounds     The bounds of the SFAA instance we put within the
     *                   container.
     *
     * @see #setSFAA(SyncFrameAccurateAnimation)
     **/
    public void initContainer(Container container, Rectangle bounds) {
        this.container = container;
        if (sfaa == null) {
            AnimationParameters p = new AnimationParameters();
            p.threadPriority = Thread.NORM_PRIORITY - 1;
            p.scaleFactor = 1;
            p.repeatCount = null;
            p.lockedToVideo = false;
            p.faaTimer = null;
            Dimension d = new Dimension(bounds.width, bounds.height);
            sfaa = SyncFrameAccurateAnimation.getInstance(d, 1, p);
            sfaa.setLocation(bounds.x, bounds.y);
            container.add(sfaa);
            sfaa.setLocation(bounds.x, bounds.y);
            sfaa.start();
        } else {
            Point pos = sfaa.getLocation();
            bounds = new Rectangle();
            bounds.x = pos.x;
            bounds.y = pos.y;
            bounds.width = sfaa.getWidth();
            bounds.height = sfaa.getHeight();
        }
        this.bounds = bounds;
    }

    /** 
     * {@inheritDoc}
     **/
    public int getWidth() {
        return bounds.width;
    }

    /** 
     * {@inheritDoc}
     **/
    public int getHeight() {
        return bounds.height;
    }

    /**
     * {@inheritDoc}
     **/
    public Component getComponent() {
        return sfaa;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This also calls SyncFrameAccurateAnimation.start()
     **/
    public synchronized void start() {
        paused = false;
        notifyAll();
        sfaa.start();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This also calls SyncFrameAccurateAnimation.stop()
     **/
    public synchronized void pause() {
        paused = true;
        notifyAll();
        sfaa.stop();
    }


    /**
     * {@inheritDoc}
     **/
    protected void clearArea(int x, int y, int width, int height) {
        currGraphics.setColor(transparent);
        currGraphics.fillRect(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is always true for SFAA.  Even in SFAA with one buffer, we're
     * forced to redraw the entire buffer each time, because some 
     * implementations clear the buffer "for us" before each frame.  We
     * still have to clear the buffer, of course, because this behavior isn't
     * required.
     * <p>
     * Note that returning true here disables optimized drawing.  Alas,
     * this is necessary with SFAA - see the class comments for
     * this class.
     **/
    protected boolean needsFullRedrawInAnimationLoop() {
        return true;
    }

    /**
     * {@inheritDoc}
     **/
    protected void callPaintTargets() throws InterruptedException {
        paintTargets(currGraphics);
    }

    /**
     * {@inheritDoc}
     **/
    protected void finishedFrame() {
        currGraphics = null;
        sfaa.finishDrawing(frameNumber);
        Thread.currentThread().yield();
    }

    /**
     * {@inheritDoc}
     **/
    protected void runAnimationLoop() throws InterruptedException {
        for (;;) {
            checkNewClients();
            
            synchronized(this) {
                if (destroyRequested()) {
                    return;
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                if (paused) {
                    wait();
                    continue;
                }
            }

            currGraphics = sfaa.startDrawing(frameNumber);

            if (currGraphics != null) {
                currGraphics.setComposite(AlphaComposite.Src);
                advanceModel();
                showFrame();
                framesBehind = 0;
            } else {
                if (Debug.LEVEL > 0) {
                    skippedFrames++;
                }
                if (skipModel) {
                    // do nothing
                } else {
                    if (framesBehind < 3) {
                        framesBehind++;
                        advanceModel();
                    } else {
                        // Skip model update until we catch up.  This should be
                        // very rare, and will never happen if we're in the
                        // (recommended) skip model mode.
                    }
                }
            }
            synchronized(this) {
                frameNumber++;
            }
            if (Debug.LEVEL > 0 && (frameNumber % 100) == 0) {
                Debug.println("Frame " + frameNumber + ", "
                                + skippedFrames + " skipped.");
            }
        }
    }

    /**
     * Get the time of the current frame of animation.  This only works
     * if you provide the manager with an SFAA that has a media start and
     * stop time.
     *
     * @throws IllegalStateException if the underlying SFAA isn't running
     *          and presenting video, or if it doesn't have a start/stop time
     *          set.
     *
     * @see #setSFAA(SyncFrameAccurateAnimation)
     **/
    public Time getAnimationFrameTime() {
        long fn;
        synchronized(this) {
            fn = frameNumber;
            // The Java memory model allows longs to be in an inconsistent
            // internal state if modified by one thread while being read
            // from another.
        }
        return sfaa.getAnimationFrameTime(fn);
    }

    /**
     * Get the number of skipped frames over the lifetime of this animation.
     **/
    public int getSkippedFrames() {
        return skippedFrames;
    }

    /**
     * Get the current frame number.
     **/
    public synchronized long getFrameNumber() {
        return frameNumber;
    }


    /**
     * {@inheritDoc}
     * <p>
     * For SFAA, destroy must be called while the SFAA instance is running.
     * For an SFAA with a start/end time, this means video must be playing
     * and the media time must be between the start and end.
     * <p>
     * See also the class comments for this class.
     **/
    public void destroy() {
        SyncFrameAccurateAnimation s = sfaa;
        super.destroy();
        if (s != null) {
            try {
                s.destroy();
            } catch (Throwable ignored) {
            }
        }
    }


    /**
     * {@inheritDoc}
     **/
    protected void terminatingEraseScreen() {
        container.remove(sfaa);
        sfaa = null;
        if (bounds != null) {
            Graphics2D g = (Graphics2D) container.getGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(transparent);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.dispose();
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
