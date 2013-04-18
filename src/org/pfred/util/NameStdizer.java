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

package org.pfred.util;

import org.apache.oro.text.perl.Perl5Util;


public class NameStdizer {

    public static Perl5Util perl5 = new Perl5Util();
   
    public static String getIllegalCharsInName(String name) {
        StringBuffer sb = new StringBuffer();
        if (name.indexOf(':') >= 0) {
            sb.append(':');
        }
        if (name.indexOf('?') >= 0) {
            sb.append(" ?");
        }

        if (name.indexOf(',') >= 0) {
            sb.append(" ,");
        }

        if (sb.length() < 1) {
            return null;
        }

        return sb.toString();
    }

 
    public static String correctProperName(String propName) {
        if (propName.indexOf(':') >= 0 || propName.indexOf('?') >= 0) {
            propName = perl5.substitute("s/:/ /g", propName); //replace as space
            propName = perl5.substitute("s/\\?/ /g", propName);
        }
        return propName;
    }


    public static String getParentName(String in) {
        return in;
    }

    public static boolean equals(String name1, String name2) {
        return name1.equals(name2);
    }

    public static String stdPfizerCpName(String in) {
        return in.trim();
    }

    public static String addDuplicationSuffix(String name, int index) {
        name = name + "_dup" + index;
        return name;
    }

    public static String removeDuplicationSuffix(String name) {
        int idx = name.indexOf("_dup");
        if (idx > 0 && idx > name.length() - 7) {
            return name.substring(0, idx);
        }
        return name;
    }

}
