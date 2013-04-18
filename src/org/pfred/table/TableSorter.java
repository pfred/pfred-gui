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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.Insets;

import org.pfred.sort.*;

import java.util.Date;

/**
 * TableSorter is a decorator for TableModels; adding sorting
 * functionality to a supplied TableModel. TableSorter does
 * not store or copy the data in its TableModel; instead it maintains
 * a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col))
 * they are passed to the underlying model after the row numbers
 * have been translated via the internal mapping array. This way,
 * the TableSorter appears to hold another copy of the table
 * with the rows in a different order.
 * <p/>
 * TableSorter registers itself as a listener to the underlying model,
 * just as the JTable itself would. Events recieved from the model
 * are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable).
 * If a change to the model has invalidated the order of TableSorter's
 * rows, a note of this is made and the sorter will resort the
 * rows the next time a value is requested.
 * <p/>
 * When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the
 * table header may be used as a complete UI for TableSorter.
 * The default renderer of the tableHeader is decorated with a renderer
 * that indicates the sorting status of each column. In addition,
 * a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Clears the sorting status of all other columns
 * and advances the sorting status of that column through three
 * values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again).
 * <li>
 * SHIFT-mouse-click: Clears the sorting status of all other columns
 * and cycles the sorting status of the column through the same
 * three values, in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except
 * that the changes to the column do not cancel the statuses of columns
 * that are already sorting - giving a way to initiate a compound
 * sort.
 * </ul>
 * <p/>
 * This is a long overdue rewrite of a class of the same name that
 * first appeared in the swing table demos in 1997.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @version 2.0 02/27/04
 */
public class TableSorter extends AbstractTableModel {

