
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

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Graphics2D; 
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.awt.GraphicsConfiguration;
import java.awt.Color;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.imageio.ImageIO;

import com.hdcookbook.grinxlet.GrinXlet;

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.Director;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.animator.AnimationClient;
import com.hdcookbook.grin.animator.AnimationContext;
import com.hdcookbook.grin.animator.AnimationEngine;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.input.RCKeyEvent;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinBinaryReader;
import com.hdcookbook.grin.io.text.ShowParser;
import com.hdcookbook.grin.util.AssetFinder;

/**
 * This is a generic test program for exercising a show file.  It
 * accepts commands to boss the show around on stdin.  Probably
 * more interesting is the subclass of this, GrinView.
 * This command-line version came first, but I pretty quickly got
 * tired of it and made the GUI instead.  Because it fell out of
 * use, this class no longer has a main method; for the old
 * gui-less tool, use "GrinView -noui".
 *
 * @author Bill Foote (http://jovial.com)
 */
public class GenericMain extends Frame implements AnimationContext {
    
    protected SEShow show;
    private Director director;
    
    private Graphics2D frameGraphics;
    private int frame;          // Current frame we're on
    private float fps = 24.0f;

    private Image background = null;

    private ScalingDirectDrawEngine engine;
    
    private DeviceConfig deviceConfig = new DeviceConfig(1920, 1080);
    private int scaleDivisor = 2;
    private int screenWidth  = deviceConfig.width / scaleDivisor;
    private int screenHeight = deviceConfig.height / scaleDivisor;

    private boolean debugWaiting = false;
    private boolean debugDraw = false;
    private boolean initialized = false;
    private String initialSegmentName = null;
    private boolean doAutoTest = false;
    private boolean sendKeyUp = true;
    private Insets insets;

    private String directorClassName = GenericDirector.class.getName();

    private GrinXlet grinXlet;

    /**
     * Monitor to be held while coordinating a pause in the animation
     * for debug.
     **/
    protected Object debugWaitingMonitor = new Object();

    public GenericMain() {
           grinXlet = new GrinXlet(this);
        // This creates a facade for controlling us, e.g. from an xlet
        // built on the generic game framework.
    }

