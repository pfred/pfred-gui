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

package org.pfred.property;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PropertyDisplayDialog
    extends JDialog
    implements ActionListener {

// we are singleton
   static PropertyDisplayDialog dialog;
    JFrame parent;

// these are the properties we want to delete if OK button is hit
    Vector toDelete = new Vector();

    JPanel bottomPanel = new JPanel();
    JButton ok = new JButton();
    JButton cancel = new JButton();

    GridBagLayout gridBagLayout1 = new GridBagLayout();

    PropOptionsPanel optPane = null;
    PropertyDisplayManager manager= null;


    public PropertyDisplayDialog(JFrame parent, String title,
                                         PropertyDisplayManager manager) {
      super(parent, title);
      this.parent = parent;
      this.manager = manager;
      optPane = new PropOptionsPanel(manager);

      try {
        jbInit();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      setVisible(true);
    }

    private void jbInit() throws Exception {
      this.setModal(true);
      //this.setTitle("Property Display Options");
      bottomPanel.add(new JLabel("       "));
      ok.setText("Ok");
      ok.addActionListener(this);
      //cancel.setText("Cancel");
      //cancel.addActionListener(this);
      bottomPanel.add(ok);
     // bottomPanel.add(cancel, null);


      getContentPane().add(bottomPanel, BorderLayout.SOUTH);
      getContentPane().add(optPane, BorderLayout.NORTH);
      pack();
    }



    public void ok_actionPerformed(ActionEvent e) {
      dispose();
    }

    public void dispose(){
      super.dispose();
      manager.displayChanged();
    }

    /* Interface:
     *  ACTIONLISTENER
     */
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == ok) {
        ok_actionPerformed(e);
      }
     /* else if (e.getSource() == cancel) {
        dispose();
      }*/
    }
  }

