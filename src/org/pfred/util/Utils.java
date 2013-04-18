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


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

public class Utils {

    // clipboard keybindings for non-windows machines
    public static void addClipboardBindings(JTextComponent c) {
        JTextComponent.KeyBinding[] defaultBindings = {
                new JTextComponent.KeyBinding(
                    KeyStroke.getKeyStroke(KeyEvent.VK_C,
                        InputEvent.CTRL_MASK),
                    DefaultEditorKit.copyAction),
                new JTextComponent.KeyBinding(
                    KeyStroke.getKeyStroke
                    (KeyEvent.VK_V, InputEvent.CTRL_MASK),
                    DefaultEditorKit.pasteAction),
                new JTextComponent.KeyBinding(
                    KeyStroke.getKeyStroke
                    (KeyEvent.VK_X, InputEvent.CTRL_MASK),
                    DefaultEditorKit.cutAction)
            };

        Keymap k = c.getKeymap();

        JTextComponent.loadKeymap(k, defaultBindings, c.getActions());
    }

    // get the binary directory for the system os
    public static String getOSDirectoryName() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.equals("linux")) return "linux";
        else if (os.equals("windows nt") ||
            os.equals("windows 2000") ||
            os.equals("windows 98") ||
            os.equals("windows 95")) return "win32";
        else if (os.equals("irix")) return "irix";
        else if (os.equals("mac os")) return "macos";
        else if (os.equals("solaris")) return "solaris";
        else return "linux";
    }

    public static String getCmdOptionChar() {
        String optionChar = "-";
        String os = System.getProperty("os.name").toLowerCase();

        if (os.equals("windows nt") ||
            os.equals("windows 2000") ||
            os.equals("windows 98") ||
            os.equals("windows 95")) optionChar = "/";
        return optionChar;
    }

    // get the base of a file name
    public static String getBaseFileName(File f) {
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1)
            return s.substring(0, i);
        else
            return s;
    }

    // add a component to a GridBag
    public static void addToGridBag(Container ct, GridBagLayout gb,
        JComponent comp, int x, int y,
        int w, int h,
        int wx, int wy, int fill, int a) {
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.gridheight = h;
        c.weightx = wx;
        c.weighty = wy;
        c.fill = fill;
        c.anchor = a;
        gb.setConstraints(comp, c);
        ct.add(comp);
    }

    // popup a menu without going off the bottom or side of the screen
    public static void showPopup(JPopupMenu popup, Component c,
        int x, int y) {
        Dimension screen_dim = c.getToolkit().getScreenSize();
        Point p = c.getLocationOnScreen();

        p.translate(x, y);
        Dimension popup_dim = popup.getSize();

        p.translate(popup_dim.width, popup_dim.height);
        if (p.x > screen_dim.width)
            x -= p.x - screen_dim.width;
        if (p.y > screen_dim.height)
            y -= p.y - screen_dim.height;
        popup.show(c, x, y);
    }

   public static JFrame getFrameForComponent(Component c) {
      if (c == null)
          return null;
      if (c instanceof JFrame)
          return (JFrame) c;
      return getFrameForComponent(c.getParent());
  }

   // like string.replaceAll(search,replace) but does NOT use regular expressions
   // and so meta chars, such as ( and ) are handled simply
   public static String simpleReplaceAll(String string, String search, String replace)
   {
     StringBuffer sb = new StringBuffer();
     int lastEnd = 0; // the char just past the search string...
     for (;;)
     {
         int start = string.indexOf(search,lastEnd);
         if (start >= 0)
         {
            // get substring from last end to start of string...
            if (start > lastEnd)
               sb.append(string.substring(lastEnd,start));

            // do the replace...
            sb.append(replace);

            // mark new end and keep going...
            lastEnd = start + search.length();

            // if this is the end...we are done...
            if (lastEnd >= string.length())
                break;
         }
         else
         {
             // ok no more...copy the rest of the string to buffer and we are done...
             sb.append(string.substring(lastEnd));
             break;
         }
     }

     return sb.toString();
   }

   public static int confirmationDialog(Component comp, String s, String t, Object[] buttons, Object def) {


     int returnValue = JOptionPane.showOptionDialog(
         comp,
         s,
         t,
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         buttons,
         def
         );

     return returnValue;
   }
   
   
   public static String[] removeEmptyLines(String[] lines){
	   int empty_line_count=0;
	   for (int i=0; i<lines.length; i++){
		   if (lines[i]==null ||lines[i].length()==0){
			   empty_line_count++;
		   }
	   }
	   String[] newlines=new String[lines.length-empty_line_count];
	   int count=0;
	   for (int i=0; i<lines.length; i++){
		   if (lines[i]==null ||lines[i].length()==0){
			   continue;
		   }
		   newlines[count]=lines[i];
		   count++;
	   }
	   return newlines;
   }

}
