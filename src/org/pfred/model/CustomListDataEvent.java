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

package org.pfred.model;

import javax.swing.event.ListDataEvent;


public class CustomListDataEvent extends ListDataEvent {
  public String propertyName="";
  public static String COLOR="COLOR";
  public static String VISIBILITY="VISIBLITY";
  public static String GROUP="GROUP";
  public static String STRUCTURE="STRUCTURE";
  public static String USER_DEFINED_PROPERTY="USER_DEFINED_PROPERTIES";
  public static int TYPE_RESORTED = -5;
  public static int TYPE_PROPERTY_UPDATED = -6;
  public static int TYPE_PROPERTY_NUMBER_CHANGED = -10;
  public static int TYPE_COLOR_CHANGED = -7;
  public static int TYPE_STRUCTURE_CHANGED =-8;
  public static int TYPE_VISIBILITY_CHANGED = -9;
  public static int TYPE_OTHERS = -10;

  public CustomListDataEvent(Object source, int type, int index0, int index1, String propertyName) {
    super(source, type, index0, index1);
    this.propertyName=propertyName;
  }

  public CustomListDataEvent(Object source, int type, int index0, int index1){
    super(source, type, index0, index1 );
  }

}
