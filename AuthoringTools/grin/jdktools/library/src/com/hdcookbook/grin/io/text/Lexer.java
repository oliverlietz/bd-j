
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

import com.hdcookbook.grin.util.AssetFinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import com.hdcookbook.grin.features.Translator; // For the constant OFFSCREEN
import com.hdcookbook.grin.util.Debug;



/**
 * Read tokens from a file.  This is used by the various parsers.
 * <p>
 * It reads various kinds of tokens on demand, and throws an
 * IOException with a plausible error message on a lexing error.
 * It makes no attempt to recover from errors, so parsers will
 * need to stop on the first error.  Methods: <ul>
 *    getInt()
 *    getString()
 * </ul>
 * <p>
 *
 *   @author     Bill Foote (http://jovial.com)
 */
public class Lexer {

    private Reader input;
    private int putbackChar = -1;       // see nextChar, putbackChar
    private int lineNum = 1;
    private StringBuffer strBuf = new StringBuffer(512);
    private String fileName;
    private ShowParser parser;
    private Lexer child = null; // For processing $include

    public Lexer (Reader input, String fileName, ShowParser parser) {
        this.input = input;
        this.fileName = fileName;
        this.parser = parser;
    }

    /**
     * Get the main ShowParser we're lexing for.  This is useful for
     * extension parsers that want to call back to the main parser, e.g.
     * to parse more complex things, like a color or a forward reference
     * to another feature.
     **/
    public ShowParser getParser() {
        return parser;
    }

    /**
     * @return the next character, or -1 on EOF
     */
    private int nextChar() throws IOException {
        int r;
        if (putbackChar != -1) {
            r = putbackChar;
            putbackChar = -1;
        } else {
            r = input.read();
            if (r == ((int) '\n')) {
                lineNum++;
            }
        }
        return r;
    }

    /**
     * Putback a character.  Only one character of putback allowed.
     * It's OK to put EOF (-1) back.
     */
    private void putback(int ch) {
        if (putbackChar != -1) {
            throw new RuntimeException("Lexer: Attempt to putback two characters");
        }
        putbackChar = ch;
    }

    /**
     * Report a nice error message with the current line number.
     **/
    public void reportError(String msg) throws IOException {
        throw new IOException(msg + " on line " + lineNum + " of " + fileName);
    }

    /**
     * Give a warning with the current line number
     **/
    public void reportWarning(String msg) {
        System.err.println(msg + " on line " + lineNum);
    }

    /**
     * Get the current line number within the main show file.  If a file
     * is currently being included, this will be the line number of the
     * include directive.
     **/
    public int getLineNumber() {
        return lineNum;
    }

    /**
     * Get the name of the main show file being read.  If a file is currently
     * being included, this won't be that file name.
     **/
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the current line number within the file getRealFileName(), which 
     * might be an included file.
     **/
    int getRealLineNumber() {
        if (child == null) {
            return lineNum;
        } else {
            return child.getRealLineNumber();
        }
    }


    String getRealFileName() {
        if (child == null) {
            return fileName;
        } else {
            return child.getRealFileName();
        }
    }

    /**
     * Set the current line number.  This is done by ForwardReference
     * so that error messages come out with the right line number, rather
     * than the last line in the show file.
     **/
    void setLineNumberAndName(int num, String name) {
        this.lineNum = num;
        this.fileName = name;
    }

    //
    // Skips whitespace.  Comments are considered to be
    // whitespace.
    private void skipWhitespace() throws IOException {
        for (;;) {
            int ch = nextChar();
            if (ch == -1 || !Character.isWhitespace((char) ch)) {
                if (ch == '#') {
                    skipPastEOL();
                } else {
                    putback(ch);
                    return;
                }
            }
        }
    }

    private void skipPastEOL() throws IOException {
        for (;;) {
            int ch = nextChar();
            if (ch == -1 || ch == '\n') {
                return;
            }
        }
    }

    private String strBufAsString() {
        int len = strBuf.length();
        char[] buf = new char[len];
        strBuf.getChars(0, len, buf, 0);
        return new String(buf);
    }

    /**
     * @return an int.  It's an error to see EOF.
     */
    public int getInt() throws IOException {
        return convertToInt(getString());
    }

    /**
     * @return a double.  It's an error to see EOF.
     */
    public double getDouble() throws IOException {
        return convertToDouble(getString());
    }

    /**
     * @return an int.  Integer.MAX_VALUE for "infinite".
     */
    public int getIntOrInfinite() throws IOException {
        String tok = getString();
        if ("infinite".equals(tok)) {
            return Integer.MAX_VALUE;
        } else {
            return convertToInt(tok);
        }
    }
    
    /**
     * @return an int.  Translator.OFFSCREEN for "offscreen".
     */
    public int getIntOrOffscreen() throws IOException {
        String tok = getString();
        if ("offscreen".equals(tok)) {
            return Translator.OFFSCREEN;
        } else {
            return convertToInt(tok);
        }
    }

    public int convertToInt(String tok) throws IOException {
        if (tok == null) {
            reportError("int expected, EOF seen");
        }
        try {
            return Integer.decode(tok).intValue();
        } catch (NumberFormatException ex) {
            reportError(ex.toString());
            return -1;  // not reached
        }
    }

