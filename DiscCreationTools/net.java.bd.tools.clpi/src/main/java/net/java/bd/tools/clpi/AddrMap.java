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
@XmlType(propOrder = {"const0x00010001", "const0x10110004", "unknownvalue1",
    "unknownvalue2", "const0x0000000e", "bytes"})
public class AddrMap {

    private Integer const0x00010001;
    private Integer const0x10110004;
    private Short unknownvalue1;
    private Short unknownvalue2;
    private Integer const0x0000000e;
    private byte[] bytes;

    public AddrMap() {
        // Nothing to do here!
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)
    public Integer getConst0x00010001() {
        return const0x00010001;
    }

    public void setConst0x00010001(Integer const0x00010001) {
        this.const0x00010001 = const0x00010001;
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)
    public Integer getConst0x10110004() {
        return const0x10110004;
    }

    public void setConst0x10110004(Integer const0x10110004) {
        this.const0x10110004 = const0x10110004;
    }

    @XmlJavaTypeAdapter(HexStringShortAdapter.class)
    public Short getUnknownvalue1() {
        return unknownvalue1;
    }

    public void setUnknownvalue1(Short unknownvalue1) {
        this.unknownvalue1 = unknownvalue1;
    }

    @XmlJavaTypeAdapter(HexStringShortAdapter.class)
    public Short getUnknownvalue2() {
        return unknownvalue2;
    }

    public void setUnknownvalue2(Short unknownvalue2) {
        this.unknownvalue2 = unknownvalue2;
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)
    public Integer getConst0x0000000e() {
        return const0x0000000e;
    }

    public void setConst0x0000000e(Integer const0x0000000e) {
        this.const0x0000000e = const0x0000000e;
    }

    @XmlJavaTypeAdapter(HexStringBinaryAdapter.class)
    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public void readObject(DataInputStream din) throws IOException {
        // 32 bit length
        // 32 bit constvalue1 (0x00010001)
        // 32 bit constvalue2 (0x10110004)
        // 16 bit unknownvalue1
        // 16 bit unknownvalue2
        // 32 bit constvalue3 (0x0000000e)
        // mapping data ...

        int length = din.readInt();
        System.out.println("AddrMap length=" + length);

        const0x00010001 = din.readInt();
        System.out.println("AddrMap const0x00010001=" + const0x00010001);

        const0x10110004 = din.readInt();
        System.out.println("AddrMap const0x10110004=" + const0x10110004);

        unknownvalue1 = din.readShort();
        System.out.println("AddrMap unknownvalue1=" + unknownvalue1);

        unknownvalue2 = din.readShort();
        System.out.println("AddrMap unknownvalue2=" + unknownvalue2);

        const0x0000000e = din.readInt();
        System.out.println("AddrMap const0x0000000e=" + const0x0000000e);

        bytes = new byte[length - 16];
        din.read(bytes);
    }

    public void writeObject(DataOutputStream out) throws IOException {
        out.writeInt(bytes.length + 16);
        out.writeInt(this.getConst0x00010001());
        out.writeInt(this.getConst0x10110004());
        out.writeShort(this.getUnknownvalue1());
        out.writeShort(this.getUnknownvalue2());
        out.writeInt(this.getConst0x0000000e());
        out.write(bytes);
    }
}
