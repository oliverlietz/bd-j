
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

package com.hdcookbook.grin;

import com.hdcookbook.grin.SEShow;

import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.commands.SEActivatePartCommand;
import com.hdcookbook.grin.commands.SEActivateSegmentCommand;
import com.hdcookbook.grin.commands.SEResetFeatureCommand;
import com.hdcookbook.grin.commands.SERunNamedCommand;
import com.hdcookbook.grin.commands.SESegmentDoneCommand;
import com.hdcookbook.grin.commands.SESyncDisplayCommand;
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
import com.hdcookbook.grin.features.SETimer;
import com.hdcookbook.grin.features.SETranslatorModel;
import com.hdcookbook.grin.features.SESetTarget;
import com.hdcookbook.grin.features.SESrcOver;
import com.hdcookbook.grin.features.SEText;
import com.hdcookbook.grin.features.SETranslator;
import com.hdcookbook.grin.input.SECommandRCHandler;
import com.hdcookbook.grin.input.SEVisualRCHandler;


/**
 * A simple dummy implementation for visitor that does nothing on visit methods.
 **/
public abstract class AbstractSEShowVisitor implements SEShowVisitor {

    public void visitShow(SEShow show) {}

    public void visitSegment(SESegment segment) {}

    public void visitAssembly(SEAssembly feature) {}
    public void visitBox(SEBox feature) {}
    public void visitClipped(SEClipped feature) {}
    public void visitFade(SEFade feature) {}
    public void visitFixedImage(SEFixedImage feature) {}
    public void visitGroup(SEGroup feature) {}
    public void visitGuaranteeFill(SEGuaranteeFill feature) {}
    public void visitImageSequence(SEImageSequence feature) {}
    public void visitSetTarget(SESetTarget feature) {}
    public void visitSrcOver(SESrcOver feature) {}
    public void visitText(SEText feature) {}
    public void visitTranslator(SETranslator feature){}
    public void visitInterpolatedModel(SEInterpolatedModel feature) {}
    public void visitScalingModel(SEScalingModel feature) {
        visitInterpolatedModel(feature);
    }
    public void visitTimer(SETimer feature) {
        visitInterpolatedModel(feature);
    }
    public void visitTranslatorModel(SETranslatorModel feature) {
        visitInterpolatedModel(feature);
    }
    public void visitMenuAssembly(SEMenuAssembly feature) {
        visitAssembly(feature);
    }
    public void visitUserDefinedFeature(Feature feature) {}
    public void visitUserDefinedModifier(Modifier modifier) { }

    public void visitCommandRCHandler(SECommandRCHandler handler) {}
    public void visitVisualRCHandler(SEVisualRCHandler handler) {}
    
    public void visitActivatePartCommand(SEActivatePartCommand command) {}
    public void visitActivateSegmentCommand(SEActivateSegmentCommand command) {}
    public void visitSegmentDoneCommand(SESegmentDoneCommand command) {}
    public void visitSyncDisplayCommand(SESyncDisplayCommand command) {}
    public void visitRunNamedCommand(SERunNamedCommand command) {}
    public void visitSetVisualRCStateCommand(SESetVisualRCStateCommand cmd) {}
    public void visitResetFeatureCommand(SEResetFeatureCommand cmd) {}
    public void visitShowCommand(SEShowCommand command) {}
    public void visitUserDefinedCommand(Command command) {}
}
