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
import java.util.StringTokenizer;

public class FormatCondition {

    public static final int Between = 0;
    public static final int NotBetween = 1;
    public static final int Equal = 2;
    public static final int NotEqual = 3;
    public static final int GreaterThan = 4;
    public static final int LessThan = 5;
    public static final int GreaterThanOrEqual = 6;
    public static final int LessThanOrEqual = 7;
    public static final int LastNumericOperator = 7; // <- intentionally repeated
    public static final int Match = 8;  // exact string match
    public static final int MatchIgnoreCase = 9;  // string match ignore case
    public static final int Contains = 10; // string contains substring
    public static final int StartsWith = 11; // string begins with
    public static final int EndsWith = 12; // string ends with
    protected static final String[] operatorNames = {
        "is between",
        "is not between",
        "is equal to",
        "is not equal to",
        "is greater than",
        "is less than",
        "is greater than or equal to",
        "is less than or equal to",
        "exactly matches",
        "matches",
        "contains",
        "starts with",
        "ends with",};
    protected int operator;
    protected String stringValue;
    protected double value; // lowerValue for between and not between, otherwise value
    protected double upperValue; // upperValue for between and not between
    protected Color color = Color.blue;

    public FormatCondition() {
    }

    public FormatCondition(String fromString) throws Exception {
        this();
        StringTokenizer st = new StringTokenizer(fromString, ",");

        int rgb = (int) Long.parseLong(st.nextToken(), 16);
        int op = Integer.parseInt(st.nextToken());
        String val = st.nextToken();
        double uVal = Double.parseDouble(st.nextToken());

        setColor(new Color(rgb));
        setOperator(op);
        if (FormatCondition.isNumericOperator(op)) {
            setValue(Double.parseDouble(val));
        } else {
            setStringValue(val);
        }
        setUpperValue(uVal);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(Integer.toHexString(color.getRGB()));
        sb.append(",");
        sb.append(operator);
        sb.append(",");
        if (stringValue != null && stringValue.length() > 0) {
            sb.append(stringValue);
        } else {
            sb.append(value);
        }
        sb.append(",");
        sb.append(upperValue);

        return sb.toString();
    }

    public FormatCondition(FormatCondition c) {
        this();
        if (c == null) {
            return;
        }
        setOperator(c.getOperator());
        setStringValue(c.getStringValue());
        setValue(c.getValue());
        setUpperValue(c.getUpperValue());
        setColor(c.getColor());
    }

    public FormatCondition(int operator, Color color, String stringValue) {
        this();
        setOperator(operator);
        setStringValue(stringValue);
        setColor(color);
    }

    public FormatCondition(int operator, Color color, double value) {
        this();
        setOperator(operator);
        setValue(value);
        setColor(color);
    }

    public FormatCondition(int operator, Color color, double value, double upperValue) {
        this();
        setOperator(operator);
        setValue(value);
        setUpperValue(upperValue);
        setColor(color);
    }

    public static final boolean isNumericOperator(int operator) {
        return operator >= 0 && operator <= LastNumericOperator;
    }

    public static final String[] getOperatorNames() {
        return operatorNames;
    }

    public static final int getNumOperators() {
        return operatorNames.length;
    }

    public static final String getOperatorName(int operator) {
        if (operator < 0 || operator >= operatorNames.length) {
            return null;
        }
        return operatorNames[operator];
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public int getOperator() {
        return operator;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getValueAsString() {
        if (isNumericOperator(operator)) {
            return Double.toString(value);
        }

        return stringValue;
    }

    public String getUpperValueAsString() {
        return Double.toString(upperValue);
    }

    public void setStringValue(String value) {
        this.stringValue = value;

    }

    public String getStringValue() {
        return stringValue;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setLowerValue(double value) {
        setValue(value);
    }

    public double getLowerValue() {
        return getValue();
    }

    public void setUpperValue(double value) {
        upperValue = value;
    }

    public double getUpperValue() {
        return upperValue;
    }

    protected boolean isTrue(double value) {
        if (operator == Between) {
            return value >= this.value && value <= this.upperValue;
        } else if (operator == NotBetween) {
            return value < this.value || value > this.upperValue;
        } else if (operator == Equal) {
            return value == this.value;
        } else if (operator == NotEqual) {
            return value != this.value;
        } else if (operator == GreaterThan) {
            return value > this.value;
        } else if (operator == LessThan) {
            return value < this.value;
        } else if (operator == GreaterThanOrEqual) {
            return value >= this.value;
        } else if (operator == LessThanOrEqual) {
            return value <= this.value;
        }

        return false;
    }

    public boolean isTrue(Object o) {
        String s = o.toString();

        if (operator <= LastNumericOperator) {
            try {
                return isTrue(Double.parseDouble(s));
            } catch (NumberFormatException e) {
                // drop through and return false...any non numeric
                // data is always false for numeric operators
            }
            return false;
        } else if (operator == Match) {
            return s.equals(stringValue);
        } else if (operator == MatchIgnoreCase) {
            return s.equalsIgnoreCase(stringValue);
        } else if (operator == Contains) {
            return s.toLowerCase().indexOf(stringValue.toLowerCase()) >= 0;
        } else if (operator == StartsWith) {
            return s.toLowerCase().startsWith(stringValue.toLowerCase());
        } else if (operator == EndsWith) {
            return s.toLowerCase().endsWith(stringValue.toLowerCase());
        }

        // unknown operator...
        return false;
    }
}
