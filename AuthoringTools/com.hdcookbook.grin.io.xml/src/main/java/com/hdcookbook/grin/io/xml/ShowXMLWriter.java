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

import com.hdcookbook.grin.AbstractSEShowVisitor;
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
import com.hdcookbook.grin.input.VisualRCHandler;
import com.hdcookbook.grin.io.builders.FeatureRef;
import com.hdcookbook.grin.io.builders.MenuAssemblyHelper;
import com.hdcookbook.grin.io.builders.MenuAssemblyHelper.Features;
import com.hdcookbook.grin.io.builders.VisualRCHandlerCell;
import com.hdcookbook.grin.io.builders.VisualRCHandlerHelper;
import com.hdcookbook.grin.io.text.SEGenericCommand;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

/**
 * This visitor converts a GRIN Show object to a JAXB generated object
 * which can be serialized into a Show XML document.
 * 
 * @author A. Sundararajan
 */
public class ShowXMLWriter extends AbstractSEShowVisitor {
    public static final String GRIN_XML_URI = "http://grin.hdcookbook.com/io/xml";
    public static final String GRIN_XML_ROOT = "show";
    
    public ShowXMLWriter(Writer out) {
        this.out = out;
    }

    @Override
    public void visitShow(SEShow show) {
        this.show = show;
        this.jaxbShow = new ShowType();

        int stackDepth = show.getSegmentStackDepth();
        if (stackDepth != 0) {
            getJaxbShow().setSegmentStackDepth(stackDepth);
        }
        List<String> dt = getJaxbShow().getDrawTargets();
        String[] drawTargets = show.getDrawTargets();
        if (drawTargets != null && drawTargets.length != 0) {
            for (String s : show.getDrawTargets()) {
                dt.add(s);
            }
        }

        SEShowCommands showCommands = show.getShowCommands();
        if (showCommands != null) {
            String code = showCommands.getOriginalSource();
            if (code != null) {
                List<ScriptType> scripts = getJaxbShow().getScript();

                if (code != null & code.length() != 0) {
                    ScriptType st = new ScriptType();
                    st.setValue(code);
                    st.setRunat("runtime");
                    st.setType("text/java");
                    scripts.add(st);
                }
            }
        }

        Feature[] allFeatures = show.getFeatures();
        getJaxbShow().setFeatures(new FeatureListType());
        List jaxbFeatures = getJaxbShow().getFeatures().getFeatureChoiceGroup();
        for (Feature f : allFeatures) {
            setCurJaxbFeature(null);
            if (f.getName() != null) {
                SEShow.acceptFeature(this, f);
            }
            if (getCurJaxbFeature() != null) {
                jaxbFeatures.add(getCurJaxbFeature());
            }
        }

        RCHandler[] allRCHandlers = show.getRCHandlers();
        getJaxbShow().setRcHandlers(new RCHandlerListType());
        List jaxbRCHandlers = getJaxbShow().getRcHandlers().getRcHandler();
        for (RCHandler rch : allRCHandlers) {
            setCurJaxbRCHandler(null);
            SEShow.acceptRCHandler(this, rch);
            if (getCurJaxbRCHandler() != null) {
                jaxbRCHandlers.add(getCurJaxbRCHandler());
            }
        }

        getJaxbShow().setSegments(new ShowType.Segments());
        List<SegmentType> jaxbSegments = getJaxbShow().getSegments().getSegment();
        for (Segment seg : show.getSegments()) {
            setCurJaxbSegment(null);
            visitSegment((SESegment) seg);
            if (getCurJaxbSegment() != null) {
                jaxbSegments.add(getCurJaxbSegment());
            }
        }

        String[] stickyImages = show.getStickyImages();
        if (stickyImages != null && stickyImages.length != 0) {
           getJaxbShow().setStickyImages(new ShowType.StickyImages());
           List<String> items = getJaxbShow().getStickyImages().getItem();
           for (String src : stickyImages) {
               items.add(src);
           }
        }
        marshall();
    }

    @Override
    public void visitSegment(SESegment segment) {
        setCurJaxbSegment(new SegmentType());
        setSegmentProperties(getCurJaxbSegment(),segment);
        Feature[] active = segment.getActiveFeatures();
        if (active != null && active.length != 0) {
            getCurJaxbSegment().setActive(new FeatureRefListType());
            List<PartType> parts = getCurJaxbSegment().getActive().getFeature();
            for (Feature f : active) {
                PartType pt = new PartType();
                if (f.getName() != null) {
                    pt.setRef(getFeatureNode(f));
                } else {
                    // inlined anonymous feature
                    pt.setFeatureChoiceGroup(visitAnonymousFeature(f));
                }
                parts.add(pt);
            }
        }

        Feature[] setup = segment.getSetupFeatures();
        if (setup != null && setup.length != 0) {
            getCurJaxbSegment().setSetup(new FeatureRefListType());
            List<PartType> parts = getCurJaxbSegment().getSetup().getFeature();
            for (Feature f : setup) {
                PartType pt = new PartType();
                if (f.getName() != null) {
                    pt.setRef(getFeatureNode(f));
                } else {
                    // inlined anonymous feature
                    pt.setFeatureChoiceGroup(visitAnonymousFeature(f));
                }
                parts.add(pt);
            }
        }

        RCHandler[] rcHandlers = segment.getRCHandlers();
        if (rcHandlers != null && rcHandlers.length != 0) {
            getCurJaxbSegment().setRcHandlers(new RCHandlerRefListType());

            List<RCHandlerRefListType.RcHandler> handlers = getCurJaxbSegment().getRcHandlers().getRcHandler();
            for (RCHandler rh : rcHandlers) {
                RCHandlerRefListType.RcHandler handler = new RCHandlerRefListType.RcHandler();
                handler.setRef(getRCHandlerNode(rh));
                handlers.add(handler);
            }
        }

        boolean nextOnSetupDone = segment.getNextOnSetupDone();
        Command[] nextCommands = segment.getNextCommands();
        if (nextOnSetupDone) {
            getCurJaxbSegment().setSetupDone(makeCommandList(nextCommands));
        } else {
            getCurJaxbSegment().setNext(makeCommandList(nextCommands));
        }
        Command[] entryCommands = segment.getOnEntryCommands();
        getCurJaxbSegment().setOnEntry(makeCommandList(entryCommands));
    }

