
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;

/**
 * In BD-J, interactive sounds are stored in a file named "sound.bdmv".
 * This class splits a given sound.bdmv file into multiple WAV format files.
 *
 * Specification references:
 *
 *    * sound.bdmv format - Section 5.6 of "BD-ROM System Description Part 3 Version 2.02".
 *    * WAV file format - http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
 *
 * @author A. Sundararajan
 */
public final class BDJSoundSplitter { 
    private static boolean debug;
    // BD-J sampling frequency for interactive sounds
    private static final int BD_J_SAMPLING_FREQUENCY = 48000; // Hz
    // BD-J bits per sample
    private static final int BD_J_SAMPLE_SIZE = 16; // bits

    // file magic for .bdmv files
    private static final byte[] SOUND_BDMV_TYPE_INDICATOR = "BCLK".getBytes();
    // sound.bdmv version string     
    private static final byte[] SOUND_BDMV_VERSION = "0200".getBytes();

    // Don't create me!
    private BDJSoundSplitter() {}

    public static void main(String[] args) {
        if (args.length == 0) {
            usage(1);
        }

        // By default, the output .wav files are created in current dir.
        // You can change it by -outputDir option.
        String outputDir = ".";
        // The file name part of sound.bdmv file is used as prefix for
        // .wav files. You can change the prefix by -prefix option.
        String prefix = null;
        String fileName = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                debug = true;
            } else if (args[i].equals("-prefix")) {
                if (i + 1 == args.length) {
                    usage(1);
                }
                prefix = args[++i];
                 
            } else if (args[i].equals("-outputDir")) {
                if (i + 1 == args.length) {
                    usage(1);
                }
                outputDir = args[++i];
            } else if (args[i].charAt(0) == '-') {
                usage(1);    
            } else {
                fileName = args[i];
            }
        }
 
        if (prefix == null) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex == -1) {
                errorExit("sound.bdmv file should have file extension", 2);
            }
            prefix = fileName.substring(0, dotIndex);
            int slash = fileName.lastIndexOf(File.separatorChar);
            if (slash != -1) {
                prefix = prefix.substring(slash);
            }
        }

        File inFile = new File(fileName);
        if (! inFile.exists()) {
            errorExit("File not found: " + fileName, 2);
        }
        try {
            // make output dir just in case...   
            new File(outputDir).mkdirs();
        
            FileInputStream fis = new FileInputStream(fileName);
            DataInputStream dis = new DataInputStream(
                new BufferedInputStream(fis));
            // check sound.bdmv header for format
            checkFormat(dis);

            // Refer to section 5.6.3 sound.bdmv - Syntax
            // SoundData_start_address (4 bytes)
            long soundDataStartAddr = readUnsignedInt(dis);

            // skip ExtensionData_start_address - 4 bytes
            dis.skip(4);

            // skip reserved 24 bytes
            dis.skip(24);

            final int sizeTillSoundIndex = 
                    4 + /* type indicator */
                    4 + /* version */
                    4 + /* SoundData_start_address */
                    4 + /* ExtensionData_start_address */
                    24;   /* reserved_for_future_use */

            // refer to section 5.6.4.1 SoundIndex() - Syntax
            long soundIndexLength = readUnsignedInt(dis);
            if (soundIndexLength != 0) {
                // skip 1 reserved byte
                dis.skip(1);

                final int numEntries = dis.readUnsignedByte();
                boolean[] channels = new boolean[numEntries];
                long[] sizes = new long[numEntries];

                // for each entry read sound attributes
                for (int soundId = 0; soundId < numEntries; soundId++) {

                    // channel configuration (4 bits): 1=mono, 3=stereo
                    // sampling rate (4 bits): must be 1=48 kHz
                    int data = dis.read();
                    channels[soundId] = ((data & 0x0F0) >> 4) == 1;

                    // skip bits-per-sample and assume the default
                    dis.skip(1);

                    // skip sound_data_start_address (4 bytes)
                    dis.skip(4);

                    // sound_data_length (4 bytes)
                    sizes[soundId] = readUnsignedInt(dis);
                }
               

                final int perEntrySize =
                        1 + /* channel_configuration, sampling freq */
                        1 + /* bits per sample + align */
                        4 + /* sound_data_start_address */
                        4;  /* sound_data_length */

                final int sizeofSoundIndex = 
                        4 +  /* length */
                        1 +  /* reserved */
                        1 +  /* number of entries */
                        /* variable size based on number of entries */
                        (numEntries*perEntrySize); 

                /* 
                 * We have to skip N1 padding_words (16 bits each) here.
                 * We compute pad by subtracing size of data read so far 
                 * from the value of SoundDataStartAddr.
                 */
                long N1bytes = soundDataStartAddr - (sizeTillSoundIndex + sizeofSoundIndex);
                if (N1bytes != 0) {
                    dis.skip(N1bytes);
                }

                for (int soundId = 0; soundId < numEntries; soundId++) {
                    File outFile = new File(outputDir, prefix + soundId + ".wav");
                    FileOutputStream fos = new FileOutputStream(outFile);
                    DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(fos));
                    LittleEndian le = new LittleEndian(dos);

                     // WAV format: http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
                    
                    // ChunkID contains the letters "RIFF" in ASCII form
                    dos.write("RIFF".getBytes());

                    
                    /*
                     * ChunkSize - 36 + SubChunk2Size
                     *
                     * This is the size of the rest of the chunk 
                     * following this number.  This is the size of the 
                     * entire file in bytes minus 8 bytes for the
                     * two fields not included in this count:
                     * ChunkID and ChunkSize.
                     */
                    int subChunk2Size = (int)sizes[soundId];
                    int chunkSize = 36 + subChunk2Size;
                    le.writeInt(chunkSize);

                    // Format - contains the letters "WAVE"
                    dos.write("WAVE".getBytes());

                    // start of "fmt " subchunk
                    // Subchunk1ID - contains the letters "fmt "
                    dos.write("fmt ".getBytes());

                    // Subchunk1Size - 16 for PCM.
                    le.writeInt(16);
                    //  AudioFormat - PCM = 1
                    le.writeShort(1);

                    // NumChannels Mono = 1, Stereo = 2, etc.
                    int numChannels = channels[soundId]? 1 : 2;
                    le.writeShort(numChannels);
                    // SampleRate 8000, 44100, etc. - in our case 48000
                    le.writeInt(BD_J_SAMPLING_FREQUENCY);
                    // ByteRate  == SampleRate * NumChannels * BitsPerSample/8
                    le.writeInt(BD_J_SAMPLING_FREQUENCY * numChannels * BD_J_SAMPLE_SIZE/8);  
                    // block align    
                    le.writeShort(numChannels * BD_J_SAMPLE_SIZE/8); 
                    // BitsPerSample    8 bits = 8, 16 bits = 16, etc.
                    le.writeShort(BD_J_SAMPLE_SIZE); 


                    // start of "data" subchunk

                    // Subchunk2ID - contains the letters "data"
                    dos.write("data".getBytes());
                    // Subchunk2Size == NumSamples * NumChannels * BitsPerSample/8   
                    le.writeInt(subChunk2Size); 
                    // The actual sound data -- make sure correct endianess is used!
                    for (int j = 0; j < subChunk2Size/2; j++) {
                       le.writeShort(dis.readShort());
                    }

                    dos.close();
                    fos.close();
                }
            }
        } catch (EOFException eof) {
            errorExit("Unexpected EOF encountered, file truncated?", 2);
        } catch (FileNotFoundException fnfe) {
            errorExit(fnfe, 2);
        } catch (Exception e) {
            errorExit(e, 2);
        }
    }

    private static long readUnsignedInt(DataInputStream dis) throws IOException {
        return 0x0FFFFFFFFL & dis.readInt();
    }

    private static void checkFormat(DataInputStream dis) throws IOException {
        
        // Refer to section 5.6.3 sound.bdmv - Syntax table
        
        // check type_indicator
        for (int i = 0; i < SOUND_BDMV_TYPE_INDICATOR.length; i++) {
            if (dis.read() != SOUND_BDMV_TYPE_INDICATOR[i]) {
                throw new RuntimeException("Type indicator 'BCLK' expected");
            }
        }     

        // check version string
        for (int i = 0; i < SOUND_BDMV_VERSION .length; i++) {
            if (dis.read() != SOUND_BDMV_VERSION [i]) {
                throw new RuntimeException("Wrong version of sound.bdmv - '0200' expected");
            }
        }
    }

    private static void usage(int code) {
        System.err.println("sound.bdmv splitter");
        System.err.print("Usage: ");
        System.err.println("java " + BDJSoundSplitter.class.getName() + " [options] <sound.bdmv file>");
        System.err.println("options include: ");
        System.err.println("    -debug");
        System.err.println("    -prefix [output file name prefix]");
        System.err.println("    -outputDir [output directory]");
        System.exit(code);
    }

    private static void errorExit(Exception exp, int code) {
        System.err.println(exp);
        if (debug) exp.printStackTrace();
        System.exit(code);
    }

    private static void errorExit(String msg, int code) {
        System.err.println(msg);
        System.exit(code);
    }
}
