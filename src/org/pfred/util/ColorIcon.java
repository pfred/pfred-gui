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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ColorIcon implements Icon
{
  public static final int DefaultWidth = 32;
  public static final int DefaultHeight = 32;
  public static final Color DefaultColor = Color.black;

  protected int width;
  protected int height;
  protected Color color;
  protected boolean a_visible;

  // all options
  public ColorIcon(Color color, int width, int height)
  {
    this.width = width;
    this.height = height;
    this.color = color;
    this.a_visible = false;
  }

  // with default color
  public ColorIcon(int width, int height)
  {
    this(DefaultColor,width,height);
  }

  // with default size
  public ColorIcon(Color color)
  {
    this(color,DefaultWidth,DefaultHeight);
  }

  // with default color and size
  public ColorIcon()
  {
    this(DefaultColor,DefaultWidth,DefaultHeight);
  }

  // get/set
  public void setWidth(int width)
  {
    this.width = width;
  }
  public int getWidth()
  {
    return width;
  }
  public void setHeight(int height)
  {
    this.height = height;
  }
  public int getHeight()
  {
    return height;
  }
  public void setColor(Color color)
  {
    this.color = color;
  }
  public Color getColor()
  {
    return color;
  }
  public void setAVisiable(boolean visible)
  {
    a_visible =visible;
  }

  //
  // Icon interface
  //
  public int getIconWidth() { return width; }
  public int getIconHeight() { return height; }
  public void paintIcon(Component c, Graphics g, int x, int y)
  {
    g.setColor(color);
    g.fillRect(x,y,width,height);
    if (a_visible)
    {
      g.setColor(Color.black);
      g.drawRect(x-1,y-1,width+1,height+1);
      g.setColor(new Color(255 - color.getRed(),
                           255 - color.getGreen(),
                           255 - color.getBlue())
                 );
      g.drawChars("a".toCharArray(), 0, 1, x+2, y + 10);
    }

  }
}