    @Override
    public void visitAssembly(SEAssembly assembly) {
        AssemblyType at = new AssemblyType();
        setCurJaxbFeature(at);
        setFeatureProperties(at, assembly);
        Feature[] parts = assembly.getParts();
        String[] partNames = assembly.getPartNames();
        List<AssemblyType.Part> jaxbParts = at.getPart();
        for (int index = 0; index < parts.length; index++) {
            AssemblyType.Part jaxbPart = new AssemblyType.Part();
            jaxbPart.setName(partNames[index]);
            Feature part = parts[index];
            if (part.getName() != null) {
                jaxbPart.setRef(getFeatureNode(part));
            } else {
                // inlined anonymous part
                jaxbPart.setFeatureChoiceGroup(visitAnonymousFeature(part));
            }
            jaxbParts.add(jaxbPart);
        }
    }

    @Override
    public void visitBox(SEBox box) {
        BoxType bt = new BoxType();
        setCurJaxbFeature(bt);
        setFeatureProperties(bt, box);
        bt.setX(box.getX());
        bt.setY(box.getY());
        bt.setWidth(box.getWidth());
        bt.setHeight(box.getHeight());
        int outlineWidth = box.getOutlineWidth();
        if (outlineWidth != 0) {
            bt.setOutline(outlineWidth);
        }
        Color fillColor = box.getFillColor();
        if (fillColor != null) {
            bt.setFillColor(colorToString(fillColor));
        }
        Color outlineColor = box.getOutlineColor();
        if (outlineColor != null) {
            bt.setOutlineColor(colorToString(outlineColor));
        }
        Feature scaleModel = box.getScalingModel();
        if (scaleModel != null) {
            bt.setScaleModel(getFeatureNode(scaleModel));
        }
    }

    @Override
    public void visitClipped(SEClipped clipped) {
        ClippedType ct = new ClippedType();
        setCurJaxbFeature(ct);
        setModifierProperties(ct, clipped);
        Rectangle rect = clipped.getClipRegion();
        ct.setX((int) rect.getX());
        ct.setY((int) rect.getY());
        ct.setWidth((int) rect.getWidth());
        ct.setHeight((int) rect.getHeight());
    }

    @Override
    public void visitFade(SEFade fade) {
        FadeType ft = new FadeType();
        setCurJaxbFeature(ft);
        setModifierProperties(ft, fade);
        boolean srcOver = fade.getSrcOver();
        if (srcOver) {
            ft.setSrcOver(srcOver);
        }
        int repeatFrame = fade.getRepeatFrame();
        if (repeatFrame != Integer.MAX_VALUE) {
            ft.setRepeatFrame(repeatFrame);
        }
        ft.setLoopCount(loopCountToString(fade.getLoopCount()));
        ft.setEndCommands(makeCommandList(fade.getEndCommands()));
        int[] frames = fade.getKeyframes();
        int[] alphas = fade.getKeyAlphas();
        FadeType.Keyframes keyFrames = new FadeType.Keyframes();
        List list = keyFrames.getKeyframe();
        for (int index = 0; index < frames.length; index++) {
            FadeType.Keyframes.Keyframe kf = new FadeType.Keyframes.Keyframe();
            kf.setFrame(frames[index]);
            kf.setAlpha("#" + Integer.toHexString(alphas[index]));
            list.add(kf);
        }
        ft.setKeyframes(keyFrames);
    }

    @Override
    public void visitFixedImage(SEFixedImage image) {
        FixedImageType fit = new FixedImageType();
        setCurJaxbFeature(fit);
        setFeatureProperties(fit, image);
        fit.setSrc(image.getFileName());
        Feature scaleModel = image.getScalingModel();
        if (scaleModel != null) {
            fit.setScaleModel(getFeatureNode(scaleModel));
        }
        SEImagePlacement placement = image.getPlacement();
        fit.setX(placement.getX());
        fit.setY(placement.getY());
        fit.setXAlign(convertXAlignment(placement.getXAlign()));
        fit.setYAlign(convertYAlignment(placement.getYAlign()));
        int xScale = (int) (placement.getScaleX() * 1000.0);
        if (xScale != 1000) {
            fit.setXScale(xScale);
        }
        int yScale = (int) (placement.getScaleY() * 1000.0);
        if (yScale != 1000) {
            fit.setYScale(yScale);
        }
    }

