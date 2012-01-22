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

/**
 *  This is a simple program that runs on a computer and collects the time
 *  profile information of the desired operations of an xlet running on a player.
 *  Note down the IP address and the port this program displays when it is
 *  launched, and provide them in a call to Profile.initProfiler() in an xlet
 *  of interest.
 */

import java.io.ByteArrayInputStream;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import com.hdcookbook.grin.util.Debug;
import com.hdcookbook.grin.util.Profile;

public class PCProfiler extends Thread {

    private int myPort;
    private DatagramSocket socket;
    PacketList packets = new PacketList(2000000);       // 1M measurements
    private boolean started;
    private PacketFollower follower = null;



    private static Map<Integer, Long> timestamps = new HashMap<Integer, Long>();
    private static Map<Integer, String> profileMessages =
            new HashMap<Integer, String>();

    public PCProfiler(int myPort) {
        this.myPort = myPort;
        try {
            socket = new DatagramSocket(myPort);
            InetAddress myAddr = InetAddress.getLocalHost();
            System.out.println("Listening for messages on port: " + myPort +
                    " on IP address:" + myAddr);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //
    // Receives datagram packets and adds to list
    //
    public void run() {

        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        System.gc();
        System.out.println("Ready to receive...");
        synchronized(this) {
            started = true;
            notifyAll();
        }
        while (true) {
            PacketList list = null;
            try {
                socket.receive(packet);
                list = packets;
                if (list == null) {
                    return;
                }
                list.add(packet, System.nanoTime());
            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
                break;
            }
            if (list.getDone()) {
                return;
            }
        }
    }

    private void waitUntilStarted() {
        synchronized(this) {
            for (;;) {
                if (started) {
                    return;
                }
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * Closes the receiving socket
     **/
    public void closeReceiveSocket() {
        try {
            socket.close();
        } catch (Exception e) {
        }
    }

    public void printHelp() {
        System.out.println();
        System.out.println("Commands:");
        System.out.println("    d    Dump data collected so far and run GUI");
        System.out.println("    q    Quit");
        System.out.println("    f    Toggle follow mode, which shows what's received on stdout");
        System.out.println("  <eof>  Same as q (thus, nothing happens when you double-click the JAR");
        System.out.println();
        System.out.println("You can also launch the program with a data file as an argument.");
        System.out.println("This will bring up the GUI on that dataset.");
        System.out.println();
    }

    public void readCommands() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        waitUntilStarted();
        System.out.println();
        System.out.println("Now capturing packets.");
        printHelp();
        for (;;) {
            String s = in.readLine();
            if (s == null) {
                System.exit(0);
            }
            s=s.trim().toLowerCase();
            if ("d".equals(s)) {
                dumpData(in);
            } else if ("q".equals(s)) {
                System.exit(0);
            } else if ("f".equals(s)) {
                toggleFollow();
            } else {
                System.out.println("??" + ((char) 7));
                printHelp();
            }
        }
    }

   
    // The toggle follow command from the keyboard
    private void toggleFollow() {
        if (follower == null) {
            follower = new PacketFollower(packets);
            follower.setPriority(3);
            follower.start();
        }
        follower.toggleRunning();
    }
   
    // The dump data command from the keyboard
    private void dumpData(BufferedReader in) {
        packets.setDone();
        System.out.println();
        System.out.println("Dumping information derived from " 
                           + packets.getLength() + " packets.");

        ProfilingRun run = new ProfilingRun();
        run.init(packets);
        packets = null;         // Allows GC, unless there's a follower
        run.writeData("profile.dat");
        System.out.println("    Wrote data to profile.dat");
        ResultsGui gui = new ResultsGui();
        gui.init(run);
        run = null;     // Allows GC
        gui.readCommands(in);
        System.exit(0);
    }

    //
    // Launching the GUI from the command line
    //
    private static void launchGUIFromFile(String fileName) throws IOException {
        ProfilingRun run = new ProfilingRun();
        run.initFromFile(fileName);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ResultsGui gui = new ResultsGui();
        gui.init(run);
        run = null;     // Allows GC
        gui.readCommands(in);
        System.exit(0);
    }

    public static void main(String args[]) {
        if (args.length > 0) {
            System.out.println("Reading data file from " + args[0]);
            try {
                launchGUIFromFile(args[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }
        PCProfiler prof = new PCProfiler(2008);
        try {
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
            System.out.println();
            System.out.println("Try setting maximum heap size, like this:");
            System.out.println("    java -Xmx200m -jar build/profiler.jar");
            System.out.println();
            System.exit(1);

            // If you're as annoyed by this as I am, see why this can't be set
            // in the JAR file at 
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6202113
        }
        prof.setPriority(5);
        prof.start();
        try {
            prof.readCommands();
        } catch (IOException e) {
            Debug.printStackTrace(e);
            System.exit(0);
        }
    }
}
