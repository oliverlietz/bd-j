
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

package com.robertpenner;

/**
  * These tweening functions provide different flavors of 
  * math-based motion under a consistent API. 
  * <p>
  * Types of easing:
  * <p><ul>
  *       <li> Linear
  *       <li> Quadratic
  *       <li> Cubic
  *       <li> Quartic
  *       <li> Quintic
  *       <li> Sinusoidal
  *       <li> Exponential
  *       <li> Circular
  *       <li> Elastic
  *       <li> Back
  *       <li> Bounce
  * </ul>
  * <p>
  * Discussed in Chapter 7 of 
  * Robert Penner's Programming Macromedia Flash MX
  * (including graphs of the easing equations)
  * <p>
  * <a href="http://www.robertpenner.com/profmx">http://www.robertpenner.com/profmx</a><br>
  * <a href="http://www.amazon.com/exec/obidos/ASIN/0072223561/robertpennerc-20">
  *    http://www.amazon.com/exec/obidos/ASIN/0072223561/robertpennerc-20</a></br>
  *
  * <p>
  * This was adapted by Bill Foote from:<br>
  * Easing Equations v1.5<br>
  * May 1, 2003<br>
  * (c) 2003 Robert Penner, all rights reserved. <br>
  * <p>
  * This work is subject to the terms in 
  * <a href="http://www.robertpenner.com/easing_terms_of_use.html">
  * http://www.robertpenner.com/easing_terms_of_use.html</a>, which is
  * repproduced in <a href="penner_terms.html">penner_terms.html</a>.
  *
  *     @author Robert Penner, http://www.robertpenner.com
  *     @author Bill Foote, http://jovial.com
  **/



public class PennerEasing {

    /**
     * Value to use to mean "default value" for a small number of optional 
     * function parameters.  In Java, nothing is == to Double.NaN, so you
     * have to test with Double.isNaN().
     **/
    public static double DEFAULT = Double.NaN;

    private PennerEasing() {
    }

    /**
     * <pre>
     * ///////////// QUADRATIC EASING: t^2 ///////////////////
     * // quadratic easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in value, d: duration
     * // t and d can be in frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeInQuad(double t, double b, double c, double d) {
        return c*(t/=d)*t + b;
    }

    /**
     * <pre>
     * ///////////// QUADRATIC EASING: t^2 ///////////////////
     * // quadratic easing out - decelerating to zero velocity
     * </pre>
     **/
    public static double easeOutQuad(double t, double b, double c, double d) {
        return -c *(t/=d)*(t-2) + b;
    }

    /**
     * <pre>
     * ///////////// QUADRATIC EASING: t^2 ///////////////////
     * // quadratic easing in/out - acceleration until halfway, then deceleration
     * </pre>
     **/
    public static double easeInOutQuad(double t, double b, double c, double d) {
        if ((t/=d/2) < 1) return c/2*t*t + b;
        return -c/2 * ((--t)*(t-2) - 1) + b;
    }



    /**
     * <pre>
     * ///////////// CUBIC EASING: t^3 ///////////////////////
     * // cubic easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in value, d: duration
     * // t and d can be frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeInCubic (double t, double b, double c, double d){
        return c*(t/=d)*t*t + b;
    }

    /**
     * <pre>
     * ///////////// CUBIC EASING: t^3 ///////////////////////
     * // cubic easing out - decelerating to zero velocity
     * // t: current time, b: beginning value, c: change in value, d: duration
     * // t and d can be frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeOutCubic(double t, double b, double c, double d) {
        return c*((t=t/d-1)*t*t + 1) + b;
    }

    /**
     * <pre>
     * ///////////// CUBIC EASING: t^3 ///////////////////////
     * // cubic easing in/out - acceleration until halfway, then deceleration
     * // t: current time, b: beginning value, c: change in value, d: duration
     * // t and d can be frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeInOutCubic(double t, double b, double c, double d) {
        if ((t/=d/2) < 1) return c/2*t*t*t + b;
        return c/2*((t-=2)*t*t + 2) + b;
    }



    /**
     * <pre>
     * ///////////// QUARTIC EASING: t^4 /////////////////////
     * // quartic easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in value, d: duration
     * // t and d can be frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeInQuart(double t, double b, double c, double d) {
        return c*(t/=d)*t*t*t + b;
    }

    /**
     * <pre>
     * ///////////// QUARTIC EASING: t^4 /////////////////////
     * // quartic easing out - decelerating to zero velocity
     * </pre>
     **/
    public static double easeOutQuart(double t, double b, double c, double d) {
        return -c * ((t=t/d-1)*t*t*t - 1) + b;
    }

    /**
     * <pre>
     * ///////////// QUARTIC EASING: t^4 /////////////////////
     * // quartic easing in/out - acceleration until halfway, then deceleration
     * </pre>
     **/
    public static double easeInOutQuart(double t, double b, double c, double d) {
        if ((t/=d/2) < 1) return c/2*t*t*t*t + b;
        return -c/2 * ((t-=2)*t*t*t - 2) + b;
    }