    @Override
    public void visitGroup(SEGroup group) {
        GroupType gt = new GroupType();
        setCurJaxbFeature(gt);
        setFeatureProperties(gt, group);
        Feature[] parts = group.getParts();
        List<PartType> partList = gt.getPart();
        for (Feature f : parts) {
            PartType pt = new PartType();
            if (f.getName() != null) {
                pt.setRef(getFeatureNode(f));
            } else {
                //  inlined anonymous part
                pt.setFeatureChoiceGroup(visitAnonymousFeature(f));
            }
            partList.add(pt);
        }
    }

    @Override
    public void visitGuaranteeFill(SEGuaranteeFill gfill) {
        GuaranteeFillType gft = new GuaranteeFillType();
        setCurJaxbFeature(gft);
        setModifierProperties(gft, gfill);
        Rectangle g = gfill.getGuaranteed();
        gft.setX((int) g.getX());
        gft.setY((int) g.getY());
        gft.setWidth((int) g.getWidth());
        gft.setHeight((int) g.getHeight());
        List<GuaranteeFillType.FillRect> fillRects = gft.getFillRect();
        for (Rectangle r : gfill.getFills()) {
            GuaranteeFillType.FillRect fr = new GuaranteeFillType.FillRect();
            fr.setX((int) g.getX());
            fr.setY((int) g.getY());
            fr.setWidth((int) g.getWidth());
            fr.setHeight((int) g.getHeight());
            fillRects.add(fr);
        }
    }

    @Override
    public void visitImageSequence(SEImageSequence is) {
        ImageSequenceType ist = new ImageSequenceType();
        setCurJaxbFeature(ist);
        setFeatureProperties(ist, is);
        boolean repeat = is.getRepeat();
        if (repeat) {
            ist.setRepeat(repeat);
        }
        Feature scaleModel = is.getScalingModel();
        if (scaleModel != null) {
            ist.setScaleModel(getFeatureNode(scaleModel));
        }
        Feature model = is.getModel();
        if (model != null) {
            ist.setModel(getFeatureNode(model));
        }
        ist.setLoopCount(loopCountToString(is.getLoopCount()));
        SEImageSeqPlacement seqPlacement = is.getPlacement();
        boolean singlePlacement = (seqPlacement instanceof SEImagePlacement);
        if (singlePlacement) {
            // placement specified only once and shared by all images
            SEImagePlacement placement = (SEImagePlacement) seqPlacement;
            ist.setX(placement.getX());
            ist.setY(placement.getY());
            ist.setXAlign(convertXAlignment(placement.getXAlign()));
            ist.setYAlign(convertYAlignment(placement.getYAlign()));
            int xScale = (int) (placement.getScaleX() * 1000.0);
            if (xScale != 1000) {
                ist.setXScale(xScale);
            }
            int yScale = (int) (placement.getScaleY() * 1000.0);
            if (yScale != 1000) {
                ist.setYScale(yScale);
            }
        }
        ImageSequenceType.Images images = new ImageSequenceType.Images();
        ist.setImages(images);
        Iterator<SEImagePlacement> placementsItr = null;
        // placement specified for each image separately
        if (!singlePlacement) {
            placementsItr = ((SEImagePlacementList) seqPlacement).getPlacements().iterator();
        }
        for (String fileName : is.getFileNames()) {
            ImageSequenceType.Images.Img im = new ImageSequenceType.Images.Img();
            im.setSrc(fileName);
            if (!singlePlacement) {
                SEImagePlacement placement = placementsItr.next();
                im.setX(placement.getX());
                im.setY(placement.getY());
                im.setXAlign(convertXAlignment(placement.getXAlign()));
                im.setYAlign(convertYAlignment(placement.getYAlign()));
                int xScale = (int) (placement.getScaleX() * 1000.0);
                if (xScale != 1000) {
                    im.setXScale(xScale);
                }
                int yScale = (int) (placement.getScaleY() * 1000.0);
                if (yScale != 1000) {
                    im.setYScale(yScale);
                }
            }
            images.getImg().add(im);
        }
        CommandListType clt = makeCommandList(is.getEndCommands());
        ist.setEndCommands(clt);
    }

    /**
     * Handling of extension feature has to be revisited!
     *
     *  -- how to allow user specified elements/attributes for extensions?
     *  -- how ShowXML schema has to be relaxed for the same?
     *  -- how extension specific XML fragment serialization/de-serialization
     *     has to be handled?
     *
     * Currently, we generate generic &lt;ext-modifier&gt; or &lt;ext-feature&gt; 
     * element for extension modifiers and features. These elements have
     * &lt;param&gt; sub-elements which have name, value attributes. These
     * named parameters are string type valued. We use JavaBean convensions
     * to get parameter values from extension feature node and serialize those
     * as "param" tags.
     */
    @Override
    public void visitUserDefinedFeature(Feature feature) {
        FeatureType ft = null;
        List<ParamType> params = null;
        if (feature instanceof Modifier) {
            ExtensionModifierType emt = new ExtensionModifierType();
            emt.setClazz(feature.getClass().getName());
            Modifier m = (Modifier) feature;
            setFeatureProperties(emt, feature);
            Feature part = m.getPart();
            PartType pt = new PartType();
            if (part.getName() != null) {
                pt.setRef(getFeatureNode(m.getPart()));
            } else {
                // anonymous inlined feature
                pt.setFeatureChoiceGroup(visitAnonymousFeature(part));
            }
            emt.setPart(pt);
            ft = emt;
            params = emt.getParam();
        } else {
            ExtensionFeatureType eft = new ExtensionFeatureType();
            eft.setClazz(feature.getClass().getName());
            setFeatureProperties(eft, feature);
            ft = eft;
            params = eft.getParam();
        }
        fillBeanProperties(params, (SENode) feature);
        setCurJaxbFeature(ft);
    }

