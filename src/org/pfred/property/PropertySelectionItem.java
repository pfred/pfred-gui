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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class PropertySelectionItem extends JPanel
        implements ActionListener {

    private static final int itemHeight = 30;
    private static final Dimension minimumSize = new Dimension(50, itemHeight);
    private static final Dimension preferredSize = new Dimension(300, itemHeight);
    private static final Dimension maximumSize = new Dimension(Integer.MAX_VALUE, itemHeight);

    public static final int getItemHeight() {
        return itemHeight;
    }
    PropertyDisplayOption prop;
    JCheckBox checkBox;
    JTextField textField;
    JComboBox comboBox;

    public PropertySelectionItem(PropertyDisplayOption prop) {
        this.prop = prop;

        checkBox = new JCheckBox();
        add(checkBox);

        textField = new JTextField(20);
        textField.setText(prop.name);
        textField.setEditable(false);
        textField.setBackground(Color.white);
        add(textField);

        comboBox = new JComboBox(PropertyDisplayOption.getTypeNames());
        comboBox.addActionListener(this);
        if (prop.type == PropertyDisplayOption.STRING) {
            comboBox.setSelectedIndex(PropertyDisplayOption.STRING);
        } else if (prop.type == PropertyDisplayOption.DATE) {
            comboBox.setSelectedIndex(2);
        } else {
            comboBox.setSelectedIndex(PropertyDisplayOption.NUMERIC);
        }
        add(comboBox);
    }

    public PropertyDisplayOption getProperty() {
        return prop;
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public Dimension getMaximumSize() {
        return maximumSize;
    }


    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        // update type from selection...
        prop.type = comboBox.getSelectedIndex();
        if (comboBox.getSelectedIndex() == 2) {
            prop.type = prop.DATE;
        }
    }
}
