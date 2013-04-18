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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import org.pfred.PFREDConstant;
import org.pfred.model.Datum;

public class GroupInfoHelper {

  private static Random random = new Random();

  public GroupInfoHelper() {
  }

  // get list of names from collection of group options
 public static String[] getNames(ArrayList groups)
 {
   String[] names = new String[groups.size()];
   for (int i=0; i<groups.size(); i++)
   {
     GroupInfo info = (GroupInfo)groups.get(i);
     names[i] = new String(info.name);
   }

   return names;
  }

  public static ArrayList getGroupInfosFromMolData(ArrayList sorted)
  {
    // we use the code from the old getAllGroupNames() to
    // build our global 'groups' list from data stored with mols
    Datum mol = null;
    String group = null;
    ArrayList groups = new ArrayList();
    HashMap groupNames = new HashMap();
    for (int i=0; i<sorted.size(); i++)
    {
      mol = (Datum) sorted.get(i);
      group = (String) mol.getProperty(PFREDConstant.GROUP_PROPERTY);
      if (group != null)
      {
        StringTokenizer st = new StringTokenizer(group, ":");
        while (st.hasMoreTokens())
        {
          String token = st.nextToken();
          if (token.trim().length()==0)
            continue;
          if (!groupNames.containsKey(token)) {
            groupNames.put(token,"");
            groups.add(new SimpleGroupInfo(token));
          }
        }
      }
    }
    return groups;
  }

  // create value string from group
  public static String getValueString(GroupInfo group)
  {
    String value = "" + group.color.getRGB() + " " + group.show;
    if (group.annotation != null)
      return value + ":" + group.annotation;
    return value;
  }

// build from a value string
  public static GroupInfo fromValueString(String name, String value)
  {
    // make a new group from the string data
    StringTokenizer st = new StringTokenizer(value);
    String rgbString = st.nextToken();
    String showString = st.nextToken();
    int rgb = Integer.parseInt(rgbString);
    Color color = new Color(rgb);
    String note = null;
    boolean show = false;
    if (showString.equalsIgnoreCase("true"))
      show = true;
    int pt = value.lastIndexOf(":");
    if (pt > 0)
      note = new String(value.substring(pt+1));

    GroupInfo group = new SimpleGroupInfo(name,color,show, note);

    return group;
  }




  // get next random light color
  public static Color randomColor()
  {
    int r = (int) (150 + random.nextFloat() * 105);
    int g = (int) (150 + random.nextFloat() * 105);
    int b = (int) (150 + random.nextFloat() *105);
    Color c = new Color(r,g,b);
    return c;
  }

  // return true if this object appears in this array
  public static boolean groupContains(GroupInfo[] groups, GroupInfo group)
  {
    for (int i=0; i<groups.length; i++)
    {
      if (groups[i] == group)
        return true;
    }

    return false;
  }
}