    @Override
    public void visitSetTarget(SESetTarget setTarget) {
        SetTargetType stt = new SetTargetType();
        setCurJaxbFeature(stt);
        setModifierProperties(stt, setTarget);
        String[] drawTargets = getShow().getDrawTargets();
        stt.setTarget(drawTargets[setTarget.getTarget()]);
    }

    @Override
    public void visitSrcOver(SESrcOver srcOver) {
        SrcOverType sot = new SrcOverType();
        setCurJaxbFeature(sot);
        setModifierProperties(sot, srcOver);
    }

    @Override
    public void visitText(SEText text) {
        TextType tt = new TextType();
        setCurJaxbFeature(tt);
        setFeatureProperties(tt, text);
        tt.setX(text.getXArg());
        tt.setY(text.getYArg());
        int alignment = text.getAlignment();
        int xAlign = (alignment & 0x03);
        switch (xAlign) {
            case SEText.LEFT:
                // This is default
                //tt.setXAlign(XAlignmentType.LEFT);
                break;
            case SEText.MIDDLE:
                tt.setXAlign(XAlignmentType.MIDDLE);
                break;
            case SEText.RIGHT:
                tt.setXAlign(XAlignmentType.RIGHT);
        }
        int yAlign = (alignment & 0x0C);
        switch (yAlign) {
            case SEText.TOP:
                // This is default
                // tt.setYAlign("top");
                break;
            case SEText.BASELINE:
                tt.setYAlign(TextYAlignmentType.BASELINE);
                break;
            case SEText.BOTTOM:
                tt.setYAlign(TextYAlignmentType.BOTTOM);
                break;
        }
        int vspace = text.getVspace();
        if (vspace != 0) {
            tt.setVspace(vspace);
        }
        int loopCount = text.getLoopCount();
        tt.setLoopCount(loopCountToString(loopCount));
        Color bgColor = text.getBackground();
        if (bgColor != null) {
            tt.setBackground(colorToString(bgColor));
        }

        List<String> clrList = tt.getColors();
        for (Color c : text.getColors()) {
            clrList.add(colorToString(c));
        }
        StringBuilder buf = new StringBuilder();
        for (String s : text.getStrings()) {
            buf.append(s);
            buf.append('\n');
        }
        tt.setValue(buf.toString());

        Font font = text.getFont();
        tt.setFont(font.getName());
        FontStyleType fst = null;
        if (font.isPlain()) {
            fst = FontStyleType.PLAIN;
        } else if (font.isBold()) {
            if (font.isItalic()) {
                fst = FontStyleType.BOLD_ITALIC;
            } else {
                fst = FontStyleType.BOLD;
            }
        } else {
            fst = FontStyleType.ITALIC;
        }
        tt.setStyle(fst);
        tt.setSize(font.getSize());
    }

    @Override
    public void visitTranslator(SETranslator trans) {
        TranslatorType tt = new TranslatorType();
        setCurJaxbFeature(tt);
        setModifierProperties(tt, trans);
        tt.setModel(getFeatureNode(trans.getModel()));
    }

    @Override
    public void visitInterpolatedModel(SEInterpolatedModel model) {
        InterpolatedModelType imt = new InterpolatedModelType();
        setCurJaxbFeature(imt);
        setFeatureProperties(imt, model);
        InterpolatedModelType.Keyframes keyFrames = new InterpolatedModelType.Keyframes();
        int[] frames = model.getFrames();
        int[][] values = model.getValues();
        for (int index = 0; index < frames.length; index++) {
            InterpolatedModelType.Keyframes.Keyframe kf = new InterpolatedModelType.Keyframes.Keyframe();
            kf.setFrame(frames[index]);
            List<Integer> valueList = kf.getValues();
            for (int i = 0; i < values.length; i++) {
                valueList.add(values[i][index]);
            }
            keyFrames.getKeyframe().add(kf);
        }
        imt.setKeyframes(keyFrames);
        int repeatFrame = model.getRepeatFrame();
        if (repeatFrame == Integer.MAX_VALUE) {
            imt.setRepeatFrame(repeatFrame);
        }
        int loopCount = model.getLoopCount();
        imt.setLoopCount(loopCountToString(loopCount));
        CommandListType clt = makeCommandList(model.getEndCommands());
        imt.setEndCommands(clt);
    }

    @Override
    public void visitTimer(SETimer timer) {
        TimerType tt = new TimerType();
        setCurJaxbFeature(tt);
        setFeatureProperties(tt, timer);
        tt.setNumFrames(timer.getNumFrames());
        int repeatFrame = timer.getRepeatFrame();
        if (repeatFrame == 0) {
            tt.setRepeat(true);
        }

        CommandListType clt = makeCommandList(timer.getEndCommands());
        tt.setEndCommands(clt);
    }

