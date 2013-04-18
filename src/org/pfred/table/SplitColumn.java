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

import java.util.ArrayList;

import javax.swing.JTable;

import org.pfred.PFREDContext;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.Oligo;
import org.pfred.property.PropertyDisplayManager;

public class SplitColumn {

    PFREDContext context;

    public SplitColumn(PFREDContext context) {
        this.context = context;
    }

    // get default options for this column...
    public SplitColumnOptions getDefaultOptions(String splitColumn) {
        SplitColumnOptions options = new SplitColumnOptions();
        options.setSplitColumn(splitColumn);

        // ok scan data to see what is the best default for this...

        return options;
    }

    // ok do an actaul split of all values in column..
    public void splitColumn(SplitColumnOptions options) {
        CustomListModel list_model = context.getDataStore().getOligoListModel();

        // gaaah - this is not the greatest way to get this info
        // i spose it should be passed in...
        CustomTablePanel customTablePanel = context.getUIManager().getOligoTablePane();
        JTable table = customTablePanel.getTable();
        TableSorter sorter = (TableSorter) table.getModel();
        CustomTableModel model = (CustomTableModel) sorter.getTableModel();
        PropertyDisplayManager displayManager = model.getPropertyDisplayManager();

        String columnName = options.getSplitColumn();
        String leftColumnName = "L_" + columnName;
        String rightColumnName = "R_" + columnName;

        list_model.setDataIsChanging(true);

        // ok now actaully split off the data from each datum and add to new column...
        ArrayList mols = list_model.getAllData();

        for (int i = 0; i < mols.size(); i++) {
            splitColumn(options, (Oligo) mols.get(i), columnName, leftColumnName, rightColumnName);
        }

        list_model.updatePropertyNames(); // add the new ones...

        // turns out this part is not really even necessary as prop display manager handles all ordering....
        // next tell PropertyManager about the new ordering of the props we added...
        //if (options.hasLeft())
        //    cmpd_model.moveProperty(leftColumnName, columnName, true);
        //if (options.hasRight())
        //    cmpd_model.moveProperty(rightColumnName, columnName, false);

        // ok finally we are done trigger that there is a new number of properties...
        list_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

        // next tell PropertyDisplayManager about the new ordering of the props we added...
        if (options.hasLeft()) {
            displayManager.moveProperty(leftColumnName, columnName, true);
        }

        if (options.hasRight()) {
            displayManager.moveProperty(rightColumnName, columnName, false);
        }
    }

    public void splitColumn(SplitColumnOptions options, Oligo mol, String columnName, String leftColumnName, String rightColumnName) {
        // ok for now we have simplistic split...we just look at SPACES...
        String oldProp = (String) mol.getProperty(columnName);
        if (oldProp == null) {
            return;
        }

        String prop = null;
        String leftProp = null;
        String rightProp = null;

        int firstNumeric = findFirstNumeric(oldProp);
        if (firstNumeric < 0) {
            prop = oldProp;

            if (options.hasLeft()) {
                leftProp = "";
            }
            if (options.hasRight()) {
                rightProp = "";
            }
        } else {
            // ok there is a numeric....
            int pastNumeric = findPastNumeric(oldProp, firstNumeric);

            // if there is no left then we grab all of numeric...
            if (options.getSplitType() == SplitColumnOptions.SplitLeftAndRight) {
                leftProp = oldProp.substring(0, firstNumeric).trim();
                prop = oldProp.substring(firstNumeric, pastNumeric);
                rightProp = oldProp.substring(pastNumeric).trim();
            } else if (options.getSplitType() == SplitColumnOptions.SplitLeft) {
                leftProp = oldProp.substring(0, firstNumeric).trim();
                prop = oldProp.substring(firstNumeric).trim();
            } else {
                prop = oldProp.substring(0, pastNumeric);
                rightProp = oldProp.substring(pastNumeric).trim();
            }
        }

        if (leftProp != null) {
            mol.setProperty(leftColumnName, leftProp);
        }
        if (prop != null) {
            mol.setProperty(columnName, prop); // this is what the old column becomes...
        }
        if (rightProp != null) {
            mol.setProperty(rightColumnName, rightProp);
        }
    }

    // return index of first char of numeric in string or -1 for none
    private int findFirstNumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.' || c == '-' || (c >= '0' && c <= '9')) {
                return i;
            }
        }

        return -1;
    }

    // is a numeric char other than first char...
    private boolean isNonFirstNumeric(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }

        if (c == '.' || c == 'e' || c == 'E') {
            return true;
        }

        return false;
    }

    // given start of numeric in string...return index after last part of numeric
    private int findPastNumeric(String s, int start) {
        int index;
        for (index = start + 1; index < s.length(); index++) {
            // if we hit a non number character stop we have found the
            // index past the last numeric...
            if (!isNonFirstNumeric(s.charAt(index))) {
                break;
            }
        }

        return index;
    }
}
