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
package org.pfred.sort;

import java.util.ArrayList;
import javax.swing.JTable;
import org.pfred.PFREDConstant;
import org.pfred.PFREDContext;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.property.PropertyDisplayOption;
import org.pfred.table.CustomTableListSelectionModel;
import org.pfred.table.CustomTableModel;
import org.pfred.table.TableSorter;
import org.pfred.table.CustomTablePanel;

public class SortCompPropCmd {

  public SortCompPropCmd() {
  }

  public static void execute(PFREDContext context, ArrayList selPropList) {

    if (selPropList != null) {
      sortMultipleCmpProp(context, selPropList);
    }
  }

  private static void sortMultipleCmpProp(PFREDContext context,
                                          ArrayList selPropList) {

    int selectedColumn = 0;
    int status = 0;
    SelSortCompPropDTO selProp = null;

    CustomTablePanel customTablePanel = context.getUIManager().
        getOligoTablePane();
    JTable table = customTablePanel.getTable();
    TableSorter sorter = (TableSorter) table.getModel();
    CustomTableModel model = (CustomTableModel) sorter.getTableModel();

    PropertyDisplayManager displayManager = model.getPropertyDisplayManager();
    int totalColCnt = model.getColumnCount() - 1; //only the first column in PFRED is special now

    for (int selPropCnt = 0; selPropCnt < selPropList.size(); selPropCnt++) {
      selProp = (SelSortCompPropDTO) selPropList.get(selPropCnt);

      for (int cnt = 0; cnt < totalColCnt; cnt++) {
        PropertyDisplayOption option = (PropertyDisplayOption) displayManager.
            getDisplayedOption(cnt);
        if ( (option.name).equals(selProp.getPropName())) {

          //System.out.println("option name:"+option.name);


          selectedColumn = cnt + 1;

          if (selPropCnt == 0) {
            sorter.cancelSorting(false);
          }

          if (selProp.getOpName().equals(PFREDConstant.INCREASING)) {
            status = 1;
          }
          else {
            status = -1;
          }

          /*
          PropertyDisplayOption opt = model.getPropertyDisplayOption(
              selectedColumn);

          //retrieve the original column option type
          int origoptiontype = opt.type;
          //System.out.println("Original option type:"+opt.type);

          if (selProp.isNumeric()) {
            //System.out.println("option type set to numeric");

            opt.type = PropertyDisplayOption.NUMERIC;
          }
          else { //dangerous to do
            //System.out.println("option type set to Text (String)");

            opt.type = PropertyDisplayOption.STRING;

          }*/

          //System.out.println("option type after reset based on Numeric checkbox sel:"+opt.type);





          sorter.setSortingStatus(selectedColumn, status);

          //Set the column type back to the orignal
          //option.type =origoptiontype;
          //System.out.println("option type after resetting to original type:"+option.type);


        }
      }

    }

    //reselect the records
    CustomTableListSelectionModel custom_sel_model = (
        CustomTableListSelectionModel) table.getSelectionModel();
    ArrayList mols = custom_sel_model.getCustomListSelectionModel().
        getSelectedData();
    custom_sel_model.getCustomListSelectionModel().setValueIsAdjusting(true);
    custom_sel_model.getCustomListSelectionModel().selectData(mols, false);
    custom_sel_model.getCustomListSelectionModel().setValueIsAdjusting(false);

  }

}
