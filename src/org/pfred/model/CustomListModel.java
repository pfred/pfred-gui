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
package org.pfred.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.pfred.PFREDConstant;
import org.pfred.property.PropertyDisplayOption;

public class CustomListModel extends AbstractListModel {

    private PropertyManager propertyManager = new PropertyManager();
    private int[] visibleToSorted = null;
    private int[] sortedToVisible = null;
    private int totalInvisible = 0; //since this is used over and over again we cache it here
    private ArrayList sorted = new ArrayList();
    private Hashtable names = new Hashtable(); // for efficient lookup
    /**
     * construct the model with an array of molecules
     * @param data
     */
    private boolean dataChanging = false;

    /**
     * determines if data in list is changing
     * @return true if data is changing
     */
    public boolean getDataIsChanging() {
        return dataChanging;
    }

    /**
     * When dataIsChanging is set to false, event gets triggered. If it is only PropertyChange
     * CompoundPropertyChangeEvent will be fired instead of the ListDataEvent. This gives us some
     * flexibility for the type of event we want
     *
     * @param changing boolean
     * @param onlyPropertyChange boolean
     */
    public void setDataIsChanging(boolean changing, String propName) {
        dataChanging = changing;
        if (dataChanging == false) {
            //trigger content changed event
            if (propName == null || propName.length() == 0) {
                fireContentsChanged();
            } else {
                firePropertyChanged(propName);
            }
        }
    }

    public void setDataIsChanging(boolean changing, String propName, int min_idx, int max_idx) {
        dataChanging = changing;
        if (dataChanging == false) {
            //trigger content changed event
            if (propName == null || propName.length() == 0) {
                fireContentsChanged();
            } else {
                firePropertyChanged(min_idx, max_idx, propName);
            }
        }
    }

    /**
     * Call this when data added or removed.
     * @param changing
     */
    public void setDataIsChanging(boolean changing) {
        dataChanging = changing;
        if (dataChanging == false) {
            //trigger content changed event
            syncVisibleSortedIndices();
            fireContentsChanged();

        }
    }

    public void setDataIsChanging(boolean changing, int type) {
        dataChanging = changing;
        if (dataChanging == false) {
            //trigger content changed event
            if (type == CustomListDataEvent.TYPE_VISIBILITY_CHANGED
                    || type == CustomListDataEvent.TYPE_RESORTED
                    || this.getVisibleDataCount() != visibleToSorted.length) {
                syncVisibleSortedIndices();
            }
            fireContentsChanged(type, 0, sorted.size() - 1);

        }

    }

    public void setDataIsChanging(boolean changing, int type, int idx0, int idx1) {
        dataChanging = changing;
        if (dataChanging == false) {
            //trigger content changed event
            if (type == CustomListDataEvent.TYPE_VISIBILITY_CHANGED
                    || type == CustomListDataEvent.TYPE_RESORTED
                    || this.getVisibleDataCount() != visibleToSorted.length) {
                syncVisibleSortedIndices();
            }
            fireContentsChanged(type, idx0, idx1);

        }

    }

    public CustomListModel() {
    }

    public PropertyManager getPropertyManager() {
        return propertyManager;
    }

    public void renameDatum(String curr_name, String newname) {
        if (!names.containsKey(curr_name)) {
            return;
        }
        Datum datum = (Datum) names.remove(curr_name);
        datum.setName(newname);
        names.put(newname, datum);
    }

    public void addData(Datum datum) {
        if (names.containsKey(datum.getName())) {
            return;
        }
        names.put(datum.getName(), datum);
        sorted.add(datum);

        if (datum.isInvisible()) {
            totalInvisible++;
        }

    }

    public void addData(Datum datum, int atIndex) {
        if (atIndex > sorted.size()) {
            return;
        }
        if (names.containsKey(datum.getName())) {
            return;
        }
        names.put(datum.getName(), datum);
        sorted.add(atIndex, datum);

        if (datum.isInvisible()) {
            totalInvisible++;
        }
    }

    /**
     * this can be and might need to be optimized
     *
     * @param datum
     */
    public void removeData(Datum datum) {
        if (!names.containsKey(datum.getName())) {
            return;
        }
        sorted.remove(datum);
        names.remove(datum.getName());
        if (datum.isInvisible()) {
            totalInvisible--;
        }

    }

