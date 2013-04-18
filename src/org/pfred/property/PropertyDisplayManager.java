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
package org.pfred.property;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.pfred.model.Datum;
import org.pfred.table.BarChartCustomData;
import org.pfred.table.ConditionalFormatting;
import org.pfred.table.CustomTableModel;


public class PropertyDisplayManager {


  String DOTS = "..";

  HashMap availableOptionNames = new HashMap();
  private ArrayList availableOptions = new ArrayList();
  private ArrayList displayedOptions = new ArrayList();
  ArrayList listeners = new ArrayList();

  // constructor
  public PropertyDisplayManager()
  {
  }

  public ArrayList getAllPropertyOptions()
  {
          return availableOptions;
  }



  public ArrayList getAllDisplayedOptions(){
    return displayedOptions;
  }

  // if options is not displayed return NULL
  public PropertyDisplayOption getDisplayedPropertyDisplayOption(String name)
  {
     PropertyDisplayOption opt = getPropertyDisplayOption(name);
     if (opt != null)
     {
         if (!displayedOptions.contains(opt))
             opt = null;
     }

     return opt;
  }

  // get a property display option by name
  public PropertyDisplayOption getPropertyDisplayOption(String name)
  {
    if (name == null)
      return null;
    return (PropertyDisplayOption) availableOptionNames.get(name);
  }

  // get a property display option by short name
  // TODO: make hash map for short name also kept up to date for faster implementation
  public PropertyDisplayOption getPropertyDisplayOptionByShortName(String shortName)
  {
    if (shortName == null)
      return null;
    for (int i=0; i<availableOptions.size(); i++)
    {
        PropertyDisplayOption opt = (PropertyDisplayOption)availableOptions.get(i);
        if (opt.shortName.equalsIgnoreCase(shortName))
            return opt;
    }
    return null;
  }

  // get all property names that are a certain type...
  public String[] getAllPropertyNames(int type)
  {
    ArrayList list = new ArrayList();
    for (int i=0; i<availableOptions.size(); i++)
    {
     PropertyDisplayOption opt = (PropertyDisplayOption)availableOptions.get(i);
     if (opt.getType() == type)
         list.add(new String(opt.name));
    }
    String[] names = (String[])list.toArray(new String[0]);
    Arrays.sort(names);
    return names;
  }


  // get array of strings giving names of currently defined propety list
  public String[] getAllPropertyNames()
  {
    String[] names = new String[availableOptions.size()];
    for (int i=0; i<availableOptions.size(); i++)
    {
     PropertyDisplayOption opt = (PropertyDisplayOption)availableOptions.get(i);
     names[i] = new String(opt.name);
    }
    return names;
  }

  // get array of strings giving display names of currently defined propety list
  public String[] getDisplayedNames()
  {
    String[] names = new String[displayedOptions.size()];
    for (int i=0; i<displayedOptions.size(); i++)
    {
      PropertyDisplayOption opt = (PropertyDisplayOption)displayedOptions.get(i);
      names[i] = new String(opt.name);
    }

    return names;
  }

  public void clearDisplayedProperty(){
    displayedOptions.clear();
  }
  public void clearAll(){
    displayedOptions.clear();
    availableOptions.clear();
    availableOptionNames.clear();
  }

  // add all options that match a given type...
  public void addDisplayedOptionsAllByType(int type)
  {
      String[] names = getAllPropertyNames(type);
      for (int i=0; i<names.length; i++)
          addDisplayedOption(names[i]);
  }

  public void addDisplayedOptionsAll()
  {
      String[] names = getAllPropertyNames();
      for (int i=0; i<names.length; i++)
          addDisplayedOption(names[i]);
  }

  public boolean isPropertyDisplayed(String name){
    PropertyDisplayOption opt = (PropertyDisplayOption)
        availableOptionNames.get(name);
    if (opt == null) return false;

    return displayedOptions.contains(opt);
  }

  public boolean hasPropertyDisplayOption(String name) {
    return availableOptionNames.containsKey(name);
  }

  public PropertyDisplayOption getDisplayedOption(int idx){
    //if (idx>displayedOptions.size()) return null;
    return (PropertyDisplayOption) displayedOptions.get(idx);
  }