    @Override
    public void visitTranslatorModel(SETranslatorModel model) {
        TranslatorModelType tmt = new TranslatorModelType();
        setCurJaxbFeature(tmt);
        setFeatureProperties(tmt, model);
        if (!model.getIsRelative()) {
            tmt.setRelative(true);
        }
        TranslatorModelType.Keyframes keyFrames = new TranslatorModelType.Keyframes();
        int[] frames = model.getFrames();
        int[] xs = model.getXs();
        int[] ys = model.getYs();
        for (int index = 0; index < frames.length; index++) {
            TranslatorModelType.Keyframes.Keyframe kf = new TranslatorModelType.Keyframes.Keyframe();
            kf.setFrame(frames[index]);
            kf.setX(xs[index]);
            kf.setY(ys[index]);
            keyFrames.getKeyframe().add(kf);
        }
        tmt.setKeyframes(keyFrames);
        int repeatFrame = model.getRepeatFrame();
        if (repeatFrame != Integer.MAX_VALUE) {
            tmt.setRepeatFrame(repeatFrame);
        }
        int loopCount = model.getLoopCount();
        tmt.setLoopCount(loopCountToString(loopCount));
        CommandListType clt = makeCommandList(model.getEndCommands());
        tmt.setEndCommands(clt);
    }

    @Override
    public void visitScalingModel(SEScalingModel model) {
        ScalingModelType smt = new ScalingModelType();
        setCurJaxbFeature(smt);
        setFeatureProperties(smt, model);
        ScalingModelType.Keyframes keyFrames = new ScalingModelType.Keyframes();
        int[] frames = model.getFrames();
        int[] xs = model.getXs();
        int[] ys = model.getYs();
        int[] xscales = model.getScaleXs();
        int[] yscales = model.getScaleYs();
        for (int index = 0; index < frames.length; index++) {
            ScalingModelType.Keyframes.Keyframe kf = new ScalingModelType.Keyframes.Keyframe();
            kf.setFrame(frames[index]);
            kf.setX(xs[index]);
            kf.setY(ys[index]);
            kf.setXScale(xscales[index]);
            kf.setYScale(yscales[index]);
            keyFrames.getKeyframe().add(kf);
        }
        smt.setKeyframes(keyFrames);
        int repeatFrame = model.getRepeatFrame();
        if (repeatFrame != Integer.MAX_VALUE) {
            smt.setRepeatFrame(repeatFrame);
        }
        int loopCount = model.getLoopCount();
        smt.setLoopCount(loopCountToString(loopCount));
        CommandListType clt = makeCommandList(model.getEndCommands());
        smt.setEndCommands(clt);
    }

    @Override
    public void visitMenuAssembly(SEMenuAssembly assembly) {
        MenuAssemblyType mat = new MenuAssemblyType();
        setCurJaxbFeature(mat);
        setFeatureProperties(mat, assembly);
        MenuAssemblyHelper helper = assembly.getHelper();
        if (helper == null) {
            throw new RuntimeException("can not handler menu assembly without helper");
        }
        MenuAssemblyType.Template jaxbTemplate = new MenuAssemblyType.Template();
        mat.setTemplate(jaxbTemplate);
        List<FeatureSetType> jaxbItems = jaxbTemplate.getItem();
        List<Features> template = helper.template;
        for (Features fs : template) {
            FeatureSetType fst = new FeatureSetType();
            fst.setId(fs.id);
            List<PartType> list = fst.getPart();
            for (FeatureRef fr : fs.features) {
                try {
                    PartType pt = new PartType();
                    Feature f = fr.getFeature();
                    if (f.getName() != null) {
                        pt.setRef(getFeatureNode(fr.getFeature()));
                    } else {
                        // anonumous inlined feature
                        pt.setFeatureChoiceGroup(visitAnonymousFeature(f));
                    }
                    list.add(pt);
                } catch (IOException exp) {
                    throw wrapException(exp);
                }
            }
            jaxbItems.add(fst);
        }
        MenuAssemblyType.Parts jaxbParts = new MenuAssemblyType.Parts();
        mat.setParts(jaxbParts);
        List<MenuAssemblyType.Parts.Part> jaxbPartList = jaxbParts.getPart();
        Iterator<String> partNamesItr = helper.partNames.iterator();
        List<List<Features>> parts = helper.parts;
        Iterator<List<Features>> partsItr = parts.iterator();
        while (partNamesItr.hasNext()) {
            MenuAssemblyType.Parts.Part jaxbPart = new MenuAssemblyType.Parts.Part();
            jaxbPart.setName(partNamesItr.next());
            List<FeatureSetType> jaxbReplacements = jaxbPart.getReplacement();
            for (Features fs : partsItr.next()) {
                FeatureSetType fst = new FeatureSetType();
                fst.setId(fs.id);
                List<PartType> partList = fst.getPart();
                for (FeatureRef fr : fs.features) {
                    try {
                        PartType pt = new PartType();
                        Feature f = fr.getFeature();
                        if (f.getName() != null) {
                            pt.setRef(getFeatureNode(fr.getFeature()));
                        } else {
                            // anonumous inlined feature
                            pt.setFeatureChoiceGroup(visitAnonymousFeature(f));
                        }
                        partList.add(pt);
                    } catch (IOException exp) {
                        throw wrapException(exp);
                    }
                }
                jaxbReplacements.add(fst);
            }
            jaxbPartList.add(jaxbPart);
        }
    }

