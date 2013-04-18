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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableCellRenderer;

import org.pfred.model.Datum;

import org.pfred.property.PropertyDisplayOption;
import org.pfred.plot.BarChartFactory;

public class BarChartRenderer /* extends JPanel */ implements TableCellRenderer, ListDataListener {

    CustomTableModel table_model;
    Datum oligo = null;
    JPanel emptyPanel = new JPanel();
    public final static String NAME = "BarChartRenderer";

    public BarChartRenderer(CustomTableModel table_model) {
        //this.setBackground(Color.white);
        this.table_model = table_model;
        emptyPanel.setBackground(Color.white);
        emptyPanel.setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        //get display option for the property
        int mIndex = table.convertColumnIndexToModel(column);

        PropertyDisplayOption opt = table_model.getPropertyDisplayOption(mIndex);
        if (opt.customData == null || !(opt.customData instanceof BarChartCustomData)) {
            return emptyPanel;
        }
        BarChartCustomData customData = (BarChartCustomData) opt.customData;
        String[] dataPropNames = customData.getDataPropNames();
        String[] errorPropNames = customData.getErrorPropNames();
        double lower_range = customData.getLowerRange();
        double upper_range = customData.getUpperRange();

        //get the row id and the mol
        oligo = table_model.getRow(row);

        //now get the data for plotting
        double[] values = new double[dataPropNames.length];
        double[] errors = new double[errorPropNames.length];

        for (int i = 0; i < dataPropNames.length; i++) {
            try {
                String v = (String) oligo.getProperty(dataPropNames[i]);
                if (v == null) {
                    continue;
                }
                values[i] = Double.parseDouble(v);
            } catch (Exception ex) {
                ;
            }

        }

        for (int i = 0; i < errorPropNames.length; i++) {
            try {
                String v = (String) oligo.getProperty(errorPropNames[i]);
                if (v == null) {
                    continue;
                }
                errors[i] = Double.parseDouble(v);
            } catch (Exception ex) {
                ;
            }

        }

        JPanel chartPanel = BarChartFactory.createSimpleStatBarChartPanel(dataPropNames, values,
                errors, "", null, true, lower_range, upper_range, 0.1);

        return chartPanel;
    }

    public void contentsChanged(ListDataEvent e) {
        // TODO Auto-generated method stub
    }

    public void intervalAdded(ListDataEvent e) {
        // TODO Auto-generated method stub
    }

    public void intervalRemoved(ListDataEvent e) {
        // TODO Auto-generated method stub
    }
}
