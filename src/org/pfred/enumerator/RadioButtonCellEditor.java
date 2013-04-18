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

public class RadioButtonCellEditor
    extends AbstractCellEditor implements TableCellEditor, ActionListener{
  JRadioButton jrb_selected=new JRadioButton();
  JTable table=null;
  int rowIndex=0;
  int colIndex=0;

  public RadioButtonCellEditor() {
    jrb_selected.addActionListener(this);
    jrb_selected.setBackground(Color.white);
  }

  public void actionPerformed(ActionEvent evt){
    if (evt.getSource()==jrb_selected){
      table.setValueAt(getCellEditorValue(), rowIndex, colIndex);
    }
    //table.updateUI(); //a hack, should go through some event/listener model
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int rowIndex,
                                               int vColIndex) {
    this.table=table;
    this.rowIndex=rowIndex;
    this.colIndex=table.convertColumnIndexToModel(vColIndex);

    if (! (value instanceof String))return null;
    String v = (String) value;
    if (v.equalsIgnoreCase("true")) {
      jrb_selected.setSelected(true);
    }
    else {
      jrb_selected.setSelected(false);
    }

    return jrb_selected;

  }


  /**
   * Returns the value contained in the editor.
   *
   * @return the value contained in the editor
   * @todo Implement this javax.swing.CellEditor method
   */
  public Object getCellEditorValue() {
    if (jrb_selected.isSelected())
      return "true";
    else
      return "false";
  }
}
