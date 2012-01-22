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

package net.java.bd.tools.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Logger {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss ");
    private static final String lineSep = System.getProperty("line.separator");
    private static File[] logFiles = null;
    
    /**
    private static String WORK_DIR = System.getProperty("bluray.bindingunit.root");
    private static File[] logFiles = { 
        new File(WORK_DIR, "logA.txt"),               
        new File(WORK_DIR, "logB.txt")
    };
     **/

    public static final int MAX_FILE_LENGTH = 5120; // in bytes

    /**
     * Sets the logging output file.
     */
    public static void setLogFile(String file) {
        
        File log = new File(file);
        /**
        if (!log.canWrite()) {
            log("ERROR: Log file " + file + " is not writable.");
            return;
        }
        **/
        
        String path = log.getPath();
        String backupFileName = log.getName().concat(".bak");
        logFiles = new File[] {
                log, new File(path, backupFileName) 
        };
        
        Logger.log("Setting the log file to " + file);
    }
   
    /**
     * Returns log files in proper order: the recent one comes first 
     * @return
     */
    public static File[] getLogFiles() {
        if (logFiles == null) {
            return null;
        }
        
        if (logFiles[0].lastModified() < logFiles[1].lastModified()) {
            File t = logFiles[0];
            logFiles[0] = logFiles[1];
            logFiles[1] = t;
        }

        return logFiles;
    }

    /**
     * 
     * @param ff
     */
    private static synchronized void activateNextFile(File[] ff) {
        ff[1].delete();
        try {
            ff[1].createNewFile();
        } catch (IOException e) {
            System.out.println("Logger: Failed to create a file " + ff[1].getAbsolutePath());
            e.printStackTrace();
        }
        File t = ff[0];
        ff[0] = ff[1];
        ff[1] = t;
    }

    /**
     * Puts a string s into the log
     * @param s
     */
    public static synchronized void log(String s) {
        if (s == null || s.length() == 0) {
            return;
        }
        
        if (observers != null) {
            for (int i = 0; i < observers.size(); i++) {
               ((Observer)observers.elementAt(i)).output(s);
            }
        }

        File[] ff = getLogFiles();
        if (ff != null) {
            int freeSpace = MAX_FILE_LENGTH - (int) ff[0].length();

            String logRecord = sdf.format(new Date()) + s + lineSep;

            if (logRecord.length() <= freeSpace) {
                output(logRecord, ff[0]);
            } else {
                output(logRecord.substring(0, freeSpace), ff[0]);
                activateNextFile(ff);
                String tail = logRecord.substring(freeSpace);

                while (tail.length() > MAX_FILE_LENGTH) {
                    output(tail.substring(0, MAX_FILE_LENGTH), ff[0]);
                    activateNextFile(ff);
                    tail = tail.substring(MAX_FILE_LENGTH);
                }

                output(tail, ff[0]);
            }
        }
    }

    /**
     * 
     * @param s
     * @param f
     */
    private static synchronized void output(String s, File f) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f.getAbsolutePath(), true));
            bw.write(s);
        } catch (IOException e) {
            System.out.println("Logger: Failed to instantiate a FileWriter for " + f.getAbsolutePath());
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.out.println("Logger: Failed to close the BufferedWriter for " + f.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Puts a string s and a stack trace of t into the log
     * @param s
     * @param t
     */
    public static void log(String s, Throwable t) {
        StringWriter sw = null;
        PrintWriter pw = null;

        try {
            sw = new StringWriter();
            sw.write(s);
            sw.write(lineSep);

            if (t != null) {
                pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.flush();
            }

            sw.flush();
            log(sw.toString());
        } catch (Exception ex) {
            System.out.println("Exception while printing a stack trace to the log: " + ex.toString());
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Exception e) {
                System.out.println("Exception while closing a PrintWriter: " + e.toString());
            }

            try {
                if (sw != null) {
                    sw.close();
                }
            } catch (Exception e) {
                System.out.println("Exception while closing a StringWriter: " + e.toString());
            }
        }
    }

    /**
     * 
     */
    public static void clearLog() {
        File[] ff = getLogFiles();
        if (ff != null) {
            ff[0].delete();
            ff[1].delete();
        }

        if (observers != null) {
            for (int i = 0; i < observers.size(); i++) {
               ((Observer)observers.elementAt(i)).clearLog();
            }
        }
    }

    public static interface Observer {

        public void output(String s);

        public void clearLog();
    }

    private static Vector observers;
    public static void addObserver(Observer obs) {
        synchronized(Logger.class) {
           if (observers == null) {
              observers = new Vector(); 
           }
           observers.add(obs);
        }
    }

    public static void removeObserver(Observer obs) {
        synchronized(Logger.class) {
            if (observers != null) {
                observers.remove(obs);
            }
        }
    }
}
