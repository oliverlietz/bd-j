/*
 * Copyright (c) 2010, Sun Microsystems, Inc.
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
package net.java.bd.tools.clpi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author ggeorg
 */
@XmlType(propOrder = {"const0x00010000", "const0x00000100", "const0x1001",
    "inTime", "outTime"})
public class ClipAttr {

    private Integer const0x00010000;
    private Integer const0x00000100;
    private Short const0x1001;
    private int inTime;
    private int outTime;

    public ClipAttr() {
        // Nothing to do here!
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)
    public Integer getConst0x00010000() {
        return const0x00010000;
    }

    public void setConst0x00010000(Integer const0x00010000) {
        this.const0x00010000 = const0x00010000;
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)
    public Integer getConst0x00000100() {
        return const0x00000100;
    }

    public void setConst0x00000100(Integer const0x00000100) {
        this.const0x00000100 = const0x00000100;
    }

    @XmlJavaTypeAdapter(HexStringShortAdapter.class)
    public Short getConst0x1001() {
        return const0x1001;
    }

    public void setConst0x1001(Short const0x1001) {
        this.const0x1001 = const0x1001;
    }

    public int getInTime() {
        return inTime;
    }

    public void setInTime(int inTime) {
        this.inTime = inTime;
    }

    public int getOutTime() {
        return outTime;
    }

    public void setOutTime(int outTime) {
        this.outTime = outTime;
    }

    public void readObject(DataInputStream din) throws IOException {
        // 32 bit length
        // 32 bit constvalue1
        // 32 bit constvalue2
        // 16 bit constvalue3
        //
        // 32 bit reserved_for_word_align
        //
        // 32 bit in time in 45KHz
        // 32 bit end time in 45KHz

        int length = din.readInt();
        System.out.println("ClipAttr length=" + length);

        const0x00010000 = din.readInt();
        System.out.println("ClipAttr const0x00010000=" + const0x00010000);

        const0x00000100 = din.readInt();
        System.out.println("ClipAttr const0x00000100=" + const0x00000100);

        const0x1001 = din.readShort();
        System.out.println("ClipAttr const0x1001=" + const0x1001);

        din.skipBytes(4);

        inTime = din.readInt();
        System.out.println("ClipAttr inTime=" + inTime + " (" + inTime / 45000 + "seconds)");

        outTime = din.readInt();
        System.out.println("ClipAttr outTime=" + outTime + " (" + outTime / 45000 + "seconds)");
    }

    public void writeObject(DataOutputStream out) throws IOException {
        out.writeInt(22);  // length (0x00000016)
        out.writeInt(getConst0x00010000());
        out.writeInt(getConst0x00000100());
        out.writeShort(getConst0x1001());

        for (int i = 0; i < 4; i++) {
            out.write(0);    // 8 bit zero
        }

        out.writeInt(getInTime());
        out.writeInt(getOutTime());

        System.out.println("ClipAttr: " + 22 + " ?= " + (out.size() - 4));
    }
}
