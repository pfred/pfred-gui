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

package org.pfred;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import org.pfred.group.GroupListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Datum;
import org.pfred.model.OligoListModel;
import com.pfizer.rtc.task.ProgressReporter;
import com.pfizer.rtc.task.ProgressWorker;
import com.pfizer.rtc.util.ErrorDialog;
import javax.swing.JRadioButton;
import java.util.HashMap;


public class SearchFrame extends JFrame implements ActionListener, ProgressWorker {

    private PFREDDataStore dataStore;
    private JButton btnSSS = new JButton();
    private JButton btnLoadPrev = new JButton();
    private XYLayout xYLayout1 = new XYLayout();
    private JFrame parent;
    private JPanel jPanel1 = new JPanel();
    private TitledBorder titledBorder1;
    private JComboBox jComboBox1a = new JComboBox();
    private XYLayout xYLayout2 = new XYLayout();
    private JComboBox jComboBox1b = new JComboBox();
    private JComboBox jComboBox3b = new JComboBox();
    private JComboBox jComboBox2a = new JComboBox();
    private JComboBox jComboBox2b = new JComboBox();
    private JComboBox jComboBox3a = new JComboBox();
    private JComboBox jComboBox4b = new JComboBox();
    private JComboBox jComboBox4a = new JComboBox();
    private JTextField jTextField1c = new JTextField();
    private JTextField jTextField3c = new JTextField();
    private JTextField jTextField2c = new JTextField();
    private JTextField jTextField4c = new JTextField();
    private JPanel jPanel3 = new JPanel();
    private TitledBorder titledBorder3;
    private XYLayout xYLayout3 = new XYLayout();
  //  private Border border4;
    private JComboBox jComboBox1 = new JComboBox();
    private JComboBox jComboBox2 = new JComboBox();
    private JComboBox jComboBox3 = new JComboBox();
    JPanel jPanel4 = new JPanel();
    Border border5;
    TitledBorder titledBorder4;
    JCheckBox jcbProperty = new JCheckBox();
    XYLayout xYLayout5 = new XYLayout();
    JCheckBox jcbGroup = new JCheckBox();
    private OligoListModel list_model;
    private CustomListSelectionModel sel_model;
    private GroupListModel group_model;
    private SearchFrame prevSSSFrame;
    JRadioButton jrb_and = new JRadioButton();
    JRadioButton jrb_or = new JRadioButton();
    JLabel jLabel1 = new JLabel();

    public SearchFrame(PFREDDataStore dataStore, JFrame parent, SearchFrame prevSSSFrame) {
        setTitle("Search Compound");
        this.parent = parent;
        this.dataStore = dataStore;
        this.prevSSSFrame = prevSSSFrame;

        list_model = dataStore.getOligoListModel();
        sel_model = dataStore.getOligoListSelectionModel();
        group_model = dataStore.getGroupListModel();

        getContentPane().setLayout(new BorderLayout());
        try {
            jbInit();

        } catch (Exception e) {
            e.printStackTrace();
        }

        pack();





    }

