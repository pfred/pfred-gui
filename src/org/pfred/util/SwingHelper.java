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

import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class SwingHelper {
	
	  public static JLabel createLabel(String text){
		    JLabel label = new JLabel(text);
		    //label.setFont(label_font);
		    return label;
		  }

		  public static JButton createButton(String text, int margin) {
		   JButton button = new JButton(text);
		   Insets ins = button.getMargin();
		   ins.left=margin;
		   ins.right=margin;
		   button.setMargin(ins);
		   button.setFocusPainted(false);
		   return button;
		 }


		  public static JButton createButton(String text) {
		    JButton button = new JButton(text);
		    Insets ins = button.getMargin();
		    ins.left=6;
		    ins.right=6;
		    button.setMargin(ins);

		    return button;
		  }
		  

		  public static JButton createImageButton(String urlString) {
		    URL url = SwingHelper.class.getResource(urlString);
		    JButton button = new JButton(new ImageIcon(url));
		    Insets ins = button.getMargin();
		    ins.left=0;
		    ins.right=0;
		    button.setMargin(ins);
		    //button.setBorderPainted(false);
		    button.setFocusPainted(false);



		    return button;
		  }
		  
		  

}
