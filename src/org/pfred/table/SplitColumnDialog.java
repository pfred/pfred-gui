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
package org.pfred.table;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SplitColumnDialog extends JDialog
        implements ActionListener {

    String splitColumn;
    JComboBox splitTypeCombo;
    SplitColumnOptions retVal = null;

    private SplitColumnDialog(Frame owner, SplitColumnOptions defaultOptions) {
        super(owner);
        init(defaultOptions);

        setLocationRelativeTo(owner);
        pack();
    }

    private void init(SplitColumnOptions defaultOptions) {
        this.setModal(true);
        this.setTitle("Choose Column Split Options");

        splitColumn = defaultOptions.getSplitColumn();

        String[] names = SplitColumnOptions.getSplitTypeNames();
        splitTypeCombo = new JComboBox(names);
        splitTypeCombo.setSelectedIndex(defaultOptions.getSplitType());

        JPanel bottomPanel = new JPanel();
        JButton ok = new JButton("OK");
        ok.setActionCommand("ok");
        ok.addActionListener(this);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel");
        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel colPanel = new JPanel();
        colPanel.add(new JLabel("Column: " + defaultOptions.getSplitColumn()));

        JPanel typePanel = new JPanel();
        typePanel.add(new JLabel("Type:"));
        typePanel.add(splitTypeCombo);

        bottomPanel.add(ok, null);
        bottomPanel.add(cancel, null);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(colPanel, BorderLayout.NORTH);
        topPanel.add(typePanel, BorderLayout.CENTER);

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        this.getContentPane().add(centerPanel, BorderLayout.CENTER);
    }

    public static SplitColumnOptions showDialog(Frame owner, SplitColumnOptions defaultOptions) {
        SplitColumnDialog dlg = new SplitColumnDialog(owner, defaultOptions);
        dlg.show();

        return dlg.retVal;
    }

    // update value from ui
    void updateValue() {
        if (retVal == null) {
            retVal = new SplitColumnOptions();
        }

        retVal.setSplitColumn(splitColumn);
        retVal.setSplitType(splitTypeCombo.getSelectedIndex());
    }

    // button press
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.equalsIgnoreCase("ok")) {
            updateValue();
            hide();
        } else if (cmd.equalsIgnoreCase("cancel")) {
            // cancel
            retVal = null;
            hide();
        }
    }
}
