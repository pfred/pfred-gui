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

import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Component;


public class HTMLCellRenderer implements TableCellRenderer  {
  public JLabel label=new JLabel();

  public HTMLCellRenderer(){
    label.setForeground(Color.blue);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    label.setText("");

    if (value==null || !(value instanceof String)) return label;
    label.setText("<html><u>"+(String)value+"</u></html>");
    return label;
  }

}
