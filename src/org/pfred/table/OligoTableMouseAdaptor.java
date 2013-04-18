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
package org.pfred.table;



import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import org.pfred.model.Oligo;
import org.pfred.OligoListPopupMenu;
import org.pfred.PFREDContext;
import org.pfred.ViewActionHandler;
import org.pfred.property.PropertyDisplayOption;
import com.pfizer.rtc.util.BrowserControl;
import com.pfizer.rtc.util.HTMLUtil;

public class OligoTableMouseAdaptor
   implements MouseListener, MouseMotionListener, ActionListener
{
  public static final int HighlightTimeout = 0;

  PFREDContext context;
  CustomTablePanel parent;
  JTable table;
  Point lastMouseEventPosition;
  Timer mouseEventTimer;

  String prefix = "http://compounddetails.pfizer.com/CompoundDetails/com.pfizer.pgrd.gwt.CompoundDetails/CmpDtls.jsp?compoundID=";

  public OligoTableMouseAdaptor(PFREDContext context, CustomTablePanel parent) {
    this.context = context;
    this.parent = parent;
    this.table = parent.getTable();

    // we listen to motion on the table itself...
    this.table.addMouseMotionListener(this);
  }

  // called to outright CANCEL a timer so it will never fire...
  private void cancelMouseEventTimer() {
     if (mouseEventTimer != null)
     {
         mouseEventTimer.stop();
         mouseEventTimer = null;
     }
  }
  // called whenever the timer should be created or reset...
  private void restartMouseEventTimer() {
      if (mouseEventTimer == null)
      {
          mouseEventTimer = new Timer(HighlightTimeout, this);
          mouseEventTimer.setRepeats(false);
          mouseEventTimer.start();
          //System.out.println("STARTING timer");
      }
      else
      {
          mouseEventTimer.restart();
          //System.out.println("RE-STARTING timer");
      }
  }

  // called when mouse position is updated
  private void updateMousePosition(MouseEvent e) {
      restartMouseEventTimer();
      lastMouseEventPosition = e.getPoint();
  }

  public void mouseClicked(MouseEvent e) {

    restartMouseEventTimer();

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


    //popup a compound table pop up menu
    if ( (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 ){
      if (e.getClickCount()==2 && idx==0&& parent.getIdColumnAsHyperLink()) {
    	  //do something for launching the RGate link
          String id = table.getValueAt(row, column).toString();
          String url = prefix + id;
          //getCmpdDetail(id);
          BrowserControl.displayURL(url);
        
      }
      else if (e.getClickCount() == 2) {
       
          //check for hyper link
          Object value = parent.getTable().getValueAt(row, column);
          if (value == null || ! (value instanceof String))return;
          if (HTMLUtil.isHyperLink( (String) value)) {
            String url = HTMLUtil.getHyperLink( (String) value);
            BrowserControl.displayURL(url);
          }
         
      }
      else {
        //do nothing
      }
    }
    if ( (e.getModifiers() & MouseEvent.BUTTON3_MASK) == 0)
      return;


 PropertyDisplayOption opt =context.getDataStore().getOligoTableModel().getPropertyDisplayOption(column);

    //  if (name.matches("^R\\d{1,2}$")) {
    // Add more flexibility in Rgroup names
    if (name.equals(Oligo.REGISTERED_ANTISENSE_OLIGO_PROP)
 		   ||name.equals(Oligo.REGISTERED_SENSE_OLIGO_PROP) ||
			  name.equals(Oligo.RNA_NOTATION_PROP)|| opt.isNotation()){
    	//these are RNANotation columns
    	CustomCellRenderer renderer=parent.getRNANotationCellRenderer(name);
    	if (renderer==null) return;
    	RNANotationPopup popup=new RNANotationPopup(parent, renderer);
    	popup.show(table, e.getX(), e.getY());
    	
    }
    else{
    	OligoListPopupMenu.showPopup(context, table, e.getX(), e.getY(), ViewActionHandler.getOligoTableTopMenu(context));
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
    if (e.getSource() == table)
        cancelMouseEventTimer();
  }



  public void mouseDragged(MouseEvent e) {
  }

  public void mouseMoved(MouseEvent e) {
      if (e.getSource() == table && (e.getModifiers() & e.CTRL_MASK)==0)
         updateMousePosition(e);
  }

  // timer actually fired...perform highlight change event...
  public void actionPerformed(ActionEvent e) {
     if (lastMouseEventPosition != null)
     {
        //System.out.println("tooltip:"+lastMouseEventPosition.x+","+lastMouseEventPosition.y);

        // if this point is not outside the table, generate "highlight" for this row
        int row = table.rowAtPoint(lastMouseEventPosition);
        if (row >= 0)
        {
            ListSelectionModel lsm = context.getDataStore().getHighlightModel();
            lsm.setSelectionInterval(row,row);
        }
     }

     mouseEventTimer = null;
  }
  
  public void getCmpdDetail(String id){
	  /*   boolean isRunning=RGate2Service.isRGateRunning();
	    if (!isRunning){
	      Object[]options=new Object[]{"Launch RGate", "Cancel"};
	     int input=JOptionPane.showOptionDialog(parent,
	                                   "RGate is not running. Launch RGate first and try again!",
	                                   "RGate Compound Detail",
	                                   JOptionPane.YES_NO_OPTION,
	                                   JOptionPane.ERROR_MESSAGE, null,
	                                   options, options[0]);

	     if (input==JOptionPane.YES_OPTION){
	       try{
	         RGate2Service.launchRGate();
	       }catch (Exception ex){
	         ex.printStackTrace();
	       }
	     }
	     return;
	    }else {
	      try{
	        RGate2Service.launchCompoundDetails(id);
	      }catch(Exception ex){
	        ex.printStackTrace();
	      }
	    }*/
  }
}
