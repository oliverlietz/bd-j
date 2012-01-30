
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

package com.hdcookbook.grin.io;


import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SESegment;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Group;
import com.hdcookbook.grin.features.InterpolatedModel;
import com.hdcookbook.grin.features.SEGroup;
import com.hdcookbook.grin.features.SEInterpolatedModel;
import com.hdcookbook.grin.features.SEScalingModel;
import com.hdcookbook.grin.features.SETimer;
import com.hdcookbook.grin.features.SETranslatorModel;
import com.hdcookbook.grin.input.RCHandler;
import com.hdcookbook.grin.io.builders.DeferredBuilder;
import com.hdcookbook.grin.io.builders.FontSpec;
import com.hdcookbook.grin.io.text.ExtensionParser;
import com.hdcookbook.grin.util.IndexedSet;

import java.io.IOException;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Pattern;


/**
 * A helper class for parsing a show.  Clients of the parser can
 * subclass this to intercept items as they are encountered.
 *
 * @author Bill Foote (http://jovial.com)
 */
public class ShowBuilder {
   
    protected SEShow show;

    private Map<String, Segment> namedSegments = new HashMap<String, Segment>();
    private Map<String, Feature> namedFeatures = new HashMap<String, Feature>();
    private Map<String, RCHandler> namedRCHandlers 
                = new HashMap<String, RCHandler>();
    private Map<String, Command> namedCommands
                = new HashMap<String, Command>();

    private List<Segment> allSegments = new ArrayList<Segment>();
    private List<Feature> allFeatures = new ArrayList<Feature>();
    private List<RCHandler> allRCHandlers = new ArrayList<RCHandler>();
    private ArrayList<SENode> allNodes = new ArrayList<SENode>();
        // Contains all segments, features, RC handlers and commands

    private List<String> exportedSegments = null;
    private List<String> exportedFeatures = null;
    private List<String> exportedRCHandlers = null;
    private List<String> exportedNamedCommands = null;

    private List<DeferredBuilder> deferredBuilders
        = new ArrayList<DeferredBuilder>();

    private String[] stickyImages = null;
    private String binaryGrinFileName = null;

    private ExtensionParser extensionParser;
    private SEGroup   showTopGroup      = null;
    private SESegment showTop           = null;

    private boolean noMoreNodes = false;

    private IndexedSet<FontSpec> fonts = new IndexedSet<FontSpec>();
    
    public ShowBuilder() {
    }

    public SEShow getShow() {
        return show;
    }

    public void init(SEShow show) {
        this.show = show;
    }    

    public void setExtensionParser(ExtensionParser parser) {
        this.extensionParser = parser;
    }
    
    /**
     * Returns an instance of ExtensionParser that this Builder is working with,
     * or null if it is not set.
     */
    public ExtensionParser getExtensionParser() {
        return extensionParser;
    }

    /**
     * Add a feature that's created programmatically during show construction,
     * e.g. from <code>SENode.postProcess()</code>.
     *
     * @see SENode#postProcess(ShowBuilder)
     **/
    public void addSyntheticFeature(Feature f) throws IOException {
        addFeature(null, 0, f);
    }

    /**
     * Add a feature that's created programmatically during show construction,
     * and inject it as the parent of the child node passed in.  The entire
     * show tree will be visited, and references to the child will be
     * changed to references to the parent where appropriate.
     *
     * @param newFeature        The new feature being added
     * @param child             The existing feature that will be its child
     *
     * @throws IOException      if a problem is encountered
     **/
    public void injectParent(Feature newFeature, Feature child) 
            throws IOException
    {
        for (int i = 0; i < allNodes.size(); i++) {
            allNodes.get(i).changeFeatureReference(child, newFeature);
        }
        addSyntheticFeature(newFeature);        // adds to allNodes
    }

    private void addNode(SENode node) throws IOException {
        if (noMoreNodes) {
            throw new IOException("Can't add new node:  The show's build phase is over.");
        }
        if (node == null) {
            throw new NullPointerException();
        }
        allNodes.add(node);
    }

