/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
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

/**
 * A ProfilingRun contains the set of data from one run of the
 * profiler.
 **/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.JsonIO;
import com.hdcookbook.grin.util.Profile;

public class ProfilingRun {

    public ProfileTiming[] timings;
    public Packet[] debugMessages;
    public long earliestTimestamp = Long.MAX_VALUE;

    public ProfilingRun() {
    }

    /**
     * Initialize from a captured PacketList
     **/
    public void init(PacketList packets) {
        if (!packets.getDone()) {
            throw new IllegalStateException();
        }
        int numMessages = 0;
        int maxID = 0;
        int minID = Integer.MAX_VALUE;
        for (int i = 0; i < packets.getLength(); i++) {
            Packet p = packets.get(i);
            if (p == null) {
                continue;
            }
            if (p.type == Profile.MESSAGE) {
                numMessages++;
                continue;
            }
            if (p.id > maxID) {
                maxID = p.id;
            }
            if (p.id < minID) {
                minID = p.id;
            }
        }
        if (minID > maxID) {
            minID = maxID;
        }
        Packet[] startPackets = new Packet[maxID - minID + 1];
        debugMessages = new Packet[numMessages];
        numMessages = 0;
        for (int i = 0; i < packets.getLength(); i++) {
            Packet p = packets.get(i);
            if (p == null) {
                continue;
            }
            if (p.type == Profile.TIMER_START) {
                startPackets[p.id - minID] = p;
                if (p.timestamp < earliestTimestamp) {
                    earliestTimestamp = p.timestamp;
                }
            } else if (p.type == Profile.MESSAGE) {
                if (p.timestamp < earliestTimestamp) {
                    earliestTimestamp = p.timestamp;
                }
                debugMessages[numMessages++] = p;
            }
        }
        int num = 0;
        for (int i = 0; i < packets.getLength(); i++) {
            Packet p = packets.get(i);
            if (p == null) {
                continue;
            }
            if (p.type == Profile.TIMER_STOP) {
                Packet start = startPackets[p.id - minID];
                if (start == null) {
                    System.out.println("Missing start packet for " + p.id);
                } else {
                    num++;
                }
            }
        }

        timings = new ProfileTiming[num];
        num = 0;
        for (int i = 0; i < packets.getLength(); i++) {
            Packet p = packets.get(i);
            if (p == null) {
                continue;
            }
            if (p.type == Profile.TIMER_START) {
                // nothing
            } else if (p.type == Profile.TIMER_STOP) {
                Packet start = startPackets[p.id - minID];
                if (start == null) {
                    // nothing
                } else {
                    ProfileTiming t = new ProfileTiming();
                    timings[num++] = t;
                    t.startTime = start.timestamp - earliestTimestamp;
                    t.duration = p.timestamp - start.timestamp;
                    t.threadID = 0xff & ((int) start.threadID);
                    t.message = start.message.toString();
                }
            }
        }
    }

    public void initFromFile(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        Reader rdr = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
        Object o = JsonIO.readJSON(rdr);
        earliestTimestamp = ((Number) o).longValue();
        o = JsonIO.readJSON(rdr);
        int num = ((Number) o).intValue();
        timings = new ProfileTiming[num];
        for (int i = 0; i < num; i++) {
            ProfileTiming t = new ProfileTiming();
            t.readData(rdr);
            timings[i] = t;
        }
        o = JsonIO.readJSON(rdr);
        num = ((Number) o).intValue();
        debugMessages = new Packet[num];
        for (int i = 0; i < debugMessages.length; i++) {
            Packet p = new Packet();
            debugMessages[i] = p;
            p.type = Profile.MESSAGE;
            o = JsonIO.readJSON(rdr);
            p.timestamp = ((Number) o).longValue();
            o = JsonIO.readJSON(rdr);
            Object[] ia = (Object[]) o;
            p.debugMessage = new byte[ia.length];
            for (int j = 0; j < ia.length; j++) {
                p.debugMessage[j] = (byte) ((Integer) ia[j]).intValue();
            }
        }
        System.out.println("Read " + num + " timing records.");
    }

    public void writeData(String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            JsonIO.writeJSON(w, new Long(earliestTimestamp));
            w.write('\n');
            JsonIO.writeJSON(w, new Integer(timings.length));
            w.write('\n');
            for (int i = 0; i < timings.length; i++) {
                timings[i].writeData(w);
            }
            JsonIO.writeJSON(w, new Integer(debugMessages.length));
            w.write('\n');
            for (int i = 0; i < debugMessages.length; i++) {
                Packet p = debugMessages[i];
                JsonIO.writeJSON(w, new Long(p.timestamp));
                Integer[] ia = new Integer[p.debugMessage.length];
                for (int j = 0; j < ia.length; j++) {
                    ia[j] = new Integer(0xff & (int) p.debugMessage[j]);
                }
                JsonIO.writeJSON(w, ia);
                w.write('\n');
            }
            w.close();
            System.out.println("Wrote " + timings.length + " timing records.");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Failed to write to " + fileName);
            System.exit(1);
        }
    }
}
