
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

import com.hdcookbook.grin.SEShow;
import com.hdcookbook.grin.input.RCHandler;
import com.hdcookbook.grin.input.VisualRCHandler;
import com.hdcookbook.grin.input.RCKeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.IOException;


/**
 * A cell within a VisualRCHandler.  This class is used only for
 * VisualRCHandlerHelper.  This class has no public constructor; instead,
 * it has different factory methods for the different cell types.
 *
 * @author Bill Foote (http://jovial.com)
 * @see VisualRCHandlerHelper
 */
public abstract class VisualRCHandlerCell {

    protected VisualRCHandlerHelper helper;
    protected int alternate;

    private int xCoord;
    private int yCoord;

    // No public constructor
    private VisualRCHandlerCell() {
    }

    void setHelper(VisualRCHandlerHelper helper, int alternate) {
        this.helper = helper;
        this.alternate = alternate;
    }

    void setXY(int x, int y) {
        this.xCoord = x;
        this.yCoord = y;
    }

    /**
     * Get the cell that this cell refers to.  Returns null if this
     * cell doesn't refer to another cell, e.g. because it's a state
     * or an <activate>.
     **/
    abstract public VisualRCHandlerCell getRefersTo();

    /**
     * Add the state referred to by this cell to the map of states.
     * Calling this method multiple times on the same cell has no effect.
     *
     * @return null if OK, or an error message if not
     **/
    abstract public String addState(Map<String, Integer> stateMap,
                                    Set<String> dupCheck,
                                    Map<String, VisualRCHandlerCell> cellMap);

    /**
     * Get the name of the state of this cell, or that this cell refers
     * to.  Returns a synthetic value for activate, wall or null cells.
     **/
    abstract public String getState();

    /**
     * Get the integer number of this state within the states map when
     * navigating to this cell from the numbered state.
     *
     * @throws IOException if there's an error in the grid construction
     **/
    abstract public int 
    getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException;
    /** 
     * Check that this cell is OK.  This is called after the cell is
     * completely initialized.
     *
     * @return null if OK, or an error message if not
     **/
    abstract public String check();

    /**
     * Get the value for the upDown parameter of VisualRCHandler.  This has
     * the state to go to on up encoded in the first 16 bits, and the
     * state to go to on down encoded in the last 16 bits.
     *
     * @throws IOException if there's an inconsistency in the handler
     **/
    public int getUpDown() throws IOException {
        String state = getState();
        int up = getStateFor("up:" + state, xCoord, yCoord-1, state);
        int down = getStateFor("down:" + state, xCoord, yCoord+1, state);
        return (up << 16) | down;
    }

    /**
     * Get the value for the rightLeft parameter of VisualRCHandler.  This has
     * the state to go to on right encoded in the first 16 bits, and the
     * state to go to on left encoded in the last 16 bits.  This can only be
     * called on cells that map to a state.
     *
     * @throws IOException if there's an inconsistency in the handler
     **/
    public int getRightLeft() throws IOException {
        String state = getState();
        int right = getStateFor("right:" + state, xCoord+1, yCoord, state);
        int left = getStateFor("left:" + state, xCoord-1, yCoord, state);
        return (right << 16) | left;
    }

    private int getStateFor(String overrideKey, int x, int y, String stateFrom) 
                throws IOException
    {
        Map<String, String> overrides = helper.getRCOverrides(alternate);
        ArrayList<ArrayList<VisualRCHandlerCell>> grid 
                = helper.getGrid(alternate);
        Map<String, Integer> states = helper.getStates();

        String state = overrides.get(overrideKey);
        if (state != null) {
            if ("<activate>".equals(state)) {
                return VisualRCHandler.GRID_ACTIVATE;
            } else {
                return states.get(state).intValue();
            }
        }

        int columns = grid.get(0).size();
        if (x < 0) {
            x = 0;
        }
        if (x >= columns) {
            x = columns - 1;
        }
        if (y < 0) {
            y = 0;
        }
        if (y >= grid.size()) {
            y = grid.size() - 1;
        }
        VisualRCHandlerCell cell = grid.get(y).get(x);
        return cell.getStateNumber(states, stateFrom);
    }

    /**
     * Create a cell that represents a new state.  Only one cell that
     * defines a state can be present in the handler's grid.
     **/
    public static VisualRCHandlerCell newState(String state) {
        return new StateCell(state);
    }

    /**
     * Create a cell that refers to a state that exists somewhere else in
     * the grid.  When this cell is navigated to, the position in the
     * grid will shift to wherever the referred-to state is.
     **/
    public static VisualRCHandlerCell newStateRef(String state) {
        return new StateRefCell(state);
    }

    /**
     * Create a cell that refers to a state somewhere else in the grid,
     * where that state is specified by an x,y position in the grid
     * (countin from 0).  When this cell is navigated to, the position
     * in the grid will shift to wherever the referred-to state is.
     *
     * @return  a new cell, or null if there's a problem (like a range error)
     **/
    public static VisualRCHandlerCell newLocationRef(int x, int y) 
    {
        return new LocationRefCell(x, y);
    }

