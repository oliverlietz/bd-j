
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

import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;


/**
 * This is a UI for editing the fake registers maintained by BDRegisterEmulator
 *
 * @see BDRegisterEmulator
 **/
public class BDRegisterEmulatorScreen extends javax.swing.JFrame {
    
    private javax.swing.JMenuBar MainMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JScrollPane registersArea;
    private JTextField[] psrFields;
    private JTextField[] gprFields;

    private BDRegisterEmulator registers;
    
    public BDRegisterEmulatorScreen() {
        registers = BDRegisterEmulator.getInstance();
        initComponents();
    }
    
    private void initComponents() {

        MainMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        closeMenuItem = new javax.swing.JMenuItem();
        registersArea = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

        fileMenu.setText("File");

        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        MainMenu.add(fileMenu);

        setJMenuBar(MainMenu);

        registersArea.setPreferredSize(new Dimension(400, 384));
        getContentPane().add(registersArea);

        Container regContainer = new Container();
        registersArea.setViewportView(regContainer);
        SpringLayout layout = new SpringLayout();
        regContainer.setLayout(layout);
        String n = SpringLayout.NORTH;
        String s = SpringLayout.SOUTH;
        String e = SpringLayout.EAST;
        String w = SpringLayout.WEST;
        int[] vals = registers.getPSRs();
        psrFields = new JTextField[vals.length];
        int rowHeight = (new JTextField()).getPreferredSize().height + 2;
        int currY = 4;
        for (int i = 0; i < vals.length; i++) {
            Component c = new JLabel("PSR  " + i);
            regContainer.add(c);
            layout.putConstraint(n, c, currY+5, n, regContainer);
            layout.putConstraint(w, c, 10, w, regContainer);
            psrFields[i] = new JTextField();
            psrFields[i].setColumns(10);
            regContainer.add(psrFields[i]);
            layout.putConstraint(n, psrFields[i], currY, n, regContainer);
            currY += rowHeight;
            final int num = i;
            psrFields[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String txt = psrFields[num].getText().trim();
                    if (txt.indexOf('(') > -1) {
                        txt = txt.substring(0, txt.indexOf('(')).trim();
                    }
                    try {
                        int val = Integer.parseInt(txt);
                        updatePSR(num, val);
                        registers.getPSRs()[num] = val;
                    } catch (NumberFormatException ex) {
                        updatePSR(num, registers.getPSRs()[num]);
                    }
                }
            });
        }
        {
            Component c = new JLabel("--------------------------------------");
            regContainer.add(c);
            layout.putConstraint(n, c, currY, n, regContainer);
            layout.putConstraint(w, c, 10, w, regContainer);
            currY += rowHeight;
        }
        vals = registers.getGPRs();
        gprFields = new JTextField[vals.length];
        Component lbl = null;
        for (int i = 0; i < vals.length; i++) {
            lbl = new JLabel("GPR  " + i);
            regContainer.add(lbl);
            layout.putConstraint(n, lbl, currY+5, n, regContainer);
            layout.putConstraint(w, lbl, 10, w, regContainer);
            gprFields[i] = new JTextField();
            gprFields[i].setColumns(10);
            regContainer.add(gprFields[i]);
            layout.putConstraint(n, gprFields[i], currY, n, regContainer);
            currY += rowHeight;
            final int num = i;
            gprFields[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String txt = gprFields[num].getText().trim();
                    if (txt.indexOf('(') > -1) {
                        txt = txt.substring(0, txt.indexOf('(')).trim();
                    }
                    try {
                        int val = Integer.parseInt(txt);
                        updateGPR(num, val);
                        registers.getGPRs()[num] = val;
                    } catch (NumberFormatException ex) {
                        updateGPR(num, registers.getGPRs()[num]);
                    }
                }
            });
        }
        int textX = lbl.getPreferredSize().width + 24;
        for (int i = 0; i < psrFields.length; i++) {
            layout.putConstraint(w, psrFields[i], textX, w, regContainer);
        }
        for (int i = 0; i < gprFields.length; i++) {
            layout.putConstraint(w, gprFields[i], textX, w, regContainer);
        }
        layout.putConstraint(s, regContainer, 10, s, 
                                gprFields[gprFields.length-1]);
        pack();

        registers.addScreen(this);

        for (int i = 0; i < psrFields.length; i++) {
            setTextValue(psrFields[i], registers.getPSRs()[i]);
        }
        for (int i = 0; i < gprFields.length; i++) {
            setTextValue(gprFields[i], registers.getGPRs()[i]);
        }
    }

    private void setTextValue(JTextField field, int val) {
        field.setText("" + val + " (0x" + Integer.toHexString(val) + ")");
    }

    void updateGPR(int reg, int value) {
         setTextValue(gprFields[reg], value);
    }

    private void updatePSR(int reg, int value) {
         setTextValue(psrFields[reg], value);
    }

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
    }
}
