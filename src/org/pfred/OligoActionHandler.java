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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Oligo;
import org.pfred.model.OligoListModel;
import org.pfred.util.NameStdizer;
import org.pfred.dialog.AnnotationDialog;

public class OligoActionHandler
        implements ActionListener {

    PFREDContext context;
    JFrame parent;

    public OligoActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();

            if (name.equals("deleteCompound")) {
                deleteCompound();
            } else if (name.equals("annotateCompound")) {
                annotateCompounds();
            } else if (name.equals("moveCompoundTop")) {
                CustomListSelectionModel sel_model =
                        context.getDataStore().getOligoListSelectionModel();
                ArrayList mols = sel_model.getSelectedData();
                moveCompoundTop(mols);
            } else if (name.equals("moveCompoundUp")) {
                CustomListSelectionModel sel_model =
                        context.getDataStore().getOligoListSelectionModel();
                ArrayList mols = sel_model.getSelectedData();
                moveCompoundUp(mols);

            } else if (name.equals("moveCompoundDown")) {
                CustomListSelectionModel sel_model =
                        context.getDataStore().getOligoListSelectionModel();
                ArrayList mols = sel_model.getSelectedData();
                moveCompoundDown(mols);

            } else if (name.equals("moveCompoundBottom")) {
                CustomListSelectionModel sel_model =
                        context.getDataStore().getOligoListSelectionModel();
                ArrayList mols = sel_model.getSelectedData();
                moveCompoundBottom(mols);

            } else if (name.equalsIgnoreCase("copyData")) {
                context.getUIManager().getOligoTablePane().copyDataToClipboard(getAllSelected());
            } else if (name.equalsIgnoreCase("flagSelection")) {
                flagSelection();
            }
        }
    }

    protected Oligo getSelected() {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        if (sel_model.getSelectedDataCount() == 0) {
            return null;
        }
        Oligo mol = (Oligo) sel_model.getSelectedData().get(0);

        return mol;
    }

    protected ArrayList getAllSelected() {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        if (sel_model.getSelectedDataCount() == 0) {
            return new ArrayList();
        }
        return sel_model.getSelectedData();

    }

    public void flagSelection() {
        String input = JOptionPane.showInputDialog(parent,
                "A new property will be created with value set to \"1\" for selected compounds \n" +
                "\"0\" for unselected compounds. Enter the property name: ",
                "Create compound flag", JOptionPane.OK_CANCEL_OPTION);

        if (input == null || input.trim().length() == 0) {
            return;
        }

        input = NameStdizer.correctProperName(input);

        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();

        ArrayList selected = sel_model.getSelectedData();

        CustomListModel oligo_model = context.getDataStore().getOligoListModel();

        ArrayList oligos = oligo_model.getAllData();
        oligo_model.setDataIsChanging(true);
        int size = oligos.size();
        for (int i = 0; i < size; i++) {
            Oligo oligo = (Oligo) oligos.get(i);
            if (sel_model.isDatumSelected(oligo)) {
                oligo.setProperty(input, "1");
            } else {
                oligo.setProperty(input, "0");
            }
        }
        oligo_model.setDataIsChanging(false, CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    // annotate selected compound
    public void annotateCompounds() {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        OligoListModel oligo_model = context.getDataStore().getOligoListModel();
        // get the selected mol
        ArrayList oligos = sel_model.getSelectedData();
        if (oligos == null || oligos.size() < 1) {
            return;
        }

        String text = "";
        String name = "multiple";
        String type = "Compound";

        if (oligos.size() == 1) {
            Oligo oligo = (Oligo) oligos.get(0);

            // get default text and name
            text = (String) oligo.getProperty(PFREDConstant.PFRED_ANNOTATION);
            name = oligo.getName();
            if (text == null) {
                text = "";
            }
        }

        // show annotation dialog, null return means cancel
        String newText = AnnotationDialog.show(parent, type, name, text);
        if (newText == null) {
            return;
        }

        // save this as the new annotation
        // NOTE: at this point if the text is blank we should remove it
      
        oligo_model.setDataIsChanging(true);
        for (int i = 0; i < oligos.size(); i++) {
            Oligo mol = (Oligo) oligos.get(i);
            oligo_model.addProperty(PFREDConstant.PFRED_ANNOTATION, newText, mol);
        }
        oligo_model.setDataIsChanging(false);

    }

    // delete selected compounds
    public void deleteCompound() {
        // get the mols to delete
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        ArrayList mols = sel_model.getSelectedData();

        if (mols.size() < 1) {
            JOptionPane.showMessageDialog(parent,
                    "There are no oligos selected.\n" +
                    "Nothing to delete");
            return;
        }

        // warn
        if (JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to delete the selected oligos permanently?", "Are you sure?",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        // delete these mols
        deleteCompounds(mols);
    }

    // delete compounds
    public synchronized void deleteCompounds(ArrayList c) {

        OligoListModel cmpd_model = context.getDataStore().getOligoListModel();
        cmpd_model.setDataIsChanging(true);
        cmpd_model.removeData(c);
        cmpd_model.setDataIsChanging(false);

        // clear current selection
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        sel_model.setValueIsAdjusting(true);
        sel_model.clearSelection();
        sel_model.setValueIsAdjusting(false);

        // ok now we delete by replacing these

    }

    public void moveCompoundTop(ArrayList selected) {
        OligoListModel oligo_model = context.getDataStore().getOligoListModel();
        oligo_model.setDataIsChanging(true);
        oligo_model.moveDataTop(selected);
        oligo_model.setDataIsChanging(false);

        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    public void moveCompoundBottom(ArrayList selected) {
        OligoListModel compound_model = context.getDataStore().getOligoListModel();
        compound_model.setDataIsChanging(true);
        compound_model.moveDataBottom(selected);
        compound_model.setDataIsChanging(false);

        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    public void moveCompoundUp(ArrayList selected) {
        OligoListModel compound_model = context.getDataStore().getOligoListModel();
        compound_model.setDataIsChanging(true);
        compound_model.moveDataUp(selected);
        compound_model.setDataIsChanging(false);

        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }

    public void moveCompoundDown(ArrayList selected) {
        OligoListModel compound_model = context.getDataStore().getOligoListModel();
        compound_model.setDataIsChanging(true);
        compound_model.moveDataDown(selected);
        compound_model.setDataIsChanging(false);

        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(selected, false);
        sel_model.setValueIsAdjusting(false);
    }
}
