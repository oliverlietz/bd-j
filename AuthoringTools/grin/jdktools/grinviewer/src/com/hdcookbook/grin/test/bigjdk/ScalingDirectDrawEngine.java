
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

import com.hdcookbook.grin.animator.ClockBasedEngine;
import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.Profile;
import java.awt.AlphaComposite;
import java.awt.Container;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * A double-buffered animation engine that uses direct draw, and
 * applies a scaling divisor.  This is based off of DirectDrawEngine,
 * with some adjustments to make it work on big JDK and provide
 * scaling.
 *
 * @see com.hdcookbook.grin.animator.DirectDrawEngine
 **/
public class ScalingDirectDrawEngine extends ClockBasedEngine {


    private Container container;
    private Component ddComponent;
    private BufferedImage buffer;
    private Graphics2D bufferG;
    private Graphics2D componentG;
    private final int scaleDivisor;
    private Image background;
    private BufferedImage nonTranslucentFix;
    private boolean debugDraw = false;
    private GenericMain main;
    private byte[] profileBlitToFB;     // Profiling model update
    private int engineNumber = 0;
    private static int nextEngineNumber = 0;

    
    /**
     * Create a new ScalingDirectDrawEngine.  It needs to be initialized with
     * the various initXXX methods (including the inherited ones).
     **/
    public ScalingDirectDrawEngine(int scaleDivisor, GenericMain main) {
        this.scaleDivisor = scaleDivisor;
        this.main = main;
        if (Debug.LEVEL > 0) {
            engineNumber = getNextEngineNumber();
        }
        if (Debug.PROFILE && Debug.PROFILE_ANIMATION) {
            profileBlitToFB = Profile.makeProfileTimer("biltToFB("+this+")");
        }
    }


    private synchronized static int getNextEngineNumber() {
        if (Debug.LEVEL > 0) {
            nextEngineNumber++;
        }
        return nextEngineNumber;
    }

    public String toString() {
        if (Debug.LEVEL <= 0) {
            return super.toString();
        } else {
            return "GrinView DD engine " + engineNumber;
        }
    }

    /**
     * {@inheritDoc}
     **/
    public void initContainer(Container container, Rectangle bounds) {
        try {
            ourInitContainer(container, bounds);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
            // If we don't do this, then the finally clause in
            // AnimationEngine.run() swallows the error message,
            // and often generates an assertion failure on Show.destroy()
            // that hides the real problem.
        }
    }

    private void ourInitContainer(Container container, Rectangle bounds) {
        buffer = new BufferedImage(bounds.width, bounds.height,
                                   BufferedImage.TYPE_4BYTE_ABGR);
            // For GrinView, we specifically want TYPE_4BYTE_ABGR,
            // and not GrahicsConfiguration.createCompatibleImage().
            // This is a big JDK program, so the color model is not
            // defined like it is in BD-J.
        bufferG = buffer.createGraphics();
        bufferG.setComposite(AlphaComposite.Src);
        bufferG.setColor(transparent);
        bufferG.fillRect(0, 0, bounds.width, bounds.height);

        this.container = container;
        ddComponent = new Component() {
            public void paint(Graphics g) {
                if (Debug.LEVEL > 0) {
                    Debug.println("repainting...");
                }
                paintNextFrameFully();
                    // This will happen the next time a frame is displayed.
                    // That's not really what you'd want in professional
                    // content, but it's good enough for a debug tool.
            }
        };
        ddComponent.setBounds(bounds);
        container.add(ddComponent);
        ddComponent.setVisible(true);

        componentG = (Graphics2D) ddComponent.getGraphics();
        if (Debug.ASSERT && componentG == null) {
            Debug.assertFail();  // Maybe container is invisible?
        }
        componentG.setComposite(AlphaComposite.Src);
    }

    /** 
     * {@inheritDoc}
     **/
    public int getWidth() {
        return buffer.getWidth();
    }

    /** 
     * {@inheritDoc}
     **/
    public int getHeight() {
        return buffer.getHeight();
    }

    /**
     * {@inheritDoc}
     **/
    public Component getComponent() {
        return ddComponent;
    }

    //
    // Ask us to start painting the background, or not
    //
    synchronized void setBackground(Image bg) {
        background = bg;
        paintNextFrameFully();
    }

    //
    // Set up a fix for platforms (like Windows) where translucent
    // drawing doesn't work right unless it's to a buffer
    //
    synchronized void setNonTranslucentFix(BufferedImage buf) {
        nonTranslucentFix = buf;
    }

    //
    // Tell is if we should step through each frame showing
    // erase, paint areas, then painted result
    //
    void setDebugDraw(boolean debugDraw) {
        this.debugDraw = debugDraw;
    }



