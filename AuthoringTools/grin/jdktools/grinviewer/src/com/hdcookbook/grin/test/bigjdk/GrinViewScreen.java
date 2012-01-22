
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * This is the UI frame that holds the control screen of GrinView
 *
 * @see GrinView
 *
 *   @author     Bill Foote (http://jovial.com)
 */
public class GrinViewScreen extends javax.swing.JFrame {

    private GrinView main;
    private int[] showTextPos;
    private boolean debugDraw = false;

    private JTextField commandText;
    private JTextField fpsText;
    private JCheckBox debugDrawCB;
    private JButton nextDrawButton;
    private JButton stopButton;
    private JButton snapshotButton;
    private JButton frameButton;
    private JLabel commandLabel;
    private JLabel fpsLabel;
    private JScrollPane jScrollPane1;
    private JScrollPane showTextScrollPane;
    private JSplitPane jSplitPane1;
    private JLabel nameLabel;
    private JLabel resultLabel;
    private JTextArea showText;
    private JTree showTree;
    private JSlider fontSlider;
    private JButton registersButton;
    private JCheckBox keyUpCB;

    private BDRegisterEmulatorScreen registerEmulatorScreen;
    
    public GrinViewScreen(GrinView main, TreeNode tree) {
        this.main = main;
        initComponents(tree);
    }
    
    private void initComponents(TreeNode tree) {
        nameLabel = new JLabel();
        commandLabel = new JLabel();
        commandText = new JTextField();
        jSplitPane1 = new JSplitPane();
        showTree = new JTree(tree);
        jScrollPane1 = new JScrollPane(showTree);
        showTextScrollPane = new JScrollPane();
        showText = new JTextArea();
        showText.setEditable(false);
        showText.setFocusable(false);
        showText.setLineWrap(false);
        debugDrawCB = new JCheckBox();
        nextDrawButton = new JButton();
        stopButton = new JButton();
        snapshotButton = new JButton();
        frameButton = new JButton();
        fpsLabel = new JLabel();
        fpsText = new JTextField();
        resultLabel = new JLabel();
        fontSlider = new JSlider(1, 128, 12);
        registersButton = new JButton();
        keyUpCB = new JCheckBox();
        setTextFont(12);

        commandLabel.setText("Command:");

        commandText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandTextActionPerformed(evt);
            }
        });

        jScrollPane1.setPreferredSize(new Dimension(400, 384));

        jSplitPane1.setLeftComponent(jScrollPane1);

        showTextScrollPane.setPreferredSize(new Dimension(200, 384));
        showTextScrollPane.setViewportView(showText);

        jSplitPane1.setRightComponent(showTextScrollPane);

        debugDrawCB.setLabel("Watch drawing");
        nextDrawButton.setText("next");
        nextDrawButton.setVisible(false);
        stopButton.setText("Stop");
        snapshotButton.setText("Snapshot");
        frameButton.setText("+frame");
        registersButton.setText("Registers");
        keyUpCB.setLabel("Send Key Up");
        keyUpCB.setSelected(true);

        fpsLabel.setText("fps:  ");

        fpsText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fpsTextActionPerformed(evt);
            }
        });

        showTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                showTreeSelection(e.getPath().getPath());
            }
        });

        showTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path 
                        = showTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        showTreeDoubleClick(path.getPath());
                    }
                }
            }
        });

        debugDrawCB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                debugDrawCBItemChanged(evt);
            }
        });

        nextDrawButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextDrawButtonActionPerformed(evt);
            }
        });

        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        snapshotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapshotButtonActionPerformed(evt);
            }
        });

        frameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameButtonActionPerformed(evt);
            }
        });

        registersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registersButtonActionPerformed(evt);
            }
        });

        keyUpCB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                keyUpCBItemChanged(evt);
            }
        });


        fontSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                setTextFont(fontSlider.getModel().getValue());
            }
        });

        Container m = getContentPane();
        String n = SpringLayout.NORTH;
        String s = SpringLayout.SOUTH;
        String e = SpringLayout.EAST;
        String w = SpringLayout.WEST;

        SpringLayout layout = new SpringLayout();
        m.setLayout(layout);

        m.add(nameLabel);
        m.add(jSplitPane1);
        m.add(commandLabel);
        m.add(commandText);
        m.add(fpsLabel);
        m.add(fpsText);
        m.add(debugDrawCB);
        m.add(nextDrawButton);
        m.add(stopButton);
        m.add(snapshotButton);
        m.add(frameButton);
        m.add(resultLabel);
        m.add(fontSlider);
        m.add(registersButton);
        m.add(keyUpCB);

        layout.putConstraint(w, nameLabel, 10, w, m);
        layout.putConstraint(n, nameLabel, 5, n, m);
        layout.putConstraint(n, fontSlider, 2, n, m);
        layout.putConstraint(w, fontSlider, 15, e, nameLabel);
        layout.getConstraints(fontSlider).setWidth(
            Spring.sum(layout.getConstraints(jSplitPane1).getWidth(),
              Spring.minus(
                  Spring.sum(Spring.constant(10),
                    Spring.sum(layout.getConstraints(nameLabel).getX(),
                      Spring.sum(layout.getConstraints(nameLabel).getWidth(),
                        Spring.sum(layout.getConstraints(keyUpCB).getWidth(),
                          layout.getConstraints(registersButton).getWidth())))))));
        layout.putConstraint(e, registersButton, -10, e, m);
        layout.putConstraint(n, registersButton, 2, n, m);
        layout.putConstraint(e, keyUpCB, -10, w, registersButton);
        layout.putConstraint(n, keyUpCB, 2, n, m);
        layout.putConstraint(w, jSplitPane1, 10, w, m);
        layout.putConstraint(n, jSplitPane1, 10, s, nameLabel);
        layout.putConstraint(e, m, 10, e, jSplitPane1);
        layout.putConstraint(n, commandText, 10, s, jSplitPane1);
        layout.putConstraint(s, debugDrawCB, -5, s, commandText);
        layout.putConstraint(s, stopButton, 0, s, commandText);
        layout.putConstraint(s, snapshotButton, 0, s, commandText);
        layout.putConstraint(e, snapshotButton, -10, e, m);
        layout.putConstraint(e, stopButton, -10, w, snapshotButton);
        layout.putConstraint(e, debugDrawCB, -2, w, stopButton);
        layout.putConstraint(w, nextDrawButton, 20, w, debugDrawCB);
        layout.putConstraint(n, nextDrawButton, 0, s, debugDrawCB);
        layout.putConstraint(n, frameButton, 20, s, commandText);
        layout.putConstraint(s, m, 10, s, frameButton);
        layout.putConstraint(w, commandLabel, 10, w, m);
        layout.putConstraint(s, commandLabel, -3, s, commandText);
        layout.putConstraint(w, commandText, 5, e, commandLabel);
        layout.getConstraints(commandText).setWidth(
            Spring.sum(layout.getConstraints(jSplitPane1).getWidth(),
                Spring.minus(Spring.sum(Spring.constant(8),
                 Spring.sum(layout.getConstraints(snapshotButton).getWidth(),
                  Spring.sum(layout.getConstraints(stopButton).getWidth(),
                   Spring.sum(layout.getConstraints(debugDrawCB).getWidth(),
                    layout.getConstraints(commandText).getX())))))));
        layout.getConstraints(commandText).setHeight(
            Spring.constant(commandText.getPreferredSize().height));
        layout.putConstraint(w, fpsLabel, 10, w, m);
        layout.putConstraint(s, fpsLabel, -3, s, fpsText);
        layout.putConstraint(w, fpsText, 5, e, fpsLabel);
        layout.putConstraint(s, fpsText, -3, s, frameButton);
        layout.getConstraints(fpsText).setWidth(Spring.constant(90));
        layout.putConstraint(w, frameButton, 20, e, fpsText);
        layout.putConstraint(w, resultLabel, 20, e, frameButton);
        layout.putConstraint(s, resultLabel, 0, s, fpsLabel);
        layout.getConstraints(resultLabel).setWidth(
            Spring.sum(layout.getConstraints(jSplitPane1).getWidth(),
                Spring.minus(Spring.sum(Spring.constant(5),
                    layout.getConstraints(resultLabel).getX()))));
    }

    private void fpsTextActionPerformed(java.awt.event.ActionEvent evt) {
        commandText.setText("f " + fpsText.getText());
        runTextCommand("f " + fpsText.getText());
    }

    private void frameButtonActionPerformed(java.awt.event.ActionEvent evt) {
        main.advanceFrames(1);
        commandText.setText("+");
    }
    
    private void registersButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (registerEmulatorScreen == null) {
            registerEmulatorScreen = new BDRegisterEmulatorScreen();
        }
        registerEmulatorScreen.setVisible(true);
    }

    private void keyUpCBItemChanged(java.awt.event.ItemEvent evt) {
        main.setSendKeyUp(evt.getStateChange() == evt.SELECTED);
    }

    private void debugDrawCBItemChanged(java.awt.event.ItemEvent evt) {
        debugDraw = evt.getStateChange() == evt.SELECTED;
        main.setDebugDraw(debugDraw);
    }

    void setDebugDrawToggle(boolean value) {
        debugDrawCB.setSelected(value);
    }

    private void nextDrawButtonActionPerformed(java.awt.event.ActionEvent evt){
        forceNextDrawButtonVisible(false);
        main.userWaitingDone();
        setResultText("");
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {
        commandText.setText("f 0");
        runTextCommand("f 0");
    }

    private void setTextFont(int size) {
        showText.setFont(new java.awt.Font("Courier", Font.PLAIN, size));
    }

    private void snapshotButtonActionPerformed(java.awt.event.ActionEvent evt) {
        main.snapshot();
    }

    private void commandTextActionPerformed(java.awt.event.ActionEvent evt) {
        runTextCommand(commandText.getText());
    }
   
    //
    // When a node is selected inthe showTree
    //
    private void showTreeSelection(Object[] path) {
        int line = -1;
        for (int i = path.length - 1; i >= 0; i--) {
            line = main.getLineNumber(path[i]);
            if (line > 0) {
                break;
            }
        }
        if (line <= 0) {
            showText.select(0, 0);
        } else {
            showText.getCaret().setDot(showTextPos[line - 1]);
            showText.getCaret().moveDot(showTextPos[line]);
            showText.getCaret().setSelectionVisible(true);
            try {
                final Rectangle pos = showText.modelToView(showTextPos[line-1]);
                pos.y -= 30;
                if (pos.y < 0) {
                    pos.y = 0;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JViewport vp = showTextScrollPane.getViewport();
                        Rectangle vr = vp.getViewRect();
                        pos.x -= vr.x;
                        pos.y -= vr.y;
                        pos.height = vr.height;
                        vp.scrollRectToVisible(pos);
                        showTree.grabFocus();
                    }
                });
            } catch (javax.swing.text.BadLocationException ex) {
            }
        }
    }

    //
    // When a node is double-clicked inthe showTree
    //
    private void showTreeDoubleClick(Object[] path) {
        String result = main.invokeShowNode(path);
        if (result != null) {
            setResultText(result);
        }
    }

    private void runTextCommand(String cmd) {
        String res = main.doKeyboardCommand(cmd);
        if (res == null) {
            res = "";
        }
        setResultText(res);
    }

    public void setShowText(String[] lines) {
        showTextPos = new int[lines.length + 1];
        showTextPos[0] = 0;
        int len = 0;
        for (int i = 0; i < lines.length; ) {
            len += lines[i].length();
            len += 1;   // for the newline
            i++;
            showTextPos[i] = len;
        }
        StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < lines.length; i++) {
            buf.append(lines[i]);
            buf.append("\n");
        }
        showText.setText(buf.toString());
    }

    public void setNameText(String s) {
        nameLabel.setText(s);
    }

    public void setResultText(String s) {
        resultLabel.setText(s);
    }

    public void setFpsText(String s) {
        fpsText.setText(s);
    }

    void forceNextDrawButtonVisible(boolean visible) {
        visible = visible || debugDraw;
        nextDrawButton.setVisible(visible);
        stopButton.setVisible(!visible);
        snapshotButton.setVisible(!visible);
        frameButton.setVisible(!visible);
        commandText.setVisible(!visible);
        fpsText.setVisible(!visible);
    }


    public void setShowTree(javax.swing.tree.TreeNode tree) {
        showTree.setModel(new javax.swing.tree.DefaultTreeModel(tree));
    }

    
}
