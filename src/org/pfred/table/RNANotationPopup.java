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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class RNANotationPopup extends JPopupMenu {

    CustomCellRenderer renderer;
    CustomTablePanel parent;

    public RNANotationPopup(CustomTablePanel parent, CustomCellRenderer renderer) {
        super();
        this.renderer = renderer;
        this.parent = parent;

        JMenuItem sequenceMode = new JCheckBoxMenuItem("Show as Sequence", renderer.getDisplayMode() == CustomCellRenderer.RNA_LETTER_DISPLAY_MODE);
        this.add(sequenceMode);
        sequenceMode.setName("showAsSequence");
        sequenceMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                RNANotationPopup.this.renderer.setDisplayMode(CustomCellRenderer.RNA_LETTER_DISPLAY_MODE);
                RNANotationPopup.this.parent.setupColumnRenderer();
                RNANotationPopup.this.parent.updateUI();
            }
        });


        JMenuItem blockMode = new JCheckBoxMenuItem("Show as Blocks", renderer.getDisplayMode() == CustomCellRenderer.RNA_SIMPLE_BLOCK_DISPLAY_MODE);
        this.add(blockMode);
        blockMode.setName("showAsBlock");
        blockMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                RNANotationPopup.this.renderer.setDisplayMode(CustomCellRenderer.RNA_SIMPLE_BLOCK_DISPLAY_MODE);
                RNANotationPopup.this.parent.setupColumnRenderer();
                RNANotationPopup.this.parent.updateUI();
            }
        });

        JMenuItem enhancedBlockMode = new JCheckBoxMenuItem("Show as Enhanced Blocks", renderer.getDisplayMode() == CustomCellRenderer.ENHANCED_BLOCK_DISPLAY_MODE);
        this.add(enhancedBlockMode);
        enhancedBlockMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                RNANotationPopup.this.renderer.setDisplayMode(CustomCellRenderer.ENHANCED_BLOCK_DISPLAY_MODE);
                RNANotationPopup.this.parent.setupColumnRenderer();
                RNANotationPopup.this.parent.updateUI();
            }
        });

        JMenuItem graphMode = new JCheckBoxMenuItem("Show as Graph", renderer.getDisplayMode() == CustomCellRenderer.PME_GRAPH_DISPLAY_MODE);
        this.add(graphMode);
        graphMode.setName("showAsGraph");
        graphMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                RNANotationPopup.this.renderer.setDisplayMode(CustomCellRenderer.PME_GRAPH_DISPLAY_MODE);
                RNANotationPopup.this.parent.setupColumnRenderer();
                RNANotationPopup.this.parent.updateUI();
            }
        });

        JMenuItem coleyMode = new JCheckBoxMenuItem("Show as Coley Notation", renderer.getDisplayMode() == CustomCellRenderer.COLEY_DISPLAY_MODE);
        this.add(coleyMode);
        coleyMode.setName("showAsColey");
        coleyMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                RNANotationPopup.this.renderer.setDisplayMode(CustomCellRenderer.COLEY_DISPLAY_MODE);
                RNANotationPopup.this.parent.setupColumnRenderer();
                RNANotationPopup.this.parent.updateUI();
            }
        });



    }
}
