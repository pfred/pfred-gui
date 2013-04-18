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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class ConditionalFormatting {

    ArrayList conditions = new ArrayList();

    public ConditionalFormatting() {
    }

    public ConditionalFormatting(String fromString) throws Exception {
        this();

        StringTokenizer st = new StringTokenizer(fromString, "|");
        // we leave space at front to put per cf-stuff outside the realm of conditions
        String dummy = st.nextToken();
        while (st.hasMoreTokens()) {
            FormatCondition cond = new FormatCondition(st.nextToken());
            addCondition(cond);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        // for now we do nothign with this 'header' section and just output
        // a an empty header which we will just plain makeup as {}
        sb.append("{}|");

        // now append each condition with a | between them
        for (int i = 0; i < getNumConditions(); i++) {
            sb.append(getCondition(i).toString());
            if (i != getNumConditions() - 1) {
                sb.append("|");
            }
        }

        return sb.toString();
    }

    public void addCondition(FormatCondition condition) {
        conditions.add(condition);
    }

    public void removeCondition(int i) {
        if (i < 0 || i >= conditions.size()) {
            return;
        }
        conditions.remove(i);
    }

    public void removeCondition(FormatCondition condition) {
        conditions.remove(condition);
    }

    public int getConditionIndex(FormatCondition condition) {
        for (int i = 0; i < conditions.size(); i++) {
            if (conditions.get(i).equals(condition)) {
                return i;
            }
        }
        return -1;
    }

    public List getConditions() {
        return conditions;
    }

    public int getNumConditions() {
        return conditions.size();
    }

    public FormatCondition getCondition(int i) {
        if (i < 0 || i >= conditions.size()) {
            return null;
        }
        return (FormatCondition) conditions.get(i);
    }

    // return format values for a given value...
    // get color (or null)
    public Color getColor(Object o) {
        if (o == null) {
            return null;
        }

        for (int i = 0; i < conditions.size(); i++) {
            FormatCondition c = getCondition(i);
            if (c.isTrue(o)) {
                return c.getColor();
            }
        }

        return null;
    }
}
