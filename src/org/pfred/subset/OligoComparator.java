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
package org.pfred.subset;

import java.util.*;

import org.pfred.model.Oligo;



public class OligoComparator
    implements Comparator {
  private String propName;
  private boolean asc;
  public OligoComparator(String propName, boolean asc) {
    this.propName=propName;
    this.asc=asc;
  }

  /**
   * Compares its two arguments for order.
   *
   * @param o1 the first object to be compared.
   * @param o2 the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
   * @todo Implement this java.util.Comparator method
   */
  public int compare(Object o1, Object o2) {
    Oligo oligo1=null;
    Oligo oligo2=null;

    if (o1 instanceof Oligo){
      oligo1=(Oligo)o1;
    }
    if (o2 instanceof Oligo){
      oligo2=(Oligo)o2;
    }

    String str_v1=(String) oligo1.getProperty(propName);
    String str_v2=(String) oligo2.getProperty(propName);


    double v1=-1;
    try{
      v1=Double.parseDouble(str_v1);
    }catch (Exception ex){
      return -1; //always put bad values to the end
    }

    double v2=-1;
    try{
      v2=Double.parseDouble(str_v2);
    }catch (Exception ex){
      return -1;
    }

    if (v1==v2) return 0;
    if (v1<v2) return asc?-1:1;
    if (v1>v2) return asc?1:-1;

    return -1; //this step will never happen
  }
}
