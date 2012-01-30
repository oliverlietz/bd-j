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

package com.hdcookbook.grin.io.xml;

import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinBinaryWriter;
import com.hdcookbook.grin.io.text.ShowParser;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.util.AssetFinder;
import com.hdcookbook.grin.compiler.GrinCompiler;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * A simple main class to convertAssembly GRIN show graph between various
 * file formats.
 *
 * @author A. Sundararajan
 */
public final class Main {
    private static final int FORMAT_TXT = 1;
    private static final int FORMAT_GRIN = 2;
    private static final int FORMAT_XML = 3;
    private static final int FORMAT_INVALID = -1;

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
        }

        int index = 0;
        List<String> assetPathLL = new ArrayList<String>();
        List<String> assetDirsLL = new ArrayList<String>();

        String extensionParserName = null;
        String outputDir = null;
        boolean debug = false;
        boolean optimize = true;
        int outFormat = FORMAT_GRIN;

        while (index < args.length) {
            if ("-format".equals(args[index])) {
                index++;
                String format = args[index];
                if (index == args.length) {
                    usage();
                }
                if (format.equals("xml")) {
                    outFormat = FORMAT_XML;
                } else if (format.equals("grin")) {
                    outFormat = FORMAT_GRIN;
                } else {
                    outFormat = FORMAT_INVALID;
                }
            } else if ("-assets".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                }
                String path = args[index];
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
            } else if ("-debug".equals(args[index])) {
                debug = true;
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
            } else if ("-avoid_optimization".equals(args[index])) {
                optimize = false;
            } else if (args[index].charAt(0) == '-') {
                usage();
            } else {
                break;
            }
            index++;
        }

        ExtensionParser extensionParser = null;

        if (extensionParserName != null && !"".equals(extensionParserName)) {
            try {
                extensionParser = (ExtensionParser) Class.forName(extensionParserName).newInstance();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        String[] assetPath = null;
        File[] assetDirs = new File[0];
        if (assetDirsLL.size() > 0) {
            assetDirs = new File[assetDirsLL.size()];
            int i = 0;
            for (String dir : assetDirsLL) {
                assetDirs[i++] = new File(dir);
            }
        }
        if (assetPathLL.isEmpty() && assetDirs.length == 0) {
            assetPath = new String[]{"."}; // current dir
            assetDirs = new File[]{new File(".")};
        } else {
            assetPath = (String[]) assetPathLL.toArray(new String[assetPathLL.size()]);
        }

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

        if (outputDir == null) {
            outputDir = ".";
        }
        AssetFinder.setSearchPath(assetPath, assetDirs);
        if (args.length - index < 1) {
            usage();
        }

        int inFormat = inferFormat(args[index]);
        if (inFormat == FORMAT_INVALID || outFormat == FORMAT_INVALID) {
            System.err.println("Unsupported input/output file format");
            System.exit(2);
        }

        if (inFormat == outFormat) {
            System.err.println("Nothing to convert! Same format!!");
            System.exit(2);
        }

        switch (inFormat) {
            case FORMAT_XML:
                if (outFormat == FORMAT_GRIN) {
                    convertXMLToBinary(args[index],
                            outputDir, debug, optimize);
                } else {
                    System.err.println("XML to text is not yet supported");
                    System.exit(2);
                }
                break;
            case FORMAT_TXT:
                if (outFormat == FORMAT_XML) {
                    convertTextToXML(args[index],
                            extensionParser, outputDir, debug, optimize);
                } else if (outFormat == FORMAT_GRIN) {
                    convertTextToBinary(args[index],
                            extensionParser, outputDir, debug, optimize);
                } else {
                    System.err.println("Unsupported output format");
                    System.exit(2);
                }
                break;
            case FORMAT_GRIN:
                System.err.println("Binary to text or XML is not yet supported");
                System.exit(2);
                break;
        }
    }

    private static int inferFormat(String fileName) {
        if (fileName.endsWith(".xml")) {
            return FORMAT_XML;
        } else if (fileName.endsWith(".txt")) {
            return FORMAT_TXT;
        } else if (fileName.endsWith(".grin")) {
            return FORMAT_GRIN;
        } else {
            return FORMAT_INVALID;
        }
    }

    private static void convertTextToXML(String inFile, ExtensionParser parser,
            String outputDir, boolean debug, boolean optimize) {
        ShowBuilder builder = new ShowBuilder();
        builder.setExtensionParser(parser);
        try {
            SEShow show = ShowParser.parseShow(inFile, null, builder);
            if (optimize) {
                new GrinCompiler().optimizeShows(new SEShow[] { show }, outputDir);
            }

            String baseName = inFile;
            if (baseName.indexOf('.') != -1) {
                baseName = baseName.substring(0, baseName.lastIndexOf('.'));
            }
            GrinBinaryWriter writer = new GrinBinaryWriter(show, debug);
            File tmpFile = File.createTempFile("grin", null);
            writer.writeShow(new DataOutputStream(new FileOutputStream(tmpFile)));

            File[] files = new File[2];
            files[0] = new File(outputDir, baseName + ".xlet.java");
            writer.writeCommandClass(show, true, files[0]);

            files[1] = new File(outputDir, baseName + ".grinview.java");
            writer.writeCommandClass(show, false, files[1]);

            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(new File(outputDir, baseName + ".xml")));
            Writer out = new OutputStreamWriter(stream);
            ShowXMLWriter gen = new ShowXMLWriter(out);
            gen.visitShow(show);
            out.flush();
        } catch (IOException ioexp) {
            System.err.println(ioexp);
            System.exit(3);
        }
    }

    private static void convertXMLToBinary(String inFile,
            String outputDir, boolean debug, boolean optimize) {
        
        try {
            URL u = AssetFinder.tryURL(inFile);
            InputStream stream;
            if (u != null) {
                stream = u.openStream();
            } else {
                stream = new FileInputStream(new File(inFile));
            }
            BufferedInputStream inStream = new BufferedInputStream(stream);
            Reader in = new InputStreamReader(inStream);
            ShowXMLReader grinReader = new ShowXMLReader(in);
            SEShow show = grinReader.readShow();
         
            if (optimize) {
                new GrinCompiler().optimizeShows(new SEShow[] { show }, outputDir);
            }

            String baseName = inFile;
            if (baseName.indexOf('.') != -1) {
                baseName = baseName.substring(0, baseName.lastIndexOf('.'));
            }
            GrinBinaryWriter writer = new GrinBinaryWriter(show, debug);
            BufferedOutputStream outStream = new BufferedOutputStream(
                    new FileOutputStream(new File(outputDir, baseName + ".grin")));
            writer.writeShow(new DataOutputStream(outStream));
            File[] files = new File[2];
            files[0] = new File(outputDir, baseName + ".xlet.java");
            writer.writeCommandClass(show, true, files[0]);

            files[1] = new File(outputDir, baseName + ".grinview.java");
            writer.writeCommandClass(show, false, files[1]);
            outStream.flush();
        } catch (IOException ioexp) {
            System.err.println(ioexp);
            System.exit(3);
        }
    }
        

    private static void convertTextToBinary(String showFile,
            ExtensionParser extensionParser, String outputDir,
            boolean debug, boolean optimize) {
        String baseName = showFile;
        if (baseName.indexOf('.') != -1) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }

        File[] files = new File[3];
        try {
            ShowBuilder builder = new ShowBuilder();
            builder.setExtensionParser(extensionParser);

            SEShow show = ShowParser.parseShow(showFile, null, builder);
            if (optimize) {
                new GrinCompiler().optimizeShows(new SEShow[] { show }, outputDir);
            }

            String fileName = baseName + ".grin";
            files[0] = new File(outputDir, fileName);

            DataOutputStream dos = new DataOutputStream(new FileOutputStream(files[0]));
            GrinBinaryWriter out = new GrinBinaryWriter(show, debug);
            out.writeShow(dos);

            files[1] = new File(outputDir, baseName + ".xlet.java");
            out.writeCommandClass(show, true, files[1]);

            files[2] = new File(outputDir, baseName + ".grinview.java");
            out.writeCommandClass(show, false, files[2]);

            return;

        } catch (IOException e) {
            // failed on writing, delete the binary file
            for (File file : files) {
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
            System.err.println(e);
            System.exit(3);
        }
    }

    private static void usage() {
        System.out.println("Error in tools argument.\n");

        System.out.println("");
        System.out.println("This tool converts GRIN show between different " +
                "formats.");
        System.out.println("");
        System.out.println("Usage: java " + Main.class.getName() +
                " <options> <.txt or .xml file>");
        System.out.println("");
        System.out.println("\t<options> can be:");
        System.out.println("\t\t-assets <directory>");
        System.out.println("\t\t-asset_dir <directory>");
        System.out.println("\t\t-extension_parser <a fully-qualified-classname>");
        System.out.println("\t\t-format <bin|xml>");
        System.out.println("\t\t-out <output_dir>");
        System.out.println("\t\t-debug");
        System.out.println("\t\t-avoid_optimization");
        System.out.println("");
        System.out.println("\t-assets and -asset_dir may be repeated to form a search path.");
        System.out.println("\t-avoid_otimization prevents the conversion process from using " +
                "GrinCompiler methods.");
        System.out.println("\t-debug includes debugging information to the generated binary file.");


        System.exit(1);
    }
}

