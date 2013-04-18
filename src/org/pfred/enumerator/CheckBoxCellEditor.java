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

import javax.swing.*;


import java.awt.*;
import javax.swing.table.*;
import java.awt.event.*;

public class CheckBoxCellEditor
    extends AbstractCellEditor implements TableCellEditor, ActionListener{
  JCheckBox jcb_selected=new JCheckBox();
  JTable table=null;
  int rowIndex=0;
  int colIndex=0;

  public CheckBoxCellEditor() {
    jcb_selected.addActionListener(this);
    jcb_selected.setBackground(Color.WHITE );
  }

  public void actionPerformed(ActionEvent evt){
    if (evt.getSource()==jcb_selected){
      table.setValueAt(getCellEditorValue(), rowIndex, colIndex);
    }
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int rowIndex,
                                               int vColIndex) {
    this.table = table;
    this.rowIndex = rowIndex;
    this.colIndex = table.convertColumnIndexToModel(vColIndex);

    if (! (value instanceof String)) {
      return null;
    }

    String v = (String) value;
    if (v.equalsIgnoreCase("true")) {
      jcb_selected.setSelected(true);
    }
    else {
      jcb_selected.setSelected(false);
    }

    return jcb_selected;

  }


  /**
   * Returns the value contained in the editor.
   *
   * @return the value contained in the editor
   * @todo Implement this javax.swing.CellEditor method
   */
  public Object getCellEditorValue() {
    if (jcb_selected.isSelected())
      return "true";
    else
      return "false";
  }
}
