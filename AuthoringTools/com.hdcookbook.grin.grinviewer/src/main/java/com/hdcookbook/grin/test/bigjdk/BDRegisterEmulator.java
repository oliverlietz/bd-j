
/*  
 * Copyright (c) 2007, Sun Microsystems, Inc.
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

package com.hdcookbook.grin.test.bigjdk;

/**
 * This class lets you emulate Blu-ray player registers in GRIN show
 * commands that run under GrinView.  This makes it possible to debug
 * register-based control logic on a PC, rather than being forced to 
 * run in a BD emulator.
 *
 * @see com.hdcookbook.grin.commands.ShowCommands
 *
 * @author Bill Foote
 **/

import java.util.ArrayList;

public class BDRegisterEmulator {

    private static Object LOCK = new Object();
    private static BDRegisterEmulator theInstance;

    private int gpr[] = new int[4096];
    private int psr[] = new int[62];

    private ArrayList<BDRegisterEmulatorScreen> screens
        = new ArrayList<BDRegisterEmulatorScreen>();
                // There should be zero or one of these because the
                // GrinView UI only makes one registers screen, but in
                // principle there could be multiple views.

    private BDRegisterEmulator() {
    }

    public static BDRegisterEmulator getInstance() {
        synchronized(LOCK) {
            if (theInstance == null) {
                theInstance = new BDRegisterEmulator();
            }
        }
        return theInstance;
    }

    /**
     *
     * @throws IllegalArgumentException   if reg < 0 or >= 4096
     **/
    public int getGPR(int reg) {
        if (reg < 0 || reg> gpr.length) {
            throw new IllegalArgumentException("Illegal GPR register " + reg);
        }
        return gpr[reg];
    }
    /**
     *
     * @throws IllegalArgumentException   if reg < 0 or >= 4096
     **/
    public void setGPR(int reg, int value) {
        if (reg < 0 || reg> gpr.length) {
            throw new IllegalArgumentException("Illegal GPR register " + reg);
        }
        gpr[reg] = value;
        synchronized(screens) {
            for (int i = 0; i < screens.size(); i++) {
                screens.get(i).updateGPR(reg, value);
            }
        }
    }

    /**
     *
     * @throws IllegalArgumentException  if reg is an illegal value
     **/
    public int getPSR(int reg) {
        if (reg < 0 || reg > psr.length || reg == 0 || reg == 9
            || reg == 10 || reg == 11 || (reg >= 21 && reg <= 28)
            || (reg >= 32 && reg <= 35) || reg == 41
            || (reg >= 45 && reg <= 47)) 
        {
            throw new IllegalArgumentException("Illegal GPR register " + reg);
        }
        return psr[reg];
    }

    int[] getPSRs() {
        return psr;
    }

    int[] getGPRs() {
        return gpr;
    }

    void addScreen(BDRegisterEmulatorScreen screen) {
        synchronized(screens) {
            screens.add(screen);
        }
    }

}
