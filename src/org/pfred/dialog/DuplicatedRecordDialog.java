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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.Container;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JRootPane;
import javax.swing.event.AncestorEvent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorListener;


public class DuplicatedRecordDialog extends JDialog implements ActionListener {
  boolean canceled=true;
  JButton bttn_OK, bttn_Cancel;
  JRadioButton jrb_merge,jrb_keepFirst,jrb_keepLast,jrb_skip,jrb_keepSeparate;
  //JCheckBox jcb_applyAll;
  JFrame parent;
  String msg;

  public static final int MERGE=0;
  public static final int KEEP_FIRST=1;
  public static final int KEEP_LAST=2;
  public static final int SKIP=3;
  public static final int KEEP_SEPARATE=4;

  public DuplicatedRecordDialog(JFrame parent, String message) {
    super(parent, "Duplicated Records", true);
    this.parent=parent;
    msg=message;
    initGUI();


    WindowAdapter adapter = new WindowAdapter() {
      private boolean gotFocus = false;
      public void windowClosing(WindowEvent we) {

      }

      public void windowGainedFocus(WindowEvent we) {
        // Once window gets focus, set initial focus
        if (!gotFocus) {
          bttn_OK.requestFocus();
          //gotFocus = true;
        }
      }
    };
    this.addWindowListener(adapter);
    this.addWindowFocusListener(adapter);

    //getRootPane().setWindowDecorationStyle(JRootPane.QUESTION_DIALOG);

    pack();
    this.setLocationRelativeTo(parent);
    setVisible(true);

    //repaint();
  }

  public DuplicatedRecordDialog(JFrame parent) {
    this(parent, "Found duplicated records. Choose action: ");
  }


  public void initGUI(){

    getContentPane().setLayout(new BorderLayout());
    JPanel rootPane=new JPanel();
    rootPane.setLayout(new BorderLayout());
    getContentPane().add(rootPane);
    GUIHelper.installDefaults(rootPane);

    JPanel topPane=new JPanel(); //add the message label

    topPane.setLayout(new BorderLayout());
    JLabel label1=new JLabel(msg);
    topPane.add(label1);
    rootPane.add(topPane, BorderLayout.NORTH);

    JPanel centerPane=new JPanel();
    centerPane.setFocusable(false);
    centerPane.setLayout(new GridLayout(0,1));
    jrb_merge=new JRadioButton("Merge duplicates. (Overlapping fields will be concatenated)");

    jrb_keepSeparate=new JRadioButton("Keep duplicates separately. (Duplicates will be renamed with a suffix)");
    jrb_keepFirst=new JRadioButton("Keep first record");
    jrb_keepLast=new JRadioButton("Keep last record");
    jrb_skip=new JRadioButton("Skip all duplicated records");
    centerPane.add(jrb_merge);
    centerPane.add(jrb_keepSeparate);
    centerPane.add(jrb_keepFirst);
    centerPane.add(jrb_keepLast);
    centerPane.add(jrb_skip);
    centerPane.setBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)));
    rootPane.add(centerPane, BorderLayout.CENTER);
    ButtonGroup bg=new ButtonGroup();
    bg.add(jrb_merge);
    bg.add(jrb_keepSeparate);
    bg.add(jrb_keepFirst);
    bg.add(jrb_keepLast);
    bg.add(jrb_skip);
    jrb_merge.setSelected(true);

    JPanel buttonPane=new JPanel();
    bttn_OK=new JButton("OK");
    bttn_Cancel=new JButton("Cancel");
    buttonPane.add(bttn_OK);
    buttonPane.add(bttn_Cancel);
    bttn_OK.addActionListener(this);
    setDefaultButton(bttn_OK);
    bttn_Cancel.addActionListener(this);

    rootPane.add(buttonPane, BorderLayout.SOUTH);
    setAllNonFocusable(centerPane);
  }

  private void setDefaultButton(JButton button) {
    button.addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent e) {
        JButton defaultButton = (JButton) e.getComponent();
        JRootPane root = SwingUtilities.getRootPane(defaultButton);
        if (root != null) {
          root.setDefaultButton(defaultButton);
        }
      }

      public void ancestorRemoved(AncestorEvent event) {}

      public void ancestorMoved(AncestorEvent event) {}
    });

  }

  private void setAllNonFocusable(Container c){
    Component[] comps=c.getComponents();
    for (int i=0; i<comps.length; i++){
      if (comps[i] instanceof Container){
        setAllNonFocusable((Container) comps[i]);
        comps[i].setFocusable(false);
      }else{
        comps[i].setFocusable(false);
      }
    }
  }



  public int getOption(){
    int option= KEEP_SEPARATE;
    if (jrb_merge.isSelected()){
      option=MERGE;
    }else if (jrb_keepFirst.isSelected()){
      option=KEEP_FIRST;
    }else if (jrb_keepLast.isSelected()){
      option=KEEP_LAST;
    }else if (jrb_skip.isSelected()){
      option=SKIP;
    }else if (jrb_keepSeparate.isSelected()){
      option=KEEP_SEPARATE;
    }

    return option;
  }

  public void actionPerformed(ActionEvent evt){
   if (evt.getSource() == bttn_OK)
   {
     canceled=false;
     dispose();
   }else if (evt.getSource()==bttn_Cancel){
     dispose();
   }
 }


  public boolean isCanceled() {
    return canceled;
  }


  public static void main(String[] args) {
    DuplicatedRecordDialog dialog = new DuplicatedRecordDialog(null);
  }
}
