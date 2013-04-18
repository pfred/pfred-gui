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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class PropertySelectionDialog
    extends JDialog implements ActionListener {
  ArrayList allProperties;

  PropertySelectionOption result;

  JPanel list;
  JCheckBox selectedCompounds;

  public static PropertySelectionOption getSelection(Frame owner, String title,
      ArrayList allProperties, boolean allowSelectedOnly) {
    PropertySelectionDialog dlg = new PropertySelectionDialog(owner, title,
        allProperties, allowSelectedOnly);
    dlg.show();
    return dlg.result;
  }

  protected PropertySelectionDialog(Frame owner, String title,
                                    ArrayList allPropertie,
                                    boolean allowSelectedOnly) {
    super(owner, title, true);
    build(allPropertie, allowSelectedOnly);
  }

  protected void build(ArrayList allProperties, boolean allowSelectedOnly) {
    this.allProperties = allProperties;

    JPanel midPanel = new JPanel(new BorderLayout());

    JPanel p;

    JPanel selPanel = new JPanel();

    JButton selAllButton = new JButton("Select All");
    selAllButton.setActionCommand("selectAll");
    selAllButton.addActionListener(this);
    selPanel.add(selAllButton);

    selPanel.add(Box.createHorizontalStrut(10));

    JButton selNoneButton = new JButton("Select None");
    selNoneButton.setActionCommand("selectNone");
    selNoneButton.addActionListener(this);
    selPanel.add(selNoneButton);

    list = buildList();
    JScrollPane scroll = new JScrollPane(list);
    scroll.getViewport().setMinimumSize(new Dimension(340, 200));
    scroll.getViewport().setPreferredSize(new Dimension(340, 200));
    //scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    JPanel listPanel = new JPanel(new BorderLayout());
    listPanel.setBorder(BorderFactory.createTitledBorder("Select Properties"));
//		listPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    p.add(scroll, BorderLayout.CENTER);
    listPanel.add(p, BorderLayout.CENTER);
    listPanel.add(selPanel, BorderLayout.SOUTH);

    midPanel.add(listPanel, BorderLayout.CENTER);

    selectedCompounds = new JCheckBox("Selected Compounds Only");
    selectedCompounds.setEnabled(allowSelectedOnly);

    JPanel optPanel = new JPanel(new BorderLayout());
    optPanel.setBorder(BorderFactory.createTitledBorder("Options"));
    p = new JPanel();

    p.add(selectedCompounds);
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    p.add(selectedCompounds);
    optPanel.add(p, BorderLayout.CENTER);

    midPanel.add(optPanel, BorderLayout.SOUTH);

    // btns
    JPanel btnPanel = new JPanel();

    final JButton okButton = new JButton("OK");
    okButton.setActionCommand("ok");
    okButton.addActionListener(this);
    btnPanel.add(okButton);

    btnPanel.add(Box.createHorizontalStrut(20));

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setActionCommand("cancel");
    cancelButton.addActionListener(this);
    btnPanel.add(cancelButton);

    getContentPane().add(midPanel, BorderLayout.CENTER);
    getContentPane().add(btnPanel, BorderLayout.SOUTH);

    pack();
  }

  protected JPanel buildList() {
    // based on the TOTAL # of properties...create grid layout
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

    // create items for this "list"
    for (int i = 0; i < allProperties.size(); i++) {
      PropertyDisplayOption prop = (PropertyDisplayOption) allProperties.get(i);
      PropertySelectionItem item = new PropertySelectionItem(prop);
      p.add(item);
    }

    // return this "list"
    return p;
  }

  protected ArrayList getSelectedProperties() {
    ArrayList selected = new ArrayList();

    Component[] comps = list.getComponents();
    for (int i = 0; i < comps.length; i++) {
      PropertySelectionItem item = (PropertySelectionItem) comps[i];
      if (item.isSelected())
        selected.add(item.getProperty());
    }

    return selected;
  }

  protected void setAllSelected(boolean selected) {
    Component[] comps = list.getComponents();
    for (int i = 0; i < comps.length; i++) {
      PropertySelectionItem item = (PropertySelectionItem) comps[i];
      item.setSelected(selected);
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    String ac = e.getActionCommand();

    if (ac.equalsIgnoreCase("ok")) {
      ArrayList props = getSelectedProperties();

      // if there are none selected give warning...
      if (props.size() < 1) {
        JOptionPane.showMessageDialog(this,
            "There are no properties selected. You must\n" +
                                      "select some properties before clicking OK.");
        return;
      }

      // output result from selection
      result = new PropertySelectionOption(props, selectedCompounds.isSelected());
      hide();
    }
    else if (ac.equalsIgnoreCase("cancel")) {
      result = null;
      hide();
    }
    else if (ac.equalsIgnoreCase("selectAll")) {
      setAllSelected(true);
    }
    else if (ac.equalsIgnoreCase("selectNone")) {
      setAllSelected(false);
    }
  }
}
