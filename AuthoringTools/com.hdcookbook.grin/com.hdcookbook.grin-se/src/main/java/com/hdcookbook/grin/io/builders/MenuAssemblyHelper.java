
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

package com.hdcookbook.grin.io.builders;

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.features.Assembly;
import com.hdcookbook.grin.features.Group;

import com.hdcookbook.grin.features.SEAssembly;
import com.hdcookbook.grin.features.SEGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


/**
 * This class builds the menu_assembly feature.  A menu_assembly is
 * turned into a normal Assembly, with a bunch of anonymous groups.
 * Please refer to the BNF in the main GRIN documentation to see a
 * syntax for this.
 * <p>
 * Here's how it works:  A menu_assembly specifies a set of "default"
 * features.  Then, it specifies the "parts" of the assembly.  Each
 * part specifies the part name, and specifies which features of the
 * default set are to be swapped out for that part, and what they're
 * to be replaced with.
 **/
public class MenuAssemblyHelper {

    /**
     * This data holder class specifies a set of features within 
     * a MenuAssembly
     **/
    public static class Features {

        /**
         * A label identifyiing the features.  A part can swap out
         * features from the default set by specifying assembly features
         * with the same id.
         **/
        public String id;

        /**
         * The list of features under the given ID.  This defaults to
         * an empty List.
         **/
        public List<FeatureRef> features = new ArrayList<FeatureRef>();
    }

    public SEShow show;
    public List<Features> template;
    public List<String> partNames;
    public List<List<Features>> parts;
    public SEAssembly assembly;
    public int lineNumber;

    /**
     * Setup the assembly.
     *
     * @return the synthetic features that were created.  These need to be
     *         added to the show by the caller.
     **/
    public Iterable<Feature> setupAssembly() throws IOException {
        String[] nameList = partNames.toArray(new String[partNames.size()]);
        Feature[] featureList = new Feature[parts.size()];
        for (int i = 0; i < featureList.length; i++) {
            featureList[i] = buildPart(parts.get(i));
        }
        assembly.setParts(nameList, featureList);
        return Arrays.asList(featureList);
    }

    private Feature buildPart(List<Features> replacements) throws IOException {
        Map<String, List<FeatureRef>> idMap 
                = new HashMap<String, List<FeatureRef>>();
        for (Features f : replacements)  {
            if (idMap.get(f.id) != null) {
                throw new IOException("The id \"" + f.id + "\" occurs twice"
                                      + " (near line " + lineNumber + ")");
            }
            idMap.put(f.id, f.features);
        }
        List<Feature> elements = new ArrayList<Feature>();
        for (int i = 0; i < template.size(); i++) {
            List<FeatureRef> fRefs = idMap.get(template.get(i).id);
            if (fRefs == null) {
                fRefs = template.get(i).features;
            }
            for (int j = 0; j < fRefs.size(); j++)  {
                elements.add(fRefs.get(j).getFeature());
            }
        }
        Feature[] members = elements.toArray(new Feature[elements.size()]);
        SEGroup g = new SEGroup(show, null);
        g.setup(members);
        return g;
    }

}
