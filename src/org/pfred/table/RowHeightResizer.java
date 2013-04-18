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
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;

public class RowHeightResizer
    extends MouseInputAdapter {
  private CustomTablePanel tablePane;
  private JTable table;
  private boolean active;
  private boolean rowSelectionAllowed;
  private int row;
  private int startY;
  private int startHeight;

  private static final int PIXELS = 5;
  private Cursor lastCursor;
  private static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.
      S_RESIZE_CURSOR);

  public RowHeightResizer(CustomTablePanel tablePane) {
    this.tablePane = tablePane;
    this.table=tablePane.getTable();
    this.tablePane.getTable().addMouseListener(this);
    this.tablePane.getTable().addMouseMotionListener(this);
    this.row = -1;
  }

  public void done() {
    if (this.table == null)
      return;
    this.table.removeMouseListener(this);
    this.table.removeMouseMotionListener(this);
  }

  public void mouseMoved(MouseEvent e) {
    Point p = e.getPoint();

    if (this.isMouseOverRowMargin(p)) {
      if (this.lastCursor == null) {
        this.lastCursor = this.table.getCursor();
      }
      this.table.setCursor(resizeCursor);
    }
    else {
      this.table.setCursor(this.lastCursor);
    }
  }

  public void mousePressed(MouseEvent e) {
    Point p = e.getPoint();

    if (this.isMouseOverRowMargin(p)) {
      this.active = true;
      this.startY = p.y;
      this.startHeight = table.getRowHeight(row);
      this.rowSelectionAllowed = this.table.getRowSelectionAllowed();
      this.table.setRowSelectionAllowed(false);
    }
  }

  public void mouseDragged(MouseEvent e) {
    if (!active)
      return;

    int newHeight = startHeight + e.getY() - startY;
    newHeight = Math.max(1, newHeight);
    if (newHeight<=10||newHeight>400) return; //do nothing if it is narrow;
    if (!e.isShiftDown())
      this.tablePane.changeRowHeight(newHeight);
    else
      this.table.setRowHeight(row, newHeight);
  }

  public void mouseReleased(MouseEvent e) {
    if (!active)
      return;

    this.table.setRowSelectionAllowed(this.rowSelectionAllowed);
    this.active = false;
    this.row = -1;
  }

  private boolean isMouseOverRowMargin(Point p) {
    if (!table.isEnabled())
      return false;
    this.row = table.rowAtPoint(p);
    int column = table.columnAtPoint(p);

    if (row == -1 || column == -1)
      return false;

    Rectangle r = table.getCellRect(row, column, true);

    if (p.y >= r.y + r.height - PIXELS) {
      return true;
    }
    return false;
  }

}
