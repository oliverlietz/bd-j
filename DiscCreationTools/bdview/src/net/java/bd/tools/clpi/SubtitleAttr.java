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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author ggeorg
 */
@XmlType(propOrder = {"const0x0001", "const0x0100", "subtitles"})
public class SubtitleAttr {

    private Short const0x0001;
    private Short const0x0100;
    private Subtitle[] subtitles;

    public SubtitleAttr() {
        // Nothing to do here!
    }

    @XmlJavaTypeAdapter(HexStringShortAdapter.class)
    public Short getConst0x0001() {
        return const0x0001;
    }

    public void setConst0x0001(Short const0x0001) {
        this.const0x0001 = const0x0001;
    }

    @XmlJavaTypeAdapter(HexStringShortAdapter.class)
    public Short getConst0x0100() {
        return const0x0100;
    }

    public void setConst0x0100(Short const0x0100) {
        this.const0x0100 = const0x0100;
    }

    @XmlElement(name = "Subtitle")
    public Subtitle[] getSubtitles() {
        return subtitles;
    }

    public void setSubtitles(Subtitle[] subTitles) {
        this.subtitles = subTitles;
    }

    public void readObject(DataInputStream din) throws IOException {
        // 32 bit length
        // 16 bit constvalue1
        // 32 bit reserved for future use (?)
        // 16 bit constvalue2
        // 8 bit number of dubtitles
        // 16 bit reserved for future use (?)

        int length = din.readInt();
        System.out.println("SubtitleAttr length=" + length);

        const0x0001 = din.readShort();
        System.out.println("SubtitleAttr const0x0001=" + const0x0001);

        din.skipBytes(4);

        const0x0100 = din.readShort();
        System.out.println("SubtitleAttr const0x0100=" + const0x0100);

        byte numOfSubtitles = din.readByte();
        System.out.println("SubtitleAttr number of subtitles=" + numOfSubtitles);

        din.skipBytes(1);

        subtitles = new Subtitle[numOfSubtitles];

        for (int i = 0; i < numOfSubtitles; i++) {
            subtitles[i] = new Subtitle();
            subtitles[i].readObject(din);
        }
    }

    public void writeObject(DataOutputStream out) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream substream = new DataOutputStream(baos);

        int numOfSubtitles = getSubtitles() == null ? 0 : getSubtitles().length;

        substream.writeShort(getConst0x0001());

        for (int i = 0; i < 4; i++) {
            substream.write(0);    // 8 bit zero
        }

        substream.writeShort(getConst0x0100());
        substream.write(numOfSubtitles);

        substream.write(0); // 8 bit zero

        for (int i = 0; i < numOfSubtitles; i++) {
            subtitles[i].writeObject(substream);
        }

        substream.flush();
        substream.close();

        byte[] data = baos.toByteArray();
        out.writeInt(data.length);
        out.write(data);
    }
}
