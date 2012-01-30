
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

package com.hdcookbook.grin.io.text;

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SESegment;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.MosaicSpec;
import com.hdcookbook.grin.features.SEFixedImage;
import com.hdcookbook.grin.features.SEGroup;
import com.hdcookbook.grin.features.parts.SEImagePlacement;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.util.AssetFinder;

import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * The parser for a mosaics file.  A mosaics file is a show file that
 * only defines mosaics.  See ../../doc-files/index.html for the
 * BNF syntax.
 *
 *   @author     Bill Foote (http://jovial.com)
 **/
public class MosaicsParser {

    private SEShow show;
    private Lexer lexer;
    private ShowBuilder builder;

    /**
     * Create a mosaic parser to parse a mosaic definition file
     * at the given location.
     *
     * @param show      The show to populate.  This should be a new, empty
     *                  show.
     * @param lexer     The lexer to use
     * @param builder   The builder to use
     **/
    public MosaicsParser(SEShow show, Lexer lexer, ShowBuilder builder) {
        this.show = show;
        this.lexer = lexer;
        this.builder = builder;
    }

    /**
     * Parse a mosaics file, starting right after the "mosaics" keyword
     * was read.
     **/
    public void parse() throws IOException {
        show.setNoShowFile(true);
        for (;;) {
            String tok = lexer.getString();
            if ("mosaic".equals(tok)) {
                parseMosaic();
            } else if ("end_mosaics".equals(tok)) {
                break;
            } else {
                lexer.reportError("\"mosaic\" or \"end_mosaics\"  expected");
            }
        }
        builder.finishBuilding();
    }

    private void parseMosaic() throws IOException {
        String fileName = lexer.getString();
        MosaicSpec spec = null;
        try {
            spec = show.newMosaicSpec(fileName);
        } catch (IOException ex) {
            lexer.reportError(ex.getMessage());
        }
        ArrayList<String> files = new ArrayList<String>();
        ArrayList<String> filesToSkip = new ArrayList<String>();
        for (;;) {
            String tok = lexer.getString();
            if (";".equals(tok)) {
                break;
            } else if ("max_width".equals(tok)) {
                spec.maxWidth = lexer.getInt();
            } else if ("max_height".equals(tok)) {
                spec.maxHeight = lexer.getInt();
            } else if ("max_pixels".equals(tok)) {
                spec.maxPixels = lexer.getInt();
            } else if ("min_width".equals(tok)) {
                spec.minWidth = lexer.getInt();
            } else if ("num_widths".equals(tok)) {
                spec.numWidths = lexer.getInt();
            } else if ("take_all_images".equals(tok)) {
                spec.takeAllImages = lexer.getBoolean();
            } else if ("image_files".equals(tok)) {
                List<String> f = parseStrings();
                files.addAll(f);
            } else if ("add_image_files".equals(tok)) {
                List<String> f = parseStrings();
                for (String file : f) {
                    SEImagePlacement placement = new SEImagePlacement();
                    placement.setX(0);
                    placement.setY(0);
                    String name = null; // anonymous
                    SEFixedImage im 
                        = new SEFixedImage(show, name, placement, file);
                    builder.addFeature(name, lexer.getLineNumber(), im);
                }
                files.addAll(f);
            } else if ("skip_image_files".equals(tok)) {
                List<String> f = parseStrings();
                filesToSkip.addAll(f);
            } else {
                lexer.expectString("; or mosaic_part", tok);
            }
        }
        spec.imagesToConsider = files.toArray(new String[files.size()]);
        spec.imagesToSkip = filesToSkip.toArray(new String[filesToSkip.size()]);
    }

    private List<String> parseStrings() throws IOException {
        lexer.parseExpected("{");
        List<String> result = new ArrayList();
        for (;;) {
            String tok = lexer.getString();
            if (tok == null) {
                lexer.reportError("EOF unexpected in string list");
            } else if ("}".equals(tok)) {
                return result;
            } else {
                result.add(tok);
            }
        }
    }
}
