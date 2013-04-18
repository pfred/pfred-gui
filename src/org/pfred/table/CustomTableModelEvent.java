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

import javax.swing.event.TableModelEvent;


import javax.swing.table.TableModel;

public class CustomTableModelEvent extends TableModelEvent {

    public static final int CUSTOM_TYPE = -10;

    public CustomTableModelEvent(TableModel src) {
        super(src);
    }

    public CustomTableModelEvent(TableModel src, int row) {
        super(src, row);
    }

    public CustomTableModelEvent(TableModel src, int firstRow, int lastRow) {
        super(src, firstRow, lastRow);
    }

    public CustomTableModelEvent(TableModel src, int firstRow, int lastRow, int column) {
        super(src, firstRow, lastRow, column);
    }

    public CustomTableModelEvent(TableModel src, int firstRow, int lastRow, int column, int type) {
        super(src, firstRow, lastRow, column, type);
    }
}
