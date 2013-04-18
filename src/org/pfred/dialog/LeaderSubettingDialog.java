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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.pfizer.rtc.util.SpringUtilities;


public class LeaderSubettingDialog
    extends JDialog implements ActionListener {
  JFrame parent=null;
  String[]propNames=null;
  JComboBox jcb_props=null;
  JCheckBox jcb_desc=null;
  JTextField jtf_maxhits=new JTextField("50", 20);
  JTextField jtf_min_distance=new JTextField("21", 20);
  JButton bttn_OK, bttn_Cancel;
  JCheckBox jcb_keepCurrentOrder=new JCheckBox();
  boolean canceled=true;
  int maxHits=20;
  String selected_prop=null;
  int min_distance=21;

  public final static String DONT_SORT="N/A";
  public LeaderSubettingDialog(JFrame parent, String[] propNames) {
    super(parent, "Leader Subsetting", true);
    this.parent=parent;
    this.propNames=propNames;
    initGUI();
    pack();
    setLocation(200,200);
  }

  public void initGUI() {
    this.getRootPane().setLayout(new BorderLayout());
    JPanel topPane=new JPanel();
    topPane.setBorder(BorderFactory.createTitledBorder("Method Description: "));
    JTextArea jta_desc=new JTextArea("Leader subsetting algorithm:\n"+
                                      "1) Sort oligos into a list by user specified the property.\n"+
                                      "2) Select oligos one at a time from the top to bottom of the list.\n"+
                                      "3) Oligo selected each time has to be minimal distance away from previously selected oligos.\n"+
                                      "Distance is defined as # of base pairs between oligo start sites.");

     jta_desc.setWrapStyleWord(true);

     jta_desc.setBackground(new JLabel().getBackground());
     topPane.setLayout(new BorderLayout());
     topPane.add(jta_desc, BorderLayout.CENTER);
    this.getRootPane().add(topPane, BorderLayout.NORTH);

    JPanel paramPane = new JPanel();
    paramPane.setBorder(BorderFactory.createTitledBorder("Parameters: "));
    paramPane.setLayout(new SpringLayout());
    this.getRootPane().add(paramPane, BorderLayout.CENTER);

    paramPane.add(new JLabel("Use Existing Sort Order: ", JLabel.TRAILING));
    paramPane.add(jcb_keepCurrentOrder);
    jcb_keepCurrentOrder.setSelected(true);
    jcb_keepCurrentOrder.addActionListener(this);

    paramPane.add(new JLabel("Sort By: ", JLabel.TRAILING));

    /*String[] props = new String[propNames.length + 1];
    props[0] = "   ";
    for (int i = 0; i < propNames.length; i++) {
      props[i + 1] = propNames[i];
    }*/

    jcb_props = new JComboBox(propNames);
    paramPane.add(jcb_props);

    jcb_desc=new JCheckBox();
    jcb_desc.setSelected(true);
    paramPane.add(new JLabel("Sort descending: ", JLabel.TRAILING));
    paramPane.add(jcb_desc);

    JLabel jlabel=new JLabel("Min distance between selected oligos: ", JLabel.TRAILING);
    jlabel.setToolTipText("Distance is defined as the base pairs between oligo start sites");
    paramPane.add(jlabel);

    paramPane.add(jtf_min_distance);

    paramPane.add(new JLabel("Maximal # of Selections", JLabel.TRAILING));
    paramPane.add(jtf_maxhits);

    SpringUtilities.makeCompactGrid(paramPane, //parent
                                    5, 2, //2x2 grid
                                    3, 3, //initX, initY
                                    3, 3); //xPad, yPad

    JPanel bttn_pane=new JPanel();
    bttn_OK=new JButton("OK");
    bttn_OK.addActionListener(this);
    bttn_pane.add(bttn_OK);
    bttn_Cancel=new JButton("Cancel");
    bttn_Cancel.addActionListener(this);
    bttn_pane.add(bttn_Cancel);
    this.getRootPane().add(bttn_pane, BorderLayout.SOUTH);

    setEnableness();
  }

  private void setEnableness(){
    boolean enabled=!jcb_keepCurrentOrder.isSelected();
    jcb_props.setEnabled(enabled);
    jcb_desc.setEnabled(enabled);
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == bttn_OK) {
      try{
        maxHits=Integer.parseInt(jtf_maxhits.getText());
      }catch(Exception ex){
        JOptionPane.showMessageDialog(parent, "Invalid integer in max selections:"+jtf_maxhits.getText(),"Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      try {
        min_distance = Integer.parseInt(jtf_min_distance.getText());
      }
      catch (Exception ex) {
        JOptionPane.showMessageDialog(parent,
            "Invalid integer entered for minimal distance:" +
                                      jtf_min_distance.getText(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }


      canceled = false;
      dispose();
    }
    else if (evt.getSource() == bttn_Cancel) {
      dispose();
    }else if (evt.getSource() == jcb_keepCurrentOrder){
      setEnableness();
    }

  }

  public boolean keepCurrentOrder(){
    return jcb_keepCurrentOrder.isSelected();
  }

  public int getMaxHits(){
    return maxHits;
  }

  public boolean sortAsc(){
    return !jcb_desc.isSelected();
  }

  public String getSelectedPropName() {
    selected_prop = jcb_props.getSelectedItem().toString();
    return selected_prop;
  }

  public int getMinDistance(){
    return min_distance;
  }

  public boolean isCanceled() {
    return canceled;
  }


}
