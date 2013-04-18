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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;

import javax.swing.table.JTableHeader;

import javax.swing.JScrollPane;

import java.awt.Container;


public class TableHeaderHeightResizer
        extends MouseInputAdapter {

    private JTable table;
    private boolean height_active;
    private boolean width_active;
    private boolean rowSelectionAllowed;
    private int width = 0;
    private int height = 0;
    private int startY = 0;
    private int startX = 0;
    private int endY = 0;
    private int endX = 0;
    private static final int PIXELS = 5;
    private Cursor lastCursor = null;
    private static Cursor resizeRowCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
    private static Cursor resizeHeightCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
    private static Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private JTableHeader tableHeader = null;

    public TableHeaderHeightResizer(JTable table) {
        this.table = table;
        tableHeader = this.table.getTableHeader();
        tableHeader.addMouseListener(this);
        /* MouseMotionListener[] existing=tableHeader.getMouseMotionListeners();
        for (int i=0; i<existing.length; i++){
        if (existing[i] instanceof javax.swing.plaf.basic.BasicTableHeaderUI.MouseInputHandler){
        tableHeader.removeMouseMotionListener(existing[i]); //remove the default one
        System.out.println("default tableheader mouseInputHandler removed");
        }
        }*/

        tableHeader.addMouseMotionListener(this);
        rowSelectionAllowed = table.getRowSelectionAllowed();
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        if (height_active || width_active) {
            return; //do not allow resizing width and height at the same time
        }
        Point p = e.getPoint();

        if (this.isMouseOverRowMargin(p)) {
            if (this.lastCursor == null) {
                this.lastCursor = this.tableHeader.getCursor();
            }
            this.tableHeader.setCursor(resizeRowCursor);
        } else if (this.isMouseOverColumnMargin(p)) {
            if (this.lastCursor == null) {
                this.lastCursor = this.tableHeader.getCursor();
            }
            this.tableHeader.setCursor(resizeHeightCursor);
        } else {
            if (!height_active && !width_active) {
                this.tableHeader.setCursor(defaultCursor);
            }
        }

    }

    public void mousePressed(MouseEvent e) {

        Point p = e.getPoint();

        if (this.isMouseOverRowMargin(p)) {

            this.height_active = true;
            this.rowSelectionAllowed = this.table.getRowSelectionAllowed();
            this.table.setRowSelectionAllowed(false);
            startY = e.getY();

            this.height = table.getTableHeader().getHeight(); //Getting slider value
            this.width = table.getTableHeader().getWidth(); //Keeping the same width

        } else if (this.isMouseOverColumnMargin(p)) {
            this.width_active = true;
            rowSelectionAllowed = table.getRowSelectionAllowed();
            startX = e.getX();
            /*this.columnIdx=table.columnAtPoint(p);
            Rectangle rect=table.getCellRect(-1, columnIdx, true);
            this.columnWidth = rect.width;*/
            this.height = table.getTableHeader().getHeight(); //Getting slider value
            this.width = table.getTableHeader().getWidth(); //Keeping the same width

        }



    }

    public void mouseDragged(MouseEvent e) {
        if (!height_active && !width_active) {
            return;
        }
        if (height_active) {
            endY = e.getY();

            int ydistance = endY - startY;
            int newHeight = height + ydistance;

            if (newHeight <= 15) {
                return;
            }

            Dimension preferredSize = table.getTableHeader().getPreferredSize();
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

            table.getTableHeader().revalidate();
            table.getTableHeader().repaint();
        }
        if (width_active) {
            //nothing needed to be done. It is all taken cared of by the
            // MouseActionHandler in BasicTableHeaderUI
     /* endX = e.getX();
            int xdistance = endX - startX;
             */
        }
    }/* Taken from javax.swing.platf.BasicTableHeaderUI.MouseActionHandler
    public void original_mouseDragged(MouseEvent e) {
    int mouseX = e.getX();

    TableColumn resizingColumn  = tableHeader.getResizingColumn();
    TableColumn draggedColumn  = tableHeader.getDraggedColumn();

    boolean headerLeftToRight = tableHeader.getComponentOrientation().isLeftToRight();
    int mouseXOffset=startX-resizingColumn.getWidth();
    if (resizingColumn != null) {
    int oldWidth = resizingColumn.getWidth();
    int newWidth;

    if (headerLeftToRight) {
    newWidth = mouseX - mouseXOffset;
    } else  {
    newWidth = mouseXOffset - mouseX;
    }
    resizingColumn.setWidth(newWidth);

    Container container;
    if ((tableHeader.getParent() == null) ||
    ((container = tableHeader.getParent().getParent()) == null) ||
    !(container instanceof JScrollPane)) {
    return;
    }

    if (!container.getComponentOrientation().isLeftToRight() &&
    !headerLeftToRight) {
    JTable table = tableHeader.getTable();
    if (table != null) {
    JViewport viewport = ((JScrollPane)container).getViewport();
    int viewportWidth = viewport.getWidth();
    int diff = newWidth - oldWidth;
    int newHeaderWidth = table.getWidth() + diff;

    // Resize a table
    Dimension tableSize = table.getSize();
    tableSize.width += diff;
    table.setSize(tableSize);


    if ((newHeaderWidth >= viewportWidth) &&
    (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)) {
    Point p = viewport.getViewPosition();
    p.x = Math.max(0, Math.min(newHeaderWidth - viewportWidth, p.x + diff));
    viewport.setViewPosition(p);

    // Update the original X offset value.
    mouseXOffset += diff;
    }
    }
    }
    }
    else if (draggedColumn != null) {
    TableColumnModel cm = tableHeader.getColumnModel();
    int draggedDistance = mouseX - mouseXOffset;
    int direction = (draggedDistance < 0) ? -1 : 1;
    int columnIndex = viewIndexForColumn(draggedColumn);
    int newColumnIndex = columnIndex + (headerLeftToRight ? direction : -direction);
    if (0 <= newColumnIndex && newColumnIndex < cm.getColumnCount()) {
    int width = cm.getColumn(newColumnIndex).getWidth();
    if (Math.abs(draggedDistance) > (width / 2)) {
    mouseXOffset = mouseXOffset + direction * width;
    header.setDraggedDistance(draggedDistance - direction * width);
    cm.moveColumn(columnIndex, newColumnIndex);
    return;
    }
    }
    setDraggedDistance(draggedDistance, columnIndex);
    }
    } */


    public void mouseReleased(MouseEvent e) {
        if (!height_active && !width_active) {
            return;
        }

        this.height_active = false;
        this.width_active = false;
        this.table.setRowSelectionAllowed(this.rowSelectionAllowed);

    }

    private boolean isMouseOverRowMargin(Point p) {
        int row = table.rowAtPoint(p);
        int column = table.columnAtPoint(p);

        Rectangle r = table.getTableHeader().getHeaderRect(column);
        if (p.y >= r.y + r.height - PIXELS) {

            return true;
        }

        return false;

    }

    private boolean isMouseOverColumnMargin(Point p) {
        //int row = table.rowAtPoint(p);
        int column = table.columnAtPoint(p);

        Rectangle r = table.getTableHeader().getHeaderRect(column);
        //Rectangle r2 = table.getTableHeader().getBounds();
        if (p.x >= r.x + r.width - 3 &&
                p.x < r.x + r.width) {

            return true;
        }

        return false;

    }
}

