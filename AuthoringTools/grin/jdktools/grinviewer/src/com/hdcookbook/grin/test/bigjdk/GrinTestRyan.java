
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
import java.awt.Scrollbar;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;

import com.hdcookbook.grin.Director;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.input.RCKeyEvent;
import com.hdcookbook.grin.test.RyanDirector;
import com.hdcookbook.grin.util.AssetFinder;

/**
 * This is a small program to exercise the GRIN
 * framework.  It was originally a demo that was
 * run on an MHP set top box in a demo at the DVD
 * Forum in 2005.  That demo had this application
 * running on top of a video called "Ryan's Life."
 * <p>
 * This program should run on any version of desktop
 * JDK.
 *
 * @author Bill Foote (http://jovial.com)
 */
public class GrinTestRyan extends Frame {
    
    static int FRAME_CHEAT = 16;
    static int WIDTH = 720;
    static int HEIGHT = 576;

    private Show show;
    private Scrollbar scrollbar;
    private MainRyanDirector director;
    
    /** Creates a new instance of GrinTestRyan */
    public GrinTestRyan() {
    }
    
    private void init() {
        director = new MainRyanDirector();
        show = director.createShow();
        show.initialize(this);

        int sbHeight = 0;
        scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 0, 10000, 0, 90000);
        sbHeight = scrollbar.getPreferredSize().height;
        if (sbHeight <= 0) {
            sbHeight = 14;
        }

        setLayout(null);
        setBackground(Color.black);
        setSize(WIDTH, HEIGHT + FRAME_CHEAT + sbHeight);  // 720x576 is SD in Europe
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                System.exit(0);
            }
        });

        java.awt.event.KeyAdapter listener = new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                // ignored
            }
            public void keyPressed(java.awt.event.KeyEvent e) {
                int code = e.getKeyCode();
                // Translate F1..F4 into red/green/yellow/blue
                if (code >= e.VK_F1 && code <= e.VK_F4) {
                    code = code - e.VK_F1 + RCKeyEvent.KEY_RED.getKeyCode();
                } else if (code >= e.VK_NUMPAD0 && code <= e.VK_NUMPAD9) {
                    code = code - e.VK_NUMPAD0 + RCKeyEvent.KEY_0.getKeyCode();
                } else if (code == e.VK_F5) {
                    code = RCKeyEvent.KEY_POPUP_MENU.getKeyCode();
                }
                show.handleKeyPressed(code);
            }
            public void keyTyped(java.awt.event.KeyEvent e) {
            }
        };
        addKeyListener(listener);
        java.awt.event.MouseAdapter mouseL = new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                show.handleMousePressed(e.getX(), e.getY(), false);
            }
        };
        addMouseListener(mouseL);
        java.awt.event.MouseMotionAdapter mouseM = new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                show.handleMouseMoved(e.getX(), e.getY(), false);
            }
        };
        addMouseMotionListener(mouseM);
        System.out.println("F1..F4 will generate red/green/yellow/blue, F5 popup_menu");
        if (scrollbar != null) {
            scrollbar.addKeyListener(listener);
        }
        setVisible(true);
    }

    private void run() throws InterruptedException {
        Segment sInitialize = show.getSegment("initialize");
        Segment sStartShow = show.getSegment("start_show");

        show.activateSegment(sInitialize);

        show.nextFrame();
        director.init();
        director.waitForVideoStartOK();
        System.out.println("Pretend the video is starting...");
        show.activateSegment(sStartShow);
        Graphics2D frameGr = (Graphics2D) getGraphics();
        BufferedImage bufIm = getGraphicsConfiguration()
                                        .createCompatibleImage(WIDTH, HEIGHT);
        Graphics2D bufGr = bufIm.createGraphics();
        bufGr.setColor(Color.black);
        bufGr.fillRect(0,0,WIDTH, HEIGHT);
        frameGr.drawImage(bufIm,0, FRAME_CHEAT, WIDTH, FRAME_CHEAT+HEIGHT,this);
        long startTime = System.currentTimeMillis();
        int fps = 25;   // Run at 25p
        director.setFrame(0);
        Color transparentColor = new Color(0,0,0,0);
        for (int frame = 1; ; frame++) {
            for (;;) {
                director.setFrame(frame);
                long tm = startTime + (frame * 1000) / fps;
                long delta = System.currentTimeMillis() - tm;
                if (delta < 0) {
                    Thread.sleep(-delta);
                } else if (delta > (1000 / fps)) {
                    // We've fallen behind, skip a frame
                    System.out.println("Behind by " + (delta  - (1000 / fps))
                                        +"ms, Skipping frame " + frame + "...");
                    frame++;
                } else {
                    break;
                }
            }
            bufGr.setComposite(AlphaComposite.Src);
            bufGr.setColor(transparentColor);
            show.nextFrame();
            synchronized(show) {
                bufGr.fillRect(0, 0, WIDTH, HEIGHT);
                bufGr.setComposite(AlphaComposite.SrcOver);
                show.paintFrame(bufGr);
            }
            frameGr.setComposite(AlphaComposite.Src);
            frameGr.drawImage(bufIm, 
                              0, FRAME_CHEAT, 
                              WIDTH, FRAME_CHEAT+HEIGHT,
                              0, 0, WIDTH, HEIGHT,
                              this);
            Toolkit.getDefaultToolkit().sync();
        }
    }
    
    private static void usage() {
        System.out.println();
        System.out.println("Usage:  java com.hdcookbook.grin.test.bigjdk.GrinTestRyan \\");
        System.out.println("        -assets <asset_dir>");
        System.out.println("        -imagemap <mapfile>");
        System.out.println("        <show file>");
        System.out.println("");
        System.out.println("    -assets may be repeated");
        System.out.println();
        System.exit(1);
    }
    
    /**
     */
    public static void main(String[] args) {
        LinkedList assetPathLL = new LinkedList();
        String imageMap = null;
        int argsUsed = 0;
        while (argsUsed < args.length - 1) {
            if ("-assets".equals(args[argsUsed])) {
                argsUsed++;
                String path = args[argsUsed];
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                assetPathLL.add(path);
                argsUsed++;
            } else if ("-imagemap".equals(args[argsUsed])) {
                if (imageMap != null) {
                    usage();
                }
                argsUsed++;
                imageMap = args[argsUsed];
                argsUsed++;
            } else {
                break;
            }
        }
        if (argsUsed != args.length) {
            usage();
        }
        String[] assetPath;
        if (assetPathLL.size() == 0) {
            assetPath = new String[] { "../test/assets/" };
        } else {
            assetPath = (String[]) 
                        assetPathLL.toArray(new String[assetPathLL.size()]);
        }
        AssetFinder.setHelper(new AssetFinder() {
            protected void abortHelper() {
                System.exit(1);
            }
        });
        AssetFinder.setSearchPath(assetPath, null);
        if (imageMap != null) {
            AssetFinder.setImageMap(imageMap);
        }
        
        GrinTestRyan t = new GrinTestRyan();
        t.init();
        try {
            t.run();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
    
}
