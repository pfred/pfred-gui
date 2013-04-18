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

import java.awt.BorderLayout;
import com.pfizer.rtc.util.SpringUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

import com.pfizer.rtc.util.FileUtil;

public class OligoEnumerationDialog
    extends JDialog implements ActionListener {
  private boolean canceled = true;
  private JFrame owner;
  private JTextArea jta_sequence;
  private JTextField jtf_oligo_length, jtf_oligo_prefix;
  private JButton bttn_OK, bttn_Cancel;


  public OligoEnumerationDialog(JFrame owner, String title) {

    super(owner);
    setModal(true);
    this.owner = owner;

    this.setTitle(title);
    try {
      buildUI();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void buildUI() {
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(new JLabel("Paste in target DNA or mRNA sequence: "),
                              BorderLayout.NORTH);
    jta_sequence = new JTextArea(">target1\nCAATAGTTGCTTCGATTCAGAAATGCAAAACCCTAACCTCACTCAAAAATTTCAGGCCAAGGGATCCAGATAATACCAGATGGGTCTTAAGAAAGCCGTTTTGCTGTGGGATAAAGAGCCGCTTAGTCGGGGATCGTTTTCGGGTCATTTTACTGAGCGCCGCCTCGCCGGGCTCAGAGCGGTTCCTGGGAAATTGGACCAATGGGCTCGCGCTGCGCGCTGCGGTGCCGCCCAGGACCTGGGCCTACATTTCCC");

    jta_sequence.setBorder(BorderFactory.createLoweredBevelBorder());
    jta_sequence.setLineWrap(true);
    jta_sequence.setWrapStyleWord(true);
    this.getContentPane().add(jta_sequence, BorderLayout.CENTER);
    JPanel bottom_pane = new JPanel();
    bottom_pane.setLayout(new BorderLayout());
    this.getContentPane().add(bottom_pane, BorderLayout.SOUTH);

    JPanel paramPane = new JPanel();
    paramPane.setLayout(new SpringLayout());
    paramPane.add(new JLabel("Enter oligo name prefix:", SwingConstants.RIGHT));
   jtf_oligo_prefix = new JTextField("Oligo_", 15);
   paramPane.add(jtf_oligo_prefix);

    paramPane.add(new JLabel("Enter oligo length:", SwingConstants.RIGHT));
    jtf_oligo_length = new JTextField("19", 15);
    paramPane.add(jtf_oligo_length);
    /*paramPane.add(new JLabel("Enumerate on reverse complement:",
                             SwingConstants.RIGHT));
    jcb_rc = new JCheckBox("", true);
    paramPane.add(jcb_rc);*/
    /*paramPane.add(new JLabel("Choose type:", SwingConstants.RIGHT));
    jcb_chooseType = new JComboBox(new String[] {PFREDConstant.RNA_OLIGO, PFREDConstant.DNA_OLIGO});
    paramPane.add(jcb_chooseType);
    */
    SpringUtilities.makeCompactGrid(paramPane, //parent
                                    2, 2, //2x2 grid
                                    3, 3, //initX, initY
                                    3, 3); //xPad, yPad
    bottom_pane.add(paramPane, BorderLayout.CENTER);
    JPanel bttn_pane = new JPanel();
    bttn_OK = new JButton("OK");
    bttn_OK.addActionListener(this);
    bttn_Cancel = new JButton("Cancel");
    bttn_Cancel.addActionListener(this);
    bttn_pane.add(bttn_OK);
    bttn_pane.add(bttn_Cancel);
    bttn_pane.add(bttn_OK);
    bttn_pane.add(bttn_Cancel);
    bottom_pane.add(bttn_pane, BorderLayout.SOUTH);
    this.setSize(new Dimension(400, 400));
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == bttn_OK) {
      String text=jtf_oligo_length.getText();
      try{
        Integer.parseInt(text.trim());
      }catch(Exception ex){
        JOptionPane.showMessageDialog(this, "Invalid oligo length.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      canceled = false;
      dispose();
    }
    else if (evt.getSource() == bttn_Cancel) {
      dispose();
    }
  }

  public boolean isCanceled() {
    return canceled;
  }

  public String getSeq(){
    String seq=jta_sequence.getText().trim();
    //now clean up on the sequence
    String [] lines=FileUtil.getFields(seq, "\n");
    StringBuffer result=new StringBuffer();
    int i=0;
    if (seq.startsWith(">")){
      result.append(lines[0]);
      result.append("\n");
      i++;
    }
    for (;i<lines.length;i++){
      result.append(lines[i]);//remove line breaks
    }
    return result.toString();
  }


  public int getLength(){
    String text=jtf_oligo_length.getText();
    return Integer.parseInt(text.trim());
  }

  public String getPrefix(){
    return jtf_oligo_prefix.getText();
  }


  public static void main(String[] args) {
    OligoEnumerationDialog dialog = new OligoEnumerationDialog(null,
        "Simple Oligo Enumerator");
    dialog.setVisible(true);
  }
}
