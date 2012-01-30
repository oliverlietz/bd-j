
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

package com.hdcookbook.grin.io.builders;

import com.hdcookbook.grin.commands.Command;
import com.hdcookbook.grin.input.SEVisualRCHandler;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * A helper class for creating a VisualRCHandler object.
 * <p>
 * To use this, create an instance, populate it, then call 
 * getFinishedHandler().
 *
 * @author Bill Foote (http://jovial.com)
 */
public class VisualRCHandlerHelper {

    private String handlerName;
    private boolean startSelected = false;
    private ArrayList<String> gridAlternateNames = new ArrayList<String>();
    private ArrayList<ArrayList<ArrayList<VisualRCHandlerCell>>> grids
        = new ArrayList<ArrayList<ArrayList<VisualRCHandlerCell>>>();
    private Map<String, Integer> states = new HashMap<String, Integer>();
        // Maps state name to number, counting from 0
    private ArrayList<Map<String, VisualRCHandlerCell>> stateToCell
            = new ArrayList<Map<String, VisualRCHandlerCell>>();
        // Maps state name to the defining cell for each grid
    private ArrayList<Map<String, String>> rcOverrides 
            = new ArrayList<Map<String, String>>();
        // See addRCOverrides
    private Command[][] selectCommands;
    private Command[][] activateCommands;
    private Rectangle[] mouseRects = null;
    private int[] mouseRectStates = null;
    private int timeout;
    private Command[] timeoutCommands;


    public VisualRCHandlerHelper() {
    }

    /**
     * Sets the handler's name
     **/
    public void setHandlerName(String name) {
        handlerName = name;
    }

    /**
     * Sets the startSelected flag
     **/
    public void setStartSelected(boolean b) {
        startSelected = b;
    }

    /**
     * Adds an RC grid.
     *
     * @return null     if all goes well, or an error message if there's
     *                  a problem.
     **/
    public String addGrid(ArrayList<ArrayList<VisualRCHandlerCell>> grid) {
        this.grids.add(grid);
        if (grid.size() == 0) {
            return "Empty grid";
        }

        int columns = grid.get(0).size();
        if (columns == 0) {
            return "Empty grid";
        }
        for (int y = 1; y < grid.size(); y++) {
            if (grid.get(y).size() != columns) {
                return "Grid row " + y 
                        + " (counting from 0) has a different length.";
            }
        }

            // Set up the cells with us (the handler) and the x,y pos
        for (int y = 0; y < grid.size(); y++) {
            List<VisualRCHandlerCell> row = grid.get(y);
            for (int x = 0; x < row.size(); x++) {
                VisualRCHandlerCell cell = row.get(x);
                int alternate = this.grids.size() - 1;
                cell.setHelper(this, alternate);
                cell.setXY(x, y);
            }
        }

            // For each cell, populate the states map

        Map<String, VisualRCHandlerCell> newStateToCell 
                = new HashMap<String, VisualRCHandlerCell>();
        stateToCell.add(newStateToCell);
        HashSet<String> dupCheck = new HashSet<String>();
        for (int y = 0; y < grid.size(); y++) {
            for (int x = 0; x < columns; x++) {
                VisualRCHandlerCell cell = grid.get(y).get(x);
                String msg = cell.addState(states, dupCheck, newStateToCell);
                if (msg != null) {
                    return msg;
                }
            }
        }

            // Make sure that a cell doesn't
            // refer to a cell that itself refers to a cell.

        for (int y = 0; y < grid.size(); y++) {
            for (int x = 0; x < columns; x++) {
                VisualRCHandlerCell cell = grid.get(y).get(x);
                VisualRCHandlerCell to = cell.getRefersTo();
                if (to != null && to.getRefersTo() != null) {
                    return "Grid refers to cell that refers to cell at x,y "
                           + x + ", " + y + " (counting from 0)";
                }
            }
        }

            // Now check all the cells
        for (int y = 0; y < grid.size(); y++) {
            for (int x = 0; x < columns; x++) {
                VisualRCHandlerCell cell = grid.get(y).get(x);
                String msg = cell.check();
                if (msg != null) {
                    return msg;
                }
            }
        }

        return null;    // null means "no error to report"
    }

    public void addGridAlternateName(String name) {
        gridAlternateNames.add(name);
    }

    public ArrayList<ArrayList<VisualRCHandlerCell>> getGrid(int i) {
        return grids.get(i);
    }

    /**
     * Sets the RC override list.  The maps Maps a key of the form 
     * "direction:from state name" to a state name.  direction is 
     * "up", "down", "right" or "left".  When in the state is
     * "from state name", the given direction will move to the 
     * second state.
     * <p>
     * The value of the map is the state to go to, or the special string
     * "&lt;activate>".
     **/
    public void addRCOverrides(Map<String, String> rcOverrides) {
        this.rcOverrides.add(rcOverrides);
    }

    public Map<String, String> getRCOverrides(int i) {
        return rcOverrides.get(i);
    }

    public void setSelectCommands(Command[][] commands) {
        selectCommands = commands;
    }

    public void setActivateCommands(Command[][] commands) {
        activateCommands = commands;
    }

    public void setMouseRects(Rectangle[] rects) {
        mouseRects = rects;
    }

    public void setMouseRectStates(int[] states) {
        mouseRectStates = states;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setTimeoutCommands(Command[] commands) {
        timeoutCommands = commands;
    }

    /**
     * @return The map from state name to index in array of states
     **/
    public Map<String, Integer> getStates() {
        return states;
    }

    Map<String, VisualRCHandlerCell> getStateToCell(int i) {
        return stateToCell.get(i);
    }

    /**
     * @return The state number referred to by the given cell, or -1 if 
     *         that cell isn't a state or doesn't refer to one.
     **/
    public int getState(int alternate, int column, int row) {
        String name = grids.get(alternate).get(row).get(column).getState();
        if (name == null) {
            return -1;
        }
        return states.get(name).intValue();
    }

    /**
     * @throws IOException if there's an inconsistency in the handler
     **/
    public SEVisualRCHandler getFinishedHandler() throws IOException {
        int[][] upDownAlternates = new int[grids.size()][];
        int[][] rightLeftAlternates = new int[grids.size()][];
        String[] gridNames = gridAlternateNames.toArray(
                                new String[gridAlternateNames.size()]);
        String[] stateNames = new String[states.size()];
        for (Map.Entry<String, Integer> entry : states.entrySet()) {
            int stateNum = entry.getValue().intValue();
            stateNames[stateNum] = entry.getKey();
        }
        for (int i = 0; i < grids.size(); i++) {
            int[] upDown = new int[states.size()];
            upDownAlternates[i] = upDown;
            int[] rightLeft = new int[states.size()];
            rightLeftAlternates[i] = rightLeft;
            for (Map.Entry<String, Integer> entry : states.entrySet()) {
                int stateNum = entry.getValue().intValue();
                VisualRCHandlerCell cell 
                        = stateToCell.get(i).get(entry.getKey());
                if (cell != null) {
                    upDown[stateNum] = cell.getUpDown();
                    rightLeft[stateNum] = cell.getRightLeft();
                }
            }
        }
        SEVisualRCHandler result
            = new SEVisualRCHandler(handlerName,  gridNames,
                                    stateNames, 
                                    upDownAlternates, rightLeftAlternates,
                                    selectCommands, activateCommands,
                                    mouseRects, mouseRectStates,
                                    timeout, timeoutCommands, startSelected, 
                                    this);
        return result;
    }
}
