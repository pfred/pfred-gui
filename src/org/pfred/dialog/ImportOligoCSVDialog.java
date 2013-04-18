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
import java.awt.event.*;
import com.pfizer.rtc.util.SpringUtilities;
import java.awt.BorderLayout;

public class ImportOligoCSVDialog extends JDialog implements ActionListener{
  JComboBox jcb_id=null;
  JComboBox jcb_seq=null;
  JComboBox jcb_start=null;
  JComboBox jcb_end=null;
  JComboBox jcb_targetSeqName=null;

  String[] props =null;
  String[] props2=null;
  JFrame parent=null;
  JButton bttn_OK=null;
  JButton bttn_Cancel=null;

  boolean isCanceled=true;

  public ImportOligoCSVDialog(JFrame parent, String[] props) {
    super(parent, "Specify fields", true);
    this.parent=parent;
    this.props=props;
    props2=new String[props.length+1];
    props2[0]="    ";
    for (int i=0; i<props.length; i++){
      props2[i+1]=props[i];
    }
    init();
    this.setLocationRelativeTo(parent);
  }
  public void actionPerformed(ActionEvent evt){
    if (evt.getSource()==bttn_OK){
      isCanceled=false;
      dispose();
    }else {
      dispose();
    }
  }
  public int getIDFieldIndex(){
    return jcb_id.getSelectedIndex();
  }

  public int getSeqFieldIndex(){
    return jcb_seq.getSelectedIndex()-1;
  }

  public int getStartFieldIndex(){
    return jcb_start.getSelectedIndex()-1;
  }

  public int getEndFieldIndex(){
   return jcb_end.getSelectedIndex()-1;
 }

 public int getTargetSeqNameFieldIndex(){
  return jcb_targetSeqName.getSelectedIndex()-1;
}




  public boolean isCanceled(){
    return isCanceled;
  }
  private void init() {
    this.getContentPane().setLayout(new BorderLayout());

    JPanel centerPane = new JPanel();
    this.getContentPane().add(centerPane, BorderLayout.CENTER);
    centerPane.setLayout(new SpringLayout());

    centerPane.add(new JLabel("Choose ID field:", JLabel.TRAILING));

    jcb_id=new JComboBox(props);
    //jcb_id.setMinimumSize(new Dimension(30, 20));
    int idx = guessIdField(props);
    jcb_id.setSelectedIndex(idx);
    centerPane.add(jcb_id);

    centerPane.add(new JLabel("Choose oligo sequence field:", JLabel.TRAILING));
    jcb_seq=new JComboBox(props2);
    //idx=guessOligoField(props);
    //jcb_seq.setSelectedIndex(idx);
    jcb_seq.setSelectedIndex(0);
    centerPane.add(jcb_seq);

    //targetName field
    centerPane.add(new JLabel("Choose TARGET NAME fields (optional):", JLabel.TRAILING));
    jcb_targetSeqName=new JComboBox(props2);
    jcb_targetSeqName.setSelectedIndex(0);
    centerPane.add(jcb_targetSeqName);

    //start field
    centerPane.add(new JLabel("Choose START fields (optional):", JLabel.TRAILING));
    jcb_start=new JComboBox(props2);
    jcb_start.setSelectedIndex(0);
    centerPane.add(jcb_start);

    //end field
    centerPane.add(new JLabel("Choose END fields (optional):",
                              JLabel.TRAILING));
    jcb_end = new JComboBox(props2);
    jcb_end.setSelectedIndex(0);
    centerPane.add(jcb_end);



    SpringUtilities.makeCompactGrid(centerPane, //parent
                                    5, 2,
                                    3, 3, //initX, initY
                                    3, 3); //xPad, yPad


    bttn_OK= new JButton("OK");
    bttn_OK.addActionListener(this);
    bttn_Cancel= new JButton("Cancel");
    bttn_Cancel.addActionListener(this);
    JPanel bttnPane = new JPanel();
    bttnPane.add(bttn_OK);
    bttnPane.add(bttn_Cancel);
    this.getContentPane().add(bttnPane, BorderLayout.SOUTH);
    pack();
  }

  private int guessIdField(String[] fields){
    int idx=-1;
    for (int i=0; i<fields.length; i++){
      String field =fields[i];
      if (field.equalsIgnoreCase("ID") || field.equalsIgnoreCase("name")){
        idx =i;
        break;
      }
    }

    if (idx==-1)
      idx=0;
    return idx;
  }




}
