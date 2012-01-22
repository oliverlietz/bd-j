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

package com.hdcookbook.grin.binaryconverter;

import com.hdcookbook.grin.compiler.GrinCompiler;
import com.hdcookbook.grin.io.binary.*;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.io.text.ShowParser;
import com.hdcookbook.grin.util.AssetFinder;
import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

/**
 * A tool that converts a text based GRIN script to the binary format.
 **/
public class Main {

    private static int scaleX = 1000;   // X scale factor in mills
    private static int scaleY = 1000;   // Y scale factor in mills
    private static int offsetX = 0;     // X offset in pixels
    private static int offsetY = 0;     // Y offset in pixels
    private static boolean headless = true;
   
   /**
    * A driver method for the Main.convert(String, String).
    * 
    * @param args   Arguments. Requires the name of the text-based GRIN script to read.
    * @see          #convert(String[], File[], String[], ExtensionParser, String, boolean, boolean)
    **/
   public static void main(String[] args) {
       
        if (args == null || args.length == 0) {
            usage();
        }
        
        int index = 0;
        LinkedList<String> assetPathLL = new LinkedList<String>();
        LinkedList<String> assetDirsLL = new LinkedList<String>();
       
        LinkedList<String> showFilesLL = new LinkedList<String>();
        String extensionParserName = "com.hdcookbook.grin.io.text.NullExtensionParser";
        String outputDir = null;
        boolean debug = false;
        boolean optimize = true;
        
        while (index < args.length) {
            if ("-assets".equals(args[index])) {
                index++;
                String path = args[index];
                if (index == args.length) {
                    usage();
                }
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                assetPathLL.add(path);
            } else if ("-asset_dir".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                }                
                String path = args[index];
                assetDirsLL.add(path);
            } else if ("-debug".equals(args[index])){
                debug = true;
            } else if ("-show_mosaic".equals(args[index])){
                headless = false;
            } else if ("-extension_parser".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                }                 
                extensionParserName = args[index];
            } else if ("-out".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                } 
                outputDir = args[index];
            } else if ("-scaleX".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                } 
                scaleX = argToMills(args[index]);
            } else if ("-scaleY".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                } 
                scaleY = argToMills(args[index]);
            } else if ("-offsetX".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                } 
                offsetX = argToInt(args[index]);
            } else if ("-offsetY".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                } 
                offsetY = argToInt(args[index]);
            } else if ("-avoid_optimization".equals(args[index])) {
                optimize = false;
            } else if ("-optimize".equals(args[index])) {
                optimize = true;
            } else if ("".equals(args[index])) {
                // Skip it.  In ant, it's much easier to make a property blank
                // than it is to pass one argument fewer to a program.
            } else {
                showFilesLL.add(args[index]);
            }
            index++;
        }
        
        if (showFilesLL.isEmpty()) {
            usage();
        }
        if (headless) {
            System.setProperty("java.awt.headless", "true");
        }
        
        ExtensionParser extensionParser = null;
        
        if (extensionParserName != null && !"".equals(extensionParserName)) {
            try {
                 extensionParser = (ExtensionParser) 
                         Class.forName(extensionParserName).newInstance();
            } catch (IllegalAccessException ex) {
                 ex.printStackTrace();
            } catch (InstantiationException ex) {
                 ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                 ex.printStackTrace();
            } 
            if (extensionParser == null) {
                System.err.println();
                System.err.println("Error:  extension parser \"" 
                                   + extensionParserName + "\" not found.");
                System.err.println();
                System.exit(1);
            }
        }

        String[] assetPath = null;
        File[] assetDirs = null;
        if (assetDirsLL.size() > 0) {
            assetDirs = new File[assetDirsLL.size()];
            int i = 0;
            for (Iterator it = assetDirsLL.iterator(); it.hasNext(); ) {
                File f = new File((String) it.next());
                assetDirs[i++] = f;
            }
        } else {
            assetDirs = new File[0];
        }
        if (assetPathLL.isEmpty() && assetDirs.length == 0) {
            assetPath = new String[]{ "." }; // current dir
        } else {
            assetPath = assetPathLL.toArray(new String[assetPathLL.size()]);
        }
        String[] showFiles=showFilesLL.toArray(new String[showFilesLL.size()]);
        
        AssetFinder.setHelper(new AssetFinder() {
            protected void abortHelper() {
                System.exit(1);
            }
            
            protected Font getFontHelper(String fontName, int style, int size) {
                // On JavaSE, one cannot easily load a custom font.
                // The font created here will have a different glyph from what's 
                // expected for the xlet runtime, but it will hold the correct
                // fontName, style, and size.
                return new Font(fontName, style, size);
            }
        });
        AssetFinder.setSearchPath(assetPath, assetDirs);
        try {
           convert(assetPath, assetDirs, showFiles, 
                   extensionParser, outputDir, debug, optimize);
        } catch (Throwable e) {           
            e.printStackTrace();
            System.exit(1);
        }
        //System.exit(0); /// HDCOOKBOOK-220: This causes the negative value exit problem
   }

    private static int argToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            System.out.println();
            System.out.println(s + " is not an integer");
            System.out.println();
            usage();
            return -1;  // not reached
        }
    }

    private static int argToMills(String s) {
        try {
            Double d = Double.parseDouble(s);
            return (int) Math.round(d * 1000);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            System.out.println();
            System.out.println(s + " is not a double");
            System.out.println();
            usage();
            return -1;  // not reached
        }
    }

   /**
    * Converts the text based GRIN script to a binary format. 
    *
    * @param assets The path to the assets in a jarfile, which is used
    * as the first parameter to <code>AssetFinder.setSearchPath(String[], File[])</code>.
    * Could be null.
    * @param assetsDir  The path to the assets in the filesystem, which is 
    * used as the second parameter to  <code>AssetFinder.setSearchPath(String[], File[])</code>.
    * Could be null.
    * @param showFiles   The GRIN text script files to read in.  
    * @param extensionParser     The ExtensionParser for handling extensions.
    * @param outputDir  The directory to output generated files.
    * @param debug      If true, include debug information to generated
    * binary file.
    * @param optimize   If true, apply optimization to the show object,
    * such as creating image mosaics.
    */
   public static void convert(String[] assets, File[] assetsDir, 
                             String[] showFiles, 
                             ExtensionParser extensionParser, String outputDir,
                             boolean debug, boolean optimize) 
                   throws IOException 
    {
        if (outputDir == null) {
            outputDir = "."; // current dir
        }
        AssetFinder.setSearchPath(assets, assetsDir);
        List<File> files = new ArrayList<File>();
        SEShow[] shows = new SEShow[showFiles.length];
        try {   
            for (int i = 0; i < showFiles.length; i++) {
                ShowBuilder builder = new ShowBuilder();
                builder.setExtensionParser(extensionParser);         
                SEShow show = ShowParser.parseShow(showFiles[i], null, builder);
                shows[i] = show;
            }
            if (scaleX != 1000 || scaleY != 1000 
                || offsetX != 0 || offsetY != 0) 
            {
                for (int i = 0; i < shows.length; i++) {
                    shows[i].scaleBy(scaleX, scaleY, offsetX, offsetY);
                }
            }
            if (optimize) {
                GrinCompiler compiler = new GrinCompiler();
                compiler.setHeadless(headless);
                compiler.optimizeShows(shows, outputDir);
            }
            for (int i = 0; i < showFiles.length; i++) {
                if (!shows[i].getNoShowFile()) {
                    String baseName = showFiles[i];
                    if (baseName.indexOf('.') != -1) {
                        baseName = baseName.substring(0, 
                                                baseName.lastIndexOf('.'));
                    }
                    String fileName = shows[i].getBinaryGrinFileName();;
                    if (fileName == null) {
                        fileName = baseName + ".grin";
                    }
                    File f = new File(outputDir, fileName);
                    files.add(f);
                    DataOutputStream dos 
                        = new DataOutputStream(new FileOutputStream(f));
                    GrinBinaryWriter out 
                        = new GrinBinaryWriter(shows[i], debug);
                    out.writeShow(dos);
                    dos.close();

                    f = new File(outputDir, baseName + ".xlet.java");
                    files.add(f);
                    out.writeCommandClass(shows[i], true, f);

                    f = new File(outputDir, baseName + ".grinview.java");
                    files.add(f);
                    out.writeCommandClass(shows[i], false, f);
                }
            }
        } catch (IOException e) { 
            // failed on writing, delete the output files
            for (File file: files) {
                if (file != null && file.exists()) {
                   file.delete();
                }
            }
            throw e;
        }
    }
   
   private static void usage() {
        System.out.println("Error in tools argument.\n");
        
        System.out.println("");
        System.out.println("This tool lets you create a binary show file " +
                "from a text show file, with possible compile time optimizations.");
        System.out.println("");
        System.out.println("Usage: com.hdcookbook.grin.io.binary.Main <options> <show files>");
        System.out.println("");
        System.out.println("\t<show files> should be one or more text based "
                           + " show files availale in the assets search path.");
        System.out.println("");
        System.out.println("\t<options> can be:");
        System.out.println("\t\t-assets <directory>");
        System.out.println("\t\t-asset_dir <directory>");
        System.out.println("\t\t-extension_parser <a fully-qualified-classname>");
        System.out.println("\t\t-out <output_dir>");      
        System.out.println("\t\t-debug");
        System.out.println("\t\t-show_mosaic");
        System.out.println("\t\t-avoid_optimization");
        System.out.println("\t\t-optimize");
        System.out.println("\t\t-scaleX <double> -scaleY <double>");
        System.out.println("\t\t-offsetX <int> -offsetY <int>");
        System.out.println("");
        System.out.println("\t-assets and -asset_dir may be repeated to form a search path.");
        System.out.println("\t-avoid_otimization prevents the conversion process from using " +
                "GrinCompiler methods.");
        System.out.println("\t-optimize undoes an -avoid_optimization earlier on the command line.");
        System.out.println("\t-debug includes debugging information to the generated binary file.");
        System.out.println("\t-show_mosaic  creates a GUI to show mosaic building");

        System.exit(0);
   }
}
