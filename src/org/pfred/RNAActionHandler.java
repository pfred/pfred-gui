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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Datum;
import org.pfred.model.Oligo;
import org.pfred.model.OligoListModel;
import org.pfred.plot.OligoActivityBarChart;


import com.pfizer.rtc.task.ProgressDialog;
import com.pfizer.rtc.task.ProgressReporter;
import com.pfizer.rtc.task.ProgressWorker;
import org.pfred.enumerator.OligoSelector;

import org.pfred.dialog.LeaderSubettingDialog;
import org.pfred.enumerator.AdvancedOligoEnumeratorDialog;
import org.pfred.subset.LeaderSubsetting;
import org.pfred.table.BarChartCreateDialog;
import org.pfred.table.BarChartCustomData;

public class RNAActionHandler implements ActionListener, ProgressWorker {

    private PFREDContext context;
    private JFrame parent;
    private AdvancedOligoEnumeratorDialog advancedOligoEnumeratorDialog = null;

    public RNAActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;

    }

    public void setParentFrame(JFrame parent) {
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();


            if (name.equalsIgnoreCase("runAdvancedOligoEnumerator")) {
                this.runAdvancedOligoEnumerator();
            } else if (name.equalsIgnoreCase("leaderSubsetting")) {
                this.leaderSubsetting();
            } else if (name.equalsIgnoreCase("oligoSelector")) {
                this.oligoSelector(false);
            } else if (name.equalsIgnoreCase("antisenseOligoSelector")) {
                this.oligoSelector(true);
            } else if (name.equalsIgnoreCase("createOligoBarChart")) {
                this.createOligoBarChart();
            }
        }
    }

    public void createOligoBarChart() {
        //ask for proerties
        CustomListModel list_model = context.getDataStore().getOligoListModel();
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        BarChartCreateDialog dialog = new BarChartCreateDialog(parent, "Create Bar Chart", list_model);
        String[] selectedPropNames = dialog.getSelectedPropNames();
        String[] selectedErrorNames = dialog.getSelectedErrorPropNames();
        if (selectedPropNames == null) {
            return;
        }

        ArrayList<Oligo> oligos = list_model.getAllData();
        if (sel_model.getSelectedDataCount() > 1) {
            oligos = sel_model.getSelectedData();
        }

        int size = oligos.size();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < size; i++) {

            Datum d = oligos.get(i);
            for (int j = 0; j < selectedPropNames.length; j++) {
                String data = (String) d.getProperty(selectedPropNames[j]);
                if (data == null) {
                    continue;
                }

                String error = (String) d.getProperty(selectedErrorNames[j]);
                double v = 0;
                double e = 0;
                try {
                    v = Double.parseDouble(data);
                } catch (Exception ex) {
                    continue;
                }
                if (error != null) {
                    try {
                        e = Double.parseDouble(error);
                    } catch (Exception ex) {
                        ;
                    }
                }
                if (min > v - e) {
                    min = v - e;
                }
                if (max < v + e) {
                    max = v + e;
                }

            }
        }

        //add custom data
        BarChartCustomData customData = new BarChartCustomData();
        customData.setDataPropNames(selectedPropNames);
        customData.setErrorPropNames(selectedErrorNames);

        if (min > 0) {
            min = 0;
        }
        if (max > 10 && max < 100) {
            max = 100;
        }
        customData.setLowerRange(min);
        customData.setUpperRange(max);
        OligoActivityBarChart barchart = new OligoActivityBarChart(oligos, customData);

        JFrame f = new JFrame("Oligo Bar Chart");
        f.add(new JScrollPane(barchart));
        f.pack();
        f.setVisible(true);

    }

    public void oligoSelector(boolean isAntiSenseDesign) {
        OligoListModel model = context.getDataStore().getOligoListModel();
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        ArrayList oligos = model.getAllData();
        ArrayList selected = sel_model.getSelectedData();
        if (selected.size() > 1) {

            Object[] options = new Object[]{"Selected Oligo", "All Oligo"};
            int opt = JOptionPane.showOptionDialog(parent,
                    "Run OligoSelector on selected compounds?",
                    "Options",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null,
                    options, options[1]);



            if (opt == JOptionPane.NO_OPTION) {
                selected = model.getAllData();
            } else {
                selected = sel_model.getSelectedData();
            }
        } else {
            selected = model.getAllData();
        }


        OligoSelector selector = new OligoSelector(parent, context, selected, isAntiSenseDesign);
        selector.setVisible(true);

        if (selector.isCanceled()) {
            return;
        }

        //get selected oligos
        selected = selector.getSelected();


        if (selected != null) {
            System.out.print("number of oligos selected:" + selected.size());
            JOptionPane.showMessageDialog(parent, selected.size() + " oligos selected.");
            //  sel_model.setValueIsAdjusting(true);
            //  sel_model.selectData(selected, false);
            //  sel_model.setValueIsAdjusting(false);
        } else {
            JOptionPane.showMessageDialog(parent, "Unable to select any oligo that meet all criteria.");
        }
    }

    public void leaderSubsetting() {
        OligoListModel list_model = context.getDataStore().getOligoListModel();
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();

        //run the leader subsetting
        String[] propNames = list_model.getAllPropertyNames();
        LeaderSubettingDialog dialog = new LeaderSubettingDialog(parent, propNames);
        dialog.setVisible(true);

        if (dialog.isCanceled()) {
            return;
        }

        //now let's do step wise filtering
        Object[] options = new Object[]{"Selected Oligo", "All Oligo"};
        int opt = JOptionPane.showOptionDialog(parent,
                "Subsetting only on selected compounds?",
                "Leader subsetting",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                options, options[1]);

        ArrayList selected = null;
        if (opt == JOptionPane.NO_OPTION) {
            selected = list_model.getAllData();
        } else {
            selected = sel_model.getSelectedData();
        }



        String propName = dialog.getSelectedPropName();
        if (dialog.keepCurrentOrder()) {
            propName = null;
        }
        boolean asc = dialog.sortAsc();
        int maxhits = dialog.getMaxHits();
        int min_distance = dialog.getMinDistance();

        ArrayList results = leaderSubsetting(selected, propName, asc, maxhits, min_distance);
        if (results != null) {
            System.out.print("number of oligos selected:" + results.size());
            JOptionPane.showMessageDialog(parent, results.size() + " oligos selected.");
            sel_model.setValueIsAdjusting(true);
            sel_model.selectData(results, false);
            sel_model.setValueIsAdjusting(false);
        }

    }

    public ArrayList leaderSubsetting(ArrayList selected, String propName, boolean asc, int maxhits, int minimalDistance) {

        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        Object[] input = new Object[]{
            selected, propName, new Boolean(asc), new Integer(maxhits),
            new Integer(minimalDistance)};
        ProgressDialog pd = new ProgressDialog(this, parent,
                "Leader Subsetting ", true,
                (Object) input);

        //set all the display properties
        ArrayList results = (ArrayList) pd.getOutput();

        return results;
    }

    public void runAdvancedOligoEnumerator() {

        try {
            JDialog frame = getAdvancedOligoEnumerationDialog();
            frame.pack();
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public AdvancedOligoEnumeratorDialog getAdvancedOligoEnumerationDialog() {
        if (advancedOligoEnumeratorDialog != null) {
            return advancedOligoEnumeratorDialog;
        }
        try {
            advancedOligoEnumeratorDialog = new AdvancedOligoEnumeratorDialog(
                    parent,
                    context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return advancedOligoEnumeratorDialog;
    }
    
    public boolean isAdvancedOligoEnumeratorDialogInitialized() {
        return advancedOligoEnumeratorDialog != null;
    }

    public ArrayList startLeaderSubsetting(ArrayList oligos, String propName, boolean asc, int maxhits, int min_distance, ProgressReporter pd) {
        try {
            ArrayList selected = LeaderSubsetting.subset(oligos, propName, asc,
                    maxhits, min_distance, pd);
            return selected;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Object startWork(ProgressReporter pd, String name, Object input) throws
            Exception {

        if (name.equals("Leader Subsetting ")) {
            Object[] inputs = (Object[]) input;
            ArrayList selected = (ArrayList) inputs[0];
            String propName = (String) inputs[1];
            boolean asc = ((Boolean) inputs[2]).booleanValue();
            int maxhits = ((Integer) inputs[3]).intValue();
            int min_distance = ((Integer) inputs[4]).intValue();

            ArrayList results = startLeaderSubsetting(selected, propName, asc, maxhits, min_distance, pd);
            return results;
        }
        return input;
    }

    // convenience
    public void workStopped(ProgressReporter pd, String name, Object output,
            Exception e) {
        if (name.equalsIgnoreCase("Leader Subsetting ")) {
            ;
        }
    }
}
