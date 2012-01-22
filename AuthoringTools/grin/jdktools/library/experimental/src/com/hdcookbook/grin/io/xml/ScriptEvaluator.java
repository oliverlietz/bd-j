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

import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class evaluates scripts in a Show XML file during "build". i.e.,
 * during XML to binary conversion performed on desktop JDK. The script
 * is specified inside &lt;script&gt; tags with "runat" attribute specified with 
 * the value "build". All such scripts are evaluated using JSR-223 compliant
 * script engines (if available). If a script engine is not available, runtime
 * exception is thrown. The build time scripts can access underlying XML document
 * using the JAXB object of type "ShowType". This object is exposed as a global
 * script variable by the name "show".
 * 
 * @author A. Sundararajan
 */
public class ScriptEvaluator {

    public ScriptEvaluator() {
        this.engineManager = new ScriptEngineManager();
        this.cachedEngines = new HashMap<String, ScriptEngine>();
    }

    /**
     * Evaulates build-time script embedded in a show document.
     * The script can make changes to show graph by manipulating
     * the global variable named "show". The script is evaluated for
     * side-effects on the show object. The return value of the script
     * is ignored. The following is a simple embedded script example to
     * add one more "draw target" to the show.
     * 
     * <pre>
     * <code>
     * &lt;script runat="build" type="text/javascript"&gt;
     *     // bean-style properties are supported in JS
     *     show.drawTargets.add("my_target");
     *     println("added a new draw target");
     * &lt;/script&gt;
     * </code>
     * </pre>
     * 
     * For a slightly more detailed sample, please refer to tumblingduke-script.xml
     * in "HelloGrinWorld" grin sample.
     * 
     * @param jaxbShow The "show" object exposed to script
     * @param st The build-time script evaluated
     */
    public void eval(ShowType jaxbShow, ScriptType st) {
        if ("build".equals(st.getRunat())) {
            String mimeType = st.getType();
            // get the correct script engine.
            ScriptEngine engine = lookupEngine(mimeType);
            try {
                // expose ShowType object to script as global var
                engine.put("show", jaxbShow);
                // evaluate the script code and ignore the result
                engine.eval(st.getValue());
            } catch (ScriptException exp) {
                throw new RuntimeException("script error: " + exp);
            }
        }
    }

    /* Internals only below this point */
    private ScriptEngineManager engineManager;
    private Map<String, ScriptEngine> cachedEngines;

    private ScriptEngine lookupEngine(String mimeType) {
        /*
         * We cache the engine by mime type so that if the same script language
         * is used in mulitple &lt;script&gt; tags, we will re-use same engine object.
         * This way global script state is preserved between script blocks.
         */
        if (cachedEngines.containsKey(mimeType)) {
            return cachedEngines.get(mimeType);
        }
        ScriptEngine engine = engineManager.getEngineByMimeType(mimeType);
        if (engine == null) {
            throw new RuntimeException("no script engine found for type: '" + mimeType + "'");
        }
        cachedEngines.put(mimeType, engine);
        return engine;
    }
}
