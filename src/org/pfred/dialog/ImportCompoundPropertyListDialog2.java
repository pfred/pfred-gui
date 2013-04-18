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
package org.pfred.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import com.pfizer.rtc.util.SpringUtilities;
import org.pfred.PFREDConstant;
import org.pfred.model.Oligo;



public class ImportCompoundPropertyListDialog2  extends JDialog implements ActionListener {


    private JFrame owner;
    private JList jList1 = new JList();


    private JButton jButtonOK = new JButton();
    private JButton jButtonCancel = new JButton();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private JLabel jLabel3 = new JLabel();

    private String msg;
    private String msglabel2;
    private String msglabel1;
    private String[] properties;
    private String[] availableproperties;
    private String[] selected;
    private String[] selected_cols;
    private boolean canceled=true;
    private JComboBox existingColumns = new JComboBox();
    private JComboBox availableProp = new JComboBox();
    private String mappedPropertySelected;
    private String nameFieldSelected;
    private JCheckBox jcb_isNamePfizerId = new JCheckBox();
    private JLabel jLabel4 = new JLabel();
    private JScrollPane scrollPane = new JScrollPane(jList1);

    private JRadioButton jrb_replace = new JRadioButton("replace with new value");
    private JRadioButton jrb_keep = new JRadioButton("keep existing");
    private JRadioButton jrb_merge = new JRadioButton("merge two values");
    private String[] excludeNames=new String[]{Oligo.ANTISENSE_OLIGO_PROP,Oligo.SENSE_OLIGO_PROP, Oligo.DNA_OLIGO_PROP, 
    		Oligo.PARENT_ANTISENSE_OLIGO_PROP, Oligo.PARENT_DNA_OLIGO_PROP, Oligo.PARENT_SENSE_OLIGO_PROP, Oligo.REGISTERED_ANTISENSE_OLIGO_PROP, Oligo.REGISTERED_SENSE_OLIGO_PROP,
    		Oligo.RNA_NOTATION_PROP};
    