  public PropertyDisplayOption getAvailableOption(int idx){
    return (PropertyDisplayOption) availableOptions.get(idx);
  }

  public int getDisplayedCount(){
    return displayedOptions.size();
  }

  public int getAvailableCount(){
    return availableOptions.size();
  }


  // given a molecule get array of strings with display names and values
  public String[] getDisplayNamesAndValues(Datum datum, boolean padding)
  {
    String[] names = new String[displayedOptions.size()];
    for (int i=0; i<displayedOptions.size(); i++)
    {
      PropertyDisplayOption opt = (PropertyDisplayOption)displayedOptions.get(i);
      Object obj = datum.getProperty(opt.name);
      String value =null;
      if (obj!=null) value = obj.toString();

      if (value == null || value.trim().length()==0)
         value = "        ";

      // limit value to 6 total chars
      if (padding) {
        if (value.length() > 8)
          value = value.substring(0, 8);
        else if (value.length() < 8) {
          padString(value, 8);
        }
      }

      String shortName = opt.shortName;
      if (shortName.length()>8)
        shortName = shortName.substring(0,6)+DOTS;

      names[i] = "   "+shortName + " " + value;
    }

    return names;
  }


  public String padString(String value, int length){
    if (value.length()>=length) return value;

    StringBuffer newValue = new StringBuffer(length);
    int pad_length = length - value.length();
    for (int i=0; i<pad_length; i++){
      newValue.append(' ');
    }
    return value+newValue.toString();
  }


  // given a molecule get array of strings with display names and values
   public String[] getFullDisplayNamesAndValues(Datum mol)
   {
     String[] names = new String[displayedOptions.size()];
     for (int i=0; i<displayedOptions.size(); i++)
     {
       PropertyDisplayOption opt = (PropertyDisplayOption)displayedOptions.get(i);
       String value = (String) mol.getProperty(opt.name);
       if (value == null || value.trim().length()==0)
         value = "    ";
       // limit value to 20 total chars
       int idx = value.indexOf("\n");
       if (idx>0) //we can't display multiple line text
           value = value.substring(0,idx);
      if (value.length() > 20)
        value = value.substring(0,20);
       String shortName = opt.shortName;
       names[i] = "  "+shortName + " " + value;
     }

     return names;
  }
  /**
   * to help to save the options to string
   * @return
   */
  public String displayOptsToString()
  {
    StringBuffer displayOpts = new StringBuffer();
    for (int i=0; i<displayedOptions.size(); i++)
    {
        PropertyDisplayOption opt = (PropertyDisplayOption)displayedOptions.get(i);
        if (i!=0)
            displayOpts.append("?");

        // the fields are order dependant so they are ALL required
        // if a field does not exist a DASH is written to make life
        // easier on the string tokenizer that has to parse this string...

        displayOpts.append(opt.name);
        displayOpts.append(":");
        displayOpts.append(opt.shortName);

        //now need to persist the type
        displayOpts.append(":");
        displayOpts.append(opt.type);
        displayOpts.append(":");
        displayOpts.append(opt.isDerived);

        displayOpts.append(":");
        if (opt.derivedRules==null)
            displayOpts.append(" "); // space means none...
        else
            displayOpts.append(opt.derivedRules.toString());

        displayOpts.append(":");
        if (opt.color == null)
           displayOpts.append(" "); // space means none...
        else
           displayOpts.append(Integer.toHexString(opt.color.getRGB()));

        displayOpts.append(":");
        if (opt.conditionalFormatting == null)
           displayOpts.append(" "); // space means none...
        else
           displayOpts.append(opt.conditionalFormatting.toString());

        displayOpts.append(":");
        if (opt.cellWidth <= 0)
           displayOpts.append(" "); // space means none...
        else
           displayOpts.append(Integer.toString(opt.cellWidth));

        displayOpts.append(":");

     
          displayOpts.append("true");
      

          displayOpts.append(":");
          if (opt.customData == null)
              displayOpts.append(" "); // space means none...
           else
              displayOpts.append(opt.customData.dataToString());

          // we always end with a terminating ":"
          displayOpts.append(":");
    }
    //now do the ones that are not displayed
    for (int i=0; i<availableOptions.size(); i++)
   {
       PropertyDisplayOption opt = (PropertyDisplayOption)availableOptions.get(i);
       if (displayedOptions.contains(opt)) continue;
       if (i!=0)
           displayOpts.append("?");

       // the fields are order dependant so they are ALL required
       // if a field does not exist a DASH is written to make life
       // easier on the string tokenizer that has to parse this string...

       displayOpts.append(opt.name);
       displayOpts.append(":");
       displayOpts.append(opt.shortName);


       //now need to persist the type
       displayOpts.append(":");
       displayOpts.append(opt.type);
       displayOpts.append(":");
       displayOpts.append(opt.isDerived);

       displayOpts.append(":");
       if (opt.derivedRules==null)
           displayOpts.append(" "); // space means none...
       else
           displayOpts.append(opt.derivedRules.toString());

       displayOpts.append(":");
       if (opt.color == null)
          displayOpts.append(" "); // space means none...
       else
          displayOpts.append(Integer.toHexString(opt.color.getRGB()));

       displayOpts.append(":");
       if (opt.conditionalFormatting == null)
          displayOpts.append(" "); // space means none...
       else
          displayOpts.append(opt.conditionalFormatting.toString());

       displayOpts.append(":");
       if (opt.cellWidth <= 0)
          displayOpts.append(" "); // space means none...
       else
          displayOpts.append(Integer.toString(opt.cellWidth));

       displayOpts.append(":");

 
         displayOpts.append("false"); //not displayed
     

         displayOpts.append(":");


         if (opt.customData == null)
            displayOpts.append(" "); // space means none...
         else
            displayOpts.append(opt.customData.dataToString());
         
         displayOpts.append(":");
   }


    return displayOpts.toString();
  }

