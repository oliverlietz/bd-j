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
 * This class represents an degenerate case of an easing function that 
 * consists of just listing the in between points.  It can be used by the
 * GRIN compiler to generate a linear interpolation that approximates that
 * list of points within a certain error.
 **/

public class PointsEasingEquation extends EasingEquation {

    private int[][] points;

    public PointsEasingEquation(int[][] points) {
        this.points = points;
    }

    public void addKeyFrames(ArrayList<int[]> keyFrames, int[] end) 
            throws IOException
    {
        int[] start = keyFrames.get(keyFrames.size() - 1);
        int startFrame = start[0];
        int endFrame = end[0];
        int duration = endFrame - startFrame;
        int[][] allFrames = new int[duration + 1][];
        if (points.length != duration) {
            throw new IOException("Expected " + duration +
                                  " points, got " + points.length);
        }
        if (start.length != end.length) {
            throw new IOException("Start and end frames have different number of values");
        }
        allFrames[0] = new int[start.length];
        for (int i = 0; i < start.length; i++) {
            allFrames[0][i] = start[i];
        }
        for (int f = 1; f <= duration; f++) {
            int[] pts = points[f-1];
            if (pts.length != end.length - 1) {
                throw new IOException("In point set " + f + " expected " +
                                      (end.length - 1) + " values but got " +
                                      pts.length);
            }
            allFrames[f] = new int[end.length];
            allFrames[f][0] = f + startFrame;
            for (int i = 1; i < end.length; i++) {
                allFrames[f][i] = pts[i-1];
            }
        }
        for (int i = 1; i < end.length; i++) {
            if (allFrames[duration][i] != end[i]) {
                throw new IOException("End point value " + i 
                                      + " mismatch:  " + end[i]
                                      + " != " + allFrames[duration][i]);
            }
        }
        trimUnneededKeyFrames(startFrame, keyFrames, end, allFrames);
    }

    public double evaluate(double t, double b, double c, double d) {
        throw new RuntimeException("not implemented, shouldn't be called");
    }
}
