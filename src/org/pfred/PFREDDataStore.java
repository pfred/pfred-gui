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
package org.pfred;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

import org.pfred.group.GroupListModel;
import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.OligoListModel;
import org.pfred.model.TargetListModel;
import org.pfred.table.CustomTableModel;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Pfizer Research Technology Center</p>
 * @author $Author: xih $
 * @version $Revision: 1.1.1.1 $   $Date: 2008/06/06 18:11:56 $
 */
public class PFREDDataStore {

    private OligoListModel oligo_model;
    private CustomListSelectionModel oligo_sel_model;
    private TargetListModel target_model;
    private GroupListModel group_model;
    private CustomTableModel oligo_table;
    private ListSelectionModel highlight_model;
    private PFREDContext context;

    public PFREDDataStore(PFREDContext context) {
        this.context = context;
    }

    public OligoListModel getOligoListModel() {
        if (oligo_model == null) {
            createOligoListModel();
        }
        return oligo_model;
    }

    public TargetListModel getTargetListModel() {
        if (target_model == null) {
            createTargetListModel();
        }
        return target_model;
    }

    public CustomListModel createOligoListModel() {
        if (oligo_model == null) {
            oligo_model = new OligoListModel();
        }
        return oligo_model;
    }

    public CustomListModel createTargetListModel() {
        if (target_model == null) {
            target_model = new TargetListModel();
        }
        return target_model;
    }

    public CustomListSelectionModel getOligoListSelectionModel() {
        if (oligo_sel_model == null) {
            createOligoListSelectionModel();
        }
        return oligo_sel_model;
    }

    public CustomTableModel getOligoTableModel() {
        if (oligo_table == null) {
            oligo_table = new CustomTableModel(context, oligo_model);
        }
        return oligo_table;
    }

    public CustomListSelectionModel createOligoListSelectionModel() {
        if (oligo_sel_model == null) {
            oligo_sel_model = new CustomListSelectionModel(oligo_model);
        }
        return oligo_sel_model;
    }

    public void clear() {

        if (group_model.getSize() != 0) {
            group_model.setDataIsChanging(true);
            group_model.clear();
            group_model.setDataIsChanging(false);
        }

        if (!oligo_sel_model.isSelectionEmpty()) {
            oligo_sel_model.setValueIsAdjusting(true);
            oligo_sel_model.clearSelection();
            oligo_sel_model.setValueIsAdjusting(false);
        }

        if (oligo_model != null && oligo_model.getSize() != 0) {
            oligo_model.setDataIsChanging(true);
            oligo_model.clear();
            oligo_model.setDataIsChanging(false);
        }

        if (target_model != null && target_model.getSize() != 0) {
            target_model.setDataIsChanging(true);
            target_model.clear();
            target_model.setDataIsChanging(false);
        }


        if (oligo_table != null) {
            oligo_table.clear();
        }

        // none...
        highlight_model.clearSelection();
    }

    public GroupListModel getGroupListModel() {
        if (group_model == null) {
            group_model = new GroupListModel(this);
        }
        return group_model;
    }

    public ListSelectionModel createHighlightModel() {
        if (highlight_model == null) {
            highlight_model = new DefaultListSelectionModel();
            highlight_model.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        return highlight_model;
    }

    public ListSelectionModel getHighlightModel() {
        if (highlight_model == null) {
            createHighlightModel();
        }
        return highlight_model;
    }
}