    protected CustomTableModel tableModel;
    public boolean sortTableModel = true; // this flag tells whether we should sort the underlying model as well
    public boolean isSortingTableModel = false;
    private Comparator[] comparatorsCache = null;
    /*
    private long time1 = 0;
    private long time2 = 0;
    private long time3 = 0;
     */
    public static final int DESCENDING = -1;
    public static final int NOT_SORTED = 0;
    public static final int ASCENDING = 1;
    private static Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED);
    private Row[] viewToModel;
    private int[] modelToView;
    private JTableHeader tableHeader;
    //private MouseListener mouseListener;
    private List tableHeaderMouseListeners;
    private TableModelListener tableModelListener;
    // private Map columnComparators = new HashMap();
    private List sortingColumns = new ArrayList();

    public TableSorter() {

        this.tableHeaderMouseListeners = new ArrayList();
        this.tableModelListener = new TableModelHandler();
    }

    public TableSorter(CustomTableModel tableModel) {
        this();
        setTableModel(tableModel);
    }

    public TableSorter(CustomTableModel tableModel, JTableHeader tableHeader,
            MouseListener headerMouseListener) {
        this();
//        this.mouseListener=headerMouseListener;
        setTableHeader(tableHeader);
        setTableModel(tableModel);
    }

    private void clearSortingState() {
        viewToModel = null;
        modelToView = null;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(CustomTableModel tableModel) {
        if (this.tableModel != null) {
            this.tableModel.removeTableModelListener(tableModelListener);
        }

        this.tableModel = tableModel;
        if (this.tableModel != null) {
            this.tableModel.addTableModelListener(tableModelListener);
        }

        clearSortingState();
        fireTableStructureChanged();
    }

    public JTableHeader getTableHeader() {
        return tableHeader;
    }

    /*    public void setHeaderMouseListener(MouseListener headerMouseListener){
    this.mouseListener=headerMouseListener;
    }
     */
    public void setTableHeader(JTableHeader tableHeader) {
        if (this.tableHeader != null) {

            for (int i = 0; i < tableHeaderMouseListeners.size(); i++) {
                this.tableHeader.removeMouseListener((MouseListener) tableHeaderMouseListeners.get(i));
            }
            TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
            if (defaultRenderer instanceof SortableHeaderRenderer) {
                this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).tableCellRenderer);
            }
        }
        this.tableHeader = tableHeader;
        if (this.tableHeader != null) {

            for (int i = 0; i < tableHeaderMouseListeners.size(); i++) {
                this.tableHeader.addMouseListener((MouseListener) tableHeaderMouseListeners.get(i));
            }
            this.tableHeader.setDefaultRenderer(
                    new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
        }
    }

    public boolean isSorting() {
        return sortingColumns.size() != 0;
    }

    private Directive getDirective(int column) {
        for (int i = 0; i < sortingColumns.size(); i++) {
            Directive directive = (Directive) sortingColumns.get(i);
            if (directive.column == column) {
                return directive;
            }
        }
        return EMPTY_DIRECTIVE;
    }

    public int getSortingStatus(int column) {
        return getDirective(column).direction;
    }

    private void sortingStatusChanged() {
        // System.out.println(" 1 "+System.currentTimeMillis());
        clearSortingState();
        fireTableDataChanged();
        if (tableHeader != null) {
            tableHeader.repaint();
        }

        //Simon's change now sort the underlying model as well
        if (sortTableModel) {
            isSortingTableModel = true;
            //System.out.println("2 " + System.currentTimeMillis());
            tableModel.sort(modelIndices());
            // System.out.println("3 " + System.currentTimeMillis());
            isSortingTableModel = false;

        }
    }

    public void setSortingStatus(int column, int status) {
        Directive directive = getDirective(column);
        if (directive != EMPTY_DIRECTIVE) {
            sortingColumns.remove(directive);
        }
        if (status != NOT_SORTED) {
            sortingColumns.add(new Directive(column, status));
        }
        sortingStatusChanged();
    }

    protected Icon getHeaderRendererIcon(int column, int size) {
        Directive directive = getDirective(column);
        if (directive == EMPTY_DIRECTIVE) {
            return null;
        }
        return new Arrow(directive.direction == DESCENDING, size, sortingColumns.indexOf(directive));
    }

    public void cancelSorting() {
        sortingColumns.clear();
        sortingStatusChanged();
    }

    public void cancelSorting(boolean refresh) {

        sortingColumns.clear();
        if (refresh) {
            sortingStatusChanged();
        }
    }

  
    //Vidhya modified - start
    protected Comparator getComparator(int column) {

        //first look into the cache
        if (comparatorsCache[column] != null) {
            return (Comparator) comparatorsCache[column];
        }

        //Modify this to get to correct sorting type

        Class columnType = tableModel.getColumnSortingClass(column);

        //Simon's change
        if (columnType.equals(Double.class) ||
                columnType.equals(Float.class) ||
                columnType.equals(Integer.class)) {

            NUMERIC_TEXT_COMPARATOR numericalTextComparator = new NUMERIC_TEXT_COMPARATOR();
            comparatorsCache[column] = numericalTextComparator;
            return numericalTextComparator;
        }//Simon's change end

        if (columnType.equals(String.class)) {
            LEXICAL_COMPARATOR lexicalComparator = new LEXICAL_COMPARATOR();
            comparatorsCache[column] = lexicalComparator;
            return lexicalComparator;
        }

        if (columnType.equals(Date.class)) {
            DATE_COMPARATOR dateComparator = new DATE_COMPARATOR();
            comparatorsCache[column] = dateComparator;
            return dateComparator;
        }


        if (Comparable.class.isAssignableFrom(columnType)) {
            COMPARABLE_COMAPRATOR comparableComparator = new COMPARABLE_COMAPRATOR();
            comparatorsCache[column] = comparableComparator;
            return comparableComparator;
        }

        LEXICAL_COMPARATOR lexicalComparator = new LEXICAL_COMPARATOR();
        comparatorsCache[column] = lexicalComparator;
        return lexicalComparator;
    }
    //  Vidhya modified - end

    private Row[] getViewToModel() {
        if (viewToModel == null) {
            int tableModelRowCount = tableModel.getRowCount();
            viewToModel = new Row[tableModelRowCount];
            for (int row = 0; row < tableModelRowCount; row++) {
                viewToModel[row] = new Row(row);
            }

            if (isSorting()) {
                comparatorsCache = new Comparator[getColumnCount()];
                Arrays.sort(viewToModel);//speed bottleneck
                comparatorsCache = null;
            }
        }
        return viewToModel;
    }

    public int modelIndex(int viewIndex) {
        return getViewToModel()[viewIndex].modelIndex;
    }

  
    public int[] modelIndices() {
        Row[] viewToModelRows = this.getViewToModel();
        int[] modelIndices = new int[viewToModelRows.length];
        for (int i = 0; i < viewToModel.length; i++) {
            modelIndices[i] = viewToModelRows[i].modelIndex;
        }
        return modelIndices;
    }

    public int viewIndex(int modelIndex) {
        return getModelToView()[modelIndex];
    }

    private int[] getModelToView() {
        if (modelToView == null) {
            int n = getViewToModel().length;
            modelToView = new int[n];
            for (int i = 0; i < n; i++) {
                modelToView[modelIndex(i)] = i;
            }
        }
        return modelToView;
    }

    public void addTableHeaderMouseListener(MouseListener listener) {
        tableHeaderMouseListeners.add(listener);
        if (this.tableHeader != null) {
            this.tableHeader.addMouseListener(listener);
        }
    }

    public void removeTableHeaderMouseListener(MouseListener listener) {
        tableHeaderMouseListeners.remove(listener);
        if (this.tableHeader != null) {
            this.tableHeader.removeMouseListener(listener);
        }
    }

    // TableModel interface methods
    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }

    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }

    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    public Class getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(modelIndex(row), column);
    }

    public Object getValueAt(int row, int column) {
        return tableModel.getValueAt(modelIndex(row), column);
    }

    public void setValueAt(Object aValue, int row, int column) {
        tableModel.setValueAt(aValue, modelIndex(row), column);
    }

    // Helper classes
    private class Row implements Comparable {

        private int modelIndex;

        public Row(int index) {
            this.modelIndex = index;
        }

        public int compareTo(Object o) {
            int row1 = modelIndex;
            int row2 = ((Row) o).modelIndex;

            for (Iterator it = sortingColumns.iterator(); it.hasNext();) {
                long start = System.currentTimeMillis();
                Directive directive = (Directive) it.next();
                int column = directive.column;

                Object o1 = tableModel.getValueAt(row1, column);
                Object o2 = tableModel.getValueAt(row2, column);
                //time3 = time3 + System.currentTimeMillis() - start;

                int comparison = 0;
                // Define null less than everything, except null.
                if (o1 == null && o2 == null) {
                    comparison = 0;
                } else if (o1 == null) {
                    //comparison = -1;
                    comparison = directive.direction == DESCENDING ? -1 : 1;
                } else if (o2 == null) {
                    comparison = directive.direction == DESCENDING ? 1 : -1;
                } else {
                    start = System.currentTimeMillis();
                    Comparator cmpr = getComparator(column);
                    start = System.currentTimeMillis();
                    comparison = cmpr.compare(o1, o2);
                }
                if (comparison != 0) {
                    return directive.direction == DESCENDING ? -comparison : comparison;
                }
            }
            return 0;
        }
    }

    private class TableModelHandler implements TableModelListener {

        public void tableChanged(TableModelEvent e) {

            //don't do anything as we already taken care things on our side
            //if (isSortingTableModel) return;
            if (!isSortingTableModel) {//this is not a sort generated from the table
                cancelSorting(false);
            }


            // If we're not sorting by anything, just pass the event along.
            if (!isSorting()) {
                clearSortingState();
                fireTableChanged(e);
                return;
            }

            // If the table structure has changed, cancel the sorting; the
            // sorting columns may have been either moved or deleted from
            // the model.
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                cancelSorting();
                fireTableChanged(e);
                return;
            }

            // We can map a cell event through to the view without widening
            // when the following conditions apply:
            //
            // a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
            // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
            // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
            // d) a reverse lookup will not trigger a sort (modelToView != null)
            //
            // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
            //
            // The last check, for (modelToView != null) is to see if modelToView
            // is already allocated. If we don't do this check; sorting can become
            // a performance bottleneck for applications where cells
            // change rapidly in different parts of the table. If cells
            // change alternately in the sorting column and then outside of
            // it this class can end up re-sorting on alternate cell updates -
            // which can be a performance problem for large tables. The last
            // clause avoids this problem.
            int column = e.getColumn();
            if (e.getFirstRow() == e.getLastRow() && column != TableModelEvent.ALL_COLUMNS && getSortingStatus(column) == NOT_SORTED && modelToView != null) {
                int viewIndex = getModelToView()[e.getFirstRow()];
                fireTableChanged(new TableModelEvent(TableSorter.this,
                        viewIndex, viewIndex,
                        column, e.getType()));
                return;
            }

  
            if (e.getType() == CustomTableModelEvent.CUSTOM_TYPE) {
                clearSortingState();
                fireTableChanged(e); //Simon: just refire the original event;
                return;
            }
            // Something has happened to the data that may have invalidated the row order.
            clearSortingState();
            fireTableDataChanged();

            return;
        }
    }

    private static class Arrow implements Icon {

        private boolean descending;
        private int size;
        private int priority;

        public Arrow(boolean descending, int size, int priority) {
            this.descending = descending;
            this.size = size;
            this.priority = priority;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color color = c == null ? Color.GRAY : c.getBackground();
            // In a compound sort, make each succesive triangle 20%
            // smaller than the previous one.
            int dx = (int) (size / 2 * Math.pow(0.8, priority));
            int dy = descending ? dx : -dx;
            // Align icon (roughly) with font baseline.
            y = y + 5 * size / 6 + (descending ? -dy : 0);
            int shift = descending ? 1 : -1;
            g.translate(x, y);

            // Right diagonal.
            g.setColor(color.darker());
            g.drawLine(dx / 2, dy, 0, 0);
            g.drawLine(dx / 2, dy + shift, 0, shift);

            // Left diagonal.
            g.setColor(color.brighter());
            g.drawLine(dx / 2, dy, dx, 0);
            g.drawLine(dx / 2, dy + shift, dx, shift);

            // Horizontal line.
            if (descending) {
                g.setColor(color.darker().darker());
            } else {
                g.setColor(color.brighter().brighter());
            }
            g.drawLine(dx, 0, 0, 0);

            g.setColor(color);
            g.translate(-x, -y);
        }

        public int getIconWidth() {
            return size;
        }

        public int getIconHeight() {
            return size;
        }
    }

    private class SortableHeaderRenderer implements TableCellRenderer {

        private TableCellRenderer tableCellRenderer;
        //private  StringBuffer newvalue = null;

        public SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
            this.tableCellRenderer = tableCellRenderer;
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            //find out the column width
            // int width = table.getColumnModel().getColumn(column).getWidth();
            //String multiline = multilineValue(value);
            Component c = tableCellRenderer.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setHorizontalTextPosition(JLabel.LEFT);
                l.setVerticalTextPosition(JLabel.BOTTOM);
                //l.setIconTextGap(l.getIconTextGap()+5);
                int modelColumn = table.convertColumnIndexToModel(column);
                l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
                l.setToolTipText(value.toString());


                Insets insets = l.getInsets();
                insets.left = 0;
                insets.right = 0;
                //l.setBorder(BorderFactory.createEtchedBorder());

                //FontMetrics font = l.getFontMetrics(l.getFont());
                String text = l.getText();
                StringBuffer htmltext = new StringBuffer();
                htmltext.append("<html>");
                int length = text.length();
                double preferredWidth = l.getPreferredSize().getWidth();
                double preferredHeight = l.getPreferredSize().getHeight();
                //double preferredWidth=font.stringWidth(text);
                double avgCharWidth = preferredWidth / length;
                //double width=l.getW;
                double width = table.getCellRect(row, column, true).getWidth();
                boolean hasIcon = false;
                if (l.getIcon() != null) {
                    hasIcon = true;
                }

                double height = table.getTableHeader().getHeight();
                int numRowsAllowed = (int) (height / preferredHeight);
                if (numRowsAllowed <= 0) {
                    numRowsAllowed = 1;
                }
                int charsPerRow = (int) (width / avgCharWidth) - 2;
                int numCharsAllowed = numRowsAllowed * charsPerRow - 1;
                boolean exceeded = false;
                if (length > numCharsAllowed) {
                    exceeded = true;
                    length = numCharsAllowed;
                }

                int folds = (int) ((length + 0.0) / charsPerRow + 0.99999);
                char[] chars = text.toCharArray();
                for (int i = 0; i < folds; i++) {

                    int numChars = charsPerRow;
                    if ((i + 1) * charsPerRow > length) {

                        numChars = length - i * charsPerRow;
                        if (hasIcon && exceeded) {
                            numChars = numChars - 2;
                        }
                    }
                    if (numChars <= 0) {
                        break;
                    }


                    if (i > 0) {
                        htmltext.append("<br>");
                    }
                    htmltext.append(chars, i * charsPerRow, numChars);
                }

                if (exceeded) {
                    htmltext.append("...");
                }

                htmltext.append("</html>");
                l.setText(htmltext.toString());

                //l.setSize(l.getWidth(),20);



            }
            return c;
        }
        /*
        private String multilineValue(String value){
        newvalue = new StringBuffer(value);
        for (int i=value.length(); i<0; i++)
        return newvalue;
        }*/
    }

    private static class Directive {

        private int column;
        private int direction;

        public Directive(int column, int direction) {
            this.column = column;
            this.direction = direction;
        }
    }
}
