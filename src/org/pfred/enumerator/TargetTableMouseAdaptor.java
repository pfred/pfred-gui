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
package org.pfred.enumerator;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Point;

import javax.swing.JTable;
import org.pfred.PFREDConstant;
import com.pfizer.rtc.util.BrowserControl;


public class TargetTableMouseAdaptor implements MouseListener, MouseMotionListener {


  public TargetTableMouseAdaptor() {

  }

  public void mouseClicked(MouseEvent e) {
    Object src = e.getSource();
    if (! (src instanceof JTable))return;
    JTable table = (JTable) src;
    //first find out which row is clicked
    double x = e.getX();
    double y = e.getY();
    Point p = e.getPoint();

    int row = table.rowAtPoint(p);
    int column = table.columnAtPoint(p);

    if (row < 0 || column < 0)
      return; //do nothing. something weird here.

    int idx = table.convertColumnIndexToModel(column);

    // DONT use converted index to get name...use column...
    String name = table.getColumnName(column);
    if ( (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ){
      if (e.getClickCount() == 2 ) {
        //do something for launching the RGate link
        Object obj=table.getValueAt(row, column);
        if (obj==null) return;
        String value = obj.toString();
        if (value.startsWith(PFREDConstant.ENSEMBL_ID_PREFIX)){
          //let's link it off to ensembl
          String url =PFREDConstant.PUBLIC_ENSEMBL_TRANSCRIPT_URL+value;
          BrowserControl.displayURL(url);
        }
        //String url = prefix + id;
        //getCmpdDetail(id);
        //BrowserControl.displayURL(url);
      }
    }

  }

  public void mousePressed(MouseEvent e) {
  //
 }
 public void mouseReleased(MouseEvent e) {
  //
 }
 public void mouseEntered(MouseEvent e) {
  //
 }
 public void mouseExited(MouseEvent e) {
   //

 }



 public void mouseDragged(MouseEvent e) {
 }

 public void mouseMoved(MouseEvent e) {

 }

}
