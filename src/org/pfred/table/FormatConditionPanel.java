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

package org.pfred.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.pfred.util.ColorIcon;


public class FormatConditionPanel extends JPanel
        implements ActionListener {

    ConditionalFormattingDialog cfd;
    FormatCondition formatCondition;
    TitledBorder border = new TitledBorder("Condition");
    JComboBox operator = new JComboBox(FormatCondition.getOperatorNames());
    JTextField value = new JTextField();
    JTextField lowerValue = new JTextField();
    JTextField upperValue = new JTextField();
    JButton color = new JButton();
    JButton delButton;
    JPanel valuePanel;
    JPanel valueLUPanel;

    public FormatConditionPanel(ConditionalFormattingDialog cfd) {
        this(cfd, null);
    }

    public FormatConditionPanel(ConditionalFormattingDialog cfd, FormatCondition fc) {
        this.cfd = cfd;

        formatCondition = new FormatCondition(fc);

        BorderLayout borderLayout = new BorderLayout();

        setLayout(borderLayout);
        setBorder(border);

        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        JPanel valuesPanel = new JPanel();
        valuePanel = new JPanel();
        valueLUPanel = new JPanel();
        valuesPanel.add(valuePanel);
        valuesPanel.add(valueLUPanel);

        operator.setSelectedIndex(formatCondition.getOperator());
        operator.setMaximumRowCount(FormatCondition.getNumOperators());
        operator.setActionCommand("operator");
        operator.addActionListener(this);

        value.setText(formatCondition.getValueAsString());
        value.setColumns(41);
        valuePanel.add(value);

        lowerValue.setText(formatCondition.getValueAsString());
        lowerValue.setColumns(18);
        valueLUPanel.add(lowerValue);
        valueLUPanel.add(new JLabel(" and "));

        upperValue.setText(formatCondition.getUpperValueAsString());
        upperValue.setColumns(18);
        valueLUPanel.add(upperValue);

        topPanel.add(new JLabel("When Cell Value"));
        topPanel.add(operator);
        topPanel.add(valuesPanel);

        color.setIcon(new ColorIcon(formatCondition.getColor(), 50, 20));
        color.setActionCommand("setColor");
        color.addActionListener(this);

        delButton = new JButton("Delete...");
        delButton.setActionCommand("delete");
        delButton.addActionListener(this);

        bottomPanel.add(new JLabel("Use Color: "));
        bottomPanel.add(color);
        bottomPanel.add(Box.createHorizontalStrut(50));
        bottomPanel.add(delButton);

        updateValuesPanel();
    }

    protected void updateValuesPanel() {
        int op = operator.getSelectedIndex();
        boolean showBoth = (op == FormatCondition.Between || op == FormatCondition.NotBetween);
        valuePanel.setVisible(!showBoth);
        valueLUPanel.setVisible(showBoth);
    }

    public String getTitle() {
        return border.getTitle();
    }

    public void setTitle(String title) {
        border.setTitle(title);
    }

    public void setDeleteEnabled(boolean enabled) {
        delButton.setEnabled(enabled);
    }

    // return true if input error
    FormatCondition getFormatCondition() {
        // update format condition from GUI...
        ColorIcon ci = (ColorIcon) color.getIcon();
        Color c = ci.getColor();
        int op = operator.getSelectedIndex();

        formatCondition.setOperator(op);
        formatCondition.setColor(c);

        if (op == FormatCondition.Between || op == FormatCondition.NotBetween) {
            try {
                double lowerV = Double.parseDouble(lowerValue.getText().trim());
                double upperV = Double.parseDouble(upperValue.getText().trim());
                formatCondition.setLowerValue(lowerV);
                formatCondition.setUpperValue(upperV);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (FormatCondition.isNumericOperator(op)) {
            try {
                double v = Double.parseDouble(value.getText().trim());
                formatCondition.setValue(v);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            formatCondition.setStringValue(value.getText());
        }

        // return it
        return formatCondition;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("setColor")) {
            ColorIcon ci = (ColorIcon) color.getIcon();
            Color oldC = ci.getColor();
            Color newC = JColorChooser.showDialog(this, "Choose Color", oldC);
            if (newC != null) {
                ci.setColor(newC);
                color.repaint();
            }
        } else if (cmd.equals("delete")) {
            cfd.deletePanel(this);
        } else if (cmd.equals("operator")) {
            updateValuesPanel();
        }
    }
}