    /** 
     * Called when a new feature is encountered.
     **/
    public void addFeature(String name, int line, Feature f) throws IOException
    {
        if (name != null) {
            if (namedFeatures.get(name) != null) {
                throw new IOException("Feature named \"" + name
                                       + "\" already exists.");
            }
            namedFeatures.put(name, f);
        }
        allFeatures.add(f);
        addNode((SENode) f);
    }

    /**
     * Called when a new segment is encountered.
     **/
    public void addSegment(String name, int line, Segment s) throws IOException
    {
        if (name != null) {
            if (namedSegments.get(name) != null) {
                throw new IOException("Segment named \"" + name
                                       + "\" already exists.");
            }
            namedSegments.put(name, s);
        }
        allSegments.add(s);
        addNode((SENode) s);
    }

    /**
     * Called when a new named command is encountered
     **/
    public void addNamedCommand(String name, int line, Command c) 
                throws IOException
    {
        if (namedCommands.get(name) != null) {
            throw new IOException("Named command \"" + name
                                       + "\" already exists.");
        }
        namedCommands.put(name, c);
    }

    /**
     * Called when a new command is encountered.
     **/
    public void addCommand(Command command, int line) throws IOException {
        addNode((SENode) command);
    }

    /**
     * Called when a new remote control handler is encountered.
     **/
    public void addRCHandler(String name, int line, RCHandler hand) 
                        throws IOException
    {
        if (name != null) {
            if (namedRCHandlers.get(name) != null) {
                throw new IOException("RC Handler named \"" + name
                                       + "\" already exists.");
            }
            namedRCHandlers.put(name, hand);
        }
        allRCHandlers.add(hand);
        addNode((SENode) hand);
    }

    /**
     * Called when a DeferrredBuilder is created.  It will be called
     * after the show is populated with all of its parts, in the
     * finishBuilding method.
     *
     * @see #finishBuilding()
     **/
    public void addDeferredBuilder(DeferredBuilder builder) {
        deferredBuilders.add(builder);
    }

    /**
     * Called when the exported clause is encountered.  This is optional;
     * if it's not called, then everything defaults to public visibility.
     * <p>
     * The segments, features and handlers may contain the wildcard
     * character "*", which cannot be escaped.
     **/
    public void setExported(String[] segments, String[] features, 
                            String[] handlers, String[] namedCommands) 
                throws IOException
    {
        if (exportedSegments != null) {
            throw new IOException("Multiple exported clauses");
        }

        exportedSegments = Arrays.asList(segments);
        exportedFeatures = Arrays.asList(features);
        exportedRCHandlers = Arrays.asList(handlers);
        exportedNamedCommands = Arrays.asList(namedCommands);
    }

    /**
     * Called when a "binary_grin_file_setting" clause is enocuntered.
     **/
    public void setBinaryGrinFileName(String fileName) {
        this.binaryGrinFileName = fileName;
    }

    /**
     * Called when a "sticky images" clause is enocuntered.
     **/
    public void setStickyImages(String[] stickyImages) {
        this.stickyImages = stickyImages;
    }
    
    /**
     * Called when a "showtop_group" clause is encountered.
     */
    public void setShowTopGroup(SEGroup showTopGroup) 
        throws IOException {
        if (this.showTopGroup != null) {
            throw new IOException("Multiple showtop_group clauses");
        }
        this.showTopGroup = showTopGroup;
    }
     /**
     * Called when a "show_top" clause is encountered.
     */   
    public void setShowTop(String showTopName) throws IOException {
        if (this.showTop != null) {
            throw new IOException("Multiple show_top clauses");
        } 
        this.showTop = makeShowTopSegment(namedFeatures.get(showTopName));
    }
    
    private SEGroup makeShowTopGroup() throws IOException {
        SEGroup g = new SEGroup(show);
        g.setParts(new Feature[0]);
        allFeatures.add(g);
        addNode(g);
        return g;
    }
    
