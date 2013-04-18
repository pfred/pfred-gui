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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.pfred.GroupActionHandler;
import org.pfred.PFREDConstant;
import org.pfred.PFREDContext;
import org.pfred.model.CustomListSelectionModel;

public class GroupLogicDialog extends JDialog implements ActionListener
{
    private PFREDContext context;
    private Dimension comboSize = new Dimension(150,25);
    private JComboBox jComboBox1 = new JComboBox();
    private JComboBox jComboBox2 = new JComboBox();
    private JComboBox jComboBox3 = new JComboBox();
    private GroupListModel group_model;
    private CustomListSelectionModel cmpd_sel_model;
    private static String[] selected;

    // TODO: note: only call this method when ONE or TWO groups are selected...
    public static void showDialog(PFREDContext context, Frame owner)
    {
        selected = getSelectedGroupNames(context);
        if (selected.length < 1 || selected.length > 2)
            return;

        GroupLogicDialog dlg = new GroupLogicDialog(context, owner);

        dlg.setVisible(true);
    }

    protected GroupLogicDialog(PFREDContext context, Frame owner)
    {
        super(owner,"Group Logic", true);
        this.context = context;

        group_model = context.getDataStore().getGroupListModel();
        cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();

        JPanel mainPanel = new JPanel();

        String[] groupNames = group_model.getGroupNames();
        if (groupNames != null) {
          jComboBox1.addItem(PFREDConstant.ALL_COMPOUNDS);
          jComboBox1.addItem(PFREDConstant.SELECTED_COMPOUNDS);
          for (int i = 0; i < groupNames.length; i++) {
            jComboBox1.addItem(groupNames[i]);
            jComboBox3.addItem(groupNames[i]);
          }
          jComboBox2 = new JComboBox(new Object[] {PFREDConstant.AND,
                                     PFREDConstant.OR, PFREDConstant.BUT_NOT});
        }

        // if there is only one group selected...default to "selected compounds and a"
        if (selected.length == 1)
        {
            jComboBox1.setSelectedIndex(1);
        }
        else
        {
            // otherwise default to the two selected groups...
            jComboBox1.setSelectedIndex(2);
            jComboBox3.setSelectedIndex(1);
        }
        jComboBox1.setPreferredSize(comboSize);
        jComboBox2.setPreferredSize(comboSize);
        jComboBox3.setPreferredSize(comboSize);

        mainPanel.add(jComboBox1);
        mainPanel.add(jComboBox2);
        mainPanel.add(jComboBox3);

        JPanel btnPanel = new JPanel();
        final JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        btnPanel.add(okButton);

        getRootPane().setDefaultButton(okButton);

        btnPanel.add(Box.createHorizontalStrut(30));

        JButton b;

        b = new JButton("Cancel");
        b.setActionCommand("cancel");
        b.addActionListener(this);

        btnPanel.add(b);

        getContentPane().add(mainPanel,BorderLayout.CENTER);
        getContentPane().add(btnPanel,BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();

        if (s.equalsIgnoreCase("ok"))
        {
            boolean createGroup = doSelection();
            dispose();

            // after doing the selection...prompt to create a new
            // group from this...
            if (createGroup)
            {
                GroupActionHandler handler = context.getUIManager().getGroupActionHandler();
                handler.newGroup();
            }
        }
        else if (s.equalsIgnoreCase("cancel"))
        {
            dispose();
        }
    }

    // return true if user want's to do selection from this...
    protected boolean doSelection()
    {
        // perform the given selection...
        ArrayList hits = null;

        String aName = jComboBox1.getSelectedItem().toString().trim();
        String bName = jComboBox3.getSelectedItem().toString().trim();
        String opName = jComboBox2.getSelectedItem().toString().trim();

        hits = group_model.getMolsByGroup(aName, opName, bName);

        if (hits == null || hits.size() == 0) {
            System.out.println("Group Search left with 0.");
            showMsg("No hits found. Group Search left with 0.");
            return false; //nothing found, return
        }
        System.out.println("Group Search left with " + hits.size());

        boolean createGroup = false;

        if (hits.size() == 1)
            createGroup = getMsg(hits.size() + " hit found.\n\nCreate new Group from this?");
        else if (hits.size() > 1)
            createGroup = getMsg(hits.size() + " hits found.\n\nCreate new Group from this?");
        cmpd_sel_model.setValueIsAdjusting(true);
        cmpd_sel_model.selectData(hits, false);
        cmpd_sel_model.setValueIsAdjusting(false);
        System.out.println(hits.size() + " hits found at the end");

        return createGroup;
    }

    private void showMsg(String msg)
    {
        JOptionPane.showMessageDialog(this, msg);
    }

    private boolean getMsg(String msg)
    {
        return JOptionPane.showConfirmDialog(this,msg,"Create New Group?",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static String[] getSelectedGroupNames(PFREDContext context)
    {
        JList groupList = context.getUIManager().getGroupList();

        Object[] values = groupList.getSelectedValues();
        String[] sel = new String[values.length];
        for (int i=0; i<values.length; i++)
            sel[i] = values[i].toString();
        return sel;
    }


}
