
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

package com.hdcookbook.grin.test;

import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.hdcookbook.grin.Director;
import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.Show;
import com.hdcookbook.grin.Feature;
import com.hdcookbook.grin.features.Assembly;
import com.hdcookbook.grin.io.text.ShowParser;
import com.hdcookbook.grin.io.ShowBuilder;
import com.hdcookbook.grin.util.AssetFinder;
import com.hdcookbook.grin.util.Debug;

/**
 * This is part of the "Ryan's life" test show.  It's mostly of
 * historical interest; it still works, but some of the ways of
 * structuring and using a show are passe.
 *
 * @author Bill Foote (http://jovial.com)
 */
public abstract class RyanDirector extends Director {

    private Assembly[] commentaryIndicators;
    private Feature[] commentaryOnIndicators;
    private Feature[] commentaryOffIndicators;

    private Assembly commentaryDirector;
    private Feature[] commentaryDirectors;  // [0] is no director
    private int directorNumber;

    private boolean commentaryOn = false;
    /** 
      */
    public RyanDirector() {
    }

    protected void init() {
        String[] nm = {
                "F_commentary_menu_count_up",
                "F_commentary_menu_active",
                "F_commentary_menu_count_down"
        };

        commentaryIndicators = new Assembly[nm.length];
        commentaryOnIndicators = new Feature[nm.length];
        commentaryOffIndicators = new Feature[nm.length];

        for (int i = 0; i < nm.length; i++) {
            commentaryIndicators[i] = (Assembly) getShow().getFeature(nm[i]);
            commentaryOnIndicators[i] = commentaryIndicators[i].findPart("on");
            commentaryOffIndicators[i] =commentaryIndicators[i].findPart("off");
            if (Debug.ASSERT &&
                (commentaryIndicators[i] == null
                 || commentaryOnIndicators[i] == null
                 || commentaryOffIndicators[i] == null)) 
            {
                Debug.assertFail();
            }
        }

        commentaryDirector = 
                (Assembly) getShow().getFeature("F_commentary_director");
        if (Debug.ASSERT && commentaryDirector == null) {
            Debug.assertFail();
        }
        commentaryDirectors = new Feature[7];
        for (int i = 0; i < commentaryDirectors.length; i++) {
            commentaryDirectors[i] = commentaryDirector.findPart("director_"+i);
            if (Debug.ASSERT && commentaryDirectors[i] == null) {
                Debug.assertFail();
            }
        }
    }

    public Show createShow() {
        String showName = "ryan_show.txt";
        SEShow show = new SEShow(this);
        URL source = null;
        BufferedReader rdr = null;
        try {
            source = AssetFinder.getURL(showName);
            if (source == null) {
                throw new IOException("Can't find resource " + showName);
            }
            rdr = new BufferedReader(
                        new InputStreamReader(source.openStream(), "UTF-8"));
            ShowBuilder builder = new ShowBuilder();
            builder.setExtensionParser(new RyanExtensionParser(this));
            ShowParser p = new ShowParser(rdr, showName, show, builder);
            p.parse();
            rdr.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println();
            System.out.println(ex.getMessage());
            System.out.println();
            System.out.println("Error trying to parse " + showName);
            System.out.println("    URL:  " + source);
            Debug.assertFail();
        } finally {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (IOException ex) {
                }
            }
        }
        return show;
    }

    /**
     * Called when initialization is done, so it's OK to start
     * the video.
     **/
    abstract public void startVideo();


    /**
     * Called when the user selectes interactive or movie mode
     **/
    abstract public void setInteractiveMode(boolean on);

    /**
     * Called when the user toggles commentary with the remote control.
     * The UI state is changed to reflect the new state.  This may only
     * be done within a command.
     **/
    void toggleCommentary() {
        commentaryOn = !commentaryOn;
        setCommentaryUI();
    }

    protected void setDirectorNumber(int num) {
        directorNumber = num;
        setCommentaryUI();
    }

    abstract protected void startCommentary();


    /**
     * Called to set the state of the commentary UI to the right state,
     * depending on whether commentary is on or off.
     **/
    void setCommentaryUI() {
        for (int i = 0; i < commentaryIndicators.length; i++) {
            Feature f;
            if (commentaryOn) {
                f = commentaryOnIndicators[i];
            } else {
                f = commentaryOffIndicators[i];
            }
            commentaryIndicators[i].setCurrentFeature(f);
        }
        if (commentaryOn) {
            commentaryDirector
                .setCurrentFeature(commentaryDirectors[directorNumber]);
        } else {
            commentaryDirector.setCurrentFeature(commentaryDirectors[0]);
        }
    }
}
