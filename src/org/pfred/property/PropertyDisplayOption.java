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
package org.pfred.property;


import java.awt.Color;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import org.pfred.table.ConditionalFormatting;
import org.pfred.util.RDateFormat;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;
import com.pfizer.rtc.notation.tools.ComplexNotationParser;
import com.pfizer.rtc.notation.tools.SimpleNotationParser;

public class PropertyDisplayOption {

    public static final int STRING = 0;
    public static final int NUMERIC = 1;
    public static final int DATE = 6;
    public static final int NOTATION = 4;
    public static final DateFormat dateFormat = RDateFormat.getSingleton();
    public static final DecimalFormat decimalFormat = new DecimalFormat();
    public static final String STRING_TYPENAME = "String";
    public static final String NUMERIC_TYPENAME = "Numeric";
    public static final String DATE_TYPENAME = "Date";
    public static final String NOTATION_TYPENAME = "Notation";
    public static final int SORT_DESC = 1;
    public static final int SORT_ASC = 2;
    public static final int SORT_UNSORTED = 0;
    public static final String[] typeNames = {
        STRING_TYPENAME,
        NUMERIC_TYPENAME,
        DATE_TYPENAME,
        NOTATION_TYPENAME
    };

    public static final String[] getTypeNames() {
        return typeNames;
    }
    public String name;
    public String shortName;
    public int type; // physical representation
    public boolean isDerived = false;
    public Object derivedRules = null;
    public ConditionalFormatting conditionalFormatting = null;
    public Color color = null;
    public int cellWidth = -1; // meaning not-set
    public PropertyDisplayOptionCustomData customData = null; //store custom data for particular type of renderer, etc spider plot renderer, IC50 curve renderer
    public boolean editable = false;
    public boolean fixed = false;
    public int sortStatus = SORT_UNSORTED;

    //public String prettyName;
    // create with a specific data...if displayName is null then use name
    public PropertyDisplayOption(String name, String shortName) {
        this(name, shortName, STRING, false);
    }

    // default display names
    public PropertyDisplayOption(String name) {
        this(name, null);
    }

    public PropertyDisplayOption(String name, int type) {
        this(name, null, type, false);
    }

    // clone
    public PropertyDisplayOption(PropertyDisplayOption opt) {
        set(opt);
    }

    public PropertyDisplayOption(String name, String shortName, int type,
            boolean isDerived) {
        this.type = type;
        this.name = name;
        //this.prettyName = prettyName==null?new String(name):prettyName;
        if (shortName == null) {
            this.shortName = this.name.substring(0, Math.min(3, this.name.length()));
        } else {
            this.shortName = shortName;
        }
        this.isDerived = isDerived;

    }

    public void copy(PropertyDisplayOption opt) {
        name = opt.name;
        shortName = opt.shortName;
        isDerived = opt.isDerived;
        type = opt.type;
        derivedRules = opt.derivedRules;
        conditionalFormatting = opt.conditionalFormatting;
        customData = opt.customData;
        color = opt.color;
        cellWidth = opt.cellWidth;
        sortStatus = opt.sortStatus;
        fixed = opt.fixed;
    }

    public Object clone() {
        PropertyDisplayOption newoption = new PropertyDisplayOption(name);
        newoption.name = name;
        newoption.shortName = shortName;
        newoption.isDerived = isDerived;
        newoption.type = type;
        newoption.conditionalFormatting = conditionalFormatting;
        newoption.customData = customData;
        newoption.color = color;
        newoption.cellWidth = cellWidth;
        newoption.sortStatus = sortStatus;
        newoption.fixed = fixed;
        return newoption;

    }

    public int getSortStatus() {
        return sortStatus;
    }

    public void setSortStatus(int direction) {
        sortStatus = direction;
    }

    public void clearSortStatus() {
        sortStatus = SORT_UNSORTED;
    }

    public void setCustomData(PropertyDisplayOptionCustomData customData) {
        this.customData = customData;
    }

    public PropertyDisplayOptionCustomData getCustomData() {
        return customData;
    }

    public boolean isNumber() {
        return type == NUMERIC;
    }

    public boolean isNotation() {
        return type == NOTATION;
    }

    public boolean isDerivedProp() {
        return isDerived;
    }

    public String baseNameForDerivedProp() {
        if (!isDerivedProp()) {
            return null;
        }
        int idx = name.indexOf("_");
        if (idx < 0) {
            return null;
        }
        return name.substring(idx + 1);
    }

