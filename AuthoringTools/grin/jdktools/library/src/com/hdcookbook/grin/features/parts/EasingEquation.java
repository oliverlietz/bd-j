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

package com.hdcookbook.grin.features.parts;

import java.util.ArrayList;
import java.io.IOException;

/**
 * This abstract class represents an easing function that can be used by the
 * GRIN compiler to generate a linear interpolation.  See ShowParser to
 * see how to tie this to a particular easing function.
 **/

public abstract class EasingEquation {

    /** 
     * Total number of keyframes added to approximate interpolations.
     **/
    public static int framesAdded = 0;

    private int maxError = 0;

    /**
     * Evaluate the eqsing equation.
     *
     * @param t current time in frames, seconds, or any other unit
     * @param b beginning value
     * @param c change in value
     * @param d duration in frames, seconds, or any other unit
     **/
    abstract public double evaluate(double t, double b, double c, double d);

    /**
     * Set the maximum allowed error when generating linear interpolations
     * from this equation.  Defaults to zero.
     **/
    public void setMaxError(int maxError) {
        this.maxError = maxError;
    }

    /**
     * Approximate this easing function with linear interpolation segments,
     * making sure that the error doesn't exceed maxError units.  We start
     * the easing from keyFrames[size-1], and add to keyFrames.
     *
     * @param   keyFrames a list of { frame#, value, ... } int arrays
     * @param   end       Where to ease to, { frame#, value, ... }
     **/
    public void addKeyFrames(ArrayList<int[]> keyFrames, int[] end) 
            throws IOException
    {
        int[] start = keyFrames.get(keyFrames.size() - 1);
        int startFrame = start[0];
        int endFrame = end[0];
        int duration = endFrame - startFrame;
        int[][] allFrames = new int[duration + 1][];
        for (int f = 0; f <= duration; f++) {
            allFrames[f] = new int[end.length];
            allFrames[f][0] = f + startFrame;
            for (int i = 1; i < end.length; i++) {
                double val = evaluate(f, start[i], end[i]-start[i], duration);
                allFrames[f][i] = (int) Math.round(val);
            }
        }
        trimUnneededKeyFrames(startFrame, keyFrames, end, allFrames);
    }

    /**
     * Approximate the list of keyFrames with a reduced list, within the error
     * band of this easing equation.  This is called from addKeyFrames().
     **/
    protected void trimUnneededKeyFrames(int startFrame,
                                         ArrayList<int[]> keyFrames, 
                                         int[] end,
                                         int[][] allFrames)
            throws IOException
    {
        for(;;) {
            int[] current = keyFrames.get(keyFrames.size() - 1);
            if (current[0] >= end[0]) {
                return;         // All done!
            }
            int frame = current[0] + 1;
            // Go until at end of function, or error is too big
            for (;;) {
                frame++;
                if (frame > end[0]) {
                    break;
                }
                int[] candidate = allFrames[frame - startFrame];
                boolean tooMuchError = false;
                // Check interpolation algorithm from InterpolatedModel
                for (int f = current[0] + 1; f < candidate[0]; f++) {
                    int dist = candidate[0] - current[0];
                    int distNext = candidate[0] - f;
                    int distLast = f - current[0];
                    for (int i = 1; i < current.length; i++) {
                        int v = (candidate[i] * distLast
                                 + current[i] * distNext) / dist;
                        int err = v - allFrames[f - startFrame][i];
                        if (Math.abs(err) > maxError) {
                            tooMuchError = true;
                        }
                    }
                }
                if (tooMuchError) {
                    break;
                }
            }
            frame--;
            keyFrames.add(allFrames[frame - startFrame]);
            framesAdded++;
        }
    }

    //
    // Useful for debugging:
    //
    private String format(int[] arr) {
        String result = "f " + arr[0] + ":  ";
        for (int i = 1; i < arr.length; i++) {
            result += arr[i] + "  ";
        }
        return result;
    }
}
