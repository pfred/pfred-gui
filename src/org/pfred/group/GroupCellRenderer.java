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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.pfred.util.ColorIcon;

public class GroupCellRenderer
        implements ListCellRenderer {

    Font stdFont = new Font("Helvetica", Font.BOLD, 12);
    Font aFont = new Font("Helvetica", Font.PLAIN, 12);
    ColorIcon colorIcon = new ColorIcon(10, 10);
    JLabel item = new JLabel();

    public GroupCellRenderer() {
        item.setOpaque(true);
        item.setBorder(BorderFactory.createCompoundBorder(item.getBorder(), // outside border,
                BorderFactory.createEmptyBorder(0, 5, 0, 0))); // add 5 pixel for right for inside Border
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        item.setIcon(colorIcon);

        String name = value.toString();
        GroupInfo options = (GroupInfo) value;

        item.setBackground(Color.white);

        if (options == null) {
            colorIcon.setColor(Color.black);
            item.setFont(stdFont);
            item.setForeground(Color.black);
        } else {
            colorIcon.setColor(options.color);
            if (options.show) {
                item.setFont(stdFont);
                item.setForeground(Color.black);
            } else {
                item.setFont(aFont);
                item.setForeground(Color.gray);
            }
        }
        item.setText(name);
        if (isSelected) {
            item.setBackground(Color.black);
            item.setForeground(Color.white);
        }


        if (hasAnnotation(options)) {

            colorIcon.setAVisiable(true);
        } else {

            colorIcon.setAVisiable(false);
        }
        return item;
    }

    private boolean hasAnnotation(GroupInfo info) {

        if (info.annotation == null || info.annotation.length() < 1) {
            return false;
        } else {
            return true;
        }
    }
}
