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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import org.pfred.dialog.AnnotationDialog;
import org.pfred.group.GroupInfo;
import org.pfred.group.GroupListModel;
import org.pfred.group.GroupLogicDialog;
import org.pfred.group.NewGroupDialog;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.enumerator.OligoSelector;
import org.pfred.group.OligoSelectorGroupInfo;

public class GroupActionHandler implements ActionListener {

    PFREDContext context;
    JFrame parent;
    boolean showAddToGroupWarning = true;

    public GroupActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();

            if (name.equals("newGroup")) {
                newGroup();
            } else if (name.equals("groupLogic")) {
                GroupLogicDialog.showDialog(context, context.getUIManager().getPFREDFrame());
            } else if (name.equals("showGroup")) {
                showGroup(getSelectedGroups());
            } else if (name.equals("hideGroup")) {
                hideGroup(getSelectedGroups());
            } else if (name.equalsIgnoreCase("moveGroupTop")) {
                GroupListModel group_model = context.getDataStore().getGroupListModel();
                group_model.setDataIsChanging(true);
                GroupInfo[] groups = getSelectedGroups();
                group_model.moveGroupToTop(groups);
                group_model.setDataIsChanging(false);
                selectGroups(groups);
            } else if (name.equalsIgnoreCase("moveGroupUp")) {
                GroupListModel group_model = context.getDataStore().getGroupListModel();
                group_model.setDataIsChanging(true);
                GroupInfo[] groups = getSelectedGroups();
                group_model.moveGroupUp(groups);
                group_model.setDataIsChanging(false);
                selectGroups(groups);
            } else if (name.equalsIgnoreCase("moveGroupDown")) {
                GroupListModel group_model = context.getDataStore().getGroupListModel();
                group_model.setDataIsChanging(true);
                GroupInfo[] groups = getSelectedGroups();
                group_model.moveGroupDown(groups);
                group_model.setDataIsChanging(false);
                selectGroups(groups);
            } else if (name.equalsIgnoreCase("moveGroupBottom")) {
                GroupListModel group_model = context.getDataStore().getGroupListModel();
                group_model.setDataIsChanging(true);
                GroupInfo[] groups = getSelectedGroups();
                group_model.moveGroupBottom(groups);
                group_model.setDataIsChanging(false);
                selectGroups(groups);
            } else if (name.equalsIgnoreCase("deleteGroup")) {
                deleteGroups(getSelectedGroups());
                clearGroupsSelection();
            } else if (name.equalsIgnoreCase("renameGroup")) {
                renameGroup();
            } else if (name.equalsIgnoreCase("recolorGroup")) {
                GroupInfo[] groups = getSelectedGroups();
                // only can recolor one
                if (groups.length > 0) {
                    Color oldColor = groups[0].color;
                    Color newColor = JColorChooser.showDialog(parent, "Choose New Group Color", oldColor);
                    if (newColor != null) {
                        setGroupColor(groups[0], newColor);
                    }
                }
            } else if (name.equalsIgnoreCase("addMolToGroup")) {
                addMolToGroups();
            } else if (name.equalsIgnoreCase("removeMolFromGroup")) {
                removeMolsFromGroup();
            } else if (name.equalsIgnoreCase("selectCompoundsGroup")) {
                selectCompoundsGroup();
            } else if (name.equalsIgnoreCase("annotateGroup")) {
                annotateGroup();
            } else if (name.equalsIgnoreCase("oligoSelector")) {
                openOligoSelector();
            }
        }

    }

    // add new group
    public void newGroup() {
        NewGroupDialog newGroupDialog = new NewGroupDialog(context, parent);
        GroupInfo opt = newGroupDialog.getGroupInfo();
        //GroupInfo opt = NewGroupDialog.showDialog(context, parent);
        selectGroups(new GroupInfo[]{opt});
    }

    // show given groups
    public void showGroup(GroupInfo[] groups) {
        if (groups == null) {
            return;
        }
        // show these
        GroupListModel group_model = context.getDataStore().getGroupListModel();
        group_model.setDataIsChanging(true);
        group_model.showGroupColor(groups, true);
        group_model.setDataIsChanging(false);
    }

    // hide given groups
    public void hideGroup(GroupInfo[] groups) {
        if (groups == null) {
            return;
        }
        // hide these
        GroupListModel group_model = context.getDataStore().getGroupListModel();
        group_model.setDataIsChanging(true);
        group_model.showGroupColor(groups, false);
        group_model.setDataIsChanging(false);
    }
    // get currently selected group(s)

    private GroupInfo[] getSelectedGroups() {
        JList groupList = context.getUIManager().getGroupList();
        Object[] selectedGroups = groupList.getSelectedValues();
        if (selectedGroups == null) {
            return null;
        }
        GroupInfo[] groups = new GroupInfo[selectedGroups.length];
        for (int i = 0; i < selectedGroups.length; i++) {
            groups[i] = (GroupInfo) selectedGroups[i];
        }
        return groups;
    }

    private void clearGroupsSelection() {
        JList groupList = context.getUIManager().getGroupList();
        groupList.clearSelection();
    }

    private void selectGroups(GroupInfo[] groups) {
        JList groupList = context.getUIManager().getGroupList();
        groupList.clearSelection();

        GroupListModel group_model = context.getDataStore().getGroupListModel();

        int[] indices = group_model.getGroupIndicies(groups);
        groupList.setSelectedIndices(indices);
    }

    public void setGroupColor(GroupInfo group, Color c) {
        GroupListModel group_model = context.getDataStore().getGroupListModel();
        group_model.setGroupColor(group, c);
        group_model.setDataIsChanging(true);
        group_model.showGroupColor(group, true);
        group_model.setDataIsChanging(false);
    }

    public void renameGroup() {
        GroupListModel group_model = context.getDataStore().getGroupListModel();

        GroupInfo[] groups = getSelectedGroups();
        // only can rename one
        if (groups.length > 0) {
            String oldName = groups[0].name;
            String newName = JOptionPane.showInputDialog(parent, "Old Name: " + oldName + "\n" +
                    "Enter a new name", "Enter New Group Name", JOptionPane.OK_CANCEL_OPTION);
            // if new name is not unique then display error
            if (group_model.getGroupInfo(newName) != null) {
                JOptionPane.showMessageDialog(parent, "That name is already in use. You must choose a different name");
                return;
            }

            if (newName != null) {
                group_model.renameGroup(oldName, newName);
            }

            selectGroups(groups);
        }
    }

    public void addMolToGroups() {
        CustomListSelectionModel cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();
        GroupListModel group_model = context.getDataStore().getGroupListModel();

        if (cmpd_sel_model.getSelectedDataCount() < 1) {
            JOptionPane.showMessageDialog(parent,
                    "There are no molecules selected. You must select some molecules \n" +
                    "before you can add them to the selected group(s). ");
            return;
        }

        GroupInfo[] groups = getSelectedGroups();
        if (groups == null || groups.length == 0) {
            int options = JOptionPane.showConfirmDialog(parent,
                    "There is no group currently selected. You need to either select a group\n" +
                    "or create a new group. Do you want to create a new group?\n",
                    "Confirm Add to Group", JOptionPane.YES_NO_OPTION);
            if (options != JOptionPane.YES_OPTION) {
                return;
            }
            //create a new group
            newGroup();
            return;
        }

        if (showAddToGroupWarning) {
            int options = JOptionPane.showConfirmDialog(parent, "Are you sure you want to add the selected\n" +
                    "compound(s) to the selected group(s)?\n", "Confirm Add", JOptionPane.YES_NO_OPTION);
            if (options != JOptionPane.YES_OPTION) {
                return;
            }
            showAddToGroupWarning = false;
        }

        // for each group
        for (int i = 0; i < groups.length; i++) {
            group_model.addMolsToGroup(groups[i], cmpd_sel_model.getSelectedData());
        }


    }

    public void removeMolsFromGroup() {
        CustomListSelectionModel cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();
        GroupListModel group_model = context.getDataStore().getGroupListModel();

        if (cmpd_sel_model.getSelectedDataCount() < 1) {
            JOptionPane.showMessageDialog(parent,
                    "There are no molecules selected. You must select some molecules \n" +
                    "before you can add them to the selected group(s). ");
            return;
        }

        GroupInfo[] groups = getSelectedGroups();
        if (groups == null || groups.length == 0) {
            JOptionPane.showMessageDialog(parent,
                    "There is no group currently selected. You need to select a group.");

            return;

        }

        // for each group
        for (int i = 0; i < groups.length; i++) {
            group_model.removeMolsFromGroup(groups[i].name, cmpd_sel_model.getSelectedData());
        }

    }

    public void selectCompoundsGroup() {
        selectCompoundsGroup(getSelectedGroups());
    }

    public void selectCompoundsGroup(GroupInfo[] groups) {
        CustomListSelectionModel cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();
        GroupListModel group_model = context.getDataStore().getGroupListModel();

        ArrayList toBeSelected = group_model.getMolsByGroups(groups);
        cmpd_sel_model.setValueIsAdjusting(true);
        cmpd_sel_model.selectData(toBeSelected, false);
        cmpd_sel_model.setValueIsAdjusting(false);

    }

    public void openOligoSelector() {
        GroupInfo[] groups = getSelectedGroups();
        OligoSelectorGroupInfo oligoSelectorGroupInfo = (OligoSelectorGroupInfo) groups[0];
        OligoSelector selector = new OligoSelector(parent, context, oligoSelectorGroupInfo);
        selector.setVisible(true);
    }

    public void annotateGroup() {
        GroupListModel group_model = context.getDataStore().getGroupListModel();

        GroupInfo[] groups = getSelectedGroups();
        if (groups == null || groups.length == 0) {
            return;
        }
        String text = "";
        String name = "multiple";
        String type = "Groups";
        if (groups.length == 1) {
            text = groups[0].annotation;
            name = groups[0].name;
            if (text == null) {
                text = "";
            }
        }

        // show annotation dialog, null return means cancel
        String newText = AnnotationDialog.show(parent, type, name, text);
        if (newText != null) {
            group_model.setDataIsChanging(true);
            for (int i = 0; i < groups.length; i++) {
                groups[i].annotation = newText;
            }
            group_model.setDataIsChanging(false);

        }
    }

    public void deleteGroups(GroupInfo[] groups) {

        int options = JOptionPane.showConfirmDialog(parent, "Are you sure you want to DELETE the\n" +
                "selected group(s)?", "Confirm Group Delete", JOptionPane.YES_NO_OPTION);
        if (options != JOptionPane.YES_OPTION) {
            return;
        }


        GroupListModel group_model = context.getDataStore().getGroupListModel();

        // this is the new groups
        group_model.setDataIsChanging(true);
        group_model.removeGroupInfo(groups);
        group_model.setDataIsChanging(false);

    }
}
