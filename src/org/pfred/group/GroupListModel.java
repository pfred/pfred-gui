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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.AbstractListModel;

import org.pfred.PFREDConstant;
import org.pfred.PFREDDataStore;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Datum;


public class GroupListModel extends AbstractListModel{
  private PFREDDataStore dataStore;
  private ArrayList m_groups = new ArrayList();

  // this is a temporary fix to keep track of when first group property is created
  private boolean firstMolAdded = false;

  private boolean dataChanging = false;

  /**
   * When dataIsChanging is set to false, event gets triggered. If it is only PropertyChange
   * CompoundPropertyChangeEvent will be fired instead of the ListDataEvent. This gives us some
   * flexibility for the type of event we want
   *
   * @param changing boolean
   * @param onlyPropertyChange boolean
   */
  public void setDataIsChanging(boolean changing) {
    dataChanging=changing;
    if (dataChanging == false) {
      //trigger content changed event
      int size = m_groups.size();
      fireContentsChanged(this, 0, size-1);
    }
  }


  private CustomListModel list_model;

  public GroupListModel(PFREDDataStore dataStore) {
      this.dataStore = dataStore;
      list_model= dataStore.getOligoListModel();
  }
  public void setCustomListModel(CustomListModel model)
  {
    list_model=model;
  }
  public CustomListModel getOligoListModel()
  {
    return list_model;
  }

  public ArrayList getMolsByGroupName(String groupName) {
    ArrayList sorted = list_model.getAllData();
    Datum mol = null;
        String group = null;
        ArrayList mols = new ArrayList();
        if (groupName.equalsIgnoreCase(PFREDConstant.ALL_COMPOUNDS))
        {
            mols = sorted;
            return mols;
        }
        if (groupName.equalsIgnoreCase(PFREDConstant.SELECTED_COMPOUNDS)) {
          CustomListSelectionModel cmpd_sel_model = dataStore.getOligoListSelectionModel();
          mols = cmpd_sel_model.getSelectedData();
          return mols;
        }
        for (int i=0; i<sorted.size(); i++)
        {
            mol = (Datum) sorted.get(i);
            group = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY);
            if (group != null) {
                StringTokenizer st = new StringTokenizer(group, ":");
                boolean found = false;
                while (st.hasMoreTokens())
                {
                    if (st.nextToken().equalsIgnoreCase(groupName))
                    {
                        found = true;
                        mols.add(mol);
                        break;
                    }
                }
            }
        }
        return mols;

  }

  public ArrayList getMolsByGroups(GroupInfo [] groups) {
     ArrayList mols = new ArrayList();
         for (int i=0; i<groups.length; i++)
         {
           ArrayList gm = getMolsByGroupName(groups[i].name);
           Iterator iter = gm.iterator();
           while (iter.hasNext())
           {
             Datum mol = (Datum)iter.next();
             if (!mols.contains(mol))
               mols.add(mol);
           }
         }
         return mols;
  }

  public ArrayList getMolsByGroup(GroupInfo group) {
    return getMolsByGroupName(group.name);
  }


    public ArrayList getMolsByGroup(String aName,String opName,String  bName)
    {
        ArrayList mols = new ArrayList();

        ArrayList aMols = getMolsByGroupName(aName);
        ArrayList bMols = getMolsByGroupName(bName);

        if (opName.equals(PFREDConstant.OR))
        {
            mols = getMolsInGroupAOrB(aMols, bMols);
        }
        else if (opName.equals(PFREDConstant.AND))
        {
            mols = getMolsInGroupAAndB(aMols, bMols);
        }
        else if (opName.equals(PFREDConstant.BUT_NOT))
        {
            mols = getMolsInGroupAButNotB(aMols, bMols);
        }
        return mols;
    }

