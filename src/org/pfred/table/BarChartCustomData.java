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

import org.pfred.property.PropertyDisplayOptionCustomData;

public class BarChartCustomData implements PropertyDisplayOptionCustomData {

    private double lowerRange = 0;
    private double upperRange = 0;
    private String[] dataPropNames = null;
    private String[] errorPropNames = null;

    public double getLowerRange() {
        return lowerRange;
    }

    public void setLowerRange(double lower_range) {
        this.lowerRange = lower_range;
    }

    public double getUpperRange() {
        return upperRange;
    }

    public void setUpperRange(double upperRange) {
        this.upperRange = upperRange;
    }

    public String[] getDataPropNames() {
        return dataPropNames;
    }

    public void setDataPropNames(String[] dataPropNames) {
        this.dataPropNames = dataPropNames;
    }

    public String[] getErrorPropNames() {
        return errorPropNames;
    }

    public void setErrorPropNames(String[] errorPropNames) {
        this.errorPropNames = errorPropNames;
    }

    public void dataFromString(String persistString) {
        if (persistString == null) {
            return;
        }
        String[] fields = persistString.split(",");
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null) {
                continue;
            }
            if (fields[i].startsWith("lowerRange=")) {
                try {
                    lowerRange = Double.parseDouble(fields[i].substring(11));
                } catch (Exception ex) {
                    //do nothing
                }
            } else if (fields[i].startsWith("upperRange=")) {
                try {
                    upperRange = Double.parseDouble(fields[i].substring(11));
                } catch (Exception ex) {
                    //do nothing
                }
            } else if (fields[i].startsWith("dataPropNames=")) {

                String names = fields[i].substring(14);
                dataPropNames = names.split(";", -1);

            } else if (fields[i].startsWith("errorPropNames=")) {

                String names = fields[i].substring(15);
                errorPropNames = names.split(";", -1);

            }
        }


    }

    public String dataToString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("lowerRange=");
        buffer.append(lowerRange);
        buffer.append(",");
        buffer.append("upperRange=");
        buffer.append(upperRange);
        buffer.append(",");
        buffer.append("dataPropNames=");
        for (int i = 0; i < dataPropNames.length; i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(dataPropNames[i]);
        }
        buffer.append(",");
        buffer.append("errorPropNames=");
        for (int i = 0; i < errorPropNames.length; i++) {
            if (i > 0) {
                buffer.append(";");
            }
            if (errorPropNames[i] != null) {
                buffer.append(errorPropNames[i]);
            }
        }
        return buffer.toString();
    }
}
