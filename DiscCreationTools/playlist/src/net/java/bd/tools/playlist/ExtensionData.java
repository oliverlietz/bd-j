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


package net.java.bd.tools.playlist;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.xml.bind.annotation.XmlType;

/*
 * BD-ROM 3-1 5.3.8 ExtensionData (Base syntax is from 5.2.4)
 */
@XmlType(propOrder={"extDataEntry", "paddingL1", "extDataBlock"})
public class ExtensionData {
    
        private ExtDataEntry[] extDataEntry;
        private int paddingL1;
        private ExtDataBlock[] extDataBlock;
    
    public void readObject(DataInputStream din) throws IOException {
        // 32 bit length
        // 32 bit dataBlockStartAddress
        // 24 bit word align
        // 8 bit number of data entries (N)
        // for i = 0 .. N
        // extDataEntry[i] 
             // 16 bit id1
             // 16 bit id2
             // 32 bit ext_data_start_address
             // 32 bit ext_data_length
        // for i = 0 ... L1
        // 32 bit padding
        // data_block() - ex. i...N pip_metadata()
        din.skipBytes(4);               // length
        int dataBlockStartAddress = din.readInt();
        din.skipBytes(3);
        int n = din.readUnsignedByte();
        ExtDataEntry[] entries = new ExtDataEntry[n];
        int[] extensionDataSize = new int[n];
        for (int i = 0; i < n; i++) {
                entries[i] = new ExtDataEntry();
                entries[i].readObject(din);
                din.skipBytes(4); // ext_data_start_address
                extensionDataSize[i] = din.readInt(); // ext_data_length
        }
        setExtDataEntry(entries);
        int paddingL1 = (dataBlockStartAddress - (12 + (n * 12))) / 4;
        setPaddingL1(paddingL1);
        ExtDataBlock[] data = new ExtDataBlock[n];
        for (int i = 0; i < n; i++) {
                ExtDataBlock.BlockType type;
                if (entries[i].getID1() == 0x0001 && entries[i].getID2() == 0x0001) {
                        type = ExtDataBlock.BlockType.pip_metadata;
                } else {
                        type = ExtDataBlock.BlockType.unknown;
                }
                data[i] = new ExtDataBlock(type, extensionDataSize[i]); 
                data[i].readObject(din);
        }
        setExtDataBlock(data);
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        
        int length;
        int dataBlockStartAddress;
        byte[] reserved = new byte[3];
        
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DataOutputStream extDataBlockStream = new DataOutputStream(baos2);
        
        // We need to write out the ExtensionDataBlock to figure out the ExtensionDataEntry's
        // ext_data_start_address and ext_data_length
        
        ExtDataBlock[] extDataBlock = getExtDataBlock();        
        int[] dataBlockSize = new int[extDataBlock.length]; 
        int previousSize = 0;
        int currentSize = 0;
        for (int i = 0; i < extDataBlock.length; i++) {
                extDataBlock[i].writeObject(extDataBlockStream);
                extDataBlockStream.flush();
            previousSize = currentSize;
                currentSize = baos2.size() - previousSize;
                dataBlockSize[i] = currentSize; 
        }
        extDataBlockStream.flush();
        extDataBlockStream.close();
        
        // Calculate offsets and lengths
        // The header is 12 bytes, each ExtDataEntry is 12 bytes, PaddingL1 is 4 bytes
        dataBlockStartAddress = 12 + (12 * getExtDataEntry().length) + 4 * getPaddingL1();
        length = dataBlockStartAddress + baos2.size() - 4;
    
        // Now write out the entire dataset to the file.
        dout.writeInt(length);
        dout.writeInt(dataBlockStartAddress);
        dout.write(reserved);
        
        ExtDataEntry[] extDataEntry = getExtDataEntry();
        dout.writeByte(extDataEntry.length);

        int startAddress = dataBlockStartAddress;
        for (int i = 0; i < extDataEntry.length; i++) {
            extDataEntry[i].writeObject(dout);  // ID1, ID2
            dout.writeInt(startAddress);        // ext_data_start_address
            dout.writeInt(dataBlockSize[i]);    // ext_data_length
            startAddress += dataBlockSize[i];
        }    
        
        for (int i = 0; i < getPaddingL1(); i++) {
                dout.writeShort(0);
                dout.writeShort(0);
        }
        
        dout.write(baos2.toByteArray());   // ExtDataBlock[]
    }
    
    public ExtDataEntry[] getExtDataEntry() {
        return extDataEntry;
    }
    
    public void setExtDataEntry(ExtDataEntry[] extDataEntry) {
        this.extDataEntry = extDataEntry;
    }
    
    public int getPaddingL1() {
        return paddingL1;
    }
    
    public void setPaddingL1(int paddingL1) {
        this.paddingL1 = paddingL1;
    }
    
    public ExtDataBlock[] getExtDataBlock() {
        return extDataBlock;
    }
    
    public void setExtDataBlock(ExtDataBlock[] extDataBlock) {
        this.extDataBlock = extDataBlock;
    }
}