    /**
     * {@inheritDoc}
     **/
    protected void clearArea(int x, int y, int width, int height) {
        bufferG.setColor(transparent);
        bufferG.fillRect(x, y, width, height);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is always false for direct draw.  Because we maintian our
     * own double buffer, nothing external can damage its contents.
     **/
    protected boolean needsFullRedrawInAnimationLoop() {
        return false;
    }

    /**
     * {@inheritDoc}
     **/
    protected void callPaintTargets() throws InterruptedException {
        if (debugDraw) {
                // Paint the area to be erased red, and wait
            int s = scaleDivisor;
            componentG.setColor(new Color(255, 0, 0, 127));
            componentG.setComposite(AlphaComposite.SrcOver);
            for (int i = 0; i < getNumEraseTargets(); i++) {
                Rectangle a = getEraseTargets()[i];
                if (!a.isEmpty()) {
                    componentG.fillRect(a.x/s, a.y/s, a.width/s, a.height/s);
                }
            }
            Toolkit.getDefaultToolkit().sync();
            main.waitForUser("To be erased areas shown with red overlay");

                // Paint the area to be drawn green, and wait
            componentG.setColor(new Color(0, 255, 0, 127));
            for (int i = 0; i < getNumDrawTargets(); i++) {
                Rectangle a = getDrawTargets()[i];
                componentG.fillRect(a.x/s, a.y/s, a.width/s, a.height/s);
            }
            Toolkit.getDefaultToolkit().sync();
            main.waitForUser("To be drawn areas shown with green overlay");
            componentG.setComposite(AlphaComposite.Src);
        }
        paintTargets(bufferG);
        bufferG.setComposite(AlphaComposite.Src);       // Add some robustness
    }

    /**
     * {@inheritDoc}
     **/
    protected void runAnimationLoop() throws InterruptedException {
        try {
            super.runAnimationLoop();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
            // If we don't do this, then the finally clause in
            // AnimationEngine.run() swallows the error message,
            // and often generates an assertion failure on Show.destroy()
            // that hides the real problem.
        }
    }

    /**
     * {@inheritDoc}
     **/
    protected void finishedFrame() {
        //
        //  This method gets a little complex.  It's here where we apply
        //  the scaling, and it's here where we simulate the BD model
        //  of BD-J graphics over the video plane.
        //
        int tok;
        if (Debug.PROFILE && Debug.PROFILE_ANIMATION) {
            tok = Profile.startTimer(profileBlitToFB, Profile.TID_ANIMATION);
        }
        Image bg = background;
        Graphics2D g = componentG;
        Graphics2D fixG = null;
        if (nonTranslucentFix != null && bg == null) {
                // On windows and Mac/Leopard/Intel (at least), 
                // the graphics device doesn't
                // natively support a translucent color model.
                // This means that alpha-blended colors don't
                // show up properly, unless we SrcOver draw them
                // over a background.  That burns another
                // framebuffer:  nonTranslucentFix.
            fixG = g;
            g = nonTranslucentFix.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.black);
            g.fillRect(0, 0, nonTranslucentFix.getWidth(),
                             nonTranslucentFix.getHeight());
            g.setComposite(AlphaComposite.SrcOver);
        } else {
            g.setComposite(AlphaComposite.Src);
        }
        if (getNumDrawTargets() > 0) {
            if (scaleDivisor == 1) {
                for (int i = 0; i < getNumDrawTargets(); i++) {
                    Rectangle a = getDrawTargets()[i];
                    if (bg != null) {
                        g.setComposite(AlphaComposite.Src);
                        drawScaledImage(g, bg, a);
                        g.setComposite(AlphaComposite.SrcOver);
                    }
                    drawScaledImage(g, buffer, a);
                }
            } else {
                // If we're scaling, then rounding errors can cause
                // odd off-by-one style artifacts, so we just paint the
                // whole frame if anything's changed.
                Rectangle a = new Rectangle(0, 0, buffer.getWidth(), 
                                                  buffer.getHeight());
                if (bg != null) {
                    g.setComposite(AlphaComposite.Src);
                    drawScaledImage(g, bg, a);
                    g.setComposite(AlphaComposite.SrcOver);
                }
                drawScaledImage(g, buffer, a);
            }
            if (fixG != null)  {
                g.dispose();
                g = fixG;
                g.setComposite(AlphaComposite.SrcOver);
                if (scaleDivisor == 1) {
                    for (int i = 0; i < getNumDrawTargets(); i++) {
                        Rectangle a = getDrawTargets()[i];
                        int s = scaleDivisor;
                        g.drawImage(nonTranslucentFix,
                                    a.x/s, a.y/s, 
                                    (a.x+a.width)/s, (a.y+a.height)/s,
                                    a.x/s, a.y/s, 
                                    (a.x+a.width)/s, (a.y+a.height)/s,
                                    null);
                    }
                } else {
                    Rectangle a = new Rectangle(0, 0, buffer.getWidth(), 
                                                      buffer.getHeight());
                    int s = scaleDivisor;
                    g.drawImage(nonTranslucentFix,
                                a.x/s, a.y/s, 
                                (a.x+a.width)/s, (a.y+a.height)/s,
                                a.x/s, a.y/s, 
                                (a.x+a.width)/s, (a.y+a.height)/s,
                                null);
                }
            }
            Toolkit.getDefaultToolkit().sync();
        }
        if (Debug.PROFILE && Debug.PROFILE_ANIMATION) {
            Profile.stopTimer(tok);
        }
        Thread.currentThread().yield();
        if (debugDraw) {
            main.waitForUser("Frame drawn");
            main.debugDrawFrameDone();
        }
    }

    private void drawScaledImage(Graphics2D dest, Image src, Rectangle a) {
        int s = scaleDivisor;
        dest.drawImage(src, a.x/s, a.y/s, 
                            (a.x+a.width)/s, (a.y+a.height)/s,
                            a.x, a.y,
                            a.x+a.width, a.y+a.height,
                            null);
    }

    /**
     * {@inheritDoc}
     **/
    protected void terminatingEraseScreen() {
        int s = scaleDivisor;
        componentG.setColor(transparent);
        componentG.fillRect(0, 0, buffer.getWidth()/s, buffer.getHeight()/s);
        Toolkit.getDefaultToolkit().sync();
    }
}
