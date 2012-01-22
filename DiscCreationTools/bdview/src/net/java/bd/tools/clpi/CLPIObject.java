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

import net.java.bd.tools.bdview.bdj.StringIOHelper;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author ggeorg
 */
@XmlRootElement
@XmlType(propOrder = {"typeIndicator", "versionNumber", "clipInfo", "clipAttr",
    "subtitleAttr", "addrMap"})
public class CLPIObject {

    public static final String TYPE = "HDMV";
    private String typeIndicator; // HDMV
    private String versionNumber; // 0200
    private ClipInfo clipInfo = new ClipInfo();
    private ClipAttr clipAttr = new ClipAttr();
    private SubtitleAttr subtitleAttr = new SubtitleAttr();
    private AddrMap addrMap = new AddrMap();

    public CLPIObject() {
        // Nothing to do here!
    }

    public String getTypeIndicator() {
        return typeIndicator;
    }

    public void setTypeIndicator(String typeIndicator) {
        this.typeIndicator = typeIndicator;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    @XmlElement(name = "ClipInfo")
    public ClipInfo getClipInfo() {
        return clipInfo;
    }

    public void setClipInfo(ClipInfo clipInfo) {
        this.clipInfo = clipInfo;
    }

    @XmlElement(name = "ClipAttr")
    public ClipAttr getClipAttr() {
        return clipAttr;
    }

    public void setClipAttr(ClipAttr clipAttr) {
        this.clipAttr = clipAttr;
    }

    @XmlElement(name = "SubtitleAttr")
    public SubtitleAttr getSubtitleAttr() {
        return subtitleAttr;
    }

    public void setSubtitleAttr(SubtitleAttr subtitleAttr) {
        this.subtitleAttr = subtitleAttr;
    }

    @XmlElement(name = "AddrMap")
    public AddrMap getAddrMap() {
        return addrMap;
    }

    public void setAddrMap(AddrMap addrMap) {
        this.addrMap = addrMap;
    }

    public void readObject(DataInputStream din) throws IOException {
        // 8*4 bit type_indicator
        // 8*4 bit version number (type_indicator2)
        // 32 bit sequence_info_start_address
        // 32 bit program_info_start_address
        // 32 bit cpi_start_address
        // 32 bit clip_mark_start_addr
        // 32 bit ext_data_start_addr
        // 96 bit reserved for future use

        typeIndicator = StringIOHelper.readISO646String(din, 4);
        System.out.println("typeIndicator=" + typeIndicator);
        versionNumber = StringIOHelper.readISO646String(din, 4);
        System.out.println("versionNumber=" + versionNumber);

        int sequenceInfoStartAddress = din.readInt();
        System.out.println("sequenceInfoStartAddress=" + sequenceInfoStartAddress);
        int programInfoStartAddress = din.readInt();
        System.out.println("programInfoStartAddress=" + programInfoStartAddress);
        int cpiStartAddress = din.readInt();
        System.out.println("cpiStartAddress=" + cpiStartAddress);
        int clipMarkStartAddr = din.readInt();
        System.out.println("clipMarkStartAddr=" + clipMarkStartAddr);

        int extDataStartAddr = din.readInt();
        System.out.println("extDataStartAddr=" + extDataStartAddr);

        din.skipBytes(12);

        // clipInfo
        clipInfo.readObject(din);

        // DVSequenceInfo
        clipAttr.readObject(din);

        // ClipMark
        subtitleAttr.readObject(din);

        // AddrMap
        addrMap.readObject(din);

        //----------------
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        try {
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(xml));
            new CLPIWriter().writeXml(this, dout);
            dout.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(xml.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream oStream = new DataOutputStream(out);
        writeObject(oStream);
        oStream.flush();
        oStream.close();

        System.out.println(DatatypeConverter.printHexBinary(out.toByteArray()));
    }

    public void writeObject(DataOutputStream out) throws IOException {

        // Write out ClipInfo, ClipAttr, SubtitleAttr and AddrMap to
        // a byte array first, to calculate data size.
        ByteArrayOutputStream baos0 = new ByteArrayOutputStream();
        DataOutputStream clipInfoStream = new DataOutputStream(baos0);
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        DataOutputStream clipAttrStream = new DataOutputStream(baos1);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DataOutputStream subtitleAttrStream = new DataOutputStream(baos2);
        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        DataOutputStream addrMapStream = new DataOutputStream(baos3);

        clipInfo.writeObject(clipInfoStream);
        clipInfoStream.flush();
        clipInfoStream.close();

        clipAttr.writeObject(clipAttrStream);
        clipAttrStream.flush();
        clipAttrStream.close();

        subtitleAttr.writeObject(subtitleAttrStream);
        subtitleAttrStream.flush();
        subtitleAttrStream.close();

        addrMap.writeObject(addrMapStream);
        addrMapStream.flush();
        addrMapStream.close();

        int dvfSequenceInfoStartAddress = 40 + baos0.size();
        int clipMarkStartAddress = dvfSequenceInfoStartAddress + baos1.size();
        int makersPrivateDataStartAddress = clipMarkStartAddress + baos2.size();
        int fileLength = makersPrivateDataStartAddress + baos3.size();

        // Now write out the entire dataset to the file.
        out.write(StringIOHelper.getISO646Bytes(getTypeIndicator()));
        out.write(StringIOHelper.getISO646Bytes(getVersionNumber()));
        out.writeInt(dvfSequenceInfoStartAddress);
        out.writeInt(clipMarkStartAddress);
        out.writeInt(makersPrivateDataStartAddress);
        out.writeInt(fileLength);

        for (int i = 0; i < 16; i++) {
            out.write(0);   // 8 bit zero
        }

        out.write(baos0.toByteArray()); // clipInfo
        out.write(baos1.toByteArray()); // clipAttr
        out.write(baos2.toByteArray()); // subtitleAttr
        out.write(baos3.toByteArray()); // addrMap

        for (int i = 0; i < 4; i++) {
            out.write(0);
        }
    }
}
