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

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;



public class CustomListSelectionModel extends DefaultListSelectionModel implements ListDataListener{

    protected CustomListModel model = null;

    /**
     * make sure the CompoundListSelecitonModel is registered as a listener to data model
     */
    public CustomListSelectionModel(CustomListModel model) {
        super();
        this.model = model;
        model.addListDataListener(this);
    }

   public int getDatumIndex(Datum datum) {
     return model.indexOf(datum);
   }

   /**
    * don't use for too often as it is not optimized for performance yet.
    * @param datum Datum
    * @return boolean
    */
   public boolean isDatumSelected(Datum datum) {
     int idx = getDatumIndex(datum);
     if (idx<0) return false;
     return isSelectedIndex(idx);
   }

   public void selectData(ArrayList data, boolean append) { //can be easily optimized
     if (data == null)
       return;

     if (!append)
       clearSelection();

     //optimized version:
     int [] indices = model.getSortedIndicesForData(data);
     if (indices ==null || indices.length==0) return;

     // we selected the first molecule first in order to trigger the selection to refresh
     // in some application this will allow it to set the current page to the page of
     // this newly selected molecule
     if (!append) {
       setValueIsAdjusting(true);
       addSelectionInterval(indices[0],indices[0]);
       setValueIsAdjusting(false);
     }

     setValueIsAdjusting(true);
     for (int i=1; i<indices.length; i++){
       addSelectionInterval(indices[i],indices[i]);
     }


     // keep selection in sync
     //syncListSelection();
   }

    public void selectDatum(Datum datum, boolean append ){

      if (datum == null)
          return;

        if (!append)
          clearSelection();

        int idx = getDatumIndex(datum);

        if (!isSelectedIndex(idx))
          addSelectionInterval(idx,idx);

        // keep selection in sync
        //syncListSelection();
    }

    public ArrayList getSelectedData(){
      ArrayList selected = new ArrayList();
      int min = Math.max(0,getMinSelectionIndex());
      int max = getMaxSelectionIndex();
      for (int i= min; i<=max; i++){
        if (isSelectedIndex(i))
          selected.add(model.getDatum(i));
      }
      return selected;
    } //speed optimization
    public int getSelectedDataCount(){
      int count=0;
      int min = Math.max(0,getMinSelectionIndex());
      int max = getMaxSelectionIndex();
      for (int i= min; i<=max; i++){
        if (isSelectedIndex(i))
         count++;
      }
      return count;

    }
    public int getLastVisibleSelectedIndex() {
      int size = model.getSize();
      for (int i=size-1; i>=0; i--) {
        if (model.isVisible(i) && isSelectedIndex(i)){
          return i;
        }
      }
      return -1;
    }

    public int getLastVisibleSelectedIndex(int beforeThis) {
      int size = Math.min(beforeThis, model.getSize());
      for (int i = size - 1; i >= 0; i--) {
        if (model.isVisible(i) && isSelectedIndex(i)) {
          return i;
        }
      }
      return -1;
    }
    public int getfirstVisibleSelectedIndex(int afterThis) {
      int start = Math.max(afterThis, 0);
      for (int i = start; i < model.getSize(); i++) {
        if (model.isVisible(i) && isSelectedIndex(i)) {
          return i;
        }
      }
      return -1;
    }

    public int [] getSelectedVisibleIndices(int start, int end){
      int count =0;
      int currVisibleIndex=-1;
      int firstVisibleIndexAfterStart=-1;
      ArrayList data = model.getAllData();
      int size = data.size();
      if (size ==0) return null;

      //we need to first count how many rows between start and end are
      //visible and selected
      if (end+1>size) end =size-1; //could sometimes happen
      for (int i=0; i<=end; i++){ //start at the end index
        //got some problem here sometime i went above data size
        Datum datum = (Datum) data.get(i);
        if (!datum.isInvisible()) {
          currVisibleIndex++;
          if (i>=start && i<=end){
            if (firstVisibleIndexAfterStart==-1) {
              firstVisibleIndexAfterStart = currVisibleIndex;
            }
            if (isSelectedIndex(i))
              count++;
          }
        }
      }

      if (count ==0) return null;

      int[] selectedVisible = new int[count];
      //now record all the indices;
      int idx = 0;
      currVisibleIndex=firstVisibleIndexAfterStart-1;
      for (int i = start; i <= end; i++) { //start at the end index
        Datum datum = (Datum) data.get(i);
        if (!datum.isInvisible()) {
          currVisibleIndex++;
          if (isSelectedIndex(i)){
            selectedVisible[idx] = currVisibleIndex;
            idx++;
          }
        }
      }
      return selectedVisible;

    }

    public void selectVisible(int startIndex, int endIndex){
      int size = model.getSize();
      int max =Math.min(size-1, endIndex);//it can't go beyond the end of the list
      for (int i=startIndex; i<=max; i++){
        if (model.isVisible(i))
          this.addSelectionInterval(i, i);
        else {
          this.removeSelectionInterval(i, i);
        }
      }
    }

    public void unselectInvisible(int startIndex, int endIndex){
     int size = model.getSize();
     int max =Math.min(size-1, endIndex);//it can't go beyond the end of the list
     for (int i=startIndex; i<=max; i++){
       if (!model.isVisible(i))
         this.removeSelectionInterval(i, i);
     }
   }

    /*
    public void selectVisible(int startIndex, int endIndex, boolean hideInvisible){
      int size = model.getSize();
      int max =Math.min(size-1, endIndex);//it can't go beyond the end of the list
      for (int i=startIndex; i<=max; i++){
        if (model.isVisible(i))
          this.addSelectionInterval(i, i);
      }
    }*/