    @Override
    public void visitCommandRCHandler(SECommandRCHandler handler) {
        CommandRCHandlerType ct = new CommandRCHandlerType();
        setCurJaxbRCHandler(ct);
        setRCHandlerProperties(ct, handler);
        ct.setExecute(makeCommandList(handler.getCommands()));
        int mask = handler.getMask();
        Vector<RCKeyEvent> keyEvents = RCKeyEvent.getEventsFromMask(mask);
        for (RCKeyEvent key : keyEvents) {
            // keep this naming convention in sync with schema document
            ct.getKeys().add(key.getName().replace('_', '-'));
        }
    }

    @Override
    public void visitVisualRCHandler(SEVisualRCHandler handler) {
        VisualRCHandlerType vrct = new VisualRCHandlerType();
        setCurJaxbRCHandler(vrct);
        setRCHandlerProperties(vrct, handler);
        VisualRCHandlerHelper helper = handler.getHelper();
        if (helper == null) {
            throw new RuntimeException("can not handler SEVisualRCHandler without helper");
        }
        SEAssembly assembly = (SEAssembly) handler.getAssembly();
        if (assembly != null) {
            vrct.setAssembly(getFeatureNode(assembly));
        }
        ArrayList<ArrayList<VisualRCHandlerCell>> grid = helper.getGrid();
        VisualRCHandlerType.Grid jaxbGrid = new VisualRCHandlerType.Grid();
        vrct.setGrid(jaxbGrid);
        List<VisualRCHandlerType.Grid.Row> jaxbRowList = jaxbGrid.getRow();
        Command[][] selectCommands = handler.getSelectCommands();
        Command[][] activateCommands = handler.getActivateCommands();
        Feature[] activateFeatures = handler.getActivateFeatures();
        Feature[] selectFeatures = handler.getSelectFeatures();
        int[] mouseRectStates = handler.getMouseRectStates();
        Rectangle[] mouseRects = handler.getMouseRects();
        for (ArrayList<VisualRCHandlerCell> row : grid) {
            VisualRCHandlerType.Grid.Row jaxbRow = new VisualRCHandlerType.Grid.Row();
            jaxbRowList.add(jaxbRow);
            List<VisualRCHandlerCellType> jaxbCells = jaxbRow.getCells();
            for (VisualRCHandlerCell cell : row) {
                if (cell instanceof VisualRCHandlerCell.ActivateCell) {
                    jaxbCells.add(new ActivateCellType());
                } else if (cell instanceof VisualRCHandlerCell.NullCell) {
                    jaxbCells.add(new NullCellType());
                } else if (cell instanceof VisualRCHandlerCell.LocationRefCell) {
                    VisualRCHandlerCell.LocationRefCell locationRef = (VisualRCHandlerCell.LocationRefCell) cell;
                    LocationRefCellType lct = new LocationRefCellType();
                    lct.setX(locationRef.getX());
                    lct.setY(locationRef.getY());
                    jaxbCells.add(lct);
                } else if (cell instanceof VisualRCHandlerCell.StateRefCell) {
                    VisualRCHandlerCell.StateRefCell stateRef = (VisualRCHandlerCell.StateRefCell) cell;
                    StateRefCellType srt = new StateRefCellType();
                    srt.setState(stateRef.getName());
                    jaxbCells.add(srt);
                } else if (cell instanceof VisualRCHandlerCell.WallCell) {
                    jaxbCells.add(new WallCellType());
                } else if (cell instanceof VisualRCHandlerCell.StateCell) {
                    VisualRCHandlerCell.StateCell state = (VisualRCHandlerCell.StateCell) cell;
                    String stateName = state.getState();
                    int stateNum = helper.getStates().get(stateName);
                    StateCellType sct = new StateCellType();
                    sct.setId(stateName);
                    if (activateCommands != null) {
                        sct.setActivateCommands(makeCommandList(activateCommands[stateNum]));
                    }
                    if (selectCommands != null) {
                        sct.setSelectCommands(makeCommandList(selectCommands[stateNum]));
                    }
                    if (assembly != null) {
                        if (activateFeatures != null) {
                            String activatePart = assembly.getPartName(activateFeatures[stateNum]);
                            sct.setActivate(activatePart);
                        }
                        if (selectFeatures != null) {
                            String selectPart = assembly.getPartName(selectFeatures[stateNum]);
                            sct.setSelect(selectPart);
                        }
                    }
                    for (int index = 0; index < mouseRectStates.length; index++) {
                        int mouseState = mouseRectStates[index];
                        if (mouseState == stateNum) {
                            Rectangle mouseRect = mouseRects[index];
                            StateCellType.MouseRect jaxbMouseRect = new StateCellType.MouseRect();
                            jaxbMouseRect.setX((int) mouseRect.getX());
                            jaxbMouseRect.setY((int) mouseRect.getY());
                            jaxbMouseRect.setWidth((int) mouseRect.getWidth());
                            jaxbMouseRect.setHeight((int) mouseRect.getHeight());
                            sct.setMouseRect(jaxbMouseRect);
                            break;
                        }
                    }

                    Map<String, String> rcOverrides = helper.getRCOverrides();
                    sct.setUp(rcOverrides.get("up:" + stateName));
                    sct.setDown(rcOverrides.get("down:" + stateName));
                    sct.setLeft(rcOverrides.get("left:" + stateName));
                    sct.setRight(rcOverrides.get("right:" + stateName));
                    jaxbCells.add(sct);
                } else {
                    throw new RuntimeException("unknown cell type: " + cell.getClass());
                }
            }
        }
        boolean startSelected = handler.getStartSelected();
        if (startSelected) {
            vrct.setStartSelected(startSelected);
        }
        int time = handler.getTimeout();
        if (time != -1) {
            VisualRCHandlerType.Timeout timeout = new VisualRCHandlerType.Timeout();
            timeout.setFrames(time);
            List<CommandType> jaxbCommands = timeout.getCommands();
            for (Command c : handler.getTimeoutCommands()) {
                setCurJaxbCommand(null);
                SEShow.acceptCommand(this, c);
                if (getCurJaxbCommand() != null) {
                    jaxbCommands.add(getCurJaxbCommand());
                }
            }
            vrct.setTimeout(timeout);
        }
    }

