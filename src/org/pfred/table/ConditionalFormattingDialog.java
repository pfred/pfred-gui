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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class ConditionalFormattingDialog extends JDialog
        implements ActionListener {

    protected static final Dimension minPanelSize = new Dimension(640, 120);
    protected static final int minPanelsInView = 3;
    protected static final Dimension minViewportSize = new Dimension(minPanelSize.width, minPanelSize.height * minPanelsInView);
    JPanel mainPanel;
    JPanel panels;
    JPanel emptyPanel;
    JScrollPane scrollPane;
    boolean inScrollPane = false;
    ConditionalFormatting retValue;
    HashMap existingFormatting;
    JComboBox existingFormattingCombo;

    public static ConditionalFormatting showDialog(Frame frame, String title, ConditionalFormatting cf, HashMap existingFormatting) {
        ConditionalFormattingDialog dlg = new ConditionalFormattingDialog(frame, title, cf, existingFormatting);
        dlg.show();

        return dlg.retValue;
    }

    protected ConditionalFormattingDialog(Frame frame, String title, ConditionalFormatting cf, HashMap existingFormatting) {
        super(frame, title, true);

        this.existingFormatting = existingFormatting == null ? new HashMap() : existingFormatting;

        String[] otherColumnNames = (String[]) existingFormatting.keySet().toArray(new String[0]);
        if (otherColumnNames.length > 0) {
            Arrays.sort(otherColumnNames);

            existingFormattingCombo = new JComboBox(otherColumnNames);
            existingFormattingCombo.setActionCommand("existingFormatting");
            existingFormattingCombo.addActionListener(this);
        } else {
            String[] names = {"None"};
            existingFormattingCombo = new JComboBox(names);
            existingFormattingCombo.setEnabled(false);
        }

        mainPanel = new JPanel();

        // create a list of ConditionalFormatPanels to edit this data
        panels = new JPanel();
        BoxLayout layout = new BoxLayout(panels, BoxLayout.Y_AXIS);
        panels.setLayout(layout);
        mainPanel.add(panels);

        emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(minPanelSize);
        mainPanel.add(emptyPanel);

        scrollPane = new JScrollPane();
        scrollPane.getViewport().setMinimumSize(minViewportSize);
        scrollPane.getViewport().setPreferredSize(minViewportSize);

        buildPanels(cf);

        JPanel btnPanel = new JPanel();

        JButton b;

        btnPanel.add(new JLabel("Copy From:"));
        btnPanel.add(existingFormattingCombo);
        btnPanel.add(Box.createHorizontalStrut(50));

        b = new JButton("Add>>");
        b.setActionCommand("add");
        b.addActionListener(this);
        btnPanel.add(b);
        btnPanel.add(Box.createHorizontalStrut(50));


        b = new JButton("OK");
        b.setActionCommand("ok");
        b.addActionListener(this);
        btnPanel.add(b);
        btnPanel.add(Box.createHorizontalStrut(20));

        b = new JButton("Cancel");
        b.setActionCommand("cancel");
        b.addActionListener(this);
        btnPanel.add(b);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        updateSize();
        setLocationRelativeTo(frame);
    }

    protected void updateSize() {
        int num = getNumConditions();
        boolean empty = num < 1;
        emptyPanel.setVisible(empty);
        panels.setVisible(!empty);

        if (num > minPanelsInView) {
            if (!inScrollPane) {
                inScrollPane = true;
                mainPanel.remove(panels);
                scrollPane.getViewport().add(panels);
                mainPanel.add(scrollPane);
            }
        } else {
            if (inScrollPane) {
                inScrollPane = false;
                scrollPane.getViewport().remove(panels);
                mainPanel.add(panels);
                mainPanel.remove(scrollPane);
            }
        }

        pack();
    }

    protected FormatConditionPanel createPanel(FormatCondition formatCondition) {
        FormatConditionPanel panel = new FormatConditionPanel(this, formatCondition);
        panel.setPreferredSize(minPanelSize);
        panels.add(panel);
        return panel;
    }

    protected void buildPanels(ConditionalFormatting cf) {
        int numPanels = 0;
        if (cf != null) {
            for (int i = 0; i < cf.getNumConditions(); i++) {
                createPanel(cf.getCondition(i));
                numPanels++;
            }
        }

        if (numPanels < 1) {
            createPanel(null);
        }

        updateTitles();
    }

    protected int getNumConditions() {
        return panels.getComponents().length;
    }

    protected ConditionalFormatting getConditionalFormatting() {
        ConditionalFormatting conditionalFormatting = new ConditionalFormatting();

        // add one for each panel...
        Component[] comps = panels.getComponents();
        for (int i = 0; i < comps.length; i++) {
            FormatConditionPanel p = (FormatConditionPanel) comps[i];
            conditionalFormatting.addCondition(p.getFormatCondition());
        }

        return conditionalFormatting;
    }

    public void deletePanel(FormatConditionPanel panel) {
        if (JOptionPane.showConfirmDialog(this, "Delete " + panel.getTitle() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        panels.remove(panel);
        updateTitles();
        updateSize();
        repaint();
    }

    protected void updateTitles() {
        Component[] comps = panels.getComponents();
        for (int i = 0; i < comps.length; i++) {
            FormatConditionPanel p = (FormatConditionPanel) comps[i];
            p.setTitle("Condition " + (i + 1));
            p.setDeleteEnabled(i >= 0);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("ok")) {
            retValue = getConditionalFormatting();
            dispose();
        } else if (cmd.equals("cancel")) {
            retValue = null;
            dispose();
        } else if (cmd.equals("add")) {
            createPanel(null);
            updateTitles();
            updateSize();
        } else if (cmd.equals("existingFormatting")) {
            String name = (String) existingFormattingCombo.getSelectedItem();
            if (name != null) {
                // copy from here
                int option = JOptionPane.showConfirmDialog(this, "Copy formatting from: " + name,
                        "Confirm Copy Formatting", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    ConditionalFormatting cf = (ConditionalFormatting) existingFormatting.get(name);
                    if (cf != null) {
                        panels.removeAll();
                        buildPanels(cf);
                        updateSize();
                    } else {
                        System.err.println("Error: no ConditionalFormatting for: " + name);
                    }
                }
            }
        }
    }
}
