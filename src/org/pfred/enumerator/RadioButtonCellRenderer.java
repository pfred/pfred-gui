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
package org.pfred.enumerator;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;


public class RadioButtonCellRenderer
    implements TableCellRenderer {
  JRadioButton jrb_selected=new JRadioButton();
  public RadioButtonCellRenderer() {
    jrb_selected.setBackground(Color.WHITE );
  }

  /**
   * Returns the component used for drawing the cell.
   *
   * @param table the <code>JTable</code> that is asking the renderer to draw;
   *   can be <code>null</code>
   * @param value the value of the cell to be rendered. It is up to the
   *   specific renderer to interpret and draw the value. For example, if
   *   <code>value</code> is the string "true", it could be rendered as a
   *   string or it could be rendered as a check box that is checked.
   *   <code>null</code> is a valid value
   * @param isSelected true if the cell is to be rendered with the selection
   *   highlighted; otherwise false
   * @param hasFocus if true, render cell appropriately. For example, put a
   *   special border on the cell, if the cell can be edited, render in the
   *   color used to indicate editing
   * @param row the row index of the cell being drawn. When drawing the
   *   header, the value of <code>row</code> is -1
   * @param column the column index of the cell being drawn
   * @return Component
   * @todo Implement this javax.swing.table.TableCellRenderer method
   */
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    if (!(value instanceof String)) return null;
    String v=(String)value;
    if (v.equalsIgnoreCase("true")) {
      jrb_selected.setSelected(true);
    }else{
      jrb_selected.setSelected(false);
    }

    return jrb_selected;

  }
}
