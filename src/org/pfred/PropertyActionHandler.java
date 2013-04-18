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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.Datum;
import org.pfred.model.Oligo;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.property.PropertyDisplayOption;
import com.pfizer.rtc.util.ErrorDialog;
import org.pfred.util.NameStdizer;
import org.pfred.util.ReplaceDialog;
import org.pfred.util.Utils;
import com.pfizer.rtc.expparser.ExpressionEvaluator;
import com.pfizer.rtc.expparser.Variable;
import com.pfizer.rtc.expparser.func.FunctionFactory;
import com.pfizer.rtc.expparser.ui.ExpressionDialog;
import com.pfizer.rtc.task.ProgressCanceledException;
import com.pfizer.rtc.task.ProgressDialog;
import com.pfizer.rtc.task.ProgressReporter;
import com.pfizer.rtc.task.ResumableProgressWorker;
import org.pfred.table.*;
import org.pfred.dialog.PropertyListDialog;
import org.pfred.dialog.AddRowIDDialog;
import org.pfred.io.PFREDFileWriter;
import org.pfred.pme.ModificationTemplateFrame;

public class PropertyActionHandler
        implements ActionListener, ResumableProgressWorker
         {
    private PFREDContext context;
    private JFrame parent;
    private CustomListModel list_model;
    ModificationTemplateFrame modificationTemplateFrame = null;


    public PropertyActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.list_model = context.getDataStore().getOligoListModel();
        this.parent = parent;

    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();

            if (name.equals("exportCompoundProperties")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                exportCompoundProperties();
                System.setSecurityManager(sm);
            } else if (name.equals("deleteProperties")) {
                removeProperties();
            } 
            else if (name.equals("newProperty")) {
                newProperty();
            } else if (name.equals("newPropertyFromExp")) {
                newPropertyFromExpression();
            } else if (name.equals("applySequenceModifications")) {
                applySequenceModifications();
            } else if (e.getActionCommand().startsWith("setPropAsCompoundId")) {
                String actionCommand = e.getActionCommand();
                try {
                    String propName = actionCommand.substring(20);
                    setPropAsCompoundId(propName);
                } catch (Exception ex) {
                    return;
                }
            } else if (e.getActionCommand().startsWith("replacePropValue")) {
                String actionCommand = e.getActionCommand();
                try {
                    String propName = actionCommand.substring(17);
                    stringReplace(propName);
                } catch (Exception ex) {
                    return;
                }
            } else if (name.equals("assignRowNumber")) {
                addRowID();
            }
        }
    }

    public void addRowID() {
        AddRowIDDialog dialog = new AddRowIDDialog(parent, "Assign Row Number");
        dialog.setVisible(true);
        if (dialog.isCanceled()) {
            return;
        }

        String prefix = dialog.getPrefix();
        int startIdx = dialog.getStartIdx();
        boolean setRowIDAsName = dialog.setRowIDAsName();
        if (setRowIDAsName) {
            int option = JOptionPane.showConfirmDialog(parent,
                    "Are you sure you want assign rename each compound to its rowid?",
                    "Rename",
                    JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) {
                return;
            }
        }

        ArrayList mols = list_model.getAllData();
        int size = mols.size();
        list_model.setDataIsChanging(true);
        for (int i = 0; i < size; i++) {
            Datum mol = (Datum) mols.get(i);
            String rowname = prefix + startIdx;
            if (setRowIDAsName) {
                String curr_name = mol.getName();
                list_model.renameDatum(curr_name, rowname);
            } else {
                mol.setProperty("ROW_NUMBER", rowname);
            }
            startIdx++;
        }
        list_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

    }

    public void setPropAsCompoundId(String propName) {
        int option = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to rename each compound?", "Rename",
                JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        ArrayList mols = list_model.getAllData();
        int size = mols.size();
        //check if the new name column is unique
        boolean passed = true;
        HashMap newnames = new HashMap();
        for (int i = 0; i < size; i++) {
            Datum mol = (Datum) mols.get(i);
            Object v = mol.getProperty(propName);
            if (v == null || newnames.containsKey(v)) {
                passed = false;
                break;
            }
            newnames.put(v, "1");
        }
        if (!passed) {
            JOptionPane.showMessageDialog(parent,
                    "The column you chose has duplicated or empty values and can't be used as compound id",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        list_model.setDataIsChanging(true);
        for (int i = 0; i < size; i++) {
            Datum mol = (Datum) mols.get(i);
            String newname = mol.getProperty(propName).toString();
            list_model.renameDatum(mol.getName(), newname);
        }
        list_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

    }

    public void columnSplit(String propName) {
        SplitColumn split = new SplitColumn(context);
        SplitColumnOptions options = split.getDefaultOptions(propName);

        options = SplitColumnDialog.showDialog(parent, options);
        if (options != null) {
            split.splitColumn(options);
        }
    }

    public void stringReplace(String propName) {
        //ask what to replace
        CustomListModel list_model = context.getDataStore().getOligoListModel();
        String[] propNames = list_model.getPropertyManager().getAllPropertyNames();
        ReplaceDialog dialog = new ReplaceDialog(parent, propNames, propName);
        //dialog.show();

        if (dialog.isCancel()) {
            return;
        }

        boolean matchExactCellOnly = dialog.matchExactCellOnly();
        boolean useRegex = dialog.useRegex();
        String findString = dialog.getFindString();
        String replaceString = dialog.getReplaceString();
        String selectedProp = dialog.getSelectedProp();

        list_model.setDataIsChanging(true);
        ArrayList mols = list_model.getAllData();
        int size = mols.size();
        if (!matchExactCellOnly) {
            for (int i = 0; i < size; i++) {
                Datum mol = (Datum) mols.get(i);
                String value = (String) mol.getProperty(selectedProp);
                if (value == null) {
                    continue;
                }
                if (value.indexOf(findString) >= 0) {
                    //replace it
                    if (useRegex) {
                        value = value.replaceAll(findString, replaceString);
                    } else {
                        value = Utils.simpleReplaceAll(value, findString, replaceString);
                    }
                    mol.setProperty(selectedProp, value);
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                Datum mol = (Datum) mols.get(i);
                String value = (String) mol.getProperty(selectedProp);
                if (value == null) {
                    continue;
                }
                if (value.equals(findString)) {
                    //replace it
                    value = replaceString;
                    mol.setProperty(selectedProp, value);
                }
            }
        }

        list_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_UPDATED);
    }

    public void newProperty() {
        context.getUIManager().getMainTabbedPane().setSelectedIndex(0); //show the
        // compound table

        String input = null;
        for (;;) {
            input = JOptionPane.showInputDialog(parent,
                    "Enter property name:",
                    "Create New Property",
                    JOptionPane.OK_CANCEL_OPTION);

            if (input == null || input.trim().length() == 0) {
                return;
            }

            String illegalChars = NameStdizer.getIllegalCharsInName(input.trim());
            if (illegalChars == null) {
                break;
            }

            JOptionPane.showMessageDialog(parent,
                    "One or more illegal characters found in name: "
                    + illegalChars
                    + "\nPlease enter a different name.",
                    "New property",
                    JOptionPane.ERROR_MESSAGE);
        }

        String inputU = input.toUpperCase();
        if (inputU.startsWith("PFRED_") || inputU.endsWith("_dl") || inputU.endsWith("_fingerprint") || inputU.matches("[;\\,:\\?]") || inputU.matches("^R\\d{1,2}")) {
            JOptionPane.showMessageDialog(parent,
                    "Illegal name used. Try a different one.");
            return;
        }

        CustomListModel list_model = context.getDataStore().getOligoListModel();
        if (list_model.size() == 0) {
            return;
        }
        list_model.setDataIsChanging(true);
        list_model.addProperty(input, " ", list_model.getDatum(0));
        list_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

    }

    public void autoAdjustColumnWidth() {
        CustomTablePanel customTablePanel = context.getUIManager().getOligoTablePane();
        JTable table = customTablePanel.getTable();
        TableSorter sorter = (TableSorter) table.getModel();
        CustomTableModel model = (CustomTableModel) sorter.getTableModel();

        PropertyDisplayManager displayManager = model.getPropertyDisplayManager();

        String[] names = displayManager.getDisplayedNames();

        //CompoundListModel list_model = context.getDataStore().getCompoundListModel();
        //String[] names = list_model.getAllPropertyNames();

        PropertyListDialog selectDialog = new PropertyListDialog(parent,
                "Auto Adjust Property Column Width",
                "Please Select Property Columns ", names);
        selectDialog.setLocationRelativeTo(parent);
        selectDialog.setVisible(true);
        String[] selPropList = selectDialog.getSelections();

        if (selPropList == null) {
            return;
        }
        System.out.println("Selected Columns:");
        for (int cnt = 0; cnt < selPropList.length; cnt++) {
            System.out.println(selPropList[cnt]);
        }

        int totalColCnt = model.getColumnCount() - 2;

        int selectedColumnNum[] = new int[selPropList.length];

        for (int selPropCnt = 0; selPropCnt < selPropList.length; selPropCnt++) {
            for (int cnt = 0; cnt < totalColCnt; cnt++) {
                PropertyDisplayOption option = (PropertyDisplayOption) displayManager.getDisplayedOption(cnt);
                if ((option.name).equals(selPropList[selPropCnt])) {
                    selectedColumnNum[selPropCnt] = cnt + 2;
                }
            }
        }

        for (int cnt = 0; cnt < selectedColumnNum.length; cnt++) {
            int viewColumn = table.convertColumnIndexToView(selectedColumnNum[cnt]);
            //TableColumn tableColumn =  table.getColumnModel().getColumn(selectedColumnNum[cnt]);
            TableColumn tableColumn = table.getColumnModel().getColumn(viewColumn);

            int col = table.getColumnModel().getColumnIndex(tableColumn.getIdentifier());

            int rowCount = table.getRowCount();

            int width = (int) table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(table, tableColumn.getIdentifier(), false, false, -1, col).
                    getPreferredSize().getWidth();

            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) table.getCellRenderer(row, col).
                        getTableCellRendererComponent(table,
                        table.getValueAt(row, col), false, false,
                        row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }

            table.getTableHeader().setResizingColumn(tableColumn); // this line is very important
            tableColumn.setWidth(width + table.getIntercellSpacing().width);

        }

    }

    public void removeProperties() {
        String[] names = list_model.getAllPropertyNames();
        PropertyListDialog selectDialog = new PropertyListDialog(parent,
                "Deleting Properties",
                "Please select properties to be deleted.", names);

        selectDialog.setLocationRelativeTo(parent);
        selectDialog.setVisible(true);

        String[] selected = selectDialog.getSelections();
        if (selected == null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(parent,
                "This will permanently remove the properties from the file. "
                + "Do you still want to do that?",
                "Deleting properties",
                JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        list_model.setDataIsChanging(true);
        list_model.removeProperties(selected);
        list_model.setDataIsChanging(false,
                CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);
    }

    public void exportCompoundProperties() {
        //first find out what to export
        CustomListModel list_model = context.getDataStore().getOligoListModel();

        String[] names = list_model.getAllPropertyNames();
        PropertyListDialog selectDialog = new PropertyListDialog(parent,
                "Select Properties",
                "Please select properties to export:", names);

        selectDialog.setLocationRelativeTo(parent);
        selectDialog.setVisible(true);

        String[] selected = selectDialog.getSelections();
        if (selectDialog.isCanceled() || selected == null) {
            return;
        }

        //check if names have registered oligo
        boolean hasRegisteredOligos = false;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i] != null
                    && (selected[i].equalsIgnoreCase(Oligo.REGISTERED_ANTISENSE_OLIGO_PROP) || selected[i].equalsIgnoreCase(Oligo.REGISTERED_SENSE_OLIGO_PROP))) {
                hasRegisteredOligos = true;
                break;
            }
        }

        boolean convertNotation2seq = false;
        if (hasRegisteredOligos) {
            Object[] options = new Object[]{"RNA Notation", "RNA Sequence"};
            int option = JOptionPane.showOptionDialog(parent, "You selected registered oligo sequences. Which format do you want to export them to?",
                    "Export oligo", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (option == JOptionPane.NO_OPTION) {
                convertNotation2seq = true;
            }
        }

        ExportPropertiesDialog dialog = new ExportPropertiesDialog(context, parent,
                selected, convertNotation2seq, true);
        dialog.setVisible(true);
    }

    // NOTE: the "mols" might be mols or they might be ClusterInfo
    // save header and all mols to this file
    public void savePropertiesToFile(ArrayList mols, File f,
            String[] propertyNames, boolean convertNotation2Seq) {

        // no header molecule in this case

        Object[] input = new Object[]{f, mols, propertyNames, convertNotation2Seq};
        ProgressDialog pd = new ProgressDialog(this, parent,
                "SavingProperties", true,
                (Object) input);

    }

    private Object startSaveProperties(ProgressReporter pd, Object input) throws
            Exception {
        // when saving to .sdf file...export the header info as well
        Object[] inputArray = (Object[]) input;
        File f = (File) inputArray[0];
        ArrayList mols = (ArrayList) inputArray[1];

        String[] selected = (String[]) inputArray[2];
        boolean convertNotation2Seq = false;
        if (inputArray.length >= 4) {
            convertNotation2Seq = ((Boolean) inputArray[3]).booleanValue();
        }
        //MolFileWriter.writeProperties(mols, f, pd, selected);
        PFREDFileWriter.writeCSV(parent, mols, new FileOutputStream(f), "\t", selected, convertNotation2Seq, pd);

        /*
         * // if we put it there...remove header if (headerMol != null)
         * mols.remove(headerMol);
         */
        return null;
    }



  
    /* (non-Javadoc)
     * @see com.pfizer.rtc.task.ResumableProgressWorker#loadInputFromString(int, java.lang.String, java.lang.String)
     */
    public Object loadInputFromString(int level, String name,
            String persistString) throws Exception {

    
        return null;
    }

    /* (non-Javadoc)
     * @see com.pfizer.rtc.task.ResumableProgressWorker#resumeWork(com.pfizer.rtc.task.ProgressReporter, java.lang.String, java.lang.Object)
     */
    public Object resumeWork(ProgressReporter pd, String name, Object input) throws
            Exception {
      
        return null;
    }

    // start doign the given time consuming task...
    public Object startWork(ProgressReporter pd, String name, Object input) throws
            Exception {
        if (name.equalsIgnoreCase("SavingProperties")) // and Exporting
        {
            return startSaveProperties(pd, input);
        }

        return null;
    }

    // convenience
    public void stopWork(ProgressReporter pd, String name, Object output,
            Exception e) {
      
    }

    // some time consuming task has stopped
    public void workStopped(ProgressReporter pd, String name, Object output,
            Exception e) {
        // handle error
        if (e != null) {
            // if successful then 'output' is the output of that task
            // otherwise it is really the input object...
            Object input = output;

            if (e instanceof ProgressCanceledException) {
                
            } else {
                JOptionPane.showMessageDialog(parent, "Error: " + name + ": " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            return;
        }

        // handle standard case
        stopWork(pd, name, output, e);
    }

  
    public void applySequenceModifications() {
        ModificationTemplateFrame f = new ModificationTemplateFrame(parent, context);
        modificationTemplateFrame = f; // save for next time...
        f.setLocationRelativeTo(parent);
        f.setResizable(false);
        f.setVisible(true);
    }

    public void newPropertyFromExpression() {

        //context.getUIManager().getMainTabbedPane().setSelectedIndex(1); // show
        // the
        // compound
        // table

        CustomListModel list_model = context.getDataStore().getOligoListModel();
        String[] names = list_model.getAllPropertyNames();

        list_model = context.getDataStore().getOligoListModel();
        if (list_model.size() == 0) {
            return;
        }

        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        //todo: make variable list;
        //1. make variable list
        ArrayList propOptions = list_model.getAllProperties();
        ArrayList variableList = new ArrayList();
        for (int i = 0; i < propOptions.size(); i++) {
            PropertyDisplayOption option = (PropertyDisplayOption) propOptions.get(i);
            String type = Variable.TYPE_STRING;
            if (option.type == PropertyDisplayOption.NUMERIC) {
                type = Variable.TYPE_NUMERIC;
            }
            Variable v = new Variable(option.name, type);
            variableList.add(v);
        }
        //2. get function list
        ArrayList functionList = FunctionFactory.getInstance().getFunctions();

        //3. make example variable values for testing
        HashMap example = new HashMap();
        Datum mol0 = (Datum) list_model.getDatum(0);
        if (mol0 == null) {
            return;
        }
        for (int cnt = 0; cnt < names.length; cnt++) {

            if ((mol0.getProperty(names[cnt]) != null)) {
                Object obj = mol0.getProperty(names[cnt]);
                if (obj != null && obj instanceof String) //add any string value
                {
                    example.put(names[cnt], obj);
                }
            }
        }


        ExpressionDialog expDialog = new ExpressionDialog(parent,
                "Expression Dialog",
                evaluator, variableList, functionList, example);
        expDialog.showDialog();
        list_model.setDataIsChanging(true);

        //System.out.println("CANCELED:"+expDialog.isCanceled());
        if (!(expDialog.isCanceled())) {

            String newPropName = expDialog.getPropName();
            //System.out.println("new Prop Name:" + newPropName);
            //now validate the propName
            //String oldPropName=newPropName;
            newPropName = NameStdizer.correctProperName(newPropName);
            boolean nameExist = false;
            for (int cnt = 0; cnt < names.length; cnt++) {
                if (newPropName.equals(names[cnt])) {
                    nameExist = true;
                    break;
                }
            }

            if (nameExist) {
                int option = JOptionPane.showConfirmDialog(parent, "Property " + newPropName
                        + " already exists. Do you want to overwrite its value?",
                        "Property already exists", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }


            String expression = expDialog.getExpression();
            //System.out.println("Expression :" + expression);



            StringBuffer errorMsg = new StringBuffer();

            try {
                evaluator.setExpression(expression);
            } catch (Exception exp) {
                exp.printStackTrace();
                errorMsg.append("ERROR while setting the expression:\n" + exp.toString());
                errorMsg.append("\n");

            }

            // Loop through for all data in the compound model

            ArrayList sorted = list_model.getAllData();
            int size = sorted.size();
            int failCount = 0;
            //System.out.println("Data SIZE:" + size);
            for (int i = 0; i < size; i++) {
                Datum datum = list_model.getDatum(i);

                HashMap variableValues = new HashMap();
                for (int cnt = 0; cnt < names.length; cnt++) {

                    if ((datum.getProperty(names[cnt]) != null)) {
                        Object obj = datum.getProperty(names[cnt]);
                        if (obj != null && obj instanceof String)//add any string value
                        {
                            variableValues.put(names[cnt], obj);
                        }
                    }

                }

                try {

                    String result = evaluator.getResult(variableValues);
                    //System.out.println("Exp result:" + result);
                    if ((result == null) || (result.length() == 0)) {
                        errorMsg.append(datum.getName());
                        errorMsg.append("\n");
                        failCount++;
                        list_model.addProperty(newPropName, "", datum);
                    } else {
                        list_model.addProperty(newPropName, result, datum);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMsg.append(datum.getName());
                    errorMsg.append("\n");
                    failCount++;
                    list_model.addProperty(newPropName, "", datum);
                }

            }
            // }
            list_model.setDataIsChanging(false,
                    CustomListDataEvent.TYPE_PROPERTY_NUMBER_CHANGED);

            if (errorMsg.toString().trim().length() != 0) {
                /*JOptionPane.showMessageDialog(parent,
                "Error(s) occured while evaulating the Expression :\n"
                + errorMsg.toString(), "Error",
                JOptionPane.ERROR_MESSAGE);*/
                ErrorDialog.showWarningDialog(parent,
                        "Warning: Unable to calculate for " + failCount
                        + " out of " + size + " rows. They are:\n" + errorMsg.toString());
            }

        }
        evaluator.clear();

    }

    public boolean saveInputToString(int level, String name, Object input, StringBuffer sb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class CSVFilter
            extends FileFilter {

        public boolean accept(File f) {
            String name = f.getName().trim().toLowerCase();
            return name.endsWith(".csv") || name.endsWith(".txt") || f.isDirectory();
        }

        public String getDescription() {
            return "comma separated (*.csv), tab delimited (*.txt)";
        }
    }
}