    /**
     * <pre>
     * ///////////// QUINTIC EASING: t^5  ////////////////////
     * // quintic easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in value, d: duration
     * // t and d can be frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeInQuint(double t, double b, double c, double d) {
        return c*(t/=d)*t*t*t*t + b;
    }

    /**
     * <pre>
     * ///////////// QUINTIC EASING: t^5  ////////////////////
     * // quintic easing out - decelerating to zero velocity
     * </pre>
     **/
    public static double easeOutQuint(double t, double b, double c, double d) {
        return c*((t=t/d-1)*t*t*t*t + 1) + b;
    }

    /**
     * <pre>
     * ///////////// QUINTIC EASING: t^5  ////////////////////
     * // quintic easing in/out - acceleration until halfway, then deceleration
     * </pre>
     **/
    public static double easeInOutQuint(double t, double b, double c, double d) {
        if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
        return c/2*((t-=2)*t*t*t*t + 2) + b;
    }


    /**
     * <pre>
     * ///////////// SINUSOIDAL EASING: sin(t) ///////////////
     * // sinusoidal easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in position, d: duration
     * </pre>
     **/
    public static double easeInSine(double t, double b, double c, double d) {
        return -c * Math.cos(t/d * (Math.PI/2)) + c + b;
    }

    /**
     * <pre>
     * ///////////// SINUSOIDAL EASING: sin(t) ///////////////
     * // sinusoidal easing out - decelerating to zero velocity
     * </pre>
     **/
    public static double easeOutSine(double t, double b, double c, double d) {
        return c * Math.sin(t/d * (Math.PI/2)) + b;
    }

    /**
     * <pre>
     * ///////////// SINUSOIDAL EASING: sin(t) ///////////////
     * // sinusoidal easing in/out - accelerating until halfway, then decelerating
     * </pre>
     **/
    public static double easeInOutSine(double t, double b, double c, double d) {
        return -c/2 * (Math.cos(Math.PI*t/d) - 1) + b;
    }



    /**
     * <pre>
     * ///////////// EXPONENTIAL EASING: 2^t /////////////////
     * // exponential easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in position, d: duration
     * </pre>
     **/
    public static double easeInExpo(double t, double b, double c, double d) {
        return (t==0) ? b : c * Math.pow(2, 10 * (t/d - 1)) + b;
    }

    /**
     * <pre>
     * ///////////// EXPONENTIAL EASING: 2^t /////////////////
     * // exponential easing out - decelerating to zero velocity
     * </pre>
     **/
    public static double easeOutExpo(double t, double b, double c, double d) {
        return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
    }

    /**
     * <pre>
     * ///////////// EXPONENTIAL EASING: 2^t /////////////////
     * // exponential easing in/out - accelerating until halfway, then decelerating
     * </pre>
     **/
    public static double easeInOutExpo(double t, double b, double c, double d) {
        if (t==0) return b;
        if (t==d) return b+c;
        if ((t/=d/2) < 1) return c/2 * Math.pow(2, 10 * (t - 1)) + b;
        return c/2 * (-Math.pow(2, -10 * --t) + 2) + b;
    }



    /**
     * <pre>
     * /////////// CIRCULAR EASING: sqrt(1-t^2) //////////////
     * // circular easing in - accelerating from zero velocity
     * // t: current time, b: beginning value, c: change in position, d: duration
     * </pre>
     **/
    public static double easeInCirc(double t, double b, double c, double d) {
        return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
    }

    /**
     * <pre>
     * /////////// CIRCULAR EASING: sqrt(1-t^2) //////////////
     * // circular easing out - decelerating to zero velocity
     * </pre>
     **/
    public static double easeOutCirc(double t, double b, double c, double d) {
        return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
    }

    /**
     * <pre>
     * /////////// CIRCULAR EASING: sqrt(1-t^2) //////////////
     * // circular easing in/out - acceleration until halfway, then deceleration
     * </pre>
     **/
    public static double easeInOutCirc(double t, double b, double c, double d) {
        if ((t/=d/2) < 1) return -c/2 * (Math.sqrt(1 - t*t) - 1) + b;
        return c/2 * (Math.sqrt(1 - (t-=2)*t) + 1) + b;
    }



    /**
     * <pre>
     * /////////// ELASTIC EASING: exponentially decaying sine wave  //////////////
     * // t: current time, b: beginning value, c: change in value, d: duration, a: amplitude (optional), p: period (optional)
     * // t and d can be in frames or seconds/milliseconds
     * </pre>
     **/

    public static double easeInElastic(double t, double b, double c, double d, double a, double p) {
        double s;
        if (Double.isNaN(a)) a = 0;
        if (t==0) return b;  if ((t/=d)==1) return b+c;  if (Double.isNaN(p)) p=d*.3;
        if (a < Math.abs(c)) { a=c; s=p/4; }
        else s = p/(2*Math.PI) * Math.asin (c/a);
        return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
    }