    public double convertToDouble(String tok) throws IOException {
        if (tok == null) {
            reportError("double expected, EOF seen");
        }
        try {
            return Double.parseDouble(tok);
        } catch (NumberFormatException ex) {
            reportError(ex.toString());
            return -1;  // not reached
        }
    }

    /**
     * @return a boolean.  It's an error to see EOF.
     */
    public boolean getBoolean() throws IOException {
        String tok = getString();
        if ("true".equals(tok)) {
            return true;
        } else if ("false".equals(tok)) {
            return false;
        } else {
            reportError("boolean expected, \"" + tok + "\" seen");
            return false;       // not reached
        }
    }

    /**
     * @return next word, or null on EOF
     *
     * @throws IOException on any unexpected characters
     */
    public String getString() throws IOException {
        // All reading from the input stream goes through this method or
        // getStringExact()
        if (child != null) {
            String result = child.getString();
            if (result != null) {
                return result;
            }
            closeInclude();
        }
        skipWhitespace();
        strBuf.setLength(0);
        int ch = nextChar();
        if (ch == -1) {
            return null;
        }
        if (ch == '"') {
            return getQuotedString();
        }
        String result;
        for (;;) {
            if (ch != -1 && !Character.isWhitespace((char) ch)) {
                strBuf.append((char) ch);
            } else if (strBuf.length() == 0) {
                reportError("Word expected");
                return null;
            } else {
                putback(ch);
                result = strBufAsString();
                break;
            }
            ch = nextChar();
        }
        if (checkInclude(result)) {
            return getString();
        }
        if (Debug.LEVEL >= 1 && result.length() > 1 && result.endsWith(";")) {
            Debug.println();
            Debug.println("==> Warning:  token \"" + result 
                          + "\" ends with ';' on line " + lineNum);
        }
        return result;
    }

    //
    // Check if this token is a $include directive.  If it is, then
    // a child lexer will be created, and true will be returned.
    //
    private boolean checkInclude(String tok) throws IOException {
        if (!("$include".equals(tok))) {
            return false;
        }
        String fileName = getString();
        //
        // We expect a ; here, but we hold of parsing it until we close the
        // include file.  That way, the line number in GrinView points to the
        // include directive, rather than the line after it.
        //
        URL source = AssetFinder.getURL(fileName);
        if (source == null) {
            reportError("Can't find included show file " + fileName);
        }
        BufferedReader rdr = new BufferedReader(
                new InputStreamReader(source.openStream(), "UTF-8"));
        child = new Lexer(rdr, fileName, parser);
        return true;
    }

    //
    // Close the include file, and parse the final semicolon
    //
    private void closeInclude() throws IOException {
        child.input.close();
        child = null;
        parseExpected(";");
    }
    
    /**
     * Gets the next string that's the exact contents of the next bit of
     * the input stream.  This will either be a token (like you'd get from
     * getString()), or it's whitespace.  Quoted strings aren't recognized.
     * <p>
     * This method was added to the lexer in order to read the java code
     * between a "[[" and a "]]".
     * 
     * @return next word or next section of whitespace, or null on eof
     */
    public String getStringExact() throws IOException {
        // All reading from the input stream goes through this method or
        // getStringExact()
        if (child != null) {
            String result = child.getStringExact();
            if (result != null) {
                return result;
            }
            closeInclude();
        }
        strBuf.setLength(0);
        int ch = nextChar();
        if (ch == -1) {
            return null;
        }
        strBuf.append((char) ch);
        if (Character.isWhitespace((char) ch)) {
            for (;;) {
                ch = nextChar();
                if (ch == -1 || !Character.isWhitespace((char) ch)) {
                    putback(ch);
                    return strBufAsString();
                }
                strBuf.append((char) ch);
            }
        } else {    // not whitespace
            for (;;) {
                ch = nextChar();
                if (ch == -1 || Character.isWhitespace((char) ch)) {
                    putback(ch);
                    String result = strBufAsString();
                    if (checkInclude(result)) {
                        return getStringExact();
                    } 
                    return result;
                }
                strBuf.append((char) ch);
            }
        }
    }
    
    /**
     * Parses a token that we expect to see.  A token is read, and
     * if it's not the expected token, an IOException is generated.
     * This can be useful for things like parsing the ";" at the
     * end of various constructs.
     **/
    public void parseExpected(String expected) throws IOException {
        String tok = getString();
        expectString(expected, tok);
    }

    /**
     * Expect to see a certain string.  The token passed in is checked, and
     * if it's not the expected token, an IOException is generated.
     * This can be useful for things like parsing the ";" at the
     * end of various constructs.
     **/
    public void expectString(String expected, String tok) throws IOException {
        if (!(expected.equals(tok))) {
           reportError("\"" + expected + "\" expected, \"" + tok 
                             + "\" seen");
        }
    }
    //
    //  Called from getString
    //
    private String getQuotedString() throws IOException {
        int startLine = lineNum;
        for (;;) {
            int ch = nextChar();
            if (ch == '"') {
                return strBufAsString();
            }
            if (ch == '\\') {
                ch = nextChar();
            }
            if (ch == -1) {
                reportError("Matching close-quote never seen for string " 
                            + "starting at line " + startLine + "...  ");
            }
            strBuf.append((char) ch);
        }
    }
}
