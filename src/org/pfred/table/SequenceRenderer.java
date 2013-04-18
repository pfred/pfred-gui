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

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;
import java.awt.Component;

import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.Oligo;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;


public class SequenceRenderer extends JPanel implements TableCellRenderer, ListDataListener {

    public static String NAME = "Sequence_Column_Renderer";
    public static int ORIG_CLIP_HEIGHT = -1;
    public final static double MIN_SEQ_POSITION_XAXIS_SCALING = 0.50357;
    RNAPolymer oligo = null;
    Rectangle cell_rect = null;
    Color light_blue = new Color(2, 148, 250);
    Color dark_green = new Color(41, 135, 55);
    Color brown = new Color(238, 149, 18);
    int MAX_OLIGO_LEN = 19;
    CustomListModel list_model;
    String propName;

    public SequenceRenderer(CustomTableModel table_model, String propName) {
        this.setBackground(Color.white);
        this.list_model = table_model.getCustomListModel();
        this.list_model.addListDataListener(this);
        this.propName = propName;
        updateMaxLength();
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected,
            boolean hasFocus, int row,
            int column) {

        oligo = null;
        if (value == null) {
            return this;
        }
        if (value instanceof String) {
            oligo = new RNAPolymer((String) value);
        } else if (value instanceof RNAPolymer) {
            oligo = (RNAPolymer) value;
        }
        cell_rect = table.getCellRect(row, column, true);
        return this;
    }

    public void paint(Graphics gObject) {
        super.paint(gObject);
        // Paint the oligo graphics using the cell rectangle size
        // instead of the clip area
        Rectangle clip = cell_rect;
        if (clip == null) {
            return;
        }
        gObject.clearRect(clip.x, clip.y, clip.width, clip.height);

        // Store clip height initially, used to calculate the y scaling factor
        if (ORIG_CLIP_HEIGHT == -1) {
            ORIG_CLIP_HEIGHT = clip.height;
        }

        if (oligo == null) {
            return;
        }

        // Use Graphics2D and AffineTransform objects to scale, rotate, etc the text
        Graphics2D g2DObject = (Graphics2D) gObject;
        g2DObject.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        AffineTransform at_temp = g2DObject.getTransform();

        FontMetrics metrics = this.getFontMetrics(g2DObject.getFont());
        int width = metrics.stringWidth("A");
        int height = metrics.getMaxAscent();

        // Color each char one at a time in the sequence.
        // Coloring both the background and the text
        String seq = oligo.getSingleLetterSeq();
        int size = seq.length();
        int i;
        for (i = 0; i < size; i++) {
            char c = seq.charAt(i);
            Color color = Color.black;
            switch (c) {
                case 'A':
                case 'a':
                    color = dark_green;
                    break;
                case 'T':
                case 't':
                case 'U':
                case 'u':
                    color = Color.blue;
                    break;
                case 'C':
                case 'c':
                    color = Color.red;
                    break;
                case 'G':
                case 'g':
                    color = brown;
                    break;
            }

            // Set background color
            g2DObject.setColor(Color.white);

            // Scaling factors for x and y for oligo sequences
            //double yScalingFactor = (double)clip.height / (double)(ORIG_CLIP_HEIGHT) * 2.0;
            double xScalingFactor = (double) (clip.width) / (double) (width * MAX_OLIGO_LEN) * 0.9;
            double yScalingFactor = 1.4;
            //double xScalingFactor=1.2;

            if (yScalingFactor < 1.0) {
                yScalingFactor = 1.0;
            }
            if (xScalingFactor < 1.0) {
                xScalingFactor = 1.0;
            }


            // Set oligo text color and placing
            g2DObject.setColor(color);
            g2DObject.scale(xScalingFactor, yScalingFactor);


            // Position of oligo text on y-axis is dependent on the height
            // of the clipping rectangle, height of the text and the scaling factor
            // This position places the text in the middle of the cell on the y-axis
            double yTextPosition = (clip.height / 2) + (height / (3 / yScalingFactor));
            int xTextPosition = 5 + width * i;

            g2DObject.drawChars(new char[]{c},
                    0, 1, xTextPosition,
                    (int) (yTextPosition / yScalingFactor));
            g2DObject.setTransform(at_temp);

            // Set sequence position color and placing
            g2DObject.setColor(Color.black);

            // Spaces between single digit numbers and double digit
            // numbers is different
            int width_ctr = 15 + width * 2 * i;
            if (i >= 9) {
                width_ctr = 12 + width * 2 * i;
            }

            // Multiply the y position by some constant to position numbers below the
            // oligo sequence
            double yPosScalingFactor = (double) clip.height / (double) (ORIG_CLIP_HEIGHT) * 0.7;
            double xPosScalingFactor = (double) (clip.width) / (double) (width * MAX_OLIGO_LEN) * 0.45;
            double yPositionBelowSequence = 2;

            if (xPosScalingFactor < MIN_SEQ_POSITION_XAXIS_SCALING) {
                xPosScalingFactor = MIN_SEQ_POSITION_XAXIS_SCALING;
            }
            if (yPosScalingFactor < 0.70 || yPosScalingFactor > 0.85) {
                yPosScalingFactor = 0.70;
            }

            g2DObject.scale(xPosScalingFactor, yPosScalingFactor);
            g2DObject.drawString(Integer.toString(i + 1),
                    (float) width_ctr,
                    (int) (yTextPosition * yPositionBelowSequence));
            g2DObject.setTransform(at_temp);

        }
        g2DObject.setTransform(at_temp);
    }

    public void clear() {
        list_model.removeListDataListener(this);
    }

    public void contentsChanged(ListDataEvent e) {
        // scan through table model and update the length
        if (e.getSource() != list_model) {
            return;
        }

        //skip sorting
        if (e instanceof CustomListDataEvent && e.getType() == CustomListDataEvent.TYPE_RESORTED) {
            return;
        }

        updateMaxLength();

    }

    public void intervalAdded(ListDataEvent e) {
        contentsChanged(e);

    }

    public void intervalRemoved(ListDataEvent e) {
        contentsChanged(e);

    }

    private void updateMaxLength() {
        ArrayList oligos = list_model.getAllData();
        int size = oligos.size();

        for (int i = 0; i < size; i++) {
            Oligo o = (Oligo) oligos.get(i);
            String value = (String) o.getProperty(propName);
            if (value == null) {
                continue;
            }
            RNAPolymer seq = new RNAPolymer(value);
            int len = seq.getLength();
            if (len > MAX_OLIGO_LEN) {
                MAX_OLIGO_LEN = len;
            }
        }
    }
}
