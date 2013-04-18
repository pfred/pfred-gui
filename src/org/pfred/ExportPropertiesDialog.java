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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.pfred.group.GroupListModel;
import org.pfred.model.CustomListSelectionModel;
import com.pfizer.rtc.util.ErrorDialog;
import com.pfizer.rtc.util.PathManager;

public class ExportPropertiesDialog extends JDialog implements ActionListener {

    private PFREDContext context;
    private JFrame owner;
    private JComboBox existingGroupCB;
    private String[] propertyNames;
    private boolean compounds;
    private boolean convertNotation2Seq = false;
    private JButton export_button, cancel_button;

    public ExportPropertiesDialog(PFREDContext context, JFrame owner, String[] propertyNames, boolean convertNotation2Seq, boolean compounds) {
        super(owner, compounds ? "Export Compound Properties" : "Export Cluster Properties");
        this.context = context;
        this.compounds = compounds;
        this.convertNotation2Seq = convertNotation2Seq;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        this.owner = owner;
        this.propertyNames = propertyNames;

        JPanel overallPanel = new JPanel(new BorderLayout(3, 1));
        JPanel selectionPanel = new JPanel();
        selectionPanel.add(new JLabel("Export:"));

        if (compounds) {
            GroupListModel group_model = context.getDataStore().getGroupListModel();
            String[] names = group_model.getGroupNames();
            existingGroupCB = new JComboBox();
            existingGroupCB.addItem(PFREDConstant.ALL_COMPOUNDS);
            existingGroupCB.addItem(PFREDConstant.SELECTED_COMPOUNDS);
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    existingGroupCB.addItem(names[i]);
                }
            }
            existingGroupCB.setEditable(false);
        } else {
            existingGroupCB = new JComboBox();
            existingGroupCB.addItem(PFREDConstant.ALL_CLUSTERS);
            existingGroupCB.addItem(PFREDConstant.SELECTED_CLUSTERS);
        }
        selectionPanel.add(existingGroupCB);

        JPanel subpanel = new JPanel();
        export_button = new JButton("Export");
        export_button.addActionListener(this);
        subpanel.add(export_button);


        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        overallPanel.add(selectionPanel, BorderLayout.NORTH);
        overallPanel.add(subpanel, BorderLayout.SOUTH);
        contentPane.add(overallPanel, BorderLayout.NORTH);
        setSize(150, 400);
        setLocationRelativeTo(owner);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == export_button) {
            String groupName = existingGroupCB.getSelectedItem().toString().trim();
            if (groupName.length() < 1) {
                ErrorDialog.showErrorDialog(owner,
                        "Please enter a group name.");
                return;
            }

            ArrayList exportMols = null;

            if (compounds) {
                if (groupName.equals(PFREDConstant.SELECTED_COMPOUNDS)) {
                    CustomListSelectionModel sel_model = context.getDataStore().
                            getOligoListSelectionModel();
                    exportMols = sel_model.getSelectedData();
                } else {
                    GroupListModel group_model = context.getDataStore().getGroupListModel();
                    exportMols = group_model.getMolsByGroupName(groupName);
                }
            }



            if (exportMols == null) {
                ErrorDialog.showErrorDialog(owner,
                        "No molecules in the selected group or the current selection");
                return;
            } /*else if (groupName.equals(props.getBackgroundString())) {
            ErrorDialog.showErrorDialog(owner,
            "Group name '" + props.getBackgroundString() + "' is reserved. Please enter a different group name.");
            return;
            }*/ else {
                JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
                fc.setFileFilter(new CSVFilter());

                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                if (fc.showDialog(this, "Export") == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    PathManager.setCurrentPath(f.getParentFile());
                    PropertyActionHandler propertyActionHandler =
                            context.getUIManager().getPropertyActionHandler();
                    propertyActionHandler.savePropertiesToFile(exportMols, f, propertyNames, convertNotation2Seq);
                }
            }

            dispose();
        } else if (e.getSource() == cancel_button) {
            dispose();
            owner.getContentPane().validate();
            owner.getContentPane().repaint();
        }
    }

    class CSVFilter
            extends FileFilter {

        public boolean accept(File f) {
            String name = f.getName().trim().toLowerCase();
            return name.endsWith(".csv") || name.endsWith(".txt") || f.isDirectory();
        }

        public String getDescription() {
            return "comma separated (*.csv), tab delimited (*.txt)";
        }
    }
}
