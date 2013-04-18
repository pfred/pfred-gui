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
package org.pfred.sort;

import java.util.Comparator;
import java.util.Date;
import org.pfred.util.RDateFormat;

public final class DATE_COMPARATOR
    implements Comparator {
  RDateFormat formater = RDateFormat.getSingleton();
  public DATE_COMPARATOR() {

  }

  public int compare(Object o1, Object o2) {
    Date d1 = getDate((String)o1);
    Date d2 = getDate((String)o2);
    if (d1==null && d2==null) return 0;
    if (d1==null) return -1;
    if (d2==null) return 1;
    return d1.compareTo(d2);
  }

  public Date getDate(String text){
    if (text==null) return null;
    try {
      Date date = formater.parse(text);
      return date;
    }catch (Exception ex){
      return null;
    }
  }
};