  public void displayOptsFromString(String opts)
  {
      displayOptsFromString(opts,null);
  }

  public void displayOptsFromString(String opts, CustomTableModel ctm)
  {
    this.clearDisplayedProperty();
      StringTokenizer displayOpts = new StringTokenizer(opts, "?");
      String tmp = null;
      String name = null;
      String shortname = null;
      boolean isDerived = false;
      String derivedRulesStr;
      Object derivedRules;
      String colorStr;
      Color color;
      String cfStr;
      ConditionalFormatting cf;
      String widthStr;
      int width;
      String displayedStr;
      boolean displayed=true;
      PropertyDisplayOptionCustomData customData=null;
      String customDataStr=null;


      //String prettyname = null;
      int type = 0;

      while (displayOpts.hasMoreTokens())
      {
          tmp = displayOpts.nextToken();
          StringTokenizer stTmp = new StringTokenizer(tmp, ":");
          name = stTmp.nextToken();
          shortname = stTmp.nextToken();

          if (name.startsWith("SPIDER_"))
              name = new String(name);
          
          if (name.equals("dna_oligo")){ //for backward compatiblity
        	  name="parent_dna_oligo";
        	  shortname="parent_dna_oligo";
          }
          if (name.equals("sense_oligo")){ 
        	  name="parent_sense_oligo";
        	  shortname="parent_sense_oligo";
          }
          if (name.equals("antisense_oligo")){
        	  name="parent_antisense_oligo";
        	  shortname="parent_antisense_oligo";
          }

          //prettyname = stTmp.nextToken();
          color = null;
          derivedRules = null;
          cf = null;

          width = -1; // meaning not set
          displayed=true;

          if (stTmp.hasMoreTokens()) { //only for version 2
            try {
              type = Integer.parseInt(stTmp.nextToken());
              isDerived = stTmp.nextToken().equalsIgnoreCase("true");
              derivedRulesStr = stTmp.nextToken().trim();
              colorStr = stTmp.nextToken().trim();
              cfStr = stTmp.nextToken().trim();
              widthStr="";
              displayedStr="";
              customDataStr="";
              
              if (stTmp.hasMoreTokens())
                  widthStr = stTmp.nextToken().trim();
              if (stTmp.hasMoreTokens())
                  displayedStr = stTmp.nextToken().trim();
              if (stTmp.hasMoreTokens())
                  customDataStr = stTmp.nextToken().trim();

              // we don't need to read past the last terminating delimeter
              if (derivedRulesStr.length() > 0){
   
                  derivedRules = derivedRulesStr;
               
              }
              if (colorStr.length() > 0)
                color = new Color( (int) Long.parseLong(colorStr.trim(), 16));
              if (cfStr.length() > 0)
                cf = new ConditionalFormatting(cfStr);
              if (widthStr.length() > 0)
            	  width = Integer.parseInt(widthStr);
              if (displayedStr.length() > 0 && displayedStr.equalsIgnoreCase("false"))
            	  displayed=false;
              if (customDataStr.length() > 0 && ctm != null){
            	  if (name.toUpperCase().endsWith("_BARCHART")){
            		  customData = new BarChartCustomData();
            		  customData.dataFromString(customDataStr);
            	  }
              }
             }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }

          PropertyDisplayOption opt = (PropertyDisplayOption) this.getPropertyDisplayOption(name);
          if (opt == null) //add the new display property
          {
              //opt = new PropertyDisplayOption(name, shortname, prettyName);
              opt = new PropertyDisplayOption(name, shortname, type, isDerived);
              opt.derivedRules=derivedRules;
              opt.color = color;
              opt.conditionalFormatting = cf;
              opt.customData = customData;
              opt.cellWidth = width;
              this.addAvailableOption(opt, displayed);
          }
          else
          {
              opt.shortName=shortname;
              opt.type=type;
              opt.isDerived=isDerived;
              opt.derivedRules=derivedRules;
              opt.color = color;
              opt.conditionalFormatting = cf;
              opt.customData = customData;
              opt.cellWidth = width;
              if(displayed)
                this.addDisplayedOption(opt);
              //displayedOptions.add(opt);//need to be careful with this. // no need to add, just update the display
          }
      }
  }



