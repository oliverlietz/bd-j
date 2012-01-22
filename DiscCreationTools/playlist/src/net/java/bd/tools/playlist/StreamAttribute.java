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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * BD-ROM Part 3-1 5.3.4.5.2.2 stream_attribute() for the STN_Table
 */
public class StreamAttribute {
    
    /**
     * Note: this class contains all possible data field for different
     * types of StreamAttribute (see StreamCodingType class for various 
     * stream types).  Only the non-null fields get
     * written out to the xml file.
     */
    private Integer streamCodingType;
    private VideoFormat videoFormat;
    private FrameRate   frameRate;
    private AudioPresentationType audioPresentationType;
    private SamplingFrequency samplingFrequency;
    private String audioLanguageCode;
    private String pgLanguageCode;
    private String igLanguageCode;
    private Integer charactorCode;
    private String textSTLanguageCode;
      
    public void readObject(DataInputStream din) throws IOException {
        din.skipBytes(1); // length
        int t = din.readUnsignedByte();         // type
        setStreamCodingType(t);
        StreamCodingType type = StreamCodingType.getType(t);
        if (type == null) {
            throw new IOException("Error in getting StreamCodingType " + Integer.toHexString(t));
        }
        if (type.isVideoStream()) {
            int value = din.readUnsignedByte();
            int format = value >> 4;
            Enum[] videoFormats = VideoFormat.values();
            for (int i = 0; i < videoFormats.length; i++) {
                if (videoFormats[i].ordinal() == format) {
                    setVideoFormat((VideoFormat) videoFormats[i]);
                    break;
                }
            }    
            int rate = value & 0x0f;
            Enum[] frameRates = FrameRate.values();
            for (int i = 0; i < frameRates.length; i++) {
                if (frameRates[i].ordinal() == rate) {
                    setFrameRate((FrameRate) frameRates[i]);
                    break;
                }
            }              
            din.skipBytes(3);
        } else if (type.isAudioStream()) {
            int value = din.readUnsignedByte();
            int presentationType = value >> 4;
            Enum[] presentationTypes = AudioPresentationType.values();
            for (int i = 0; i < presentationTypes.length; i++) {
                if (presentationTypes[i].ordinal() == presentationType) {
                    setAudioPresentationType((AudioPresentationType) presentationTypes[i]);
                    break;
                }
            }    
            int freq = value & 0x0f;
            Enum[] frequencies = SamplingFrequency.values();
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i].ordinal() == freq) {
                    setSamplingFrequency((SamplingFrequency) frequencies[i]);
                    break;
                }
            }             
            setAudioLanguageCode(StringIOHelper.readISO646String(din, 3));           
        } else if (type.isPGStream()) {
            setPGLanguageCode(StringIOHelper.readISO646String(din, 3));   
            din.skipBytes(1);
        } else if (type.isIGStream()) {
            setIGLanguageCode(StringIOHelper.readISO646String(din, 3));   
            din.skipBytes(1);
        } else if (type.isTextSubTitleStream()) {
            setCharactorCode(din.readUnsignedByte());
            setTextSTLanguageCode(StringIOHelper.readISO646String(din, 3));   
        } else {
            throw new IOException("Error in reading StreamAttribute, type = " + type);
        }
    }
    
    public void writeObject(DataOutputStream dout) throws IOException {
        Integer t = getStreamCodingType();
        StreamCodingType type = StreamCodingType.getType(t);
        if (type == null) {
            throw new IOException("Error in getting StreamCodingType " + t);
        }        
        if (type.isVideoStream()) {
            dout.write(5); // length
            dout.writeByte(getStreamCodingType());
            int i = getVideoFormat().ordinal() << 4;
            i |= getFrameRate().ordinal();
            dout.writeByte(i);
            dout.write(new byte[3]);
        } else if (type.isAudioStream()) {
            dout.write(5); // length
            dout.writeByte(getStreamCodingType()); 
            int i = getAudioPresentationType().ordinal() << 4 ;
            i |= getSamplingFrequency().ordinal();
            dout.writeByte(i);
            dout.write(StringIOHelper.getISO646Bytes(getAudioLanguageCode()));
        } else if (type.isPGStream()) {
            dout.write(5); // length
            dout.writeByte(getStreamCodingType());
            dout.write(StringIOHelper.getISO646Bytes(getPGLanguageCode()));  
            dout.writeByte(0);
        } else if (type.isIGStream()) {
            dout.write(5); // length
            dout.writeByte(getStreamCodingType());
            dout.write(StringIOHelper.getISO646Bytes(getIGLanguageCode()));  
            dout.writeByte(0);            
        } else if (type.isTextSubTitleStream()) {
            dout.write(5); // length
            dout.writeByte(getStreamCodingType());  
            dout.writeByte(getCharactorCode());
            dout.write(StringIOHelper.getISO646Bytes(getTextSTLanguageCode()));
        } else {
            dout.write(1); // length
            dout.writeByte(getStreamCodingType());
        }
    }

    @XmlAttribute
    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)    
    public Integer getStreamCodingType() {
        return streamCodingType;
    }

    public void setStreamCodingType(Integer streamCodingType) {
        this.streamCodingType = streamCodingType;
    }

    public VideoFormat getVideoFormat() {
        return videoFormat;
    }

    public void setVideoFormat(VideoFormat videoFormat) {
        this.videoFormat = videoFormat;
    }

    public FrameRate getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(FrameRate frameRate) {
        this.frameRate = frameRate;
    }

    public AudioPresentationType getAudioPresentationType() {
        return audioPresentationType;
    }

    public void setAudioPresentationType(AudioPresentationType audioPresentationType) {
        this.audioPresentationType = audioPresentationType;
    }

    public SamplingFrequency getSamplingFrequency() {
        return samplingFrequency;
    }

    public void setSamplingFrequency(SamplingFrequency samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
    }

    public String getAudioLanguageCode() {
        return audioLanguageCode;
    }

    public void setAudioLanguageCode(String audioLanguageCode) {
        this.audioLanguageCode = audioLanguageCode;
    }

    public String getPGLanguageCode() {
        return pgLanguageCode;
    }

    public void setPGLanguageCode(String pgLanguageCode) {
        this.pgLanguageCode = pgLanguageCode;
    }

    public String getIGLanguageCode() {
        return igLanguageCode;
    }

    public void setIGLanguageCode(String igLanguageCode) {
        this.igLanguageCode = igLanguageCode;
    }

    @XmlJavaTypeAdapter(HexStringIntegerAdapter.class)    
    public Integer getCharactorCode() {
        return charactorCode;
    }

    public void setCharactorCode(Integer charactorCode) {
        this.charactorCode = charactorCode;
    }

    public String getTextSTLanguageCode() {
        return textSTLanguageCode;
    }

    public void setTextSTLanguageCode(String textSTLanguageCode) {
        this.textSTLanguageCode = textSTLanguageCode;
    }
    
    public enum VideoFormat {

        RESERVED,
        VIDEO_480i,
        VIDEO_576i,
        VIDEO_480p,
        VIDEO_1080i,
        VIDEO_720p,
        VIDEO_1080p,
        VIDEO_576p;

        public byte getEncoding() {
            return (byte) ordinal();
        }
    }     
    
    public enum FrameRate {
        RESERVED,
        Hz_24000_1001,
        Hz_24,
        Hz_25,
        Hz_30000_1001,
        RESERVED_5,
        Hz_50,
        Hz_60000_1001;
        
        public byte getEncoding() {
            return (byte) ordinal();
        }        
    }
    
    public enum AudioPresentationType {
        RESERVED_0,
        SINGLE_MONO_CHANNEL,
        RESERVED_2,
        STEREO,
        RESERVED_4,
        RESERVED_5,
        MULTI_CHANNEL,
        RESERVED_7,
        RESERVED_8,
        RESERVED_9,
        RESERVED_10,
        RESERVED_11,
        STEREO_AND_MULTI_CHANNEL,
        RESERVED_13,
        RESERVED_14,
        RESERVED_15;
        
        public byte getEncoding() {
            return (byte) ordinal();
        }        
    }  
    
    public enum SamplingFrequency {
        
        RESERVED_0,
        KHz_48,
        RESERVED_2,
        RESERVED_3,
        KHz_96,
        KHz_192,
        RESERVED_6,
        RESERVED_7,
        RESERVED_8,
        RESERVED_9,
        RESERVED_10,
        RESERVED_11,
        KHz_48_AND_192,
        RESERVED_13,
        KHz_48_AND_96,
        RESERVED_15;
        
        public byte getEncoding() {
            return (byte) ordinal();
        }         
    }
}
