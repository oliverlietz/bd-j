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

package com.hdcookbook.grin.fontstrip;

import com.hdcookbook.grin.util.AssetFinder;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

public class Main {

    private static double scaleX = 1.0;
    private static double scaleY = 1.0;
    
   public static void main(String[] args) {
       
        if (args == null || args.length == 0) {
            usage();
        }
        
        int index = 0;
        LinkedList<String> assetDirsLL = new LinkedList<String>();
       
        String configFile = null;
        String outputDir  = null;
        boolean designOnly = false;
        
        while (index < args.length) {
            if ("-asset_dir".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                }                
                String path = args[index];
                assetDirsLL.add(path);
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
                scaleX = argToDouble(args[index]);
            } else if ("-scaleY".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                } 
                scaleY = argToDouble(args[index]);
            } else if ("-config".equals(args[index])) {
                index++;
                if (index == args.length) {
                    usage();
                }  
                configFile = args[index];
            } else if ("-design_only".equals(args[index])) {
                designOnly = true;
            } else {
                usage();
            }
                
            index++;
        }
        
        if (configFile == null) {
            System.err.println("Need a configuration file to use the tool.");
            usage();
        }
       
        File[] assetDirs = null;
        if (assetDirsLL.size() > 0) {
            assetDirs = new File[assetDirsLL.size()];
            int i = 0;
            for (Iterator it = assetDirsLL.iterator(); it.hasNext(); ) {
                File f = new File((String) it.next());
                assetDirs[i++] = f;
            }
        }
        if (assetDirs == null || assetDirs.length == 0) {
            assetDirs = new File[]{ new File(".") }; // current dir
        } 
        
        AssetFinder.setSearchPath(null, assetDirs);
        
        FontStripImageGenerator generator 
                = new FontStripImageGenerator(configFile, scaleX, scaleY,
                                              assetDirs, outputDir);
        
        try {
            generator.generateImages(designOnly);
        } catch (Throwable e) {           
            e.printStackTrace();
            System.exit(1);
        }
        
        System.exit(0);
   }

    private static double argToDouble(String arg) {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            usage();
            return -1;  // not reached
        }
    }
    
   
   private static void usage() {
        System.out.println("Error in tools argument.\n");
        
        System.out.println("");
        System.out.println("This tool lets you generate images from fonts " +
                "which one can use at grin runtime with the fontstrip_text extension feature." +
                "By default, both intermediate font images for editing and final runtime images are generated.");
        System.out.println("");
        System.out.println("Usage: com.hdcookbook.grin.fontstrip.Main <options>");
        System.out.println("");
        System.out.println("\t<options> can be:");
        System.out.println("\t\t-asset_dir <directory>");
        System.out.println("\t\t-config <a configuration file name>");
        System.out.println("\t\t-out <output_dir>");     
        System.out.println("\t\t-design_only");   
        System.out.println("\t\t-scaleX <double> -scaleY <double>");   
        System.out.println("");
        System.out.println("\t-asset_dir may be repeated to form a search path.");
        System.out.println("\tWith -design_only argument, the tool will only generate" +
                "intermediate font images for editing and not the final fontstrip images for runtime.");
        
        System.exit(1);
   }
}
