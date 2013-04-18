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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.pfred.property.PropertyDisplayDialog;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.sort.SortCompoundsMultiPropDialog;
import org.pfred.table.CustomTableModel;
import org.pfred.table.CustomTablePanel;


public class ViewActionHandler implements ActionListener {

    PFREDContext context;
    JFrame parent;

    public ViewActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();

            if (name.equals("cmpdTablePropertyDisplayOption")) {
                CustomTableModel cmpd_table = context.getDataStore().getOligoTableModel();
                showDisplayPropertyDialog(cmpd_table.getPropertyDisplayManager(),
                        "Display Properties in Compound Table");
            } else if (name.equals("screenshot")) {
                screenshot();
            } else if (name.equals("sortCompounds")) {
                sortOligoByProperty();
            } else if (name.equals("configCompoundTableColumns")) {
                configCompoundTableColumns();
            }


        }
    }

    public void configCompoundTableColumns() {
    }

    public void showDisplayPropertyDialog(PropertyDisplayManager manager,
            String title) {

        PropertyDisplayDialog dialog = new PropertyDisplayDialog(parent, title,
                manager);
        //dialog.show();
    }

    public void screenshot() {


        CustomTablePanel table = context.getUIManager().getOligoTablePane();
        if (table != null && table.isShowing()) {
            table.makeScreenShot();
            return;
        }

    }

    public void sortOligoByProperty() {
        // sort compounds by property values

        //SortCompoundsDialog dialog = new SortCompoundsDialog(context, parent);
        SortCompoundsMultiPropDialog dialog = new SortCompoundsMultiPropDialog(context, parent);

        dialog.setVisible(true);

    }

    public static JMenu getOligoTableTopMenu(PFREDContext context) {
        JMenu menu = new JMenu("Oligo Table");
        JMenuItem mi;
        menu.add(mi = new JMenuItem("Display Properties..."));
        mi.setName("cmpdTablePropertyDisplayOption");
        mi.addActionListener(context.getUIManager().getViewActionHandler());

        context.getUIManager().getOligoTablePane().addCustomPopupMenu(context, menu);
        return menu;
    }
}
