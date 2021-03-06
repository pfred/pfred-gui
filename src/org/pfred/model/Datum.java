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

import java.awt.Color;
import java.util.ArrayList;



public interface Datum{
  public String getName();
  public void setName(String name);
  public String getDisplayName();
  public void setProperty(String name, Object value);
  public void setProperty(String name, Object value, boolean first);
  public Object getProperty(String name);
  public Object removeProperty(String name);
  public ArrayList propertyNames();
  public int propertyCount();
  public void clearProperties();
  public boolean isInvisible();
  public void setInvisible(boolean invisible);
  public Color getColor();
  public void setColor(Color c);
  public Object getDisplayObject();
}
