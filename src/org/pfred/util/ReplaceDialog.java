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

package org.pfred.util;

import javax.swing.*;
import java.awt.*;
import com.borland.jbcl.layout.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class ReplaceDialog extends JDialog implements ActionListener{
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  JButton bttn_Replace = new JButton();
  JButton bttn_Cancel = new JButton();
  XYLayout xYLayout1 = new XYLayout();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JTextField jtf_find = new JTextField();
  JTextField jtf_replace = new JTextField();
  JComboBox jComboBox1;
  JLabel jLabel3 = new JLabel();
  JCheckBox jCheckBox1 = new JCheckBox();
  JCheckBox jCheckBox2 = new JCheckBox();
  FlowLayout flowLayout1 = new FlowLayout();

  boolean isCanceled=true;

  public ReplaceDialog(JFrame parent, String[]propNames, String defaultSelectedProp) {
    super(parent, "Replace");
    setModal(true);
    jComboBox1 = new JComboBox(propNames);
    jComboBox1.setSelectedItem(defaultSelectedProp);

    try {
      jbInit();
      setSize(390, 210);
      setLocation(200,200);
      setVisible(true);
      jtf_find.requestFocus(true);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public boolean isCancel(){
    return isCanceled;
  }

  public String getFindString(){
    return jtf_find.getText();
  }

  public String getReplaceString(){
    return jtf_replace.getText();
  }

  public String getSelectedProp(){
    return jComboBox1.getSelectedItem().toString();
  }

  public boolean matchExactCellOnly(){
      return jCheckBox1.isSelected();
    }

  public boolean useRegex(){
      return jCheckBox2.isSelected();
    }

  public void actionPerformed(ActionEvent evt){
    if (evt.getSource() == bttn_Replace)
    {
      isCanceled=false;
      dispose();
    }else if (evt.getSource()==bttn_Cancel){
      dispose();
    }

  }

  private void jbInit() throws Exception {
    bttn_Replace.setText("Replace");
    bttn_Cancel.setText("Cancel");
    jPanel1.setLayout(xYLayout1);
    jLabel1.setText("Find what:");
    jLabel2.setText("Replace with:");
    jLabel3.setToolTipText("");
    jLabel3.setText("in property");
    jPanel2.setLayout(flowLayout1);
    jCheckBox1.setToolTipText("");
    jCheckBox1.setText("Find entire cell only");
    jCheckBox2.setText("Use regex");
    jCheckBox2.setSelected(false);
    jtf_find.setText("");
    jtf_replace.setText("");
    jPanel2.add(bttn_Replace, null);
    jPanel2.add(bttn_Cancel, null);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jLabel1, new XYConstraints(35, 6, 67, 24));
    jPanel1.add(jtf_find,  new XYConstraints(34, 27, 310, 23));
    jPanel1.add(jtf_replace,      new XYConstraints(34, 73, 310, 23));
    jPanel1.add(jLabel2,    new XYConstraints(35, 54, 80, 22));
    jPanel1.add(jLabel3, new XYConstraints(32, 97, 63, 23));
    jPanel1.add(jComboBox1,       new XYConstraints(34, 118, 310, 23));
    jPanel1.add(jCheckBox1,  new XYConstraints(128, 10, 135, 15));
    jPanel1.add(jCheckBox2,  new XYConstraints(270, 10, 100, 15));
    this.getContentPane().add(jPanel2, BorderLayout.SOUTH);

    bttn_Replace.addActionListener(this);
    bttn_Cancel.addActionListener(this);
  }

}
