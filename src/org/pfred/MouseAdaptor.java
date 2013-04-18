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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JTabbedPane;

import org.pfred.group.GroupListPopupMenu;
import org.pfred.model.CustomListSelectionModel;

public class MouseAdaptor implements MouseListener {

    PFREDContext context;

    public MouseAdaptor(PFREDContext context) {
        this.context = context;
    }

    public void mouseClicked(MouseEvent e) {
        Object src = e.getSource();
        //first process right click
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            if (e.getSource() == context.getUIManager().getGroupList()) {
                GroupListPopupMenu.showPopup(context, context.getUIManager().getGroupList(), e.getX(), e.getY());
            }
        } //process single click
        else if (src instanceof Component) {
            String name = ((Component) src).getName();
            if (name == null) {
                return;
            }
            if (name.equals("groupListLabel")) {
                JList groupList = context.getUIManager().getGroupList();
                groupList.setValueIsAdjusting(true);
                groupList.clearSelection();
                groupList.setValueIsAdjusting(false);
            } else if (e.getClickCount() > 1) {
                if (name.equals("groupList")) {
                    //select cmpds in the cluster
                    GroupActionHandler handler = context.getUIManager().getGroupActionHandler();
                    handler.selectCompoundsGroup();
                }
            } else if (name.equals("mainTabbedPane")) {
                JTabbedPane mainTabbedPane = context.getUIManager().getMainTabbedPane();
                //make sure it not click on the tabs
                Point p = e.getPoint();
                if (p.y < 25 && p.x < 380) {
                    return;
                }

                int idx = mainTabbedPane.getSelectedIndex();
                if (idx == 0 || idx == 1) {

                    CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
                    sel_model.setValueIsAdjusting(true);
                    sel_model.clearSelection();
                    sel_model.setValueIsAdjusting(false);

                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
