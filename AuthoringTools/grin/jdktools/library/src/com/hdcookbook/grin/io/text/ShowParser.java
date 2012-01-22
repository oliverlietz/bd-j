
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

package com.hdcookbook.grin.io.text;

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowCommand;
import com.hdcookbook.grin.SEShowCommands;
import com.hdcookbook.grin.Director;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.MosaicSpec;
import com.hdcookbook.grin.SESegment;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.commands.SEActivateSegmentCommand;
import com.hdcookbook.grin.commands.SEActivatePartCommand;
import com.hdcookbook.grin.commands.SECommandList;
import com.hdcookbook.grin.commands.SEResetFeatureCommand;
import com.hdcookbook.grin.commands.SERunNamedCommand;
import com.hdcookbook.grin.commands.SESegmentDoneCommand;
import com.hdcookbook.grin.commands.SESetVisualRCStateCommand;
import com.hdcookbook.grin.commands.SESyncDisplayCommand;
import com.hdcookbook.grin.features.Assembly;
import com.hdcookbook.grin.features.ImageSequence;
import com.hdcookbook.grin.features.InterpolatedModel;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.features.SEAssembly;
import com.hdcookbook.grin.features.SEBox;
import com.hdcookbook.grin.features.SEClipped;
import com.hdcookbook.grin.features.SEFade;
import com.hdcookbook.grin.features.SEFixedImage;
import com.hdcookbook.grin.features.SEGroup;
import com.hdcookbook.grin.features.SEGuaranteeFill;
import com.hdcookbook.grin.features.SEImageSequence;
import com.hdcookbook.grin.features.SEMenuAssembly;
import com.hdcookbook.grin.features.SESetTarget;
import com.hdcookbook.grin.features.SESrcOver;
import com.hdcookbook.grin.features.SEText;
import com.hdcookbook.grin.features.SETranslator;
import com.hdcookbook.grin.features.SETranslatorModel;
import com.hdcookbook.grin.features.Translator;
import com.hdcookbook.grin.features.parts.EasingEquation;
import com.hdcookbook.grin.features.parts.PointsEasingEquation;
import com.hdcookbook.grin.features.parts.SEImagePlacement;
import com.hdcookbook.grin.features.parts.SEImagePlacementList;
import com.hdcookbook.grin.features.parts.SEImageSeqPlacement;
import com.hdcookbook.grin.input.RCKeyEvent;
import com.hdcookbook.grin.input.VisualRCHandler;
import com.hdcookbook.grin.input.RCHandler;
import com.hdcookbook.grin.input.SECommandRCHandler;
import com.hdcookbook.grin.input.SEVisualRCHandler;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.builders.BackgroundSpec;
import com.hdcookbook.grin.io.builders.FontSpec;
import com.hdcookbook.grin.io.builders.MenuAssemblyHelper;
import com.hdcookbook.grin.io.builders.TranslatorHelper;
import com.hdcookbook.grin.io.builders.VisualRCHandlerHelper;
import com.hdcookbook.grin.io.builders.VisualRCHandlerCell;
import com.hdcookbook.grin.util.AssetFinder;
import com.robertpenner.PennerEasing;

import java.io.Reader;
import java.io.IOException;
import java.awt.Font;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import java.util.Vector;

/**
 * The parser of a show file.  This is a really simple-minded
 * parser.  For example, all tokens are just strings, so, for example,
 * you have to write "( 0 3 )" and not "(0 3)", since the first has four
 * tokens and the second only two.
 *
 *   @author     Bill Foote (http://jovial.com)
 **/
public class ShowParser {

    private SEShow show;
    private Lexer lexer;
    private ExtensionParser extParser;
    private ArrayList<ArrayList<ForwardReference>> deferred;
    private static final int NUM_DEFERRED_LISTS = 4;    // We use 4 slots
    private Map<String, VisualRCHandlerHelper> visualRCHelpers
        = new HashMap<String, VisualRCHandlerHelper>();

    /** 
     * A useful constant for parsing:  An empty command array
     **/
    public final static Command[] emptyCommandArray = new Command[0];

    private ShowBuilder builder;

    /**
     * Create a parser to parse a show at the given location.
     *
     * @param reader    Where to read the show from.  We read it up to the
     *                  end_show token.  It is recommended to be a 
     *                  BufferedReader instance for a performance improvement.
     *
     * @param showName  The name of the show, for error messages.
     *
     * @param show      The show to populate.  This should be a new, empty
     *                  show.
     **/
    public ShowParser(Reader reader, String showName, SEShow show) {
        this(reader, showName, show, null);
    }

    /**
     * Create a parser to parse a show at the given location.
     *
     * @param reader    Where to read the show from.  We read it up to the
     *                  end_show token. It is recommended to be a BufferedReader
     *                  instance for a performance improvement.
     *
     * @param showName  The name of the show, for error messages.
     *
     * @param show      The show to populate.  This should be a new, empty
     *                  show.
     *
     * @param builder   A helper to build the tree.  You can use something
     *                  other than the default to add decorations to the
     *                  tree, e.g. for debugging.
     **/
    public ShowParser(Reader reader, String showName, SEShow show, 
                      ShowBuilder builder) 
    {
        deferred = new ArrayList();
        for (int i = 0; i < NUM_DEFERRED_LISTS; i++) {
            deferred.add(new ArrayList());
        }
        this.show = show;
        Director d = show.getDirector();
        this.lexer = new Lexer(reader, showName, this);
        
        if (builder == null) {
            builder = new ShowBuilder();
        }
        
        this.builder = builder;
        builder.init(show);
        
        this.extParser = builder.getExtensionParser();
    }

    /**
     * Get the builder used as a helper to build the show.  This can be useful
     * for extension parsers, and was added in response to
     * http://forums.java.net/jive/post!reply.jspa?messageID=477501 .  There,
     * an extension parser wanted to call getFontIndex(FontSpec) for a rich
     * text extension feature, so that it could use the pool of fonts in a
     * .grin file.  
     **/
    public ShowBuilder getBuilder() {
        return builder;
    }

    /**
     * Adds a forward reference to a show feature.  The forward reference
     * will be resolved after the entire show has been read.  This is
     * useful from an extension parser for looking up other parts of a show, 
     * like a named feature.  You can use it like this:
     * <pre>
     *
     *     Lexer lexer = ...;
     *     final ShowParser parser = lexer.getParser();
     *     final MyFeature feature = ...;
     *     final String otherFeatureName = ...;
     *     ForwardReference fw = new ForwardReference(lexer) {
     *         public void resolve() throws IOException {
     *             Feature f = parser.lookupFeatureOrFail(otherFeatureName);
     *             feature.setOtherFeature(f);
     *         }
     *     };
     *     parser.addForwardReference(fw, 0);
     *
     * </pre>
     *
     * The GRIN parser guarantees that all of its forward reference resolution
     * will be copleted before the first forward reference added by this
     * method.  For example, all groups and assemblies will be completely
     * populated before any of your forward references are.
     * <p>
     * Within your forward references, you might need to make sure that
     * some of them are resolved before others.  Within GRIN, and example
     * of this is the visual RC handler, which depends on the assembly it's
     * bound to being completely resolved before the RC handler's references
     * are resolved.  If you need to impose an ordering on the resolution
     * order of different kinds of forward references, use the rank parameter.
     *
     *   @param fw      The forward reference to add
     *   @param rank    The rank order.  Higher numbered forward references
     *                  are processed after lower numbered ones.  Internally,
     *                  the parser uses this as an array index.  Must be >= 0.
     **/
    public void addForwardReference(ForwardReference fw, int rank) {
        while (deferred.size() <= rank + NUM_DEFERRED_LISTS) {
            deferred.add(new ArrayList());
        }
        deferred.get(rank + NUM_DEFERRED_LISTS).add(fw);
    }

    /**
     * Parse the current show file.
     **/
    public void parse() throws IOException {
        String tok = lexer.getString();
        if ("mosaics".equals(tok)) {
            MosaicsParser p = new MosaicsParser(show, lexer, builder);
            p.parse();
            return;
        }
        if (!"show".equals(tok)) {
            lexer.reportError("\"show\" expected");
        }
        show.setDrawTargets(new String[] { "T:Default" });      // default value

            // Parse the settings
        for (;;) {
            tok = lexer.getString();
            int lineStart = lexer.getLineNumber();
            if (!("setting".equals(tok))) {
                break;
            }
            tok = lexer.getString();
            if ("segment_stack_depth".equals(tok)) {
                parseSegmentStackDepth();
            } else if ("draw_targets".equals(tok)) {
                parseDrawTargets();
            } else if ("sticky_images".equals(tok)) {
                parseStickyImages();
            } else if ("binary_grin_file".equals(tok)) {
                parseBinaryGrinFile();
            } else if ("grinview_background".equals(tok)) {
                parseGrinviewBackground();
            } else {
                lexer.reportError("Unrecognized setting \"" + tok + "\".");
            }
        }

            // Parse the exports clause
        if ("exports".equals(tok)) {
            parseExpected("segments");
            String[] publicSegments = parseStrings();
            parseExpected("features");
            String[] publicFeatures = parseStrings();
            parseExpected("handlers");
            String[] publicHandlers = parseStrings();
            tok = lexer.getString();
            String[] publicNamedCommands;
            if ("named_commands".equals(tok)) {
                publicNamedCommands = parseStrings();
                tok = lexer.getString();
            } else {
                publicNamedCommands = new String[0];
            }
            lexer.expectString(";", tok);
            builder.setExported(publicSegments, publicFeatures, publicHandlers,
                                publicNamedCommands);
            tok = lexer.getString();
        }

        if ("java_generated_class".equals(tok)) {
            String className = lexer.getString();
            parseExpected("[[");
            StringBuffer xletClassBody = new StringBuffer();
            StringBuffer grinviewClassBody = new StringBuffer();
            StringBuffer originalSource = new StringBuffer();
            originalSource.append("java_generated_class");
            originalSource.append(" ");
            originalSource.append(className);
            originalSource.append(" [[\n");
            readJavaSource(xletClassBody, grinviewClassBody, originalSource, null);
            SEShowCommands cmds = show.getShowCommands();
            cmds.setClassName(className);
            cmds.setXletClassBody(xletClassBody.toString());
            cmds.setGrinviewClassBody(grinviewClassBody.toString());
            cmds.setOriginalSource(originalSource.toString());
            tok = lexer.getString();
        }


            // Parse the show body
        for (;;) {
                // Current token is in tok
            int lineStart = lexer.getLineNumber();
            if (tok == null) {
                lexer.reportError("EOF unexpected");
            } else if ("end_show".equals(tok)) {
                finishBuilding();
                return;
            } else if ("segment".equals(tok)) {
                parseSegment(lineStart);
            } else if ("feature".equals(tok)) {
                parseFeature(true, lineStart);
            } else if ("named_command".equals(tok)) {
                parseNamedCommands(lineStart);
            } else if ("rc_handler".equals(tok)) {
                tok = lexer.getString();
                if ("assembly_grid".equals(tok)) {
                    parseAssemblyGridRCHandler();       // deprecated
                } else if ("visual".equals(tok)) {
                    parseVisualRCHandler();
                } else if ("key_pressed".equals(tok)) {
                    parseCommandRCHandler(true);
                } else if ("key_released".equals(tok)) {
                    parseCommandRCHandler(false);
                } else {
                    lexer.reportError("Unrecognized token \"" + tok + "\"");
                }
            } else if ("mosaic_hint".equals(tok)) {
                lexer.reportWarning("mosaic_hint is deprecated; please use \"mosaics\" file instead.");
                String name = lexer.getString();
                MosaicSpec ms = null;
                try {
                    ms = show.newMosaicSpec(name);
                } catch (IOException ex) {
                    lexer.reportError(ex.getMessage());
                }
                ms.minWidth = lexer.getInt();
                ms.maxWidth = ms.minWidth;
                ms.maxHeight = lexer.getInt();
                ms.imagesToConsider = parseStrings();
                parseExpected(";");
            } else if ("show_top".equals(tok)) {
                parseShowTop();                
            } else {
                lexer.reportError("Unrecognized token \"" + tok + "\"");
            }
            tok = lexer.getString();
        }
    }

