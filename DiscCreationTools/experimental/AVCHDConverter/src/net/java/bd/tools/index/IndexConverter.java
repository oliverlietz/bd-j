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

package net.java.bd.tools.index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * This is an experimental wrapper class around the index tool,
 * which converts BD image's "index.bdmv" to AVCHD format's "INDEX.BDM" file.
 * 
 * The tool takes two arguments, an input and an output file name.  
 * It parses the input file as an index file, then
 * 1) updates the version string to "0100"
 * 2) inserts mysterious byte array to the ExtensionData area.
 * ... and outputs the content in the index.bdmv format to a file specified 
 * by the user.
 *
 * If the input index file contains ExtensionData, then it is overwritten.
 * Before the overwrite, the original content of the ExtensionData will be 
 * saved in a separate "extension.data" file as raw bytes.
 */
public class IndexConverter {
    
    static short[] AVCHD_EXTENSION_DATA = {
        0, 24, 0, 1, 4096, 256, 0, 24, 0, 382, 18756, 17752, 0, 0, 0, 354,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 310,
        4115, 1, 20054, 8244, 11833, 11831, 11826, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 6, -1, 16928, 1800, 2048, 21587, 144, 2644, 21024, 12334, 12334, 12590, 14336, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 2, 0,
        0, 2, 0, 1, 12336, 12336, 12289, 0, 0,
    };


    public static void main(String[] args) throws Exception {
        
        if (args == null || args.length < 2) {
            System.err.println("Missing arguments.");    
            return;
        }
        
        String input = args[0];
        String output = args[1];
        
        DataInputStream din = new DataInputStream(
                new BufferedInputStream(
                new FileInputStream(input)));
        
        IndexReader reader = new IndexReader();
        Index index = reader.readBinary(din);
        din.close();
        
        index.setVersion("0100");
        ExtensionData data = index.getExtensionData();
        if (data.data != null) {
            System.out.println("Warning: input file contains extension data,");           
            System.out.println("which will be overwritten in the generated file.");
            writeDataInShort(data.data);
        }
        data.setData(AVCHD_EXTENSION_DATA);
        
        DataOutputStream dout = new DataOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(output)));
        IndexWriter writer = new IndexWriter();
        writer.writeBinary(index, dout);
        dout.close();
 
        System.out.println("Generated " + output + " with a different version number"
                + " and additional extension data.");
    }
    
    static void writeDataInShort(short[] s) {
        String filename = "extension.data";
        try {
            System.out.println("Saving original data in the " + filename);
            DataOutputStream dout = new DataOutputStream(
                    new FileOutputStream(filename));
            
            for (int i = 0; i < s.length; i++) {
                dout.writeShort(s[i]);
            }
            
            dout.close();
            
            // Uncomment this section to get the content of the 
            // extension data that can be posted to AVCHD_EXTENSION_DATA
            // section above.
            /**
            DataInputStream din = new DataInputStream(
                    new FileInputStream(filename));
            int count = 0;
            while (true) {
                short s2;
                try { 
                   s2 = (short) din.readUnsignedShort();
                } catch (EOFException e) { break; }
                System.out.print(s2 + ",");   
                count++;
                if (count % 16 == 0) {
                   System.out.println();
                }
            }
             * **/
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }

}
