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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AnnotationDialog extends JDialog
    implements ActionListener
{

  final JButton ok_button = new JButton("OK");
  final JButton cancel_button = new JButton("Cancel");
  JPanel button_panel = new JPanel();
  Frame owner;

  static String value;
  String defaultText;
  JPanel center_panel = new JPanel();
  JLabel jLabel1 = new JLabel();
  JTextArea annotation_text = new JTextArea();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public static String show(Frame owner, String type,
                            String name, String defaultText)
  {
    value = null;
    AnnotationDialog dialog = new AnnotationDialog(owner, type,name,defaultText);
    dialog.show();

    return value;
  }

  public AnnotationDialog(Frame owner, String type, String name,
                          String defaultText)
  {
    super(owner,"Annotation for "+type+" "+name);
    this.setModal(true);
    this.defaultText = defaultText;
    this.owner = owner;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  {
    center_panel.setLayout(gridBagLayout1);
    jLabel1.setText("Enter the annotation text:");
    annotation_text.setBorder(BorderFactory.createEtchedBorder());
    annotation_text.setPreferredSize(new Dimension(300, 200));
    annotation_text.setLineWrap(true);
    ok_button.addActionListener(this);
    cancel_button.addActionListener(this);
    button_panel.add(ok_button);
    button_panel.add(cancel_button);
    this.getContentPane().add(center_panel, BorderLayout.CENTER);
    this.getContentPane().add(button_panel, BorderLayout.SOUTH);
    center_panel.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(9, 16, 0, 129), 121, -1));
    annotation_text.setText(defaultText);
    center_panel.add(annotation_text,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 16, 6, 21), 61, 23));

    setSize(350,250);
    setLocationRelativeTo(owner);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == ok_button)
    {
      // return value we typed
      value = annotation_text.getText();
      dispose();
    }
    else if (e.getSource() == cancel_button)
    {
      // value stays at null for cancel
      dispose();
    }
  }
}