    private SESegment makeShowTopSegment(Feature showTopFeature) throws IOException {
        Feature[] features = new Feature[] { showTopFeature };
        SESegment segment = new SESegment(" $show_top$ ", features, features,
                new RCHandler[0], new Command[0], false, new Command[0]);
        allSegments.add(segment);
        addNode((SENode) segment);
        return segment;
    }

    /** 
     * Look up a segment in the list of all named segments.
     **/
    public Segment getNamedSegment(String name) {
        return namedSegments.get(name);
    }

    /**
     * Look up a command in the list of all named commands.
     **/
    public Command getNamedCommand(String name) {
        return namedCommands.get(name);
    }

    /** 
     * Look up a feature in the list of all named features.
     **/
    public Feature getNamedFeature(String name) {
        return namedFeatures.get(name);
    }

    /** 
     * Look up an RC handler in the list of all named handlers.
     **/
    public RCHandler getNamedRCHandler(String name) {
        return namedRCHandlers.get(name);
    }

    /**
     * Called when the show has finished parsing and all forward references
     * have been resolved.  Any DeferredBuider instances are processed here.
     **/
    public void finishBuilding() throws IOException {
                    
        if ((showTop == null) != (showTopGroup == null)) {
            throw new IOException("show_top and showtop_group should both be set or unset.");
        }    
        
        if (showTop == null) {
            showTopGroup = makeShowTopGroup();
            showTop = makeShowTopSegment(showTopGroup);
        }

        // In the following iteration, we specifically use the ArrayList
        // methods, because they guarantee that elements that are added
        // to the list during the iteration will be visited.
        for (int i = 0; i < allNodes.size(); i++) {
            allNodes.get(i).postProcess(this);
        }
        
        noMoreNodes = true;
            // This prevents any new nodes from being added to the
            // show tree.

        Segment[] segments 
            = allSegments.toArray(new Segment[allSegments.size()]);
        Feature[] features
            = allFeatures.toArray(new Feature[allFeatures.size()]);
        RCHandler[] rcHandlers
            = allRCHandlers.toArray(new RCHandler[allRCHandlers.size()]);
        Hashtable publicSegments 
                = findPublic(namedSegments, namedSegments.keySet(),
                             exportedSegments, "Segment");
        Hashtable publicFeatures 
                = findPublic(namedFeatures, namedFeatures.keySet(),
                             exportedFeatures, "Feature");
        Hashtable publicRCHandlers 
                = findPublic(namedRCHandlers, namedRCHandlers.keySet(),
                             exportedRCHandlers, "RC Handler");
        Hashtable publicNamedCommands 
                = findPublic(namedCommands, namedCommands.keySet(),
                             exportedNamedCommands, "Named Command");
        {
            Collection<Command> v = namedCommands.values();
            Command[] a = (Command[]) v.toArray(new Command[v.size()]);
            show.setNamedCommands(a);
        }
        
        FontSpec[] fontArray = fonts.toArray(FontSpec.class);
        String[] fontName = new String[fontArray.length];
        int[] fontStyleSize = new int[fontArray.length];
        for (int i = 0; i < fontArray.length; i++) {
            FontSpec s = fontArray[i];
            fontName[i] = s.name;
            fontStyleSize[i] = s.style + (s.size << 2);
        }

        show.buildShow(segments, features, rcHandlers, stickyImages, 
                       showTop, (Group)showTopGroup, 
                       publicSegments,
                       publicFeatures, publicRCHandlers, publicNamedCommands,
                       fontName, fontStyleSize);
        
        if (binaryGrinFileName != null) {
            show.setBinaryGrinFileName(binaryGrinFileName);
        }
        for (DeferredBuilder builder : deferredBuilders) {
            builder.finishBuilding(show);
        }
    }

