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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.xml.bind.annotation.XmlType;

/**
 * BD-ROM Part 3-1 5.3.8 PIP Metadata Block Data.
 */
@XmlType(propOrder={"pipMetadataTimeStamp", "pipHorizontalPosition", "pipVerticalPosition", "pipScale"})
public class PIPMetadataEntry {
    
        private long pipMetadataTimeStamp;
        private int pipHorizontalPosition;
        private int pipVerticalPosition;
        private PIPScaleType pipScale;
        
    public void readObject(DataInputStream din) throws IOException {
        // 32 bit pip_metadata_time_stamp                       4 unsigned
        //        pip_composition_metadata()
        // 12 bit pip_horizontal_position                       .
        // 12 bit pip_vertical_position                         .
        //  4 bit pip_scale                                                     .
        //  4 bit reserved                                                      .
        
        byte[] pipMetadataTimeStampBytes = new byte[4];
        byte[] pipCompositionMetadataBytes = new byte[4];
        
        din.readFully(pipMetadataTimeStampBytes);
        setPipMetadataTimeStamp(UnsignedIntHelper.convertToLong(pipMetadataTimeStampBytes));
        din.readFully(pipCompositionMetadataBytes);
        long pipCompositionMetadata = UnsignedIntHelper.convertToLong(pipCompositionMetadataBytes);
        int horizontalPosition = (int) ((pipCompositionMetadata & 0xfff00000) >> 20);
        int verticalPosition = (int) ((pipCompositionMetadata & 0x000fff00) >> 8);
        int pipScale = (int) ((pipCompositionMetadata & 0x000000f0) >> 4);
        setPipHorizontalPosition(horizontalPosition);
        setPipVerticalPosition(verticalPosition);
        Enum[] pipScaleTypes = PIPScaleType.values();
        for (int k = 0; k < pipScaleTypes.length; k++) {
                if (pipScaleTypes[k].ordinal() == pipScale) {
                        setPipScale((PIPScaleType) pipScaleTypes[k]);
                        break;
                }
        }
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        dout.write(UnsignedIntHelper.convertToBytes(getPipMetadataTimeStamp()));
        long pipCompositionMetadata = 0;
        pipCompositionMetadata |= getPipHorizontalPosition() << 20;
        pipCompositionMetadata |= getPipVerticalPosition() << 8;
        pipCompositionMetadata |= getPipScale().ordinal() << 4;
        dout.write(UnsignedIntHelper.convertToBytes(pipCompositionMetadata));
    }
    
    public long getPipMetadataTimeStamp() {
        return pipMetadataTimeStamp;
    }
    
    public void setPipMetadataTimeStamp(long pipMetadataTimeStamp) {
        this.pipMetadataTimeStamp = pipMetadataTimeStamp;
    }
    
    public int getPipHorizontalPosition() {
        return pipHorizontalPosition;
    }
    
    public void setPipHorizontalPosition(int pipHorizontalPosition) {
        this.pipHorizontalPosition = pipHorizontalPosition;
    }
    
    public int getPipVerticalPosition() {
        return pipVerticalPosition;
    }
    
    public void setPipVerticalPosition(int pipVerticalPosition) {
        this.pipVerticalPosition = pipVerticalPosition;
    }
    
    public PIPScaleType getPipScale() {
        return pipScale;
    }
    
    public void setPipScale(PIPScaleType pipScale) {
        this.pipScale = pipScale;
    }

    
    // PIP Timeline Type
    enum PIPScaleType {
        reserved,
        NO_SCALING,
        HALF_SCALING,
        QUARTER_SCALING,
        ONE_AND_HALF_SCALING,
        FULL_SCREEN_SCALING;

        public byte getEncoding() {
            return (byte) ordinal();
        }
    }
}
