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
package org.pfred.sort;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


import javax.swing.JButton;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import org.pfred.*;
import org.pfred.model.CustomListModel;


public class SortCompoundsMultiPropDialog extends JDialog implements ActionListener
{

    protected Frame owner = null;

    private JList operatorCB1 = null;
    private JComboBox propertyCB1 = null;
   // private JCheckBox numericCheckBox1 = null;

    private JList operatorCB2 = null;
    private JComboBox propertyCB2 = null;
    //private JCheckBox numericCheckBox2= null;

    private JList operatorCB3 = null;
    private JComboBox propertyCB3 = null;
   //private JCheckBox numericCheckBox3 = null;

    private JList operatorCB4 = null;
    private JComboBox propertyCB4 = null;
    //private JCheckBox numericCheckBox4 = null;

    private JButton ok_button = null;
    private JButton cancel_button = null;

    private PFREDContext context = null;

    public SortCompoundsMultiPropDialog(PFREDContext context,Frame owner)
    {
        super(owner, "Sort Compounds");

        this.setModal(true);
        this.context = context;
        this.owner = owner;

        buildGUI();
    }

    private void buildGUI() {


        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BoxLayout(overallPanel,BoxLayout.PAGE_AXIS));


        CustomListModel cmpd_model = context.getDataStore().getOligoListModel();
        String[] propNames = cmpd_model.getAllPropertyNames();


        String[] operatorNames = { PFREDConstant.INCREASING, PFREDConstant.DECREASING };


        operatorCB1 = new JList(operatorNames);
        operatorCB1.setSelectedIndex(0);
        propertyCB1 = new JComboBox(propNames);
        //numericCheckBox1 = new JCheckBox("Numeric",true);

        String[] propNames2 = new String[propNames.length+1];
        propNames2[0]="(None)";
        System.arraycopy(propNames,0,propNames2,1,propNames.length);


        operatorCB2 = new JList(operatorNames);
        operatorCB2.setSelectedIndex(0);
        propertyCB2 = new JComboBox(propNames2);
        //numericCheckBox2 = new JCheckBox("Numeric",true);

        String[] propNames3 = new String[propNames.length+1];
        propNames3[0]="(None)";
        System.arraycopy(propNames,0,propNames3,1,propNames.length);


        operatorCB3 = new JList(operatorNames);
        operatorCB3.setSelectedIndex(0);
        propertyCB3 = new JComboBox(propNames3);
        //numericCheckBox3 = new JCheckBox("Numeric",true);

        String[] propNames4 = new String[propNames.length+1];
        propNames4[0]="(None)";
        System.arraycopy(propNames,0,propNames4,1,propNames.length);

        operatorCB4 = new JList(operatorNames);
        operatorCB4.setSelectedIndex(0);
        propertyCB4 = new JComboBox(propNames4);
        //numericCheckBox4 = new JCheckBox("Numeric",true);


        JPanel sortLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortLabelPanel.add(new JLabel("Sort By:"));

        JPanel thenbyLabelPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thenbyLabelPanel1.add(new JLabel("Then by:"));

        JPanel thenbyLabelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thenbyLabelPanel2.add(new JLabel("Then by:"));

        JPanel thenbyLabelPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thenbyLabelPanel3.add(new JLabel("Then by:"));


        JPanel selectionPanel1 = new JPanel();
        selectionPanel1.add(operatorCB1);
        selectionPanel1.add(propertyCB1);
       // selectionPanel1.add(numericCheckBox1);

        JPanel selectionPanel2 = new JPanel();
        selectionPanel2.add(operatorCB2);
        selectionPanel2.add(propertyCB2);
      //  selectionPanel2.add(numericCheckBox2);

        JPanel selectionPanel3 = new JPanel();
        selectionPanel3.add(operatorCB3);
        selectionPanel3.add(propertyCB3);
        //selectionPanel3.add(numericCheckBox3);

        JPanel selectionPanel4 = new JPanel();
        selectionPanel4.add(operatorCB4);
        selectionPanel4.add(propertyCB4);
        //selectionPanel4.add(numericCheckBox4);



        JPanel subpanel = new JPanel();
        ok_button = new JButton("Sort");
        if (propNames ==null || propNames.length <1)
            ok_button.setEnabled(false);
        ok_button.addActionListener(this);
        subpanel.add(ok_button);

        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);



        overallPanel.add(sortLabelPanel);
        overallPanel.add(selectionPanel1);
        overallPanel.add(thenbyLabelPanel1);
        overallPanel.add(selectionPanel2);
        overallPanel.add(thenbyLabelPanel2);
        overallPanel.add(selectionPanel3);
        overallPanel.add(thenbyLabelPanel3);
        overallPanel.add(selectionPanel4);
        overallPanel.add(subpanel);

        contentPane.add(overallPanel, BorderLayout.NORTH);

        setLocationRelativeTo(owner);
        pack();
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == ok_button) {

        	ArrayList selectedPropertiesList = new ArrayList();

            String opName = null;
            String propName =  null;
           // boolean numeric =  false;
            SelSortCompPropDTO selSortCompPropDTO = null;



                opName = operatorCB1.getSelectedValue().toString().trim();
                propName = propertyCB1.getSelectedItem().toString().trim();
               // numeric = numericCheckBox1.isSelected();

                selSortCompPropDTO = new SelSortCompPropDTO();
                selSortCompPropDTO.setOpName(opName);
                selSortCompPropDTO.setPropName(propName);
                //selSortCompPropDTO.setNumeric(numeric);
                selectedPropertiesList.add(selSortCompPropDTO);



           if ( propertyCB2.getSelectedIndex() != 0 ){

                opName = operatorCB2.getSelectedValue().toString().trim();
                propName = propertyCB2.getSelectedItem().toString().trim();
              //  numeric = numericCheckBox2.isSelected();

                selSortCompPropDTO = new SelSortCompPropDTO();
                selSortCompPropDTO.setOpName(opName);
                selSortCompPropDTO.setPropName(propName);
                //selSortCompPropDTO.setNumeric(numeric);
                selectedPropertiesList.add(selSortCompPropDTO);

           }

           if ( propertyCB3.getSelectedIndex() != 0 ){

               opName = operatorCB3.getSelectedValue().toString().trim();
               propName = propertyCB3.getSelectedItem().toString().trim();
              // numeric = numericCheckBox3.isSelected();

               selSortCompPropDTO = new SelSortCompPropDTO();
               selSortCompPropDTO.setOpName(opName);
               selSortCompPropDTO.setPropName(propName);
              // selSortCompPropDTO.setNumeric(numeric);
               selectedPropertiesList.add(selSortCompPropDTO);

          }

           if ( propertyCB4.getSelectedIndex() != 0 ){

               opName = operatorCB4.getSelectedValue().toString().trim();
               propName = propertyCB4.getSelectedItem().toString().trim();
              // numeric = numericCheckBox4.isSelected();

               selSortCompPropDTO = new SelSortCompPropDTO();
               selSortCompPropDTO.setOpName(opName);
               selSortCompPropDTO.setPropName(propName);
              // selSortCompPropDTO.setNumeric(numeric);
               selectedPropertiesList.add(selSortCompPropDTO);

          }

           SortCompPropCmd.execute(context,selectedPropertiesList);

        }

        dispose();

    }
}

