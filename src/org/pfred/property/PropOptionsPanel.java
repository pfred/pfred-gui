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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;


public class PropOptionsPanel extends JPanel
  implements ActionListener, KeyListener, ListSelectionListener
{
  //protected CellDisplayOptions value;
  String[] availablePropNames =null;
  String[] displayedPropNames =null;
  PropertyDisplayManager manager = null;
  boolean displayedListAdjusting=false;
  boolean availableListAdjusting=false;
  // these are the properties we want to delete if OK button is hit
  Vector toDelete = new Vector();

  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  protected JList availableList = new JList();
  protected JList displayedList = new JList();
  JScrollPane displayedScroll = new JScrollPane(displayedList);
  JScrollPane availableScroll = new JScrollPane(availableList);
  JButton add = new JButton();
  JButton remove = new JButton();
  JTextField tf_name = new JTextField();
  JComboBox combo_type = new JComboBox(PropertyDisplayOption.getTypeNames());
  JLabel jLabel3 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  private XYLayout xYLayout1 = new XYLayout();

  JPanel optionalPanel = new JPanel();
  XYLayout xYLayout2 = new XYLayout();
  //JCheckBox bottomDisplay = new JCheckBox();
  //JButton rename = new JButton();
  JLabel jLabel4 = new JLabel();
  JTextField tf_shortName = new JTextField();
  //JButton delete = new JButton("Delete...");
    private JPanel jPanel1 = new JPanel();
    private XYLayout xYLayout3 = new XYLayout();
  JButton up = new JButton();
  JButton down = new JButton();


  public PropOptionsPanel(PropertyDisplayManager manager)
  {
    this.availablePropNames = manager.getAllPropertyNames();
    this.displayedPropNames = manager.getDisplayedNames();
    this.manager = manager;

    try    {
      jbInit();
    }
    catch(Exception e)    {
      e.printStackTrace();
    }

    if (availableList!=null)
      availableList.setListData(availablePropNames);
    if (displayedList!=null)
      displayedList.setListData(displayedPropNames);

    // we listen to key presses in the editable text fields
    tf_shortName.addKeyListener(this);
    //prettyName.addKeyListener(this);

    // we listen to selection on the lists
    availableList.addListSelectionListener(this);
    displayedList.addListSelectionListener(this);

    // we listen to the type combo box...
    combo_type.addActionListener(this);
  }


  private void jbInit() throws Exception
  {

    jLabel1.setText("Displayed Properties");
    jLabel2.setText("Available Properties");
    add.setText(">");
    add.addActionListener(this);
    remove.setText("<");
    remove.addActionListener(this);
    tf_name.setEditable(false);
    tf_name.setText("name");
    jLabel3.setText("Name");
    this.setLayout(xYLayout1);
    availableScroll.setPreferredSize(new Dimension(260, 130));
    availableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    displayedScroll.setPreferredSize(new Dimension(260, 130));
    displayedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    //availableList.setPreferredSize(new Dimension(100, 100));
    //displayedList.setPreferredSize(new Dimension(100, 100));
    this.setMinimumSize(new Dimension(780, 342));
    this.setPreferredSize(new Dimension(780, 342));
    optionalPanel.setLayout(xYLayout2);
    /*bottomDisplay.setPreferredSize(new Dimension(104, 23));
    bottomDisplay.setToolTipText("display properties at the bottom of structures");
    bottomDisplay.setMargin(new Insets(0, 0, 2, 2));
    bottomDisplay.setText("Show at bottom");
        bottomDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bottomDisplay_actionPerformed(e);
            }
        });
    bottomDisplay.addActionListener(this);
    rename.setText("Rename...");
    rename.addActionListener(this); */
    jLabel4.setText("Displayed Name");
    tf_shortName.setText("Name");
    /* delete.addActionListener(this);
    delete.setToolTipText("Delete property");
    */
    xYLayout1.setWidth(701);
    xYLayout1.setHeight(447);
    jPanel1.setLayout(xYLayout3);
    up.addActionListener(this);
    up.setActionCommand("^");
    up.setText("^");

    down.setActionCommand("v");
    down.setText("v");
    down.addActionListener(this);
    this.add(jLabel3,    new XYConstraints(14, 45, 66, -1));
    this.add(tf_name,    new XYConstraints(14, 68, 122, -1));
    //availableScroll.getViewport().add(availableList, null);
    this.add(displayedScroll, new XYConstraints(460, 51, 181, 291));
    this.add(jLabel1,  new XYConstraints(493, 20, -1, -1));
    this.add(optionalPanel,    new XYConstraints(13, 98, 135, 238));
    //displayedScroll.getViewport().add(displayedList, null);
    //optionalPanel.add(rename,  new XYConstraints(0, 70, -1, -1));
    optionalPanel.add(jLabel4, new XYConstraints(0, 0, -1, -1));
    optionalPanel.add(tf_shortName,  new XYConstraints(0, 24, 119, -1));

    optionalPanel.add(new JLabel("Type:"), new XYConstraints(0, 60, 119, -1));
    optionalPanel.add(combo_type, new XYConstraints(0, 80, 119, -1));

    //optionalPanel.add(delete,   new XYConstraints(0, 111, 91, -1));
    //    optionalPanel.add(bottomDisplay,      new XYConstraints(3, 155, 116, 43));
        this.add(jLabel2, new XYConstraints(172, 17, -1, -1));
        this.add(jPanel1,  new XYConstraints(9, 34, 133, 215));
        this.add(availableScroll, new XYConstraints(154, 51, 181, 291));
    this.add(add, new XYConstraints(377, 96, 46, 29));
    this.add(up, new XYConstraints(377, 154, 46, 29));
    this.add(down, new XYConstraints(377, 205, 46, 29));
    this.add(remove, new XYConstraints(377, 255, 46, 29));

  }



  // get currently selected property from displayed list
  public synchronized String [] getDisplayedSelected()
  {
    Object[] values = (Object[]) displayedList.getSelectedValues();
    String[] propNames = new String[values.length];
    for (int i=0; i<values.length; i++){
      propNames[i] = (String) values[i];
    }
   return propNames;

  }

  // get currently selected property from available list
  public synchronized String[] getAvailableSelected()
  {
     Object[] values = (Object[]) availableList.getSelectedValues();
     String[] propNames = new String[values.length];
     for (int i=0; i<values.length; i++){
       propNames[i] = (String) values[i];
     }
    return propNames;
  }

/*
  public synchronized PropertyDisplayOption buildCurrentAvailable() {
    String propName = (String)availableList.getSelectedValue();

    String tmpName=propName;
    String suffix=null;

    if (propName == null)
     return null;
    if (propName.endsWith(PFREDConstant.PROP_RANGE) ) {
      suffix = PFREDConstant.PROP_RANGE;
      tmpName = propName.substring(0, propName.indexOf(PFREDConstant.PROP_RANGE));
    }
    else if (propName.endsWith(PFREDConstant.PROP_DISTRIBUTION)) {
      suffix = PFREDConstant.PROP_DISTRIBUTION;
      tmpName = propName.substring(0, propName.indexOf(PFREDConstant.PROP_DISTRIBUTION));
    }
    else if (propName.endsWith(PFREDConstant.PROP_AVERAGE)) {
      suffix = PFREDConstant.PROP_AVERAGE ;
      tmpName = propName.substring(0, propName.indexOf(PFREDConstant.PROP_AVERAGE));
    }
    else if (propName.endsWith(PFREDConstant.PROP_MIN)) {
      suffix = PFREDConstant.PROP_MIN ;
      tmpName = propName.substring(0, propName.indexOf(PFREDConstant.PROP_MIN));
    }
    else if (propName.endsWith(PFREDConstant.PROP_MAX)) {
      suffix = PFREDConstant.PROP_MAX ;
      tmpName = propName.substring(0, propName.indexOf(PFREDConstant.PROP_MAX));
    }


    PropertyDisplayOption opt = value.getPropertyDisplayOptionTyped(tmpName, suffix);
    if (suffix != null)
      opt.tf_shortName = suffix; //tmp
    return opt;
  }*/



  // on any keypress in either editable text box
  // do an "applyChanges"
  public void applyChanges()
  {
    // no need to validate...accept any values for short/pretty name
    PropertyDisplayOption opt = manager.getPropertyDisplayOption(tf_name.getText());
    if (opt == null)
      return;
    String sName = tf_shortName.getText();
    //String pName = prettyName.getText();
    opt.shortName=sName;
  }

  // on combo change update
  private boolean comboBoxAdjusting = false;
  public void updateType(String typeName)
  {
      comboBoxAdjusting = true;
      combo_type.setSelectedItem(typeName);
      comboBoxAdjusting = false;
  }
  public void applyTypeChange()
  {
      if (comboBoxAdjusting)
          return;

      PropertyDisplayOption opt = manager.getPropertyDisplayOption(tf_name.getText());
      if (opt == null)
        return;
      String typeValue = (String)combo_type.getSelectedItem();
      opt.setTypeFromName(typeValue);
  }

  // keypress in text field
  public void keyTyped(KeyEvent e)
  {
  }
  public void keyPressed(KeyEvent e){}
  public void keyReleased(KeyEvent e)
  {
    applyChanges();
  }

  /* rename currently selected property
  public void renameProperty(int index, PropertyDisplayOption opt, String newName)
  {
    // we must rename it in every mol
    props.renameProperty(opt.name,newName);

    // also we must rename it in the displayProperty itself
    opt.name = newName;

    // and also reload the GUI list
    sync();

    // make this selected again
    availableList.setSelectedIndex(index);

    //update the FP metrics because a new property name might be picked up as an fingerprint property
    props.updateDistMetricsFromMolData();
  }*/

  /* if this property is already in use
  public boolean  (String newName)
  {
    Iterator iter = value.propertyOptions.iterator();
    while (iter.hasNext())
    {
      PropertyDisplayOption opt = (PropertyDisplayOption)iter.next();
      if (opt.name.equalsIgnoreCase(newName))
        return true;
    }

    return false;
  }*/

  // button press
public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == add)
    {
      addToDisplayed();
    }
    else if (e.getSource() == remove)
    {
     removeFromDisplayed();
    }
    else if (e.getSource() == up) {
      moveDisplayedSelectedUp();
    }
    else if (e.getSource() == down) {
      moveDisplayedSelectedDown();
    }
    else if (e.getSource() == combo_type) {
        applyTypeChange();
    }
  }

  public void moveDisplayedSelectedUp() {
    Object[] values = (Object[]) displayedList.getSelectedValues();
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        manager.moveDisplayOptionUp(values[i].toString());
      }
    }
    syncDisplayed();
    syncDisplayedSelections();
  }

  public void moveDisplayedSelectedDown() {
    Object[] values = (Object[]) displayedList.getSelectedValues();
   if (values != null) {
     for (int i = values.length-1; i >=0; i--) {
       manager.moveDisplayOptionDown(values[i].toString());
     }
   }
   syncDisplayed();
   syncDisplayedSelections();
  }


  public void addToDisplayed() {
    // remove it if it already has it
     String [] availableSelected = getAvailableSelected();
     for (int i=0; i<availableSelected.length; i++){
       String propName = availableSelected[i];
       if (manager.isPropertyDisplayed(propName)) continue;
       manager.addDisplayedOption(propName);
     }
     syncDisplayed();
     syncDisplayedSelections();
  }

  public void syncDisplayed(){
    displayedListAdjusting = true;
    displayedList.setListData(manager.getDisplayedNames());
    displayedListAdjusting = false;
  }

  public void syncDisplayedSelections(){
    String [] availableSelected = getAvailableSelected();
    String [] displayedPropNames = manager.getDisplayedNames();
    displayedList.setValueIsAdjusting(true);
    displayedList.clearSelection();
    for (int i=0; i<availableSelected.length; i++){
      for (int j=0; j<displayedPropNames.length; j++){
        if (availableSelected[i].equals(displayedPropNames[j])) {
          displayedList.addSelectionInterval(j, j);
          break;
        }
      }
    }
    displayedList.setValueIsAdjusting(false);
  }


  public void syncAvailableSelections(){
    String [] displayedSelected = getDisplayedSelected();
    String [] availablePropNames = manager.getAllPropertyNames();
    availableList.setValueIsAdjusting(true);
    availableList.clearSelection();
    for (int i=0; i<displayedSelected.length; i++){
      for (int j=0; j<availablePropNames.length; j++){
        if (displayedSelected[i].equals(availablePropNames[j])) {
          availableList.addSelectionInterval(j, j);
          break;
        }
      }
    }
    availableList.setValueIsAdjusting(false);
  }


  public void removeFromDisplayed() {
    // remove it
     Object[] values = (Object[]) displayedList.getSelectedValues();
     if (values != null)
     {
       for (int i=0; i<values.length; i++) {
         manager.removeDisplayedOption(values[i].toString());
       }

       // now sync interface
       syncDisplayed();
       syncDisplayedSelections();

      }
  }


  // list selection
  public void valueChanged(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting())
      return;
    if (e.getSource() == availableList  && !availableListAdjusting) {
      int idx = availableList.getMaxSelectionIndex();
      if (idx<0) return;
      String name = (String) availableList.getModel().getElementAt(idx);
      PropertyDisplayOption opt = manager.getPropertyDisplayOption(name);
      if (opt==null) return;

      tf_name.setText(opt.name);
      tf_shortName.setText(opt.shortName);
      updateType(opt.getTypeName());

     // avoid dead lock
        availableListAdjusting=true;
        syncDisplayedSelections();
        availableListAdjusting =false;

    }
    else if (e.getSource() == displayedList && !displayedListAdjusting) {
      displayedListAdjusting = true;
      syncAvailableSelections();
      displayedListAdjusting = false;
    }

  }
}
