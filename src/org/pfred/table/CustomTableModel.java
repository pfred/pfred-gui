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
import java.util.HashMap;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.pfred.PFREDConstant;
import org.pfred.PFREDContext;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.Datum;
import org.pfred.property.PropertyDisplayListener;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.property.PropertyDisplayOption;
import org.pfred.property.PropertyDisplayOptionEvent;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;

public class CustomTableModel extends AbstractTableModel
        implements ListDataListener, PropertyDisplayListener {

    CustomListModel list_model = null;
    String[] defaultHeader = {"Id"};
    PropertyDisplayManager displayManager = new PropertyDisplayManager();
    Class idclass = String.class;
    boolean contentsChanging = false;
    private PFREDContext context;

    public CustomTableModel(PFREDContext context, CustomListModel list_model) {
        this.context = context;
        this.list_model = list_model;
        list_model.addListDataListener(this);
        displayManager.addPropertyDisplayListener(this);
    }

    public PropertyDisplayManager getPropertyDisplayManager() {
        return displayManager;
    }

    public PropertyDisplayOption getPropertyDisplayOption(int column) {
        if (column < 1) {
            return null;
        }
        return displayManager.getDisplayedOption(column - 1);

    }

    public CustomListModel getCustomListModel() {
        return list_model;
    }

    public void setIDClass(Class id_class) {
        idclass = id_class;
    }

    public int getRowCount() {
        return list_model.getVisibleDataCount();
    }

    public int getColumnCount() {
        //2 extra columns one for name, the other for structure
        //return list_model.getPropertyCount() +2 ; //need to do a filter on this
        return displayManager.getDisplayedCount() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        //first translate the columnIndex to propName
        int trueRowIndex = list_model.visibleIndexToSortedIndex(rowIndex); //this need to be optimized
        Datum datum = list_model.getDatum(trueRowIndex);

        if (columnIndex == 0) {
            return datum.getName();
        }

        PropertyDisplayOption option = displayManager.getDisplayedOption(columnIndex - 1);

        Object value = datum.getProperty(option.name);
        CustomCellRenderer customCellRenderer = context.getUIManager().getOligoTablePane().getRNANotationCellRenderer(option.name);
        if (customCellRenderer.getDisplayMode() == CustomCellRenderer.COLEY_DISPLAY_MODE) {
            if (value.getClass().toString().endsWith("RNAPolymer")) {
                return ((RNAPolymer) value).getColeyNotation();
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    public Datum getRow(int rowIndex) {
        //first translate the columnIndex to propName
        int trueRowIndex = list_model.visibleIndexToSortedIndex(rowIndex); //this need to be optimized
        Datum datum = list_model.getDatum(trueRowIndex);
        return datum;
    }

    public String getColumnName(int columnIndex) {

        if (columnIndex == 0) {
            return defaultHeader[columnIndex];
        }
        PropertyDisplayOption option = (PropertyDisplayOption) displayManager.getDisplayedOption(columnIndex - 1);
        return option.shortName;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public Class getColumnSortingClass(int columnIndex) {
        if (columnIndex == 0) {
            if (idclass == null) {
                guessIDClass();
            }
            return idclass;
        }
        PropertyDisplayOption property = displayManager.getDisplayedOption(columnIndex - 1);
        return property.getTypeClass();

    }

    private void guessIDClass() {
        //estimate how many of the ids are numeric
        ArrayList data = list_model.getAllData();
        int size = data.size();
        int numericCount = 0;
        for (int i = 0; i < size; i++) {
            String id = ((Datum) data.get(i)).getName().trim();
            try {
                Double.parseDouble(id);
                numericCount++;
            } catch (Exception ex) {//do nothing;}
            }
        }
        if (numericCount > size / 2.0f) {
            idclass = Float.class;
        } else {
            idclass = String.class;
        }
    }

    public void sort(int[] newOrder) {
        list_model.setDataIsChanging(true);
        list_model.sortVisible(newOrder);
        list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_RESORTED);
    }

    public boolean isCellEditable(int row, int col) {
        if (col >= 1) {
            PropertyDisplayOption option = displayManager.getDisplayedOption(col - 1);
            return option.isEditable();
        }
        return false;
    }

    public void setValueAt(Object aValue, int row, int col) {
        //update the property
        if (col < 1) {
            return;
        }
        PropertyDisplayOption option = displayManager.getDisplayedOption(col - 1);
        //if Chart Column do nothing
        if (!(option.name).startsWith(PFREDConstant.PROP_DISTRIBUTION)) {
            int trueRowIndex = list_model.visibleIndexToSortedIndex(row); //this need to be optimized
            Datum datum = list_model.getDatum(trueRowIndex);
            list_model.setDataIsChanging(true);
            list_model.addProperty(option.name, aValue.toString(), datum);
            list_model.setDataIsChanging(true,
                    CustomListDataEvent.TYPE_PROPERTY_UPDATED);
        }
    }

    public boolean getContentsIsChanging() {
        return contentsChanging;
    }

    /****************************************************************/
    /**
     * Sent after the indices in the index0,index1
     * interval have been inserted in the data model.
     * The new interval includes both index0 and index1.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void intervalAdded(ListDataEvent e) {
        contentsChanged(e);
    }

    /**
     * Sent after the indices in the index0,index1 interval
     * have been removed from the data model.  The interval
     * includes both index0 and index1.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void intervalRemoved(ListDataEvent e) {
        contentsChanged(e);
    }

    /**
     * Sent when the contents of the list has changed in a way
     * that's too complex to characterize with the previous
     * methods.  Index0 and index1 bracket the change.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void contentsChanged(ListDataEvent e) {
        if (e.getSource() != list_model) {
            return;
        }
        System.out.println("Debug: CustomTableModel: ListDataEvent received");

        contentsChanging = true;

        if (e instanceof CustomListDataEvent) {
            if (e.getType() != CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED &&
                    e.getType() != CustomListDataEvent.TYPE_VISIBILITY_CHANGED) {
                this.fireTableChanged(new TableModelEvent(this, 0, this.getRowCount(),
                        TableModelEvent.ALL_COLUMNS,
                        TableModelEvent.UPDATE));
//no need to recreate table
            } else {
                syncDisplayOption();
                this.fireTableChanged(new CustomTableModelEvent(this, 0, this.getRowCount(),
                        TableModelEvent.ALL_COLUMNS,
                        CustomTableModelEvent.CUSTOM_TYPE)); //use ALL_COLUMNS as a custom event type
            }
            contentsChanging = false;
            return;
        }
        syncDisplayOption();
        this.fireTableChanged(new CustomTableModelEvent(this, 0, this.getRowCount(),
                TableModelEvent.ALL_COLUMNS,
                CustomTableModelEvent.CUSTOM_TYPE));
        contentsChanging = false;
    }

    public void syncDisplayOption() {
        ArrayList options = list_model.getAllProperties();
        HashMap allPropNameHash = new HashMap();

        //1. check if any property added ;
        int size = options.size();
        for (int i = 0; i < size; i++) {
            PropertyDisplayOption option = (PropertyDisplayOption) options.get(i);
            String name = option.name;
            allPropNameHash.put(name, "");
            if (displayManager.hasPropertyDisplayOption(name)) {
                continue;
            }
            //new added prop
            PropertyDisplayOption newoption = new PropertyDisplayOption(name);
            //newoption.type=option.type;
            //newoption.uniqName=name;

            newoption.copy(option);
            newoption.shortName = option.name;

            if (newoption.name.startsWith(PFREDConstant.REP_PREFIX)) //temp code. It shouldn't be here. in order not to display Rep_ properties by default cluster table.
            {
                displayManager.addAvailableOption(newoption, false);
            } else {
                displayManager.addAvailableOption(newoption, true);
            }
        }

        //2. remove property that is no long in the available list
        for (int i = 0; i < displayManager.getAvailableCount();) {
            PropertyDisplayOption option = displayManager.getAvailableOption(i);
            if (allPropNameHash.containsKey(option.name)) {
                i++;
                continue;
            }
            //don't remove derived property though
      /* if (option.isAggregate()){
            //this is table specific, you can only remove it from table display properties dialog, manually
            i++;
            continue;
            }*/
            //remove it
            displayManager.removeAvailableOption(option);
        }
    }

    /****************************PropertyDisplayListener **************/
    public void displayedPropertyChanged(PropertyDisplayOptionEvent evt) {
        //notify the listeners that we have something changed
        this.fireTableChanged(new TableModelEvent(this, 0, this.getRowCount(), 0,
                CustomTableModelEvent.CUSTOM_TYPE));
    }

    public void clear() {
        displayManager.clearAll();
        idclass = null;
    }
}
