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

import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.features.Modifier;
import java.io.IOException;

/**
 * The ExtensionParser interface defines methods that handle parsing of the
 * custom (user-defined) GRIN features, modifiers and commands  
 * from text-based GRIN script. These who are defining the custom GRIN 
 * subclasses  and reading them from a text GRIN file should implement 
 * these methods accordingly.
 *
 * @see ExtensionParserList
 */

public interface ExtensionParser {
    
    /**
     * Get a feature of the given type.  
     * The syntax of an extension feature is fixed at
     * <pre>
     *     "feature" "modifier" namespace:type_name name feature_name arguments ";"
     * </pre>
     * 
     * This method gets a Lexer instance that is pointed to the beginning of 
     * the arguments.
     *
     * The implementation of this method is expected to return an instance of a feature 
     * which loads on a standard JDK.  It can be different from the feature class
     * that is going to be used with the xlet in the BD-J environment.     
     *
     * @param show      The show being parsed
     * @param typeName  The name of the feature's type.  This will always
     *                  contain a ":".
     * @param name      The name of this instance of feature
     *                  a list of commands if needed.
     * @param lexer     The lexer to parse arguments for this feature.
     *                  The implementation of this method should parse up to ";"
     *                  which indicates the end of the feature declaration.
     *
     * @throws      IOException if there's an error.
     *
     * @return      A feature if one of the given type is known, null otherwise
     */
    public Feature getFeature(Show show, String typeName, 
                              String name, Lexer lexer)
                       throws IOException;    

    /**
     * Get a modifier feature of the given type.  The type name will have a
     * colon in it.  The sub-feature will automatically be set up for
     * you.
     * <p>
     * The syntax of an extension feature is fixed at
     * <pre>
     *     "feature" "modifier" namespace:type_name name feature_name arguments ";"
     * </pre>
     * where feature_name is given iff the feature is a Modifier.
     * 
     * This method gets a Lexer instance that is pointed to the beginning of 
     * the arguments.
     *
     * The implementation of this method is expected to return an instance of a modifier 
     * which loads on a standard JDK.  It can be different from the modifier class
     * that is going to be used with the xlet in the BD-J environment.     
     *
     * @param show      The show being parsed
     * @param typeName  The name of the feature's type.  This will always
     *                  contain a ":".
     * @param name      The name of this instance of feature
     *                  a list of commands if needed.
     * @param lexer     The lexer to parse arguments for this feature.
     *                  The implementation of this method should parse up to ";"
     *                  which indicates the end of the feature declaration.
     *
     * @throws      IOException if there's an error.
     *
     * @return      A feature if one of the given type is known, null otherwise
     */
    public Modifier getModifier(Show show, String typeName, 
                                String name, Lexer lexer)
                       throws IOException;

     /**
     * Get a modifier command of the given type. 

     * The implementation of this method is expected to return an instance of a command 
     * which loads on a standard JDK.  It can be different from the command class
     * that is going to be used with the xlet in the BD-J environment.  
     *
     * @param show      The show being parsed
     * @param typeName  The name of the commands's type.  This will always
     *                  contain a ":".
     * @param lexer     The lexer to parse arguments for this feature.
     *                  The implementation of this method should parse up to ";"
     *                  which indicates the end of the feature declaration.
     *
     * @throws      IOException if there's an error.
     *
     * @return      A command if one of the given type is known, null otherwise
     */
    public Command getCommand(Show show, String typeName, Lexer lexer)
                       throws IOException;
}
