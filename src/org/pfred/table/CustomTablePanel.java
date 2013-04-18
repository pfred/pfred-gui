/*
 *  PFRED: A computational tool for siRNA and antisense design
 *  Copyright (C) 2011 Pfizer, Inc.
 *
 *  This file is part of the PFRED software.
 *
 *  PFRED is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.pfred.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.pfred.OligoHelper;
import org.pfred.PFREDContext;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Datum;
import org.pfred.model.Oligo;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;
import org.pfred.property.PropertyDisplayDialog;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.property.PropertyDisplayOption;
import org.pfred.util.ClipImage;
import org.pfred.util.Utils;


import org.pfred.dialog.PropertyListDialog;
import org.pfred.icon.IconLoader;



public class CustomTablePanel extends JPanel implements TableModelListener,
        ActionListener, ClipboardOwner, ListSelectionListener, MouseListener,
        TableColumnModelListener, ComponentListener {

    PFREDContext context;
    JScrollPane tableScroll = new JScrollPane();
    BorderLayout borderLayout1 = new BorderLayout();
    JTable table = new JTable();
    HashMap seq_renderers = new HashMap();
    HashMap<String, CustomCellRenderer> rna_renderers = new HashMap<String, CustomCellRenderer>();
    CustomColumnRenderer custom_renderer;
    CustomColumnRenderer id_column_renderer;
   // CurveColumnRenderer curve_renderer;
    //ColeyPlotRenderer coley_renderer;
    BarChartRenderer bar_chart_renderer;
    TableSorter sorter = null;
    CustomTableModel table_model = null;
    JPanel upperPane, buttPane;
    CustomTableListSelectionModel custom_sel_model = null;
    JLabel status_right_label = new JLabel();
    ArrayList customMenuItems = new ArrayList();
    TableColumnModel columnModelWeListenTo = null;
    int cell0Width = -1; // meaning not set...
    boolean adjustingWidths = false;
    boolean mousePressedInHeader = false;
    private static int MIN_ROW_HEIGHT = 10;
    private final static int OLIGO_COLUMN_WIDTH = 200;
    private final static int OLIGO_ROW_HEIGHT = 50;
    int rowHeight = OLIGO_ROW_HEIGHT; // meaning not set...
    JFrame parent;
    JButton configureButton;
    public final static String[] neverEditableNamePrefixes = new String[]{"PFRED_GROUP"};
    public final static String[] neverRenameProperties = new String[]{"name", "RNA_Notation", "PFRED_GROUP", "start", "end", "target_name", "parent_dna_oligo",
        "parent_sense_oligo", "parent_antisense_oilgo", "registered_sense_oligo", "registered_antisense_oligo"};
    public final static String[] neverEditableNameSuffixes = new String[]{/*"_fp",*/"_fingerprint"};
    private String mol_color_scheme = "cpk";//default. can also be atomset or mono
    private String status_prefix = "";

    public CustomTablePanel(PFREDContext context, JFrame parent, CustomTableModel model,
            CustomListSelectionModel sel_model) {


        this.context = context;
        this.table_model = model;
        this.parent = parent;
        sorter = new TableSorter(model);
        sorter.addTableHeaderMouseListener(this);
        table.setModel(sorter);
        Border currentBorder = table.getBorder();
        if (!(currentBorder instanceof LineBorder)) {
            table.setBorder(BorderFactory.createLineBorder(Color.black));
        }

        sorter.setTableHeader(table.getTableHeader());
        custom_sel_model = new CustomTableListSelectionModel(sel_model, sorter);
        table.setSelectionModel(custom_sel_model);
        custom_sel_model.addListSelectionListener(this);

        // Anna
        rowHeight = MIN_ROW_HEIGHT; // default...
        // Anna

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        //apparently table.setSelectionMode() doesn't set the selection mode in selection model
        //automatically I have to call it manually otherwise the mutiple selection event
        //won't be processed correctly from the table.
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sel_model.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        table.getModel().addTableModelListener(this);
        table.setAutoCreateColumnsFromModel(false);

        //it registers itself with the table as MouseInputListener
        RowHeightResizer rowHeightResizer = new RowHeightResizer(this);
        TableHeaderHeightResizer tableheaderHeightResizer = new TableHeaderHeightResizer(table);


        setupColumnRenderer();
        //saveRowHeight(OLIGO_ROW_HEIGHT);//set initial row height
    }

    public JTable getTable() {
        return table;
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setCellSelectionEnabled(true);
        this.add(tableScroll, BorderLayout.CENTER);
        tableScroll.getViewport().add(table, null);
        tableScroll.getViewport().setBackground(Color.white);


        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 1;

        upperPane = new JPanel();
        this.add(upperPane, BorderLayout.NORTH);
        upperPane.setLayout(gridBagLayout);

        JPanel toolPane = new JPanel();
        toolPane.setLayout(new FlowLayout(SwingConstants.LEADING));
        configureButton = new JButton("Table Menu");
        configureButton.setIcon(IconLoader.getIcon("DownArrow"));
        configureButton.setHorizontalTextPosition(JButton.LEADING);
        //configureButton.setRolloverEnabled(true);
        configureButton.setActionCommand("configure");
        configureButton.addActionListener(this);
        toolPane.add(configureButton);
        upperPane.add(toolPane);

        upperPane.add(toolPane);
        gridBagLayout.setConstraints(toolPane, c);


        JPanel statusPane = new JPanel();
        statusPane.setLayout(new FlowLayout(SwingConstants.LEFT));
        statusPane.setAlignmentY(JPanel.BOTTOM_ALIGNMENT);

        status_right_label = new JLabel("0 loaded");
        statusPane.add(status_right_label);

        c.gridwidth = 1;
        c.weightx = 1;
        gridBagLayout.setConstraints(statusPane, c);
        upperPane.add(statusPane);


        JButton btn;

        buttPane = new JPanel();
        buttPane.setLayout(new FlowLayout(SwingConstants.RIGHT));
        // these names are same as in edit menu...
        buttPane.add(btn = createButton("Hide"));
        btn.setActionCommand("hideSelected");
        btn.addActionListener(this);

        buttPane.add(btn = createButton("Show Only"));
        btn.setActionCommand("showOnlySelected");
        btn.addActionListener(this);
        buttPane.add(btn = createButton("Show All"));
        btn.setActionCommand("showAll");
        btn.addActionListener(this);

        c.gridwidth = GridBagConstraints.REMAINDER;
        gridBagLayout.setConstraints(buttPane, c);
        upperPane.add(buttPane);

    }

    public String getMolColorScheme() {
        return mol_color_scheme;
    }

    public void setMolColorScheme(String color_scheme) {
        mol_color_scheme = color_scheme;

        repaint();
    }

    public JButton createButton(String text) {
        JButton button = new JButton(text);
        Insets ins = button.getMargin();
        ins.left = 6;
        ins.right = 6;
        button.setMargin(ins);

        return button;
    }

    public String optsToString() {
        // do this here cause listening to table aint working...
        saveRowHeight();

        return "" + rowHeight + " " + cell0Width + " " + table.getTableHeader().getHeight();
    }

    public void setOptsFromString(String opts) {
        if (opts == null) {
            return;
        }

        StringTokenizer st = new StringTokenizer(opts);
        int height = -1;
        int c0Width = -1;
        int headerHeight = -1;
        try {
            // for now this is all we have...
            height = Integer.parseInt(st.nextToken().trim());
            if (st.hasMoreTokens()) {
                c0Width = Integer.parseInt(st.nextToken().trim());
            }
            if (st.hasMoreTokens()) {
                headerHeight = Integer.parseInt(st.nextToken().trim());
            }
        } catch (NumberFormatException e) {
        }

        // Anna
        if (height > 0 && height > MIN_ROW_HEIGHT) {
            changeRowHeight(height);
        }
        // Anna
        if (c0Width > 0) {
            cell0Width = c0Width;
        }

        if (headerHeight > 0) {
            setTableHeaderHeight(headerHeight);
        }
    }

    public void tableChanged(TableModelEvent e) {
        if (e.getType() == CustomTableModelEvent.CUSTOM_TYPE) {
            if (sorter != null) {
                sorter.cancelSorting(false); //clear up sort status
            }
            table.createDefaultColumnsFromModel();
            setupRowHeight();
            setupColumnWidths();
            setupColumnRenderer();
        }
        //validate();
        reloadStatusView();

    }

    public void reloadStatusView() {
        //String display_status = "0 loaded";
        if (table_model == null) {
            status_right_label.setText(status_prefix + "0 loaded");
        } else {
            status_right_label.setText(
                    status_prefix +
                    table_model.getCustomListModel().size() +
                    " loaded  " +
                    custom_sel_model.getCustomListSelectionModel().getSelectedDataCount() +
                    " selected  " + table_model.getRowCount() +
                    " visible");
        }


    }

    private boolean isRegular(int i, String name) {
        if (i == 1) //hardcoded a bit here
        {
            return false;
        }
        return true;
    }

    private boolean isEditable(int column) {
        if (column < 1) {
            return false;
        }
        return table_model.getPropertyDisplayOption(column).isEditable();
    }

    private boolean isEditNeverAllowed(String propName) {


        for (int i = 0; i < neverEditableNamePrefixes.length; i++) {
            if (propName.startsWith(neverEditableNamePrefixes[i])) {
                return true;
            }
        }

        for (int i = 0; i < neverEditableNameSuffixes.length; i++) {
            if (propName.endsWith(neverEditableNameSuffixes[i])) {
                return true;
            }
        }

        PropertyDisplayOption opt = table_model.getPropertyDisplayManager().getPropertyDisplayOption(propName);
        if (opt.isDerivedProp()) {
            return true;
        }
        return false;
    }

    private boolean isRenameNeverAllowed(String propName) {
        for (int i = 0; i < neverRenameProperties.length; i++) {
            if (propName.equalsIgnoreCase(neverRenameProperties[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isTypeChangeNeverAllowed(String propName) {
        //if (isEditNeverAllowed(propName)) return true;

        //if (isSmiles(propName)) return true;

        return false;
    }

    private void setColumnEditable(int column, boolean editable) {
        if (column < 1) {
            return;
        }
        table_model.getPropertyDisplayOption(column).setEditable(editable);
    }

    // paul: exposed this as well to make it easy to update ENTIRE table should we need to...
    public void fireTableChanged() {
        CustomTableModelEvent event =
                new CustomTableModelEvent(table_model, 0, table_model.getRowCount(),
                TableModelEvent.ALL_COLUMNS,
                CustomTableModelEvent.CUSTOM_TYPE); //use ALL_COLUMNS as a custom event type
        tableChanged(event);
        //table_model.fireTableChanged(event);
    }

    private void saveRowHeight() {
        rowHeight = table.getRowHeight(0);
    }

    // paul: made this public so applyTemplate() could call it...
    public void setupRowHeight() {
        if (rowHeight > 0) {
            table.setRowHeight(rowHeight);
        }
    }

    public void changeRowHeight(int height) {
        rowHeight = height;
        table.setRowHeight(rowHeight);
    }

    //int column = columnModel.getColumn(viewColumn).getModelIndex();
    private void saveColumnWidths() {
        // System.out.println("SAVING COLUMN WIDTHS...");

        TableModel model = table.getModel();
        TableColumnModel col_model = table.getColumnModel();

        // columns 0 and 1 are saved differently...
        TableColumn col;
        col = col_model.getColumn(0);
        this.cell0Width = col.getWidth();
        //System.out.println("col 0 width = "  + cell0Width);


        int columnCount = model.getColumnCount();


        for (int i = 1; i < columnCount; i++) {
            PropertyDisplayOption opt = table_model.getPropertyDisplayOption(i);
            if (opt == null) // this will be true for i==1
            {
                continue;
            }

            col = col_model.getColumn(i);
            int width = col.getWidth();
            opt.setCellWidth(width);
            //System.out.println("col " + i + " width = "  + width);
        }
    }

    private void setupColumnWidths() {

        adjustingWidths = true;

        TableModel model = table.getModel();
        TableColumnModel col_model = table.getColumnModel();

        // we listen to the column model...
        if (columnModelWeListenTo != null) {
            columnModelWeListenTo.removeColumnModelListener(this);
        }
        columnModelWeListenTo = col_model;
        columnModelWeListenTo.addColumnModelListener(this);


        if (cell0Width > 0) {
            col_model.getColumn(0).setPreferredWidth(cell0Width);
        }


        // only for colums 1 and on...the first two
        // column widths are stored with the row height in the opts...
        int columnCount = model.getColumnCount();
        for (int i = 1; i < columnCount; i++) {
            PropertyDisplayOption opt = table_model.getPropertyDisplayOption(i);
            if (opt == null) // this will be true for i==1
            {
                continue;
            }

            TableColumn col = col_model.getColumn(i);
            int width = opt.getCellWidth();
            if (width >= 0) {
                if (width < 10) // dont make TOO damn small...
                {
                    width = 10;
                }

                col.setPreferredWidth(width);
            }
        }

        adjustingWidths = false;
    }

    protected void setupColumnRenderer() {


        TableModel model = table.getModel();
        TableColumnModel col_model = table.getColumnModel();
        //TODO assign renderer basing on column type


        if (id_column_renderer == null) {
            id_column_renderer = new CustomColumnRenderer(table_model,
                    context.getDataStore().
                    getHighlightModel());
        }

        col_model.getColumn(0).setCellRenderer(id_column_renderer);

        // now set smiles renderer for Rgroups if there are any
        // and chart renderer for property distribution
        // and any conditional renderers setup...
        int columnCount = model.getColumnCount();
        for (int i = 1; i < columnCount; i++) {
            PropertyDisplayOption opt = table_model.getPropertyDisplayOption(i);
            if (opt == null) {
                continue;
            }

            String col_name = opt.name;

        //    System.out.println(col_name + " type=" + opt.getType() + " isNotation=" + opt.isNotation());
            if (col_name.toUpperCase().endsWith("_OLIGO") || opt.isNotation()) {

                CustomCellRenderer customCellRenderer = getRNANotationCellRenderer(col_name);
                TableCellRenderer seq_renderer = customCellRenderer.getRenderer();
                if (seq_renderer != null) {
                    col_model.getColumn(i).setCellRenderer(seq_renderer);
                }
                col_model.getColumn(i).setPreferredWidth(OLIGO_COLUMN_WIDTH);
            } else if (col_name.toUpperCase().endsWith("_BARCHART")) {
                col_model.getColumn(i).setCellRenderer(getCellRenderer(BarChartRenderer.NAME));
            } else if (col_name.toUpperCase().endsWith("RNA_NOTATION")) {
                CustomCellRenderer customCellRenderer = getRNANotationCellRenderer(col_name);
                TableCellRenderer rna_renderer = customCellRenderer.getRenderer();
                if (rna_renderer != null) {
                    col_model.getColumn(i).setCellRenderer(rna_renderer);
                }
                col_model.getColumn(i).setPreferredWidth(OLIGO_COLUMN_WIDTH);
            } else if (i != 0) {
                // all others get the CustomCellRenderer
                col_model.getColumn(i).setCellRenderer(getCellRenderer(CustomColumnRenderer.NAME));
            }
        }
    }

    public TableCellRenderer getCellRenderer(PropertyDisplayOption opt) {
        // all others get the CustomCellRenderer
        return getCellRenderer(CustomColumnRenderer.NAME);

    }

    public TableCellRenderer getCellRenderer(String name) {

        if (name.equals(CustomColumnRenderer.NAME)) {
            if (custom_renderer == null) {
                custom_renderer = new CustomColumnRenderer(table_model,
                        context.getDataStore().
                        getHighlightModel());
            }
            return custom_renderer;
        } else if (name.equals(BarChartRenderer.NAME)) {
            if (bar_chart_renderer == null) {
                bar_chart_renderer = new BarChartRenderer(table_model);
            }
            return bar_chart_renderer;
        }



        return null;
    }

    /* public void setColumnRender(int column_type, TableCellRenderer renderer){

    }*/
    public TableCellRenderer getSeqCellRenderer(String propName) {

        //do a little bit of clean up here for obsolete renderers as each one is registered with the table model
        Iterator iter = seq_renderers.keySet().iterator();
        ArrayList toBeRemoved = new ArrayList();

        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (!table_model.getPropertyDisplayManager().hasPropertyDisplayOption(key)) {
                SequenceRenderer renderer = (SequenceRenderer) seq_renderers.get(key);
                renderer.clear();
                toBeRemoved.add(key);
            }
        }

        for (int i = 0; i < toBeRemoved.size(); i++) {
            seq_renderers.remove(toBeRemoved.get(i));
        }

        ////////

        SequenceRenderer seq_renderer = (SequenceRenderer) seq_renderers.get(propName);
        if (seq_renderer == null) {
            seq_renderer = new SequenceRenderer(table_model, propName);
            seq_renderers.put(propName, seq_renderer);
            return seq_renderer;
        } else {
            return (SequenceRenderer) seq_renderers.get(propName);
        }



    }

    public CustomCellRenderer getRNANotationCellRenderer(String propName) {

        //do a little bit of clean up here for obsolete renderers as each one is registered with the table model
        Iterator iter = rna_renderers.keySet().iterator();
        ArrayList toBeRemoved = new ArrayList();

        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (!table_model.getPropertyDisplayManager().hasPropertyDisplayOption(key)) {
                CustomCellRenderer renderer = (CustomCellRenderer) rna_renderers.get(key);
                toBeRemoved.add(key);
            }
        }

        for (int i = 0; i < toBeRemoved.size(); i++) {
            rna_renderers.remove(toBeRemoved.get(i));
        }


        ////////

        CustomCellRenderer rna_renderer = (CustomCellRenderer) rna_renderers.get(propName);
        if (rna_renderer == null) {
            rna_renderer = new CustomCellRenderer();
            rna_renderers.put(propName, rna_renderer);
            return rna_renderer;
        } else {
            return (CustomCellRenderer) rna_renderers.get(propName);
        }



    }

    public void addMouseListener(MouseListener listener) {
        table.addMouseListener(listener);
    }

    public void removeMouseListener(MouseListener listener) {
        table.removeMouseListener(listener);
    }

    public void changeColumnType(int column) {
        if (column < 1 || column >= table_model.getColumnCount()) {
            return;
        }
        String[] typeNames = PropertyDisplayOption.getTypeNames();
        String currentType = table_model.getPropertyDisplayOption(column).
                getTypeName();
        String columnName = table_model.getPropertyDisplayOption(column).shortName;
        Object option = JOptionPane.showInputDialog(this, "Select column type for " + columnName,
                "Change Column Type",
                JOptionPane.OK_CANCEL_OPTION,
                null, typeNames,
                currentType);
        if (option == null) {
            return;
        }

        PropertyDisplayOption opt = table_model.getPropertyDisplayOption(column);
        if (option.equals(PropertyDisplayOption.STRING_TYPENAME)) {
            opt.type = PropertyDisplayOption.STRING;
        } else if (option.equals(PropertyDisplayOption.NUMERIC_TYPENAME)) {
            opt.type = PropertyDisplayOption.NUMERIC;
        } else if (option.equals(PropertyDisplayOption.DATE_TYPENAME)) {
            opt.type = PropertyDisplayOption.DATE;
        }
    }

    public void setColumnType(int column, int type) {
        if (column < 1 || column >= table_model.getColumnCount()) {
            return;
        }
        PropertyDisplayOption opt = table_model.getPropertyDisplayOption(column);
        opt.type = type;
    }

    public boolean getIdColumnAsHyperLink() {
        return id_column_renderer.getDisplayTextAsHyperLink();
    }

    public void setIdColumnAsHyperLink(boolean isHyperLink) {
        if (id_column_renderer != null) {
            id_column_renderer.setDisplayTextAsHyperLink(isHyperLink);
        }
    }

    public void setTableHeaderHeight(int newHeight) {

        JTableHeader tableHeader = table.getTableHeader();
        Dimension preferredSize = tableHeader.getPreferredSize();
        preferredSize.setSize(preferredSize.width, newHeight);

        /**
         * resizing column header is tricky, you have to
         * call scrollpane instead of call tableHeader.setPreferredSize()
         * table header is wrapped by the scrollpane
         */
        Container container;
        if ((tableHeader.getParent() == null) ||
                ((container = tableHeader.getParent().getParent()) == null) ||
                !(container instanceof JScrollPane)) {
            return;
        }
        JScrollPane scrollPane = (JScrollPane) container;
        scrollPane.getColumnHeader().setPreferredSize(preferredSize);

        tableHeader.revalidate();
        tableHeader.repaint();

    }

    /**
     * set the prefix for status label e.g. cmpd, cls
     * @param prefix
     */
    public void setStatusLabelPrefix(String prefix) {
        status_prefix = prefix;
    }

    public void addCustomHeaderPopupMenuItems(JMenuItem item) {
        customMenuItems.add(item);
    }

    public void addHeaderPopupMenuSeparator() {
        customMenuItems.add("JSeparator");
    }

    public void clearCustomHeaderPopupMenuItems() {
        customMenuItems.clear();
    }

    public void conditionalFormatting(int column) {
        PropertyDisplayOption opt = table_model.getPropertyDisplayOption(column);
        String name = table_model.getColumnName(column);
        if (opt == null) {
            return;
        }

        HashMap existingFormatting = getExistingConditionalFormatting(column);

        String msg = "Conditional Coloring for Column";
        if (opt.shortName != null) {
            msg += ": " + opt.shortName;
        }
        ConditionalFormatting cf = ConditionalFormattingDialog.showDialog(
                Utils.getFrameForComponent(this), msg, opt.conditionalFormatting, existingFormatting);

        if (cf != null) {
            if (cf.getNumConditions() < 1) // if empty we just set back to null
            {
                cf = null;
            }

            opt.conditionalFormatting = cf;
            table.repaint();
        }
    }

    public HashMap getExistingConditionalFormatting(int skipColumn) {
        HashMap map = new HashMap();

        for (int i = 0; i < table_model.getColumnCount(); i++) {
            if (i == skipColumn) {
                continue;
            }
            PropertyDisplayOption opt = table_model.getPropertyDisplayOption(i);
            if (opt == null) {
                continue;
            }
            if (opt.conditionalFormatting != null) {
                map.put(opt.name, opt.conditionalFormatting);
            }
        }

        return map;
    }

    public void setColumnColor(int column) {
        PropertyDisplayOption opt = table_model.getPropertyDisplayOption(column);
        if (opt == null) {
            return;
        }

        Color oldC = opt.color;
        String msg = "Choose Color for Column";
        if (opt.shortName != null) {
            msg += ": " + opt.shortName;
        }
        Color c = JColorChooser.showDialog(this, msg, oldC);
        if (c != null) {
            opt.color = c;
            table.repaint();
        }
    }

    /************** this is the ClipboardOwner interface implementation *********/
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.out.println("screenshot: lost of ownership");
    }

    public void makeScreenShot() {
        Point p = table.getTableHeader().getLocationOnScreen();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //Rectangle bound = this.getBounds();
        Rectangle bound = table.getVisibleRect();
        Rectangle headerBound = table.getTableHeader().getVisibleRect();

        bound.height += headerBound.height; //to get the header
        bound.setLocation(p);
        try {
            //BufferedImage img = new BufferedImage(bound.width, bound.height,
            //                                      BufferedImage.TYPE_BYTE_INDEXED);
            ClipImage ci = new ClipImage(new Robot().createScreenCapture(bound));
            clipboard.setContents(ci, this);
            //Graphics2D imgG = (Graphics2D) img.getGraphics();
            //table.paint(imgG);
            //clipboard.setContents(new ClipImage(img), this);
            //System.out.println("new screen shot");
        } catch (Exception ex) {
            System.err.println("Copy screenshot: Unable to copy. " + ex.getMessage());
        }

    }

    public void copyDataToClipboard(ArrayList data) {
        if (data == null || data.size() == 0) {
            return;
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //StringBuffer sb = new StringBuffer();
        int size = data.size();
        //ask to choose properties
        String[] names = table_model.getPropertyDisplayManager().getAllPropertyNames();
        PropertyListDialog selectDialog = new PropertyListDialog(parent,
                "Select Properties",
                "Please select properties to export:", names);

        selectDialog.setLocationRelativeTo(parent);
        selectDialog.setVisible(true);


        String[] selected = selectDialog.getSelections();
        if (selectDialog.isCanceled() || selected == null) {
            return;
        }

        //check if names have registered oligo
        boolean hasRegisteredOligos = false;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i] != null &&
                    (selected[i].equalsIgnoreCase(Oligo.REGISTERED_ANTISENSE_OLIGO_PROP) || selected[i].equalsIgnoreCase(Oligo.REGISTERED_SENSE_OLIGO_PROP))) {
                hasRegisteredOligos = true;
                break;
            }
        }

        boolean convertNotation2Seq = false;
        if (hasRegisteredOligos) {
            Object[] options = new Object[]{"RNA Notation", "RNA Sequence"};
            int option = JOptionPane.showOptionDialog(parent, "You selected registered oligo sequences. Which format do you want to export them to?",
                    "Export oligo", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (option == JOptionPane.NO_OPTION) {
                convertNotation2Seq = true;
            }
        }


        //make the
        StringWriter sw = new StringWriter();

        try {
            writeProperties(data, sw, selected, convertNotation2Seq);
            String dataAsString = sw.toString();
            //dataAsString = dataAsString.replace('\n',' '); // a really inefficient way to do this...
            //dataAsString = dataAsString.replace('\r',' ');
            StringSelection sel = new StringSelection(dataAsString);
            clipboard.setContents(sel, this);
        } catch (Exception ex) {
            System.err.println("Copy data to clipboard: Unable to copy. " +
                    ex.getMessage());
        }
    }

    // NOTE: the "mols" input array list EITHER contains mols OR it contains Clusters
    //       this code detects what type of object and does the appropriate thing..
    public static void writeProperties(ArrayList selected, Writer out,
            String[] propertyNames, boolean convertNotation2Seq) throws Exception {

        String delimitor = "\t";
        Datum datum = null;

        String prop = null;
        Object value = null;

        PrintWriter writer = new PrintWriter(out);

        //print the header line
        int num_fields = propertyNames.length;
        writer.print("name");
        for (int i = 0; i < num_fields; i++) {
            if (propertyNames[i].equalsIgnoreCase("name")) {
                continue;
            }
            writer.print(delimitor + propertyNames[i]);
        }
        writer.println();

        //print all the fields
        int size = selected.size();

        for (int i = 0; i < size; i++) {
            datum = (Datum) selected.get(i);
            Object obj = datum.getDisplayObject();
            StringBuffer sb = new StringBuffer();

            sb.append(datum.getName());

            for (int j = 0; j < num_fields; j++) {
                if (propertyNames[j].equalsIgnoreCase("name")) //skip name
                {
                    continue;
                }
                prop = propertyNames[j];

                value = datum.getProperty(prop);
                if (value == null) {
                    value = "";
                }

                if (OligoHelper.isOligoSeqProperty(prop) && OligoHelper.isRNANotation(value.toString()) && convertNotation2Seq) {
                    RNAPolymer polymer = new RNAPolymer(value.toString());
                    value = polymer.getSingleLetterSeq();
                }

                sb.append(delimitor);
                String stValue = value.toString();
                stValue = stValue.replace('\n', ' ');
                stValue = stValue.replace('\r', ' ');
                sb.append(stValue);
            }
            writer.println(sb.toString());
        }
        writer.flush();
        writer.close();
    }

    /************ ActionListener ************************/
    public void actionPerformed(ActionEvent e) {

        String name = e.getActionCommand();

        if (name.equals("hideSelected")) {
            hideSelection();
        } else if (name.equals("showOnlySelected")) {
            showOnly();
        } else if (name.equals("showAll")) {
            showAll();
        } else if (name.equals("configure")) {
            showConfigureMenu();
        } else if (name.equals("displayProperties")) {
            showDisplayPropertiesDialog();
        } else if (name.equals("hideColumn")) {
            JComponent comp = (JComponent) e.getSource();
            hideProperty(comp.getName());
        } else if (name.equals("deleteColumn")) {
            JComponent comp = (JComponent) e.getSource();
            deleteProperty(comp.getName());
        } else if (name.equals("renameColumn")) {
            JComponent comp = (JComponent) e.getSource();
            renameProperty(comp.getName());
        } else if (name.equals("conditionalFormatting")) {
            String s = ((Component) e.getSource()).getName();
            int column = Integer.parseInt(s);
            conditionalFormatting(column);
        } else if (name.equals("setColumnColor")) {
            String s = ((Component) e.getSource()).getName();
            int column = Integer.parseInt(s);
            setColumnColor(column);
        } else if (name.equals("enableEditing")) {
            String s = ((Component) e.getSource()).getName();
            int column = Integer.parseInt(s);
            setColumnEditable(column, true);
        } else if (name.equals("disableEditing")) {
            String s = ((Component) e.getSource()).getName();
            int column = Integer.parseInt(s);
            setColumnEditable(column, false);
        } else if (name.equals("changeType")) {
            String s = ((Component) e.getSource()).getName();
            int column = Integer.parseInt(s);
            changeColumnType(column);
            setupColumnRenderer();//render changes basing on the type
        } else if (name.equals("addBarChart")) {

            addBarChart();
        }


    }

    public void addBarChart() {


        //add bar chart
        CustomListModel list_model = table_model.getCustomListModel();

        //ask for proerties
        BarChartCreateDialog dialog = new BarChartCreateDialog(parent, "Create Bar Chart", list_model);
        String[] selectedPropNames = dialog.getSelectedPropNames();
        String[] selectedErrorNames = dialog.getSelectedErrorPropNames();
        if (selectedPropNames == null) {
            return;
        }

        //create a bogus property and find min and max for the selected properties
        String displayName = (String) JOptionPane.showInputDialog(parent, "Please enter a new column name:", "Enter name", JOptionPane.OK_CANCEL_OPTION);
        if (displayName == null) {
            return;
        }

        if (!displayName.toUpperCase().endsWith("_BARCHART")) {
            displayName = displayName + "_BarChart";
        }

        list_model.setDataIsChanging(true);
        int size = list_model.getSize();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            list_model.addProperty(displayName, "0", list_model.getDatum(i));
            Datum d = list_model.getDatum(i);
            for (int j = 0; j < selectedPropNames.length; j++) {
                String data = (String) d.getProperty(selectedPropNames[j]);
                if (data == null) {
                    continue;
                }

                String error = (String) d.getProperty(selectedErrorNames[j]);
                double v = 0;
                double e = 0;
                try {
                    v = Double.parseDouble(data);
                } catch (Exception ex) {
                    continue;
                }
                if (error != null) {
                    try {
                        e = Double.parseDouble(error);
                    } catch (Exception ex) {
                        ;
                    }
                }
                if (min > v - e) {
                    min = v - e;
                }
                if (max < v + e) {
                    max = v + e;
                }

            }
        }
        list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

        //add custom data
        PropertyDisplayOption opt = table_model.getPropertyDisplayManager().getPropertyDisplayOption(displayName);

        BarChartCustomData customData = new BarChartCustomData();
        customData.setDataPropNames(selectedPropNames);
        customData.setErrorPropNames(selectedErrorNames);

        if (min > 0) {
            min = 0;
        }
        if (max > 10 && max < 100) {
            max = 100;
        }
        customData.setLowerRange(min);
        customData.setUpperRange(max);

        opt.setCustomData(customData);
        updateUI();
    }

    public void hideSelection() {
        CustomListSelectionModel sel_model = custom_sel_model.getCustomListSelectionModel();
        CustomListModel list_model = table_model.getCustomListModel();

        ArrayList selected = sel_model.getSelectedData();
        list_model.setDataIsChanging(true);
        list_model.setVisible(selected, false);
        int[] indices = list_model.getStartEndIndexForData(selected);
        if (indices == null) {
            return;
        }
        list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_VISIBILITY_CHANGED, indices[0], indices[1]);
        custom_sel_model.clearSelection();

    }

    public void showOnly() {
        CustomListSelectionModel sel_model = custom_sel_model.getCustomListSelectionModel();
        CustomListModel list_model = table_model.getCustomListModel();
        ArrayList selected = sel_model.getSelectedData();
        if (selected.size() == 0) {
            return;
        }
        list_model.setDataIsChanging(true);
        list_model.setVisibleOnly(selected);
        int[] indices = list_model.getStartEndIndexForData(selected);
        list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_VISIBILITY_CHANGED,
                indices[0], indices[1]);

        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    public void showAll() {
        CustomListSelectionModel sel_model = custom_sel_model.getCustomListSelectionModel();
        CustomListModel list_model = table_model.getCustomListModel();

        //ArrayList selected = sel_model.getSelectedData();
        if (list_model.size() == 0) {
            return;
        }

        list_model.setDataIsChanging(true);
        list_model.setAllVisible();
        //int[] indices = list_model.;
        list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_VISIBILITY_CHANGED,
                0, list_model.size());

        sel_model.setValueIsAdjusting(true);
        ArrayList selected = sel_model.getSelectedData();
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    public void scrollToLastColumn() {
        Rectangle rect = tableScroll.getVisibleRect();

        // last row...
        JViewport vp = tableScroll.getViewport();
        Component view = vp.getView();

        // get position that is scrolled to the last column...
        int width = view.getWidth();

        // dont even back off...just do the max (as horrible kludge for any size incrase taking place in table...)
        //int x = width - rect.width;
        int x = width;

        // scroll to it...
        rect.x = x;
        vp.scrollRectToVisible(rect);

        // ok WOOPS...maybe any of those ways below woul dhave worked
        // i was scrolling the WRONG durn table!!!

        // this don't work...
        //vp.setViewPosition(new Point(x,y));

        // this don't work...
        // scroll to last column
        //JScrollBar scrollBar = tableScroll.getHorizontalScrollBar();
        //scrollBar.setValue(scrollBar.getMaximum()-scrollBar.getVisibleAmount());
    }

    public void showConfigureMenu() {
        int x = configureButton.getX();
        int y = configureButton.getY() + configureButton.getHeight();

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mi;

        popupMenu.add(mi = new JMenuItem("Display Properties..."));
        mi.setActionCommand("displayProperties");
        mi.addActionListener(this);


        addCustomPopupMenu(context, popupMenu);
        popupMenu.show(this, x, y);
    }

    public void addCustomPopupMenu(PFREDContext context, JComponent popupMenu) {
        //can be over written

        JMenuItem mi;
        popupMenu.add(new JSeparator());

        popupMenu.add(new JSeparator());

        popupMenu.add(mi = new JMenuItem("Add Activity Bar Chart ..."));
        mi.setActionCommand("addBarChart");
        mi.setName("addBarChart");
        mi.addActionListener(this);
        //mi.setVisible(false);

        //JMenu newmenu = new JMenu("Add Property");
        //popupMenu.add(newmenu);
        popupMenu.add(mi = new JMenuItem("Apply Formula ..."));
        mi.setName("newPropertyFromExp");
        mi.addActionListener(context.getUIManager().getPropertyActionHandler());


        popupMenu.add(new JSeparator());
        popupMenu.add(mi = new JMenuItem("Delete Property ..."));
        mi.setName("deleteProperties");
        mi.addActionListener(context.getUIManager().getPropertyActionHandler());

        popupMenu.add(mi = new JMenuItem("Export Properties..."));
        mi.setName("exportCompoundProperties");
        mi.addActionListener(context.getUIManager().getPropertyActionHandler());

        popupMenu.add(mi = new JMenuItem("Create Empty Property ..."));
        mi.setName("newProperty");
        mi.addActionListener(context.getUIManager().getPropertyActionHandler());

    }

    public void showDisplayPropertiesDialog() {
        PropertyDisplayDialog dialog = new PropertyDisplayDialog(parent,
                "Display Properties in Table",
                table_model.getPropertyDisplayManager());
    }

    public void hideProperty(String prop) {
        PropertyDisplayManager pdm = table_model.getPropertyDisplayManager();
        pdm.removeDisplayedOption(prop);
        pdm.displayChanged();
    }

    public void deleteProperty(String prop) {
        if (JOptionPane.showConfirmDialog(parent, "Delete Property: " + prop, "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        CustomListModel cmpd_model = context.getDataStore().getOligoListModel();

        cmpd_model.setDataIsChanging(true);
        cmpd_model.removeProperty(prop);
        cmpd_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);
    }

    public void renameProperty(String prop) {
        //rename property is tricky
        // we need to first validate the new name is valid
        // then we need to rename the display in the table and then rename the
        // actual property in the cmpd_list_model
        String newname = JOptionPane.showInputDialog(parent, "Rename Property: " + prop,
                "Confirm Rename", JOptionPane.OK_CANCEL_OPTION);
        if (newname == null || newname.trim().length() == 0) {
            return;
        }
        //TODO: check legitimate property name, check if name is already taken

        //now rename the display option
        boolean successful = table_model.displayManager.renameDisplayOption(prop, newname);
        if (!successful) {
            JOptionPane.showMessageDialog(parent, "Unable to rename. Check if new name already exists");
            return;
        }

        CustomListModel cmpd_model = context.getDataStore().getOligoListModel();
        cmpd_model.setDataIsChanging(true);
        cmpd_model.renameProperty(prop, newname);
        cmpd_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

    }

    protected void showColumnPopup(MouseEvent e, int columnIndex) {
        if (columnIndex < 1) {
            return; //no popup for the first two columns
        }
        PropertyDisplayOption opt = table_model.getPropertyDisplayOption(columnIndex);
        String propName = "";
        if (opt != null) {
            propName = opt.name;
        }

        String name = table_model.getColumnName(columnIndex);
        String indexString = Integer.toString(columnIndex);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi;

        menu.add(mi = new JMenuItem(name));
        mi.setEnabled(false);
        menu.addSeparator();

        if (columnIndex >= 1) {
            menu.add(mi = new JMenuItem("Hide ..."));
            mi.setActionCommand("hideColumn");
            mi.setName(propName); // <-- NOTE: we pass property name for this column here...
            mi.addActionListener(this);

            menu.add(mi = new JMenuItem("Delete ..."));
            mi.setActionCommand("deleteColumn");
            mi.setName(propName); // <-- NOTE: we pass property name for this column here...

            mi.addActionListener(this);
            if (!isEditNeverAllowed(propName) && !isRenameNeverAllowed(propName)) {

                menu.add(mi = new JMenuItem("Rename ..."));
                mi.setActionCommand("renameColumn");
                mi.setName(propName); // <-- NOTE: we pass property name for this column here...
                mi.addActionListener(this);
            }


            menu.addSeparator();
        }

        menu.add(mi = new JMenuItem("Conditional Coloring..."));
        mi.setActionCommand("conditionalFormatting");
        mi.setName(indexString);
        mi.addActionListener(this);
        mi.setEnabled(isRegular(columnIndex, propName));

        menu.add(mi = new JMenuItem("Set Column Color..."));
        mi.setActionCommand("setColumnColor");
        mi.setName(indexString);
        mi.addActionListener(this);
        mi.setEnabled(isRegular(columnIndex, propName));

        if (columnIndex >= 1 && !isEditNeverAllowed(propName)) {
            menu.addSeparator();

            menu.add(mi = new JMenuItem("Enable Editing"));
            mi.setActionCommand("enableEditing");
            mi.setName(indexString);
            mi.addActionListener(this);
            mi.setVisible(!isEditable(columnIndex));

            menu.add(mi = new JMenuItem("Disable Editing"));
            mi.setActionCommand("disableEditing");
            mi.setName(indexString);
            mi.addActionListener(this);
            mi.setVisible(isEditable(columnIndex));

        }

        if (columnIndex >= 1 && !isTypeChangeNeverAllowed(propName)) {
            //menu.addSeparator();

            menu.add(mi = new JMenuItem("Change Type"));
            mi.setActionCommand("changeType");
            mi.setName(indexString);
            mi.addActionListener(this);
        }

        if (customMenuItems.size() > 0) {
            menu.addSeparator();
            for (int i = 0; i < customMenuItems.size(); i++) {
                Object tmp = customMenuItems.get(i);
                if (tmp instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) tmp;
                    menu.add(item);
                    String actionCommand = item.getName();
                    item.setActionCommand(actionCommand + ":" + propName);
                    //item.getAction().putValue("ColumnName", propName);
                } else {
                    menu.addSeparator();
                }
            }
        }


        menu.show(table.getTableHeader(), e.getX(), e.getY());
    }

    /************implement the ListSelectionListener*******************/
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        reloadStatusView();
        scrollToSelectedRow();

    }

    private void scrollToSelectedRow() {
        int minSelectionIndex = custom_sel_model.getMinSelectionIndex();
        int maxSelectionIndex = custom_sel_model.getMaxSelectionIndex();
        if (minSelectionIndex == maxSelectionIndex && minSelectionIndex > -1) {
            //single selection
            Rectangle r = getTable().getCellRect(minSelectionIndex, 0, true);
            Rectangle r2 = getTable().getVisibleRect();
            r.x = r2.x;//keep current width;
            r.width = 10;
            getTable().scrollRectToVisible(r);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) {

            JTableHeader h = (JTableHeader) e.getSource();
            //cache the current selection
            ArrayList mols = custom_sel_model.getCustomListSelectionModel().getSelectedData();

            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            if (viewColumn < 0) {
                return;//it is off the header
            }
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            if (column != -1) {
                int status = sorter.getSortingStatus(column);
                if (!e.isControlDown()) {
                    sorter.cancelSorting(false); //avoid duplicated event firing
                }
                // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
                // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
                // status = status + (e.isShiftDown() ? -1 : 1);
                //status = (status + 4) % 3 - 1; // signed mod, returning {-1, 0, 1}
                status = status > 0 ? -1 : 1;  //it now alternate between -1,1
                sorter.setSortingStatus(column, status);
            }

            //reselect the records
            custom_sel_model.getCustomListSelectionModel().setValueIsAdjusting(true);
            custom_sel_model.getCustomListSelectionModel().selectData(mols, false);
            custom_sel_model.getCustomListSelectionModel().setValueIsAdjusting(false);
            return;
        }

        // code cribbed from java developers almanac 1.4 -  e967. Listening for Clicks on a Column Header in a JTable Component
        JTableHeader header = (JTableHeader) e.getSource();
        JTable table = header.getTable();
        TableColumnModel colModel = table.getColumnModel();

        // The index of the column whose header was clicked
        int vColIndex = colModel.getColumnIndexAtX(e.getX());

        // Return if not clicked on any column header
        if (vColIndex == -1) {
            return;
        }

        int mColIndex = table.convertColumnIndexToModel(vColIndex);

        showColumnPopup(e, mColIndex);

//    // Determine if mouse was clicked between column heads
//    Rectangle headerRect = header.getHeaderRect(vColIndex);
//    if (vColIndex == 0) {
//        headerRect.width -= 3;    // Hard-coded constant
//    } else {
//        headerRect.grow(-3, 0);   // Hard-coded constant
//    }
//    if (!headerRect.contains(e.getX(), e.getY())) {
//        // Mouse was clicked between column heads
//        // vColIndex is the column head closest to the click
//
//        // vLeftColIndex is the column head to the left of the click
//        int vLeftColIndex = vColIndex;
//        if (e.getX() < headerRect.x) {
//            vLeftColIndex--;
//        }
//    }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        mousePressedInHeader = true;
    }

    public void mouseReleased(MouseEvent e) {
        mousePressedInHeader = false;

        if (movingColumnOldIndex >= 0) {
            processMovedColumn();
        }
    }

    public void columnMarginChanged(ChangeEvent e) {

        // save all the column widths as ONE at least has changed...
        if (!this.adjustingWidths && !table_model.getContentsIsChanging()) {
            saveColumnWidths();
        }
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
        int i = 0;
    }

    public void columnAdded(TableColumnModelEvent e) {
    }
    protected boolean movingColumnProgrammatically = false;
    protected int movingColumnNewIndex = -1;
    protected int movingColumnOldIndex = -1;

    public void processMovedColumn() {
        // ALWAYS undo the actual move....
        movingColumnProgrammatically = true;
        table.moveColumn(movingColumnNewIndex, movingColumnOldIndex);
        movingColumnProgrammatically = false;


        if (movingColumnNewIndex > 0 && movingColumnOldIndex > 0) {
            // reorder the data...
            PropertyDisplayManager mgr = table_model.getPropertyDisplayManager();
            mgr.moveDisplayOption(movingColumnOldIndex - 1, movingColumnNewIndex - 1);
            mgr.displayChanged();
        }

        movingColumnNewIndex = movingColumnOldIndex = -1;

        // now redraw
        revalidate();
    }

    public void columnMoved(TableColumnModelEvent e) {
        // ok the idea here is that we will UNDO this move
        // and then re-order the data as if this move occured
        // prevent re-entry...it's us preventing a column change we don't like
        if (movingColumnProgrammatically) {
            return;
        }

        int oldIndex = e.getFromIndex();
        int newIndex = e.getToIndex();
        if (oldIndex == newIndex) {
            return;
        }

        // we undo the move by checking for it in mouse release...
        if (movingColumnOldIndex < 0) {
            movingColumnOldIndex = oldIndex;
        }
        movingColumnNewIndex = newIndex;


    }

    public void columnRemoved(TableColumnModelEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        saveRowHeight(); // this is not being called when row height changes...we do it from optToString()
    }

    public void componentShown(ComponentEvent e) {
    }
}
