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

import java.util.HashMap;

/**
 * BD-ROM Part 3-1 5.4.4.3.2 stream_coding_type
 * This class object is not marshalled/unmarshalled to the xml,
 * but used in the StreamAttribute class to identify it's type.
 */
public class StreamCodingType {
    
    private static HashMap<Integer, StreamCodingType> types = new HashMap();
    
    static StreamCodingType getType(Integer value) {
        return types.get(value);
    }

    static {
        register(new StreamCodingType(0x02, "MPEG-2 Video Stream for Primary / Secondary video"));
        register(new StreamCodingType(0x1b, "MPEG-4 AVC Video Stream for Primary / Secondary video"));
        register(new StreamCodingType(0xea, "SMPTE VC-1 Video Stream for Primary / Secondary video"));
        register(new StreamCodingType(0x80, "HDMV LPCM audio stream for Primary audio"));
        register(new StreamCodingType(0x81, "Dolby Digital (AC-3) audio stream for Primary audio"));
        register(new StreamCodingType(0x82, "DTS audio stream for Primary audio"));
        register(new StreamCodingType(0x83, "Dolby Lossless audio stream for Primary audio"));
        register(new StreamCodingType(0x84, "Dolby Digital Plus audio stream for Primary audio"));
        register(new StreamCodingType(0x85, "DTS-HD audio stream except XLL for Primary audio"));
        register(new StreamCodingType(0x86, "DTS-HD audio stream XLL for Primary audio"));
        register(new StreamCodingType(0xA1, "Dolby digital Plus audio stream for secondary audio"));
        register(new StreamCodingType(0xA2, "DTS-HD audio stream for secondary audio"));
        register(new StreamCodingType(0x90, "Presentation Graphics Stream"));
        register(new StreamCodingType(0x91, "Interactive Graphics Stream"));
        register(new StreamCodingType(0x92, "Text Subtitle stream"));
    }
    
    static void register(StreamCodingType type) {
        Integer value = type.getValue();
        if (types.containsKey(value)) {
            throw new IllegalArgumentException("StreamCodingType " + value + " already registered.");
        }
        types.put(value, type);
    }
    
    private int value;
    private String description;
    public StreamCodingType(int value, String description) {
        setValue(value);
        setDescription(description);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAudioStream() {
        // upper bit should be 8 or A
        int i = getValue() & 0xf0;
        return ( i == 0x80 || i == 0xa0 );
    }

    public boolean isVideoStream() {
        // ?? Can't find any logic here...
        return (getValue() == 0x02 ||
                getValue() == 0x1b ||
                getValue() == 0xea);
    }

    public boolean isPGStream() {
        // 0x90
        return (getValue() == 0x90);
    }

    public boolean isIGStream() {
        // 0x91
        return (getValue() == 0x91);
    }

    public boolean isTextSubTitleStream() {
        // 0x92
        return (getValue() == 0x92);
    }

}
