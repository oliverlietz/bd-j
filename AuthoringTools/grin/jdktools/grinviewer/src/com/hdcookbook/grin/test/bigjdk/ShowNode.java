
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

package com.hdcookbook.grin.test.bigjdk;


import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.Segment;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.io.builders.BackgroundSpec;
import com.hdcookbook.grin.features.Assembly;
import com.hdcookbook.grin.features.Group;
import com.hdcookbook.grin.features.Modifier;
import com.hdcookbook.grin.features.InterpolatedModel;
import com.hdcookbook.grin.features.Translator;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.swing.tree.TreeNode;

/**
 * This is a Swing TreeNode that represents a node in the show graph.
 * It's used for the tree-browsing UI of GrinView.
 *
 * @see GrinView
 *
 * @author Bill Foote (http://jovial.com)
 */
public class ShowNode implements TreeNode {

    private Object contents;
    private ShowNode[] children;
    private boolean leaf;
    private TreeNode parent;
    private boolean expanded = false;
    private String label = null;        // Extra label, usually null

    /**
     * Create a show tree, that is, a top-level ShowNode for a show.
     **/
    public ShowNode(SEShow show, String showName) {
        BackgroundSpec[] backgrounds = show.getGrinviewBackgrounds();
        ShowNode backgroundsNode = null;
        if (backgrounds.length > 0) {
            ShowNode[] children = new ShowNode[backgrounds.length];
            for (int i = 0; i < children.length; i++) {
                children[i] = new ShowNode(backgrounds[i], null);
            }
            backgroundsNode = new ShowNode("background images", children);
        }
        Segment[] segments = show.getSegments();
        int j = 0;
        if (backgroundsNode != null) {
            j++;
        }
        ShowNode[] sa = new ShowNode[j + segments.length];
        sa[0] = backgroundsNode;
        for (int i = 0; i < segments.length; i++, j++) {
            sa[j] = new ShowNode(segments[i], null);
            sa[j].expand();
        }
        this.contents = showName;
        this.leaf = false;
        setChildren(sa);
    }

    private ShowNode(Object contents, ShowNode[] children) {
        this.contents = contents;
        this.leaf = children == null;
        if (this.leaf) {
            if (contents instanceof Segment
                || contents instanceof Assembly
                || contents instanceof Translator
                || contents instanceof InterpolatedModel
                || contents instanceof Group
                || contents instanceof Modifier) 
            {
                leaf = false;
                // expansion will create children
            } else {
                children = new ShowNode[0];
            }
        }
        if (children != null) {
            setChildren(children);
        }
    }

    private void setChildren(ShowNode[] children) {
        this.children = children;
        for (int i = 0; i < children.length; i++) {
            children[i].parent = this;
        }
    }

    Object getContents() {
        return contents;
    }

    public Enumeration children() {
        expand();
        return new Enumeration() {
            private int i = 0;
            public boolean hasMoreElements() {
                return i < children.length;
            }

            public Object nextElement() {
                if (i < children.length) {
                    return children[i];
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public boolean getAllowsChildren() {
        return !leaf;
    }

    public int getChildCount() {
        expand();
        return children.length;
    }
    
    public TreeNode getChildAt(int i) {
        expand();
        return children[i];
    }

    public int getIndex(TreeNode node) {
        expand();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == node) {
                return i;
            }
        }
        return -1;
    }

    public TreeNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return leaf;
    }

    void expand() {
        if (expanded) {
            return;
        }
        expanded = true;
        if (contents instanceof Segment) {
            Segment seg = (Segment) contents;
            leaf = false;
            ShowNode[] newChildren = new ShowNode[5];
            newChildren[0] = makeNode("active", seg.getActiveFeatures());
            newChildren[1] = makeNode("setup", seg.getSetupFeatures());
            newChildren[2] = makeNode("rc_handlers", seg.getRCHandlers());
            newChildren[3] = makeNode("on_entry", seg.getOnEntryCommands());
            newChildren[4] = makeNode("next", seg.getNextCommands());
            setChildren(newChildren);
        } else if (contents instanceof Assembly) {
            Assembly a = (Assembly) contents;
            String[] partNames = a.getPartNames();
            setChildren(makeChildren(a.getParts()));
            for (int i = 0; i < children.length; i++) {
                children[i].label = partNames[i];
            }
        } else if (contents instanceof Translator) {
            Translator t = (Translator) contents;
            ShowNode[] na = new ShowNode[2];
            na[0] = new ShowNode(t.getModel(), null);
            na[1] = new ShowNode(t.getPart(), null);
            setChildren(na);
            children[0].label = "Translation";
        } else if (contents instanceof InterpolatedModel) {
            InterpolatedModel t = (InterpolatedModel) contents;
            setChildren(makeChildren(t.getEndCommands()));
        } else if (contents instanceof Group) {
            Group g = (Group) contents;
            setChildren(makeChildren(g.getParts()));
        } else if (contents instanceof Modifier) {
            Modifier c = (Modifier) contents;
            Feature[] f = { c.getPart() };
            setChildren(makeChildren(f));
        } else if (children == null) {
            setChildren(new ShowNode[0]);
        }
    }

    private static ShowNode makeNode(String name, Object[] kids) {
        return new ShowNode(name, makeChildren(kids));
    }

    private static ShowNode[] makeChildren(Object[] kids) {
        ShowNode[] children = new ShowNode[kids.length];
        for (int i = 0; i < children.length; i++) {
            children[i] = new ShowNode(kids[i], null);
        }
        return children;
    }

    
    public String toString() {
        if (label == null) {
            return contents.toString();
        } else {
            return label + ": " + contents.toString();
        }
    }
}