    private void parseSegmentStackDepth() throws IOException {
        int depth = lexer.getInt();
        if (depth < 0) {
            lexer.reportError("Illegal depth:  " + depth);
        }
        parseExpected(";");
        show.setSegmentStackDepth(depth);
    }

    private void parseDrawTargets() throws IOException {
        String[] drawTargets = parseStrings();
        if (drawTargets.length == 0) {
            lexer.reportError("Must have at least one draw target");
        }
        parseExpected(";");
        show.setDrawTargets(drawTargets);
    }

    private void parseShowTop() throws IOException {
        final String showTopName = lexer.getString();
        parseExpected(";");
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                builder.setShowTop(showTopName);        
            }
        };
        deferred.get(0).add(fw);
    }

    private void parseStickyImages() throws IOException {
        String[] stickyImages = parseStrings();
        parseExpected(";");
        builder.setStickyImages(stickyImages);
    }

    private void parseBinaryGrinFile() throws IOException {
        String fileName = lexer.getString();
        parseExpected(";");
        builder.setBinaryGrinFileName(fileName);
    }

    private void parseGrinviewBackground() throws IOException {
        ArrayList<BackgroundSpec> specs = new ArrayList<BackgroundSpec>();
        parseExpected("{");
        specs.add(new BackgroundSpec());        // blank
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            }
            BackgroundSpec spec = new BackgroundSpec();
            spec.imageName = tok;
            if (spec.imageName == null) {
                lexer.reportError("Unexpected EOF");
            }
            specs.add(spec);
        }
        parseExpected(";");
        show.setGrinviewBackgrounds(specs);
    }

    private void parseSegment(final int line) throws IOException {
        final String name = lexer.getString();
        String[] sa;
        String s;
        String tok = lexer.getString();
        if ("active".equals(tok)) {
            sa = parseStrings();
            tok = lexer.getString();
        } else {
            sa = new String[0];
        }
        final SubFeature[] active = makeSubFeatureList(sa);
        if ("setup".equals(tok)) {
            sa = parseStrings();
            tok = lexer.getString();
        } else {
            sa = new String[0];
        }
        final SubFeature[] setup = makeSubFeatureList(sa);
        if ("chapter".equals(tok)) {
            lexer.getString();
            tok = lexer.getString();
            lexer.reportWarning("Deprecated chapter name ignored");
        }
        if ("rc_handlers".equals(tok)) {
            sa = parseStrings();
            tok = lexer.getString();
        } else {
            sa = new String[0];
        }
        final String[] rcHandlers = sa;
        Command[] ca;
        if ("on_entry".equals(tok)) {
            ca = parseCommands();
            tok = lexer.getString();
        } else {
            ca = emptyCommandArray;
        }
        final Command[] onEntry = ca;
        final boolean nextOnSetupDone = "setup_done".equals(tok);
        if (nextOnSetupDone || "next".equals(tok)) {
            ca = parseCommands();
            tok = lexer.getString();
        } else {
            ca = emptyCommandArray;
        }
        final Command[] next = ca;
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Feature[] a = makeFeatureList(active);
                Feature[] s = makeFeatureList(setup);
                RCHandler[] h = makeRCHandlerList(rcHandlers);
                builder.addSegment(name,line,new SESegment(name, a, s, h,
                                   onEntry, nextOnSetupDone, next));
            }
        };
        deferred.get(0).add(fw);
    }

    //
    // Segments only have named subfeatures, so this works.
    //
    private SubFeature[] makeSubFeatureList(String[] names) {
        SubFeature[] result = new SubFeature[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = new SubFeature(builder, names[i], lexer);
        }
        return result;
    }

    private Feature parseFeature(boolean hasName, int lineStart) 
                throws IOException 
    {
        String tok = lexer.getString();
        if ("fixed_image".equals(tok)) {
            return parseFixedImage(hasName, lineStart);
        } else if ("image_sequence".equals(tok)) {
            return parseImageSequence(hasName, lineStart);
        } else if ("box".equals(tok)) {
            return parseBox(hasName, lineStart);
        } else if ("assembly".equals(tok)) {
            return parseAssembly(hasName, lineStart);
        } else if ("menu_assembly".equals(tok)) {
            return parseMenuAssembly(hasName, lineStart);
        } else if ("group".equals(tok)) {
            return parseGroup(hasName, lineStart);
        } else if ("clipped".equals(tok)) {
            return parseClipped(hasName, lineStart);
        } else if ("src_over".equals(tok)) {
            return parseSrcOver(hasName, lineStart);
        } else if ("fade".equals(tok)) {
            return parseFade(hasName, lineStart);
        } else if ("timer".equals(tok)) {
            return parseTimer(hasName, lineStart);
        } else if ("translation".equals(tok) || "translator_model".equals(tok)) 
        {
            return parseTranslatorModel(hasName, lineStart);
        } else if ("translator".equals(tok)) {
            return parseTranslator(hasName, lineStart);
        } else if ("text".equals(tok)) {
            return parseText(hasName, lineStart);
        } else if ("scaling_model".equals(tok)) {
            return parseScalingModel(hasName, lineStart);
        } else if ("guarantee_fill".equals(tok)) {
            return parseGuaranteeFill(hasName, lineStart);
        } else if ("set_target".equals(tok)) {
            return parseSetTarget(hasName, lineStart);
        } else if ("showtop_group".equals(tok)) {
            return parseShowTopGroup(hasName, lineStart);
        } else if (extParser == null || tok == null) {
            lexer.reportError("Unrecognized feature \"" + tok + "\"");
            return null;        // not reached
        } else if ("extension".equals(tok) || "modifier".equals(tok)) {
            String typeName = lexer.getString();
            String name = null;
            if (hasName) {
                name = lexer.getString();
            }
            SubFeature sub = null;
            if ("modifier".equals(tok)) {
                sub = parseSubFeature(lexer.getString());
            }
            Feature f;
            if (typeName.indexOf(':') < 0) {
                lexer.reportError(typeName + " doesn't contain \":\"");
            }
            if (sub == null) {
                f = extParser.getFeature(show, typeName, name, lexer);
            } else {
                Modifier m = extParser.getModifier(show, typeName,
                                                   name, lexer);
                f = m;
                if (m != null) {
                    resolveModifier(m, sub);
                }
            }
            if (f == null) {
                lexer.reportError("Unrecognized feature " + typeName);
            }
            builder.addFeature(name, lineStart, f);
            return f;
        } else {
            lexer.reportError("Unrecognized feature \"" + tok + "\"");
            return null;        // not reached
        }
    }

    private Feature parseFixedImage(boolean hasName, int line) 
                throws IOException 
    {
        final String name = parseFeatureName(hasName);
        final SEImagePlacement placement 
            = parseImagePlacement(lexer.getString());
        String fileName = lexer.getString();
        String tok = lexer.getString();
        String scalingModel = null;
        if ("scaling_model".equals(tok)) {
            scalingModel = lexer.getString();
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
            lexer.reportError("';' expected, " + tok + " seen");
        }
        final SEFixedImage f = new SEFixedImage(show, name, placement,fileName);
        builder.addFeature(name,line, f);
        if (scalingModel != null) {
            final String scalingModelF = scalingModel;
            ForwardReference fw = new ForwardReference(lexer) {
                public void resolve() throws IOException {
                    Feature smf = builder.getNamedFeature(scalingModelF);
                    if (smf == null || !(smf instanceof InterpolatedModel)) {
                        lexer.reportError("In fixed_image " + name + 
                                  " can't find scaling_model "
                                  + scalingModelF + ".");
                    }
                    f.setScalingModel((InterpolatedModel) smf);
                }
            };
            deferred.get(0).add(fw);
        }
        return f;
    }

    private Feature parseImageSequence(boolean hasName, int line) 
                throws IOException 
    {
        final String name = parseFeatureName(hasName);
        SEImageSeqPlacement placement = parseImageSeqPlacement();
        String fileName = lexer.getString();
        String[] middle = parseStrings();
        if (middle.length == 0) {
            lexer.reportError("Must have at least one file in a sequence");
        }
        for (int i = 0; i < middle.length; i++) {
            if ("+".equals(middle[i])) {
                if (i == 0) {
                    middle[i] = null;
                } else { 
                    middle[i] = middle[i-1];
                }
            } else if ("-".equals(middle[i])) {
                middle[i] = null;
            }
        }
        String extension = lexer.getString();
        String tok = lexer.getString();
        boolean repeat = false;
        if ("repeat".equals(tok)) {
            repeat = true;
            tok = lexer.getString();
        }
        String scalingModel = null;
        if ("scaling_model".equals(tok)) {
            scalingModel = lexer.getString();
            tok = lexer.getString();
        }
        String model = null;
        Command[] endCommands = emptyCommandArray;
        int loopCount = 1;
        if ("model".equals(tok) || "linked_to".equals(tok)) {
            model = lexer.getString();
            tok = lexer.getString();
        } else {
            if ("loop_count".equals(tok)) {
                loopCount = lexer.getIntOrInfinite();
                tok = lexer.getString();
            }
            if ("end_commands".equals(tok)) {
                endCommands = parseCommands();
                tok = lexer.getString();
            }
        }
        if (";".equals(tok)) {
        } else {
            lexer.reportError("';' expected, " + tok + " seen");
        }
        final SEImageSequence f 
                = new SEImageSequence(show, name, placement, fileName, 
                                      middle,  extension, repeat, loopCount,
                                      endCommands);
        builder.addFeature(name, line, f);
        if (model != null) {
            final String mod = model;
            ForwardReference fw = new ForwardReference(lexer) {
                public void resolve() throws IOException {
                    Feature modf = builder.getNamedFeature(mod);
                    if (modf == null || !(modf instanceof ImageSequence)) {
                            lexer.reportError("In image_sequence " + name + 
                                      " can't find image_sequence linked_to " 
                                      + mod + ".");
                    }
                    f.setModel((ImageSequence) modf);
                }
            };
            deferred.get(0).add(fw);
        }
        if (scalingModel != null) {
            final String scalingModelF = scalingModel;
            ForwardReference fw = new ForwardReference(lexer) {
                public void resolve() throws IOException {
                    Feature smf = builder.getNamedFeature(scalingModelF);
                    if (smf == null || !(smf instanceof InterpolatedModel)) {
                        lexer.reportError("In image_sequence " + name + 
                                  " can't find scaling_model "
                                  + scalingModelF + ".");
                    }
                    f.setScalingModel((InterpolatedModel) smf);
                }
            };
            deferred.get(0).add(fw);
        }
        return f;
    }
    
    
    private SEImageSeqPlacement parseImageSeqPlacement() throws IOException {
        String tok = lexer.getString();
        if ("{".equals(tok)) {
            ArrayList<SEImagePlacement> placements = new ArrayList();
            for (;;) {
                tok = lexer.getString();
                if ("}".equals(tok)) {
                    break;
                }
                placements.add(parseImagePlacement(tok));
            }
            return new SEImagePlacementList(placements);
        } else {
            return parseImagePlacement(tok);
        }
    }

    //
    // We pass in the first token so that parseImageSeqPlacement can figure
    // out the list syntax
    //
    private SEImagePlacement parseImagePlacement(String tok) throws IOException 
    {
        SEImagePlacement placement = new SEImagePlacement();
        if ("(".equals(tok)) {
            tok = lexer.getString();
            if ("left".equals(tok)) {
                placement.setXAlign(SEImagePlacement.HorizontalAlignment.LEFT);
            } else if ("middle".equals(tok)) {
                placement.setXAlign(
                            SEImagePlacement.HorizontalAlignment.MIDDLE);
            } else if ("right".equals(tok)) {
                placement.setXAlign(SEImagePlacement.HorizontalAlignment.RIGHT);
            } else {
                lexer.reportError("left, middle or right expected, \"" 
                                  + tok + "\" seen.");
            }
            placement.setX(lexer.getInt());

            tok = lexer.getString();
            if ("top".equals(tok)) {
                placement.setYAlign(SEImagePlacement.VerticalAlignment.TOP);
            } else if ("middle".equals(tok)) {
                placement.setYAlign(SEImagePlacement.VerticalAlignment.MIDDLE);
            } else if ("bottom".equals(tok)) {
                placement.setYAlign(SEImagePlacement.VerticalAlignment.BOTTOM);
            } else {
                lexer.reportError("left, middle or right expected, \"" 
                                  + tok + "\" seen.");
            }
            placement.setY(lexer.getInt());

            tok = lexer.getString();
            if ("scale".equals(tok)) {
                placement.setScaleX(lexer.getInt() / 1000.0);
                placement.setScaleY(lexer.getInt() / 1000.0);
                parseExpected("mills");
                tok = lexer.getString();
            }
            lexer.expectString(")", tok);
        } else {
            placement.setX(lexer.convertToInt(tok));
            placement.setY(lexer.getInt());
        }
        return placement;
    }

    private Feature parseBox(boolean hasName, int line) throws IOException {
        final String name = parseFeatureName(hasName);
        Rectangle placement = parseRectangle();
        String tok = lexer.getString();
        int outlineWidth = 0;
        Color outlineColor = null;
        if ("outline".equals(tok)) {
            outlineWidth = lexer.getInt();
            if (outlineWidth < 1) {
                lexer.reportError("" + outlineWidth + " is an illegal width");
            }
            if (outlineWidth*2 > placement.width || outlineWidth*2 > placement.height) {
                lexer.reportError("Outline too wide for box size");
            }
            outlineColor = parseColor();
            tok = lexer.getString();
        }
        Color fillColor = null;
        if ("fill".equals(tok)) {
            fillColor = parseColor();
            tok = lexer.getString();
        }
        String scalingModel = null;
        if ("scaling_model".equals(tok)) {
            scalingModel = lexer.getString();
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        final SEBox box = new SEBox(show, name, placement.x, placement.y,
                                placement.width, placement.height, 
                                outlineWidth, outlineColor, fillColor);
        builder.addFeature(name, line, box);
        if (scalingModel != null) {
            final String scalingModelF = scalingModel;
            ForwardReference fw = new ForwardReference(lexer) {
                public void resolve() throws IOException {
                    Feature smf = builder.getNamedFeature(scalingModelF);
                    if (smf == null || !(smf instanceof InterpolatedModel)) {
                        lexer.reportError("In box " + name + 
                                  " can't find scale_model "
                                  + scalingModelF + ".");
                    }
                    box.setScalingModel((InterpolatedModel) smf);
                }
            };
            deferred.get(0).add(fw);
        }
        return box;
    }


    private Feature parseAssembly(boolean hasName, int line) throws IOException
    {
        String name = parseFeatureName(hasName);
        ArrayList<String> namesList = new ArrayList<String>();
        ArrayList<SubFeature> partsList = new ArrayList<SubFeature>();
        parseExpected("{");
        for (;;) {
            String tok = lexer.getString();
            if (tok == null) {
                lexer.reportError("Unexpected EOF");
            } else if ("}".equals(tok)) {
                break;
            }
            namesList.add(tok);
            tok = lexer.getString();
            partsList.add(parseSubFeature(tok));
        }
        parseExpected(";");
        final SEAssembly a = new SEAssembly(show);
        a.setName(name);
        builder.addFeature(name, line, a);
        final String[] names = namesList.toArray(new String[namesList.size()]);
        final SubFeature[] parts 
                = partsList.toArray(new SubFeature[partsList.size()]);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                a.setParts(names, makeFeatureList(parts));
            }
        };
        deferred.get(0).add(fw);
        return a;
    }

    private Feature parseMenuAssembly(boolean hasName, final int line) 
                throws IOException
    {
        final MenuAssemblyHelper helper = new MenuAssemblyHelper();
        helper.lineNumber = lexer.getLineNumber();
        helper.show = show;
        String name = parseFeatureName(hasName);
        parseExpected("template");
        helper.template = parseMenuAssemblyFeatures();
        helper.partNames = new ArrayList<String>();
        helper.parts = new ArrayList<List<MenuAssemblyHelper.Features>>();
        parseExpected("parts");
        parseExpected("{");
        for (;;) {
            String tok = lexer.getString();
            if (tok == null) {
                lexer.reportError("Unexpected EOF");
            } else if ("}".equals(tok)) {
                break;
            }
            helper.partNames.add(tok);
            helper.parts.add(parseMenuAssemblyFeatures());
        }
        parseExpected(";");
        SEAssembly a = new SEMenuAssembly(show, helper);
        a.setName(name);
        helper.assembly = a;
        builder.addFeature(name, line, a);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Iterable<Feature> syntheticFeatures = helper.setupAssembly();
                //
                // this step combines resolving forward references, and
                // creating synthetic features.  It was written before
                // SENode.postProcess() existed.  It would be a bit
                // cleaner to split the feature-creation part out into
                // SEMenuAssembly.postProcess(), but this old way of doing
                // it works too.
                //
                for (Feature f: syntheticFeatures) {
                    builder.addSyntheticFeature(f);
                }
            }
        };
        deferred.get(1).add(fw);
        return a;
    }

    private List<MenuAssemblyHelper.Features> parseMenuAssemblyFeatures()
            throws IOException
    {
        List<MenuAssemblyHelper.Features> result 
            = new ArrayList<MenuAssemblyHelper.Features>();
        parseExpected("{");
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            }
            MenuAssemblyHelper.Features feat 
                = new MenuAssemblyHelper.Features();
            result.add(feat);
            feat.id = tok;
            parseExpected("{");
            for (;;) {
                tok = lexer.getString();
                if ("}".equals(tok)) {
                    break;
                }
                SubFeature sf = parseSubFeature(tok);
                feat.features.add(sf);
            }
        }
        return result;
    }

    private Feature parseGroup(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        final SubFeature[] parts = parsePartsList();
        parseExpected(";");
        final SEGroup group = new SEGroup(show, name);
        builder.addFeature(name, line, group);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                group.setup(makeFeatureList(parts));
            }
        };
        deferred.get(0).add(fw);
        return group;
    }

    private Feature parseShowTopGroup(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        parseExpected(";");
        final SEGroup group = new SEGroup(show, name);
        group.setParts(new Feature[0]);
        builder.addFeature(name, line, group);
        builder.setShowTopGroup(group);
        // nothing to setup for this...     
        return group;
    }
    
    private Feature parseClipped(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        SubFeature part = parseSubFeature(lexer.getString());
        Rectangle clipRegion = parseRectangle();
        parseExpected(";");
        SEClipped clipped = new SEClipped(show, name, clipRegion);
        builder.addFeature(name, line, clipped);
        resolveModifier(clipped, part);
        return clipped;
    }

    private Feature parseSrcOver(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        SubFeature part = parseSubFeature(lexer.getString());
        parseExpected(";");
        SESrcOver so = new SESrcOver(show, name);
        builder.addFeature(name, line, so);
        resolveModifier(so, part);
        return so;
    }

    private void resolveModifier(final Modifier m, final SubFeature part) {
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                m.setup(part.getFeature());
            }
        };
        deferred.get(0).add(fw);
    }

    private Feature parseFade(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        SubFeature part = parseSubFeature(lexer.getString());
        String tok = lexer.getString();
        boolean srcOver = false;
        if ("src_over".equals(tok)) {
            srcOver = true;
            tok = lexer.getString();
        }
        if (!("{".equals(tok))) {
            lexer.reportError("'{' expected, \"" + tok + "\" seen.");
        }
        ArrayList<int[]> keyframes = new ArrayList<int[]>();
        tok = lexer.getString();
        for (;;) {
            if ("}".equals(tok)) {
                break;
            }
            int frameNum = 0;
            try {
                frameNum = Integer.decode(tok).intValue();
            } catch (NumberFormatException ex) {
                lexer.reportError(ex.toString());
            }
            int alpha = lexer.getInt();
            EasingEquation[] easing = new EasingEquation[1];
            tok = lexer.getString();
            tok = parseEasing(tok, easing);
            if (keyframes.size() == 0 || easing[0] == null) {
                keyframes.add(new int[] { frameNum, alpha } );
            } else {
                int[] destination = { frameNum, alpha };
                try {
                    easing[0].addKeyFrames(keyframes, destination);
                } catch (IOException ex) {
                    lexer.reportError("Easing:  " + ex.getMessage());
                }
            }
        }
        tok = lexer.getString();
        Command[] endCommands = emptyCommandArray;
        int repeatFrame;
        if ("repeat".equals(tok)) {
            repeatFrame = lexer.getInt();
            tok = lexer.getString();
        } else {
            repeatFrame = Integer.MAX_VALUE; // Off the end
        }
        int loopCount = 1;
        if ("loop_count".equals(tok)) {
            loopCount = lexer.getIntOrInfinite();
            tok = lexer.getString();
        }
        if ("end_commands".equals(tok)) {
            endCommands = parseCommands();
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        int[] fs = new int[keyframes.size()];
        int[] alphas = new int[keyframes.size()];
        for (int i = 0; i < keyframes.size(); i++) {
            int[] el = keyframes.get(i);
            fs[i] = el[0];
            alphas[i] = el[1];
            if (i > 0 && fs[i] <= fs[i-1]) {
                lexer.reportError("Frame number must be increasing");
            }
            if (alphas[i] < 0 || alphas[i] > 255) {
                lexer.reportError("Illegal alpha value:  " + alphas[i]);
            }
        }
        if (fs.length < 1) {
            lexer.reportError("Need at least one keyframe");
        }
        if (fs[0] != 0) { 
            lexer.reportError("Keyframes must start at frame 0");
        }
        SEFade fade = new SEFade(show, name, srcOver, fs, alphas, repeatFrame,
                                 loopCount, endCommands);
        builder.addFeature(name, line, fade);
        resolveModifier(fade, part);
        return fade;
    }

    private Feature parseTimer(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        int numFrames = lexer.getInt();
        String tok = lexer.getString();
        boolean repeat = false;
        if ("repeat".equals(tok)) {
            repeat = true; 
            parseExpected("{");
        } else if ("{".equals(tok)) {
            // do nothing, i.e. consume token
        } else {
            lexer.reportError("'{' or 'repeat' expected");
        }
        Command[] commands = parseCommandsNoOpenBrace();
        parseExpected(";");
        if (numFrames < 0 || (repeat && numFrames < 1)) {
            lexer.reportError("More frames, please.");
        }
        InterpolatedModel timer 
            = builder.makeTimer(name, numFrames, repeat, commands);
        builder.addFeature(name, line, timer);
        return timer;
    }

    private Feature parseGuaranteeFill(boolean hasName, int line) 
            throws IOException 
    {
        String name = parseFeatureName(hasName);
        SubFeature part = parseSubFeature(lexer.getString());
        Rectangle g = parseRectangle();
        parseExpected("{");
        Vector v = new Vector();
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            } else if (!("(".equals(tok))) {
               lexer.reportError("\"(\" expected, \"" + tok + "\" seen");
            }
            v.add(parseRectangleNoLeading());
        }
        parseExpected(";");
        int num = v.size();
        Rectangle[] result = null;
        if (num > 0) {
            result = new Rectangle[num];
            for (int i = 0; i < num; i++) {
                result[i] = (Rectangle) v.elementAt(i);
            }
        }
        SEGuaranteeFill f = new SEGuaranteeFill(show, name, g, result);
        builder.addFeature(name, line, f);
        resolveModifier(f, part);
        return f;
    }

    private Feature parseSetTarget(boolean hasName, int line) 
                throws IOException 
    {
        String name = parseFeatureName(hasName);
        SubFeature part = parseSubFeature(lexer.getString());
        String targetName = lexer.getString();
        String[] names = show.getDrawTargets();
        int target = -1;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(targetName)) {
                target = i;
                break;
            }
        }
        if (target == -1) {
            lexer.reportError("Target name \"" + targetName + "\" not found");
        }
        parseExpected(";");
        SESetTarget f = new SESetTarget(show, name, target);
        builder.addFeature(name, line, f);
        resolveModifier(f, part);
        return f;
    }


    private Feature parseTranslatorModel(boolean hasName, int line) 
                throws IOException 
    {
        String name = parseFeatureName(hasName);
        parseExpected("{");
        ArrayList<int[]> keyframes = new ArrayList<int[]>();
        boolean isRelative = false;
        String tok = lexer.getString();
        for (;;) {
            if ("}".equals(tok)) {
                tok = lexer.getString();
                break;
            }
            int frameNum = 0;
            try {
                frameNum = Integer.decode(tok).intValue();
            } catch (NumberFormatException ex) {
                lexer.reportError(ex.toString());
            }
            int x = lexer.getIntOrOffscreen();
            int y = lexer.getIntOrOffscreen();
            String tweenType = lexer.getString();
            boolean thisIsRelative = true;
            EasingEquation[] easing = new EasingEquation[1];
            if ("linear".equals(tweenType)) {
                thisIsRelative = false;
            } else if ("linear-relative".equals(tweenType)) {
                tweenType = "linear";
            }
            tok = parseEasing(tweenType, easing);
            if (keyframes.size() == 0 || easing[0] == null) {
                keyframes.add(new int[] { frameNum, x, y } );
            } else {
                int[] destination = { frameNum, x, y };
                try {
                    easing[0].addKeyFrames(keyframes, destination);
                } catch (IOException ex) {
                    lexer.reportError("Easing:  " + ex.getMessage());
                }
            }
            if (keyframes.size() == 1) {
                if (!thisIsRelative) {
                    lexer.reportWarning(
                      "\nWARNING:  "
                      + "Deprecated non-relative linear interpolation;"
                      + "\n    consider using linear-relative instead."
                      + "\n    Note that \"linear\" isn't guaranteed to work with all feature types."
                      + "\n    For example, it's known that it doesn't work with the text feature."
                      + "\n    ==> Dangerous usage occurs");
                }
            } else if (thisIsRelative != isRelative) {
                lexer.reportError("Use of incompatible, deprecated linear with other interpolation types; must use linear-relative.");
            }
            isRelative = thisIsRelative;
        }
        int repeatFrame = -1;
        if ("repeat".equals(tok)) {
            repeatFrame = lexer.getInt();
            tok = lexer.getString();
        }
        int loopCount = 1;
        if ("loop_count".equals(tok)) {
            loopCount = lexer.getIntOrInfinite();
            tok = lexer.getString();
        }
        Command[] endCommands = emptyCommandArray;
        if ("end_commands".equals(tok)) {
            endCommands = parseCommands();
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        int[] fs = new int[keyframes.size()];
        int[][] values = new int[2][];
        values[0] = new int[keyframes.size()];
        values[1] = new int[keyframes.size()];
        for (int i = 0; i < keyframes.size(); i++) {
            int[] el = keyframes.get(i);
            fs[i] = el[0];
            values[Translator.X_FIELD][i] = el[1];
            values[Translator.Y_FIELD][i] = el[2];
            if (i > 0 && fs[i] <= fs[i-1]) {
                lexer.reportError("Frame number must be increasing");
            }
        }
        if (fs.length < 1) {
            lexer.reportError("Need at least one keyframe");
        }
        if (fs[0] != 0) { 
            lexer.reportError("Keyframes must start at frame 0");
        }
        if (repeatFrame == -1) {
            repeatFrame = Integer.MAX_VALUE;    // Make it stick at end
        } else if (repeatFrame > fs[fs.length - 1]) {
            lexer.reportError("repeat > max frame");
        }
        InterpolatedModel trans 
            = builder.makeTranslatorModel(name, fs, values, isRelative,
                                          repeatFrame, loopCount, endCommands);
        builder.addFeature(name, line, trans);
        return trans;
    }

    //
    // Parse the easing, and set easing[0] to that value.  Return the next
    // token to be parsed.
    //
    private String parseEasing(String tweenType, EasingEquation[] easing) 
                throws IOException
    {
        String tok = lexer.getString();

        if ("linear".equals(tweenType)) {
            // Nothing special needed
        } else if ("start".equals(tweenType)) {
            // start is a synonym for linear-relative
        } else if ("ease-in-quad".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInQuad(t, b, c, d);
                }
            };
        } else if ("ease-out-quad".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutQuad(t, b, c, d);
                }
            };
        } else if ("ease-in-out-quad".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutQuad(t, b, c, d);
                }
            };
        } else if ("ease-in-cubic".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInCubic(t, b, c, d);
                }
            };
        } else if ("ease-out-cubic".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutCubic(t, b, c, d);
                }
            };
        } else if ("ease-in-out-cubic".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutCubic(t, b, c, d);
                }
            };
        } else if ("ease-in-quart".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInQuart(t, b, c, d);
                }
            };
        } else if ("ease-out-quart".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutQuart(t, b, c, d);
                }
            };
        } else if ("ease-in-out-quart".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutQuart(t, b, c, d);
                }
            };
        } else if ("ease-in-quint".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInQuint(t, b, c, d);
                }
            };
        } else if ("ease-out-quint".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutQuint(t, b, c, d);
                }
            };
        } else if ("ease-in-out-quint".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutQuint(t, b, c, d);
                }
            };
        } else if ("ease-in-sine".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInSine(t, b, c, d);
                }
            };
        } else if ("ease-out-sine".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutSine(t, b, c, d);
                }
            };
        } else if ("ease-in-out-sine".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutSine(t, b, c, d);
                }
            };
        } else if ("ease-in-expo".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInExpo(t, b, c, d);
                }
            };
        } else if ("ease-out-expo".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutExpo(t, b, c, d);
                }
            };
        } else if ("ease-in-out-expo".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutExpo(t, b, c, d);
                }
            };
        } else if ("ease-in-circ".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInCirc(t, b, c, d);
                }
            };
        } else if ("ease-out-circ".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutCirc(t, b, c, d);
                }
            };
        } else if ("ease-in-out-circ".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutCirc(t, b, c, d);
                }
            };
        } else if ("ease-in-elastic".equals(tweenType)) {
            final double[] a = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "amplitude", a);
            final double[] p = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "period", p);
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInElastic(t, b, c, d,a[0],p[0]);
                }
            };
        } else if ("ease-out-elastic".equals(tweenType)) {
            final double[] a = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "amplitude", a);
            final double[] p = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "period", p);
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutElastic(t, b, c,d,a[0],p[0]);
                }
            };
        } else if ("ease-in-out-elastic".equals(tweenType)) {
            final double[] a = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "amplitude", a);
            final double[] p = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "period", p);
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return 
                    PennerEasing.easeInOutElastic(t, b, c, d, a[0], p[0]);
                }
            };
        } else if ("ease-in-back".equals(tweenType)) {
            final double[] s = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "overshoot", s);
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInBack(t, b, c, d, s[0]);
                }
            };
        } else if ("ease-out-back".equals(tweenType)) {
            final double[] s = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "overshoot", s);
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutBack(t, b, c, d, s[0]);
                }
            };
        } else if ("ease-in-out-back".equals(tweenType)) {
            final double[] s = { PennerEasing.DEFAULT };
            tok = getOptionalParameter(tok, "overshoot", s);
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutBack(t, b, c, d, s[0]);
                }
            };
        } else if ("ease-in-bounce".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInBounce(t, b, c, d);
                }
            };
        } else if ("ease-out-bounce".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeOutBounce(t, b, c, d);
                }
            };
        } else if ("ease-in-out-bounce".equals(tweenType)) {
            easing[0] = new EasingEquation() {
                public double 
                evaluate(double t, double b, double c, double d) {
                    return PennerEasing.easeInOutBounce(t, b, c, d);
                }
            };
        } else if ("ease-points".equals(tweenType)) {
            lexer.expectString("{", tok);
            easing[0] = parseEasePoints();
            tok = lexer.getString();
        } else {
            lexer.reportError("unknown tween type \"" + tweenType + "\"");
        }
        if ("max-error".equals(tok)) {
            int maxError = lexer.getInt();
            tok = lexer.getString();
            if (easing[0] == null) {
                // It's OK to specify a max-error for linear interpolation,
                // but it doesn't mean anything, since there's no
                // approximation with linear interpolation.
            } else {
                easing[0].setMaxError(maxError);
            } 
        }
        return tok;
    }

    private EasingEquation parseEasePoints() throws IOException {
        ArrayList<int[]> frames = new ArrayList<int[]>();
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            } else if (!("(".equals(tok))) {
                lexer.reportError("\"(\" expected");
            }
            ArrayList<Integer> values = new ArrayList<Integer>();
            for (;;) {
                tok = lexer.getString();
                if (")".equals(tok)) {
                    break;
                }
                values.add(new Integer(lexer.convertToInt(tok)));
            }
            int[] arr = new int[values.size()];
            for (int i = 0; i < values.size(); i++) {
                arr[i] = values.get(i).intValue();
            }
            frames.add(arr);
        }
        int[][] points = frames.toArray(new int[frames.size()][]);
        return new PointsEasingEquation(points);
    }

    //
    // Get an optional parameter named name.  Returns the next token after
    // the optional parameter.  Delivers result to value[0].
    //
    private String getOptionalParameter(String tok, String name, double[] value)
                throws IOException
    {
        if (name.equals(tok)) {
            value[0] = lexer.getDouble();
            return lexer.getString();
        } else {
            return tok;
        }
    }

    private Feature parseTranslator(boolean hasName, int line) 
                throws IOException 
    {
        String name = parseFeatureName(hasName);
        final String translationName = lexer.getString();
        final SubFeature[] parts = parsePartsList();
        parseExpected(";");
        final SETranslator trans = new SETranslator(show, name);
        builder.addFeature(name, line, trans);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Feature t  = builder.getNamedFeature(translationName);
                if (t == null || !(t instanceof SETranslatorModel)) {
                    lexer.reportError("Translation \"" + translationName 
                                        + "\" not found");
                }
                SETranslatorModel m = (SETranslatorModel) t;
                trans.setupModelIsRelative(m.getIsRelative());
                Feature[] fa = makeFeatureList(parts);
                if (fa.length == 1) {
                    trans.setup((InterpolatedModel) t, fa[0]);
                } else {
                    SEGroup group = new SEGroup(show, null);
                    group.setup(fa);
                    trans.setup((InterpolatedModel) t, group);
                    builder.addFeature(null, 0, group);
                }
            }
        };
        deferred.get(0).add(fw);
        builder.addDeferredBuilder(new TranslatorHelper(trans, line));
        return trans;
    }


    private Feature parseText(boolean hasName, int line) throws IOException {
        String name = parseFeatureName(hasName);
        String tok = lexer.getString();
        int xAlign = SEText.LEFT;
        if ("left".equals(tok)) {
            xAlign = SEText.LEFT;
            tok = lexer.getString();
        } else if ("middle".equals(tok)) {
            xAlign = SEText.MIDDLE;
            tok = lexer.getString();
        } else if ("right".equals(tok)) {
            xAlign = SEText.RIGHT;
            tok = lexer.getString();
        }
        int x = lexer.convertToInt(tok);
        tok = lexer.getString();
        int yAlign = SEText.TOP;
        if ("top".equals(tok)) {
            yAlign = SEText.TOP;
            tok = lexer.getString();
        } else if ("baseline".equals(tok)) {
            yAlign = SEText.BASELINE;
            tok = lexer.getString();
        } else if ("bottom".equals(tok)) {
            yAlign = SEText.BOTTOM;
            tok = lexer.getString();
        }
        int y = lexer.convertToInt(tok);
        int alignment = xAlign | yAlign;

        tok = lexer.getString();
        String[] textStrings;
        int vspace = 0;
        if ("{".equals(tok)) {
            textStrings = parseStringsWithOpenBraceRead();
        } else {
            textStrings = new String[] { tok };
        }
        tok = lexer.getString();
        if ("vspace".equals(tok)) {
            vspace = lexer.getInt();
            tok = lexer.getString();
        }
        FontSpec fontSpec = parseFontSpec(tok);
        int fontIndex = builder.getFontIndex(fontSpec);
        parseExpected("{");
        Vector colors = new Vector();
        Color lastColor = null;
        for (;;) {
            tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            } else if ("+".equals(tok)) {
                if (lastColor == null) {
                    lexer.reportError("First color must be specified");
                }
            } else if (!("{".equals(tok))) {
                lexer.reportError("'{' or '+' expected, \"" + tok + "\" seen.");
            } else {
                lastColor = parseColorNoOpenBrace();
            }
            colors.addElement(lastColor);
        }
        tok = lexer.getString();
        int loopCount = 1;
        if ("loop_count".equals(tok)) {
            loopCount = lexer.getIntOrInfinite();
            tok = lexer.getString();
        }
        Color background = null;
        if ("background".equals(tok)) {
            background = parseColor();
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        Color[] cols = new Color[colors.size()];
        for (int i = 0; i < cols.length; i++) {
            cols[i] = (Color) colors.elementAt(i);
        }
        if (cols.length < 1) {
            lexer.reportError("At least one color needed");
        }
        SEText text = new SEText(show, name, x, y, alignment, textStrings, 
                                 vspace, fontIndex, cols, loopCount,background);
        builder.addFeature(name, line, text);
        return text;
    }

    private Feature parseScalingModel(boolean hasName, int line) 
                throws IOException 
    {
        String name = parseFeatureName(hasName);
        parseExpected("{");
        ArrayList<int[]> keyframes = new ArrayList<int[]>();
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            }
            int frameNum = 0;
            try {
                frameNum = Integer.decode(tok).intValue();
            } catch (NumberFormatException ex) {
                lexer.reportError(ex.toString());
            }
            int x = lexer.getInt();
            int y = lexer.getInt();
            int scaleX = lexer.getInt();
            int scaleY = lexer.getInt();
            tok = lexer.getString();
            EasingEquation[] easing = new EasingEquation[1];
            if (!("mills".equals(tok))) {
                tok = parseEasing(tok, easing);
            }
            if (keyframes.size() == 0 || easing[0] == null) {
                keyframes.add(new int[] { frameNum, x, y, scaleX, scaleY } );
            } else {
                int[] destination = { frameNum, x, y, scaleX, scaleY };
                try {
                    easing[0].addKeyFrames(keyframes, destination);
                } catch (IOException ex) {
                    lexer.reportError("Easing:  " + ex.getMessage());
                }
            }
            lexer.expectString("mills", tok);
        }
        String tok = lexer.getString();
        int repeatFrame = -1;
        if ("repeat".equals(tok)) {
            repeatFrame = lexer.getInt();
            tok = lexer.getString();
        }
        int loopCount = 1;
        if ("loop_count".equals(tok)) {
            loopCount = lexer.getIntOrInfinite();
            tok = lexer.getString();
        }
        Command[] endCommands = emptyCommandArray;
        if ("end_commands".equals(tok)) {
            endCommands = parseCommands();
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        int[] fs = new int[keyframes.size()];
        int[][] values = new int[4][];
        values[0] = new int[keyframes.size()];
        values[1] = new int[keyframes.size()];
        values[2] = new int[keyframes.size()];
        values[3] = new int[keyframes.size()];
        for (int i = 0; i < keyframes.size(); i++) {
            int[] el = keyframes.get(i);
            fs[i] = el[0];
            values[InterpolatedModel.SCALE_X_FIELD][i] = el[1];
            values[InterpolatedModel.SCALE_Y_FIELD][i] = el[2];
            values[InterpolatedModel.SCALE_X_FACTOR_FIELD][i] = el[3];
            values[InterpolatedModel.SCALE_Y_FACTOR_FIELD][i] = el[4];
            if (i > 0 && fs[i] <= fs[i-1]) {
                lexer.reportError("Frame number must be increasing");
            }
        }
        if (fs.length < 1) {
            lexer.reportError("Need at least one keyframe");
        }
        if (fs[0] != 0) { 
            lexer.reportError("Keyframes must start at frame 0");
        }
        if (repeatFrame == -1) {
            repeatFrame = Integer.MAX_VALUE;    // Make it stick at end
        } else if (repeatFrame > fs[fs.length - 1]) {
            lexer.reportError("repeat > max frame");
        }
        InterpolatedModel scaleModel 
            = builder.makeScalingModel(name, fs, values, repeatFrame,
                                       loopCount, endCommands);
        builder.addFeature(name, line, scaleModel);
        return scaleModel;
    }

    private FontSpec parseFontSpec(String tok) throws IOException {
        FontSpec result = new FontSpec();
        result.name = tok;
        tok = lexer.getString();
        if ("plain".equals(tok)) {
            result.style = Font.PLAIN;
        } else if ("bold".equals(tok)) {
            result.style = Font.BOLD;
        } else if ("italic".equals(tok)) {
            result.style = Font.ITALIC;
        } else if ("bold-italic".equals(tok)) {
            result.style = Font.BOLD | Font.ITALIC;
        } else {
            lexer.reportError("font style expected, \"" + tok + "\" seen");
        }
        result.size = lexer.getInt();
        return result;
    }

    private String parseFeatureName(boolean hasName) throws IOException {
        if (hasName) {
            return lexer.getString();
        } else {
            return null;
        }
    }

    private void parseNamedCommands(int lineStart) throws IOException {
        String name = lexer.getString();
        Command[] arr = parseCommands();
        parseExpected(";");
        Command result;
        if (arr.length == 1) {
            result = arr[0];
        } else {
            result = new SECommandList(show, arr);
        }
        builder.addNamedCommand(name, lineStart, result);
    }

    private void parseAssemblyGridRCHandler() throws IOException {
        lexer.reportWarning("Deprecated rc_handler assembly_grid");
        int lineStart = lexer.getLineNumber();
        String handlerName = lexer.getString();
        parseExpected("assembly");
        final String assemblyName = lexer.getString();
        parseExpected("select");
        final String[][] selectParts = parseMatrix();
        parseExpected("invoke");
        final String[][] invokeParts = parseMatrix();
        final int height = selectParts.length;
        if (height <= 0) {
            lexer.reportError("Empty grid");
        }
        final int width = selectParts[0].length;
        if (selectParts.length != invokeParts.length) {
            lexer.reportError("matricies have different number of rows: " +
                        selectParts.length + " vs. " + invokeParts.length
                        + ".");
        }
        for (int i = 0; i < selectParts.length; i++) {
            if (selectParts[i].length != invokeParts[i].length) {
                lexer.reportError("row " + (i+1) 
                            + " has different number of entries: "
                            + selectParts[i].length + " vs. "
                            + invokeParts[i].length + ".");
            }
            if (selectParts[i].length != width) {
                lexer.reportError("Unequal widths of grid");
            }
        }

        String tok = lexer.getString();
        int timeout = -1;
        Command[] timeoutCommands = emptyCommandArray;
        if ("timeout".equals(tok)) {
            timeout = lexer.getInt();
            parseExpected("frames");
            timeoutCommands = parseCommands();
            tok = lexer.getString();
        }
        Command[][] activateCommands = null;
        if ("when_invoked".equals(tok)) {
            activateCommands = new Command[height * width][];
            parseExpected("{");
            for (;;) {
                tok = lexer.getString();
                if ("}".equals(tok)) {
                    break;
                }
                Command[] c = parseCommands();
                int i = findInMatrix(tok, invokeParts);
                if (i == -1) {
                    lexer.reportError("Can't find part " + tok);
                } else if (activateCommands[i] != null) {
                    lexer.reportError("Duplicate part " + tok);
                }
                activateCommands[i] = c;
            }
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        VisualRCHandlerHelper helper = new VisualRCHandlerHelper();
        helper.setHandlerName(handlerName);
        visualRCHelpers.put(handlerName, helper);
        ArrayList<ArrayList<VisualRCHandlerCell>> grid 
            = new ArrayList<ArrayList<VisualRCHandlerCell>>();
        for (int y = 0; y < height; y++) {
            ArrayList<VisualRCHandlerCell> row 
                = new ArrayList<VisualRCHandlerCell>();
            grid.add(row);
            for (int x = 0; x < selectParts[y].length; x++) {
                row.add(VisualRCHandlerCell.newState("" + x + "," + y));
            }
        }
        String msg = helper.addGrid(grid);
        if (msg != null) {
           lexer.reportError(msg);
        }
        helper.addRCOverrides(new HashMap<String, String>());
        helper.setSelectCommands(null);
        helper.setActivateCommands(activateCommands);
        helper.setMouseRects(null);
        helper.setMouseRectStates(null);
        helper.setTimeout(timeout);
        helper.setTimeoutCommands(timeoutCommands);
        final SEVisualRCHandler hand = helper.getFinishedHandler();
        builder.addRCHandler(handlerName, lineStart, hand);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Assembly assembly = lookupAssemblyOrFail(assemblyName);
                Feature[] realSelectParts 
                    = lookupFeatureGrid(assembly, selectParts, width, height);
                Feature[] realInvokeParts 
                    = lookupFeatureGrid(assembly, invokeParts, width, height);
                hand.setup(assembly, realSelectParts, realInvokeParts);
            }
        };
        deferred.get(0).add(fw);
    }

    private void parseVisualRCHandler() throws IOException {
        VisualRCHandlerHelper helper = new VisualRCHandlerHelper();
        int lineStart = lexer.getLineNumber();
        String handlerName = lexer.getString();
        helper.setHandlerName(handlerName);
        visualRCHelpers.put(handlerName, helper);
        String tok = lexer.getString();
        if ("grid".equals(tok)) {
            String msg = helper.addGrid(parseVisualGrid(null));
            if (msg != null) {
                lexer.reportError(msg);
            }
            tok = lexer.getString();
            if ("rc_override".equals(tok)) {
                helper.addRCOverrides(parseRCOverride());
                tok = lexer.getString();
            } else {
                helper.addRCOverrides(new HashMap<String, String>());
            }
        } else if ("grid_alternates".equals(tok)) {
            parseExpected("{");
            for (;;) {
                tok = lexer.getString();
                if ("}".equals(tok)) {
                    tok = lexer.getString();
                    break;
                }
                helper.addGrid(parseVisualGrid(helper));
                helper.addGridAlternateName(tok);
            }
        } else {
           lexer.reportError("\"grid\" or \"grid_alternates\" expected, \"" 
                                + tok + "\" seen");
        }
        if ("assembly".equals(tok)) {
            tok = lexer.getString();    // Assembly name
            String next = lexer.getString();
            if ("start_selected".equals(next)) {
                helper.setStartSelected(lexer.getBoolean());
                next = lexer.getString();
            }
            lexer.expectString("select", next);
        } else {
            lexer.expectString("select", tok);
            tok = null;         // Assembly name
        }
        final String assemblyName = tok;

        Map<String, Integer> states = helper.getStates();
        Object[] oa = parseVisualActions(states, assemblyName != null);
        final String[] selectParts = (String[]) oa[0];
        helper.setSelectCommands((Command[][]) oa[1]);
        parseExpected("activate");
        oa = parseVisualActions(states, assemblyName != null);
        final String[] activateParts = (String[]) oa[0];
        helper.setActivateCommands((Command[][]) oa[1]);

        tok = lexer.getString();
        Rectangle[] mouseRects = null;
        int[] mouseRectStates = null;
        if ("mouse".equals(tok)) {
            Vector v = parseMouseLocations(states);
            mouseRects = new Rectangle[v.size() / 2];
            mouseRectStates = new int[v.size() / 2];
            for (int i = 0; i < mouseRects.length; i++) {
                mouseRectStates[i] = ((Integer)v.elementAt(i*2)).intValue();
                mouseRects[i] = (Rectangle) v.elementAt(i*2 + 1);
            }
            tok = lexer.getString();
        }
        helper.setMouseRects(mouseRects);
        helper.setMouseRectStates(mouseRectStates);
        int timeout = -1;
        Command[] timeoutCommands = emptyCommandArray;
        if ("timeout".equals(tok)) {
            timeout = lexer.getInt();
            parseExpected("frames");
            timeoutCommands = parseCommands();
            tok = lexer.getString();
        }
        helper.setTimeout(timeout);
        helper.setTimeoutCommands(timeoutCommands);
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        SEVisualRCHandler handler = null;
        try {
            handler = helper.getFinishedHandler();
        } catch (IOException ex) {
            lexer.reportError(ex.getMessage());
        }
        final SEVisualRCHandler hand = handler;
        builder.addRCHandler(handlerName, lineStart, hand);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Assembly assembly = null;
                Feature[] realSelParts = null;
                Feature[] realActParts = null;
                if (assemblyName != null) {
                    assembly = lookupAssemblyOrFail(assemblyName);
                    realSelParts = lookupAssemblyParts(assembly, selectParts);
                    realActParts =  lookupAssemblyParts(assembly,activateParts);
                }
                hand.setup(assembly, realSelParts, realActParts);
            }
        };
        deferred.get(2).add(fw);
    }

    private ArrayList<ArrayList<VisualRCHandlerCell>> 
    parseVisualGrid(VisualRCHandlerHelper helper) 
                throws IOException 
    {
        ArrayList<ArrayList<VisualRCHandlerCell>>  result = new ArrayList();
        parseExpected("{");
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                if (helper != null) {
                    helper.addRCOverrides(new HashMap<String, String>());
                }
                break;
            } else if (helper != null && "rc_override".equals(tok)) {
                    // If we have a helper, that means we're in
                    // visual_grid_alternates, and therefore the rc_override
                    // happens within the grid, and not outside of it.
                helper.addRCOverrides(parseRCOverride());
                parseExpected("}");
                break;
            } else if ("{".equals(tok)) {
                result.add(parseVisualGridRow());
            } else {
                lexer.reportError("'{' or '}' expected, " + tok + " seen");
            }
        }
        return result;
    }

    private ArrayList<VisualRCHandlerCell> parseVisualGridRow() 
            throws IOException 
    {
        ArrayList<VisualRCHandlerCell> result = new ArrayList();
        for (;;) {
            String tok = lexer.getString();
            VisualRCHandlerCell cell = null;
            if (tok == null) {
                lexer.reportError("EOF unexpected in string list");
            } else if ("}".equals(tok)) {
                break;
            } else if ("(".equals(tok)) {
                int x = lexer.getInt();
                int y = lexer.getInt();
                cell = VisualRCHandlerCell.newLocationRef(x, y);
                if (cell == null)  {
                    lexer.reportError("Invalid cell address ( " + x + " "
                                      + y + " )");
                }
                parseExpected(")");
            } else if ("[".equals(tok)) {
                String name = lexer.getString();
                parseExpected("]");
                cell = VisualRCHandlerCell.newStateRef(name);
            } else if ("<activate>".equals(tok)) {
                cell = VisualRCHandlerCell.newActivate();
            } else if ("<wall>".equals(tok)) {
                cell = VisualRCHandlerCell.newWall();
            } else if ("<null>".equals(tok)) {
                cell = VisualRCHandlerCell.newNull();
            } else {
                cell = VisualRCHandlerCell.newState(tok);
            }
            result.add(cell);
        }
        return result;
    }

    //
    // The key is a string of the form "<direction>:<statename>", as
    // in "up:close".  The value is the name of the state to go to with 
    // that keypress, or the special value "<activate>"
    //
    private Map<String, String> parseRCOverride() throws IOException {
        Map<String, String> result = new HashMap<String, String>();
        parseExpected("{");
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            } else if (!("{".equals(tok))) {
                lexer.reportError("\"{\" expected, \"" + tok + "\" seen");
            }
            String fromState = lexer.getString();
            String direction = lexer.getString();
            String toState = lexer.getString();
            parseExpected("}");
            if (!("up".equals(direction) || "down".equals(direction)
                  || "left".equals(direction) || "right".equals(direction))) 
            {
                lexer.reportError("Direction must be up, down, right or left, "
                                  + "not \"" + direction + "\".");
            }
            result.put(direction + ":" + fromState, toState);
        }
        return result;
    }

    // Return value [0] is list of parts, [1] is list of commands lists.
    Object[] parseVisualActions(Map<String, Integer> states, 
                                boolean hasAssembly) 
                throws IOException 
    {
        String[] parts = null;
        Command[][] commands = null;
        parseExpected("{");
        String tok = lexer.getString();
        for (;;) {
            if ("}".equals(tok)) {
                break;
            }
            String stateName = tok;
            Integer state = states.get(stateName);
            if (state == null) {
                lexer.reportError("State " + tok + " not found");
            }
            tok = lexer.getString();
            if (!("{".equals(tok)))     {       // If not command list
                if (!hasAssembly) {
                    lexer.reportError("No assembly specified");
                }
                if (parts == null) {
                    parts = new String[states.size()];
                }
                if (parts[state.intValue()] != null) {
                    lexer.reportError("State " + stateName
                                      + " has duplicate assembly parts");
                }
                parts[state.intValue()] = tok;
                tok = lexer.getString();
            }
            if ("{".equals(tok)) {              // If command list
                if (commands == null) {
                    commands = new Command[states.size()][];
                }
                if (commands[state.intValue()] != null) {
                    lexer.reportError("State " + stateName
                                      + " has duplicate commands");
                }
                commands[state.intValue()] = parseCommandsNoOpenBrace();
                tok = lexer.getString();
            }
        }
        return new Object[] { parts, commands };
    }

    // Return value alternates Integer state # and Rectangle
    private Vector parseMouseLocations(Map<String, Integer> states) 
                throws IOException 
    {
        parseExpected("{");
        Vector result = new Vector();
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            }
            Integer state = states.get(tok);
            if (state == null || state.intValue() == -1) {
                lexer.reportError("State " + tok + " not found");
            }
            result.add(state);
            result.add(parseRectangle());
        }
        return result;
    }

    private Rectangle parseRectangle() throws IOException {
        parseExpected("(");
        return parseRectangleNoLeading();
    }

    private Rectangle parseRectangleNoLeading() throws IOException {
        int x1 = lexer.getInt();
        int y1 = lexer.getInt();
        int x2 = lexer.getInt();
        int y2 = lexer.getInt();
        if (x2 < x1 ||  y2 < y1) {
            lexer.reportError("Second coordinates must be lower-right corner");
        }
        parseExpected(")");
        return new Rectangle(x1, y1, 1+x2-x1, 1+y2-y1);
    }

    private int findInMatrix(String target, String[][] matrix) {
        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[y].length; x++) {
                if (target.equals(matrix[y][x])) {
                    return y * matrix[0].length + x;
                }
            }
        }
        return -1;
    }

    private void parseCommandRCHandler(boolean wantsPressed) throws IOException {
        int lineStart = lexer.getLineNumber();
        String handlerName = lexer.getString();
        int mask = parseRCKeyList();
        parseExpected("execute");
        Command[] commands = parseCommands();
        parseExpected(";");
        builder.addRCHandler(handlerName, lineStart,
                 new SECommandRCHandler(handlerName, mask, wantsPressed, commands));
    }

    //
    // Returns the key mask of the set of keys
    //
    private int parseRCKeyList() throws IOException {
        parseExpected("{");
        int mask = 0;
        for (;;) {
            String tok = lexer.getString();
            RCKeyEvent e = null;
            if ("}".equals(tok)) {
                break;
            }
            if (tok != null) {
                e = RCKeyEvent.getKeyByName(tok);
            }
            if (e == null) {
                lexer.reportError("Unexpected token " + tok);
            }
            mask |= e.getBitMask();
        }
        return mask;
    }

    /**
     * Parse a list of commands
     **/
    public Command[] parseCommands() throws IOException {
        parseExpected("{");
        return parseCommandsNoOpenBrace();
    }

    /**
     * Parse a list of commands after the leading "{" has already been
     * read
     **/
    public Command[] parseCommandsNoOpenBrace() throws IOException {
        Vector v = new Vector();
        for (;;) {
            String tok = lexer.getString();
            int lineStart = lexer.getLineNumber();
            if ("}".equals(tok)) {
                break;
            } else {
                Command c = parseCommand(tok);
                v.addElement(c);
                builder.addCommand(c, lineStart);
            }
        }
        int num = v.size();
        Command[] result = new Command[num];
        for (int i = 0; i < num; i++) {
            result[i] = (Command) v.elementAt(i);
        }
        return result;
    }
   
    //
    // Parse a command.  The first token is passed in as argument, and
    // remaining tokens are pulled from the lexer.
    //
    private Command parseCommand(String tok) throws IOException {
        if ("activate_segment".equals(tok)) {
            return parseActivateSegment();
        } else if ("activate_part".equals(tok)) {
            return parseActivatePart();
        } else if ("segment_done".equals(tok)) {
            return parseSegmentDone();
        } else if ("invoke_assembly".equals(tok)) { // deprecated
            return parseInvokeAssembly();
        } else if ("set_visual_rc".equals(tok)) {
            return parseVisualRC();
        } else if ("reset_feature".equals(tok)) {
            return parseResetFeature();
        } else if ("sync_display".equals(tok)) {
            return parseSyncDisplay();
        } else if ("run_named_commands".equals(tok)) {
            return parseNamedCommands();
        } else if ("java_command".equals(tok)) {
            return parseJavaCommand();
        } else if (extParser == null || tok == null || tok.indexOf(':') < 0) {
            lexer.reportError("command expected, " + tok + " seen");
            return null;
        } else {
            String typeName = tok;
            Command result = extParser.getCommand(show, typeName, lexer);
            if (result == null) {
                lexer.reportError("command expected, " + tok + " seen");
            }
            return result;
        }
    }

    private Command parseActivateSegment() throws IOException {
        final String name = lexer.getString();
        final boolean pop = "<pop>".equals(name);
        String tok = lexer.getString();
        final boolean push = !pop && "<push>".equals(tok);
        if (push) {
            tok = lexer.getString();
        }
        if (!(";".equals(tok))) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");
        }
        final SEActivateSegmentCommand cmd 
                = new SEActivateSegmentCommand(show, push, pop);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                if (!pop) {
                    Segment s = builder.getNamedSegment(name);
                    if (s == null) {
                        reportError("Segment \"" + name + " not found");
                    } else {
                        cmd.setup(s);
                    }
                }
                if (push || pop) {
                    if (show.getSegmentStackDepth() <= 0) {
                        reportError("Segment stack depth is 0");
                    }
                }
            }
        };
        deferred.get(3).add(fw);
        return cmd;
    }

    private Command parseActivatePart() throws IOException {
        final String assemblyName = lexer.getString();
        final String partName = lexer.getString();
        parseExpected(";");
        final SEActivatePartCommand cmd = new SEActivatePartCommand(show);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Assembly a = lookupAssemblyOrFail(assemblyName);
                Feature f = a.findPart(partName);
                if (f == null) {
                    reportError("Assembly part \"" + partName + " not found");
                }
                cmd.setup(a, f);
            }
        };
        deferred.get(3).add(fw);
        return cmd;
    }

    private Command parseSegmentDone() throws IOException {
        parseExpected(";");
        return new SESegmentDoneCommand(show);
    }

    private Command parseInvokeAssembly() throws IOException {
        lexer.reportWarning("Deprecated invoke_assembly command");
        int r = 0;
        int c = 0;
        String tok = lexer.getString();
        if ("selected_cell".equals(tok)) {
            r = -1;
            c = -1;
        } else if ("cell".equals(tok)) {
            r = lexer.getInt();
            c = lexer.getInt();
            if (r < 0 || c < 0) {
                lexer.reportError("Negative value for row or column");
            }
        } else {
            lexer.reportError("Unexpected token:  " + tok);
        }
        final String handlerName = lexer.getString();
        final int row = r;
        final int column = c;
        parseExpected(";");
        final SESetVisualRCStateCommand cmd = new SESetVisualRCStateCommand(show);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                RCHandler h = builder.getNamedRCHandler(handlerName);
                if (h == null || !(h instanceof VisualRCHandler)) {
                    reportError("Handler not found or wrong type ");
                }
                VisualRCHandler handler = (VisualRCHandler) h;
                int state = -1;
                if (row > -1) {
                    VisualRCHandlerHelper helper 
                        = visualRCHelpers.get(handlerName);
                    state = helper.getState(0, column, row);
                    if (state == -1) {
                        reportError("Illegal cell - doesn't refer to state");
                    }
                }
                cmd.setup(true, state, handler, false, -1);
            }
        };
        deferred.get(0).add(fw);
        return cmd;
    }

    private Command parseVisualRC() throws IOException {
        final String handlerName = lexer.getString();
        String tok = lexer.getString();
        String state = null;
        if ("current".equals(tok)) {
            state = null;
        } else if ("state".equals(tok)) {
            state = lexer.getString();
        } else {
            lexer.reportError("Unexpected token:  " + tok);
        }
        tok = lexer.getString();
        boolean activate = false;
        if ("selected".equals(tok)) {
            activate = false;
        } else if ("activated".equals(tok)) {
            activate = true;
        } else {
            lexer.reportError("Unexpected token:  " + tok);
        }
        
        boolean runCommands = false;
        tok = lexer.getString();
        String gridAlternate = null;
        if ("grid_alternate".equals(tok)) {
            gridAlternate = lexer.getString();
            tok = lexer.getString();
        }
        if ("run_commands".equals(tok)) {
            runCommands = true;
            tok = lexer.getString();
        }
        if (!";".equals(tok)) {
           lexer.reportError("\";\" expected, \"" + tok + "\" seen");            
        }
        final SESetVisualRCStateCommand cmd = new SESetVisualRCStateCommand(show);
        final String stateF = state;
        final boolean activateF = activate;
        final boolean runCommandsF = runCommands;
        final String gridAlternateF = gridAlternate;
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                RCHandler h = builder.getNamedRCHandler(handlerName);
                if (h == null || !(h instanceof VisualRCHandler)) {
                    reportError("Handler not found or wrong type ");
                }
                VisualRCHandler handler = (VisualRCHandler) h;
                int state = -1;
                if (stateF != null)  {
                    state = handler.lookupState(stateF);
                    if (state == -1) {
                        lexer.reportError("State \"" + stateF + "\" not found");
                    }
                }
                int ga = -1;
                if (gridAlternateF != null) {
                    ga = handler.lookupGrid(gridAlternateF);
                    if (ga == -1) {
                        lexer.reportError("Grid alternate \"" + gridAlternateF
                                           + "\" not found");
                    }
                }
                cmd.setup(activateF, state, handler, runCommandsF, ga);
            }
        };
        deferred.get(0).add(fw);
        return cmd;
    }
    
    private Command parseResetFeature() throws IOException {
        final String featureName = lexer.getString();
        parseExpected(";");
        final SEResetFeatureCommand cmd = new SEResetFeatureCommand(show);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Feature f = lookupFeatureOrFail(featureName);
                cmd.setFeature(f);
            }
        };
        deferred.get(0).add(fw);
        return cmd;
    }

    private Command parseSyncDisplay() throws IOException {
        parseExpected(";");
        return new SESyncDisplayCommand(show);
    }

    private Command parseNamedCommands() throws IOException {
        final String name = lexer.getString();
        parseExpected(";");
        final SERunNamedCommand cmd = new SERunNamedCommand(show);
        ForwardReference fw = new ForwardReference(lexer) {
            public void resolve() throws IOException {
                Command target = builder.getNamedCommand(name);
                if (target == null) {
                    reportError("Can't find named command \"" + name + "\"");
                }
                cmd.setTarget(target);
            }
        };
        deferred.get(0).add(fw);
        return cmd;
    }

    
    private Command parseJavaCommand() throws IOException {
        SEShowCommands cmds = show.getShowCommands();
        if (cmds.getClassName() == null) {
            lexer.reportError("java_command seen, but java_generated_class not set");
        }
        int lineStart = lexer.getLineNumber();
        parseExpected("[[");
        StringBuffer xletBody = new StringBuffer();
        StringBuffer grinviewBody = new StringBuffer();
        StringBuffer originalSource = new StringBuffer();
        originalSource.append("java_command ");
        originalSource.append("[[");
        SEShowCommand result = cmds.addNewCommand();
        readJavaSource(xletBody, grinviewBody, originalSource, result);
        result.setXletMethodBody(xletBody.toString());
        result.setGrinviewMethodBody(grinviewBody.toString());
        result.setOriginalSource(originalSource.toString());
        builder.addCommand(result, lineStart);
        return result;
    }


    private void finishBuilding() throws IOException {
        for (int i = 0; i < deferred.size(); i++) {
            ArrayList<ForwardReference> list = deferred.get(i);
            for (int j = 0; j < list.size(); j++) {
                ForwardReference fw = list.get(j);
                fw.resolveAtLine();
            }
        }

        builder.finishBuilding();
        if (EasingEquation.framesAdded > 0) {
            System.out.println(EasingEquation.framesAdded 
                        + " animation keyframes added for easing functions.");
        }
    }

    //***************    Convenience Methods    ******************

    private SubFeature[] parsePartsList() throws IOException {
        parseExpected("{");
        ArrayList<SubFeature> partsList = new ArrayList<SubFeature>();
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            } 
            partsList.add(parseSubFeature(tok));
        }
        return partsList.toArray(new SubFeature[partsList.size()]);
    }

    private SubFeature parseSubFeature(String tok) throws IOException {
        if ("}".equals(tok)) {
            lexer.reportError("\"}\" unexpected");
        } else if (tok == null) {
            lexer.reportError("Unexpected EOF");
        } else if ("sub_feature".equals(tok)) {
            // inline feature
            return new SubFeature(parseFeature(false, lexer.getLineNumber()), 
                                  lexer);
        } else {
            // reference to named feature
            return new SubFeature(builder, tok, lexer);
        }
        return null;    // not reached
    }

    private Feature[] makeFeatureList(SubFeature[] parts) throws IOException {
        Feature[] result = new Feature[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parts[i].getFeature();
        }
        return result;
    }

    private RCHandler[] makeRCHandlerList(String[] names) throws IOException {
        RCHandler[] result = new RCHandler[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = builder.getNamedRCHandler(names[i]);
            if (result[i] == null) {
                lexer.reportError("RC handler \"" + names[i] + "\" not found");
            }
        }
        return result;
    }

    private Feature[] lookupFeatureGrid(Assembly assembly, String[][] grid,
                                        int width, int height) 
            throws IOException 
    {
        Feature[] result = new Feature[width * height];
        int i = 0;
        for (int y = 0; y < height; y++) {
            String[] names = grid[y];
            for (int x = 0; x < width; x++) {
                result[i] = assembly.findPart(names[x]);
                if (result[i] == null) {
                    lexer.reportError("Assembly part \"" + names[x] 
                                      + "\" not found");
                }
                i++;
            }
        }
        return result;
    }

    /**
     * Look up the parts of an assembly.  This can be called from a
     * forward reference.
     *
     * @see ForwardReference
     **/
    public Feature[] lookupAssemblyParts(Assembly assembly, String[] parts)
            throws IOException 
    {
        if (parts == null) {
            return null;
        }
        Feature[] result = new Feature[parts.length];
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                result[i] = null;
            } else {
                result[i] = assembly.findPart(parts[i]);
                if (result[i] == null) {
                    lexer.reportError("Assembly part \"" + parts[i]
                                      + "\" not found");
                }
            }
        }
        return result;
    }

    /**
     * Look up a named feature.  Fail with an IOException if it's not found.
     * This can be called from a forward reference.
     *
     * @see ForwardReference
     **/
    public Feature lookupFeatureOrFail(String name) throws IOException {
        Feature f = builder.getNamedFeature(name);
        if (f == null) {
            lexer.reportError("Feature " + name + " not found,");
        }
        return f;
    }


    /**
     * Look up a named assembly.  Fail with an IOException if it's not found.
     * This can be called from a forward reference.
     *
     * @see ForwardReference
     **/
    public Assembly lookupAssemblyOrFail(String name) throws IOException {
        Feature f = lookupFeatureOrFail(name);
        if (f == null || !(f instanceof Assembly)) {
            lexer.reportError("Feature " + name + " is not an assembly");
        }
        return (Assembly) f;
    }


    //***************    BASIC TYPES  ************************

    /** 
     * Parse a 2-D matrix of strings.  It's not required to be
     * rectangular; each row may have a different length.
     **/
    public String[][] parseMatrix() throws IOException {
        Vector v = new Vector();
        parseExpected("{");
        for (;;) {
            String tok = lexer.getString();
            if ("}".equals(tok)) {
                break;
            } else if ("{".equals(tok)) {
                String[] el = parseStringsWithOpenBraceRead();
                v.addElement(el);
            } else {
                lexer.reportError("'{' or '}' expected, " + 
                                  tok + " seen");
            }
        }
        int num = v.size();
        String[][] result = new String[num][];
        for (int i = 0; i < num; i++) {
            result[i] = (String[]) v.elementAt(i);
        }
        return result;
    }

    /**
     * Parse a list of strings
     **/
    public String[] parseStrings() throws IOException {
        parseExpected("{");
        return parseStringsWithOpenBraceRead();
    }

    /**
     * Parse a list of strings, without reading the leading open-brace.
     **/
    public String[] parseStringsWithOpenBraceRead() throws IOException {
        Vector v = new Vector();
        for (;;) {
            String tok = lexer.getString();
            if (tok == null) {
                lexer.reportError("EOF unexpected in string list");
            } else if ("}".equals(tok)) {
                break;
            } else {
                v.addElement(tok);
            }
        }
        int num = v.size();
        String[] result = new String[num];
        for (int i = 0; i < num; i++) {
            result[i] = (String) v.elementAt(i);
        }
        return result;
    }

    /** 
     * Parse a boolean value
     **/
    public boolean parseBoolean() throws IOException {
        String tok = lexer.getString();
        if ("true".equals(tok)) {
            return true;
        } else if ("false".equals(tok)) {
            return false;
        } else {
            lexer.reportError("\"true\" or \"false\" expected, \"" + tok 
                                + "\" seen");
            return false;
        }
    }

    /**
     * Parse a color representation ("{ r g b a }")
     **/
    public Color parseColor() throws IOException {
        parseExpected("{");
        return parseColorNoOpenBrace();
    }

    /**
     * Parse a color representation when the opening brace has already
     * been read.
     *
     * @see #parseColor()
     **/
    public Color parseColorNoOpenBrace() throws IOException {
        int r = lexer.getInt();
        checkColorValue("r", r);
        int g = lexer.getInt();
        checkColorValue("g", g);
        int b = lexer.getInt();
        checkColorValue("b", b);
        int a = lexer.getInt();
        checkColorValue("a", a);
        parseExpected("}");
        return AssetFinder.getColor(r, g, b, a);
    }

    private void checkColorValue(String name, int value) throws IOException {
        if (value < 0 || value > 255) {
            throw new IOException("Illegal color value for " + name + ":  "
                                  + value);
        }
    }

    
    /**
     * Parses a token that we expect to see.  A token is read, and
     * if it's not the expected token, an IOException is generated.
     * This can be useful for things like parsing the ";" at the
     * end of various constructs.
     **/
    public void parseExpected(String expected) throws IOException {
        lexer.parseExpected(expected);
    }
    
    /**
     * Read a java_source fragment.  This is terminated with a "]]", which
     * is consumed.  This recognizes the special sequences
     * XLET_ONLY_[[ java_source ]],  GRINVIEW_ONLY_[[ java_source ]]
     * and GRIN_COMMAND_[[ f ]]
     **/
    public void readJavaSource(StringBuffer xletSource, 
                               StringBuffer grinviewSource,
                               StringBuffer originalSource,
                               SEShowCommand command)
                       throws IOException
    {
        for (;;) {
            String src = lexer.getStringExact();
            originalSource.append(src);
            if (src == null) {
                lexer.reportError("EOF unexpected");
            } else if ("]]".equals(src)) {
                return;
            } else if ("XLET_ONLY_[[".equals(src)) {
                readJavaSource(xletSource, null, originalSource, command);
            } else if ("GRINVIEW_ONLY_[[".equals(src)) {
                readJavaSource(null, grinviewSource, originalSource, command);
            } else {
                if ("GRIN_COMMAND_[[".equals(src)) {
                    if (command == null) {
                        lexer.reportError("GRINVIEW_COMMAND_[[ unexpected");
                    }
                    
                    String cmdStr = lexer.getString();
                    originalSource.append(' ');
                    originalSource.append(cmdStr);
                    originalSource.append(' ');
                    int lineStart = lexer.getLineNumber();
                    Command cmd = parseCommand(cmdStr);
                    builder.addCommand(cmd, lineStart);
                    parseExpected("]]");
                    originalSource.append(" ]]");
                    src = command.addSubCommand(cmd);
                }
                if (xletSource != null) {
                    xletSource.append(src);
                }
                if (grinviewSource != null) {
                    grinviewSource.append(src);
                }
            }
        }
    }
    
    /**
     * A utility method to convert text-based grin file to an
     * SEShow object.  Before calling this method, AssetFinder
     * should be set with a proper search path for assets.
     * <p>
     * On a parse error, this prints an error message to stderr,
     * then aborts execution.  If you want the IOException yourself,
     * then call parseShowNoAbort().
     * 
     * @param showName The name of the show text file to parse.
     * @param director The director to use for the recreated show,
     * could be null.
     * @param builder The show builder object with the extension parser
     * being set, could be null.
     * 
     * @return the SEShow object that got reconstructed.
     *
     * @see #parseShowNoAbort(String, Director, ShowBuilder)
     */
    public static SEShow parseShow(String showName, 
                                   Director director, ShowBuilder builder) 
    {
        try {
            return parseShowNoAbort(showName, director, builder);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println();
            System.err.println("Error trying to parse " + showName + ":");
            System.err.println("    " + e.getMessage());
            System.err.println();
            System.err.println("See the stack trace above for more details.");
            System.err.println("Aborting.");
            System.err.println();
            System.exit(1);
            return null;        // not reached
        }
    }

    /**
     * A utility method to convert text-based grin file to an
     * SEShow object.  Before calling this method, AssetFinder
     * should be set with a proper search path for assets.
     * <p>
     * On a parse error, this throws an IO Exception, and doesn't  print
     * anything to stderr.  If you want a more friendly error message,
     * try parseShow().
     * 
     * @param showName The name of the show text file to parse.
     * @param director The director to use for the recreated show,
     * could be null.
     * @param builder The show builder object with the extension parser
     * being set, could be null.
     * 
     * @return the SEShow object that got reconstructed.
     *
     * @see #parseShow(String, Director, ShowBuilder)
     */
    public static SEShow parseShowNoAbort(String showName, 
                                     Director director, ShowBuilder builder) 
               throws IOException 
    {
        BufferedReader rdr = null;
        SEShow show = new SEShow(director);
        IOException ioe = null;

        try {
            URL source = AssetFinder.getURL(showName);
            if (source == null) {
                throw new IOException("Can't find resource " + showName);
            }
            rdr = new BufferedReader(
                    new InputStreamReader(source.openStream(), "UTF-8"));

            ShowParser parser = new ShowParser(rdr, showName, show, builder);
            parser.parse(); // populates show
            rdr.close();
        } finally {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (IOException ex) {
                }
            }
        }
        return show;
    }

}