    /*public void unselectVisible(int startIndex, int endIndex){
      int size = model.getSize();
      int max =Math.min(size-1, endIndex);//it can't go beyond the end of the list
      for (int i=startIndex; i<=max; i++){
        if (model.isVisible(i))
          this.removeSelectionInterval(i, i);
      }
    }*/


  /**
   * selected only these indices
   *
   * @param visibleIndices
   */
    public void selectVisibleIndices(int[] visibleIndices){
      clearSelection();
      int size = model.getSize();
      int currVisibleIndex = -1;
      //int [] toBeSelected = new int[visibleIndices.size];

      for (int i=0, idx=0; i<size && idx<visibleIndices.length; i++){
        if (model.isVisible(i)) {
          currVisibleIndex++;
          if (currVisibleIndex==visibleIndices[idx]){
            this.addSelectionInterval(i,i);
            idx++;
          }
        }
      }
    }


    /**
     * return a number of selected compounds after the startSelectedIndex
     * @param startSelectedIndex int
     *   starting index in the selected datumecules
     * @param count int
     *   number of selected compound to return
     * @return ArrayList
     */
    public ArrayList getSelectedData(int startSelectedIndex, int count){
      ArrayList data = new ArrayList();
      ArrayList sorted = model.getAllData();
      int curr=-1;
      int size=0;
      for (int i=0; i<sorted.size(); i++) {
        if (isSelectedIndex(i)) {curr++;
          if (curr >= startSelectedIndex) {
            data.add(sorted.get(i));
            size++;
            if (size == count) {
              return data;
            }
          }
        }
      }
      return data;
    }

    //get selected as Xlist
    public ArrayList getSelectedAsXlist()
    {
      ArrayList names = new ArrayList();
      int min = Math.max(0,getMinSelectionIndex());
      int max = getMaxSelectionIndex();
      for (int i= min; i<=max; i++){
        if (isSelectedIndex(i)) {
          names.add(model.getDatum(i).getName());
        }
      }

      return names;
    }


    // get names of selected mols as a string separated by the given delim
    // if endWithDelim is true then append delim to end of string as well
    public String getSelectedMolsAsString(String delim, boolean endWithDelim)
    {
      StringBuffer names = new StringBuffer();
      int min = Math.max(0,getMinSelectionIndex());
      int max = getMaxSelectionIndex();
      for (int i= min; i<=max; i++){
        if (isSelectedIndex(i)) {
          names.append(model.getDatum(i).getName());
          names.append(delim);
        }
      }

      if (!endWithDelim)
        names.deleteCharAt(names.length()-1);

      return names.toString();
    }

    public void deselectData(ArrayList data) { //can be optimized
      if (data == null)
        return;

      for (Iterator iter = data.iterator(); iter.hasNext(); ) {
        Datum datum = (Datum) iter.next();
        int idx = getDatumIndex(datum);
        if (idx < 0)continue;
        removeSelectionInterval(idx, idx);
      }
        // keep selection in sync
        //syncListSelection();
    }
    public void inverseSelection() {
      int total = model.getAllData().size();
      for (int i=0; i<total; i++) {
        if (isSelectedIndex(i))
          removeSelectionInterval(i,i);
        else
          addSelectionInterval(i,i);
      }

        // keep selection in sync
        //syncListSelection();
    }
    public void deselectDatum(Datum datum) {
      if (datum == null)
        return;

      int idx = getDatumIndex(datum);
      if (idx < 0) return;
      removeSelectionInterval(idx, idx);

      // keep selection in sync
      //syncListSelection();


    } //notify changes

    public int size(){
      return getSelectedData().size();
    }

//**** overwrite the default method
    public void clearSelection(){
        super.clearSelection();
        //syncListSelection();
        //setValueIsAdjusting(false); //triggers an ListSelectionEvent
    } //notify changes

    public synchronized void syncListSelection()
    {
        setValueIsAdjusting(true);
        //first clear the internal selection indices maintained by DefaultListSelectionModel
       /* super.clearSelection();

        ArrayList data = getAllMols();
        Iterator iter = data.iterator();
        int i=0;
        while (iter.hasNext())
        {
            if (selected.contains(iter.next()))
                addSelectionInterval(i,i);
            i++;
        }*/
        setValueIsAdjusting(false); //triggers an ListSelectionEvent
    }

   


   /****************************************************************/
   /**
     * Sent after the indices in the index0,index1
     * interval have been inserted in the data model.
     * The new interval includes both index0 and index1.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void intervalAdded(ListDataEvent e) {contentsChanged(e);}


    /**
     * Sent after the indices in the index0,index1 interval
     * have been removed from the data model.  The interval
     * includes both index0 and index1.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void intervalRemoved(ListDataEvent e){contentsChanged(e);}


    /**
     * Sent when the contents of the list has changed in a way
     * that's too complex to characterize with the previous
     * methods.  Index0 and index1 bracket the change.
     *
     * @param e  a ListDataEvent encapuslating the event information
     */
    public void contentsChanged(ListDataEvent e) {
      if (e instanceof CustomListDataEvent)
      {
        if (e.getType()!=CustomListDataEvent.TYPE_RESORTED)
          return;
        else {
          //when the listDataModel is resorted, we lose the selection information
        }
      }
      clearSelection();
    }

    public void fireValueChanged(boolean isAdjusting){
      if (!isAdjusting)
        super.fireValueChanged(isAdjusting);
    }
    public void fireValueChanged(int start, int end, boolean isAdjusting){
      if (!isAdjusting)
        super.fireValueChanged(start, end, isAdjusting);
    }
    public void fireValueChanged(int start, int end){
     if (!getValueIsAdjusting())
       super.fireValueChanged(start, end);
   }





}