// get ilst of all compounds in all groups
    public ArrayList getMolsInGroupAOrB(ArrayList aGroup, ArrayList bGroup)
    {
      ArrayList mols = new ArrayList();
      for (int i=0; i<aGroup.size();i++)
          mols.add(aGroup.get(i));
      for (int i=0; i<bGroup.size(); i++)
      {
          Datum mol = (Datum)bGroup.get(i);
          if (!mols.contains(mol))
            mols.add(mol);
      }

      return mols;
    }
    // find mols contained in BOTH groups A and B
    public ArrayList getMolsInGroupAAndB(ArrayList aMols, ArrayList bMols)
    {
      ArrayList mols = new ArrayList();

      Iterator iter = aMols.iterator();
      while (iter.hasNext())
      {
        Datum mol = (Datum)iter.next();
        if (bMols.contains(mol))
          mols.add(mol);
      }

      return mols;
    }

    // find mols contained in group A but not in group B
    public ArrayList getMolsInGroupAButNotB(ArrayList aMols, ArrayList bMols)
    {
      ArrayList mols = new ArrayList();

      Iterator iter = aMols.iterator();
      while (iter.hasNext())
      {
        Datum mol = (Datum)iter.next();
        if (!bMols.contains(mol))
          mols.add(mol);
      }

      return mols;
    }

   /**
    * setups up group info with random colors and default values
    * for all the groups detected in the mol properties.
    * this is used by SarVis to re-create groups from pre-existing properties
    *
    */
   public void addGroupInfoFromProperties()
   {
       // first gather a set of group names
       HashSet groupNames = new HashSet();
       ArrayList mols = dataStore.getOligoListModel().getAllData();

       Iterator iter = mols.iterator();
       while (iter.hasNext())
       {
           Datum mol = (Datum)iter.next();

           // get group string...
           Object groupObj = mol.getProperty(PFREDConstant.GROUP_PROPERTY);
           if (groupObj == null)
               continue;
           String groupString = groupObj.toString();

           // get the groups from it and add them if unique...
           ArrayList groups = getGroupsFromGroupString(groupString);
           Iterator gIter = groups.iterator();
           while (gIter.hasNext())
           {
               String group = gIter.next().toString();
               if (!groupNames.contains(group))
                   groupNames.add(group);
           }
       }

       // now create deafult values for these groups...
       GroupInfo[] groups = new GroupInfo[groupNames.size()];
       iter = groupNames.iterator();
       for (int i=0; iter.hasNext(); i++)
           groups[i] = new SimpleGroupInfo(iter.next().toString());

       addGroupInfo(groups);
   }



  /**
   * This returns the GroupInfo object by its name;
   * @param name
   * @return
   */
  public GroupInfo getGroupInfo(String name) {
    for (int i = 0; i < m_groups.size(); i++) {
      GroupInfo g = (GroupInfo) m_groups.get(i);
      if (g.name.equalsIgnoreCase(name))
        return g;
    }
    return null;
  }

  public synchronized String[] getGroupNames()
  {
    String[] names = new String[m_groups.size()];

    for (int i=0; i<m_groups.size(); i++)
    {
      GroupInfo opt = (GroupInfo)m_groups.get(i);
      names[i] = opt.name;
    }

    return names;
  }

  public ArrayList getAllGroupInfos(){
    return m_groups;
  }

  public int getGroupInfoIndex(String name) {
    for (int i = 0; i < m_groups.size(); i++) {
      GroupInfo g = (GroupInfo) m_groups.get(i);
      if (g.name.equalsIgnoreCase(name))
        return i;
    }
    return -1;
  }

/*
  public GroupInfo[] getGroupInfo(String[] names) {
    return null;
  }*/

