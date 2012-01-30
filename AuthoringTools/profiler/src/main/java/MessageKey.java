/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
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

/**
 *  This is a simple program that runs on a computer and collects the time
 *  profile information of the desired operations of an xlet running on a player.
 *  Note down the IP address and the port this program displays when it is
 *  launched, and provide them in a call to Profile.initProfiler() in an xlet
 *  of interest.
 */


import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.Profile;

import java.nio.charset.Charset;


/**
 * A holder for a message "string" encoded as UTF-8 in a byte array with an offset
 * and length into that array.  This class is useful as the key in a HashMap.
 **/
public class MessageKey {

    private byte[] data;
    private int start;
    private int length;
    private int hash = 0;
    private String stringRep = null;

    private final static Charset UTF_8 = Charset.forName("UTF-8");

    public MessageKey() {
    }
    
    public void set(byte[] data, int start, int length) {
        this.data = data;
        this.start = start;
        this.length = length;
        hash = length;
        int lim = start + length;
        for (int i = start; i < lim; i++) {
            hash = 31 * hash + (int) data[i];
        }
    }

    public MessageKey makeCopy() {
        MessageKey k = new MessageKey();
        k.data = new byte[length];
        for (int i = 0; i < length; i++) {
            k.data[i] = data[start + i];
        }
        k.start = 0;
        k.length = length;
        k.hash = hash;
        return k;
    }

    public int hashCode() {
        return hash;
    }

    public boolean equals(Object other) {
        if (!(other instanceof MessageKey)) {
            return false;
        }
        MessageKey mk = (MessageKey) other;
        if (this.length != mk.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (this.data[this.start + i] != mk.data[mk.start + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gives the string represented by this MessageKey
     **/
    public synchronized String toString() {
        if (stringRep == null) {
            stringRep = new String(data, start, length, UTF_8);
        }
        return stringRep;
    }
}