    private Hashtable findPublic(Map namedThings, Set<String> names,
                                 List<String> exportedThings, String thingName) 
                throws IOException 
    {
        Hashtable result = new Hashtable();
        if (exportedThings == null) {
            result.putAll(namedThings);
        } else {
            List<Pattern> patterns = new ArrayList<Pattern>();
            for (String pat : exportedThings) {
                patterns.add(convertWildcard(pat));
            }
            for (String key : names) {
                boolean found = false;
                for (Pattern pat : patterns) {
                    if (pat.matcher(key).matches()) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    Object value = namedThings.get(key);
                    assert value != null;
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    private Pattern convertWildcard(String wildcard) {
        StringBuilder pat = new StringBuilder();
        for (int i = 0; i < wildcard.length(); i++) {
            char ch = wildcard.charAt(i);
            if (ch == '*') {
                pat.append(".*");
            } else if (ch == '?') {
                pat.append(".");
            } else if (ch == '[') {
                int pos = wildcard.indexOf("]", i);
                String range = null;
                if (pos > -1) {
                    range = wildcard.substring(i, pos + 1);
                }
                if (range.indexOf("[", 1) != -1) {
                    // Can't handle '[' inside a '[' ']' pair)
                    pos = -1;
                }
                if (pos == -1)  {
                    // If we don't have a valid set of characters
                    pat.append(Pattern.quote("" + ch));
                } else {
                    pat.append(range);
                    i += range.length() - 1;
                }
            } else {
                pat.append(Pattern.quote("" + ch));
            }
        }
        return Pattern.compile(pat.toString());
    }

    public InterpolatedModel makeTimer(String name, int numFrames, 
                                       boolean repeat, Command[] commands)
    {
        int[] frames = new int[] { 0,  numFrames-1 };
                // That means keyframes from 0 through numFrames-1, which is
                // a total of numFrames frames.  For example, a timer that's
                // one frame long runs from frame 0 through frame 0.
        int[][] values = new int[0][];
        int repeatFrame;
        if (repeat) {
            repeatFrame = 0;
        } else {
            repeatFrame = Integer.MAX_VALUE;
        }
        int loopCount = 1;
        return makeInterpolatedModel(name, frames, values, repeatFrame,
                                     loopCount, commands, SETimer.class);
            // Timer can be implemented as a degenerate case of 
            // InterpolatedModel.  It's just a model that interpolates zero
            // data values between frame 0 and frame numFrames.
    }

    public SETranslatorModel
    makeTranslatorModel(String name, int[] frames, int[][] values, 
                        boolean isRelative, int repeatFrame, int loopCount, 
                        Command[] commands) 
    {
        SETranslatorModel model = (SETranslatorModel)
               makeInterpolatedModel(name, frames, values, repeatFrame, 
                                     loopCount, commands, 
                                     SETranslatorModel.class);
        model.setIsRelative(isRelative);
        return model;
    }

    public InterpolatedModel 
    makeScalingModel(String name, int[] frames, int[][] values, 
                     int repeatFrame, int loopCount, Command[] commands) 
    {
        return makeInterpolatedModel(name, frames, values, repeatFrame, 
                                     loopCount, commands, SEScalingModel.class);
    }

    public InterpolatedModel 
    makeInterpolatedModel(String name, int[] frames, int[][] values, 
                          int repeatFrame, int loopCount, Command[] commands, 
                          Class clazz)
    {
        int[] currValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            int[] valueList = values[i];
            assert valueList.length > 0;
            boolean allSame = true;
            currValues[i] = valueList[0];
            for (int j = 1; j < valueList.length; j++) {
                allSame = allSame && currValues[i] == valueList[j];
            }
            if (allSame) {
                values[i] = null;
                    // If all of the values are the same, we don't need the
                    // list of values for this parameter.
            }
        }
        SEInterpolatedModel result = null;
        try {
            result = (SEInterpolatedModel) clazz.newInstance();
        } catch (Exception ex) {     // newInstance() throws checked exceptions
            ex.printStackTrace();
            assert false;
        }
        result.setup(show, name, frames, currValues, values, repeatFrame,
                     loopCount, commands);
        return result;
    }

    /**
     * Get the index of the given font spec in the pool of fonts.  If that
     * font hasn't been seen before, add it to the pool and return the index.
     **/
    public int getFontIndex(FontSpec spec) {
        return fonts.getIndex(spec);
    }

}