    /** 
     * Create a cell that causes an activate.  When this cell is navigated
     * to, the state of the grid remains the same (and stays on whatever
     * cell was previously active), but an activate of that cell is
     * triggered.  Thus, navigating to such a cell is equivalent to
     * pressing the ENTER/OK button.
     **/
    public static VisualRCHandlerCell newActivate() {
        return new ActivateCell();
    }

    /**
     * Create a cell that acts as a "wall".  When you navigate to a wall
     * cell using the arrow keys, you "bounce off", that is, you stay
     * in whatever state you were in before.
     **/
    public static VisualRCHandlerCell newWall() {
        return new WallCell();
    }

    /**
     * Create a null cell.  If you can navigate to a null cell, then
     * it's an error, and compilation fails.
     **/
    public static VisualRCHandlerCell newNull() {
        return new NullCell();
    }

    public static class StateCell extends VisualRCHandlerCell {
        private String name;
        private boolean added = false;
        StateCell(String name) {
            this.name = name;
        }

        public VisualRCHandlerCell getRefersTo() {
            return null;
        }

        public String addState(Map<String, Integer> stateMap,
                               Set<String> dupCheck,
                               Map<String, VisualRCHandlerCell> cellMap) 
        {
            if (added) {
                return null;
            }
            added = true;
            if (dupCheck.contains(name)) {
                return "Duplicate state \"" + name + "\".";
            }
            dupCheck.add(name);
            if (stateMap.get(name) == null) {
                stateMap.put(name, new Integer(stateMap.size()));
            }
            cellMap.put(name, this);
            return null;
        }

        public String getState() {
            return name;
        }
    
        public int getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException
        {
            return states.get(name).intValue();
        }

        public String check() {
            return null;
        }
    }

    public static class StateRefCell extends VisualRCHandlerCell {
        private String name;
        StateRefCell(String name) {
            this.name = name;
        }

        public VisualRCHandlerCell getRefersTo() {
            VisualRCHandlerCell result 
                = helper.getStateToCell(alternate).get(name);
            assert result != null;
            return result;
        }

        public String addState(Map<String, Integer> stateMap,
                               Set<String> dupCheck,
                               Map<String, VisualRCHandlerCell> cellMap) 
        {
            // do nothing
            return null;
        }

        public String getName() {
            return name;
        }

        public String getState() {
            return getRefersTo().getState();
        }
    
        public int getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException
        {
            return getRefersTo().getStateNumber(states, stateFrom);
        }

        public String check() {
            VisualRCHandlerCell result 
                = helper.getStateToCell(alternate).get(name);
            if (result == null) {
                return "State \"" + name + "\" not found";
            } else {
                return null;
            }
        }
    }

    public static class LocationRefCell extends VisualRCHandlerCell {
        private int x;
        private int y;
        LocationRefCell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public VisualRCHandlerCell getRefersTo() {
            VisualRCHandlerCell result 
                = helper.getGrid(alternate).get(y).get(x);
            assert result != null;
            return result;
        }

        public String addState(Map<String, Integer> stateMap,
                               Set<String> dupCheck,
                               Map<String, VisualRCHandlerCell> cellMap) 
        {
            // do nothing
            return null;
        }

        public String getState() {
            return getRefersTo().getState();
        }
    
        public int getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException
        {
            return getRefersTo().getStateNumber(states, stateFrom);
        }

        public String check() {
            ArrayList<ArrayList<VisualRCHandlerCell>> grid 
                = helper.getGrid(alternate);
            if (x < 0 || x >= grid.get(0).size() || y < 0 || y >= grid.size()) {
                return "" + x + ", " + y + " is an illegal cell.  "
                       + "Cell numbers count from zero.";
            }
            return null;
        }
    }

    public static class ActivateCell extends VisualRCHandlerCell {
        ActivateCell() {
        }

        public VisualRCHandlerCell getRefersTo() {
            return null;
        }

        public String addState(Map<String, Integer> stateMap,
                               Set<String> dupCheck,
                               Map<String, VisualRCHandlerCell> cellMap) {
            // do nothing
            return null;
        }

        public String getState() {
            return "<activate>";
        }
    
        public int getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException
        {
            return VisualRCHandler.GRID_ACTIVATE;
        }

        public String check() {
            return null;
        }
    }

    public static class WallCell extends VisualRCHandlerCell {
        WallCell() {
        }

        public VisualRCHandlerCell getRefersTo() {
            return null;
        }

        public String addState(Map<String, Integer> stateMap,
                               Set<String> dupCheck,
                               Map<String, VisualRCHandlerCell> cellMap) {
            // do nothing
            return null;
        }

        public String getState() {
            return "<wall>";
        }
    
        public int getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException
        {
            return states.get(stateFrom).intValue();
        }

        public String check() {
            return null;
        }
    }

    public static class NullCell extends VisualRCHandlerCell {
        NullCell() {
        }

        public VisualRCHandlerCell getRefersTo() {
            return null;
        }

        public String addState(Map<String, Integer> stateMap,
                               Set<String> dupCheck,
                               Map<String, VisualRCHandlerCell> cellMap) {
            // do nothing
            return null;
        }

        public String getState() {
            return "<null>";
        }
    
        public int getStateNumber(Map<String, Integer> states, String stateFrom)
                throws IOException
        {
            throw new IOException(
                "Illegal grid:  <null> cell can be navigated to");
        }

        public String check() {
            return null;
        }
    }
}
