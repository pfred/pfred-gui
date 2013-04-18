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
package org.pfred.dialog;

import com.pfizer.rtc.util.SpringUtilities;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import javax.swing.border.BevelBorder;

public class AddRowIDDialog extends JDialog implements ActionListener{
  int INVALID=-12324253;
  boolean canceled=true;
  JTextField prefix=new JTextField();
  JTextField startIdx=new JTextField("1");
  JCheckBox setRowAsName=new JCheckBox("Set Row Number as Cmpd Name");
  JButton okButton=new JButton("OK");
  JButton cancelButton=new JButton("Cancel");
  JFrame parent=null;
  public AddRowIDDialog(JFrame parent, String title) {
    super(parent, title, true);
    this.parent=parent;
    buildGUI();
    pack();
    this.setLocationRelativeTo(parent);
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == okButton) {
      int idx=getStartIdx();
      if (idx==INVALID){
        JOptionPane.showMessageDialog(parent, "Start Index needs to an integer value!");
        return;
      }
      canceled=false;

      dispose();
    }
    else if (evt.getSource() == cancelButton) {
      dispose();
    }
  }

  public String getPrefix(){
    return prefix.getText();
  }

  public int getStartIdx(){
    try{
      return Integer.parseInt(startIdx.getText().trim());
    }catch(Exception ex){
      return INVALID;
    }
  }

  public boolean setRowIDAsName(){
    return setRowAsName.isSelected();
  }

  private void buildGUI(){
    setLayout(new BorderLayout());
    JPanel mainPane=new JPanel();
    mainPane.setLayout(new SpringLayout());
    JPanel top=new JPanel();
    top.add(new JLabel("Prefix:"));
    prefix.setMinimumSize(new Dimension(50,20));
    prefix.setPreferredSize(new Dimension(50,20));
    top.add(prefix);
    top.add(new JLabel("Starting Index:"));

    startIdx.setMinimumSize(new Dimension(50,20));
    startIdx.setPreferredSize(new Dimension(50,20));
    top.add(startIdx);

    mainPane.add(top);
    mainPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    mainPane.add(setRowAsName);
    setRowAsName.setSelected(false);
    SpringUtilities.makeCompactGrid(mainPane, //parent
                            2,1,  //2x2 grid
                            1, 1,  //initX, initY
                            3, 3); //xPad, yPad
    getContentPane().add(mainPane, BorderLayout.CENTER);
    JPanel bttnPane=new JPanel();
    bttnPane.add(okButton);
    okButton.addActionListener(this);
    bttnPane.add(cancelButton);
    cancelButton.addActionListener(this);
    getContentPane().add(bttnPane, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    AddRowIDDialog dialog = new AddRowIDDialog(null, "Set Row ID");
    dialog.setVisible(true);
  }
}
