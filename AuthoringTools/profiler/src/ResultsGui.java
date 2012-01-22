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

/**
 *  This is a really simple GUI to show the results of a profiling run graphically.
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFileChooser;
import javax.imageio.ImageIO;

import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.Profile;

public class ResultsGui extends Frame {

    private long earliestTimestamp;             // Earliest timestame of any packet
    private long startTime = Long.MAX_VALUE;    // start/end time of data
    private long endTime = Long.MIN_VALUE;
    private long displayStart;                  // start/end being displayed
    private long displayEnd;
    private String[] timerName;
    private ProfileTiming[][] timings;
    private Packet[] debugMessages;
    private boolean ready = false;
    private Font debugMessageFont = new Font("SansSerif", Font.BOLD, 16);
    private int lastMessageShown = -1;
    private File defaultDirectory = new File(".");


    private static Color[] colors = { Color.blue, Color.cyan, Color.green, Color.magenta,
                                      Color.orange, Color.pink, Color.red, Color.yellow };
    private static String[] colorNames = { "blue   ", "cyan   ", "green  ", "magenta",
                                           "orange ", "pink   ", "red    ", "yellow " };
    private int H_BORDER = 35;

    public ResultsGui() {
    }

    public void init(ProfilingRun run) {
        debugMessages = run.debugMessages;
        earliestTimestamp = run.earliestTimestamp;
        HashMap<String, Integer> msgIndex = new HashMap<String, Integer>();
        for (int i = 0; i < run.timings.length; i++) {
            ProfileTiming t = run.timings[i];
            String m = t.message;
            if (msgIndex.get(m) == null) {
                Integer v = new Integer(msgIndex.size());
                msgIndex.put(m, v);
            }
            if (t.startTime < startTime) {
                startTime = t.startTime;
            }
            long end = t.startTime + t.duration;
            if (end > endTime) {
                endTime = end;
            }
        }
        int num = msgIndex.size();
        System.out.println(num + " unique messages");
        timerName = new String[num];
        timings = new ProfileTiming[num][];
        for (Map.Entry<String, Integer> entry : msgIndex.entrySet()) {
            timerName[entry.getValue().intValue()] = entry.getKey();
        }
        int[] sizes = new int[num];
        for (int i = 0; i < run.timings.length; i++) {
            ProfileTiming t = run.timings[i];
            int name = msgIndex.get(t.message).intValue();
            t.message = timerName[name];        
                // Canonicalize string instances to save memory
            sizes[name]++;
        }

        // Now arrange the ProfileTiming instances by name
        for (int i = 0; i < sizes.length; i++) {
            timings[i] = new ProfileTiming[sizes[i]];
            sizes[i] = 0;
        }
        for (int i = 0; i < run.timings.length; i++) {
            ProfileTiming t = run.timings[i];
            int name = msgIndex.get(t.message).intValue();
            timings[name][sizes[name]] = t;
            sizes[name]++;
        }

        displayStart = startTime;
        displayEnd = endTime;
        synchronized(this) {
            ready = true;
            repaint();
        }

        setSize(1000, 600);
        setVisible(true);
    }

    public void printHelp() {
        System.out.println();
        System.out.println("Commands:");
        System.out.println("    d <s> <e>  Set display range, in seconds");
        System.out.println("    l          Move left 1/2 screen");
        System.out.println("    r          Move right 1/2 screen");
        System.out.println("    m <a> <b>  Move row a to row b, counting from 0");
        System.out.println("    p <num>    Print debug message <num>");
        System.out.println("    s          Save snapshot of screen as .png");
        System.out.println("    t          Print text report to stdout");
        System.out.println("    q          Quit");
        System.out.println("  <eof>        Same as q");
        System.out.println();
        System.out.println("Data runs from " + nsToMs(startTime) 
                            + "ms to " + nsToMs(endTime) + "ms.");
        System.out.println("Displaying from " + nsToMs(displayStart)
                            + "ms to " + nsToMs(displayEnd) + "ms.");
        System.out.println();
        for (int i = 0; i < timerName.length; i++) {
            System.out.println("    " + i + " - " + colorNames[i % colorNames.length]
                               + "    " + timerName[i]);
        }
        System.out.println();
    }

    public long nsToMs(long ns) {
        return (ns + 50000) / 1000000;
    }
    
    public void paint(Graphics g) {
        synchronized(this) {
            if (!ready) {
                return;
            }
        }
        int height = getHeight();
        int width = getWidth() - 2 * H_BORDER;
        if (width < 0) {
            return;
        }
        g.setColor(Color.black);
        g.fillRect(0, 0, width + 2 * H_BORDER, height);

        g.translate(H_BORDER, 0);

        g.setFont(debugMessageFont);
        g.setColor(new Color(164, 164, 164, 255));
        drawScale(g, displayStart, displayEnd, width, 160 + 70*timings.length);

        for (int i = 0; i < debugMessages.length; i++) {
            if (i == lastMessageShown) {
                g.setColor(Color.white);
            } else { 
                g.setColor(Color.lightGray);
            }
            int y = 60;
            if (i % 2 == 1) {
                y = 80;
            }
            Packet msg = debugMessages[i];
            double x = msg.timestamp - earliestTimestamp;
            x -= displayStart;
            x *= width;
            x /= (displayEnd - displayStart);
            g.drawString("D_" + i, ((int) (x + 0.5)), y);
        }

        for (int listNum = 0; listNum < timings.length; listNum++) {
            g.setColor(colors[listNum % colors.length]);
            int y = 100 + 70*listNum;
            ProfileTiming[] list = timings[listNum];
            for (int i = 0; i < list.length; i++) {
                ProfileTiming t = list[i];
                double x = t.startTime;
                x -= displayStart;
                x *= width;
                x /= (displayEnd - displayStart);
                double w = t.duration;
                w *= width;
                w /= (displayEnd - displayStart);
                int ix = (int) (x + 0.5);
                int iw = (int) (w + 0.5);
                if (iw <= 0) {
                    iw = 1;
                }
                g.fillRect(ix, y, iw, 50);
            }
        }
    }

    //
    // Draw the scale along the bottom.
    //
    // This tries to do a reasonable job of making intuitive numbers along
    // the bottom, but the algorithm could stand some improvement.  It does
    // make sure the numbers don't run into eachother, but it doesn't make sure
    // they don't get clipped at the left and right edges of the screen.  If
    // you're zoomed into a small area a good way into the data, the numbers
    // get big.  For example, if you display 10s to 10s+100ns, it will express 
    // the numbers in ns, and 10s expressed in ns is a big number.
    //
    private void drawScale(Graphics g, long start, long end, int width, int y) {
        long delta = end - start;
        if (delta <= 0) {
            return;
        }
        String units;
        long divisor;
        long increment;
        if (delta >= 10000000000L) { // 10 s
            divisor = 1000000000L; 
            increment = 1000000000L; 
            units = "s";
        } else if (delta >= 1000000000L) { // 1 s
            divisor = 1000000; 
            increment = 100000000L; 
            units = "ms";
        } else if (delta >= 100000000) { // 100 ms
            divisor = 1000000; 
            increment = 10000000; 
            units = "ms";
        } else if (delta >= 10000000) { // 10 ms
            divisor = 1000000; 
            increment = 1000000; 
            units = "ms";
        } else if (delta >= 1000000) { // 1 ms
            divisor = 1000; 
            increment = 100000; 
            units = "\u00B5s";  // 00B5 is micro
        } else if (delta >= 100000) { // 100 us
            divisor = 1000; 
            increment = 10000; 
            units = "\u00B5s";
        } else if (delta >= 10000) { // 10 us
            divisor = 1000; 
            increment = 1000; 
            units = "\u00B5s";
        } else if (delta >= 1000) { // 1 us
            divisor = 1; 
            increment = 100; 
            units = "ns";
        } else if (delta >= 100) { // 100 ns
            divisor = 1; 
            increment = 10; 
            units = "ns";
        } else { // 10 ns
            divisor = 1; 
            increment = 1; 
            units = "ns";
        }
        int r = (int) (delta / increment);      // 10..100
        if (r <= 10) {
            // do nothing;
        } else if (r <= 20) {
            increment *= 2;
        } else {
            increment *= 5;
        }
        int lastX = -100000;
        int i = ((int) (start / increment));
        for (;;) {
            long v = i * increment;
            int d = (int) (v / divisor);
            double x = v;
            x -= start;
            x *= width;
            x /= (end - start);
            int ix = (int) (x + 0.5);
            g.drawLine(ix, 90, ix, y);
            FontMetrics fm = g.getFontMetrics();
            String msg = "" + d + " " + units;
            int w = fm.stringWidth(msg);
            int sx = ix - (w / 2);
            if (sx > lastX +6) {
                lastX = sx + w;
                g.drawString(msg, ix - (w / 2), y + 20);
            }
            if (v >= end + increment) {
                break;
            }
            i++;
        }
    }

    public void readCommands(BufferedReader in) {
        String blankPattern = "\\p{Blank}+";
        for (;;) {
            String s = null;
            printHelp();
            try {
                s = in.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            if (s == null) {
                System.exit(0);
            }
            s=s.trim().toLowerCase();
            if ("q".equals(s)) {
                System.exit(0);
            } else if ("l".equals(s)) {
                moveScreen(-0.5);
            } else if ("r".equals(s)) {
                moveScreen(0.5);
            } else if (s.startsWith("d")) {
                setRange(s.substring(1).trim().split(blankPattern));
            } else if (s.startsWith("m")) {
                moveRow(s.substring(1).trim().split(blankPattern));
            } else if (s.startsWith("p")) {
                printMessage(s.substring(1).trim().split(blankPattern));
            } else if ("s".equals(s)) {
                snapshot();
            } else if ("t".equals(s)) {
                printTextReport();
            } else {
                System.out.println("??" + ((char) 7));
            }
        }
    }

    public void printTextReport() {
        System.out.println();
        System.out.println();
        System.out.println("    PROFILING REPORT");
        System.out.println();
        for (int i = 0; i < timerName.length; i++) {
            System.out.println(timerName[i] + ":");
            ProfileTiming[] list = timings[i];
            for (int j = 0; j < list.length; j++) {
                ProfileTiming t = list[j];
                System.out.println("    start " + formatTime(t.startTime) + " for "
                                   + formatTime(t.duration) + ", thread "
                                   + t.threadID);
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < debugMessages.length; i++) {
            Packet m = debugMessages[i];
            System.out.println(formatTime(m.timestamp - earliestTimestamp) 
                                + " thread " + m.threadID);
            System.out.println(m.getDebugMessage());
        }
        System.out.println();
        System.out.println();
    }

    private static String formatTime(long ns) {
        float ms = ns / 1000000f;
        return "" + ms + "ms";
    }

    private void setRange(String[] args) {
        if (args.length < 2) {
            System.out.println("??" + ((char) 7));
            return;
        }
        double start = 0;
        double end = 0;
        try {
            start = Double.parseDouble(args[0]);
            end = Double.parseDouble(args[1]);
        } catch (NumberFormatException ex) {
            System.out.println("??" + ((char) 7));
            return;
        }
        displayStart = (long) (0.5 + start * 1000000000.0);
        displayEnd = (long) (0.5 + end* 1000000000.0);
        repaint();
    }

    private void moveRow(String[] args) {
        if (args.length < 2) {
            System.out.println("??" + ((char) 7));
            return;
        }
        int from = 0;
        int to = 0;
        double end = 0;
        try {
            from = Integer.parseInt(args[0]);
            to = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            System.out.println("??" + ((char) 7));
            return;
        }
        if (from < 0 || from >= timerName.length || to < 0 || to >= timerName.length) {
            System.out.println("??" + ((char) 7));
            return;
        }
        String tmpS = timerName[from];
        ProfileTiming[] tmpT = timings[from];
        if (from < to) {
            for (int i = from; i < to; i++) {
                timerName[i] = timerName[i+1];
                timings[i] = timings[i+1];
            }
        } else if (from > to) {
            for (int i = from; i > to; i--) {
                timerName[i] = timerName[i-1];
                timings[i] = timings[i-1];
            }
        }
        timerName[to] = tmpS;
        timings[to] = tmpT;
        repaint();
    }

    private void printMessage(String[] args) {
        if (args.length < 1) {
            System.out.println("??" + ((char) 7));
            return;
        }
        int num = 0;
        try {
            num = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            System.out.println("??" + ((char) 7));
            return;
        }
        if (num < 0 || num >= debugMessages.length) {
            System.out.println("??" + ((char) 7));
            return;
        }
        lastMessageShown = num;
        System.out.println(debugMessages[num].getDebugMessage());
        repaint();
    }

    private void moveScreen(double factor) {
        long delta = (long) (0.5 + (displayEnd - displayStart) * factor);
        displayStart += delta;
        displayEnd += delta;
        repaint();
    }

    private void snapshot() {
        int height = getHeight();
        int width = getWidth();
        final BufferedImage snapshot
            = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = snapshot.createGraphics();
        g.setComposite(AlphaComposite.Src);
        paint(g);
        g.dispose();
        (new Thread() {
            public void run() {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(defaultDirectory);
                fc.setDialogTitle("Save .png snapshot");
                int ret = fc.showSaveDialog(ResultsGui.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    defaultDirectory = fc.getCurrentDirectory();
                    File file = fc.getSelectedFile();
                    System.out.println("Saving PNG to " + file + "...");
                    try {
                        boolean ok = ImageIO.write(snapshot, "PNG", file);
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
}


