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
package org.pfred;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListDataListener;

import org.pfred.model.Target;
import org.pfred.model.TargetListModel;

import javax.swing.event.ListDataEvent;

public class TargetSequencePanel
        extends JPanel implements ListDataListener, ActionListener {

    private JTextField jtf_id = new JTextField(20);
    private JTextArea jta_seq = new JTextArea();
    private TargetListModel list_model;

    public TargetSequencePanel(TargetListModel list_model) {
        this.list_model = list_model;
        list_model.addListDataListener(this);
        this.setLayout(new BorderLayout());
        //set up the gui
        JPanel northPane = new JPanel();
        northPane.setLayout(new FlowLayout(FlowLayout.LEADING));
        JLabel label = new JLabel("Primary target transcript id:");
        northPane.add(label);
        northPane.add(jtf_id);
        this.add(northPane, BorderLayout.NORTH);

        JPanel centerPane = new JPanel();
        centerPane.setLayout(new BorderLayout());
        label = new JLabel("Primary transcript sequence:");
        centerPane.add(label, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(jta_seq);
        centerPane.add(scrollPane, BorderLayout.CENTER);
        jta_seq.setLineWrap(true);
        this.add(centerPane, BorderLayout.CENTER);

        JPanel bttn_pane = new JPanel();
        JButton bttn_OK = new JButton("Save Change");
        bttn_OK.addActionListener(this);
        bttn_OK.setName("Save");
        bttn_pane.add(bttn_OK);
        JButton bttn_Cancel = new JButton("Cancel");
        bttn_pane.setVisible(false);
        bttn_Cancel.addActionListener(this);
        bttn_Cancel.setName("Cancel");
        bttn_pane.add(bttn_Cancel);
        this.add(bttn_pane, BorderLayout.SOUTH);

    }

    public void actionPerformed(ActionEvent evt) {
        //update the sequence
    }

    /****************************************************************/
    /**
     * Sent after the indices in the index0,index1
     * interval have been inserted in the data model.
     * The new interval includes both index0 and index1.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void intervalAdded(ListDataEvent e) {
        contentsChanged(e);
    }

    /**
     * Sent after the indices in the index0,index1 interval
     * have been removed from the data model.  The interval
     * includes both index0 and index1.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void intervalRemoved(ListDataEvent e) {
        contentsChanged(e);
    }

    /**
     * Sent when the contents of the list has changed in a way
     * that's too complex to characterize with the previous
     * methods.  Index0 and index1 bracket the change.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void contentsChanged(ListDataEvent e) {
        if (e.getSource() != list_model) {
            return;
        }
        System.out.println("Debug: CustomTableModel: ListDataEvent received");

        syncData();

    }

    private void syncData() {
        jtf_id.setText("");
        jta_seq.setText("");

        if (list_model.getSize() <= 0) {
            return;
        }
        Target t = (Target) list_model.getDatum(0);

        jtf_id.setText(t.getName());
        jta_seq.setText(t.getTargetSeq());
    }
}
