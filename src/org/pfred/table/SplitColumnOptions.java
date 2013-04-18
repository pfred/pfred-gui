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


public class SplitColumnOptions {

    public static int SplitLeft = 0;
    public static int SplitRight = 1;
    public static int SplitLeftAndRight = 2;
    private static String[] splitTypeNames = {
        "Split Left",
        "Split Right",
        "Split Left And Right",};

    public static String[] getSplitTypeNames() {
        return splitTypeNames;
    }
    // data
    private String splitColumn;
    private int splitType = SplitLeft;

    // bean
    public SplitColumnOptions() {
    }

    public void setSplitType(int splitType) {
        this.splitType = splitType;
    }

    public int getSplitType() {
        return this.splitType;
    }

    public void setSplitColumn(String splitColumn) {
        this.splitColumn = splitColumn;
    }

    public String getSplitColumn() {
        return this.splitColumn;
    }

    // useful
    public boolean hasLeft() {
        return splitType == SplitLeft || splitType == SplitLeftAndRight;
    }

    public boolean hasRight() {
        return splitType == SplitRight || splitType == SplitLeftAndRight;
    }
}
