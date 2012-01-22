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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * BD-ROM Part 3-1 5.3.8 ExtensionData Data_Block item.
 */
@XmlType(propOrder={"metadataBlockHeader", "paddingX", "metadataBlockData", "data"})
public class ExtDataBlock {
        
        private BlockType type;
    private long size;  // need the size info when ExtDataBlock is an unknown type
        // Type: pip_metadata
        private MetadataBlockHeader[] metadataBlockHeader;
        private int paddingX;
        private MetadataBlockData[] metadataBlockData;
        // Type: unknown
        private byte[] data;
        
        public ExtDataBlock() {}
        public ExtDataBlock(BlockType type, long size) {
                setType(type);
                this.size = size;
        }
    
    public void readObject(DataInputStream din) throws IOException {
        if (type == BlockType.pip_metadata) {
                readPIPMetadata(din);
        } else {
                readUnknownExtData(din);
        }
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        if (type == BlockType.pip_metadata) {
                writePIPMetadata(dout);
        } else {
                writeUnknownExtData(dout);
        }
    }
    
    @XmlAttribute
    public BlockType getType() {
        return type;
    }
    
    public void setType(BlockType type) {
        this.type = type;
    }
    
    
    // Type: pip_metadata
    
    private void readPIPMetadata(DataInputStream din) throws IOException {
        // 32 bit length                                                        4 unsigned
        // 16 bit number_of_metadata_block_entries      2
        // MetadataBlockHeader()[n]                                     14
        // Padding X                                                            ?
        // MetadataBlockData()[n]                                       ?
        din.skipBytes(4);                                                       // length
        int n = din.readUnsignedShort();                        // number_of_metadata_block_entries
        MetadataBlockHeader[] headers = new MetadataBlockHeader[n];
        int blockDataStartAddress[] = new int[n];
        for (int i = 0; i < n; i++) {
                headers[i] = new MetadataBlockHeader();
                headers[i].readObject(din);
                blockDataStartAddress[i] = din.readInt(); // 20
        }
        setMetadataBlockHeader(headers);       
        long firstStartAddress = blockDataStartAddress[0];
        int paddingX = 0;
        if (firstStartAddress > 0) {
                paddingX = (int) ((firstStartAddress - (6 + n * 14)) / 2);
                for (int i = 0; i < paddingX; i++) {
                        din.readShort();
                }
        }
        setPaddingX(paddingX);
        MetadataBlockData[] blockdata = new MetadataBlockData[n];
        for (int i = 0; i < n; i++) {
                long size = 0L;
                if (i < n - 1) {
                        size = blockDataStartAddress[i + 1] - 
                                  blockDataStartAddress[i];
                }
                blockdata[i] = new MetadataBlockData(size);
                blockdata[i].readObject(din);
        }
        setMetadataBlockData(blockdata);
    }
    
    private void writePIPMetadata(DataOutputStream dout) throws IOException {
        // Write out PIPMetadata to a byte array first, to calculate data size.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream pipMetadataStream = new DataOutputStream(baos);
        
        MetadataBlockData[] blockdata = getMetadataBlockData();         
        int[] metadataBlockStartAddress = new int[blockdata.length];
        int startAddress = 6 + (14 * blockdata.length) + 2 * getPaddingX();
        int dataBlockSize = 0;
        for (int i = 0; i < blockdata.length; i++) {
            metadataBlockStartAddress[i] = dataBlockSize + startAddress;
                blockdata[i].writeObject(pipMetadataStream);
                pipMetadataStream.flush();
                dataBlockSize = baos.size();
        }
        
        pipMetadataStream.flush();
        pipMetadataStream.close();

        MetadataBlockHeader[] headers = getMetadataBlockHeader();       
        
        // Write actual data
        // 32 bit length                            4 unsigned 
        // 16 bit number_of_metadata_block_entries  2
        // MetadataBlockHeader()[n]                 14
        // Padding X                                2 * paddingX count
        // MetadataBlockData()[n]                   ?       
        
        // Data length.  Note that the 4 byte for this field's size itself is excluded!
        dout.writeInt(2 + (14 * headers.length) + 2 * getPaddingX() + (dataBlockSize));
        
        dout.writeShort(headers.length);
        for (int i = 0; i < headers.length; i++) {
           headers[i].writeObject(dout);
           dout.writeInt(metadataBlockStartAddress[i]);
        }
        for (int i = 0; i < getPaddingX(); i++) {
           dout.writeShort(0);
        }
        
        dout.write(baos.toByteArray());
    }
    
    public MetadataBlockHeader[] getMetadataBlockHeader() {
        return metadataBlockHeader;
    }
    
    public void setMetadataBlockHeader(MetadataBlockHeader[] metadataBlockHeader) {
        this.metadataBlockHeader = metadataBlockHeader;
    }
    
    public int getPaddingX() {
        return paddingX;
    }
    
    public void setPaddingX(int paddingX) {
        this.paddingX = paddingX;
    }
    
    public MetadataBlockData[] getMetadataBlockData() {
        return metadataBlockData;
    }
    
    public void setMetadataBlockData(MetadataBlockData[] metadataBlockData) {
        this.metadataBlockData = metadataBlockData;
    }
    
    
    // Type: unknown   
    private void readUnknownExtData(DataInputStream din) throws IOException {
                byte[] data = new byte[(int) size];
                din.readFully(data);
                setData(data);
    }
    
    private void writeUnknownExtData(DataOutputStream dout) throws IOException {
        dout.write(getData());
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    
    // ExtDataBlock Type
    
    public enum BlockType {
        pip_metadata,
        unknown;
    }
}
