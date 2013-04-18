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

import com.pfizer.rtc.notation.editor.renderer.RNATableCellRenderer;
import com.pfizer.rtc.notation.editor.renderer.PMEGraphTableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class CustomCellRenderer {

    private RNATableCellRenderer rnaTableCellRenderer;
    private PMEGraphTableCellRenderer pmeTableCellRenderer;
    private DefaultTableCellRenderer defaultTableCellRenderer;
    private int displayMode;
    public final static int RNA_SIMPLE_BLOCK_DISPLAY_MODE = 1;
    public final static int RNA_LETTER_DISPLAY_MODE = 2;
    public final static int PME_GRAPH_DISPLAY_MODE = 3;
    public final static int COLEY_DISPLAY_MODE = 4;
    public final static int ENHANCED_BLOCK_DISPLAY_MODE = 5;

    public CustomCellRenderer() {
        rnaTableCellRenderer = new RNATableCellRenderer();
        pmeTableCellRenderer = new PMEGraphTableCellRenderer();
        defaultTableCellRenderer = new DefaultTableCellRenderer();
        displayMode = RNA_LETTER_DISPLAY_MODE;
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;

        switch (displayMode) {
            case RNA_SIMPLE_BLOCK_DISPLAY_MODE:
                rnaTableCellRenderer.setDisplayMode(RNATableCellRenderer.SIMPLE_BLOCK_DISPLAY_MODE);
                break;
            case RNA_LETTER_DISPLAY_MODE:
                rnaTableCellRenderer.setDisplayMode(RNATableCellRenderer.LETTER_DISPLAY_MODE);
                break;
            case ENHANCED_BLOCK_DISPLAY_MODE:
                rnaTableCellRenderer.setDisplayMode(RNATableCellRenderer.ENHANCED_BLOCK_DISPLAY_MODE);
                break;
        }
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public TableCellRenderer getRenderer() {
        switch (displayMode) {
            case RNA_SIMPLE_BLOCK_DISPLAY_MODE:
            case RNA_LETTER_DISPLAY_MODE:
            case ENHANCED_BLOCK_DISPLAY_MODE:
                return rnaTableCellRenderer;
            case PME_GRAPH_DISPLAY_MODE:
                return pmeTableCellRenderer;
            case COLEY_DISPLAY_MODE:
                return defaultTableCellRenderer;
            default:
                return rnaTableCellRenderer;
        }
    }
}