    // set values from another
    public void set(PropertyDisplayOption opt) {
        // TODO: derivedRules and conditionalFormatting need copy constructors...
        this.name = new String(opt.name);
        this.shortName = new String(opt.shortName);
        //this.prettyName = new String(opt.prettyName);
        this.type = opt.type;
        this.derivedRules = opt.derivedRules; // woops dont know how to copy these...
        this.conditionalFormatting = opt.conditionalFormatting; // woops we dont know how to copy...
        this.customData = opt.customData; // woops ... same
        this.color = new Color(opt.color.getRed(), opt.color.getGreen(),
                opt.color.getBlue(), opt.color.getAlpha());
        this.cellWidth = opt.cellWidth;
        this.fixed = opt.fixed;
    }

    public boolean equals(PropertyDisplayOption pdo) {
        if (name.equals(pdo.name)) {
            return true;
        }
        return false;
    }

    public void rename(String newName) {
        name = newName;
        shortName = name.substring(0, Math.min(3, this.name.length()));
    }

    public void rename(String newName, String newShortName) {
        name = newName;
        shortName = newShortName;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        if (type == STRING) {
            return STRING_TYPENAME;
        }
        if (type == NUMERIC) {
            return NUMERIC_TYPENAME;
        }
        if (type == DATE) {
            return DATE_TYPENAME;
        }
        if (type == NOTATION) {
            return NOTATION_TYPENAME;
        }

        return STRING_TYPENAME;

    }

    public void setTypeFromName(String typeName) {
        if (typeName.equalsIgnoreCase(NUMERIC_TYPENAME)) {
            type = NUMERIC;
        } else if (typeName.equalsIgnoreCase(DATE_TYPENAME)) {
            type = DATE;
        } else if (typeName.equalsIgnoreCase(STRING_TYPENAME)) {
            type = STRING;
        } else {
            System.out.println(
                    "PropertyDisplayOption.setTypeFrameName(): error, unknown name: "
                    + typeName);
        }
    }

    public Class getTypeClass() {
        switch (type) {
            case STRING:
                return String.class;
            case NUMERIC:
                return Float.class;
            case DATE:
                return Date.class;
            case NOTATION:
                return RNAPolymer.class;

        }
        return String.class;
    }

    // determine the type given a single value...
    // take the best guess
    public static int guessType(String value) {
        if (value == null) {
            return STRING;
        }
        String[] fields = value.split("\\s");
        if (fields == null || fields.length == 0) {
            return STRING;
        }

        value = fields[0]; //only look at the first value
        try {
            double d = Double.parseDouble(value);
            return NUMERIC;
        } catch (NumberFormatException e) {
        }
        try {
            dateFormat.parse(value);
            return DATE;
        } catch (Exception e) {
        }

        try {
            decimalFormat.parse(value);
            return NUMERIC;
        } catch (Exception e) {
        }

        try {
            boolean simpleNotation = SimpleNotationParser.validateSimpleNotationForRNA(value);
            if (simpleNotation) {
                return NOTATION;
            }
            boolean complexNotation = ComplexNotationParser.validateComplexNotation(value);
            if (complexNotation) {
                return NOTATION;
            }
        } catch (Exception ex) {
        }

        try {
            boolean complexNotation = ComplexNotationParser.validateComplexNotation(value);
            if (complexNotation) {
                return NOTATION;
            }
        } catch (Exception ex) {
        }

        return STRING;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    /**
     * guess the type of the column from a list of string values;
     * @param values ArrayList
     * @return int
     */
    public static int guessType(ArrayList values) {

        int size = values.size();
        int type = -1;
        int numberCount = 0;
        int dateCount = 0;
        int stringCount = 0;
        for (int i = 0; i < size; i++) {
            String value = (String) values.get(i);

            if (value == null || value.trim().length() == 0) {
                continue; //don't count missing values
            }
            type = guessType(value);
            switch (type) {
                case NUMERIC:
                    if (numberCount == i && numberCount > (.1f * size)) {
                        return NUMERIC;
                    }
                    numberCount++;
                    break;
                case DATE:
                    if (dateCount == i && dateCount > (.1f * size)) {
                        return DATE;
                    }
                    dateCount++;
                    break;
                case STRING:
                    if (stringCount == i && stringCount > (.1f * size)) {
                        return STRING;
                    }
                    stringCount++;
                    break;
                case NOTATION:
                    return NOTATION;
            }
        }
        type = STRING;
        int max = stringCount;
        if (numberCount > max) {
            type = NUMERIC;
            max = numberCount;
        }
        if (dateCount > max) {
            type = DATE;
            max = dateCount;
        }
        return type;
    }

    public static void main(String[] args) {
    }
}
