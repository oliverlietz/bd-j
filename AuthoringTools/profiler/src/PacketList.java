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
 *  A list of the received packets.
 **/

import java.util.HashMap;
import java.util.Map;
import java.net.DatagramPacket;

import com.hdcookbook.grin.util.Profile;

public class PacketList {

    private HashMap<MessageKey, MessageKey> messages = new HashMap<MessageKey, MessageKey>();
        // We use messages to canonicalize MessageKey instances, so we can avoid allocating
        // them.

    private Packet[] packets;
    private int length;
    private boolean done = false;
    MessageKey tmpKey = new MessageKey();

    public PacketList(int capacity) {
        //
        // Pre-allocate everything we can to minimize GC pauses during
        // profile data capture
        //
        packets = new Packet[capacity];
        for (int i = 0; i < capacity; i++) {
            packets[i] = new Packet();
        }
    }

    public int getCapacity() {
        return packets.length;
    }

    public int getLength() {
        return length;
    }

    /**
     * Mark this list as closed for new additions.
     **/
    public synchronized void setDone() {
        done = true;
        notifyAll();
    }

    public synchronized boolean getDone() {
        return done;
    }

    public synchronized void add(DatagramPacket packet, long timestamp) {
        if (done || length >= packets.length) {
            return;
        }
        byte[] buf = packet.getData();
        int i = packet.getOffset();
        int len = packet.getLength();
        if (len < 1) {
            return;
        }
        byte type = buf[i++];
        if (type == Profile.MESSAGE) {
            Packet p = packets[length++];
            p.type = type;
            p.timestamp = timestamp;
            p.debugMessage = new byte[len-1];
                // We're forced to allocate this here, since the message,
                // by definition, can change each time
            System.arraycopy(buf, i, p.debugMessage, 0, len-1);
            notifyAll();        // In case a follower is waiting on get()
            return;
        }
        if (len < 5) {
            return;
        }
        int id = 0xff & (int) buf[i++];
        id <<= 8;
        id += 0xff & (int) buf[i++];
        id <<= 8;
        id += 0xff & (int) buf[i++];
        id <<= 8;
        id += 0xff & (int) buf[i++];
        byte threadID = (byte) 0;
        MessageKey message = null;
        if (type == Profile.TIMER_START) {
            if (len < 6) {
                return;
            }
            threadID = buf[i++];
            tmpKey.set(buf, i, len - i);
            message = messages.get(tmpKey);
            if (message == null) {
                message = tmpKey.makeCopy();
                messages.put(message, message);
            }
        } else if (type == Profile.TIMER_STOP) {
            // do nothing
        } else {
            return;
        }
        Packet p = packets[length++];
        p.type = type;
        p.id = id;
        p.threadID = threadID;
        p.message = message;
        p.timestamp = timestamp;
        notifyAll();    // In case a follower is waiting on get()
    }

    /**
     * Get the ith packet received (counting from 0).  If this
     * packet hasn't yet arrived, block until it does.  Return null
     * if it hasn't arrived and we're done receiving packets, or if
     * we're interrupted.
     **/
    public synchronized Packet get(int i) {
        for (;;) {
            if (i < length) {
                break;
            }
            if (done) {
                return null;
            }
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return packets[i];
    }
}