// add a new group
  public void addGroupInfo(GroupInfo opt) {
    addGroupInfo(opt, false);
  }

  public void addGroupInfo(GroupInfo opt, boolean addToTop) {
    // already got one
    if (getGroupInfo(opt.name) != null)
      return;

    if (addToTop){
       m_groups.add(0,opt);
     }
     else{
       m_groups.add(opt);
     }
  }

   public void addGroupInfo(String name, String value, boolean addToTop)
   {
     // already got one
     if (getGroupInfo(name) != null)
       return;

     // build from value string
     GroupInfo opt = GroupInfoHelper.fromValueString(name,value);
     addGroupInfo(opt,addToTop);
   }


  public void addGroupInfo(GroupInfo[] groups) {
    for (int i=0; i<groups.length; i++) {
      addGroupInfo(groups[i], false);
    }
  }

  public void addGroupInfo(ArrayList groups) {
    for (int i=0; i<groups.size(); i++) {
      addGroupInfo((GroupInfo) groups.get(i), false);
    }
  }

  public void removeGroupInfo(GroupInfo group ) {
      // if we dont have one
      if (group == null)
        return;

      // remove any reference to it in any mol
      deleteGroupReferences(group.name);

      // delete the group itself
      m_groups.remove(group);

      //updateMolColorsFromGroupInfo(new GroupInfo[]{group});
      updateMolColorsFromGroupInfo();
  }
  public void removeGroupInfo(String groupName ) {
    // if we dont have one
    if (groupName == null)
      return;

    // delete the group itself
    GroupInfo option = getGroupInfo(groupName);
    if (option != null) {
      // remove any reference to it in any mol
      deleteGroupReferences(groupName);

      m_groups.remove(option);
    }

    updateMolColorsFromGroupInfo();
  }
  public void removeGroupInfo(GroupInfo[] groups) {
    for (int i=0; i<groups.length; i++) {
      removeGroupInfo(groups[i]);
    }
    // tell any listeners group going bye-bye

     //int lastSize = m_groups.size();
     //fireContentsChanged(this, 0, lastSize - 1);
    // updateMolColorsFromGroupInfo(groups);
     updateMolColorsFromGroupInfo();
  }


  public void addMolsToGroup(String groupName, ArrayList mols)
  {

    GroupInfo group = getGroupInfo(groupName);
    if (group == null) return;

    list_model.setDataIsChanging(true);
    for (int i=0; i<mols.size(); i++)
    {
      Datum mol = (Datum) mols.get(i);
      String groupString = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY); //the string looks like Group1:Group2:...

      if (groupString == null)
        mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupName);

      else
      {
        ArrayList g = getGroupsFromGroupString(groupString);
        if (!g.contains(groupName))
        {
          g.add(groupName);
          groupString = getGroupStringFromGroups(g);
          mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupString);
        }
      }
    }

    // update any open windows group coloring

    //updateMolColorsFromGroupInfo(new GroupInfo[]{group});
     updateMolColorsFromGroupInfo();
    list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_COLOR_CHANGED);
  }

  public void addMolsToGroup(GroupInfo group, ArrayList mols)
  {
    list_model.setDataIsChanging(true);
    for (int i=0; i<mols.size(); i++)
    {
      Datum mol = (Datum) mols.get(i);
      String groupString = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY); //the string looks like Group1:Group2:...

      if (groupString == null)
        mol.setProperty(PFREDConstant.GROUP_PROPERTY, group.name);

      else
      {
        ArrayList g = getGroupsFromGroupString(groupString);
        if (!g.contains(group.name))
        {
          g.add(group.name);
          groupString = getGroupStringFromGroups(g);
          mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupString);
        }
      }
    }
    if (firstMolAdded)  {
      list_model.setDataIsChanging(false,
                                   CustomListDataEvent.
                                   TYPE_PROPERTY_NUMBER_CHANGED);
      firstMolAdded=true;
    }

      // update any open windows group coloring
    list_model.setDataIsChanging(true);
    //updateMolColorsFromGroupInfo(new GroupInfo[]{group});
     updateMolColorsFromGroupInfo();
    list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_COLOR_CHANGED);

  }


  /**
   * use addMolsToGroup if you have large number of mols to be added. It causes dataChangeEvent
   * every time it is called
   *
   * @param groupName
   * @param mol
   */
  public void addMolToGroup(String groupName, Datum mol)
  {
    GroupInfo group = getGroupInfo(groupName);
    if (group == null) return;

    list_model.setDataIsChanging(true);
    String groupString = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY); //the string looks like Group1:Group2:...
    if (groupString == null)
      mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupName);
    else
    {
      ArrayList g = getGroupsFromGroupString(groupString);
      if (!g.contains(groupName))
      {
        g.add(groupName);
        groupString = getGroupStringFromGroups(g);
        mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupString);
      }
    }



    // update any open windows group coloring
    //updateMolColorsFromGroupInfo(new GroupInfo[]{group});
     updateMolColorsFromGroupInfo();
    list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_COLOR_CHANGED);
  }



  public void removeMolsFromGroup(String groupName, ArrayList mols) {
    GroupInfo group = getGroupInfo(groupName);
    if (group == null) return;

    list_model.setDataIsChanging(true);
    for (int i = 0; i < mols.size(); i++) {
      Datum mol = (Datum) mols.get(i);
      String groupString = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY); //the string looks like Group1:Group2:...:
      if (groupString == null)
        continue;

      ArrayList g = getGroupsFromGroupString(groupString);
      if (g.contains(groupName)) {
        removeGroupFromGroups(groupName, g);
        if (g.size() == 0) {
          mol.setProperty(PFREDConstant.GROUP_PROPERTY, "");
        }
        else {
          groupString = getGroupStringFromGroups(g);
          mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupString);
        }
      }

      // this old way was prone to error...in particular if one group
      // name was a subset of another then this would mangle the data
      //if (groups != null && groups.indexOf(groupName)>=0)
      // {
      //     int idx = groups.indexOf(groupName);
      //     groups = groups.substring(0,idx) + groups.substring(idx+groupName.length()+1);
      //     mol.setProperty(PFREDConstant.GROUP_PROPERTY, groups); //be careful. sometimes we will get a lot of properties with "" in the value;
      // }
    }

    // update any open windows group coloring
    //updateMolColorsFromGroupInfo(new GroupInfo[]{group});
     updateMolColorsFromGroupInfo();
     list_model.setDataIsChanging(false,  CustomListDataEvent.TYPE_COLOR_CHANGED);
  }

    // change the mol colors according to the current group info
  public void updateMolColorsFromGroupInfo()
  {
    // first clear all colors
    list_model.setDataIsChanging(true);
    list_model.clearColor();

    // now set colors in reverse order so that the
    // top of the list gets priority
    for (int i=m_groups.size()-1; i>=0; i--)
    {
      GroupInfo opt = (GroupInfo)m_groups.get(i);

      // if its not selected...ignore it
      if (!opt.show)
        continue;

      // color members of this group
      list_model.setColor(getMolsByGroupName(opt.name), opt.color);
    }
    //System.out.println("before updating group color");
    list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_COLOR_CHANGED);
    //System.out.println("Done updating group color");
  }

 /* public void updateMolColorsFromGroupInfo(GroupInfo[] groups)
  {
    // first clear all colors
    list_model.setDataIsChanging(true);

    // now set colors in reverse order so that the
    // top of the list gets priority
    for (int i=groups.length-1; i>=0; i--)
    {
      GroupInfo opt = groups[i];

      // if its not selected...ignore it
      if (!opt.show) {
        list_model.setColor(getMolsByGroupName(opt.name), Color.white);
        continue;
      }

      // color members of this group
      list_model.setColor(getMolsByGroupName(opt.name), opt.color);
    }
    list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_COLOR_CHANGED);
  }*/


  public void setGroupColor(GroupInfo group, Color c) {
    group.color = c;
    updateMolColorsFromGroupInfo();
  }

  public void showGroupColor(GroupInfo[] groups, boolean show){
    for (int i=0; i<groups.length; i++)
      groups[i].show = show;
    updateMolColorsFromGroupInfo();
  }

  public void showGroupColor(GroupInfo group, boolean show){
    group.show = show;
    updateMolColorsFromGroupInfo();
  }

  public void renameGroup(String oldName, String newName) {
    // if we dont have one
    GroupInfo group = getGroupInfo(oldName);
    if (group == null)
      return;

    // rename any reference to it in any mol
    renameGroupReferences(oldName, newName);

    // change its name
    // copy for paranoias sake...if we can avoid the copy lets do that
    group.name = newName;
  }


  // delete all references to this group in the mol data
    private synchronized void deleteGroupReferences(String groupName)
    {
      ArrayList mols = list_model.getAllData();
      list_model.setDataIsChanging(true);
      Iterator iter = mols.iterator();
      while (iter.hasNext())
      {
        Datum mol = (Datum)iter.next();
        // get group property string
        String groupString = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY);

        if (groupString == null)
          continue;

        ArrayList g = getGroupsFromGroupString(groupString);
        if (g.contains(groupName))
        {
          removeGroupFromGroups(groupName,g);
          if (g.size() == 0)
          {
            mol.setProperty(PFREDConstant.GROUP_PROPERTY,"");
          }
          else
          {
            groupString = getGroupStringFromGroups(g);
            mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupString);
          }
        }
      }
      list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_PROPERTY_UPDATED);
    }
    protected synchronized void renameGroupReferences(String name, String newName)
   {
     ArrayList mols = list_model.getAllData();
     list_model.setDataIsChanging(true);
     Iterator iter = mols.iterator();
     while (iter.hasNext())
     {
       Datum mol = (Datum)iter.next();
       // get group property string
       String groupString = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY);

       if (groupString == null)
         continue;

       // remove old name
       ArrayList g = getGroupsFromGroupString(groupString);
       if (g.contains(name)) {
         removeGroupFromGroups(name, g);

         // add new name
         g.add(newName);
         groupString = getGroupStringFromGroups(g);
         mol.setProperty(PFREDConstant.GROUP_PROPERTY, groupString);
       }
     }
     list_model.setDataIsChanging(false, CustomListDataEvent.TYPE_PROPERTY_UPDATED);
   }



    // get groups as arraylist
    public ArrayList getGroupsFromGroupString(String groupString)
    {
      StringTokenizer st = new StringTokenizer(groupString,":");
      ArrayList g = new ArrayList();
      while (st.hasMoreTokens())
        g.add(st.nextToken());

      return g;
    }

    // get string from array list of groups
    public String getGroupStringFromGroups(ArrayList g)
    {
      String s = "";
      for (Iterator iter = g.iterator(); iter.hasNext(); )
        s = s + (String)iter.next() + ":";
      return s.substring(0,s.length()-1);
    }

    // remove a given group from the group list
    public void removeGroupFromGroups(String groupName, ArrayList g)
    {
      for (Iterator iter = g.iterator(); iter.hasNext(); )
      {
        String s = (String)iter.next();
        if (s.equalsIgnoreCase(groupName))
          iter.remove();
      }
    }


    public void clear(){
      m_groups.clear();
    }

    public void moveGroupToTop(GroupInfo [] groups){
      // build a new groups array list
      ArrayList newGroups = new ArrayList();

// add these groups first
      for (int i=0; i<groups.length; i++)
        newGroups.add(groups[i]);

// now add all the other groups...make sure not to add a group twice

      Iterator iter = m_groups.iterator();
      while (iter.hasNext())
      {
        GroupInfo group = (GroupInfo)iter.next();
        // dont add if we already added that one
        if (!newGroups.contains(group) && m_groups.contains(group))
          newGroups.add(group);
      }

      m_groups = newGroups;

      updateMolColorsFromGroupInfo();

    }



    // move given groups to BOTTOM of list
    public void moveGroupBottom(GroupInfo[] groups)
    {
      // build a new groups array list
      ArrayList newGroups = new ArrayList();

      // now add all the groups EXCEPT this one...make sure not to add a group twice
      Iterator iter = m_groups.iterator();
      while (iter.hasNext())
      {
        GroupInfo group = (GroupInfo)iter.next();
        // dont add now...add at end
        if (!GroupInfoHelper.groupContains(groups,group))
          newGroups.add(group);
      }

      // add these groups last
      for (int i=0; i<groups.length; i++)
        newGroups.add(groups[i]);

      // this is the new groups
      m_groups = newGroups;

      updateMolColorsFromGroupInfo();
    }

    // move given groups UP the list
       public void moveGroupUp(GroupInfo[] groups)
       {
         // get the current indexes of these groups in sorted order
         int[] index = getGroupIndicies(groups);
         if (index.length < 1)
           return;

         // if the first index is already at the top...do nothing
         if (index[0] < 1)
           return;

         // otherwise we want to decrease each index by one
         for (int i=0; i<index.length; i++)
           index[i] = index[i] - 1;

         // force this order
         forceGroupOrder(groups,index);

         updateMolColorsFromGroupInfo();

       }

       // move given groups DOWN the list
       public void moveGroupDown(GroupInfo[] groups)
       {
         // get the current indexes of these groups in sorted order
         int[] index = getGroupIndicies(groups);
         if (index.length < 1)
           return;

         // if the last index is already at the bottom...do nothing
         if (index[index.length-1] >= m_groups.size()-1)
           return;

         // otherwise we want to increase each index by one
         for (int i=0; i<index.length; i++)
           index[i] = index[i] + 1;

         // force this order
         forceGroupOrder(groups,index);

         updateMolColorsFromGroupInfo();
       }

       // force these groups to be in this order
   public void forceGroupOrder(GroupInfo[] groups, int[] index)
   {
     // first remove all these groups
     for (int i=0; i<groups.length; i++) {
       m_groups.remove(groups[i]);
       m_groups.add(index[i],groups[i]);
     }
    }

    // get indicies for given array of groups in sorted order
    public int[] getGroupIndicies(GroupInfo[] groups)
    {
      int[] index = new int[groups.length];
      int currIndex = 0;

      for (int i=0; i<groups.length; i++)
      {
        GroupInfo group = groups[i];
        // is this one of them?
        for (int j=0; j<m_groups.size(); j++)
        {
          GroupInfo g = (GroupInfo)m_groups.get(j);
          if (g == group) {
            index[currIndex++] = j;
            break;
          }
        }
      }

      return index;
    }


  //*** implementation to the abstract ListModel method
   public Object getElementAt(int index) {
     if (m_groups.size() <= index)
       return null;

     Object group = m_groups.get(index);
     return group;
   }

  public int getSize() {
    return m_groups.size();
  }




}

