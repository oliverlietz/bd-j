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
package com.hdcookbook.grin;

import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.io.binary.GrinDataOutputStream;
import java.io.IOException;

/**
 * Defines methods needed to compile Show into a binary file.
 * All show elements (Segments, Features, Commands, RCHandlers) that
 * are constructed on the SE side during show compilation are expected
 * to implement this interface.
 * 
 * @see Node
 */

public interface SENode {
    
    /**
     * Record this node information to the binary file format.  
     * 
     * An implementation of this method is recommended to call 
     * out.writeSuperClassData(this)
     * as the first line of the method to write out data
     * defined in the base class of this Node type. 
     * 
     * @param out The stream to write out.
     * @throws java.io.IOException if error occurs.
     * 
     * @see GrinDataOutputStream#writeSuperClassData(Feature)
     * @see GrinDataOutputStream#writeSuperClassData(RCHandler)
     * @see GrinDataOutputStream#writeSuperClassData(Segment)
     * @see GrinDataOutputStream#writeSuperClassData(Command)
     */
    public void writeInstanceData(GrinDataOutputStream out) 
            throws IOException;
    
    /**
     * Returns the class that should be constructed at Show runtime
     * to represent this SENode.
     * 
     * @return String A fully qualified classname of a Node at xlet runtime.
     */
    public String getRuntimeClassName();

    /**
     * Calls the visit method corresponding to this node type.
     * <p>
     * If you are defining a user-defined feature, there are some
     * restrictions that you'll want to follow.  If your extension is
     * a feature that has children, then you should make it a subclass
     * of one of the built-in feature types, Assembly, Modifer or Group.
     * That's because the GRIN compiler defines visitors that need to
     * visit every node in the tree, but these built-in visitors don't
     * know about your extension types.  By making your extension type
     * a subclass of one of the standard ones, and by making your
     * <code>accept()</code> method call either 
     * <code>visitAssembly()</code>, <code>visitGroup()</code> or
     * <code>visitUserDefinedModifier()</code>, as appropriate,
     * you'll ensure that all the children get visited.
     *
     * @param visitor SEShowVisitor object.
     *
     * @see SEShowVisitor
     */
    public void accept(SEShowVisitor visitor);

    /**
     * Do any needed post-processing in this show.  The grin compiler works
     * as follows:
     * <ul>
     *    <li>GRIN source file is read and tree is built in memory
     *    <li>Forward references are resolved (in multiple passes)
     *    <li>postProcess() is called on every node.  During the call,
     *        child feature nodes can be added, and parent features can
     *        be injected.
     *    <li>The double-use checker runs to ensure the graph structure
     *        is valid.
     *    <li>Deferred builders (e.g. TranslatorHelper) are called.
     *        Deferred builders cannot make changes to the tree structure.
     * </ul>
     * <p>
     * During the post-process phase, a node can add new features to the
     * show, by calling <code>ShowBuilder.addSyntheticFeature()</code>,
     * and it can insert a new feature as the parent of a given feature,
     * by calling <code>ShowBuilder.injectParent()</code>.  It can also
     * add segments and handlers by calling the appropriate builder methods.
     * <p>
     * When a parent is injected, the
     * ShowBuilder calls postProcess() on all nodes automatically,
     * including nodes that are created during the execution of postProcess()
     * in another node.
     *
     * @param   builder     The builder that holds state for the show
     *
     * @throws  IOException if an error is encountered
     *
     * @see ShowBuilder#addSyntheticFeature(Feature)
     * @see ShowBuilder#injectParent(Feature, Feature)
     **/
    public void postProcess(ShowBuilder builder) throws IOException;

    /**
     * Change a feature reference from one feature to a new one.  If your
     * node has a reference to the feature <code>from</code>, it should be
     * changed to refer to <code>to</code>.  This is called for every node
     * in an SEShow when <code>SENode.injectParent</code> is used.
     * <p>
     * The ShowBuilder calls changeFeatureReference() on all nodes 
     * automatically, including nodes that are created during the 
     * execution of postProcess().
     *
     * @throws IOException      If the operation can't be completed
     *
     * @see SENode#changeFeatureReference(Feature, Feature)
     **/
    public void changeFeatureReference(Feature from, Feature to) 
        throws IOException ;
}
