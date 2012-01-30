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

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SENode;
import com.hdcookbook.grin.SESegment;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.SEShowCommand;
import com.hdcookbook.grin.SEShowCommands;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.commands.SEActivatePartCommand;
import com.hdcookbook.grin.commands.SEActivateSegmentCommand;
import com.hdcookbook.grin.commands.SEResetFeatureCommand;
import com.hdcookbook.grin.commands.SESegmentDoneCommand;
import com.hdcookbook.grin.commands.SESetVisualRCStateCommand;
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
import com.hdcookbook.grin.features.SEInterpolatedModel;
import com.hdcookbook.grin.features.SEMenuAssembly;
import com.hdcookbook.grin.features.SEScalingModel;
import com.hdcookbook.grin.features.SESetTarget;
import com.hdcookbook.grin.features.SESrcOver;
import com.hdcookbook.grin.features.SEText;
import com.hdcookbook.grin.features.SETimer;
import com.hdcookbook.grin.features.SETranslator;
import com.hdcookbook.grin.features.SETranslatorModel;
import com.hdcookbook.grin.features.parts.SEImagePlacement;
import com.hdcookbook.grin.features.parts.SEImagePlacementList;
import com.hdcookbook.grin.features.parts.SEImageSeqPlacement;
import com.hdcookbook.grin.input.RCHandler;
import com.hdcookbook.grin.input.RCKeyEvent;
import com.hdcookbook.grin.input.SECommandRCHandler;
import com.hdcookbook.grin.input.SEVisualRCHandler;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import com.hdcookbook.grin.io.builders.FeatureRef;
import com.hdcookbook.grin.io.builders.MenuAssemblyHelper;
import com.hdcookbook.grin.io.builders.TranslatorHelper;
import com.hdcookbook.grin.io.builders.VisualRCHandlerCell;
import com.hdcookbook.grin.io.builders.VisualRCHandlerHelper;
import com.hdcookbook.grin.io.text.Lexer;
import com.hdcookbook.grin.io.text.SEGenericCommand;
import com.hdcookbook.grin.io.text.SEGenericFeature;
import com.hdcookbook.grin.io.text.SEGenericModifier;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

/**
 * This class reads Show XML document to objects of JAXB generated classes and 
 * then maps those objects to SENode subclass objects -- which can serialized
 * into GRIN binary format if needed.
 * 
 * @author A. Sundararajan
 */
public class ShowXMLReader {
    public ShowXMLReader(Reader reader) {
        this.reader = reader;
        this.builder = new ShowBuilder();
        this.idToNode = new HashMap<String, SENode>();
        this.featureResolvers = new ArrayList<Resolver>();
        this.rcHandlerResolvers = new ArrayList<Resolver>();
        this.commandResolvers = new ArrayList<Resolver>();
        this.namedCommands = new HashMap<String, Command>();
    }

    public SEShow readShow() throws IOException {
        this.jaxbShow = unmarshall();
        this.show = new SEShow(null);
        getBuilder().init(getShow());

        getShow().setSegmentStackDepth(getJaxbShow().getSegmentStackDepth());

        List<String> drawTargetsList = getJaxbShow().getDrawTargets();
        String[] drawTargets = new String[drawTargetsList.size()];
        drawTargetsList.toArray(drawTargets);
        getShow().setDrawTargets(drawTargets);

        CommandListType jaxbCommands = getJaxbShow().getCommands();
        if (jaxbCommands != null) {
            for (CommandType ct : jaxbCommands.getCommands()) {
                Command cmd = convertCommand(ct);
                if (cmd != null) {
                    String id = ct.getId();
                    if (id != null) {
                        // these named commands can be referred for sub-commands
                        // inside (Java) script command. See GRIN_COMMAND_REF_[[
                        // handling in readJavaSource() method.
                        getNamedCommands().put(id, cmd);
                    }
                }
            }
        }

        List<ScriptType> scripts = getJaxbShow().getScript();
        for (ScriptType st : scripts) {
            handleScript(st, true);
        }

        List<String> exportedFeatures = new ArrayList<String>();
        FeatureListType features = getJaxbShow().getFeatures();
        if (features != null) {
            List<FeatureType> jaxbFeatures = features.getFeatureChoiceGroup();
            for (FeatureType ft : jaxbFeatures) {
                Feature feature = convertFeature(ft);
                String id = ft.getId();
                if (ft.isExport()) {
                    if (id == null) {
                        throw new RuntimeException("exported Feature without id attribute!");
                    }
                    exportedFeatures.add(id);
                }
            }
        }

        List<String> exportedHandlers = new ArrayList<String>();
        RCHandlerListType handlers = getJaxbShow().getRcHandlers();
        if (handlers != null) {
            List<RCHandlerType> jaxbHandlers = handlers.getRcHandler();
            for (RCHandlerType rct : jaxbHandlers) {
                RCHandler rch = convertRCHandler(rct);
                String id = rct.getId();
                if (rct.isExport()) {
                    if (id == null) {
                        throw new RuntimeException("exported RC Handler without id attribute!");
                    }
                    exportedHandlers.add(id);
                }
            }
        }

        List<String> exportedSegments = new ArrayList<String>();
        ShowType.Segments segments = getJaxbShow().getSegments();
        if (segments != null) {
            List<SegmentType> jaxbSegments = segments.getSegment();
            for (SegmentType st : jaxbSegments) {
                Segment segment = convertSegment(st);
                String id = st.getId();
                if (st.isExport()) {
                    if (id == null) {
                        throw new RuntimeException("exported Segment without id attribute!");
                    }
                    exportedSegments.add(id);
                }
            }
        }
        
        ShowType.StickyImages stickyImages = getJaxbShow().getStickyImages();
        if (stickyImages != null) {
            List<String> items = stickyImages.getItem();
            String[] images = new String[items.size()];
            items.toArray(images);
            getBuilder().setStickyImages(images);
        }

        for (Resolver r : getFeatureResolvers()) {
            r.resolve();
        }

        for (Resolver r : getRcHandlerResolvers()) {
            r.resolve();
        }

        for (Resolver r : getCommandResolvers()) {
            r.resolve();
        }

        // add exports
        String[] exportedSegmentNames = new String[exportedSegments.size()];
        exportedSegments.toArray(exportedSegmentNames);
        String[] exportedFeatureNames = new String[exportedFeatures.size()];
        exportedFeatures.toArray(exportedFeatureNames);
        String[] exportedHandlerNames = new String[exportedHandlers.size()];
        exportedHandlers.toArray(exportedHandlerNames);
        getBuilder().setExported(exportedSegmentNames, exportedFeatureNames, exportedHandlerNames);

        getBuilder().finishBuilding();
        return getShow();
    }

    /* protected members only below this point. */
    protected Command handleScript(ScriptType st, boolean isHeaderScript) {
        String runat = st.getRunat();
        String mimeType = st.getType();

        if ("runtime".equals(runat)) {
            if ("text/java".equals(mimeType)) {
                try {
                    return handleJavaCode(st.getValue(), isHeaderScript);
                } catch (IOException exp) {
                    throw wrapException(exp);
                }
            } else {
                throw new RuntimeException("Non Java code <script> block is not supported at 'runtime'");
            }
        // we do not allow non-runtime runat scripts for oommands.
        } else if (isHeaderScript && "build".equals(runat)) {
            // handle build time jsr-223 script   
            getScriptEvaluator().eval(getJaxbShow(), st);
            return null;
        } else {
            throw new RuntimeException("unsupported 'runat' value for <script> tag : '" + runat + "'");
        }
    }

