
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

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.io.builders.BackgroundSpec;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.features.Assembly;
import com.hdcookbook.grin.util.AssetFinder;

import com.hdcookbook.grin.io.text.ExtensionParser;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.IdentityHashMap;

import javax.swing.SwingUtilities;

/**
 * This is like GenericMain, and also includes a GUI to browse the
 * Show file and control show execution.  The GUI is equivalent to the
 * command line interface provided by GenericMain.
 *
 * @author Bill Foote (http://jovial.com)
 */
public class GrinView extends GenericMain {

    private IdentityHashMap lineNumberMap = new IdentityHashMap();
    private GrinViewScreen screen;
            
     // Possible screen sizes supported.
    static final DeviceConfig VGA = new DeviceConfig(640, 480);
    static final DeviceConfig NTSC = new DeviceConfig(720, 480);
    static final DeviceConfig PAL  = new DeviceConfig(720, 576);
    static final DeviceConfig P720 = new DeviceConfig(1280, 720);
    static final DeviceConfig QHD  = new DeviceConfig(960, 540);
    static final DeviceConfig FOURTHREE = new DeviceConfig(1440, 1080);
    static final DeviceConfig FULLHD = new DeviceConfig(1920, 1080);  

    static boolean doInputLoop = false; 
        // Can be set false externally, e.g. by GrinViewJar
        
    public GrinView() {
    }
    public GrinView(String grinxlet) {
        super(grinxlet);
    }