    private void jbInit() throws Exception {

        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "Property Search");
        titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), "Group Search");

        border5 = BorderFactory.createEtchedBorder(Color.white,
                new Color(148, 145, 140));
        titledBorder4 = new TitledBorder(border5, "Search Options");
        btnSSS.setFont(new java.awt.Font("Dialog", 1, 12));
        btnSSS.setBorder(BorderFactory.createRaisedBevelBorder());
        btnSSS.setText("Search");
        btnSSS.addActionListener(this);
        this.getContentPane().setLayout(xYLayout1);
  
        btnLoadPrev.setFont(new java.awt.Font("Dialog", 1, 12));
        btnLoadPrev.setBorder(BorderFactory.createRaisedBevelBorder());
        btnLoadPrev.setText("Load Prev");
        btnLoadPrev.setEnabled(prevSSSFrame != null);
        btnLoadPrev.addActionListener(this);

        //populate the property search box
        String[] propNames = list_model.getAllPropertyNames();

        if (propNames != null) {
            jComboBox1a.addItem("");
            jComboBox2a.addItem("");
            jComboBox3a.addItem("");
            jComboBox4a.addItem("");
            for (int i = 0; i < propNames.length; i++) {
                jComboBox1a.addItem(propNames[i]);
                jComboBox2a.addItem(propNames[i]);
                jComboBox3a.addItem(propNames[i]);
                jComboBox4a.addItem(propNames[i]);
            }

            jComboBox1b = new JComboBox(new Object[]{">", "<", "=", "matches",
                        "exact matches"});
            jComboBox2b = new JComboBox(new Object[]{">", "<", "=", "matches",
                        "exact matches"});
            jComboBox3b = new JComboBox(new Object[]{">", "<", "=", "matches",
                        "exact matches"});
            jComboBox4b = new JComboBox(new Object[]{">", "<", "=", "matches",
                        "exact matches"});
        }

        //populate the group logic box
        String[] groupNames = group_model.getGroupNames();
        String[] operatorNames = {
            PFREDConstant.AND, PFREDConstant.OR,
            PFREDConstant.BUT_NOT};
        if (propNames != null) {
            jComboBox1.addItem(PFREDConstant.ALL_COMPOUNDS);
            jComboBox1.addItem(PFREDConstant.SELECTED_COMPOUNDS);
            jComboBox3.addItem("");
            for (int i = 0; i < groupNames.length; i++) {
                jComboBox1.addItem(groupNames[i]);
                jComboBox3.addItem(groupNames[i]);
            }
            jComboBox2 = new JComboBox(new Object[]{"", PFREDConstant.AND,
                        PFREDConstant.OR, PFREDConstant.BUT_NOT});
        }
        xYLayout1.setWidth(481);
        xYLayout1.setHeight(316);
        jPanel1.setBorder(titledBorder1);
        jPanel1.setLayout(xYLayout2);
        jPanel3.setBorder(titledBorder3);
        jPanel3.setLayout(xYLayout3);
        jPanel4.setBorder(titledBorder4);
        jPanel4.setLayout(xYLayout5);
        jcbProperty.setSelected(true);
        jcbProperty.setText("Property");
        jcbGroup.setSelected(true);
        jcbGroup.setText("Group");
        jrb_and.setToolTipText("");
        jrb_and.setText("AND");
        jrb_or.setText("OR");
        jLabel1.setToolTipText("");
        jLabel1.setText("Logic:");

        ButtonGroup bg = new ButtonGroup();
        bg.add(jrb_and);
        bg.add(jrb_or);
        jrb_and.setSelected(true);


        jPanel4.add(jcbProperty, new XYConstraints(138, 0, -1, 22));
        jPanel4.add(jcbGroup, new XYConstraints(228, 0, -1, 22));
        jPanel3.add(jComboBox2, new XYConstraints(203, 0, 59, 20));
        jPanel3.add(jComboBox1, new XYConstraints(0, 0, 201, 20));
        jPanel3.add(jComboBox3, new XYConstraints(262, 1, 183, 20));
        this.getContentPane().add(jPanel4, new XYConstraints(11, 220, 465, -1));
        this.getContentPane().add(btnLoadPrev, new XYConstraints(343, 277, 80, 28));
        this.getContentPane().add(jPanel3, new XYConstraints(9, 158, 464, 58));
        jPanel1.add(jComboBox4a, new XYConstraints(0, 83, 292, 20));
        jPanel1.add(jComboBox1a, new XYConstraints(0, 21, 291, 20));
        jPanel1.add(jComboBox1b, new XYConstraints(292, 21, 80, 20));
        jPanel1.add(jTextField1c, new XYConstraints(373, 21, 80, 20));
        jPanel1.add(jTextField2c, new XYConstraints(373, 41, 80, 20));
        jPanel1.add(jTextField3c, new XYConstraints(373, 62, 80, 20));
        jPanel1.add(jTextField4c, new XYConstraints(373, 82, 80, 20));
        jPanel1.add(jComboBox4b, new XYConstraints(292, 83, 80, 20));
        jPanel1.add(jComboBox3b, new XYConstraints(292, 62, 80, 20));
        jPanel1.add(jComboBox2a, new XYConstraints(0, 42, 291, 20));
        jPanel1.add(jComboBox3a, new XYConstraints(0, 62, 291, 20));
        jPanel1.add(jComboBox2b, new XYConstraints(292, 41, 80, 20));
        this.getContentPane().add(jPanel1, new XYConstraints(5, 9, 465, 141));
        this.getContentPane().add(btnSSS, new XYConstraints(116, 278, 216, 28));
        jPanel1.add(jrb_or, new XYConstraints(124, 3, 64, 16));
        jPanel1.add(jrb_and, new XYConstraints(54, 3, 65, 16));
        jPanel1.add(jLabel1, new XYConstraints(19, 4, 36, -1));
    }

    public void actionPerformed(ActionEvent evt) {

        if (evt.getSource() == btnLoadPrev) {
            int opt = JOptionPane.showConfirmDialog(this, "Load Previous Search?",
                    "Confirm Load", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                // load it
                loadFromPrevious();

                // now we can't load it again...
                prevSSSFrame = null;
                btnLoadPrev.setEnabled(false);
            }
        }
        if (evt.getSource() == btnSSS) {
            ArrayList hits = null;
            //now let's do step wise filtering
            //1. group logics
            if (jcbGroup.isSelected()) {
                if (jComboBox2.getSelectedIndex() == 0) { //no logical operator set
                    String gName = jComboBox1.getSelectedItem().toString().trim();
                    hits = group_model.getMolsByGroupName(gName);
                } else if (jComboBox2.getSelectedIndex() != 0 &&
                        jComboBox3.getSelectedIndex() != 0) { //group logical operator is set
                    String aName = jComboBox1.getSelectedItem().toString().trim();
                    String bName = jComboBox3.getSelectedItem().toString().trim();
                    String opName = jComboBox2.getSelectedItem().toString().trim();

                    hits = group_model.getMolsByGroup(aName, opName, bName);
                }
                if (hits == null || hits.size() == 0) {
                    System.out.println("Group Search left with 0.");
                    showMsg("No hits found. Group Search left with 0.");
                    return; //nothing found, return
                }
                System.out.println("Group Search left with " + hits.size());
            } else {
                hits = list_model.getAllData();
            }


            //2. property filter
            if (jcbProperty.isSelected()) {
                if (isAndLogic()) {
                    if (jComboBox1a.getSelectedIndex() != 0 &&
                            jTextField1c.getText().trim().length() > 0) {
                        String propName = jComboBox1a.getSelectedItem().toString().trim();
                        String comparator = jComboBox1b.getSelectedItem().toString().
                                trim();
                        String query = jTextField1c.getText().trim();
                        hits = searchByProp(propName, comparator, query, hits);
                    }
                    if (hits == null || hits.size() == 0) {
                        System.out.println("Property search left with 0.");
                        showMsg("No hits found. Property Search left with 0.");
                        return; //nothing found, return
                    }

                    if (jComboBox2a.getSelectedIndex() != 0 &&
                            jTextField2c.getText().trim().length() > 0) {
                        String propName = jComboBox2a.getSelectedItem().toString().trim();
                        String comparator = jComboBox2b.getSelectedItem().toString().
                                trim();
                        String query = jTextField2c.getText().trim();
                        hits = searchByProp(propName, comparator, query, hits);
                    }
                    if (hits == null || hits.size() == 0) {
                        showMsg("No hits found. Property Search left with 0.");
                        System.out.println("Property Search left with 0.");
                        return; //nothing found, return
                    }

                    if (jComboBox3a.getSelectedIndex() != 0 &&
                            jTextField3c.getText().trim().length() > 0) {
                        String propName = jComboBox3a.getSelectedItem().toString().trim();
                        String comparator = jComboBox3b.getSelectedItem().toString().
                                trim();
                        String query = jTextField3c.getText().trim();
                        hits = searchByProp(propName, comparator, query, hits);
                    }
                    if (hits == null || hits.size() == 0) {
                        System.out.println("Property search left with 0");
                        showMsg("No hits found. Property search left with 0");
                        return; //nothing found, return
                    }

                    if (jComboBox4a.getSelectedIndex() != 0 &&
                            jTextField4c.getText().trim().length() > 0) {
                        String propName = jComboBox4a.getSelectedItem().toString().trim();
                        String comparator = jComboBox4b.getSelectedItem().toString().
                                trim();
                        String query = jTextField4c.getText().trim();
                        hits = searchByProp(propName, comparator, query, hits);
                    }
                    if (hits == null || hits.size() == 0) {
                        System.out.println("Property search left with 0");
                        showMsg("No hits found. Property search left with 0.");
                        return; //nothing found, return
                    }
                    System.out.println("Property search left with " + hits.size());
                } else {
                    ArrayList originalSet = hits;
                    ArrayList resultSet = new ArrayList();
                    if (jComboBox1a.getSelectedIndex() != 0 &&
                            jTextField1c.getText().trim().length() > 0) {
                        String propName = jComboBox1a.getSelectedItem().toString().trim();
                        String comparator = jComboBox1b.getSelectedItem().toString().trim();
                        String query = jTextField1c.getText().trim();
                        hits = searchByProp(propName, comparator, query, originalSet);
                        resultSet = mergeSets(resultSet, hits);
                    }



                    if (jComboBox2a.getSelectedIndex() != 0 &&
                            jTextField2c.getText().trim().length() > 0) {
                        String propName = jComboBox2a.getSelectedItem().toString().trim();
                        String comparator = jComboBox2b.getSelectedItem().toString().
                                trim();
                        String query = jTextField2c.getText().trim();
                        hits = searchByProp(propName, comparator, query, originalSet);
                        resultSet = mergeSets(resultSet, hits);
                    }



                    if (jComboBox3a.getSelectedIndex() != 0 &&
                            jTextField3c.getText().trim().length() > 0) {
                        String propName = jComboBox3a.getSelectedItem().toString().trim();
                        String comparator = jComboBox3b.getSelectedItem().toString().
                                trim();
                        String query = jTextField3c.getText().trim();
                        hits = searchByProp(propName, comparator, query, originalSet);
                        resultSet = mergeSets(resultSet, hits);
                    }



                    if (jComboBox4a.getSelectedIndex() != 0 &&
                            jTextField4c.getText().trim().length() > 0) {
                        String propName = jComboBox4a.getSelectedItem().toString().trim();
                        String comparator = jComboBox4b.getSelectedItem().toString().
                                trim();
                        String query = jTextField4c.getText().trim();
                        hits = searchByProp(propName, comparator, query, originalSet);
                        resultSet = mergeSets(resultSet, hits);
                    }



                    if (resultSet == null || resultSet.size() == 0) {
                        System.out.println("Property search left with 0");
                        showMsg("No hits found. Property search left with 0.");
                        return; //nothing found, return
                    }
                    System.out.println("Property search left with " + hits.size());
                    hits = resultSet;
                }

            }


            if (hits.size() == 1) {
                showMsg(hits.size() + " hit found.");
            } else if (hits.size() > 1) {
                showMsg(hits.size() + " hits found.");
            }
            sel_model.setValueIsAdjusting(true);
            sel_model.selectData(hits, false);
            sel_model.setValueIsAdjusting(false);
            System.out.println(hits.size() + " hits found at the end");
        }
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private boolean isAndLogic() {
        return jrb_and.isSelected();
    }

    private ArrayList mergeSets(ArrayList set1, ArrayList set2) {
        if (set2 == null) {
            return set1;
        }
        ArrayList combined = new ArrayList();
        combined.addAll(set1);
        HashMap names = new HashMap();
        int size = set1.size();
        for (int i = 0; i < size; i++) {
            Datum datum = (Datum) set1.get(i);
            names.put(datum.getName(), datum);
        }
        size = set2.size();
        for (int i = 0; i < size; i++) {
            Datum datum = (Datum) set2.get(i);
            if (!names.containsKey(datum.getName())) {
                combined.add(datum);
            }
        }
        return combined;
    }

    private void loadFromPrevious() {
        if (prevSSSFrame == null) {
            return; // woops this should never happen
        }
        // ok load stuff up from previous dialog...
        jcbProperty.setSelected(prevSSSFrame.jcbProperty.isSelected());

        jcbGroup.setSelected(prevSSSFrame.jcbGroup.isSelected());



        // load properties but if properties involved in search don't exist...dont copy that part
        if (jcbProperty.isSelected()) {
            // get any props...if any dont exist then scuttle all of 'em
            String prop1a = (String) prevSSSFrame.jComboBox1a.getSelectedItem();
            String prop2a = (String) prevSSSFrame.jComboBox2a.getSelectedItem();
            String prop3a = (String) prevSSSFrame.jComboBox3a.getSelectedItem();
            String prop4a = (String) prevSSSFrame.jComboBox4a.getSelectedItem();
            String prop1b = (String) prevSSSFrame.jComboBox1b.getSelectedItem();
            String prop2b = (String) prevSSSFrame.jComboBox2b.getSelectedItem();
            String prop3b = (String) prevSSSFrame.jComboBox3b.getSelectedItem();
            String prop4b = (String) prevSSSFrame.jComboBox4b.getSelectedItem();
            String prop1c = prevSSSFrame.jTextField1c.getText();
            String prop2c = prevSSSFrame.jTextField2c.getText();
            String prop3c = prevSSSFrame.jTextField3c.getText();
            String prop4c = prevSSSFrame.jTextField4c.getText();

            // if no properties exist...
            if (propertyDoesNotExist(prop1a) ||
                    propertyDoesNotExist(prop2a) ||
                    propertyDoesNotExist(prop3a) ||
                    propertyDoesNotExist(prop4a)) {
                jcbProperty.setSelected(false);
            } else {
                setPropCombo(prop1a, prop1b, prop1c, jComboBox1a, jComboBox1b, jTextField1c);
                setPropCombo(prop2a, prop2b, prop2c, jComboBox2a, jComboBox2b, jTextField2c);
                setPropCombo(prop3a, prop3b, prop3c, jComboBox3a, jComboBox3b, jTextField3c);
                setPropCombo(prop4a, prop4b, prop4c, jComboBox4a, jComboBox4b, jTextField4c);
            }
        }

        // load groups but if groups invovled in search don't exist...dont copy that part
        if (jcbGroup.isSelected()) {
            String prop1 = (String) prevSSSFrame.jComboBox1.getSelectedItem();
            String prop2 = (String) prevSSSFrame.jComboBox2.getSelectedItem();
            String prop3 = (String) prevSSSFrame.jComboBox3.getSelectedItem();

            // if groups don't exist...
            if (groupDoesNotExist(prop1) || groupDoesNotExist(prop3)) {
                jcbGroup.setSelected(false);
            } else {
                jComboBox1.setSelectedItem(prop1);
                jComboBox2.setSelectedItem(prop2);
                jComboBox3.setSelectedItem(prop3);
            }
        }
    }

    private boolean propertyDoesNotExist(String prop) {
        OligoListModel cmpd_model = dataStore.getOligoListModel();
        return prop != null &&
                prop != "" &&
                cmpd_model.getProperty(prop) == null;
    }

    private void setPropCombo(String propA, String propB, String propC, JComboBox a, JComboBox b, JTextField c) {
        if (propA != null && propA != "") {
            a.setSelectedItem(propA);
            b.setSelectedItem(propB);
            c.setText(propC);
        }
    }

    private boolean groupDoesNotExist(String group) {
        GroupListModel group_model = dataStore.getGroupListModel();
        return group != null &&
                group != "" &&
                group != PFREDConstant.ALL_COMPOUNDS &&
                group != PFREDConstant.SELECTED_COMPOUNDS &&
                group_model.getGroupInfo(group) == null;

    }

    private ArrayList searchByProp(String propName, String comparator, String query, ArrayList dbMols) {
        //validate user input here
        if (comparator.indexOf("matches") < 0) {
            try {
                Float.parseFloat(query);
            } catch (Exception ex) {
                ErrorDialog.showErrorDialog(this, "Invalid input. Input has to be a number");
                return null;
            }
        }

        //ArrayList mols = props.getAllMols();
        ArrayList found = OligoHelper.searchOligos(propName, comparator,
                query.toLowerCase(), dbMols);
        return found;
    }

    //implementing ProgressWorker interface
    public Object startWork(ProgressReporter pd, String name, Object input) throws Exception {
        if (name.equalsIgnoreCase("SSS")) {
            return null;
        } else {
            return null;
        }
    }
    // convenience

    public void workStopped(ProgressReporter pd, String name, Object output, Exception e) {
        if (name.equalsIgnoreCase("SSS")) {
            return;
        }
    }
}