    /**
     * Read a Java source fragment.  This is terminated with a "]]", which
     * is consumed.  This recognizes the special sequences
     * XLET_ONLY_[[ java_source ]],  GRINVIEW_ONLY_[[ java_source ]]
     * and GRIN_COMMAND_[[ grin_cmd ]], GRIN_COMMAND_REF_[[ cmd_ref ]]
     **/
    protected void readJavaSource(Lexer lexer,
            StringBuffer xletSource, StringBuffer grinviewSource,
            SEShowCommand command) throws IOException {
        for (;;) {
            String src = lexer.getStringExact();
            if (src == null) {
                lexer.reportError("EOF unexpected");
            } else if ("]]".equals(src)) {
                return;
            } else if ("XLET_ONLY_[[".equals(src)) {
                readJavaSource(lexer, xletSource, null, command);
            } else if ("GRINVIEW_ONLY_[[".equals(src)) {
                readJavaSource(lexer, null, grinviewSource, command);
            } else {
                if ("GRIN_COMMAND_[[".equals(src)) {
                    if (command == null) {
                        lexer.reportError("GRIN_COMMAND_[[ unexpected");
                    } else {
                        lexer.reportWarning("GRIN_COMMAND_ ignored");
                    }
                    // ignore command string
                    lexer.getString();
                    // ignore command ending
                    lexer.parseExpected("]]");
                    // we do *NOT* support GRIN_COMMAND_[[
                    // src = command.addSubCommand(cmd);
                    return;
                } else if ("GRIN_COMMAND_REF_[[".equals(src)) {
                    if (command == null) {
                        lexer.reportError("GRIN_COMMAND_REF_[[ unexpected");
                    }
                    String cmdRef = lexer.getString().trim();
                    lexer.parseExpected("]]");
                    Command subCmd = getNamedCommands().get(cmdRef);
                    if (subCmd == null) {
                        lexer.reportError("invalid command reference: '" + cmdRef + "'");
                    }
                    src = command.addSubCommand(subCmd);
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

    protected Command handleJavaCode(String code, boolean isHeaderScript) throws IOException {
        Lexer lexer = new Lexer(new StringReader(code), "<script>", null);
        if (isHeaderScript) {
            lexer.parseExpected("java_generated_class");
            String className = lexer.getString();
            lexer.parseExpected("[[");
            StringBuffer xletClassBody = new StringBuffer();
            StringBuffer grinviewClassBody = new StringBuffer();
            readJavaSource(lexer, xletClassBody, grinviewClassBody, null);
            SEShowCommands cmds = getShow().getShowCommands();
            cmds.setClassName(className);
            cmds.setXletClassBody(xletClassBody.toString());
            cmds.setGrinviewClassBody(grinviewClassBody.toString());
            return null;
        } else {
            // script for a java_command
            SEShowCommands cmds = show.getShowCommands();
            if (cmds.getClassName() == null) {
                lexer.reportError("java_command seen, but java_generated_class not set");
            }
            lexer.parseExpected("java_command");
            lexer.parseExpected("[[");
            StringBuffer xletBody = new StringBuffer();
            StringBuffer grinviewBody = new StringBuffer();
            SEShowCommand result = cmds.addNewCommand();
            readJavaSource(lexer, xletBody, grinviewBody, result);
            result.setXletMethodBody(xletBody.toString());
            result.setGrinviewMethodBody(grinviewBody.toString());
            return result;
        }
    }

    protected Feature getFeatureFromId(Object obj) {
        return getFeatureFromId(obj, Feature.class);
    }

    protected <T> T getFeatureFromId(Object obj, Class<T> expectedType) {
        if (obj instanceof NodeType) {
            return getFeatureFromId(((NodeType) obj).getId(), expectedType);
        } else {
            throw new RuntimeException("node type expected, but got " + obj);
        }
    }

    protected Feature getFeatureFromId(String id) {
        if (!idToNode.containsKey(id)) {
            throw new RuntimeException("Unresolved feature refence: " + id);
        }
        SENode node = getIdToNode().get(id);
        if (node instanceof Feature) {
            return (Feature) node;
        } else {
            throw new RuntimeException(id + " does not refer to a feature");
        }
    }

    protected <T> T getFeatureFromId(String id, Class<T> expectedType) {
        Feature f = getFeatureFromId(id);
        if (!(expectedType.isAssignableFrom(f.getClass()))) {
            throw new RuntimeException(id + " is not a " + expectedType.getName());
        }
        return expectedType.cast(f);
    }

    protected RCHandler getRCHandlerFromId(Object obj) {
        return getRCHandlerFromId(obj, RCHandler.class);
    }

    protected <T> T getRCHandlerFromId(Object obj, Class<T> expectedType) {
        if (obj instanceof NodeType) {
            return getRCHandlerFromId(((NodeType) obj).getId(), expectedType);
        } else {
            throw new RuntimeException("node type expected, but got  " + obj);
        }
    }

    protected RCHandler getRCHandlerFromId(String id) {
        if (!idToNode.containsKey(id)) {
            throw new RuntimeException("Unresolved RC Handler refence: " + id);
        }
        SENode node = getIdToNode().get(id);
        if (node instanceof RCHandler) {
            return (RCHandler) node;
        } else {
            throw new RuntimeException(id + " does not refer to a RC Handler");
        }
    }

    protected <T> T getRCHandlerFromId(String id, Class<T> expectedType) {
        RCHandler rc = getRCHandlerFromId(id);
        if (!(expectedType.isAssignableFrom(rc.getClass()))) {
            throw new RuntimeException(id + " is not a " + expectedType.getName());
        }
        return expectedType.cast(rc);
    }

    protected SESegment getSegmentFromId(Object obj) {
        if (obj instanceof NodeType) {
            return getSegmentFromId(((NodeType) obj).getId());
        } else {
            throw new RuntimeException("node type expected, but got " + obj);
        }
    }

    protected SESegment getSegmentFromId(String id) {
        if (!idToNode.containsKey(id)) {
            throw new RuntimeException("Unresolved Segment refence: " + id);
        }
        SENode node = getIdToNode().get(id);
        if (node instanceof SESegment) {
            return (SESegment) node;
        } else {
            throw new RuntimeException(id + " does not refer to a Segment");
        }
    }

    protected Feature convertFeature(final FeatureType jaxbFeature) {
        Feature feature = null;
        if (jaxbFeature instanceof AssemblyType) {
            feature = convertAssembly((AssemblyType) jaxbFeature);
        } else if (jaxbFeature instanceof BoxType) {
            feature = convertBox((BoxType) jaxbFeature);
        } else if (jaxbFeature instanceof ClippedType) {
            feature = convertClipped((ClippedType) jaxbFeature);
        } else if (jaxbFeature instanceof FadeType) {
            feature = convertFade((FadeType) jaxbFeature);
        } else if (jaxbFeature instanceof FixedImageType) {
            feature = convertFixedImage((FixedImageType) jaxbFeature);
        } else if (jaxbFeature instanceof GroupType) {
            feature = convertGroup((GroupType) jaxbFeature);
        } else if (jaxbFeature instanceof GuaranteeFillType) {
            feature = convertGuaranteeFill((GuaranteeFillType) jaxbFeature);
        } else if (jaxbFeature instanceof ImageSequenceType) {
            feature = convertImageSequence((ImageSequenceType) jaxbFeature);
        } else if (jaxbFeature instanceof MenuAssemblyType) {
            feature = convertMenuAssembly((MenuAssemblyType) jaxbFeature);
        } else if (jaxbFeature instanceof InterpolatedModelType) {
            feature = convertInterpolatedModel((InterpolatedModelType) jaxbFeature);
        } else if (jaxbFeature instanceof ScalingModelType) {
            feature = convertScalingdModel((ScalingModelType) jaxbFeature);
        } else if (jaxbFeature instanceof SetTargetType) {
            feature = convertSetTarget((SetTargetType) jaxbFeature);
        } else if (jaxbFeature instanceof SrcOverType) {
            feature = convertSrcOver((SrcOverType) jaxbFeature);
        } else if (jaxbFeature instanceof TextType) {
            feature = convertText((TextType) jaxbFeature);
        } else if (jaxbFeature instanceof TimerType) {
            feature = convertTimer((TimerType) jaxbFeature);
        } else if (jaxbFeature instanceof TranslatorType) {
            feature = convertTranslator((TranslatorType) jaxbFeature);
        } else if (jaxbFeature instanceof TranslatorModelType) {
            feature = convertTranslatorModel((TranslatorModelType) jaxbFeature);
        } else if (jaxbFeature instanceof ExtensionFeatureType) {
            feature = convertExtensionFeature((ExtensionFeatureType) jaxbFeature);
        } else if (jaxbFeature instanceof ExtensionModifierType) {
            feature = convertExtensionModifier((ExtensionModifierType) jaxbFeature);
        } else {
            throw new RuntimeException("unknown feature type : " + jaxbFeature.getClass());
        }

        if (feature != null) {
            String id = jaxbFeature.getId();
            if (id != null) {
                feature.setName(id);
                getIdToNode().put(id, (SENode) feature);
            }
            try {
                getBuilder().addFeature(id, -1, feature);
            } catch (IOException exp) {
                throw wrapException(exp);
            }
            if (feature instanceof Modifier && jaxbFeature instanceof ModifierType) {
                final Modifier mod = (Modifier) feature;
                final ModifierType jaxbMod = (ModifierType) jaxbFeature;
                final PartType pt = jaxbMod.getPart();
                if (pt.getRef() == null) {
                    // anonymous inlined feature
                    mod.setup(convertFeature(pt.getFeatureChoiceGroup()));
                } else {
                    getFeatureResolvers().add(new Resolver() {

                        public void resolve() {
                            Feature part = getFeatureFromId(pt.getRef());
                            mod.setup(part);
                        }
                    });
                }
            }
        }
        return feature;
    }

    protected SEAssembly convertAssembly(final AssemblyType jaxbAssembly) {
        final SEAssembly assembly = new SEAssembly(getShow());
        final List<AssemblyType.Part> jaxbParts = jaxbAssembly.getPart();
        final String[] names = new String[jaxbParts.size()];
        final Feature[] parts = new Feature[jaxbParts.size()];
        int index = 0;
        for (AssemblyType.Part jaxbPart : jaxbParts) {
            if (jaxbPart.getRef() == null) {
                parts[index] = convertFeature(jaxbPart.getFeatureChoiceGroup());
            }
            names[index] = jaxbPart.getName();
            index++;
        }
        getFeatureResolvers().add(new Resolver() {

            public void resolve() {
                int index = 0;
                for (AssemblyType.Part jaxbPart : jaxbParts) {
                    if (jaxbPart.getRef() != null) {
                        parts[index] = getFeatureFromId(jaxbPart.getRef());
                    }
                    index++;
                }
                assembly.setParts(names, parts);
            }
        });
        return assembly;
    }

    protected SEBox convertBox(final BoxType jaxbBox) {
        final SEBox box = new SEBox(getShow());
        box.setX(jaxbBox.getX());
        box.setY(jaxbBox.getY());
        box.setHeight(jaxbBox.getHeight());
        box.setWidth(jaxbBox.getWidth());
        Integer outline = jaxbBox.getOutline();
        if (outline != null) {
            box.setOutlineWidth(outline);
        } else {
            box.setOutlineWidth(1);
        }

        String fillColor = jaxbBox.getFillColor();
        if (fillColor != null) {
            box.setFillColor(stringToColor(fillColor));
        }
        String outlineColor = jaxbBox.getOutlineColor();
        if (outlineColor != null) {
            box.setOutlineColor(stringToColor(outlineColor));
        }

        final Object scaleModel = jaxbBox.getScaleModel();
        if (scaleModel != null) {
            getFeatureResolvers().add(new Resolver() {

                public void resolve() {
                    box.setScalingModel(getFeatureFromId(scaleModel,
                            SEScalingModel.class));
                }
            });
        }
        return box;
    }

    protected SEClipped convertClipped(ClippedType jaxbClipped) {
        Rectangle clipRegion = new Rectangle(jaxbClipped.getX(),
                jaxbClipped.getY(), jaxbClipped.getWidth(),
                jaxbClipped.getHeight());
        return new SEClipped(getShow(), jaxbClipped.getId(), clipRegion);
    }

    protected SEFade convertFade(FadeType jaxbFade) {
        String name = jaxbFade.getId();
        boolean srcOver = jaxbFade.isSrcOver();
        List<FadeType.Keyframes.Keyframe> keyframes = jaxbFade.getKeyframes().getKeyframe();
        int[] fs = new int[keyframes.size()];
        int[] alphas = new int[fs.length];
        int index = 0;
        for (FadeType.Keyframes.Keyframe kf : keyframes) {
            fs[index] = kf.getFrame();
            // skip '#' prefix and parse as hex
            alphas[index] = Integer.parseInt(kf.getAlpha().substring(1), 16);
            index++;
        }
        int repeatFrame = convertRepeatFrame(jaxbFade.getRepeatFrame());
        int loopCount = convertLoopCount(jaxbFade.getLoopCount());
        Command[] commands = convertCommandList(jaxbFade.getEndCommands());
        return new SEFade(getShow(), name, srcOver, fs, alphas, repeatFrame,
                loopCount, commands);
    }

    protected SEFixedImage convertFixedImage(FixedImageType jaxbFixedImage) {
        String name = jaxbFixedImage.getId();
        SEImagePlacement placement = new SEImagePlacement();
        placement.setX(jaxbFixedImage.getX());
        placement.setY(jaxbFixedImage.getY());
        placement.setXAlign(convertXAlignmentType(jaxbFixedImage.getXAlign()));
        placement.setYAlign(convertYAlignmentType(jaxbFixedImage.getYAlign()));
        placement.setScaleX(jaxbFixedImage.getXScale() / 1000.0);
        placement.setScaleY(jaxbFixedImage.getYScale() / 1000.0);
        String fileName = jaxbFixedImage.getSrc();

        try {
            final SEFixedImage fixedImage = new SEFixedImage(getShow(), name, placement, fileName);
            final Object scaleModel = jaxbFixedImage.getScaleModel();
            if (scaleModel != null) {
                getFeatureResolvers().add(new Resolver() {

                    public void resolve() {
                        fixedImage.setScalingModel(getFeatureFromId(scaleModel,
                                SEScalingModel.class));
                    }
                });
            }
            return fixedImage;
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }

    protected SEGroup convertGroup(final GroupType jaxbGroup) {
        final SEGroup group = new SEGroup(getShow());
        final List<PartType> jaxbParts = jaxbGroup.getPart();
        final Feature[] parts = new Feature[jaxbParts.size()];
        int index = 0;
        for (PartType jaxbPart : jaxbParts) {
            Object ref = jaxbPart.getRef();
            if (ref == null) {
                parts[index] = convertFeature(jaxbPart.getFeatureChoiceGroup());
            }
            index++;
        }
        getFeatureResolvers().add(new Resolver() {

            public void resolve() {
                int index = 0;
                for (PartType jaxbPart : jaxbParts) {
                    Object ref = jaxbPart.getRef();
                    if (ref != null) {
                        parts[index] = getFeatureFromId(ref);
                    }
                    index++;
                }
                group.setup(parts);
            }
        });
        return group;
    }

    protected SEGuaranteeFill convertGuaranteeFill(final GuaranteeFillType jaxbGfill) {
        String name = jaxbGfill.getId();
        Rectangle rect = new Rectangle(
                jaxbGfill.getX(), jaxbGfill.getY(),
                jaxbGfill.getWidth(), jaxbGfill.getHeight());
        List<GuaranteeFillType.FillRect> jaxbFillRects = jaxbGfill.getFillRect();
        Rectangle[] fills = new Rectangle[jaxbFillRects.size()];
        int index = 0;
        for (GuaranteeFillType.FillRect jaxbFillRect : jaxbFillRects) {
            fills[index] = new Rectangle(
                    jaxbFillRect.getX(), jaxbFillRect.getY(),
                    jaxbFillRect.getWidth(), jaxbFillRect.getHeight());
            index++;
        }
        return new SEGuaranteeFill(getShow(), name, rect, fills);
    }

    protected SEImageSequence convertImageSequence(ImageSequenceType jaxbImgSeq) {
        String name = jaxbImgSeq.getId();
        boolean repeat = jaxbImgSeq.isRepeat();
        int loopCount = convertLoopCount(jaxbImgSeq.getLoopCount());
        Command[] commands = convertCommandList(jaxbImgSeq.getEndCommands());
        boolean singlePlacement = (jaxbImgSeq.getX() != null);
        SEImageSeqPlacement placement = null;
        List<ImageSequenceType.Images.Img> jaxbImages = jaxbImgSeq.getImages().getImg();
        String[] files = new String[jaxbImages.size()];
        int index = 0;
        for (ImageSequenceType.Images.Img jaxbIm : jaxbImages) {
            files[index] = jaxbIm.getSrc();
            index++;
        }
        if (singlePlacement) {
            SEImagePlacement place = new SEImagePlacement();
            place.setX(jaxbImgSeq.getX());
            place.setY(jaxbImgSeq.getY());
            place.setXAlign(convertXAlignmentType(jaxbImgSeq.getXAlign()));
            place.setYAlign(convertYAlignmentType(jaxbImgSeq.getYAlign()));
            place.setScaleX(jaxbImgSeq.getXScale() / 1000.0);
            place.setScaleY(jaxbImgSeq.getYScale() / 1000.0);
            placement = place;
        } else {
            ArrayList<SEImagePlacement> placements = new ArrayList<SEImagePlacement>();
            for (ImageSequenceType.Images.Img jaxbIm : jaxbImages) {
                SEImagePlacement place = new SEImagePlacement();
                place.setX(jaxbIm.getX());
                place.setY(jaxbIm.getY());
                place.setXAlign(convertXAlignmentType(jaxbIm.getXAlign()));
                place.setYAlign(convertYAlignmentType(jaxbIm.getYAlign()));
                place.setScaleX(jaxbIm.getXScale() / 1000.0);
                place.setScaleY(jaxbIm.getYScale() / 1000.0);
                placements.add(place);
            }
            placement = new SEImagePlacementList(placements);
        }

        try {
            final SEImageSequence imgSeq = new SEImageSequence(getShow(), name, placement, "",
                    files, "", repeat, loopCount, commands);
            final Object model = jaxbImgSeq.getModel();
            if (model != null) {
                getFeatureResolvers().add(new Resolver() {

                    public void resolve() {
                        imgSeq.setModel(getFeatureFromId(model,
                                SEImageSequence.class));
                    }
                });
            }
            final Object scaleModel = jaxbImgSeq.getScaleModel();
            if (scaleModel != null) {
                getFeatureResolvers().add(new Resolver() {

                    public void resolve() {
                        imgSeq.setScalingModel(getFeatureFromId(scaleModel,
                                SEScalingModel.class));
                    }
                });
            }
            return imgSeq;
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }

    }

    protected SEMenuAssembly convertMenuAssembly(MenuAssemblyType jaxbMenu) {
        final MenuAssemblyHelper helper = new MenuAssemblyHelper();
        helper.show = getShow();
        String name = jaxbMenu.getId();
        helper.template = new ArrayList<MenuAssemblyHelper.Features>();
        MenuAssemblyType.Template jaxbTemplate = jaxbMenu.getTemplate();
        List<FeatureSetType> jaxbFsList = jaxbTemplate.getItem();
        for (FeatureSetType fst : jaxbFsList) {
            MenuAssemblyHelper.Features fs = new MenuAssemblyHelper.Features();
            fs.id = fst.getId();
            // fs.features.add(new SubFeature());
            List<PartType> parts = fst.getPart();
            for (final PartType pt : parts) {
                final Object ref = pt.getRef();
                final Feature anonFeature = (ref != null) ? null : convertFeature(pt.getFeatureChoiceGroup());
                fs.features.add(new FeatureRef() {

                    public Feature getFeature() {
                        if (ref != null) {
                            return getFeatureFromId(ref);
                        } else {
                            // anonmyous inlined feature
                            return anonFeature;
                        }
                    }
                });
            }
            helper.template.add(fs);
        }

        helper.partNames = new ArrayList<String>();
        helper.parts = new ArrayList<List<MenuAssemblyHelper.Features>>();

        List<MenuAssemblyType.Parts.Part> jaxbMenuParts = jaxbMenu.getParts().getPart();
        for (MenuAssemblyType.Parts.Part jaxbMenuPart : jaxbMenuParts) {
            helper.partNames.add(jaxbMenuPart.getName());
            List<MenuAssemblyHelper.Features> replacements = new ArrayList<MenuAssemblyHelper.Features>();
            List<FeatureSetType> jaxbReplacements = jaxbMenuPart.getReplacement();
            for (FeatureSetType fst : jaxbReplacements) {
                MenuAssemblyHelper.Features fs = new MenuAssemblyHelper.Features();
                fs.id = fst.getId();
                // fs.features.add(new SubFeature());
                List<PartType> parts = fst.getPart();
                for (final PartType pt : parts) {
                    final Object ref = pt.getRef();
                    final Feature anonFeature = (ref != null) ? null : convertFeature(pt.getFeatureChoiceGroup());
                    fs.features.add(new FeatureRef() {

                        public Feature getFeature() {
                            if (ref != null) {
                                return getFeatureFromId(ref);
                            } else {
                                // anonmyous inlined feature
                                return anonFeature;
                            }
                        }
                    });
                }
                replacements.add(fs);
            }
            helper.parts.add(replacements);
        }

        SEMenuAssembly ma = new SEMenuAssembly(getShow(), helper);
        helper.assembly = ma;
        getFeatureResolvers().add(new Resolver() {

            public void resolve() {
                try {
                    Iterable<Feature> syntheticFeatures = helper.setupAssembly();
                    for (Feature f : syntheticFeatures) {
                        getBuilder().addSyntheticFeature(f);
                    }
                } catch (IOException exp) {
                    throw wrapException(exp);
                }
            }
        });
        return ma;
    }

    protected SEInterpolatedModel convertInterpolatedModel(InterpolatedModelType jaxbIm) {
        String name = jaxbIm.getId();
        int loopCount = convertLoopCount(jaxbIm.getLoopCount());
        int repeatFrame = convertRepeatFrame(jaxbIm.getRepeatFrame());
        List<InterpolatedModelType.Keyframes.Keyframe> keyframes =
                jaxbIm.getKeyframes().getKeyframe();
        int numValues = keyframes.get(0).getValues().size();
        int[] frames = new int[keyframes.size()];
        int[][] values = new int[numValues][frames.length];
        int frameIndex = 0;
        for (InterpolatedModelType.Keyframes.Keyframe kf : keyframes) {           
            int valueIndex = 0;
            for (int value : kf.getValues()) {
                values[valueIndex][frameIndex] = value;
                valueIndex++;
            }
            frames[frameIndex] = kf.getFrame();
            frameIndex++;
        }
        Command[] commands = convertCommandList(jaxbIm.getEndCommands());
        return (SEInterpolatedModel) getBuilder().makeInterpolatedModel(name, frames, values,
                repeatFrame, loopCount, commands, SEInterpolatedModel.class);
    }

    public SEScalingModel convertScalingdModel(ScalingModelType jaxbSm) {
        String name = jaxbSm.getId();
        int loopCount = convertLoopCount(jaxbSm.getLoopCount());
        int repeatFrame = convertRepeatFrame(jaxbSm.getRepeatFrame());
        List<ScalingModelType.Keyframes.Keyframe> keyframes =
                jaxbSm.getKeyframes().getKeyframe();
        int[] frames = new int[keyframes.size()];
        int[][] values = new int[4][frames.length];
        int frameIndex = 0;
        for (ScalingModelType.Keyframes.Keyframe kf : keyframes) {
            values[InterpolatedModel.SCALE_X_FIELD][frameIndex] = kf.getX();
            values[InterpolatedModel.SCALE_Y_FIELD][frameIndex] = kf.getY();
            values[InterpolatedModel.SCALE_X_FACTOR_FIELD][frameIndex] = kf.getXScale();
            values[InterpolatedModel.SCALE_Y_FACTOR_FIELD][frameIndex] = kf.getYScale();
            frames[frameIndex] = kf.getFrame();
            frameIndex++;
        }
        Command[] commands = convertCommandList(jaxbSm.getEndCommands());
        return (SEScalingModel) getBuilder().makeScalingModel(name, frames, values,
                repeatFrame, loopCount, commands);
    }

    protected SESetTarget convertSetTarget(SetTargetType jaxbSt) {
        String name = jaxbSt.getId();
        String targetName = jaxbSt.getTarget();
        String[] names = getShow().getDrawTargets();
        int target = -1;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(targetName)) {
                target = i;
                break;
            }
        }
        if (target == -1) {
            throw new RuntimeException("invalid target '" + targetName + "' in '" + name + "'");
        }
        return new SESetTarget(getShow(), name, target);
    }

    protected SESrcOver convertSrcOver(SrcOverType jaxbSrcOver) {
        return new SESrcOver(getShow());
    }

    protected SEText convertText(TextType jaxbText) {
        String name = jaxbText.getId();
        int x = jaxbText.getX();
        int y = jaxbText.getY();
        int alignment = 0;
        XAlignmentType xAlign = jaxbText.getXAlign();
        switch (xAlign) {
            case LEFT:
                alignment |= SEText.LEFT;
                break;
            case MIDDLE:
                alignment |= SEText.MIDDLE;
                break;
            case RIGHT:
                alignment |= SEText.RIGHT;
                break;
        }
        TextYAlignmentType yAlign = jaxbText.getYAlign();
        switch (yAlign) {
            case TOP:
                alignment |= SEText.TOP;
                break;
            case BASELINE:
                alignment |= SEText.BASELINE;
                break;
            case BOTTOM:
                alignment |= SEText.BOTTOM;
                break;
        }

        String[] strings = jaxbText.getValue().split("\n");
        int vspace = jaxbText.getVspace();
        String fontName = jaxbText.getFont();
        int fontSize = jaxbText.getSize();
        int fontStyle = 0;
        FontStyleType fs = jaxbText.getStyle();
        switch (fs) {
            case BOLD:
                fontStyle |= Font.BOLD;
                break;
            case PLAIN:
                fontStyle |= Font.PLAIN;
                break;
            case ITALIC:
                fontStyle |= Font.ITALIC;
                break;
            case BOLD_ITALIC:
                fontStyle |= Font.BOLD;
                fontStyle |= Font.ITALIC;
                break;
        }
        Font font = new Font(fontName, fontSize, fontStyle);
        List<Color> clrList = new ArrayList<Color>();
        for (String clrStr : jaxbText.getColors()) {
            clrList.add(stringToColor(clrStr));
        }
        Color[] colors = new Color[clrList.size()];
        clrList.toArray(colors);
        int loopCount = convertLoopCount(jaxbText.getLoopCount());
        Color bkColor = stringToColor(jaxbText.getBackground());
        return new SEText(getShow(), name, x, y, alignment, strings, vspace,
                font, colors, loopCount, bkColor);
    }

    protected SETimer convertTimer(TimerType jaxbTimer) {
        String name = jaxbTimer.getId();
        int numFrames = jaxbTimer.getNumFrames();
        boolean repeat = jaxbTimer.isRepeat();
        Command[] commands = convertCommandList(jaxbTimer.getEndCommands());
        return (SETimer) getBuilder().makeTimer(name, numFrames, repeat, commands);
    }

    protected SETranslator convertTranslator(final TranslatorType jaxbTrans) {
        final SETranslator trans = new SETranslator(getShow(), jaxbTrans.getId());
        getFeatureResolvers().add(new Resolver() {

            public void resolve() {
                SETranslatorModel transModel =
                        getFeatureFromId(jaxbTrans.getModel(),
                        SETranslatorModel.class);
                trans.setModel(transModel);
            }
        });
        getBuilder().addDeferredBuilder(new TranslatorHelper(trans, -1));
        return trans;
    }

    protected SETranslatorModel convertTranslatorModel(TranslatorModelType jaxbTransModel) {
        String name = jaxbTransModel.getId();
        int loopCount = convertLoopCount(jaxbTransModel.getLoopCount());
        int repeatFrame = convertRepeatFrame(jaxbTransModel.getRepeatFrame());
        List<TranslatorModelType.Keyframes.Keyframe> keyframes =
                jaxbTransModel.getKeyframes().getKeyframe();
        int[] frames = new int[keyframes.size()];
        int[][] values = new int[2][frames.length];
        int frameIndex = 0;
        for (TranslatorModelType.Keyframes.Keyframe kf : keyframes) {
            values[InterpolatedModel.SCALE_X_FIELD][frameIndex] = kf.getX();
            values[InterpolatedModel.SCALE_Y_FIELD][frameIndex] = kf.getY();
            frames[frameIndex] = kf.getFrame();
            frameIndex++;
        }
        boolean isRelative = jaxbTransModel.isRelative();
        Command[] commands = convertCommandList(jaxbTransModel.getEndCommands());
        return (SETranslatorModel) getBuilder().makeTranslatorModel(name, frames, values,
                isRelative, repeatFrame, loopCount, commands);
    }

    /**
     * Handling of extension feature has to be revisited!
     *
     *  -- how to allow user specified elements/attributes for extensions?
     *  -- how ShowXML schema has to be relaxed for the same?
     *  -- how to handle XML fragment serialization/de-serialization
     *     for extensions?
     *
     * Currently, we create SEGenericFeature for all extension features.
     */
    protected Feature convertExtensionFeature(ExtensionFeatureType jaxbExt) {
        List<ParamType> params = jaxbExt.getParam();
        String tmp = null;
        for (ParamType pt : params) {
            if (pt.getName().equals("runtimeClassName")) {
                tmp = pt.getValue();
            }
        }
        final String runtimeClassName = tmp;
        Feature feature = new SEGenericFeature(
                getShow()) {

            public void writeInstanceData(GrinDataOutputStream out)
                    throws IOException {
                out.writeSuperClassData(this);
            // nothing specific to this class to record.
            }

            public String getRuntimeClassName() {
                return runtimeClassName;
            }
        };
        fillBeanProperties(feature, params);
        return feature;
    }

    /**
     * Handling of extension modifiers has to be revisited!
     *
     *  -- how to allow user specified elements/attributes for extensions?
     *  -- how ShowXML schema has to be relaxed for the same?
     *  -- how to handle XML fragment serialization/de-serialization
     *     for extensions?
     *
     * Currently, we create SEGenericModifier for all extension modifiers.
     */
    protected Modifier convertExtensionModifier(ExtensionModifierType jaxbExt) {
        List<ParamType> params = jaxbExt.getParam();
        String tmp = null;
        for (ParamType pt : params) {
            if (pt.getName().equals("runtimeClassName")) {
                tmp = pt.getValue();
            }
        }
        final String runtimeClassName = tmp;
        Modifier m = new SEGenericModifier(
                getShow()) {

            public void writeInstanceData(GrinDataOutputStream out)
                    throws IOException {
                out.writeSuperClassData(this);
            // nothing specific to this class to record.
            }

            public String getRuntimeClassName() {
                return runtimeClassName;
            }
        };
        fillBeanProperties(m, params);
        return m;
    }

    protected void fillBeanProperties(Object obj, List<ParamType> params) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
        } catch (Exception exp) {
            // exp.printStackTrace();
            return;
        }
        PropertyDescriptor[] propDescs = beanInfo.getPropertyDescriptors();
        for (ParamType pt : params) {
            for (PropertyDescriptor prop : propDescs) {
                if (pt.getName().equals(prop.getName())) {
                    Method writer = prop.getWriteMethod();
                    if (writer != null) {
                        try {
                            writer.setAccessible(true);
                            Object value = writer.invoke(obj, pt.getValue());
                        } catch (Exception exp) {
                            // exp.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    protected SEImagePlacement.HorizontalAlignment convertXAlignmentType(XAlignmentType xAlign) {
        switch (xAlign) {
            case LEFT:
                return SEImagePlacement.HorizontalAlignment.LEFT;
            case MIDDLE:
                return SEImagePlacement.HorizontalAlignment.MIDDLE;
            case RIGHT:
                return SEImagePlacement.HorizontalAlignment.RIGHT;
            default:
                return SEImagePlacement.HorizontalAlignment.LEFT;
        }
    }

    protected SEImagePlacement.VerticalAlignment convertYAlignmentType(YAlignmentType yAlign) {
        switch (yAlign) {
            case TOP:
                return SEImagePlacement.VerticalAlignment.TOP;
            case MIDDLE:
                return SEImagePlacement.VerticalAlignment.MIDDLE;
            case BOTTOM:
                return SEImagePlacement.VerticalAlignment.BOTTOM;
            default:
                return SEImagePlacement.VerticalAlignment.TOP;
        }
    }

    protected int convertRepeatFrame(Integer repeatFrame) {
        return (repeatFrame == null) ? Integer.MAX_VALUE : repeatFrame;
    }

    protected int convertLoopCount(String loopCount) {
        return (loopCount == null) ? 1 : ((loopCount.equals("infinite")) ? Integer.MAX_VALUE : Integer.parseInt(loopCount));
    }

    protected Color stringToColor(String str) {
        if (str == null) {
            return null;
        }
        int red = Integer.parseInt(str.substring(1, 3), 16);
        int green = Integer.parseInt(str.substring(3, 5), 16);
        int blue = Integer.parseInt(str.substring(5, 7), 16);
        int alpha = (str.length() == 9) ? Integer.parseInt(str.substring(7, 9), 16) : 255;
        return new Color(red, green, blue, alpha);
    }

    protected Feature[] convertFeatureRefList(FeatureRefListType frList) {
        Feature[] features;
        if (frList != null) {
            List<PartType> parts = frList.getFeature();
            features = new Feature[parts.size()];
            int index = 0;
            for (PartType pt : parts) {
                Object ref = pt.getRef();
                if (ref != null) {
                    features[index] = getFeatureFromId(ref);
                } else {
                    features[index] = convertFeature(pt.getFeatureChoiceGroup());
                }
                index++;
            }
        } else {
            features = new Feature[0];
        }
        return features;
    }

    protected RCHandler convertRCHandler(RCHandlerType rht) {
        RCHandler rcHandler;
        if (rht instanceof CommandRCHandlerType) {
            rcHandler = convertCommandRCHandler((CommandRCHandlerType) rht);
        } else if (rht instanceof VisualRCHandlerType) {
            rcHandler = convertVisualRCHandler((VisualRCHandlerType) rht);
        } else {
            throw new RuntimeException("unknown RC Handler type " + rht.getClass());
        }
        rcHandler.setShow(getShow());
        String id = rht.getId();
        if (id != null) {
            getIdToNode().put(id, (SENode) rcHandler);
        }
        try {
            getBuilder().addRCHandler(id, -1, rcHandler);
        } catch (IOException exp) {
            throw wrapException(exp);
        }
        return rcHandler;
    }

    protected RCHandler convertCommandRCHandler(CommandRCHandlerType rht) {
        String name = rht.getId();
        List<String> keyList = rht.getKeys();
        int mask = 0;
        for (String key : keyList) {
            String actualKey = key.replace('-', '_');
            RCKeyEvent rcEventKey = RCKeyEvent.getKeyByName(actualKey);
            if (rcEventKey == null) {
                throw new RuntimeException("invalid RC key: " + key);
            }
            mask |= rcEventKey.getBitMask();
        }
        Command[] commands = convertCommandList(rht.getExecute());
        return new SECommandRCHandler(name, mask, commands);
    }

    protected RCHandler convertVisualRCHandler(VisualRCHandlerType vht) {
        VisualRCHandlerHelper helper = new VisualRCHandlerHelper();
        String name = vht.getId();
        helper.setHandlerName(name);
        ArrayList<ArrayList<VisualRCHandlerCell>> grid = new ArrayList();
        VisualRCHandlerType.Grid jaxbGrid = vht.getGrid();
        List<StateCellType> jaxbStates = new ArrayList<StateCellType>();
        List<StateCellType> jaxbMouseRectStates = new ArrayList<StateCellType>();
        List<VisualRCHandlerType.Grid.Row> jaxbRows = jaxbGrid.getRow();
        for (VisualRCHandlerType.Grid.Row jaxbRow : jaxbRows) {
            List<VisualRCHandlerCellType> jaxbCells = jaxbRow.getCells();
            ArrayList<VisualRCHandlerCell> cells = new ArrayList<VisualRCHandlerCell>();
            for (VisualRCHandlerCellType jaxbCell : jaxbCells) {
                if (jaxbCell instanceof ActivateCellType) {
                    cells.add(VisualRCHandlerCell.newActivate());
                } else if (jaxbCell instanceof NullCellType) {
                    cells.add(VisualRCHandlerCell.newNull());
                } else if (jaxbCell instanceof LocationRefCellType) {
                    LocationRefCellType jaxbLocCell = (LocationRefCellType) jaxbCell;
                    cells.add(VisualRCHandlerCell.newLocationRef(jaxbLocCell.getX(), jaxbLocCell.getY()));
                } else if (jaxbCell instanceof StateRefCellType) {
                    StateRefCellType jaxbStateRef = (StateRefCellType) jaxbCell;
                    cells.add(VisualRCHandlerCell.newStateRef(jaxbStateRef.getState()));
                } else if (jaxbCell instanceof WallCellType) {
                    cells.add(VisualRCHandlerCell.newWall());
                } else if (jaxbCell instanceof StateCellType) {
                    StateCellType jaxbState = (StateCellType) jaxbCell;
                    cells.add(VisualRCHandlerCell.newState(jaxbState.getId()));
                    jaxbStates.add(jaxbState);
                    if (jaxbState.getMouseRect() != null) {
                        jaxbMouseRectStates.add(jaxbState);
                    }
                } else {
                    throw new RuntimeException("unknown cell type: " + jaxbCell.getClass());
                }
            }
            grid.add(cells);
        }
        helper.setGrid(grid);
        Map<String, String> rcOverrides = new HashMap<String, String>();
        for (VisualRCHandlerType.Grid.Row jaxbRow : jaxbRows) {
            List<VisualRCHandlerCellType> jaxbCells = jaxbRow.getCells();
            for (VisualRCHandlerCellType jaxbCell : jaxbCells) {
                if (jaxbCell instanceof StateCellType) {
                    StateCellType jaxbState = (StateCellType) jaxbCell;
                    String stateName = jaxbState.getId();
                    String up = jaxbState.getUp();
                    if (up != null) {
                        rcOverrides.put("up:" + stateName, up);
                    }
                    String down = jaxbState.getDown();
                    if (down != null) {
                        rcOverrides.put("down:" + stateName, down);
                    }
                    String left = jaxbState.getLeft();
                    if (left != null) {
                        rcOverrides.put("left:" + stateName, left);
                    }
                    String right = jaxbState.getRight();
                    if (right != null) {
                        rcOverrides.put("right:" + stateName, right);
                    }
                }
            }
        }
        helper.setRCOverrides(rcOverrides);

        Map<String, Integer> stateToId = helper.getStates();
        int[] mouseRectStates = null;
        Rectangle[] mouseRects = null;
        if (!jaxbMouseRectStates.isEmpty()) {
            mouseRectStates = new int[jaxbMouseRectStates.size()];
            mouseRects = new Rectangle[mouseRectStates.length];
            int index = 0;
            for (StateCellType jaxbState : jaxbMouseRectStates) {
                mouseRectStates[index] = stateToId.get(jaxbState.getId());
                StateCellType.MouseRect jaxbRect = jaxbState.getMouseRect();
                assert (jaxbRect != null) : "check the code above!";
                mouseRects[index] = new Rectangle(
                        jaxbRect.getX(), jaxbRect.getY(),
                        jaxbRect.getWidth(), jaxbRect.getHeight());
                index++;
            }
        }
        helper.setMouseRectStates(mouseRectStates);
        helper.setMouseRects(mouseRects);

        final int numStates = jaxbStates.size();
        Command[][] activateCommands = new Command[numStates][];
        Command[][] selectCommands = new Command[numStates][];
        for (StateCellType jaxbState : jaxbStates) {
            int stateNum = stateToId.get(jaxbState.getId());
            activateCommands[stateNum] = convertCommandList(jaxbState.getActivateCommands());
            selectCommands[stateNum] = convertCommandList(jaxbState.getSelectCommands());
        }
        helper.setActivateCommands(activateCommands);
        helper.setSelectCommands(selectCommands);

        int timeout = -1;
        Command[] timeoutCommands = getEmptyCommandArray();
        VisualRCHandlerType.Timeout jaxbTimeout = vht.getTimeout();
        if (jaxbTimeout != null) {
            timeout = jaxbTimeout.getFrames();
            timeoutCommands = convertCommandList(jaxbTimeout);
        }
        helper.setTimeout(timeout);
        helper.setTimeoutCommands(timeoutCommands);

        SEVisualRCHandler rcHandler;
        try {
            rcHandler = helper.getFinishedHandler();
        } catch (IOException exp) {
            throw wrapException(exp);
        }

        final Object assemblyRef = vht.getAssembly();
        if (assemblyRef != null) {
            final String[] selectPartNames = new String[numStates];
            final String[] activatePartNames = new String[numStates];
            int index = 0;
            for (StateCellType jaxbState : jaxbStates) {
                selectPartNames[index] = jaxbState.getSelect();
                activatePartNames[index] = jaxbState.getActivate();
                index++;
            }
            Boolean startSelected = vht.isStartSelected();
            if (startSelected != null) {
                helper.setStartSelected(startSelected);
            }
            final SEVisualRCHandler fHandler = rcHandler;

            getRcHandlerResolvers().add(new Resolver() {

                public void resolve() {
                    SEAssembly assembly = getFeatureFromId(assemblyRef, SEAssembly.class);
                    Feature[] selectParts = new Feature[numStates];
                    Feature[] activateParts = new Feature[numStates];
                    for (int i = 0; i < selectPartNames.length; i++) {
                        String name = selectPartNames[i];
                        if (name != null) {
                            selectParts[i] = assembly.findPart(name);
                            if (selectParts[i] == null) {
                                throw new RuntimeException("Assembly part '" + name + "' not found");
                            }
                        }
                        name = activatePartNames[i];
                        if (name != null) {
                            activateParts[i] = assembly.findPart(name);
                            if (selectParts[i] == null) {
                                throw new RuntimeException("Assembly part '" + name + "' not found");
                            }
                        }
                    }
                    fHandler.setup(assembly, selectParts, activateParts);
                }
            });
        }

        return rcHandler;
    }

    protected RCHandler[] convertRCHandlerRefList(RCHandlerRefListType rchList) {
        RCHandler[] rcHandlers;
        if (rchList != null) {
            List<RCHandlerRefListType.RcHandler> jaxbHandlerList = rchList.getRcHandler();
            rcHandlers = new RCHandler[jaxbHandlerList.size()];
            int index = 0;
            for (RCHandlerRefListType.RcHandler rcRef : jaxbHandlerList) {
                rcHandlers[index] = getRCHandlerFromId(rcRef.getRef());
                index++;
            }
        } else {
            rcHandlers = new RCHandler[0];
        }
        return rcHandlers;
    }

    protected SESegment convertSegment(SegmentType st) {
        String name = st.getId();
        Feature[] active = convertFeatureRefList(st.getActive());
        Feature[] setup = convertFeatureRefList(st.getSetup());
        RCHandler[] rcHandlers = convertRCHandlerRefList(st.getRcHandlers());
        Command[] entryCommands = convertCommandList(st.getOnEntry());
        CommandListType next = st.getNext();
        CommandListType setupDone = st.getSetupDone();
        boolean nextOnSetupDone = setupDone != null;
        Command[] commands = nextOnSetupDone ? convertCommandList(setupDone) : convertCommandList(next);
        try {
            SESegment segment = new SESegment(name, active, setup, rcHandlers, entryCommands,
                    nextOnSetupDone, commands);
            if (name != null) {
                getIdToNode().put(name, segment);
            }
            getBuilder().addSegment(st.getId(), -1, segment);
            return segment;
        } catch (IOException exp) {
            throw wrapException(exp);
        }
    }

    protected Command convertCommand(CommandType ct) {
        if (ct instanceof ActivatePartCommandType) {
            return convertActivatePartCommand((ActivatePartCommandType) ct);
        } else if (ct instanceof ActivateSegmentCommandType) {
            return convertActivateSegmentCommand((ActivateSegmentCommandType) ct);
        } else if (ct instanceof ResetFeatureCommandType) {
            return convertResetFeatureCommand((ResetFeatureCommandType) ct);
        } else if (ct instanceof SegmentDoneCommandType) {
            return convertSegmentDone((SegmentDoneCommandType) ct);
        } else if (ct instanceof SetVisualRCStateCommandType) {
            return convertSetVisualRCState((SetVisualRCStateCommandType) ct);
        } else if (ct instanceof ExtensionCommandType) {
            return convertExtensionCommand((ExtensionCommandType) ct);
        } else if (ct instanceof ScriptCommandType) {
            return convertScriptCommand((ScriptCommandType) ct);
        } else {
            throw new RuntimeException("unknown command type: " + ct.getClass());
        }
    }

    protected SEActivatePartCommand convertActivatePartCommand(final ActivatePartCommandType jaxbCmd) {
        final SEActivatePartCommand cmd = new SEActivatePartCommand(getShow());
        getCommandResolvers().add(new Resolver() {

            public void resolve() {
                SEAssembly assembly = getFeatureFromId(jaxbCmd.getAssembly(), SEAssembly.class);
                Feature part = assembly.findPart(jaxbCmd.getPartName());
                if (part == null) {
                    throw new RuntimeException("Assembly part '" + jaxbCmd.getPartName() + " not found");
                }
                cmd.setup(assembly, part);
            }
        });
        return cmd;
    }

    protected SEActivateSegmentCommand convertActivateSegmentCommand(ActivateSegmentCommandType jaxbCmd) {
        final SEActivateSegmentCommand cmd = new SEActivateSegmentCommand(getShow());
        final Object segmentRef = jaxbCmd.getSegment();
        cmd.setPop(jaxbCmd.isPop());
        cmd.setPush(jaxbCmd.isPush());
        if (segmentRef != null) {
            getCommandResolvers().add(new Resolver() {

                public void resolve() {
                    SESegment segment = getSegmentFromId(segmentRef);
                    cmd.setup(segment);




                }
            });
        }
        return cmd;
    }

    protected SEResetFeatureCommand convertResetFeatureCommand(ResetFeatureCommandType jaxbCmd) {
        final SEResetFeatureCommand cmd = new SEResetFeatureCommand(getShow());
        final Object featureRef = jaxbCmd.getFeature();
        getCommandResolvers().add(new Resolver() {

            public void resolve() {
                cmd.setFeature(getFeatureFromId(featureRef));
            }
        });
        return cmd;
    }

    protected SESegmentDoneCommand convertSegmentDone(SegmentDoneCommandType jaxbCmd) {
        return new SESegmentDoneCommand(getShow());
    }

    protected SESetVisualRCStateCommand convertSetVisualRCState(final SetVisualRCStateCommandType jaxbCmd) {
        final SESetVisualRCStateCommand cmd = new SESetVisualRCStateCommand(getShow());

        getCommandResolvers().add(new Resolver() {

            public void resolve() {
                Object rcHandlerRef = jaxbCmd.getRcHandler();
                String stateName = jaxbCmd.getState();
                SEVisualRCHandler handler = getRCHandlerFromId(rcHandlerRef,
                        SEVisualRCHandler.class);
                VisualRCHandlerHelper helper = handler.getHelper();
                Map<String, Integer> states = helper.getStates();
                int state = (stateName == null) ? -1 : states.get(stateName);
                cmd.setVisualRCHandler(handler);
                cmd.setup(jaxbCmd.isActivate(), state, handler, jaxbCmd.isRunCommands());
            }
        });
        return cmd;
    }

    /**
     * Handling of extension commands has to be revisited!
     *
     *  -- how to allow user specified elements/attributes for extensions?
     *  -- how ShowXML schema has to be relaxed for the same?
     *  -- how to handle XML fragment serialization/de-serialization
     *     for extensions?
     *
     * Currently, we use SEGeneraticCommand for all extension commands.
     */
    protected Command convertExtensionCommand(ExtensionCommandType jaxbCmd) {
        List<ParamType> params = jaxbCmd.getParam();
        String tmpClassName = null;
        String tmpArgument = null;
        for (ParamType pt : params) {
            if (pt.getName().equals("runtimeClassName")) {
                tmpClassName = pt.getValue();
            }
            if (pt.getName().equals("argument")) {
                tmpArgument = pt.getValue();
            }
        }
        final String runtimeClassName = tmpClassName;
        final String argument = tmpArgument;

        SEGenericCommand command = new SEGenericCommand(
                getShow()) {

            public String getArgument() {
                return argument;
            }

            public void execute() { // nothing to do.
            }

            public void writeInstanceData(GrinDataOutputStream out)
                    throws IOException {
                out.writeSuperClassData(this);
                out.writeString(argument);
            }

            public String getRuntimeClassName() {
                return runtimeClassName;
            }
        };
        
        fillBeanProperties(command, params);
        return command;
    }

    protected Command convertScriptCommand(ScriptCommandType jaxbCmd) {
        return handleScript(jaxbCmd.getScript(), false);
    }

    protected Command[] convertCommandList(CommandListType cmdListType) {
        if (cmdListType == null) {
            return getEmptyCommandArray();
        } else {
            List<CommandType> cmdList = cmdListType.getCommands();
            Command[] commands = new Command[cmdList.size()];
            int index = 0;
            for (CommandType ct : cmdList) {
                Command cmd = convertCommand(ct);
                if (cmd != null) {
                    commands[index] = cmd;
                }
                index++;
            }
            return commands;
        }
    }

    protected ShowType unmarshall() {
        try {
            JAXBContext ctx = JAXBContext.newInstance("com.hdcookbook.grin.io.xml");
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            JAXBElement<ShowType> jaxbElement = (JAXBElement<ShowType>) unmarshaller.unmarshal(getReader());
            return jaxbElement.getValue();
        } catch (Exception exp) {
            throw wrapException(exp);
        }

    }

    protected static RuntimeException wrapException(Exception exp) {
        if (exp instanceof RuntimeException) {
            return (RuntimeException) exp;
        } else {
            return new RuntimeException(exp);
        }
    }

    /* accessors */
    protected static Command[] getEmptyCommandArray() {
        return emptyCommandArray;
    }

    protected Reader getReader() {
        return reader;
    }

    protected ShowBuilder getBuilder() {
        return builder;
    }

    protected Map<String, SENode> getIdToNode() {
        return idToNode;
    }

    protected SEShow getShow() {
        return show;
    }

    protected ShowType getJaxbShow() {
        return jaxbShow;
    }

    protected List<Resolver> getRcHandlerResolvers() {
        return rcHandlerResolvers;
    }

    protected static interface Resolver {

        public void resolve();
    }

    protected List<Resolver> getFeatureResolvers() {
        return featureResolvers;
    }

    protected List<Resolver> getRCHandlerResolvers() {
        return getRcHandlerResolvers();
    }

    protected List<Resolver> getCommandResolvers() {
        return commandResolvers;
    }

    protected Map<String, Command> getNamedCommands() {
        return namedCommands;
    }

    protected ScriptEvaluator getScriptEvaluator() {
        // construct script evaluator lazily
        if (scriptEvaluator == null) {
            scriptEvaluator = new ScriptEvaluator();
        }
        return scriptEvaluator;
    }

    /* Internals only below this point */
    private static Command[] emptyCommandArray = new Command[0];
    private Reader reader;
    private ShowBuilder builder;
    private Map<String, SENode> idToNode;
    private SEShow show;
    private ShowType jaxbShow;
    private List<Resolver> featureResolvers;
    private List<Resolver> rcHandlerResolvers;
    private List<Resolver> commandResolvers;
    private Map<String, Command> namedCommands;
    private ScriptEvaluator scriptEvaluator;
}
