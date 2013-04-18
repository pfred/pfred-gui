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
package org.pfred.group;

import java.awt.Color;


public abstract class  GroupInfo
{


  public String name;
  public Color color;
  public String annotation=null;
  public boolean show;

  // create with a specific color and show state
  public GroupInfo(String name, Color color, boolean show)
  {
    this.name = name;
    this.color = color;
    this.show = show;
  }

  // create with a specific color and show state
   public GroupInfo(String name, Color color, boolean show, String annotation)
   {
     this.name = name;
     this.color = color;
     this.show = show;
     this.annotation = annotation;
  }

  // create with randomly assigned color
  public GroupInfo(String name)
  {
    this.name = name;
    this.color = GroupInfoHelper.randomColor();
    this.show = false;
  }

  public String toString(){
    return name;
  }






}
