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
package org.pfred.group;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.pfred.PFREDConstant;
import org.pfred.PFREDContext;
import org.pfred.enumerator.OligoSelectorModel;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Datum;
import org.pfred.util.ColorIcon;
import com.pfizer.rtc.util.ErrorDialog;


public class NewGroupDialog extends JDialog implements ActionListener {

    private PFREDContext context;
    private Frame owner;
    private JCheckBox addSelected = new JCheckBox("Add Selected Compounds", true);
    private JCheckBox showColor = new JCheckBox("Show Color", true);
    private JTextField name = new JTextField();
    private JButton color_button = new JButton(new ColorIcon());
    private Color newColor;
    private final JButton ok_button = new JButton("OK");
    private JButton cancel_button = new JButton("Cancel");
    private GroupInfo value;
    public final static int BASIC = 1;
    public final static int OLIGO_SELECTOR = 2;
    private int groupType = BASIC;
    private OligoSelectorModel oligoSelectorModel;

    public NewGroupDialog(PFREDContext context, Frame owner) {
        super(owner, "New Group");
        this.owner = owner;
        this.context = context;
        initialize();

    }

    public NewGroupDialog(PFREDContext context, Frame owner, OligoSelectorModel oligoSelectorModel) {
        super(owner, "New Group");
        this.owner = owner;
        this.context = context;
        this.oligoSelectorModel = oligoSelectorModel;
        groupType = OLIGO_SELECTOR;
        initialize();

    }

    public GroupInfo getGroupInfo() {
        return value;
    }

    private void initialize() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        centerPanel.add(new JLabel("Group Name:"));
        name.setColumns(20);
        centerPanel.add(name);

        color_button.addActionListener(this);
        centerPanel.add(color_button);

        ok_button.addActionListener(this);
        getRootPane().setDefaultButton(ok_button);
        bottomPanel.add(ok_button);

        cancel_button.addActionListener(this);
        bottomPanel.add(cancel_button);

        JPanel optionPane = new JPanel();
        optionPane.add(addSelected);
        optionPane.add(showColor);
        optionPane.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        contentPane.add(optionPane, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);


        setSize(150, 400);
        pack();
        setLocationRelativeTo(owner);
        name.setRequestFocusEnabled(true);
        name.requestFocus();

        setModal(true);

        newColor = GroupInfoHelper.randomColor();
        color_button.setIcon(new ColorIcon(newColor));
        name.setText("");
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == color_button) {

            Color color = JColorChooser.showDialog(this, "Choose Group Color", newColor);
            if (color == null) {
                return;
            }
            newColor = color;
            color_button.setIcon(new ColorIcon(newColor));
        } else if (e.getSource() == ok_button) {

            GroupListModel group_model = context.getDataStore().getGroupListModel();
            String groupName = name.getText().trim();
            if (groupName.length() < 1) {
                ErrorDialog.showErrorDialog(owner,
                        "Please enter a group name.");
                return;
            } else if (groupName.indexOf(":") != -1) {
                ErrorDialog.showErrorDialog(owner,
                        "The group name cannot contains colons. Please enter a different group name.");
                return;
            } else if (group_model.getGroupInfo(groupName) != null) {
                ErrorDialog.showErrorDialog(owner,
                        "A group with that name is already defined. Please enter a different group name.");
                return;
            }


            // set the return value
            groupName = groupName.replace(' ', '_');
            switch (groupType) {

                case OLIGO_SELECTOR:
                    value = new OligoSelectorGroupInfo(groupName, newColor, false,oligoSelectorModel);
                    break;
                case BASIC:
                default:
                    value = new SimpleGroupInfo(groupName, newColor, false);
                    break;
            }


            group_model.setDataIsChanging(true);
            group_model.addGroupInfo(value, true);

            CustomListSelectionModel cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();
            // create this as a legit group before returning it
            CustomListModel cmpd_model = context.getDataStore().getOligoListModel();
            cmpd_model.setDataIsChanging(true);
            if (addSelected.isSelected()) {
                group_model.addMolsToGroup(value.name, cmpd_sel_model.getSelectedData()); //todo: notify listeners here?
            } else {
                Datum mol = (Datum) cmpd_model.getDatum(0);
                if (mol != null && mol.getProperty(PFREDConstant.GROUP_PROPERTY) == null) {
                    mol.setProperty(PFREDConstant.GROUP_PROPERTY, "");
                }
            }
            if (showColor.isSelected()) {
                group_model.showGroupColor(value, true);
            }
            cmpd_model.setDataIsChanging(false,
                    CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);
            group_model.setDataIsChanging(false);

            dispose();
        } else if (e.getSource() == cancel_button) {
            // return value of null means cancel
            value = null;
            dispose();
        }
    }
}