  public PropertyDisplayOption removeDisplayedOption(String name)
  {
    // lookup the index (if any)
    PropertyDisplayOption option = (PropertyDisplayOption) availableOptionNames.get(name);
    if (option==null) return null;
    displayedOptions.remove(option);

    return option;
  }



  public void addDisplayedOption(PropertyDisplayOption opt)
  {
      if (displayedOptions.contains(opt))return;
      displayedOptions.add(opt);
  }

  public void addDisplayedOption(String name){
    if (!availableOptionNames.containsKey(name)) return;
    PropertyDisplayOption opt = (PropertyDisplayOption) availableOptionNames.get(name);

    if (displayedOptions.contains(opt))return;
    displayedOptions.add(opt);
  }

  public void addAvailableOption(PropertyDisplayOption opt, boolean displayed){
    if ( availableOptionNames.containsKey(opt.name)) return;

    availableOptionNames.put(opt.name, opt);
    availableOptions.add(opt);

    if (displayed){
      addDisplayedOption(opt);
    }
  }

  public void removeAvailableOption(PropertyDisplayOption opt){
    if ( !availableOptionNames.containsKey(opt.name)) return;
    PropertyDisplayOption option = (PropertyDisplayOption)
        availableOptionNames.remove(opt.name);

    availableOptions.remove(option);
    displayedOptions.remove(option);
  }

  public PropertyDisplayOption removeAvailableOption(String name)
  {
    // lookup the index (if any)
    PropertyDisplayOption option = (PropertyDisplayOption) availableOptionNames.get(name);
    if (option==null) return null;
    removeAvailableOption(option);

    return option;
  }


  public void removeDisplayedOption(PropertyDisplayOption opt){
    displayedOptions.remove(opt);
  }

  public void displayChanged(){
    PropertyDisplayOptionEvent event = new PropertyDisplayOptionEvent(this,
        PropertyDisplayOptionEvent.PROPERTY_CHANGED);
    for (int i=0; i<listeners.size(); i++){
      PropertyDisplayListener listener = (PropertyDisplayListener) listeners.get(i);
      listener.displayedPropertyChanged(event);
    }
  }