    private void buildControlGUI(String showName, boolean isBinary) {
        screen = new GrinViewScreen(this, new ShowNode(show, showName));
        screen.setNameText("GRIN show viewer:  " + showName);
        screen.setResultText("Double-click in the tree to activate a segment.");

        try {
            String[] lines = readShowFile(showName, isBinary);
            screen.setShowText(lines);            
        } catch (IOException ex) {
            System.out.println();
            System.out.println("Error reading show:  " + ex);
            System.out.println();
            System.exit(1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                screen.pack();
                int x = 0;
                int y = getHeight() + getInsets().top;
                screen.setLocation(x, y);
                screen.setVisible(true);
                screen.setFpsText("" + getFps());
                screen.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        exitGrinview();
                    }
                });
            }
        });
    }
    
    private String[] readShowFile(String showName, boolean isBinary) 
                throws IOException 
    {
        String[] lines;
        if (isBinary) {
            lines = readShowBinary(showName);
        } else {
            lines = readShowText(showName);
        }
        
        return lines;
    }

    private String[] readShowText(String showName) throws IOException {
        URL source = AssetFinder.getURL(showName);
        if (source == null) {
            throw new IOException("Can't find resource " + showName);
        }
        BufferedReader rdr 
            = new BufferedReader(
                    new InputStreamReader(source.openStream(), "UTF-8"));
        LinkedList lines = new LinkedList();
        for (int lineNo = 1; ; lineNo++) {
            String line = rdr.readLine();
            if (line == null) {
                break;
            }
            String num;
            if (lineNo < 10) {
                num = "    ";
            } else if (lineNo < 100) {
                num = "   ";
            } else if (lineNo < 1000) {
                num = "  ";
            } else if (lineNo < 10000) {
                num = " ";
            } else {
                num = "";
            }
            num = num + lineNo + ":   ";
            lines.add(num + line);
        }
        rdr.close();
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    void addLineNumber(Object obj, int line) {
        lineNumberMap.put(obj, new Integer(line));
    }
    
    private String[] readShowBinary(String showName) throws IOException {
        URL source = AssetFinder.getURL(showName);
        if (source == null) {
            throw new IOException("Can't find resource " + showName);
        }
        BufferedInputStream bis = new BufferedInputStream(source.openStream());
        LinkedList lines = new LinkedList();
        int ch;
        int count = 0;
        StringBuffer hexInts = new StringBuffer(); // Hex integer for each line
        StringBuffer content = new StringBuffer(); // hexInt in either char or '.' 
        for(;;) {
            ch = bis.read();
            int m = count % 16;
            if (m == 0) {
                if (ch == -1) {
                    break;
                }
                hexInts.append(toHex(count, 8) + ":  ");
            }
            if (m == 8) {
                hexInts.append(" ");
            }
            if (ch == -1) {
                hexInts.append("  ");
            } else {
                hexInts.append(toHex(ch, 2));
                if (ch >= 32 && ch < 127) {
                    content.append((char) ch);
                } else {
                    content.append('.');
                }
            }
            if (m == 15)  {
                hexInts.append("   ");
                hexInts.append(content);
                lines.add(hexInts.toString());
                hexInts = hexInts.delete(0, hexInts.length());
                content = content.delete(0, content.length());
            } else {
                hexInts.append(" ");
            }
            count++;
        }
              
        bis.close();
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    private static String hexDigits = "0123456789abcdef";
    private static String toHex(int b, int digits) {
        if (digits <= 0) {
            throw new IllegalArgumentException();
        }
        String result = "";
        while (digits > 0 || b > 0) {
            result = hexDigits.charAt(b % 16) + result;
            b = b / 16;
            digits--;
        }
        return result;
    }
    
    int getLineNumber(Object o) {
        ShowNode node = (ShowNode) o;
        Object v = lineNumberMap.get(node.getContents());
        if (v == null) {
            return -1;
        } else {
            return ((Integer) v).intValue();
        }
    }

    //
    // Handles an "invoke" (a double-click) on a show node.  Returns
    // a string describing the action performed.
    //
    String invokeShowNode(Object[] path) {
        Feature part = null;
        for (int i = path.length - 1; i >= 0; i--) {
            ShowNode node = (ShowNode) path[i];
            if (node.getContents() instanceof Segment) {
                Segment s = (Segment) node.getContents();
                show.activateSegment(s);
                return "Activated segment " + s.getName() + ".";
            } else if (node.getContents() instanceof Assembly) {
                if (part != null) {
                    Assembly a = (Assembly) node.getContents();
                    a.setCurrentFeature(part);
                    return "Activated part " + part.getName()
                           + " in assembly " + a.getName() + ".";
                }
                part = (Feature) node.getContents();
            } else if (node.getContents() instanceof Feature) {
                part = (Feature) node.getContents();
            } else if (node.getContents() instanceof BackgroundSpec) {
                String name = ((BackgroundSpec) node.getContents()).imageName;
                if (name == null || "".equals(name)) {
                    setBackground((URL) null);
                    return "Selected background image \"\"";
                } else {
                    URL url = AssetFinder.tryURL(name);
                    if (url == null) {
                        return "Can't find " + name;
                    }
                    setBackground(url);
                    return "Selected background image " + name;
                }
            } else {
                part = null;
            }
        }
        return null;
    }

    protected String setFps(float newFps) {
        String result = super.setFps(newFps);
        if (screen != null) {
            screen.setFpsText("" + newFps);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     **/
    protected void waitForUser(String msg) {
        float fps = getFps();
        if (fps <= 0f) {
                // Make sure that there's no race condition where a button
                // press happens after we set the button visible but before
                // we get down into doWaitForUser
            synchronized(debugWaitingMonitor) {
                screen.forceNextDrawButtonVisible(true);
                screen.setResultText(msg);
                doWaitForUser();
            }
        } else {
            // If we aren't stopped, we pause 1/4 frame.
            long ms = (long) ((0.25 / fps) * 1000 + 0.5);
            if (ms > 0) {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     **/
    public void debugDrawFrameDone() {
        if (getFps() == 0f) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    advanceFrames(1);
                }
            });
        }
    }
    
    /**
     * Supported synonyms are
     *     720x480, 480, 480i, 480p, ntsc
     *     720x576, 576, 576i, 576p, pal, secam
     *     1280x720, 720, 720p
     *     960x540, 960, qhd
     *     1440x1080, 1440
     *     1920x1080, 1080, 1080p, 1080i, fullhd 
     * @param arg one of the element in the set.
     * @return 480, 576, 720, 960, 960, or 1080, to discribe the screen size.
     */
    private static DeviceConfig getDeviceConfig(String arg) {
        if ("720x480".equals(arg) || "480".equals(arg) || 
            "480i".equals(arg) || "ntsc".equalsIgnoreCase(arg)) {
            return NTSC;
        } else if ("720x576".equals(arg) || "576".equals(arg) ||
            "576p".equals(arg) || "pal".equalsIgnoreCase(arg) ||
            "secam".equalsIgnoreCase(arg)) {
            return PAL;
        } else if ("1280x720".equals(arg) || "720".equals(arg) ||
            "720p".equals(arg)) {
            return P720;
        } else if ("960x540".equals(arg) || "960".equals(arg) ||
            "qhd".equalsIgnoreCase(arg)) {
            return QHD;
        } else if ("1440x1080".equals(arg) || "1440".equals(arg)) {
            return FOURTHREE;
        } else if ("1920x1080".equals(arg) || "1080".equals(arg) ||
            "1080p".equals(arg) || "1080i".equals(arg) || 
            "fullhd".equalsIgnoreCase(arg)) {
            return FULLHD;
        } else if ("640x480".equals(arg) || "vga".equals(arg)) {
            return VGA;
        }
        
        // Nothing matched... print usage and exit.
        usage();
        return null; // Never reaches here
    }
    
    private static void usage() {
        System.out.println();
        System.out.println("Usage:  java com.hdcookbook.grin.test.bigjdk.GrinView <option> <show file>\\");
        System.out.println("            <show file> can be a .grin binary file, or a text show file.");
        System.out.println("                        It is searched for in the asset search path.");

        System.out.println("            <options> can be:");
        System.out.println("                -fps <number>");
        System.out.println("                -assets <asset path in jar file>");
        System.out.println("                -asset_dir <directory in filesystem>");
        System.out.println("                -imagemap <mapfile>");
        System.out.println("                -background <image>");        
        System.out.println("                -screensize <keyword>");
        System.out.println("                -automate");
        System.out.println("                -noui");
        System.out.println("                -scale <number>");
        System.out.println("                -segment <segment name to activate>");      
        System.out.println("                -extension_parser <a fully qualified classname>");       
        System.out.println("                -director <a fully qualified classname>");
        System.out.println("                -grinxlet <a fully qualified classname>");
        System.out.println("                -binary");
        System.out.println("                -stdin_commands");
        System.out.println("");
        System.out.println("            -assets and -asset_dir may be repeated to form a search path.");
        System.out.println("            -screensize keyword can be fullhd, pal, ntsc, 720p, vga, or 960x540.");
        System.out.println("                        Default screen size is fullhd (1920x1080), with a scale ");
        System.out.println("                        factor 2.  Some synonymous names are also allowed.");
        System.out.println("            -automate option will launch a testing thread which activates");       
        System.out.println("                      show segments in order.");
        System.out.println("            -noui option will suppress the GrinView UI, which is good for profiling");       
        System.out.println("            every named segment in a given show with an 1 second interval.");
        System.out.println("            -extension_parser is needed when the show is a text file and");
        System.out.println("                              uses a custom feature or command subclass.");
        System.out.println("            -binary tells GrinView to read a binary .grin file.");
        System.out.println("            -director tells GrinView to instantiate the given class as Direcor.");
        System.out.println("            -grinxlet tells GrinView to instantiate the given class instead of the default GrinXlet class.");
        System.out.println();
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            String java_version = System.getProperty("java.version");
            String os_name = System.getProperty("os.name");
            String os_arch = System.getProperty("os.arch");
                
            System.out.println("Java version: " + java_version);
            System.out.println("O/S name: " + os_name);
            System.out.println("O/S architecture: " + os_arch);
                
            if (java_version.startsWith("1.5.")
                && os_name.startsWith("Mac")
                && (os_arch.startsWith("i386") || os_arch.startsWith("x86")))
            {
                System.setProperty("apple.awt.graphics.UseQuartz", "false");

                System.out.println();
                System.out.println("NOTE:  Java 5 on Mac/Intel detected.  Due to known issues, Quartz is being");
                System.out.println("       disabled.  You may wish to switch to Java 6 to get better performance.");
                System.out.println("       You can set your default java runtime from Applications -> Utilities");
                System.out.println("       -> Java Preferences.");
                System.out.println();
                System.out.println("       See also https://hdcookbook.dev.java.net/issues/show_bug.cgi?id=183");
                System.out.println();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        LinkedList assetPathLL = new LinkedList();
        LinkedList assetDirsLL = new LinkedList();
        String imageMap = null;
        String background = null;
        int argsUsed = 0;
        String fps = null;
        String segment = null;
        String scaleDivisor = null;
        DeviceConfig deviceConfig = null;
        String extensionParserName = null;
        boolean isBinary = false;
        boolean noUI = false;
        String director = null;
        boolean doAutoTest = false;
        String grinxlet = null;
        while (argsUsed < args.length - 1) {
            if ("-fps".equals(args[argsUsed])) {
                argsUsed++;
                if (fps != null) {
                    usage();
                }
                fps = args[argsUsed];
                argsUsed++;
            } else if ("-background".equals(args[argsUsed])) {
                argsUsed++;
                if (background != null) {
                    usage();
                }
                background = args[argsUsed];
                argsUsed++;
            } else if ("-assets".equals(args[argsUsed])) {
                argsUsed++;
                String path = args[argsUsed];
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                assetPathLL.add(path);
                argsUsed++;
            } else if ("-asset_dir".equals(args[argsUsed])) {
                argsUsed++;
                String path = args[argsUsed];
                assetDirsLL.add(path);
                argsUsed++;
            } else if ("-imagemap".equals(args[argsUsed])) {
                if (imageMap != null) {
                    usage();
                }
                argsUsed++;
                imageMap = args[argsUsed];
                argsUsed++;
            } else if ("-segment".equals(args[argsUsed])) {
                argsUsed++;
                if (segment != null) {
                        usage();
                }
                segment = args[argsUsed];
                argsUsed++;
            } else if ("-scale".equals(args[argsUsed])) {
                argsUsed++;
                if (scaleDivisor != null) {
                    usage();
                }
                scaleDivisor = args[argsUsed];
                argsUsed++;     
            } else if ("-screensize".equals(args[argsUsed])) {
                argsUsed++;
                if (deviceConfig != null) {
                    usage();
                }
                deviceConfig = getDeviceConfig(args[argsUsed]);
                argsUsed++;                
            } else if ("-extension_parser".equals(args[argsUsed])) {
                if (extensionParserName != null) {
                    usage();
                }
                argsUsed++;
                extensionParserName = args[argsUsed];
                argsUsed++; 
            } else if ("-director".equals(args[argsUsed])) {
                if (director != null) {
                    usage();
                }
                argsUsed++;
                director = args[argsUsed];
                argsUsed++;
            } else if ("-grinxlet".equals(args[argsUsed])) {
                if (grinxlet != null) {
                    usage();
                }
                argsUsed++;
                grinxlet = args[argsUsed];
                argsUsed++;
            } else if ("-binary".equals(args[argsUsed])) {
                isBinary = true;
                argsUsed++;
            } else if ("-automate".equals(args[argsUsed])) {
                doAutoTest = true;
                argsUsed++;
            } else if ("-stdin_commands".equals(args[argsUsed])) {
                doInputLoop = true;
                argsUsed++;
            } else if ("-noui".equals(args[argsUsed])) {
                noUI = true;
                argsUsed++;
            } else {
                break;
            }
        }
        if (argsUsed+1 != args.length) {
            usage();
        }
        String showFile = args[argsUsed++];
        String[] assetPath = null;
        File[] assetDirs = null;
        if (assetDirsLL.size() > 0) {
            assetDirs = new File[assetDirsLL.size()];
            int i = 0;
            for (Iterator it = assetDirsLL.iterator(); it.hasNext(); ) {
                File f = new File((String) it.next());
                assetDirs[i++] = f;
            }
        }
        if (assetPathLL.size() == 0) {
            if (assetDirs == null) {
                assetPath = new String[] { "../test/assets/" };
            }
        } else {
            assetPath = (String[]) 
                        assetPathLL.toArray(new String[assetPathLL.size()]);
        }
        AssetFinder.setHelper(new AssetFinder() {
            protected void abortHelper() {
                System.exit(1);
            }
        });
        AssetFinder.setSearchPath(assetPath, assetDirs);
        if (imageMap != null) {
            AssetFinder.setImageMap(imageMap);
        }

        GrinView m;
        if (grinxlet == null) {
            m = new GrinView();
        } else {
            m = new GrinView(grinxlet);
        }
        if (director != null) {
            m.setDirectorClassName(director);
        }
        
        if (scaleDivisor != null || deviceConfig != null) {
            m.adjustScreenSize(scaleDivisor, deviceConfig);
        }
        
        ExtensionParser reader = null;
        if (extensionParserName != null && !("".equals(extensionParserName))) {
            try {
                reader = (ExtensionParser)
                        Class.forName(extensionParserName).newInstance();
            } catch (Exception e) {
                System.err.println("Error instantiating " + extensionParserName);
                e.printStackTrace();
            }
        }
        
        ShowBuilder builder;
        if (noUI) {
            builder = new ShowBuilder();
        } else {
            builder = new GuiShowBuilder(m);
        }
        builder.setExtensionParser(reader);
        m.init(showFile, isBinary, builder, segment, doAutoTest);

        if (!noUI) {
            m.buildControlGUI(showFile, isBinary);
        }
        m.startEngine();

        if (background != null) {
            try {
                m.setBackground(new File(background).toURI().toURL());
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        if (fps != null) {
            m.doKeyboardCommand("f " + fps); // set fps  
        }

        if (doInputLoop) {
            m.inputLoop();
            System.exit(0);
        }
    }

    public void setDebugDraw(boolean value) {
        super.setDebugDraw(value);
        if (screen != null) {
            screen.setDebugDrawToggle(value);
        }
    }
    
}