    private void syncInvisible() {
        totalInvisible = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Datum tmp_datum = (Datum) sorted.get(i);
            if (tmp_datum.isInvisible()) {
                totalInvisible++;
            }
        }
    }

    public void addData(ArrayList data) {
        // we must increase the size of the sorted array
        for (Iterator iter = data.iterator(); iter.hasNext();) {
            Datum datum = (Datum) iter.next();
            if (names.containsKey(datum.getName())) {
                continue;
            }
            sorted.add(datum);
            names.put(datum.getName(), datum);
        }
        syncInvisible();
    }

    /**
     * entry point for constructing the model
     * @param data ArrayList
     */
    public void setData(ArrayList data) {
        sorted = data;
        names = new Hashtable(sorted.size());
        for (Iterator iter = data.iterator(); iter.hasNext();) {
            Datum datum = (Datum) iter.next();
            names.put(datum.getName(), datum);

        }
        syncInvisible();
        //updatePropertyNames();
        //syncListModel();
    }

    public void removeData(ArrayList data) {
        //to improve performance on large dataset do the following
        //1. create a hash table
        int size = data.size();
        String tmp = "";
        HashMap hash = new HashMap(size);
        for (int i = 0; i < size; i++) {
            hash.put(data.get(i), tmp);
        }

        size = sorted.size();
        for (int i = size - 1; i >= 0; i--) {
            if (hash.get(sorted.get(i)) != null) {
                Datum datum = (Datum) sorted.get(i);
                names.remove(datum.getName());
                sorted.remove(i);
            }

        }
        syncInvisible();
        //syncListModel();
    }

    public void removeData(String[] names) {
        ArrayList data = getData(names);
        removeData(data);
    } //provide extra mapping for names to idx

    public void clear() {
        sorted = new ArrayList();
        totalInvisible = 0;
        names = new Hashtable();

        getPropertyManager().clearAll();
    }

    public ArrayList getAllData() {
        return sorted;
    }

    public ArrayList getAllVisibleData() {
        ArrayList visible = new ArrayList();
        for (int i = 0; i < sorted.size(); i++) {
            Datum datum = (Datum) sorted.get(i);
            if (!datum.isInvisible()) {
                visible.add(datum);
            }
        }
        return visible;
    }

    public Datum getDatum(String name) {
        if (names.containsKey(name)) {
            return (Datum) names.get(name);
        }
        return null;
    }

    public Datum getDatum(int idx) {
        if (idx >= sorted.size()) {
            return null;
        }
        return (Datum) sorted.get(idx);
    }

    public boolean hasDatum(String name) {
        return names.containsKey(name);
    }

    public ArrayList getData(String[] l_names) {
        return getData(l_names, false);
    }

    public ArrayList getData(String[] l_names, boolean printError) {
        ArrayList data = new ArrayList();
        for (int i = 0; i < l_names.length; i++) {
            if (l_names[i] == null) {
                continue;
            }

            Object tmp = names.get(l_names[i]);
            if (tmp != null) {
                data.add(tmp);
            } else {
                if (printError) {
                    System.err.println(l_names[i] + " not found in CustomListModel");
                }
            }
        }
        return data;
    } //use hashtable names to improve speed

    public Datum getVisibleDatum(int idx) {
        int curr = -1;
        for (int i = 0; i < sorted.size(); i++) {
            Datum datum = (Datum) sorted.get(i);
            if (!datum.isInvisible()) {
                curr++;
            }
            if (curr == idx) {
                return datum;
            }
        }
        return null;
    }

    public ArrayList getVisibleData(int startVisibleIndex, int count) {
        ArrayList data = new ArrayList();
        int curr = -1;
        int size = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Datum datum = (Datum) sorted.get(i);
            if (!datum.isInvisible()) {
                curr++;
                if (curr >= startVisibleIndex) {
                    data.add(datum);
                    size++;
                    if (size == count) {
                        return data;
                    }
                }
            }
        }
        return data;
    }

    public int getVisibleDataCount() {
        return sorted.size() - totalInvisible;
    }

    public ArrayList getVisibleData() {
        int size = sorted.size();
        ArrayList visibles = new ArrayList();
        for (int i = 0; i < size; i++) {
            Datum datum = (Datum) sorted.get(i);
            if (!datum.isInvisible()) {
                visibles.add(datum);
            }
        }
        return visibles;
    }

    public int visibleIndexToSortedIndex(int visibleIndex) {
        if (visibleIndex >= visibleToSorted.length) {
            return -1;
        }
        return visibleToSorted[visibleIndex];
    }

    public int sortedIndexToVisibleIndex(int sortedIndex) {
        if (sortedIndex < 0 || sortedIndex >= sorted.size()) {
            return -1;
        }
        return sortedToVisible[sortedIndex];
    }

    public int indexOf(String name) {
        Datum d = (Datum) names.get(name);
        return indexOf(d);
    }

    public int indexOf(Datum datum) {
        int size = sorted.size();
        for (int i = 0; i < size; i++) {
            if (datum == sorted.get(i)) {
                return i;
            }
        }
        return -1;
    }  //slow, don't use it for large loops, concern about the speed

    public int size() {
        return sorted.size();
    }

    public boolean isVisible(int idx) {
        int size = sorted.size();
        if (idx >= size) {
            return false;
        }
        Datum datum = (Datum) sorted.get(idx);

        return !datum.isInvisible();
    }

    public boolean isVisible(String name) {
        if (!names.containsKey(name)) {
            return false;
        }
        Datum datum = (Datum) names.get(name);
        return !datum.isInvisible();
    }

    public boolean hasAnnotation(String name) {
        Datum datum = getDatum(name);
        if (datum == null) {
            return false;
        }
        String annotation = (String) datum.getProperty(PFREDConstant.PFRED_ANNOTATION);
        if (annotation == null || annotation.length() < 1) {
            return false;
        } else {
            return true;
        }
    }

    public void setColor(int idx, Color c) {
        int size = sorted.size();
        if (idx >= size) {
            return;
        }
        Datum datum = (Datum) sorted.get(idx);
        datum.setColor(c);
    } //use hashtable for faster structure retrieval and notify the change after it

    public void setColor(ArrayList data, Color c) {
        for (int i = 0; i < data.size(); i++) {
            Datum datum = (Datum) data.get(i);
            datum.setColor(c);
        }
    }

    public void clearColor() {
        Datum datum = null;
        ArrayList data = getAllData();
        for (int i = 0; i < data.size(); i++) {
            datum = (Datum) data.get(i);
            datum.setColor(null);
        }
    }

    public void setVisible(int idx, boolean isVisible) {
        int size = sorted.size();
        if (idx >= size) {
            return;
        }
        Datum datum = (Datum) sorted.get(idx);

        datum.setInvisible(!isVisible);
        syncInvisible();

    }

    public void setVisible(ArrayList data, boolean isVisible) {
        for (int i = 0; i < data.size(); i++) {
            Datum datum = (Datum) data.get(i);
            datum.setInvisible(!isVisible);
        }
        syncInvisible();
    }

    public void setVisibleOnly(ArrayList data) {

        for (int i = 0; i < sorted.size(); i++) {
            Datum datum = (Datum) sorted.get(i);
            datum.setInvisible(true);
        }

        for (int i = 0; i < data.size(); i++) {
            Datum datum = (Datum) data.get(i);
            datum.setInvisible(false);
        }

        totalInvisible = sorted.size() - data.size();
    }

    public void setAllVisible() {
        for (int i = 0; i < sorted.size(); i++) {
            Datum datum = (Datum) sorted.get(i);
            datum.setInvisible(false);
        }
        totalInvisible = 0;
    }

    public void removeProperty(String name) {

        Iterator iter = sorted.iterator();
        while (iter.hasNext()) {
            Datum datum = (Datum) iter.next();
            datum.removeProperty(name);
        }
    }

    public void removeProperties(String[] propNames) {
        for (int i = 0; i < propNames.length; i++) {
            Iterator iter = sorted.iterator();
            while (iter.hasNext()) {
                Datum datum = (Datum) iter.next();
                datum.removeProperty(propNames[i]);
            }
        }
    }

    public void addProperty(String name, String value, Datum datum) {
        datum.setProperty(name, value);
    }

    public void addProperty(String name, String value, Datum datum, boolean first) {
        datum.setProperty(name, value, first);
    }

    public String[] getAllPropertyNames() {
        return getPropertyManager().getAllPropertyNames();
    }

    public String getPropertyName(int i) {
        return getPropertyManager().getPropertyName(i);
    }

    public ArrayList getAllProperties() {
        return getPropertyManager().getAllProperties();
    }

    public String[] getAllPropertyNames(boolean excludeDerived) {
        if (!excludeDerived) {
            return getAllPropertyNames();
        }

        String[] allPropNames = getPropertyManager().getAllPropertyNames();
        ArrayList regularProps = new ArrayList();
        for (int i = 0; i < allPropNames.length; i++) {
            PropertyDisplayOption opt = getPropertyManager().getProperty(allPropNames[i]);
            if (opt == null || opt.isDerivedProp()) {
                continue;
            }
            regularProps.add(allPropNames[i]);
        }
        int size = regularProps.size();
        String[] propNames = new String[size];
        for (int i = 0; i < propNames.length; i++) {
            propNames[i] = (String) regularProps.get(i);
        }
        return propNames;
    }

    public String[] getAllPropertyNames(int type) {
        String[] allPropNames = getPropertyManager().getAllPropertyNames();
        ArrayList regularProps = new ArrayList();
        for (int i = 0; i < allPropNames.length; i++) {
            PropertyDisplayOption opt = getPropertyManager().getProperty(allPropNames[i]);
            if (opt == null || opt.isDerivedProp() || opt.type != type) {

                continue;
            }
            regularProps.add(allPropNames[i]);
        }
        int size = regularProps.size();
        String[] propNames = new String[size];
        for (int i = 0; i < propNames.length; i++) {
            propNames[i] = (String) regularProps.get(i);
        }
        return propNames;
    }

    public boolean hasProperty(String name) {
        return getPropertyManager().hasProperty(name);
    }

    public PropertyDisplayOption getProperty(int i) {
        return getPropertyManager().getProperty(i);
    }

    public PropertyDisplayOption getProperty(String name) {
        return getPropertyManager().getProperty(name);
    }

    public int getPropertyCount() {
        return getPropertyManager().getPropCount();
    }

    public void renameProperty(String oldName, String newName) {
        //first rename the properties
        int size = sorted.size();
        for (int i = 0; i < size; i++) {
            Datum datum = (Datum) sorted.get(i);
            Object value = datum.getProperty(oldName);
            if (value != null) {
                datum.removeProperty(oldName);
                datum.setProperty(newName, value);
            }
        }
        updatePropertyNames();
    }

    public void updatePropertyNames() {
        getPropertyManager().updatePropertyNames();
    }

    public String propertyOptionsToString() {
        return getPropertyManager().optionsToString();
    }

    public void propertyOptionsFromString(String optString) {
        getPropertyManager().optionsFromString(optString);
    }

    public void addDerivedProp(String name, String displayName) { //all derived properties are numeric
        getPropertyManager().addProperty(name, displayName,
                PropertyDisplayOption.NUMERIC,
                true);
    }

    public void addDerivedProp(String name, String displayName,
            Object derivedRules) { //all derived properties are numeric
        getPropertyManager().addProperty(name, displayName,
                PropertyDisplayOption.NUMERIC,
                true);
        getPropertyManager().getProperty(name).derivedRules = derivedRules;
    }

    public void addDerivedProp(String name, String displayName, int type,
            Object derivedRules) { //all derived properties are numeric except aggregates
        getPropertyManager().addProperty(name, displayName,
                type,
                true);
        getPropertyManager().getProperty(name).derivedRules = derivedRules;
    }

    public void removeDerivedProp(String name) {
        getPropertyManager().removeProperty(name);
    }

    public void removeDerivedProp(String[] name) {
        for (int i = 0; i < name.length; i++) {
            getPropertyManager().removeProperty(name[i]);
        }
    }

    /**
     * returns the start and end indices for the molecules in the input
     * @param mols
     * @return
     */
    public int[] getStartEndIndexForData(ArrayList data) {
        HashMap names = new HashMap(data.size());
        int size = data.size();
        for (int i = 0; i < size; i++) {
            names.put(data.get(i), "");
        }

        size = sorted.size();
        int[] indices = new int[2];
        boolean found = false;
        for (int i = 0; i < size; i++) {
            if (names.containsKey(sorted.get(i))) {
                indices[0] = i;
                found = true;
                break;
            }
        }

        for (int i = size - 1; i >= 0; i--) {
            if (names.containsKey(sorted.get(i))) {
                indices[1] = i;
                break;
            }
        }
        if (found) {
            return indices;
        }
        return null;
    }

    public int[] getSortedIndicesForData(ArrayList data) {

        HashMap dataHash = new HashMap(data.size());

        for (int i = 0; i < data.size(); i++) {
            dataHash.put(data.get(i), "");
        }
        int[] indices = new int[dataHash.size()]; //unique size

        int counter = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (dataHash.containsKey(sorted.get(i))) {
                indices[counter] = i;
                counter++;
            }
        }
        return indices;
    }

    // move given clusters to TOP of list
    public void moveDataTop(ArrayList selected) {
        int[] index = new int[selected.size()];
        if (index.length < 1) {
            return;
        }

        // otherwise we want to decrease each index by one
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        forceOrder(selected, index);
    }

    // move given clusters to BOTTOM of list
    public void moveDataBottom(ArrayList selected) {

        int[] index = new int[selected.size()];
        if (index.length < 1) {
            return;
        }

        int total = sorted.size();
        // otherwise we want to decrease each index by one
        for (int i = index.length - 1; i >= 0; i--) {
            index[i] = total - index.length + i;
        }

        forceOrder(selected, index);

    }

    // force these clusters to be in this order
    public void forceOrder(ArrayList selected, int[] index) {
        // first remove all these clusters
        for (int i = 0; i < selected.size(); i++) {
            sorted.remove(selected.get(i));
        }

        // add them at the given index
        for (int i = 0; i < selected.size(); i++) {
            sorted.add(index[i], selected.get(i));
        }
    }

    // move given groups UP the list
    public void moveDataUp(ArrayList selected) {
        // get the current indexes of these groups in sorted order
        int[] index = getSortedIndicesForData(selected);
        if (index.length < 1) {
            return;
        }

        // if the first index is already at the top...do nothing
        if (index[0] < 1) {
            return;
        }

        // otherwise we want to decrease each index by one
        for (int i = 0; i < index.length; i++) {
            index[i] = index[i] - 1;
        }

        // force this order
        forceOrder(selected, index);


    }

    // move given groups DOWN the list
    public void moveDataDown(ArrayList selected) {
        // get the current indexes of these groups in sorted order
        int[] index = getSortedIndicesForData(selected);
        if (index.length < 1) {
            return;
        }

        // if the last index is already at the bottom...do nothing
        if (index[index.length - 1] >= sorted.size()) {
            return;
        }

        // otherwise we want to increase each index by one
        for (int i = 0; i < index.length; i++) {
            index[i] = index[i] + 1;
        }

        // force this order
        forceOrder(selected, index);


    }

    // set the given list to the given order
    // if there are compounds not in this ordered list, append them to the end
    // if any in list is not loaded then crap out right away
    public boolean reorderData(ArrayList data) {
        System.out.println(System.currentTimeMillis());
        // create a new list containing ALL mols...but with the given mols
        // sorted according to the new order
        ArrayList newSorted = new ArrayList(data);
        //ArrayList unSorted = new ArrayList();
        //Iterator iter = sorted.iterator();
        int size = data.size();
        HashMap hash = new HashMap(size);
        String tempString = "";
        for (int i = 0; i < size; i++) {
            hash.put(data.get(i), tempString);
        }
        System.out.println(System.currentTimeMillis());
        size = sorted.size();
        for (int i = 0; i < size; i++) {
            Object datum = sorted.get(i);
            if (!hash.containsKey(datum)) {
                newSorted.add(datum); //add it at the end
            }
        }

        sorted = newSorted;

        return true;
    }

    public void sortVisible(int[] newOrder) {
        //first get all the visible mols into an array
        ArrayList visibleData = this.getVisibleData();

        ArrayList newOrderedVisibleData = new ArrayList(newOrder.length);
        for (int i = 0; i < newOrder.length; i++) {
            newOrderedVisibleData.add(visibleData.get(newOrder[i]));
        }
        System.out.println("SortVisible " + System.currentTimeMillis());
        reorderData(newOrderedVisibleData);
        System.out.println("SortVisible Done" + System.currentTimeMillis());
    }

    public void syncVisibleSortedIndices() {
        int size = sorted.size();
        int visibleSize = getVisibleDataCount();
        visibleToSorted = new int[visibleSize];
        sortedToVisible = new int[size];
        int currVisibleIndex = -1;
        int sortedIndex = -1;
        for (int i = 0; i < size; i++) {
            Datum datum = (Datum) sorted.get(i);
            if (!datum.isInvisible()) {
                currVisibleIndex++;
                visibleToSorted[currVisibleIndex] = i;
                sortedToVisible[i] = currVisibleIndex;
            } else {
                sortedToVisible[i] = -1;
            }
        }
    }

    //*** implementation to the abstract ListModel method
    public Object getElementAt(int index) {
        if (sorted.size() <= index) {
            return null;
        }

        Object datum = sorted.get(index);
        return datum;
    }

    public int getSize() {
        return sorted.size();
    }

    public void fireContentsChanged() {
        int lastSize = sorted.size();
        updatePropertyNames(); //need to think more about this
        fireContentsChanged(this, 0, lastSize - 1);
    }

    public void fireContentsChanged(int event_type, int idx0, int idx1) {
        if (event_type == CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED) {
            updatePropertyNames();
        }

        ListDataEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        if (event_type <= -1) { //this is cutom event type
            e = new CustomListDataEvent(this, event_type, idx0, idx1);
        } else {
            e = new ListDataEvent(this, event_type, idx0, idx1);
        }
        for (int i = listeners.length - 2; i >= 0; i -= 2) { //why there are two copies of listener registered?
            if (listeners[i] == ListDataListener.class) {
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }

    }

    public void firePropertyChanged(String propertyName) {
        //this is a property update event
        int lastSize = sorted.size();
        firePropertyChanged(0, lastSize - 1, propertyName);
    }

    public void firePropertyChanged(int minIndex, int maxIndex, String propertyName) {
        // this give us a little more information to distinguish property change vs. data addition or removal
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = new CustomListDataEvent(this,
                CustomListDataEvent.TYPE_PROPERTY_UPDATED,
                minIndex, maxIndex, propertyName);

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }

    }

    public boolean moveProperty(String property, String refProperty, boolean before) {
        return getPropertyManager().moveProperty(property, refProperty, before);
    }

    // A utility class to manage properties in the data set
    public class PropertyManager {

        private HashMap allPropNames = new HashMap(); //manages the propertyDisplayOptions
        private ArrayList allProp = new ArrayList(); // use an array in order to keep the order

        public void clearAll() {
            allPropNames = new HashMap();
            allProp = new ArrayList();
        }

        public boolean moveProperty(String property, String refProperty, boolean before) {
            if (getProperty(refProperty) == null) {
                return false;
            }

            PropertyDisplayOption opt = getProperty(property);
            if (opt == null) {
                return false;
            }

            allProp.remove(opt);

            int refIndex = allProp.indexOf(getProperty(refProperty));
            int where = before ? refIndex : refIndex + 1;

            allProp.add(where, opt);

            return true;
        }

        public void updatePropertyNames() {
            HashMap propNames = new HashMap();
            //first find out all the propNames in the current file
            //and figure out which ones are new

            //allPropNames = new ArrayList();
            for (int i = 0; i < sorted.size(); i++) {
                Datum datum = (Datum) sorted.get(i);
                ArrayList keys = datum.propertyNames();
                for (int j = 0; j < keys.size(); j++) {
                    Object key = keys.get(j);
                    if (!propNames.containsKey(key)) {
                        propNames.put(key, "");
                    }
                    if (!allPropNames.containsKey(key)) {
                        int type = guessPropertyType((String) key);
                        PropertyDisplayOption newprop = new PropertyDisplayOption(key.toString(), type);
                        allPropNames.put(key, newprop);
                        allProp.add(newprop);
                    }
                }
            }

            //now we need to remove the obsolete properties from allPropNames
            ArrayList toBeRemoved = new ArrayList();
            Iterator keys = allPropNames.keySet().iterator();
            while (keys.hasNext()) {
                String name = (String) keys.next();
                PropertyDisplayOption option = (PropertyDisplayOption) allPropNames.get(name);
                if (option.isDerivedProp()) {
                    name = option.baseNameForDerivedProp();
                    if (!propNames.containsKey(name)
                            && !propNames.containsKey(PFREDConstant.REP_PREFIX + name)) { //tmp code
                        toBeRemoved.add(option.name);
                    }
                } else if (!propNames.containsKey(name)) {
                    toBeRemoved.add(option.name);
                }
            }

            for (int i = 0; i < toBeRemoved.size(); i++) {
                Object key = toBeRemoved.get(i);
                allProp.remove(allPropNames.get(key));
                allPropNames.remove(key);
            }
        }

        public int getPropCount() {
            return allProp.size();
        }

        /**
         * look through all molecules and see which one has more numeric or date
         * @param propName String
         * @return int
         */
        public int guessPropertyType(String propName) {
            ArrayList data = getAllData();
            int size = data.size();
            int numericCount = 0;
            ArrayList values = new ArrayList();
            for (int i = 0; i < size; i++) {
                Object value = ((Datum) data.get(i)).getProperty(propName);
                if (value != null) {
                    values.add(value.toString());
                }
            }
            int type = PropertyDisplayOption.guessType(values);
            return type;
        }

        public String[] getAllPropertyNames() {
            int size = allProp.size();
            String[] names = new String[size];
            for (int i = 0; i < size; i++) {
                PropertyDisplayOption property = (PropertyDisplayOption) allProp.get(i);
                names[i] = property.name;
            }
            return names;
        }

        public ArrayList getAllProperties() {
            return allProp;
        }

        public boolean hasProperty(String name) {
            return allPropNames.containsKey(name);
        }

        public PropertyDisplayOption getProperty(int i) {
            return (PropertyDisplayOption) allProp.get(i);
        }

        public PropertyDisplayOption getProperty(String name) {
            return (PropertyDisplayOption) allPropNames.get(name);
        }

        public String getPropertyName(int i) {
            PropertyDisplayOption property = (PropertyDisplayOption) allProp.get(i);
            return property.name;
        }

        public void renameProperty(String oldName, String newName) {
            if (!allPropNames.containsKey(oldName)) {
                return;
            }

            PropertyDisplayOption property = (PropertyDisplayOption) allPropNames.remove(oldName);
            property.rename(newName);
            allPropNames.put(newName, property);
        }

        public void addProperty(String name, String displayName, int type, boolean isDerived) {
            if (allPropNames.containsKey(name)) {
                return;
            }

            PropertyDisplayOption property = new PropertyDisplayOption(name,
                    displayName,
                    type, isDerived);
            allProp.add(property);
            allPropNames.put(name, property);
        }

        public void removeProperty(String name) {
            if (!allPropNames.containsKey(name)) {
                return;
            }

            PropertyDisplayOption property = (PropertyDisplayOption) allPropNames.remove(name);
            allProp.remove(property);
        }

        public void removeProperties(String[] names) {
            for (int i = 0; i < names.length; i++) {
                removeProperty(names[i]);
            }
        }

        /**
         * to help to save the options to string
         * @return
         */
        public String optionsToString() {
            StringBuffer displayOpts = new StringBuffer();
            for (int i = 0; i < allProp.size(); i++) {
                PropertyDisplayOption opt = (PropertyDisplayOption) allProp.get(i);
                if (i != 0) {
                    displayOpts.append("?");
                }

                displayOpts.append(opt.name);
                displayOpts.append(":");
                displayOpts.append(opt.shortName);

                displayOpts.append(":");
                displayOpts.append(opt.type);
                displayOpts.append(":");
                displayOpts.append(opt.isDerived);

                if (opt.derivedRules != null) {
                    displayOpts.append(":");
                    displayOpts.append(opt.derivedRules.toString());
                }

            }
            return displayOpts.toString();
        }

        public void optionsFromString(String opts) {

            StringTokenizer displayOpts = new StringTokenizer(opts, "?");
            String tmp = null;
            String name = null;
            String shortname = null;
            boolean isDerived = false;
            Object derivedRules = null;

            int type = 0;

            while (displayOpts.hasMoreTokens()) {
                tmp = displayOpts.nextToken();
                StringTokenizer stTmp = new StringTokenizer(tmp, ":");
                name = stTmp.nextToken();
                shortname = stTmp.nextToken();

                if (stTmp.hasMoreTokens()) {
                    try {
                        type = Integer.parseInt(stTmp.nextToken());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (stTmp.hasMoreTokens()) {
                    try {
                        if (stTmp.nextToken().equalsIgnoreCase("true")) {
                            isDerived = true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (stTmp.hasMoreTokens()) {
                    try {
                        derivedRules = stTmp.nextToken();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                PropertyDisplayOption opt = (PropertyDisplayOption) this.getProperty(name);
                if (opt == null) //add the new display property
                {
                    addProperty(name, shortname, type, isDerived);
                    getProperty(name).derivedRules = derivedRules;
                } else {//just update the info
                    opt.shortName = shortname;
                    opt.type = type;
                    opt.isDerived = isDerived;
                    opt.derivedRules = derivedRules;
                }
            }
        }
    }
}
