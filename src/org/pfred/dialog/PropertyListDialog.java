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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;


public class PropertyListDialog
        extends JDialog implements ActionListener {
    private JFrame owner;
    private JList jList1 = new JList();

    private JButton jButtonOK = new JButton();
    private JButton jButtonCancel = new JButton();
    private JLabel jLabel1 = new JLabel();
    private String msg;
    private String[] properties;
    private String[] selected;
    private boolean canceled=true;


    public PropertyListDialog(JFrame owner, String title, String msg, String[] properties) {
        super(owner);
        setModal(true);
        this.owner = owner;
        this.msg = msg;
        this.setTitle(title);
        this.properties = properties;
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == jButtonOK)
        {
            Object[] objs = jList1.getSelectedValues();
            if (objs==null || objs.length==0)
            {
                JOptionPane.showMessageDialog(this, "At least one property needs to be selected",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selected = new String[objs.length];
            for (int i=0; i<objs.length; i++)
            {
                selected[i]=(String) objs[i];
            }

            canceled =false;
            dispose();
        }
        else if (evt.getSource() == jButtonCancel)
        {
            dispose();
        }
    }
    public boolean isCanceled ()
    {
        return canceled;
    }

    /**
     * this will trigger the dialog and return user selections;
     */

    public String[] getSelections( )
    {

        return selected;
    }
    private void jbInit() throws Exception {
        this.getContentPane().setLayout(new BorderLayout());
        JPanel rootPane=new JPanel();
        GUIHelper.installDefaults(rootPane);
        rootPane.setLayout(new BorderLayout());
        this.getContentPane().add(rootPane, BorderLayout.CENTER);
        this.setSize(300,250);
        jButtonOK.setText("OK");
        jButtonCancel.setText("Cancel");
        jLabel1.setToolTipText("");
        jLabel1.setText(msg);
        JScrollPane scrollPane = new JScrollPane(jList1);

        rootPane.add(scrollPane, BorderLayout.CENTER);

        JPanel butt_pane=new JPanel();
        butt_pane.add(jButtonOK);
        butt_pane.add(jButtonCancel);
        rootPane.add(butt_pane, BorderLayout.SOUTH);
        rootPane.add(jLabel1,  BorderLayout.NORTH);

        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jButtonOK.addActionListener(this);
        jButtonCancel.addActionListener(this);
        jList1.setListData(properties);
        pack();

    }

}
