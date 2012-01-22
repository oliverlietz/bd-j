
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * In BD-J, interactive sound(s) is/are stored in a file named "sound.bdmv".
 * This class can be used to convert various platform file formats like .wav files
 * into a sound.bdmv file. Any input format supported by javax.sound.sampled API
 * can be used.
 *
 * Please refer to section 5.6 of BD-ROM System Description Part 3 Version 2.02.
 *
 * @author A. Sundararajan
 */
public final class BDJSoundGenerator { 
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
    private BDJSoundGenerator() {}

    public static void main(String[] args) {
        if (args.length < 2) {
            usage(1);
        }
       
        if ("-debug".equals(args[0])) {
            debug = true;
            if (args.length < 3) usage(1);
        }

        int numInputs = (debug)? (args.length - 2) : (args.length - 1);
        File[] files = new File[numInputs];
        for (int i = 0; i < files.length; i++) {
            String arg = debug? args[i+1] : args[i];
            files[i] = new File(arg);
            if (! files[i].exists()) {
                errorExit("File not found: " + files[i], 1);
            }

            if (files[i].isDirectory()) {
                errorExit(files[i] + " is a directory", 1);
            }
        }


        // check format convertibility and number of channels
        for (int i = 0; i < files.length; i++) {
            try {
                AudioFormat format = AudioSystem.getAudioFileFormat(files[i]).getFormat();
                if (! AudioSystem.isConversionSupported(
                    AudioFormat.Encoding.PCM_SIGNED, format)) {
                    errorExit("format conversion not supported for : " + files[i], 2);
                }

                if (format.getChannels() > 2) {
                    errorExit("only mono and stereo are supported in BD-J", 2);
                }
            } catch (UnsupportedAudioFileException uafe) {
                errorExit(uafe, 2);
            } catch (IOException ioe) {
                errorExit(ioe, 2);
            }
        }

        File outputFile = new File(args[args.length - 1]);    
        try {          
            // channel count in each audio input
            int[] channels = new int[numInputs];
            // frame length for each audio input   
            int[] frameLengths = new int[numInputs];
            /*
             * See section 5.6.2 of BDROM Part 3_v2.02D specification.
             * sound.bdmv format of data samples:
             *
             *    Sampling frequency 48 kHz
             *    Bits per sample 16
             *    Both mono, stero accepted
             */
             AudioFormat bdjFormat = new AudioFormat(
                 BD_J_SAMPLING_FREQUENCY ,
                 BD_J_SAMPLE_SIZE,
                 /* mono or stereo */ AudioSystem.NOT_SPECIFIED, 
                 /* signed */ true,  
                 /* big-endian */ true);

            /*
             * We need to get the PCM converted frame length and not from input!
             * For example, input may not be 48KHz sampled and/or 16 bits-per-sample.
             * We get info for each input audio file and close the stream so that 
             * we don't have to keep all input file streams open at the same time.
             */
            for (int i = 0; i < files.length; i++) {                
                AudioInputStream ais = AudioSystem.getAudioInputStream(bdjFormat, 
                    AudioSystem.getAudioInputStream(files[i]));

                // collect channel count and frame length.
                channels[i] = ais.getFormat().getChannels();
                frameLengths[i] = (int) ais.getFrameLength();
 
                ais.close();
            }


            FileOutputStream fos = new FileOutputStream(outputFile);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fos));

            writeSoundHeader(out, numInputs);
            writeSoundAttributes(out, channels, frameLengths);

            for (int i = 0; i < files.length; i++) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(bdjFormat, 
                    AudioSystem.getAudioInputStream(files[i]));
                byte[] buf = new byte[8*1024]; // 8K at a time
                int numBytes = -1;
                while ((numBytes = ais.read(buf, 0, buf.length)) > 0) {
                    out.write(buf, 0, numBytes);
                }
                // close current input file
                ais.close();
            }

            // close output file
            out.close();
            fos.close();
        } catch (FileNotFoundException fnfe) {
            errorExit(fnfe, 2);
        } catch (IllegalArgumentException iae) {
            errorExit(iae, 2);
        } catch (IOException exp) {
            errorExit(exp, 2);
        } catch (UnsupportedAudioFileException uafe) {
            errorExit(uafe, 2);
        } 
    }


    private static void writeSoundHeader(DataOutputStream dos, int numInputs) throws IOException {
        // Refer to section 5.6.3 sound.bdmv - Syntax table
        dos.write(SOUND_BDMV_TYPE_INDICATOR);
        dos.write(SOUND_BDMV_VERSION);
        
        // Section 5.6.4.1 SoundIndex() - Syntax table
        final int sizeTillSoundIndex = 
                    4 + /* type indicator */
                    4 + /* version */
                    4 + /* SoundData_start_address */
                    4 + /* ExtensionData_start_address */
                    24;   /* reserved_for_future_use */

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
                        (numInputs*perEntrySize); 


        // SoundData_start_address (4 bytes)
        dos.writeInt(sizeTillSoundIndex + sizeofSoundIndex);

        // ExtensionData_start_address  (4 bytes)
        dos.writeInt(0);

        // reserved (24 bytes) 
        for (int i = 0; i < 24; i++) {
            dos.write(0);
        }

        // SoundIndex() start..

        /*
         * length (4 bytes) - length is number of bytes immediately following 
         * length field and up to the end of SoundIndex() -- so, it does *not* 
         * include the size of the 'length' field itself. (section 5.6.4.2)
         */
        dos.writeInt(sizeofSoundIndex - 4);
        // reserved (1 byte)
        dos.write(0);
        // number of sound entries (1 byte)
        dos.write(numInputs);
    }

    private static void writeSoundAttributes(DataOutputStream dos, int[] channels, int[] frameLengths) 
        throws IOException {
        /*
         * Refer to table 5.6.4.1 SoundIndex() - Syntax
         */
        int totalSize = 0;
        for (int i = 0; i < channels.length; i++) {
            int outputFrameSize = channels[i] * BD_J_SAMPLE_SIZE/8;
            int currentSize = frameLengths[i] * outputFrameSize;
            boolean isStereo = channels[i] > 1;

            // channel configuration (4 bits): 1=mono, 3=stereo
            // sampling frequency (4 bits): must be 1=48 kHz
            dos.write(isStereo ? 0x31 : 0x11);

            // bits per sample (2 bits)
            // reserved (6 bits): must be 1 (16 bits/sample)
            dos.write(0x40);

            // sound_data_start_address (4 bytes)
            dos.writeInt(totalSize);

            // sound_data_length (4 bytes)
            dos.writeInt(currentSize);

            totalSize += currentSize;
        }
    }

    private static void usage(int code) {
        System.err.println("sound.bdmv converter");
        System.err.print("Usage: ");
        System.err.println("java " + BDJSoundGenerator.class.getName() + " [-debug] <inputs> <output>");
        System.exit(code);
    }

    private static void errorExit(Exception exp, int code) {
        System.err.println(exp.getMessage());
        if (debug) exp.printStackTrace();
        System.exit(code);
    }

    private static void errorExit(String msg, int code) {
        System.err.println(msg);
        System.exit(code);
    }
}
