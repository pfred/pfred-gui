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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.pfred.GroupActionHandler;
import org.pfred.PFREDContext;
import org.pfred.util.ColorIcon;

public class GroupListPopupMenu extends JPopupMenu {

    PFREDContext context;

    public GroupListPopupMenu(PFREDContext context, Component invoker, int x, int y, JMenuItem topItem) {
        super();
        this.context = context;
        GroupActionHandler handler = context.getUIManager().getGroupActionHandler();

        // assume right click...so throw up menu

        JMenuItem item;
        JMenu menu;

        if (topItem != null) {
            //topItem.setEnabled(false);
            add(topItem);
            //topItem.setName("toggleGroup");
            //topItem.addActionListener(this);
            add(new JSeparator());
        }

        boolean oneSelected = getNumSelected(context) == 1;
        boolean twoSelected = getNumSelected(context) == 2;
        boolean someSelected = getNumSelected(context) >= 1;

        boolean oligoSelctorGroup = oligoSelectorGroupSelected(context);

        item = new JMenuItem("New Group");
        item.setName("newGroup");
        item.addActionListener(handler);
        add(item);
        add(new JSeparator());

        item = new JMenuItem("Group Logic...");
        item.setName("groupLogic");
        item.addActionListener(handler);
        item.setEnabled(oneSelected || twoSelected);
        add(item);
        add(new JSeparator());

        item = new JMenuItem("Select");
        item.setName("selectCompoundsGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);

        add(new JSeparator());

        item = new JMenuItem("Add to Group");
        item.setName("addMolToGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);
        item = new JMenuItem("Remove from Group");
        item.setName("removeMolFromGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);

        add(new JSeparator());

        menu = new JMenu("Move");
        add(menu);
        menu.setEnabled(someSelected);
        item = new JMenuItem("Move to Top");
        item.setName("moveGroupTop");
        item.addActionListener(handler);
        menu.add(item);
        item = new JMenuItem("Move Up");
        item.setName("moveGroupUp");
        item.addActionListener(handler);
        menu.add(item);
        item = new JMenuItem("Move Down");
        item.setName("moveGroupDown");
        item.addActionListener(handler);
        menu.add(item);
        item = new JMenuItem("Move to Bottom");
        item.setName("moveGroupBottom");
        item.addActionListener(handler);
        menu.add(item);
        add(new JSeparator());

        menu = new JMenu("Color");
        add(menu);
        item = new JMenuItem("Show Color");
        item.setName("showGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        menu.add(item);
        item = new JMenuItem("Hide Color");
        item.setName("hideGroup");
        item.setEnabled(someSelected);
        item.addActionListener(handler);
        menu.add(item);

        item = new JMenuItem("Recolor ");
        item.setName("recolorGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        menu.add(item);

        add(new JSeparator());

        item = new JMenuItem("Annotate");
        item.setName("annotateGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);

        if (oligoSelctorGroup) {
            add(new JSeparator());
            item = new JMenuItem("Open Oligo Selector");
            item.setName("oligoSelector");
            item.addActionListener(handler);
            item.setEnabled(someSelected);
            add(item);
        }

        add(new JSeparator());
        item = new JMenuItem("Delete Group");
        item.setName("deleteGroup");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);
        item = new JMenuItem("Rename Group");
        item.setName("renameGroup");
        item.addActionListener(handler);
        item.setEnabled(oneSelected);
        add(item);

        //setGroupMenuEnable(this);
        pack();

    }

    public static void showPopup(PFREDContext context, Component invoker, int x, int y) {
        GroupListPopupMenu popup = new GroupListPopupMenu(context, invoker, x, y, getTopItem(context));

        double tmp1 = popup.getBounds().getHeight();
        tmp1 = popup.getPreferredSize().getHeight();
        if (y > invoker.getHeight() - tmp1) {
            y = (int) (invoker.getHeight() - tmp1);
        }
        //popup.setLocation(x,y);
        popup.show(invoker, x + 10, y);
    }

    // get menu item representing currently selected group(s)
    public static JMenuItem getTopItem(PFREDContext context) {
        JList groupList = context.getUIManager().getGroupList();
        GroupListModel groupModel = context.getDataStore().getGroupListModel();

        GroupInfo group = null;
        Object[] values = groupList.getSelectedValues();
        if (values.length < 1) {
            return null;
        }

        if (values.length == 1) {
            group = (GroupInfo) values[0];
        }

        JMenuItem item;
        if (group != null) {
            ColorIcon icon = new ColorIcon(group.color, 10, 10);
            item = new JMenuItem(group.name, icon);
            //item.setAlignmentX(item.CENTER_ALIGNMENT);
            item.setEnabled(false);
        } else {
            item = getNoTopItem("multiple");
        }

        return item;
    }

    public static JMenuItem getNoTopItem(String name) {
        ColorIcon icon = new ColorIcon(Color.gray, 10, 10);
        JMenuItem item = new JMenuItem(name, icon);
        //item.setAlignmentX(item.CENTER_ALIGNMENT);
        item.setEnabled(false);
        return item;
    }

    private static int getNumSelected(PFREDContext context) {
        JList groupList = context.getUIManager().getGroupList();
        Object[] values = groupList.getSelectedValues();
        return values.length;
    }

    private static boolean oligoSelectorGroupSelected(PFREDContext context) {
        JList groupList = context.getUIManager().getGroupList();
        Object[] values = groupList.getSelectedValues();
        if (values.length != 1) {
            return false;
        }
        System.out.println("values[0].getClass()=" + values[0].getClass());
        if (values[0].getClass().toString().endsWith("OligoSelectorGroupInfo")) {
            return true;
        }

        return false;
        // return values.length;
    }
}
