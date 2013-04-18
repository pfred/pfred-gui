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

package org.pfred.icon;



import java.util.HashMap;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class IconLoader {
  private static HashMap icons=new HashMap();


 static {

    load("Print", "/org/pfred/icon/print.gif");
    load("Screenshot", "/org/pfred/icon/screenshot.gif");
    load("Formula", "/org/pfred/icon/fx2.gif");
    load("Save", "/org/pfred/icon/save.gif");
    load("OpenCSV", "/org/pfred/icon/opencsv.gif");
    load("Open", "/org/pfred/icon/openstructure.gif");
    load("Search", "/org/pfred/icon/search.gif");
    load("DBSearch", "/org/pfred/icon/dbsearch.gif");
    load("BackgroundTasks", "/org/pfred/icon/backgroundtasks.gif");
    load("TaskComplete", "/org/pfred/icon/taskcomplete.gif");
    load("TaskInProgress", "/org/pfred/icon/taskinprog.gif");
    load("AddMolToGroup", "/org/pfred/icon/addmoltogroup.gif");
    load("DrawMol", "/org/pfred/icon/drawmol.gif");
    load("LoadFromFile", "/org/pfred/icon/edit.gif");
    load("PFRED", "/org/pfred/icon/pfred.jpg");
    load("AppSave", "/org/pfred/icon/app_save.gif");
    load("AppExplorer", "/org/pfred/icon/app_explorer.gif");
    load("AppExcel", "/org/pfred/icon/app_excel.gif");
    load("AppAcrobat", "/org/pfred/icon/app_acrobat.gif");
    load("AppPictureViewer", "/org/pfred/icon/app_pictureviewer.gif");
    load("SpiderPlot", "/org/pfred/icon/spider_plot.gif");
    load("ID2oligo", "/org/pfred/icon/id2oligo_24.jpg");
    load("Seq2oligo", "/org/pfred/icon/seq2oligo_24.jpg");
    load("ensembl", "/org/pfred/icon/ensembl.gif");
    load("DownArrow", "/org/pfred/icon/DownArrow.gif");
    load("UpArrow", "/org/pfred/icon/UpArrow.gif");
  }

  public static ImageIcon getIcon(String name){
    return (ImageIcon) icons.get(name);
  }

  public static JButton getButton(String name){
    ImageIcon ii =getIcon(name);
    if (ii==null) return new JButton(name);
    JButton bttn= new JButton(ii);
    bttn.setToolTipText(name);
    return bttn;
  }

  public static void load(String name, String urlstring){
    URL url = new IconLoader().getClass().getResource(urlstring);
    if (url!=null){
      ImageIcon ii = new ImageIcon(url);
      icons.put(name, ii);
    }
  }


  public static void main(String[] args) {
    IconLoader iconloader = new IconLoader();
  }


}