    /**
     * <pre>
     * /////////// ELASTIC EASING: exponentially decaying sine wave  //////////////
     * // t: current time, b: beginning value, c: change in value, d: duration, a: amplitude (optional), p: period (optional)
     * // t and d can be in frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeOutElastic(double t, double b, double c, double d, double a, double p) {
        double s;
        if (Double.isNaN(a)) a = 0;
        if (t==0) return b;  if ((t/=d)==1) return b+c;  if (Double.isNaN(p)) p=d*.3;
        if (a < Math.abs(c)) { a=c; s=p/4; }
        else s = p/(2*Math.PI) * Math.asin (c/a);
        return a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b;
    }

    /**
     * <pre>
     * /////////// ELASTIC EASING: exponentially decaying sine wave  //////////////
     * // t: current time, b: beginning value, c: change in value, d: duration, a: amplitude (optional), p: period (optional)
     * // t and d can be in frames or seconds/milliseconds
     * </pre>
     **/
    public static double easeInOutElastic(double t, double b, double c, double d, double a, double p) {
        double s;
        if (Double.isNaN(a)) a = 0;
        if (t==0) return b;  if ((t/=d/2)==2) return b+c;  if (Double.isNaN(p)) p=d*(.3*1.5);
        if (a < Math.abs(c)) { a=c; s=p/4; }
        else s = p/(2*Math.PI) * Math.asin (c/a);
        if (t < 1) return -.5*(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
        return a*Math.pow(2,-10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )*.5 + c + b;
    }


    /**
     * <pre>
     * /////////// BACK EASING: overshooting cubic easing: (s+1)*t^3 - s*t^2  //////////////
     * // back easing in - backtracking slightly, then reversing direction and moving to target
     * // t: current time, b: beginning value, c: change in value, d: duration, s: overshoot amount (optional)
     * // t and d can be in frames or seconds/milliseconds
     * // s controls the amount of overshoot: higher s means greater overshoot
     * // s has a default value of 1.70158, which produces an overshoot of 10 percent
     * // s==0 produces cubic easing with no overshoot
     * </pre>
     **/
    public static double easeInBack(double t, double b, double c, double d, double s) {
        if (Double.isNaN(s)) s = 1.70158;
        return c*(t/=d)*t*((s+1)*t - s) + b;
    }

    /**
     * <pre>
     * /////////// BACK EASING: overshooting cubic easing: (s+1)*t^3 - s*t^2  //////////////
     * // back easing out - moving towards target, overshooting it slightly, then reversing and coming back to target
     * </pre>
     **/
    public static double easeOutBack(double t, double b, double c, double d, double s) {
        if (Double.isNaN(s)) s = 1.70158;
        return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
    }

    /**
     * <pre>
     * /////////// BACK EASING: overshooting cubic easing: (s+1)*t^3 - s*t^2  //////////////
     * // back easing in/out - backtracking slightly, then reversing direction and moving to target,
     * // then overshooting target, reversing, and finally coming back to target
     * </pre>
     **/
    public static double easeInOutBack(double t, double b, double c, double d, double s) {
        if (Double.isNaN(s)) s = 1.70158; 
        if ((t/=d/2) < 1) return c/2*(t*t*(((s*=(1.525))+1)*t - s)) + b;
        return c/2*((t-=2)*t*(((s*=(1.525))+1)*t + s) + 2) + b;
    }



    /**
     * <pre>
     * /////////// BOUNCE EASING: exponentially decaying parabolic bounce  //////////////
     * // bounce easing in
     * // t: current time, b: beginning value, c: change in position, d: duration
     * </pre>
     **/
    public static double easeInBounce(double t, double b, double c, double d) {
        return c - easeOutBounce (d-t, 0, c, d) + b;
    }

    /**
     * <pre>
     * /////////// BOUNCE EASING: exponentially decaying parabolic bounce  //////////////
     * // bounce easing out
     * // t: current time, b: beginning value, c: change in position, d: duration
     * </pre>
     **/
    public static double easeOutBounce(double t, double b, double c, double d) {
        if ((t/=d) < (1/2.75)) {
                return c*(7.5625*t*t) + b;
        } else if (t < (2/2.75)) {
                return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
        } else if (t < (2.5/2.75)) {
                return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
        } else {
                return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
        }
    }

    /**
     * <pre>
     * /////////// BOUNCE EASING: exponentially decaying parabolic bounce  //////////////
     * // bounce easing in/out
     * // t: current time, b: beginning value, c: change in position, d: duration
     * </pre>
     **/
    public static double easeInOutBounce(double t, double b, double c, double d) {
        if (t < d/2) return easeInBounce (t*2, 0, c, d) * .5 + b;
        return easeOutBounce (t*2-d, 0, c, d) * .5 + c*.5 + b;
    }


}