    @Override
    public void visitActivatePartCommand(SEActivatePartCommand command) {
        ActivatePartCommandType apct = new ActivatePartCommandType();
        setCurJaxbCommand(apct);
        SEAssembly assembly = (SEAssembly) command.getAssembly();
        String partName = assembly.getPartName(command.getPart());
        apct.setAssembly(getFeatureNode(command.getAssembly()));
        apct.setPartName(partName);
    }

    @Override
    public void visitActivateSegmentCommand(SEActivateSegmentCommand command) {
        ActivateSegmentCommandType asct = new ActivateSegmentCommandType();
        setCurJaxbCommand(asct);
        Segment segment = command.getSegment();
        if (segment != null) {
            asct.setSegment(getSegmentNode(segment));
        }
        boolean push = command.getPush();
        if (push) {
            asct.setPush(push);
        }

        boolean pop = command.getPop();
        if (pop) {
            asct.setPop(pop);
        }
    }

    @Override
    public void visitSegmentDoneCommand(SESegmentDoneCommand command) {
        setCurJaxbCommand(new SegmentDoneCommandType());
    }

    @Override
    public void visitSetVisualRCStateCommand(SESetVisualRCStateCommand command) {
        SetVisualRCStateCommandType ct = new SetVisualRCStateCommandType();
        setCurJaxbCommand(ct);
        VisualRCHandler rcHandler = command.getVisualRCHandler();
        ct.setRcHandler(getRCHandlerNode(rcHandler));
        boolean runCommands = command.getRunCommands();
        if (runCommands) {
            ct.setRunCommands(runCommands);
        }

        ct.setActivate(command.getActivated());
        ct.setState(rcHandler.getStateName(command.getState()));
    }

    @Override
    public void visitResetFeatureCommand(SEResetFeatureCommand command) {
        ResetFeatureCommandType ct = new ResetFeatureCommandType();
        setCurJaxbCommand(ct);
        ct.setFeature(getFeatureNode(command.getFeature()));
    }

    @Override
    public void visitShowCommand(SEShowCommand command) {
        ScriptCommandType sct = new ScriptCommandType();
        setCurJaxbCommand(sct);
        sct.setScript(new ScriptType());
        
        String code = command.getOriginalSource();
        if (code != null && code.length() != 0) {
            ScriptType st = sct.getScript();
            st.setValue(code);
            st.setRunat("runtime");
            st.setType("text/java");
        }
    }

    
    /**
     * Handling of extension commands has to be revisited!
     *
     *  -- how to allow user specified elements/attributes for extensions?
     *  -- how ShowXML schema has to be relaxed for the same?
     *  -- how to handle XML fragment serialization/de-serialization
     *     for extensions?
     *
     * Currently, we generate generic &lt;ext-command&gt; 
     * element for extension modifiers and features. These elements have
     * &lt;param&gt; sub-elements which have name, value attributes. These
     * named parameters are string type valued. We use JavaBean convensions
     * to get parameter values from extension feature node and serialize those
     * as "param" tags.
     */
    @Override
    public void visitUserDefinedCommand(Command command) {
        ExtensionCommandType ect = new ExtensionCommandType();
        List<ParamType> params = ect.getParam();
        fillBeanProperties(params, (SENode) command);
        setCurJaxbCommand(ect);
    }
    
    /* protected stuff below this point */
    
    protected Writer getOut() {
        return out;
    }

    protected SEShow getShow() {
        return show;
    }

    protected ShowType getJaxbShow() {
        return jaxbShow;
    }

    protected FeatureType getCurJaxbFeature() {
        return curJaxbFeature;
    }

    protected void setCurJaxbFeature(FeatureType curJaxbFeature) {
        this.curJaxbFeature = curJaxbFeature;
    }

    protected CommandType getCurJaxbCommand() {
        return curJaxbCommand;
    }

    protected void setCurJaxbCommand(CommandType curJaxbCommand) {
        this.curJaxbCommand = curJaxbCommand;
    }

    protected RCHandlerType getCurJaxbRCHandler() {
        return curJaxbRCHandler;
    }

    protected void setCurJaxbRCHandler(RCHandlerType curJaxbRCHandler) {
        this.curJaxbRCHandler = curJaxbRCHandler;
    }

    protected SegmentType getCurJaxbSegment() {
        return curJaxbSegment;
    }

    protected void setCurJaxbSegment(SegmentType curJaxbSegment) {
        this.curJaxbSegment = curJaxbSegment;
    }
    
    protected FeatureType visitAnonymousFeature(Feature f) {
        FeatureType oldCurJaxbFeature = getCurJaxbFeature();
        SEShow.acceptFeature(this, f);
        FeatureType result = getCurJaxbFeature();
        setCurJaxbFeature(oldCurJaxbFeature);
        return result;
    }

