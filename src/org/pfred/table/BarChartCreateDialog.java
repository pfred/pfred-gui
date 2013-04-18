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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.pfred.model.CustomListModel;
import org.pfred.property.PropertyDisplayOption;

public class BarChartCreateDialog extends JDialog implements ActionListener {

    CustomListModel list_model;
    JPanel mainPanel;
    JPanel panels;
    JPanel emptyPanel;
    JScrollPane scrollPane;
    String[] availablePropNames = null;
    boolean inScrollPane = false;
    Frame frame;
    //protected static final Dimension minPanelSize = new Dimension(640,120);
    protected static final int minPanelsInView = 3;
    //protected static final Dimension minViewportSize = new Dimension(minPanelSize.width,minPanelSize.height * minPanelsInView);
    String[] selectedPropNames = null;

    public String[] getSelectedPropNames() {
        return selectedPropNames;
    }
    String[] selectedErrorPropNames = null;

    public String[] getSelectedErrorPropNames() {
        return selectedErrorPropNames;
    }

    public BarChartCreateDialog(Frame frame, String title, CustomListModel list_model) {
        super(frame, title, true);
        this.frame = frame;
        this.list_model = list_model;
        initGUI();
        pack();
        setVisible(true);
    }

    private void initGUI() {
        availablePropNames = list_model.getAllPropertyNames(PropertyDisplayOption.NUMERIC);
        mainPanel = new JPanel();

        // create a list of ConditionalFormatPanels to edit this data
        panels = new JPanel();
        BoxLayout layout = new BoxLayout(panels, BoxLayout.Y_AXIS);
        panels.setLayout(layout);
        mainPanel.add(panels);

        emptyPanel = new JPanel();
        mainPanel.add(emptyPanel);

        scrollPane = new JScrollPane();
        //scrollPane.getViewport().setMinimumSize(minViewportSize);
        //scrollPane.getViewport().setPreferredSize(minViewportSize);

        createPanel();

        JButton b;

        JPanel btnPanel = new JPanel();
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
        updateTitles();
        setLocationRelativeTo(frame);
    }

    protected void updateSize() {
        int num = getNumPanels();
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

    protected JPanel createPanel() {
        SelectPropertyPanel panel = new SelectPropertyPanel(availablePropNames);
        //panel.setPreferredSize(minPanelSize);
        panels.add(panel);
        return panel;
    }

    protected int getNumPanels() {
        return panels.getComponents().length;
    }

    public void deletePanel(SelectPropertyPanel panel) {
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
            SelectPropertyPanel p = (SelectPropertyPanel) comps[i];
            p.setTitle("Property " + (i + 1));
            p.setDeleteEnabled(i >= 0);
        }
    }

    private String[] getProperties() {
        int size = panels.getComponentCount();
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            SelectPropertyPanel p = (SelectPropertyPanel) panels.getComponent(i);
            names[i] = p.getSelectedPropName();
        }
        return names;
    }

    private String[] getErrors() {
        int size = panels.getComponentCount();
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            SelectPropertyPanel p = (SelectPropertyPanel) panels.getComponent(i);
            names[i] = p.getSelectedErrorName();
        }
        return names;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("ok")) {
            selectedPropNames = getProperties();
            selectedErrorPropNames = getErrors();

            dispose();
        } else if (cmd.equals("cancel")) {
            selectedPropNames = null;
            selectedErrorPropNames = null;
            dispose();
        } else if (cmd.equals("add")) {
            createPanel();
            updateTitles();
            updateSize();
        }

    }

    public class SelectPropertyPanel extends JPanel implements ActionListener {

        JComboBox jcb_names = null;
        JComboBox jcb_errors = null;
        TitledBorder border = new TitledBorder("Property ");
        JButton delButton = null;

        public SelectPropertyPanel(String[] propNames) {
            String[] names = new String[propNames.length + 1];
            names[0] = "";
            for (int i = 0; i < propNames.length; i++) {
                names[i + 1] = propNames[i];
            }

            jcb_names = new JComboBox(names);
            jcb_errors = new JComboBox(names);
            this.add(new JLabel("Property:"));
            this.add(jcb_names);
            this.add(new JLabel("Error:"));
            this.add(jcb_errors);
            this.add(Box.createHorizontalStrut(20));
            delButton = new JButton("Delete");
            delButton.setName("Delete");
            delButton.addActionListener(SelectPropertyPanel.this);
            this.add(delButton);
            setBorder(border);
        }

        public String getSelectedPropName() {
            if (jcb_names.getSelectedIndex() == 0) {
                return null;
            }
            return (String) jcb_names.getSelectedItem();
        }

        public String getSelectedErrorName() {
            if (jcb_errors.getSelectedIndex() == 0) {
                return null;
            }
            return (String) jcb_errors.getSelectedItem();
        }

        public void actionPerformed(ActionEvent e) {
            Component c = (Component) e.getSource();
            String name = c.getName();
            if (name == null) {
                return;
            }
            if (name.equals("Delete")) {
                deletePanel(SelectPropertyPanel.this);
            }

        }

        public String getTitle() {
            return border.getTitle();
        }

        public void setTitle(String title) {
            border.setTitle(title);
        }

        public void setDeleteEnabled(boolean enabled) {
            delButton.setEnabled(enabled);
        }
    }
}
