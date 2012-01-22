
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

package net.java.bd.tools;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Simple utility class to write data in little endian form.
 *
 * @author A. Sundararajan
 */
public class LittleEndian implements DataOutput {
    private DataOutput out;

    public LittleEndian(DataOutput out) {
        this.out = out;
    }

    public void writeDouble(double d) throws IOException {
        // FIXME: Do I need this?
        writeLong(Double.doubleToLongBits(d));
    }
    

    public void writeFloat(float f) throws IOException {
        // FIXME: Do I need this?
        writeInt(Float.floatToIntBits(f));
    }
    
    public void write(int i) throws IOException {
        out.write(i);
    }
    
    public void writeByte(int b) throws IOException {
        out.writeByte(b);
    }
    

    public void writeChar(int ch) throws IOException {
        out.writeChar(ch);
    }
    

    public void writeInt(int i) throws IOException {
        out.writeByte(i & 0xff);
        out.writeByte((i >> 8) & 0xff);
        out.writeByte((i >> 16) & 0xff);
        out.writeByte((i >> 24) & 0xff);
    }
    
    public void writeShort(int i) throws IOException {
        out.writeByte(i & 0xff);
        out.writeByte((i >> 8) & 0xff);
    }
    
    public void writeLong(long l) throws IOException {
        out.writeByte((int) l & 0xff);
        out.writeByte((int) (l >> 8) & 0xff);
        out.writeByte((int) (l >> 16) & 0xff);
        out.writeByte((int) (l >> 24) & 0xff);
        out.writeByte((int) (l >> 32) & 0xff);
        out.writeByte((int) (l >> 40) & 0xff);
        out.writeByte((int) (l >> 48) & 0xff);
        out.writeByte((int) (l >> 56) & 0xff);
    }
    
    public void writeBoolean(boolean b) throws IOException {
        out.writeBoolean(b);
    }
    
    public void write(byte[] buf) throws IOException {
        out.write(buf);
    }
    
    public void write(byte[] buf, int start, int len) throws IOException {
        out.write(buf, start, len);
    }
    
    public void writeBytes(String str) throws IOException {
        out.writeBytes(str);
    }
    
    public void writeChars(String str) throws IOException {
        out.writeChars(str);
    }
    
    public void writeUTF(String str) throws IOException {
        out.writeUTF(str);
    }
}