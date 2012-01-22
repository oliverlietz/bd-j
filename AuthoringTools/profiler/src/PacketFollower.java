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
 * A PacketFollower will follow the growth of a PacketList, and send a
 * text record  to stdout.  It's like "tail -f" for a PacketList.
 **/

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.Profile;

public class PacketFollower extends Thread {

    PacketList packets;
    private boolean running = false;

    private Packet[] startPackets;      // Indexed by ID

    public PacketFollower(PacketList packets) {
        this.packets = packets;
        startPackets = new Packet[packets.getCapacity() / 2];
            // No more than half of the packets can be start packets
    }

    public synchronized void toggleRunning() {
        running = !running;
        System.out.println("Follow mode now " + running);
        notifyAll();
    }

    public void run() {
        for (int i = 0; ; i++) {
            Packet p = packets.get(i);
            if (p == null) {
                return;
            }
            synchronized(this) {
                while (!running) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
            if (p.id < 0 || p.id >= startPackets.length) {
                System.out.println("Illegal packet id " + p.id);
            } else if (p.type == Profile.TIMER_START) {
                startPackets[p.id] = p;
            } else if (p.type == Profile.TIMER_STOP) {
                Packet start = startPackets[p.id];
                if (start == null) {
                    System.out.println("Missing start packet for " + p.id);
                } else {
                    String time = "" + (p.timestamp - start.timestamp) + " ns ";
                    if (time.length() < 15) {
                        time = "               ".substring(time.length()) + time;
                    }
                    System.out.println("[Time Profile tid=" 
                        + (0xff & (int) start.threadID) + "]  " + time
                        + start.message);
                }
            } else if (p.type == Profile.MESSAGE) {
                    String time = "" + (p.timestamp) + " ns ";
                    if (time.length() < 15) {
                        time = "               ".substring(time.length()) + time;
                    }
                    System.out.println("[Debug message " + time + ":");
                    System.out.println(p.getDebugMessage());
            } else {
                System.out.println("Unrecognized packet!");
            }
        }
    }
}