  public void moveDisplayOption(int oldIndex, int newIndex)
  {
     System.out.println("moving disopt, here is current order:");
     for (int i=0; i<displayedOptions.size(); i++)
     {
         PropertyDisplayOption option = (PropertyDisplayOption) displayedOptions.get(i);
         System.out.println(option.name);
     }
     if (oldIndex <0 || oldIndex == newIndex || newIndex < 0 ||
         oldIndex >= displayedOptions.size() || newIndex >= displayedOptions.size())
         return;

     PropertyDisplayOption option = (PropertyDisplayOption) displayedOptions.get(oldIndex);
     displayedOptions.remove(oldIndex);
     displayedOptions.add(newIndex, option);
  }

  public void moveDisplayOptionUp(String name){
    int size = displayedOptions.size();
    for (int i=0; i<size; i++){
      PropertyDisplayOption option = (PropertyDisplayOption) displayedOptions.get(i);
      if (name.equals(option.name) && i>0) {
          displayedOptions.remove(i);
          displayedOptions.add(i-1, option);
          break;
      }
    }
  }

  public void moveDisplayOptionDown(String name){
    int size = displayedOptions.size();
    for (int i=0; i<size; i++){
      PropertyDisplayOption option = (PropertyDisplayOption) displayedOptions.get(i);
      if (name.equals(option.name) && i<size-1) {
          displayedOptions.remove(i);
          displayedOptions.add(i+1, option);
          break;
      }
    }
  }
  
  public void moveDisplayOptionLast(String[] names){
	  HashMap<String,String> nameHash=new HashMap();
	  for (int i=0; i<names.length; i++)
		  nameHash.put(names[i], "1");
	  
	  int size = displayedOptions.size();
	  ArrayList toBeMoved=new ArrayList();
	  for (int i=size-1; i>=0; i--){
		  PropertyDisplayOption option = (PropertyDisplayOption) displayedOptions.get(i);
		  if (nameHash.containsKey(option.name)){
			  toBeMoved.add(option);
			  displayedOptions.remove(i);
		  }
	  }
	  
	  size=toBeMoved.size();
	  for (int i=size-1; i>=0; i--){
		  displayedOptions.add(toBeMoved.get(i));//add to end
	  }
	  
      // fire listeners...
      displayChanged();
  }

  /**
   * if new name already exist return false
   *
   * @param oldname
   * @param newname
   * @return
   */
  public boolean renameDisplayOption(String oldname, String newname){
   if (availableOptionNames.containsKey(newname)) return false;//new name already exist

    PropertyDisplayOption option =
        (PropertyDisplayOption) availableOptionNames.remove(oldname);
    if (option==null) return false;

    option.rename(newname, newname);
    availableOptionNames.put(newname, option);
    //no need to change displayOptions as it only stores the reference
    return true;
  }


  public void addPropertyDisplayListener(PropertyDisplayListener listener){
    if (!listeners.contains(listener))
      listeners.add(listener);
  }

  public void removePropertyDisplayListener(PropertyDisplayListener listener){
    if (listeners.contains(listener))
      listeners.remove(listener);
  }

  public boolean moveProperty(String property, String refProperty, boolean before)
  {
      // property and ref property must both be DISPLAYED properties...
      PropertyDisplayOption opt = getDisplayedPropertyDisplayOption(property);
      PropertyDisplayOption refOpt = getDisplayedPropertyDisplayOption(refProperty);

      if (opt == null || refOpt == null)
          return false;

      displayedOptions.remove(opt);

      int refIndex = displayedOptions.indexOf(refOpt);
      int where = before?refIndex:refIndex+1;

      displayedOptions.add(where, opt);

      // fire listeners...
      displayChanged();

      return true;
  }

  public Set getAvailablePropertiesSet()
  {
      HashSet set = new HashSet();

      for (int i=0; i<availableOptions.size(); i++)
      {
          PropertyDisplayOption opt = (PropertyDisplayOption)availableOptions.get(i);
          set.add(opt.name);
      }

      return set;
  }

  public int indexOf(String name)
  {
      for (int i=0; i<availableOptions.size(); i++)
      {
          PropertyDisplayOption opt = (PropertyDisplayOption)availableOptions.get(i);
          if (name.equalsIgnoreCase(opt.name))
              return i;
      }
      return -1;
  }
}