    protected String loopCountToString(int loopCount) {
        return (loopCount == 1) ? null : ((loopCount == Integer.MAX_VALUE) ? "infinite" : Integer.toString(loopCount));
    }

    protected NodeType newNode(String id) {
        if (id == null) {
            return null;
        }
        NodeType nt = new NodeType();
        nt.setId(id);
        return nt;
    }

    protected NodeType getFeatureNode(Feature f) {
        return newNode(f.getName());
    }

    protected NodeType getSegmentNode(Segment s) {
        return newNode(s.getName());
    }

    protected NodeType getRCHandlerNode(RCHandler rh) {
        return newNode(rh.getName());
    }

    protected CommandListType makeCommandList(Command[] commands) {
        if (commands != null && commands.length != 0) {
            CommandListType clt = new CommandListType();
            for (Command c : commands) {
                setCurJaxbCommand(null);
                SEShow.acceptCommand(this, c);
                if (getCurJaxbCommand() != null) {
                    clt.getCommands().add(getCurJaxbCommand());
                }

            }
            return clt;
        } else {
            return null;
        }

    }

    protected XAlignmentType convertXAlignment(SEImagePlacement.HorizontalAlignment xalign) {
        XAlignmentType xAlign = XAlignmentType.valueOf(xalign.name());
        // ignore default
        return (xAlign == XAlignmentType.LEFT) ? null : xAlign;
    }

    protected YAlignmentType convertYAlignment(SEImagePlacement.VerticalAlignment yalign) {
        YAlignmentType yAlign = YAlignmentType.valueOf(yalign.name());
        // ignore default
        return (yAlign == YAlignmentType.TOP) ? null : yAlign;
    }

    protected String colorToString(Color c) {
        StringBuilder sb = new StringBuilder();
        sb.append('#');
        sb.append(twoHexDigits(c.getRed()));
        sb.append(twoHexDigits(c.getGreen()));
        sb.append(twoHexDigits(c.getBlue()));
        sb.append(twoHexDigits(c.getAlpha()));
        return sb.toString();
    }

    protected String twoHexDigits(int i) {
        assert i < 256 && i > -1 : "has to be in [0-255]";
        String str = Integer.toHexString(i);
        // prepend "0" if needed to make it 2 digits always
        return str.length() == 2 ? str : ("0" + str);
    }

    protected void setRCHandlerProperties(RCHandlerType rcht, RCHandler rch) {
        rcht.setId(rch.getName());
        boolean isPublic = getShow().isPublic(rch);
        if (isPublic) {
            rcht.setExport(true);
        }

    }

    protected void setSegmentProperties(SegmentType st, Segment s) {
        st.setId(s.getName());
        boolean isPublic = getShow().isPublic(s);
        if (isPublic) {
            st.setExport(true);
        }
    }

    protected void setFeatureProperties(FeatureType ft, Feature f) {
        ft.setId(f.getName());
        boolean isPublic = getShow().isPublic(f);
        if (isPublic) {
            ft.setExport(true);
        }

    }

    protected void setModifierProperties(ModifierType mt, Modifier m) {
        setFeatureProperties(mt, m);
        Feature part = m.getPart();
        PartType pt = new PartType();
        if (part.getName() != null) {
            pt.setRef(getFeatureNode(part));
        } else {
            // inlined anonymous part
            pt.setFeatureChoiceGroup(visitAnonymousFeature(part));
        }
        mt.setPart(pt);
    }

    protected void fillBeanProperties(List<ParamType> params, SENode grinObject) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(grinObject.getClass());
        } catch (Exception exp) {
            // exp.printStackTrace();
            return;
        }
        PropertyDescriptor[] propDescs = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor prop : propDescs) {
            Method reader = prop.getReadMethod();
            if (reader != null) {
                try {
                    reader.setAccessible(true);
                    Object value = reader.invoke(grinObject, (Object[]) null);
                    ParamType p = new ParamType();
                    if (value != null) {
                        p.setName(prop.getName());
                        p.setValue(value.toString());
                        params.add(p);
                    }
                } catch (Exception exp) {
                    // exp.printStackTrace();
                }
            }
        }
    }

    protected void marshall() {
        try {
            JAXBContext ctx = JAXBContext.newInstance("com.hdcookbook.grin.io.xml");
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    new Boolean(true));
            JAXBElement<ShowType> root = new JAXBElement<ShowType>(new QName(GRIN_XML_URI, GRIN_XML_ROOT),
                    ShowType.class,getJaxbShow());
            marshaller.marshal(root,getOut());
        } catch (Exception exp) {
            throw wrapException(exp);
        }

    }
    
    protected void warn(String msg) {
        System.err.println("WARNING: " + msg);
    }

    protected static RuntimeException wrapException(Exception exp) {
        if (exp instanceof RuntimeException) {
            return (RuntimeException) exp;
        } else {
            return new RuntimeException(exp);
        }
    }

    /* Internals only below this point */
    private final Writer out;
    private SEShow show;
    private ShowType jaxbShow;
    /* transient state to handle current node */
    private FeatureType curJaxbFeature;
    private CommandType curJaxbCommand;
    private RCHandlerType curJaxbRCHandler;
    private SegmentType curJaxbSegment;
}