    public ImportCompoundPropertyListDialog2(JFrame owner,
                                             String title,
                                             String msg,String msg1,
                                             String msg2, String[] properties,
                                             String[] availableproperties) {
        super(owner);
        setModal(true);
        this.owner = owner;
        this.msg = msg;
        this.msglabel1=msg1;
        this.msglabel2=msg2;
        this.setTitle(title);
        this.properties = properties;
        this.availableproperties = availableproperties;
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean validateName(String name){
    	for (int i=0; i<excludeNames.length; i++){
    		if (name.equals(excludeNames[i])){
    			return false;
    		}
    	}
    	return true;
    }
    
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == jButtonOK)
        {

            nameFieldSelected = existingColumns.getSelectedItem().toString();
            int nameFieldIndex = existingColumns.getSelectedIndex();
            jList1.addSelectionInterval(nameFieldIndex,nameFieldIndex); //make sure name index is selected;

            mappedPropertySelected = availableProp.getSelectedItem().toString();

            Object[] objs = jList1.getSelectedValues();
            
           
            if ((nameFieldSelected==null )||(mappedPropertySelected==null)|| (objs.length == 0))
            {
                JOptionPane.showMessageDialog(this, "Please select Id Field, Matching Id field and Properties to Import.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selected = new String[objs.length];
            selected_cols = new String[objs.length];

            //selColumn.add(nameFieldSelected);

            ArrayList<String> cleanNames=new ArrayList();
            boolean foundReservedName=false;
            String reservedName=null;
            for (int i=0; i<objs.length; i++)
            {
                selected_cols[i]=objs[i].toString();
                if(!validateName(selected_cols[i])){
                	foundReservedName=true;
                	reservedName=selected_cols[i];
                }else{
                	cleanNames.add(selected_cols[i]);
                }
                
            }
            
            if(foundReservedName)
            	JOptionPane.showMessageDialog(this, "Some column names, e.g."+ reservedName+", are reserved name in PFRED and these columns won't imported.","Warning", JOptionPane.WARNING_MESSAGE);
        	
            selected_cols=cleanNames.toArray(new String[]{});
            if ((selected_cols.length == 1)){
                JOptionPane.showMessageDialog(this, "Selected Property to Import is same as the Id Field. Please select different property to import.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            canceled =false;
            dispose();
        }
        else if (evt.getSource() == jButtonCancel)
        {
            dispose();
        }
    }
    public boolean isCanceled ()
    {
        return canceled;
    }

    public String getMappedPropertySelected ()
    {
        return mappedPropertySelected;
    }

    public String getNameFieldSelected ()
    {
        return nameFieldSelected;
    }

    public boolean isNamePfizerId(){
      return jcb_isNamePfizerId.isSelected();
    }
/*
    public boolean lookupStructure(){
      return jcb_lookupStructure.isSelected();
    }
*/
    public int getMergeOption(){
      if (jrb_keep.isSelected()){
        return PFREDConstant.KEEP_ORIGINAL;
      }else if (jrb_merge.isSelected()){
        return PFREDConstant.MERGE;
      }else {
        return PFREDConstant.REPLACE;
      }
    }
    /**
     * this will trigger the dialog and return user selections;
     */


    public String[] getSelColumn( )
    {
        setLocationRelativeTo(owner);
        setVisible(true);
        return selected_cols;
    }

    private void jbInit() throws Exception {


        this.getContentPane().setLayout(new BorderLayout());
        JPanel mainPane=new JPanel(new SpringLayout());
        mainPane.setBorder(BorderFactory.createEtchedBorder());
        this.getContentPane().add(mainPane, BorderLayout.CENTER);
        jButtonOK.setText("OK");
        jButtonCancel.setText("Cancel");
        jLabel1.setText(msg);
        jLabel2.setText(msglabel1);
        jLabel3.setText(msglabel2);


        jcb_isNamePfizerId.setSelected(false);
        jcb_isNamePfizerId.setVisible(false);
        jcb_isNamePfizerId.setText("Is this a Pfizer cmpd id field? (If checked, parent name matching rules will be used)");


        mainPane.add(jLabel1);
        mainPane.add(existingColumns);
        mainPane.add(jLabel2);
        mainPane.add(availableProp);

        JPanel buttonPane=new JPanel();
        buttonPane.add(jButtonCancel);
        buttonPane.add(jButtonOK);
        this.getContentPane().add(buttonPane, BorderLayout.SOUTH);

        mainPane.add(jcb_isNamePfizerId);
        mainPane.add(jLabel4);
        mainPane.add(jLabel3);
        mainPane.add(scrollPane);



        JPanel questionPane=new JPanel();
        questionPane.setLayout(new SpringLayout());
        mainPane.add(questionPane);
        questionPane.add(new JLabel("If property already exists in PFRED, "));
        questionPane.add(jrb_keep);
        questionPane.add(jrb_replace);
        questionPane.add(jrb_merge);
        ButtonGroup bg=new ButtonGroup();
        bg.add(jrb_keep);
        bg.add(jrb_replace);
        bg.add(jrb_merge);
        jrb_replace.setSelected(true);
        SpringUtilities.makeCompactGrid(questionPane, 4,1,1,1,3,3);

        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jButtonOK.addActionListener(this);
        jButtonCancel.addActionListener(this);


        for (int i=0;i<properties.length;i++){
        		existingColumns.addItem(properties[i]);
        }
        jList1.setListData(properties);
        jList1.setSelectionInterval(0, properties.length-1);


        for (int colindex=0;colindex<availableproperties.length;colindex++){
        	availableProp.addItem(availableproperties[colindex]);
        }

        SpringUtilities.makeCompactGrid(mainPane, //parent
                            9,1,  //nx1 grid
                            1, 1,  //initX, initY
                            3, 3); //xPad, yPad


        pack();

    }

    public static void main(String[] args){
      try{
        com.jgoodies.looks.LookUtils.setLookAndTheme(
            new com.jgoodies.looks.plastic.Plastic3DLookAndFeel(),
            new com.jgoodies.looks.plastic.theme.DesertBluer());
      }catch (Exception ex){
        ex.printStackTrace();
      }
      ImportCompoundPropertyListDialog2 dialog
          = new ImportCompoundPropertyListDialog2(
          null,
          "Select Id and Properties to Import",
          "New properties: Select unique ID field",
          "Existing properties: Select unique ID field",
          "Please select new properties to import:",
          new String[]{"test","test2"},
          new String[]{"test","test2"});
      dialog.setVisible(true);

    }



}