    public GenericMain(String grinxlet) {
        try {
            Class clazz = Class.forName(grinxlet);
            grinXlet = (GrinXlet) clazz.getConstructor(GenericMain.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // This creates a facade for controlling us, e.g. from an xlet
        // built on the generic game framework.
    }
    /**
     * Get the list of animation clients
     **/
    public AnimationClient[] getAnimationClients() {
        return engine.getAnimationClients();
    }

    /**
     * Reset the list of animation clients
     **/
    public void resetAnimationClients(AnimationClient[] clients) {
        engine.resetAnimationClients(clients);
    }

    /**
     * Give the animation engine
     **/
    public AnimationEngine getAnimationEngine() {
        return engine;
    }

    protected void setBackground(URL file) {
        if (background != null) {
            background.flush();
        }
        if (file == null) {
            engine.setBackground(null);
            System.out.println("Set background null.");
            return;
        }
        System.out.println("Setting background to " + file);
        Toolkit tk = Toolkit.getDefaultToolkit();
        background = tk.createImage(file);
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(background, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (background.getWidth(null) <= 0) {
            System.out.println("Error reading background image " + file);
            System.exit(1);
        }
        engine.setBackground(background);
    }
   
    /**
     * Adjust the scaling factor and the screen size used to display the show.
     * This can only be called before init().
     **/
    protected void adjustScreenSize(String scale, DeviceConfig config) {
        if (scale != null) {
           try {
              scaleDivisor = Integer.parseInt(scale);   
           } catch (NumberFormatException e) {
              System.out.println("Could not reset the scaling factor " + scale);
              return;
           }
        }   

        if (config != null) {
            deviceConfig = config;
        } 
               
        screenWidth = deviceConfig.width / scaleDivisor;
        screenHeight = deviceConfig.height / scaleDivisor;
    }

    public void setDirectorClassName(String nm) {
        directorClassName = nm;
    }
    
   
    protected void init(String showName, boolean isBinary,
            ShowBuilder builder,
            String initialSegmentName, boolean doAutoTest) {

        this.initialSegmentName = initialSegmentName;
        this.doAutoTest = doAutoTest;

        try {
            Class cl = Class.forName(directorClassName);
            director = (Director) cl.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
        show = createShow(showName, director, builder, isBinary);

        setBackground(Color.black);
        setLayout(null);
        pack();
        insets = getInsets();
        setSize(screenWidth + insets.left + insets.right,
                screenHeight + insets.top + insets.bottom);

        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
                exitGrinview();
            }
        });
        grinXlet.pushKeyInterest(show);
        addKeyListener(grinXlet);
        System.out.println("F1..F4 will generate red/green/yellow/blue, " + "F5 popup_menu");
        addMouseListener(grinXlet);
        addMouseMotionListener(grinXlet);
        setVisible(true);
    }

    private SEShow createShow(String showName, Director director, 
                              ShowBuilder builder, boolean isBinary) 
    {
        SEShow show = new SEShow(director);
        show.setIsBinary(isBinary);
        URL source = null;
        BufferedReader rdr = null;
        BufferedInputStream bis = null;
        try {
            source = AssetFinder.getURL(showName);
            if (source == null) {
                throw new IOException("Can't find resource " + showName);
            }
            if (AssetFinder.tryURL("images.map") != null) {
                System.out.println("Found images.map, using mosaic.");
                AssetFinder.setImageMap("images.map");
            } else {
                System.out.println("No images.map found");
            }
            
            if (!isBinary) {
                rdr = new BufferedReader(
                        new InputStreamReader(source.openStream(), "UTF-8"));
                ShowParser p = new ShowParser(rdr, showName, show, builder);
                p.parse();
                rdr.close();
            } else {
                bis = new BufferedInputStream(source.openStream());
                GrinBinaryReader reader = new GrinBinaryReader(bis);
                reader.readShow(show);
                bis.close();
            }   
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println();
            System.out.println(ex.getMessage());
            System.out.println();
            System.out.println("Error trying to parse " + showName);
            System.out.println("    URL:  " + source);
            System.exit(1);
        } finally {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (IOException ex) {
                }
            }   
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ex) {
                }    
            }
        }
        return show;
    }


    protected void exitGrinview() {
        try {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            show.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    protected float getFps() {
        return fps;
    }

    private void printHelpMessage() {
        System.out.println();
        System.out.println("Commands:  ");
        System.out.println("    f<number>    Set animation fps (0 is OK)");
        System.out.println("    s<segment>   Go to named segment");
        System.out.println("    +<number>    Advance that many frames");
        System.out.println("    +            Advance one frame");
        System.out.println("    w<number>    Wait this many seconds");
        System.out.println("    ? or h       Get this help message");
        System.out.println();
        System.out.println("Currently displaying " + fps + " fps.");
        System.out.println();
    }

    protected void startEngine() {
        setFps(fps);
        engine = new ScalingDirectDrawEngine(scaleDivisor, this);
        engine.initialize(this);        // Calls animationInitialize() and
                                        // animationFinishInitialiation()
        engine.start();
    }

    protected void inputLoop() {
        try {
            BufferedReader in 
                = new BufferedReader(new InputStreamReader(System.in));
            printHelpMessage();
            for (;;) {
                String msg = null;
                String s = in.readLine();
                if (s == null) {        // EOF
                    break;
                }
                if ("".equals(s) && userWaitingDone()) {
                    continue;
                    // Do nothing, we were waiting for enter
                }
                msg = doKeyboardCommand(s);
                if (msg != null) {
                    System.out.println(msg);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * When debugging frame-by-frame, this waits for the user to do
     * something, like hit enter or press a button.
     **/
    protected void waitForUser(String msg) {
        System.out.print("==>  " + msg + "; hit enter to advance...  ");
        System.out.flush();
        doWaitForUser();
    }

    /**
     * When debugging frame-by-frame, this is called when a complete
     * frame has just finished.
     **/
    public void debugDrawFrameDone() {
        // Overridden in GUI subclass
    }

    /**
     * Do the actual waiting on the monitor for waitForUser
     **/
    protected final void doWaitForUser() {
        synchronized(debugWaitingMonitor) {
            debugWaiting = debugDraw;
            while (debugWaiting) {
                try { 
                    debugWaitingMonitor.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }


    /**
     * This should be called when the wait of waitForUser() is done
     *
     * @return true  if we were waiting
     **/
    protected boolean userWaitingDone() {
        boolean wasWaiting;
        synchronized(debugWaitingMonitor) {
            wasWaiting = debugWaiting;
            debugWaiting = false;
            debugWaitingMonitor.notifyAll();
        }
        return wasWaiting;
    }

    public void snapshot() {
        BufferedImage snapshot;
        BufferedImage framebuffer;
        snapshot = new BufferedImage(deviceConfig.width, deviceConfig.height, 
                                         BufferedImage.TYPE_INT_ARGB);
        framebuffer = new BufferedImage(deviceConfig.width, deviceConfig.height, 
                                         BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = framebuffer.createGraphics();
        try {
            engine.repaintFrame(g);
        } catch (InterruptedException ignored) {
        }
        g.dispose();
        g = snapshot.createGraphics();
        g.setComposite(AlphaComposite.Src);
        if (background == null) {
            g.setColor(new Color(0,0,0,0));
            g.fillRect(0, 0, deviceConfig.width, deviceConfig.height);
        } else {
            g.drawImage(background, 0, 0, null, null);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(framebuffer, 0, 0, null, null);
        g.dispose();
        final BufferedImage snapshotF = snapshot;
        new Thread(new Runnable() {
            public void run() {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Save .png snapshot");
                int ret = fc.showSaveDialog(GenericMain.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Saving PNG to " + file + "...");
                    try {
                        boolean ok = ImageIO.write(snapshotF, "PNG", file);
                        if (ok) {
                            System.out.println("Saved snapshot to " + file);
                        } else {
                            System.out.println("**** Error writing to " + file);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.out.println("**** Error writing to " + file);
                    }
                }
            }
        }).start();
    }

    public String doKeyboardCommand (String s) {
        if (!initialized) {
            synchronized(this) {
                while (!initialized) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }

        try {
            if (s.startsWith("s")) {
                s = s.substring(1).trim();
                return gotoSegment(s);
            } else if (s.startsWith("w")) {
                s = s.substring(1).trim();
                float tm = 0f;
                try {
                    tm = Float.parseFloat(s);
                } catch (NumberFormatException ex) {
                    System.out.println(ex);
                }
                long ltm = (long) (tm * 1000);
                System.out.println("Sleeping " + ltm + " ms.");
                try {
                    Thread.sleep(ltm);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                return "Slept " + tm + " seconds.";
            } else if (s.startsWith("+")) {
                s = s.substring(1).trim();
                int num = 0;
                if ("".equals(s)) {
                    num = 1;
                } else {
                    try {
                        num = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        System.out.println(ex);
                    }
                }
                return advanceFrames(num);
            } else if (s.startsWith("f")) {
                s = s.substring(1).trim();
                float newFps = 0f;
                try {
                    newFps = Float.parseFloat(s);
                } catch (NumberFormatException ex) {
                    System.out.println(ex);
                }
                return setFps(newFps);
            } else {
                printHelpMessage();
                if ("?".equals(s) || "h".equals(s)) {
                    return null;
                } else {
                    return "Command \"" + s + "\" unrecognized.";
                }
            }
        } catch (NumberFormatException ex) {
            return ex.toString();
        }
    }

    protected String gotoSegment(String name) {
        Segment seg = show.getSegment(name);
        String msg = null;
        if (seg != null) {
            msg = "Activating public segment " + seg;
        } else {
            seg = show.getPrivateSegment(name);
            if (seg != null) {
                msg = "Activating private segment " + seg;
            }
        }
        if (msg == null) {
            return "No segment called \"" + name + "\".";
        } else {
            show.activateSegment(seg);
            return msg;
        }
    }
    
    protected String advanceFrames(int num) {
        try {
            engine.skipFrames(num);
        } catch (InterruptedException ignored) {
        }
        return "    Skipped " + num + " frames.";
    }

    public void setDebugDraw(boolean doDebugDraw) {
        debugDraw = doDebugDraw;
        synchronized(debugWaitingMonitor) {
            if (!debugDraw) {
                debugWaiting = false;
                debugWaitingMonitor.notifyAll();
            }
        }
        if (engine != null) {
            engine.setDebugDraw(doDebugDraw);
        }
    }

    public void setSendKeyUp(boolean v) {
        sendKeyUp = v;
    }

    public boolean getSendKeyUp() {
        return sendKeyUp;
    }
    
    protected String setFps(float newFps) {
        if ((fps > 0) && (newFps <= 0)) {
            setDebugDraw(false);
            // Going into single-step mode when in debug draw
            // can lead to deadlocks.  Fixing this would probably require
            // cleaning up GrinView's locking model, which isn't worth
            // doing for just this, so when the framerate goes to 0, we
            // just uncheck the debug draw checkbox, and re-set debug draw
            // mode.
        }
        fps = newFps;
        if (engine != null) {
            if (newFps <= 0) {
                try {
                    engine.pause();
                    engine.skipFrames(1);  // Output one more, with background.
                } catch (InterruptedException ignored) {
                }
            } else {
                engine.setFps((int) (newFps * 1001));
                engine.start();
            }
        }
        return "    Set fps to " + newFps;
    }
    
    public void animationInitialize() throws InterruptedException {
        AnimationClient[] clients = { show };
        engine.initClients(clients);
        GraphicsConfiguration con = getGraphicsConfiguration();
        if (con.getColorModel().getTransparency() != Transparency.TRANSLUCENT) {
            // On windows and Mac/Intel/Leopard (at least), alpha blending to a 
            // background image requires
            // special handling.  See the comments in paint(Graphics).
            BufferedImage im 
                = con.createCompatibleImage(screenWidth, screenHeight);
            engine.setNonTranslucentFix(im);
        }
        Rectangle ourBounds=new Rectangle(insets.left, insets.top, 1920, 1080);
        engine.initContainer(this, ourBounds);
    }

    public void animationFinishInitialization() throws InterruptedException {
        System.out.println("Starting frame pump...");
        synchronized(this) {
            initialized = true;
            notifyAll();
        }
        if (initialSegmentName != null) {
            doKeyboardCommand("s " + initialSegmentName); 
                // Calls Show.activateSegment
        }
        
        requestFocus();

        if (doAutoTest) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Segment[] segments = show.getSegments();
                        for (Segment seg : segments) {
                            String name = seg.getName();
                            if (name != null) {
                                doKeyboardCommand("s " + name);
                                Thread.sleep(1000);
                            }
                        }
                        dispose();
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }.start();
        }
    }

    public int getScaleDivisor() {
        return scaleDivisor;
    }
    
    static class DeviceConfig {
       public int width;
       public int height;
    
       public DeviceConfig(int w, int h) {
          width  = w;
          height = h;
       }
    }
}
