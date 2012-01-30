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

/**
 * Profile browser for viewing profiled data. This is a long flat
 * class with UI code in it.
 * The UI plots duration of method execution times for each method
 * It uses:
 *      1. Prefuse display for plotting the execution times.    
 *      1. Swing ComboBox: Selection of time unit for viewing the data
 *      2. Prefuse SearchQuery: Selection of method/s of interest 
 *      3. Prefuse JRangeSlider: Selection of the time view window
 *      4. Swing Spinner: Selection of standard deviation factor for detecting
 *              execution time anamolies.
 */

import com.hdcookbook.grin.util.JsonIO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import java.text.NumberFormat;

import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Formatter;
import java.util.Locale;

import java.io.FileInputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.ToolTipControl;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TableTuple;
import prefuse.data.column.Column;
import prefuse.data.column.ColumnMetadata;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Expression;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.query.ListQueryBinding;
import prefuse.data.query.RangeQueryBinding;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.query.NumberRangeModel;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.UpdateListener;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JRangeSlider;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

public class ProfileBrowser extends JPanel implements
         ActionListener, ChangeListener {

    // the range: start and end execution time of gathered data in nano seconds.
    static double startTimeNano;
    static double endTimeNano;

    // the view range: start and end execution time of currenlty viewed data
    // in the selected time unit.
    static double UIstartTime;
    static double UIendTime;

    static double extent = 10000; //corresponds to nano secs.

    static int totalMethods = 0;
    static HashMap<String, Double> stdDeviations = new HashMap<String, Double>();
    static HashMap<String, Double> means = new HashMap<String, Double>();

    static TimeUnit currTimeUnit = TimeUnit.MICRO;
    static String METHOD_FIELD = "MethodName";
    static String THREAD_FIELD = "ThreadID";
    static JFrame frame;
    static ProfileBrowser currentView;

    static final double SDF_MIN = 1;
    static final double SDF_MAX = 50; // randomly selected
    static final double SDF_INIT = 1;
    static final double SDF_STEP = 1;
    static double sdf = 1;

    static String filename = "profile.dat";

    public static void main(String[] args) {
        if (args.length > 0) {
            filename = args[0];
        }
        UILib.setPlatformLookAndFeel();
        createFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    public static void createFrame() {
        // load the data
        Table table = null;
        try {
            //table = new DelimitedTextTableReader().readTable("/profile.txt");
            table = initFromFile(filename);
            table = condenseMethodNames(table);
            initData(table);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(1);
        }
        frame = new JFrame("h d c o o k b o o k | p r o f i l e r");
        currentView = new ProfileBrowser(table);
        frame.setContentPane(currentView);
        frame.pack();
    }

    public static Table initFromFile(String fileName) throws IOException {
        String timeField = TimeUnit.NANO.startTimeField();
        String durationField = TimeUnit.NANO.durationField();

        FileInputStream fis = new FileInputStream(fileName);
        Reader rdr = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
        Object o = JsonIO.readJSON(rdr);
        o = JsonIO.readJSON(rdr);
        int num = ((Number) o).intValue();

        Table table = new Table(num, 4);
        table.addColumn(timeField, double.class);
        table.addColumn(durationField, double.class);
        table.addColumn(THREAD_FIELD, int.class);
        table.addColumn(METHOD_FIELD, String.class);
        TableTuple tt;
        for (int i = 0; i < num; i++) {
            ProfileTiming t = new ProfileTiming();
            t.readData(rdr);
            table.setDouble(i, timeField, t.startTime);
            table.setDouble(i, timeField, t.startTime);
            table.setDouble(i, durationField, t.duration);
            table.setInt(i, THREAD_FIELD, t.threadID);
            table.setString(i, METHOD_FIELD, t.message);
        }
        System.out.println(table.getTupleCount() + 
                " data entries added to the table ...");
        return table;
    }

    /**
     * This method determines the start and end execution times,
     * mean and standard deviations of execution times for each
     * method. The unit for the resulting values is nano seconds.
     * 
     * It also initializes the viewable execution time range in the
     * current time unit.
     */
    public static void initData(Table t) {
        ColumnMetadata clmd = t.getMetadata(METHOD_FIELD);
        totalMethods = clmd.getUniqueCount();
        startTimeNano = Double.MAX_VALUE;    // start/end time of data
        endTimeNano = Double.MIN_VALUE;
        
        HashMap<String, Double> sumOfSquares = new HashMap<String, Double>();
        HashMap<String, Double> sums = new HashMap<String, Double>();
        HashMap<String, Integer> instances = new HashMap<String, Integer>();

        for (int i = 0; i < t.getTupleCount(); i++) {
            double tStart = t.getDouble(i, currTimeUnit.NANO.startTimeField()); 
            if (tStart < startTimeNano) {
                startTimeNano = tStart;
            }
            double duration = t.getDouble(i, currTimeUnit.NANO.durationField());        
            double tEnd = tStart + duration;
            if (tEnd > endTimeNano) {
                endTimeNano = tEnd;
            }
            
            String method = t.getString(i, METHOD_FIELD);
            Double d = sumOfSquares.get(method);
            double sum = (d == null) ? 0 : d.doubleValue();
            sum += Math.pow(duration, 2);
            sumOfSquares.put(method, sum);

            d = sums.get(method);
            sum = (d == null) ? 0 : d.doubleValue();
            sum += duration;
            sums.put(method, sum);

            Integer s = instances.get(method);
            int size = (s == null) ? 0 : s.intValue();
            size++;
            instances.put(method, size);
        }
        UIstartTime = toCurrentUnit(startTimeNano);
        UIendTime = toCurrentUnit(endTimeNano);

        Set<Map.Entry<String, Double>> set = sumOfSquares.entrySet();
        for (Iterator<Map.Entry<String, Double>> iter = set.iterator(); 
                iter.hasNext();) {      
           Map.Entry<String, Double> e = iter.next();
           String method = e.getKey();
           Double sumOfSq = e.getValue();
           Double avgOfSumOfSq =  sumOfSq / instances.get(method);
           Double mean = sums.get(method) / instances.get(method);
           Double sqOfAvg = Math.pow(mean, 2);
           Double stdDev = Math.sqrt((avgOfSumOfSq - sqOfAvg)); 
           stdDeviations.put(method, stdDev);
           means.put(method, mean);
        }
        System.out.println("The viewable data range is: " + startTimeNano + "-" +
                        endTimeNano + " " + currTimeUnit.NANO.dname());
    }

    /*
     * Determines if the given execution time is within the specified factor of
     * stardard deviation from the mean execution time for the given method
     */
    static boolean withinStdDev(String method, Double duration) {
        Double sd = toCurrentUnit(stdDeviations.get(method));     
        Double mean = toCurrentUnit(means.get(method));     

        // Lets only indicate longer execution times.
        return (duration > ((sd * sdf) + mean + 0.5)); 
                //|| (duration < (mean - (sd * sdf)));  
    }

    static double toCurrentUnit(double value) {
        return currTimeUnit.convert(value);
    }

    /**
     * Adds a new coloumn to the prefuse data table 
     * The new column is the method execution time in the
     * selected time unit if one does not already exists.
     */
    static Table addCurrTimeUnit(Table t) {
        String sField = TimeUnit.NANO.startTimeField();
        String newSField = currTimeUnit.startTimeField();
        String dField = TimeUnit.NANO.durationField();
        String newDField = currTimeUnit.durationField();
        
        if (t.getColumn(newSField) != null) {
            return t;
        }
        t.addColumn(newSField, double.class);
        t.addColumn(newDField, double.class);

        for (int rowIndex= 0; rowIndex < t.getTupleCount(); rowIndex++) {
            double value = t.getDouble(rowIndex, sField);       
            value = currTimeUnit.convert(value);
            t.set(rowIndex, newSField, Double.valueOf(value));
            value = t.getLong(rowIndex, dField);        
            value = currTimeUnit.convert(value);
            t.set(rowIndex, newDField, Double.valueOf(value));
        }
        return t;
    }

    // strips off the characters starting from '(' from the method names.
    // we need a better solution to visualize longer method names.
    public static Table condenseMethodNames(Table t) {
        for (int rowIndex= 0; rowIndex < t.getTupleCount(); rowIndex++) {
            String value = t.getString(rowIndex, METHOD_FIELD); 
            if (value == null) {
                System.out.println("Processing input file. Row no:" + rowIndex +
                        "  is missing the method name");
                System.exit(1);
            }
            int idx = value.indexOf('(');
            if (idx > -1) {
                value = value.substring(0, idx);
            }
            t.setString(rowIndex, METHOD_FIELD, value);
        }
        return t;
    }
    

    // ------------------------------------------------------------------------
    
    private static final String title = "h d c  p r o f i l e r";

    private int totalTime = 0;
    private JFastLabel m_total;
    private JFastLabel m_details;
    
    private Visualization m_vis;
    private String group = "data";
    private Table t;
    private VisualTable m_vt; 
    private Display m_display;

    private Rectangle2D m_dataB = new Rectangle2D.Double();
    private Rectangle2D m_xlabB = new Rectangle2D.Double();
    private Rectangle2D m_ylabB = new Rectangle2D.Double();

    public ProfileBrowser(Table t) {
        super(new BorderLayout());
        this.t = t;
        
        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data
        
        final Visualization vis = new Visualization();
        m_vis = vis;

        t = addCurrTimeUnit(t);
        m_vt = vis.addTable(group, t);
        vis.setRendererFactory(new RendererFactory() {
            AbstractShapeRenderer sr = new RectRenderer();
            Renderer arY = new AxisRenderer(Constants.RIGHT, Constants.TOP);
            Renderer arX = new AxisRenderer(Constants.LEFT, Constants.FAR_BOTTOM);
            public Renderer getRenderer(VisualItem item) {
                return item.isInGroup("ylab") ? arY :
                       item.isInGroup("xlab") ? arX : sr;
            }
        }); 

        // --------------------------------------------------------------------
        // STEP 2: create actions to process the visual data

        // set up dynamic queries, search set
        RangeQueryBinding  timelineQ = new RangeQueryBinding(m_vt,
                                         currTimeUnit.startTimeField());
        SearchQueryBinding searchQ   = new SearchQueryBinding(m_vt, METHOD_FIELD);
        
        // construct the filtering predicate
        AndPredicate filter = new AndPredicate(searchQ.getPredicate());
        filter.add(timelineQ.getPredicate());
        
        // set up the actions
        AxisLayout xaxis = new AxisLayout(group, METHOD_FIELD,
                Constants.X_AXIS, VisiblePredicate.TRUE); 
        AxisLayout yaxis = new AxisLayout(group, currTimeUnit.startTimeField(),
                Constants.Y_AXIS, VisiblePredicate.TRUE);
        yaxis.setRangeModel(timelineQ.getNumberModel());
        timelineQ.getNumberModel().setValueRange(UIstartTime, UIendTime,
                                         UIstartTime, UIendTime); 
        xaxis.setLayoutBounds(m_dataB);
        yaxis.setLayoutBounds(m_dataB);
        
        AxisLabelLayout ylabels = new AxisLabelLayout("ylab", yaxis, m_ylabB);
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(0);
        ylabels.setNumberFormat(nf);
        
        AxisLabelLayout xlabels = new AxisLabelLayout("xlab", xaxis, m_xlabB, 30);
        m_vis.putAction("xlabels", xlabels);
        
        int[] palette = ColorLib.getCoolPalette();
        DataColorAction color = new DataColorAction(group, METHOD_FIELD,
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
        ShapeAction shape = new ShapeAction(group);

        ActionList draw = new ActionList();
        draw.add(color);
        draw.add(shape);
        draw.add(xaxis);
        draw.add(yaxis);
        draw.add(ylabels);
        draw.add(xlabels);
        // If the DataColor action is set to strokecolor only then uncomment the line below 
        //draw.add(new ColorAction(group, VisualItem.FILLCOLOR, 0));
        draw.add(new RepaintAction());
        m_vis.putAction("draw", draw);

        ActionList update = new ActionList();
        update.add(new VisibilityFilter(group, filter));
        update.add(xaxis);
        update.add(yaxis);
        update.add(ylabels);
        update.add(new RepaintAction());
        m_vis.putAction("update", update);
        
        UpdateListener lstnr = new UpdateListener() {
            public void update(Object src) {
                m_vis.run("update");
            }
        };
        filter.addExpressionListener(lstnr);
        this.addComponentListener(lstnr);
        
        // --------------------------------------------------------------------
        // STEP 4: set up a display and ui components to show the visualization
        m_display = new Display(vis);
        m_display.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        m_display.setSize(800,550);
        m_display.setHighQuality(true);
        m_display.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                displayLayout();
            }
        });

        displayLayout();
        m_details = new JFastLabel(title);
        m_details.setPreferredSize(new Dimension(75,20));
        m_details.setVerticalAlignment(SwingConstants.BOTTOM);
        
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);

        // Explicit argument indices may be used to re-order output.
        formatter.format("%.0f", UIendTime);
        m_total = new JFastLabel(totalMethods +" Methods over " + sb.toString() + 
                                        " " + currTimeUnit.dname());
        m_total.setPreferredSize(new Dimension(500,20));
        m_total.setHorizontalAlignment(SwingConstants.RIGHT);
        m_total.setVerticalAlignment(SwingConstants.BOTTOM);
       
        ToolTipControl ttc = new ToolTipControl(currTimeUnit.durationField());
        Control hoverc = new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent evt) {
                if (item.isInGroup(group) ) {
                  item.setFillColor(item.getStrokeColor());
                  item.setStrokeColor(ColorLib.rgb(0,0,0));
                  item.getVisualization().repaint();
                }
            }
            public void itemExited(VisualItem item, MouseEvent evt) {
                if ( item.isInGroup(group) ) {
                  item.setFillColor(item.getEndFillColor());
                  item.setStrokeColor(item.getEndStrokeColor());
                  item.getVisualization().repaint();
                }
            }
        };
        m_display.addControlListener(ttc);
        m_display.addControlListener(hoverc);
        
        // --------------------------------------------------------------------        
        // STEP 5: launching the visualization
        
        // details
        Box infoBox = new Box(BoxLayout.X_AXIS);
        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(m_details);
        infoBox.add(Box.createHorizontalGlue());
        infoBox.add(Box.createHorizontalStrut(5));
        infoBox.add(m_total);
        infoBox.add(Box.createHorizontalStrut(5));
        
        // set up search box
        JSearchPanel searcher = searchQ.createSearchPanel();
        searcher.setLabelText("Method: ");
        searcher.setBorder(BorderFactory.createEmptyBorder(5,5,5,0));

        // set up spinner
        JSpinner jsp = createSDFSpinner();
        JLabel l = new JLabel("Deviation Factor");
        l.setLabelFor(jsp);
        
        // create dynamic queries
        Box radioBox = new Box(BoxLayout.X_AXIS);
        radioBox.add(Box.createHorizontalStrut(5));
        radioBox.add(searcher);
        radioBox.add(Box.createHorizontalGlue());
        radioBox.add(Box.createHorizontalStrut(5));
        radioBox.add(l);
        radioBox.add(jsp);
        radioBox.add(createUnitSelector());
        
        JRangeSlider slider = timelineQ.createVerticalRangeSlider();
        slider.setThumbColor(null);
        slider.setMinExtent((int)toCurrentUnit(extent)); // uncommenting this slows down the slider
        slider.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                m_display.setHighQuality(false);
            }
            public void mouseReleased(MouseEvent e) {
                m_display.setHighQuality(true);
                Object o = e.getSource();
                if (o instanceof JRangeSlider) {
                    JRangeSlider rs = (JRangeSlider) o;
                    NumberRangeModel nm = (NumberRangeModel) rs.getModel(); 
                    Double low = (Double) nm.getLowValue();
                    Double high  = (Double) nm.getHighValue();
                    UIstartTime = low.doubleValue();
                    UIendTime = high.doubleValue();
                }
                m_display.repaint();
            }
        });
        slider.addChangeListener(this);
        jsp.addChangeListener(this);
        vis.run("draw");
        vis.run("xlabels");
        
        add(infoBox, BorderLayout.NORTH);
        add(m_display, BorderLayout.CENTER);
        add(slider, BorderLayout.EAST);
        add(radioBox, BorderLayout.SOUTH);
        UILib.setColor(this, ColorLib.getColor(255,255,255), Color.GRAY);
        slider.setForeground(Color.LIGHT_GRAY);
        UILib.setFont(radioBox, FontLib.getFont("Tahoma", 15));
        m_details.setFont(FontLib.getFont("Tahoma", 18));
        m_total.setFont(FontLib.getFont("Tahoma", 16));
    }

    // Creates time unit selector box
    public JComponent createUnitSelector() {
        Vector vec = new Vector();
        for (TimeUnit tu : TimeUnit.values()) {
            vec.add(tu.dname());
        }
        JComboBox box = new JComboBox(vec);
        box.setSelectedIndex(currTimeUnit.index());
        box.addActionListener(this);
        return box;
   }

   public JSpinner createSDFSpinner() {
        SpinnerModel sdfModel = new SpinnerNumberModel(
                                        SDF_INIT, //initial value
                                        SDF_MIN,  //min
                                        SDF_MAX,  //max
                                        SDF_STEP);       //step
        
        JSpinner jsp = new JSpinner (sdfModel);
        jsp.setEditor(new JSpinner.NumberEditor(jsp, "#"));     
        jsp.setValue(Double.valueOf(sdf));
        return jsp;
   }

    /** Listens to the combo box. */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String timeUnitName = (String)cb.getSelectedItem();
        TimeUnit tu = TimeUnit.match(timeUnitName);
        if (tu != null) {
            currTimeUnit = tu;
            UIstartTime = toCurrentUnit(startTimeNano);
            UIendTime = toCurrentUnit(endTimeNano);
        }
        updateDisplay();
    }

    public void updateDisplay() {
        frame.remove(currentView);
        currentView = new ProfileBrowser(t);
        frame.setContentPane(currentView);
        frame.pack();
        //System.out.println("Display should be new!");
    }

    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof JRangeSlider) {
            JRangeSlider rs = (JRangeSlider) o;
            //UIstartTime = rs.getLowValue();
            //UIendTime = rs.getHighValue();
        } else if (o instanceof JSpinner) {
            JSpinner jsp = (JSpinner) o;
            SpinnerNumberModel model = (SpinnerNumberModel) jsp.getModel();
            sdf = model.getNumber().doubleValue();
            jsp.setValue(Double.valueOf(sdf));
            //System.out.println("sdf:" + sdf);
            updateDisplay();
        }
    }
    
    public void displayLayout() {
        Insets i = m_display.getInsets();
        int w = m_display.getWidth();
        int h = m_display.getHeight();
        int iw = i.left+i.right;
        int ih = i.top+i.bottom;
        int aw = 85;
        int ah = 30;
        
        m_dataB.setRect(i.left, i.top, w-iw-aw, h-ih-ah);
        m_xlabB.setRect(i.left, h-ah-i.bottom, w-iw-aw, ah-10);
        m_ylabB.setRect(i.left, i.top, w-iw, h-ih-ah);
        
        m_vis.run("update");
        m_vis.run("xlabels");
    }

} // end of class ProfileBrowser
