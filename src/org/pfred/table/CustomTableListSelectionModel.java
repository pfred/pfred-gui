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


import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pfred.model.CustomListSelectionModel;

public class CustomTableListSelectionModel extends DefaultListSelectionModel
        implements ListSelectionListener {

    //flag used to avoid unneccessary callback from list model
    private boolean adjustingList = false;
    private CustomListSelectionModel list_model;
    private TableSorter sorter;

    public CustomTableListSelectionModel(CustomListSelectionModel list_model, TableSorter sorter) {
        super();
        this.list_model = list_model;
        list_model.addListSelectionListener(this);
        this.sorter = sorter;
    }

    public void clearSelection() {
        super.clearSelection();
        //adjustingList=true;
        //list_model.clearSelection();
        //adjustingList=false;
    }

    public void addSelectionInterval(int index0, int index1) {
        super.addSelectionInterval(index0, index1);
        //adjustingList=true;
        //syncListSelection();
        //adjustingList=false;
    }

    public void removeSelectionInterval(int index0, int index1) {
        super.removeSelectionInterval(index0, index1);
        //adjustingList=true;
        //syncListSelection();
        //adjustingList=false;
    }

    public void insertIndexInterval(int index, int length, boolean before) {
        //TODO need to be overwritten
        super.insertIndexInterval(index, length, before);
    }

    public void removeIndexInterval(int index0, int index1) {
        //TODO need to be overwritten
        super.removeIndexInterval(index0, index1);
    }

    public void setValueIsAdjusting(boolean valueIsAdjusting) {

        adjustingList = true;
        if (!valueIsAdjusting) {
            syncListSelection();
        }
        list_model.setValueIsAdjusting(valueIsAdjusting);
        adjustingList = false;
        super.setValueIsAdjusting(valueIsAdjusting);
    }

    public CustomListSelectionModel getCustomListSelectionModel() {
        return list_model;
    }

    private void syncListSelection() {
        int min = this.getMinSelectionIndex();
        int max = this.getMaxSelectionIndex();
        int selectedCount = 0;
        for (int i = min; i <= max; i++) {
            if (this.isSelectedIndex(i)) {
                selectedCount++;
            }
        }
        int[] selectedIndices = new int[selectedCount];
        int idx = 0;
        for (int i = min; i <= max; i++) {
            if (this.isSelectedIndex(i)) {
                selectedIndices[idx] = sorter.modelIndex(i);
                idx++;
            }
        }
        //sort it real quick
        java.util.Arrays.sort(selectedIndices);

        if (selectedIndices.length == 0) {
            list_model.clearSelection();
        } else {
            list_model.selectVisibleIndices(selectedIndices);
        }
    }

    public int getSelectedDataCount() {
        int count = 0;
        int min = Math.max(0, getMinSelectionIndex());
        int max = getMaxSelectionIndex();
        for (int i = min; i <= max; i++) {
            if (isSelectedIndex(i)) {
                count++;
            }
        }
        return count;

    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() != list_model) {
            return;
        }

        if (e.getValueIsAdjusting()) {
            return;
        }

        if (adjustingList) {
            return;
        }

        //Up to this point, this call much be originated from the list_model itself
        //int start = e.getFirstIndex();
        //int end = e.getLastIndex();
        int start = list_model.getMinSelectionIndex();
        int end = list_model.getMaxSelectionIndex();

        int[] selectedIndices = list_model.getSelectedVisibleIndices(start, end);
        if (selectedIndices == null || selectedIndices.length == 0) {
            super.setValueIsAdjusting(true);
            super.addSelectionInterval(0, 0); //triger some changes
            super.clearSelection();
            super.setValueIsAdjusting(false);
            //super.fireValueChanged(false); //force selection change event
            return; //nothing changed in the visible selections
        }

        //System.out.println("TableListSelectionModel beforeValueChanging "
        //                   +System.currentTimeMillis());
        super.setValueIsAdjusting(true);
        //first clear current selections within the range
        //super.removeSelectionInterval(selectedIndices[0], selectedIndices[length-1]);
        super.clearSelection();

        //now select the ones in the range
        //System.out.println("TableListSelectionModel afterValueChanging2 "
        //                   +System.currentTimeMillis());
        int t_start = selectedIndices[0];
        int t_end = selectedIndices[0];
        for (int i = 1; i < selectedIndices.length; i++) {
            //int viewIdx = sorter.viewIndex(selectedIndices[i]);
            //super.addSelectionInterval(viewIdx, viewIdx);
            //super.addSelectionInterval(selectedIndices[i], selectedIndices[i]);

            if (selectedIndices[i] == selectedIndices[i - 1] + 1) {
                t_end = selectedIndices[i];//continuous;
                continue;
            }
            super.addSelectionInterval(t_start, t_end);
            t_start = t_end = selectedIndices[i];
        }
        super.addSelectionInterval(t_start, t_end); //select the last batch
        //System.out.println("TableListSelectionModel afterValueChanging3 "
        //                  +System.currentTimeMillis());
        super.setValueIsAdjusting(false);
        //System.out.println("TableListSelectionModel afterValueChanging "
        //                  +System.currentTimeMillis());
    }
}
