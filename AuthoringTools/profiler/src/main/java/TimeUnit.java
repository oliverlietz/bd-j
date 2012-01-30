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

/*
 * A TimeUnit holder class. It takes care of conversions
 * between time units, the field names for the time units used by prefuse
 * and the display name for each unit
 */
public enum TimeUnit {

    SEC (1000000000, "seconds", "StartTimeSec", "DurationSec", 0),
    MILLIS (1000000, "milli seconds", "StartTimeMillis", "DurationMillis", 1),
    MICRO  (1000, "micro seconds", "StartTimeMicro", "DurationMicro", 2),
    NANO  (1, "nano seconds", "StartTime", "Duration", 3);
    

    private final double conversionFactor;
    private final String displayName;
    private final String startTimeField;
    private final String durationField;
    private final int index;

    TimeUnit(double conv, String name, String sField, String dField, int index) {
        this.conversionFactor = conv;
        this.displayName = name;
        this.startTimeField = sField;
        this.durationField = dField;
        this.index = index;
    }

    public double convertionFact() {
        return conversionFactor;
    }

    public String dname() {
        return displayName;
    }

    public String startTimeField() {
        return startTimeField;
    }

    public String durationField() {
        return durationField;
    }

    public int index() {
        return index;
    }

    public double convert(double value) {
        return (value / conversionFactor);
    }

    public static TimeUnit match(String name) {
        if (name.equals(NANO.dname())) {
            return NANO;
        } else if (name.equals(MICRO.dname())) {
            return MICRO;
        } else if (name.equals(MILLIS.dname())) {
            return MILLIS;
        } else if (name.equals(SEC.dname())) {
            return SEC;
        } else {
            System.out.println("Invalid TimeUnit name:" + name);
            return null;
        }
    }
}
