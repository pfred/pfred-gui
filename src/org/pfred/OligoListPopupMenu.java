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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import java.util.ArrayList;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Oligo;
import org.pfred.util.ColorIcon;

public class OligoListPopupMenu extends JPopupMenu {

    PFREDContext context;

    public OligoListPopupMenu(PFREDContext context, Component invoker, int x, int y, JMenuItem topItem, JMenu topMenu) {
        super();

        this.context = context;

        OligoActionHandler handler = context.getUIManager().getOligoActionHandler();

        // assume right click...so throw up menu

        JMenuItem item;

        if (topItem != null) {
            //topItem.setEnabled(false);
            add(topItem);
            //topItem.setName("toggleGroup");
            //topItem.addActionListener(this);
            add(new JSeparator());
        }

        boolean oneSelected = getNumSelected(context) == 1;
        boolean someSelected = getNumSelected(context) >= 1;

        JMenu menu = new JMenu("Copy");
        add(menu);
        //menu.setEnabled(oneSelected);

        item = new JMenuItem("Copy ID xlist");
        item.setName("copyToClipboard");
        item.addActionListener(context.getUIManager().getSelectionActionHandler());
        //item.setEnabled(oneSelected);
        menu.add(item);

        item = new JMenuItem("Copy data");
        item.setName("copyData");
        item.addActionListener(context.getUIManager().getOligoActionHandler());
        //item.setEnabled(oneSelected);
        menu.add(item);


        menu = new JMenu("Move");
        menu.setEnabled(someSelected);
        add(menu);

        item = new JMenuItem("Move to Top");
        item.setName("moveCompoundTop");
        item.addActionListener(handler);
        menu.add(item);
        item = new JMenuItem("Move Up");
        item.setName("moveCompoundUp");
        item.addActionListener(handler);
        menu.add(item);
        item = new JMenuItem("Move Down");
        item.setName("moveCompoundDown");
        item.addActionListener(handler);
        menu.add(item);
        item = new JMenuItem("Move to Bottom");
        item.setName("moveCompoundBottom");
        item.addActionListener(handler);
        menu.add(item);

        item = new JMenuItem("Delete...");
        item.setName("deleteCompound");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);


        addSeparator();
        JMenu sortMenu = new JMenu("Sort Oligos");
        add(sortMenu);
        sortMenu.add(item = new JMenuItem("Sort Oligo by Properties..."));
        item.setName("sortCompounds");
        item.addActionListener(context.getUIManager().getViewActionHandler());

        add(new JSeparator());
        item = new JMenuItem("Annotate...");
        item.setName("annotateCompound");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);

        item = new JMenuItem("Flag...");
        item.setName("flagSelection");
        item.addActionListener(handler);
        item.setEnabled(someSelected);
        add(item);



        if (topMenu != null) {
            addSeparator();
            add(topMenu);
        }



        pack();
    }

    public static void showPopup(PFREDContext context, Component invoker, int x, int y, JMenu topMenu) {
        OligoListPopupMenu popup = new OligoListPopupMenu(context, invoker, x, y, getTopItem(context), topMenu);

        double tmp1 = popup.getBounds().getHeight();
        tmp1 = popup.getPreferredSize().getHeight();
        if (y > invoker.getHeight() - tmp1) {
            y = (int) (invoker.getHeight() - tmp1);
        }
        //popup.setLocation(x,y);
        popup.show(invoker, x + 10, y);
    }

    private static int getNumSelected(PFREDContext context) {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        ArrayList selected = sel_model.getSelectedData();
        return selected.size();
    }

    // get menu item representing currently selected group(s)
    public static JMenuItem getTopItem(PFREDContext context) {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();

        Oligo oligo = null;
        ArrayList selected = sel_model.getSelectedData();
        if (selected.size() < 1) {
            return null;
        }

        if (selected.size() == 1) {
            oligo = (Oligo) selected.get(0);
        }

        JMenuItem item;
        if (oligo != null) {
            ColorIcon icon = new ColorIcon(oligo.getColor(), 10, 10);
            item = new JMenuItem(oligo.getName(), icon);
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
}
