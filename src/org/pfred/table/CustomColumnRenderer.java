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
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import java.awt.Rectangle;
import java.awt.FontMetrics;

import org.pfred.model.Datum;

import org.pfred.property.PropertyDisplayOption;
import com.pfizer.rtc.util.HTMLUtil;
import java.awt.Graphics;

public class CustomColumnRenderer
        implements TableCellRenderer, ListSelectionListener {

    public static String NAME = "Custom_Column_Renderer";
    public static final int highlightLineThickness = 1;
    public static final Color highlightLineColor = Color.blue;
    private CustomJLabel label = new CustomJLabel();
    private JPanel panel = new JPanel(new BorderLayout());
    private CustomTableModel table_model;
    private boolean displayTextAsHyperLink = false;
    private String LESS_THAN_SYMBOL = "<";
    private String HTML_RECOGNIZABLE_LESS_THAN_SYMBOL = "&lt;";
    private Font defaultFont = label.getFont();
    private Font boldFont = new Font(defaultFont.getName(), Font.BOLD, defaultFont.getSize());
    private ListSelectionModel highlightModel;

    public CustomColumnRenderer(CustomTableModel table_model) {

        this(table_model, null);
    }

    public CustomColumnRenderer(CustomTableModel table_model,
            ListSelectionModel highlightModel) {
        this.table_model = table_model;
        this.highlightModel = highlightModel;
        this.highlightModel.addListSelectionListener(this);

        panel.add(label);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    }

    public void setDisplayTextAsHyperLink(boolean isHyperLink) {
        this.displayTextAsHyperLink = isHyperLink;
    }

    public boolean getDisplayTextAsHyperLink() {
        return displayTextAsHyperLink;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        //Color back = getBackgroundColor(row);
        Color back = Color.white;
        label.setText("");

        boolean exists = false;
        boolean isHyperLink = false;
        String data = null;
        if (value != null) {
            data = value.toString();
            exists = isValueContainsLessThanSign(data);
            isHyperLink = isHyperLink(data);
        }

        boolean isHighlighted = highlightModel != null && highlightModel.isSelectedIndex(row);

        if (isHighlighted) {
            label.setFont(boldFont);
        } else {
            label.setFont(defaultFont);
        }

        int mIndex = table.convertColumnIndexToModel(column);
        if (mIndex == 0) {
            //ID column
            //get the color for the datum
            row = table_model.getCustomListModel().visibleIndexToSortedIndex(row);
            Datum datum = table_model.getCustomListModel().getDatum(row);
            back = datum.getColor();
            if (back == null) {
                back = Color.white;
            }
        }

        PropertyDisplayOption opt = null;
        if (mIndex >= 2) {
            opt = table_model.getPropertyDisplayOption(mIndex);
        }

        if (opt != null) {
            if (opt.isNumber()) {
                label.setHorizontalAlignment(JLabel.TRAILING);
            } else {
                label.setHorizontalAlignment(JLabel.LEADING);
            }

            if (opt.color != null) {
                back = opt.color;
            }

            // alter background color based on any conditionals or any
            // hard coded color that might be set...
            if (opt.conditionalFormatting != null) {
                Color c = opt.conditionalFormatting.getColor(value);
                if (c != null) {
                    back = c;
                }
            }
        }

        if (isSelected) {
            label.setForeground(back);
            panel.setBackground(Color.black);
        } else {
            if (displayTextAsHyperLink || isHyperLink) {
                label.setForeground(Color.blue);
            } else {
                label.setForeground(Color.black);
            }
            panel.setBackground(back);
        }



        if (value != null) {
            if (exists && !isHyperLink) {
                data = replaceLessThanSign(data);
            }

            if (displayTextAsHyperLink || isHyperLink) {
                label.setText("<html><u>" + data + "</u></html>");
            } else {
                label.setText("<html>" + data + "</html>");
            }
        }

        Rectangle rec = table.getCellRect(row, column, false);
        label.rec = rec;

        return panel;
    }

    private boolean isHyperLink(String value) {
        return HTMLUtil.isHyperLink(value);
    }

//   Vidhya added - start
    /**
     * Checks if Value contains Less Than Sign (<)
     * @param value - Data which may contain less than sign
     * @return exists
     */
    private boolean isValueContainsLessThanSign(String value) {
        return (value.indexOf(LESS_THAN_SYMBOL) >= 0);
    }

    /**
     * Replaces Less Than Sign with html recognizable symbol "&lt;"
     * @param value
     * @return
     */
    private String replaceLessThanSign(String value) {
        return value.replaceAll(LESS_THAN_SYMBOL, HTML_RECOGNIZABLE_LESS_THAN_SYMBOL);

    }
//   Vidhya added - end

    public void valueChanged(ListSelectionEvent e) {
        // ok when highlight changes...we tell our table the row has changed
        // so it will redraw...
        int row = e.getFirstIndex();
        table_model.fireTableRowsUpdated(row, row);

        if (e.getLastIndex() != row) {
            row = e.getLastIndex();
            table_model.fireTableRowsUpdated(row, row);
        }
    }

    public class CustomJLabel extends JLabel {

        public Rectangle rec = null;

        public void paint(Graphics g) {
            //now get the string length
            FontMetrics metrics = label.getGraphics().getFontMetrics(defaultFont);
            // get the height of a line of text in this font and render context
            int hgt = metrics.getHeight();
            // get the advance of my text in this font and render context
            String text = getText();
            if (text == null || text.length() == 0) {
                return;
            }
            int adv = metrics.stringWidth(getText());
            // calculate the size of a box to hold the text with some padding.
            //Dimension size = new Dimension(adv+2, hgt+2);
            int verticalAlignment = SwingConstants.CENTER;
            int row_half_count = (int) (rec.height / (1.2 * (hgt + 2)));
            if (adv / rec.width > 2 && adv / rec.width > row_half_count) {
                int line_count = text.split(";").length; //our multi-line delimitor
                if (line_count > row_half_count) {
                    verticalAlignment = SwingConstants.TOP;
                }
            }

            setVerticalAlignment(verticalAlignment);
            super.paint(g);

        }
    }
}
