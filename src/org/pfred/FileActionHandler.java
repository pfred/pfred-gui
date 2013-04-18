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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.pfred.model.CustomListDataEvent;
import org.pfred.model.CustomListModel;
import org.pfred.model.Datum;
import org.pfred.model.Oligo;
import org.pfred.model.OligoListModel;
import org.pfred.model.Target;
import org.pfred.model.TargetListModel;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.property.PropertyImporter;
import org.pfred.group.GroupInfoHelper;
import org.pfred.group.GroupListModel;
import org.pfred.io.PFREDFileWriter;
import org.pfred.io.PFREDFileReader;
import org.pfred.table.CustomTableModel;
import org.pfred.table.CustomTablePanel;
import com.pfizer.rtc.util.ErrorDialog;
import com.pfizer.rtc.util.FileUtil;
import com.pfizer.rtc.util.PathManager;
import org.pfred.util.NameStdizer;
import com.pfizer.rtc.task.ProgressCanceledException;
import com.pfizer.rtc.task.ProgressReporter;
import com.pfizer.rtc.task.ProgressWorker;
import org.pfred.dialog.OligoEnumerationDialog;
import org.pfred.dialog.ImportOligoCSVDialog;


/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Pfizer Research Technology Center</p>
 * @author $Author: xih $
 * @version $Revision: 1.7 $   $Date: 2008/07/10 15:08:09 $
 */
public class FileActionHandler
        implements ActionListener, ProgressWorker {

    PFREDContext context;
    JFrame parent;

    public File m_file = null;

    public FileActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;
    }

    // confirm save...return false on cancel
    public boolean confirmSave(String msg) {
        if (context.getDataStore().getOligoListModel().getSize() == 0) {
            return true;
        }

        int options;

        options = JOptionPane.YES_NO_CANCEL_OPTION;

        int option = JOptionPane.showConfirmDialog(parent,
                msg +
                "\nDo you wish to save first?",
                "Confirm Save", options);
        if (option == JOptionPane.CANCEL_OPTION) {
            return false;
        }
        if (option == JOptionPane.YES_OPTION) {
            save();
        }

        return true;
    }

    public File cleanFile(File f) {
        File tempF = null;
        try {
            tempF = File.createTempFile("xlist", "tmp");

            PrintWriter pw = new PrintWriter(new FileWriter(tempF));

            // copy the input file...skipping blank lines..
            BufferedReader br = new BufferedReader(new FileReader(f));
            for (;;) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                // skip blank...
                if (line.trim().length() < 1) {
                    continue;
                }
                pw.println(line);
            }
            pw.flush();
            pw.close();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            tempF = null;
        }
        if (tempF == null) {
            return f; // bail out
        }
        return tempF;
    }

    private String getDupName(CustomListModel cmpd_model, HashMap existingNames,
            String orig_name) {
        String name = orig_name;
        int under = orig_name.lastIndexOf('_');
        if (under > 0 && orig_name.length() > under + 3) {
            String postfix = orig_name.substring(under);
            if (postfix.startsWith("_dup")) {
                name = orig_name.substring(0, under);
            }
        }

        String n = null;
        for (int i = 1;; i++) {
            n = name + "_dup" + i;
            if (cmpd_model.getDatum(n) == null && !existingNames.containsKey(n)) {
                break;
            }
        }

        return n;
    }


    public void postImportMols(ArrayList mols) {
        postImportMols(mols, false);
    }

    public void postImportMols(ArrayList oligos, boolean calledFromLoad) {
        // test the first to see if it is a header
        CustomListModel oligo_model = context.getDataStore().getOligoListModel();

        // remove mols we already got
        // first find out the max idx of compound_xxx
        int max_tmp_idx = 0;
        int size = oligo_model.size();
        for (int i = 0; i < size; i++) {
            Oligo o = (Oligo) oligo_model.getDatum(i);
            if (o.getName().startsWith("oligo_")) {
                try {
                    int idx = o.getName().indexOf("_");
                    int tmp = Integer.parseInt(o.getName().substring(idx + 1));
                    if (tmp > max_tmp_idx) {
                        max_tmp_idx = tmp;
                    }
                } catch (Exception ex) {
                    //do nothing
                }
            }
        }
        for (int i = 0; i < oligos.size(); i++) {
            Oligo o = (Oligo) oligos.get(i);
            if (o.getName().startsWith("oligo_")) {
                try {
                    int idx = o.getName().indexOf("_");
                    int tmp = Integer.parseInt(o.getName().substring(idx + 1));
                    if (tmp > max_tmp_idx) {
                        max_tmp_idx = tmp;
                    }
                } catch (Exception ex) {
                    //do nothing
                }
            }
        }

        String action = "";
        Iterator iter = oligos.iterator();
        HashMap existingNames = new HashMap();

        while (iter.hasNext()) {
            Oligo o = (Oligo) iter.next();
            if (oligo_model.getDatum(o.getName()) != null) {
                if (!action.startsWith("Always")) {
                    action = DuplicatedMolDialog.showDialog(parent, o.getName(),
                            calledFromLoad);
                }
                if (action.equalsIgnoreCase(DuplicatedMolDialog.SKIP) ||
                        action.equalsIgnoreCase(DuplicatedMolDialog.ALWAYS_SKIP)) {
                    iter.remove();
                } else if (action.equalsIgnoreCase(DuplicatedMolDialog.NEW_NAME) ||
                        action.equalsIgnoreCase(DuplicatedMolDialog.ALWAYS_NEW_NAME)) {
               
                    o.setName(getDupName(oligo_model, existingNames, o.getName()));

                    // NOTE: the following code was COMMENTED OUT...I uncommented
                    // it to fix the problem with renamed mols still retnaining
                    // their old names...but i don't know why it was ever commented
                    // out in the first place...
                    o.setName(o.getName());
                    existingNames.put(o.getName(), "1");
              

                } else if (action.equalsIgnoreCase(DuplicatedMolDialog.OVERRIDE) ||
                        action.equalsIgnoreCase(DuplicatedMolDialog.ALWAYS_OVERRIDE)) {

                    OligoHelper.copyOligo(o, (Oligo) oligo_model.getDatum(o.getName()));
                    iter.remove();
                }
            }

        }

        // ok add the new mols
        oligo_model.setDataIsChanging(true);
        iter = oligos.iterator();

        while (iter.hasNext()) {
            Oligo o = (Oligo) iter.next();
            // remove any offending properties
            OligoHelper.stripImportedOligo(o);
        }
        //System.out.println(System.currentTimeMillis());
        oligo_model.addData(oligos);
        //System.out.println(System.currentTimeMillis());

        oligo_model.setDataIsChanging(false);
    }

    public void clearContext() {
        if (m_file != null) {
            if (!confirmSave("This will clear all rows.")) {
                return;
            }
        }
        ((PFREDView) parent).init(); //clean up
    }

    public void loadFile() {
        if (m_file != null) {
            if (!confirmSave("When you open a file you will lose any unsaved data.")) {
                return;
            }
        }

        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
        fc.setFileFilter(new PFREDFilter());
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showDialog(parent, "Open") == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            PathManager.setCurrentPath(f.getParentFile());
            loadFile(f);
        }
    }

    public void loadFile(File f) {
        clearContext();
        long start, end;
        PFREDFileReader reader = new PFREDFileReader(parent);
        try {
            start = System.currentTimeMillis();
            reader.read(f, true);//do better error reporting in the future
            end = System.currentTimeMillis();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        start = System.currentTimeMillis();
        //get the oligos, s, configuration
        ArrayList oligos = reader.getOligos();
        OligoListModel oligo_model = context.getDataStore().getOligoListModel();
        oligo_model.setDataIsChanging(true);
        oligo_model.addData(oligos);
        oligo_model.setDataIsChanging(false);
        end = System.currentTimeMillis();


        start = System.currentTimeMillis();
        TargetListModel target_model = context.getDataStore().getTargetListModel();
        ArrayList targets = reader.getTargets();
        target_model.setDataIsChanging(true);
        target_model.addData(targets);
        target_model.setDataIsChanging(false);
        end = System.currentTimeMillis();

        //load the configuration
        start = System.currentTimeMillis();
        HashMap configs = reader.getConfiguration();
        loadConfig(configs);
        end = System.currentTimeMillis();

        //add to MRU file list
        context.getUIManager().getPFREDFrame().addMRUFile(f.getPath());
        m_file = f;
        if (parent != null && parent instanceof PFREDView) {
            parent.setTitle(((PFREDView) parent).frame_name + ": " + m_file.getName());
        }

    }

//    public void loadCSVFile() {
//        if (m_file != null) {
//            if (!confirmSave("When you open a file you will lose any unsaved data.")) {
//                return;
//            }
//        }
//
//        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
//        fc.setFileFilter(new CSVFilter());
//        fc.setDialogType(JFileChooser.OPEN_DIALOG);
//        if (fc.showDialog(parent, "Open") == JFileChooser.APPROVE_OPTION) {
//            File f = fc.getSelectedFile();
//            PathManager.setCurrentPath(f.getParentFile());
//            loadCSVFile(f, true);
//        }
//    }

//    public void loadCSVFile(File f, boolean setFilenameAsTitle) {
//
//        //From file get the 1st line and get the header
//        //using delimiter, retrieve all the column names in the string array
//        //Instantiate LoadCSVFileDialog.java and pass the necessary parameters
//        //From the return value, get the following: Name field selected and selected properties.
//        //Create a new temp csv file with 1st column as the name field
//        //and then remaining columns as the selected properties column
//        //Then use that file as the file f to be used for loading CSV functionality.
//
//        String[] lines = FileUtil.readStringColumnCSV(f);
//
//        if (lines.length < 2) {
//            return;
//        }
//
//        //check out the property names to avoid illegal characters first
//        lines[0] = PfizerNameStdizer.correctProperName(lines[0]);
//
//        String delim = FileUtil.calcDelim2(lines[0]);
//
//        StringTokenizer hst = new StringTokenizer(lines[0], delim);
//        String[] colNames = new String[hst.countTokens()];
//
//        for (int i = 0; i < colNames.length; i++) {
//            colNames[i] = PfizerNameStdizer.correctProperName(hst.nextToken());
//        }
//
//        LoadCSVFileDialog selectDialog = new LoadCSVFileDialog(parent,
//                "Select Id and Properties to Import",
//                "Please select Id Field:",
//                "Please select Properties to Import:",
//                colNames);
//        //selectDialog.show();
//        String[] selected_cols = selectDialog.getSelColumn();
//
//        int[] selected_col_indices = selectDialog.getSelColumnOrigIndex();
//
//        if (selectDialog.isCanceled() || selected_cols == null || selected_cols.length == 0) {
//            return;
//        }
//
//        String[] newlines = new String[lines.length];
//
//        String id_column = selectDialog.getNameFieldSelected();
//        int id_column_index = selectDialog.getNameFieldIndex();
//
//        //Finally create a temp file with new header and new values.
//        StringBuffer header = new StringBuffer();
//        header.append(id_column);
//
//        //build the header
//        for (int i = 0; i < selected_cols.length; i++) {
//            if (selected_cols[i].equals(id_column)) {
//                continue;
//            }
//
//            header.append(delim);
//            header.append(selected_cols[i]);
//        }
//        newlines[0] = header.toString();
//
//        //build the data rows
//        int numNoName = 0;
//
//        for (int i = 1; i < lines.length; i++) {
//            if (lines[i].trim().length() == 0) { //skip empty lines
//                newlines[i] = "";
//                continue;
//            }
//
//            String[] fields = FileUtil.getFields(lines[i], delim);
//
//            // skip (and count) lines with empty ID
//            if (id_column_index >= fields.length || fields[id_column_index].trim().length() < 1) {
//                numNoName++;
//                continue;
//            }
//
//            StringBuffer tempLine = new StringBuffer();
//            //tempfilecontent.append(System.getProperty("line.separator"));
//            //write the id field
//            //tempfilecontent.append(fields[id_column_index]);
//            tempLine.append(fields[id_column_index]);
//
//            for (int j = 0; j < selected_col_indices.length; j++) {
//                if (selected_col_indices[j] == id_column_index) {
//                    continue;
//                }
//
//                tempLine.append(delim);
//                int idx = selected_col_indices[j];
//                if (idx >= fields.length) {
//                    tempLine.append(""); //this should never happen but nevertheless
//                } else {
//                    tempLine.append(fields[idx]);
//                }
//            }
//            newlines[i] = tempLine.toString();
//
//        }
//
//        //remove duplicated rows if any
//        PropertyImporter importer = new PropertyImporter(context, parent);
//        newlines = importer.deduplicate2(newlines, 0, true, delim);
//        if (newlines == null) {
//            return; //user canceled the action
//        }
//
//        Format dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String tempfilename = "tmp_prop_" + dateFormatter.format(new Date());
//
//
//        try {
//            f = FileUtil.writeTempFile(tempfilename, ".txt", newlines);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // if there were some with blank IDs....
//        if (numNoName > 0) {
//            String msg;
//            if (numNoName == 1) {
//                msg = "There was one line read with an EMPTY id field. This line was skipped.\n" +
//                        "Do you wish to continue loading?";
//            } else {
//                msg = "There were " + numNoName +
//                        " lines read with EMPTY id fields. These lines were skipped.\n" +
//                        "Do you wish to continue loading?";
//            }
//
//            // report on blank IDs...we can also quit out here
//            if (JOptionPane.showConfirmDialog(parent, msg, "Confirm Continue",
//                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
//                return;
//            }
//        }
//
//        //Temp file is sucessfully created and assigned to the new File f object.
//        ((PFREDView) parent).init(); //clean up
//
//        String[] ids = null;
//        try {
//            ids = getCompoundIdsAsString(f);
//        } catch (Exception ex) {
//            //ex.printStackTrace();
//            //rarely happens
//            return;
//        }
//
//        ArrayList mols = getOligosFromIDs(ids);
//        //System.out.print("ARRAYLIST MOLS FROM 'LOADINGCSV' progressdialog output:"+mols);
//
//        if (mols != null) {
//            System.out.print("ARRAYLIST MOLS length:" + mols.size());
//        }
//
//        if ((mols == null) || (mols.isEmpty())) {
//            return;
//        }
//        postProcessLoad(mols);
//        // now that the mols themselves are loaded...we need
//        // to load the properties as well...
//
//        loadPropertiesFromTempCSVFile(f);
//
//        //move properties to the end
//        CustomTableModel table_model = context.getDataStore().getOligoTableModel();
//
//        table_model.getPropertyDisplayManager().moveDisplayOptionLast(new String[]{
//                    Oligo.PARENT_DNA_OLIGO_PROP, Oligo.PARENT_SENSE_OLIGO_PROP, Oligo.PARENT_ANTISENSE_OLIGO_PROP,
//                    Oligo.REGISTERED_ANTISENSE_OLIGO_PROP, Oligo.REGISTERED_SENSE_OLIGO_PROP
//                });
//
//
//        if (setFilenameAsTitle) {
//            m_file = f;
//            parent.setTitle(((PFREDView) parent).frame_name + ": " + m_file.getName());
//        } else {
//            m_file = new File("Untitled");
//        }
//    }

//    private ArrayList getOligosFromIDs(String[] ids) {
//        // do it in a MODAL progress dialog
//        RNANotationLookup service = null;
//        try {
//            service = new RNANotationLookup(parent);
//            if (!service.authenticate()) {
//                JOptionPane.showMessageDialog(parent,
//                        "Unable to authenticate. ",
//                        "Login Error",
//                        JOptionPane.ERROR_MESSAGE);
//                return null;
//            }
//        } catch (Exception ex) {
//            System.out.println("Unable to connect to NLService");
//            JOptionPane.showMessageDialog(parent,
//                    "Unable to connect to server. " + ex.getMessage(),
//                    "Login Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return null;
//        }
//        if (service == null) {
//            return null;
//        }
//        Object[] input = new Object[]{
//            ids, service};
//        ProgressDialog pd = new ProgressDialog(this, parent, "LoadingCSV", true,
//                (Object) input);
//
//
//
//        //set all the display properties
//        ArrayList mols = (ArrayList) pd.getOutput();
//
//        return mols;
//    }

    public void loadOligoCSVFile() {
        // tell them wahts going on
		/*JOptionPane.showMessageDialog(parent,
        "This feature lets you load molecules and molecule properties from a tab or comma \n" +
        "delimited text file. The first line in the file must be the property names and\n" +
        "the first column must be the compound ids.\n");
         */

        if (m_file != null) {
            if (!confirmSave("When you open a file you will lose any unsaved data.")) {
                return;
            }
        }

        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
        fc.setFileFilter(new CSVFilter());
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showDialog(parent, "Open") == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            PathManager.setCurrentPath(f.getParentFile());
            loadOligoCSVFile(f);
        }
    }

    public void loadOligoCSVFile(File f) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String header_line = in.readLine();
            String delim = FileUtil.calcDelim2(header_line);
            String[] header_fields = FileUtil.getFields(header_line, delim);
            ImportOligoCSVDialog dialog = new ImportOligoCSVDialog(parent, header_fields);
            dialog.setVisible(true);
            if (dialog.isCanceled()) {
                return;
            }
            int id_idx = dialog.getIDFieldIndex();
            int seq_idx = dialog.getSeqFieldIndex();
            int start_idx = dialog.getStartFieldIndex();
            int end_idx = dialog.getEndFieldIndex();
            int target_idx = dialog.getTargetSeqNameFieldIndex();
            ArrayList lines = new ArrayList();
            //lines.add(header_line);
            String line = in.readLine();
            while (line != null) {
                lines.add(line);
                line = in.readLine();
            }
            int lineCount = lines.size();
            String[] data = new String[lineCount];
            for (int i = 0; i < lineCount; i++) {
                data[i] = (String) lines.get(i);
            }

            String id_colname = header_fields[id_idx];
            String seq_colname = null;
            int oligo_type = Oligo.TYPE_DNA_OLIGO;
            if (seq_idx >= 0) {
                seq_colname = header_fields[seq_idx];
                String input = (String) JOptionPane.showInputDialog(parent, "Choose sequence type:", "Sequence Type",
                        JOptionPane.QUESTION_MESSAGE, null, new String[]{"RNA Notation", "DNA Sequence", "Sense RNA", "Antisense RNA"}, "DNA Sequence");
                if (input.equals("Antisense RNA")) {
                    oligo_type = Oligo.TYPE_ANTISENSE_OLIGO;
                } else if (input.equals("RNA Notation")) {
                    oligo_type = Oligo.TYPE_RNANOTATION;
                }
            }
            String start_colname = null;
            if (start_idx >= 0) {
                start_colname = header_fields[start_idx];
            }
            String end_colname = null;
            if (end_idx >= 0) {
                end_colname = header_fields[end_idx];
            }
            String target_colname = null;
            if (target_idx >= 0) {
                target_colname = header_fields[target_idx];
            }


            //DNA oligo is hardcoded here and need to be improved in the future to allow
            //user to select the type

            loadOligoCSV(data, 0, header_line, delim, seq_colname, oligo_type,
                    id_colname, start_colname, end_colname, target_colname);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadOligoCSV(String[] data, int offset, String header,
            String delim, String oligo_seq_colname,
            int oligo_type,
            String name_colname, String start_colname,
            String end_colname, String target_colname) {
        clearContext();
        ArrayList oligos = new ArrayList();
        String[] header_fields = FileUtil.getFields(header, delim);

        int oligo_seq_idx = -1;
        int name_idx = -1;
        int start_idx = -1;
        int end_idx = -1;
        int target_idx = -1;
        for (int i = 0; i < header_fields.length; i++) {
            if (oligo_seq_colname != null && header_fields[i].trim().equalsIgnoreCase(oligo_seq_colname)) {
                oligo_seq_idx = i;
            }
            if (name_idx == -1 && name_colname != null && header_fields[i].trim().equalsIgnoreCase(name_colname)) {
                name_idx = i;
            }
            if (start_colname != null && header_fields[i].trim().equalsIgnoreCase(start_colname)) {
                start_idx = i;
            }
            if (end_colname != null && header_fields[i].trim().equalsIgnoreCase(end_colname)) {
                end_idx = i;
            }
            if (target_colname != null && header_fields[i].trim().equalsIgnoreCase(target_colname)) {
                target_idx = i;
            }
        }
        if (name_idx == -1) {
            JOptionPane.showMessageDialog(parent,
                    "Name of the oligo sequence is missing in the data file",
                    "Unable to load", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = offset; i < data.length; i++) {
            String line = data[i];
            String[] fields = FileUtil.getFields(line, delim);
            if (fields == null) {
                continue;
            }
            if (fields.length == 0 || fields.length != header_fields.length) {
              //  System.err.println("Missing fields in the line: " + line);
              //  continue;
            }

            String name = null;
            String start = null;
            String seq = null;
            String end = null;
            String target = null;
            Oligo oligo = new Oligo();
            for (int j = 0; j < fields.length; j++) {
                if (j == oligo_seq_idx) {

                    oligo.setSeq(fields[j], oligo_type);
                    // oligo.setSMILES(fields[j]);
                }
                if (j == name_idx) {
                    oligo.setName(fields[j]);
                }
                if (j == start_idx) {
                    try {
                        oligo.setStart(fields[j]);
                    } catch (Exception ex) {
                        ;
                    }
                }
                if (j == end_idx) {
                    try {
                        oligo.setEnd(fields[j]);
                    } catch (Exception ex) {
                        ;
                    }
                }
                if (j == target_idx) {
                    try {
                        oligo.setTargetName(fields[j]);
                    } catch (Exception ex) {
                        ;
                    }
                }
                if (j != oligo_seq_idx && j != name_idx && j != start_idx && j != end_idx && j != target_idx && !header_fields[j].equals("name")) {//avoid duplicated name columns
                    oligo.setProperty(header_fields[j], fields[j]);
                }
            }

            if (oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO) == null || oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO).length() == 0) {
                oligo.setSeq(" ", Oligo.TYPE_PARENT_DNA_OLIGO);//set empty sequence
            }

            if (oligo.getName() != null && oligo.getName().length() != 0 &&
                    oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO) != null && oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO).length() != 0) {//required fields
                oligos.add(oligo);
            }

        }
        postProcessLoad(oligos);

        //move properties to the end
        CustomTableModel table_model = context.getDataStore().getOligoTableModel();
        table_model.getPropertyDisplayManager().moveDisplayOptionLast(new String[]{
                    Oligo.PARENT_DNA_OLIGO_PROP, Oligo.PARENT_SENSE_OLIGO_PROP, Oligo.PARENT_ANTISENSE_OLIGO_PROP,
                    Oligo.REGISTERED_ANTISENSE_OLIGO_PROP, Oligo.REGISTERED_SENSE_OLIGO_PROP
                });
    }

    public void loadConfig(HashMap configs) {
        Iterator iter = configs.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String) configs.get(key);
            if (key.equalsIgnoreCase(PFREDConstant.GROUP_CONFIG)) {
                //group params
                GroupListModel group_model = context.getDataStore().getGroupListModel();
                String[] fields = FileUtil.getFields(value, "\n");
                //each line is the config for a particular group
                for (int i = 0; i < fields.length; i++) {
                    String line = fields[i];
                    int index = line.indexOf("=");
                    if (index <= 0) {
                        continue;
                    }
                    String name = line.substring(0, index);
                    String groupInfoString = line.substring(index + 1);

                    // add this group from string data
                    // note we DONT want to add them to the top of the list or this
                    // will REVERSE the order...
                    group_model.addGroupInfo(name, groupInfoString, false);
                }

                //update group color, etc
                // ArrayList groups = GroupInfoHelper.getGroupInfosFromMolData(mols);
                group_model.setDataIsChanging(true);
                // group_model.addGroupInfo(groups);
                group_model.updateMolColorsFromGroupInfo(); //show the group color
                group_model.setDataIsChanging(false);
            } else if (key.equalsIgnoreCase(PFREDConstant.OLIGO_TABLE_DISP_OPT)) {
                //loading in the oligo table configuration
                PropertyDisplayManager pdm = context.getDataStore().getOligoTableModel().getPropertyDisplayManager();
                pdm.displayOptsFromString(value, context.getDataStore().getOligoTableModel());
                pdm.displayChanged();
            } else if (key.equalsIgnoreCase(PFREDConstant.OLIGO_TABLE_OPTS)) {
                CustomTablePanel oligo_table_panel = context.getUIManager().getOligoTablePane();
                oligo_table_panel.setOptsFromString(value);
            }
        }

    }

    public void setTargetSeq(String name, String seq) {
        TargetListModel target_model = context.getDataStore().getTargetListModel();
        Target t = new Target();
        t.setName(name);
        t.setSeq(seq, Oligo.TYPE_DNA_OLIGO);
        ArrayList data = new ArrayList();
        data.add(t);
        target_model.setDataIsChanging(true);
        target_model.clear();
        target_model.setData(data);
        target_model.setDataIsChanging(false);
    }

    public void enumOligoFromTarget() {

        OligoEnumerationDialog dialog = new OligoEnumerationDialog(parent, "Oligo Enumerator");
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        if (dialog.isCanceled()) {
            return;
        }
        String input = dialog.getSeq();
        int oligo_length = dialog.getLength();
        String prefix = dialog.getPrefix();
        try {
            enumOligoFromTarget(input, prefix, oligo_length, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void enumOligoFromTarget(String seq, String prefix,
            int oligoSize, boolean rc) {
        ArrayList mols = new ArrayList();

        String target = seq;
        String targetName = "target1";
        if (target.startsWith(">")) {
            //this is fasta format
            String[] fields = FileUtil.getFields(target, "\n");
            if (fields.length <= 1) {
                JOptionPane.showMessageDialog(parent, "Invalid FASTA format in the input",
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            targetName = fields[0].substring(1);
            int idx = targetName.indexOf(",");
            if (idx > 0) {
                targetName = targetName.substring(0, idx);
            }
            target = fields[1];

            //always convert RNA to DNA for targets
            if (target.indexOf("U") >= 0 || target.indexOf("u") >= 0) {
                target = OligoHelper.U2T(target);
            }

        }

        //add the target to the model
        //in the future promote people for doing that
        TargetListModel target_model = context.getDataStore().getTargetListModel();

        Target t = new Target();
        t.setName(targetName);
        t.setSeq(target, Oligo.TYPE_DNA_OLIGO);

        target_model.setDataIsChanging(true);
        target_model.addData(t);
        target_model.setDataIsChanging(false);

        int length = target.length();
        for (int i = 0; i < length - (oligoSize - 1); i++) {

            String oligo_seq = target.substring(i, i + oligoSize);
            Oligo oligo = new Oligo();
            oligo.setSeq(oligo_seq, Oligo.TYPE_DNA_OLIGO);
            oligo.setTargetName(targetName);
            oligo.setName(prefix + (i + 1));

            if (rc) {
                oligo.setStart(length - (i + oligoSize - 1));
                oligo.setEnd(length - i);
            } else {
                oligo.setStart(i + 1);
                oligo.setEnd(i + oligoSize);
            }
            mols.add(oligo);
        }
        postProcessLoad(mols);

        //move properties to the end
        CustomTableModel table_model = context.getDataStore().getOligoTableModel();
        table_model.getPropertyDisplayManager().moveDisplayOptionLast(new String[]{
                    Oligo.PARENT_DNA_OLIGO_PROP, Oligo.PARENT_SENSE_OLIGO_PROP, Oligo.PARENT_ANTISENSE_OLIGO_PROP,
                    Oligo.REGISTERED_ANTISENSE_OLIGO_PROP, Oligo.REGISTERED_SENSE_OLIGO_PROP
                });

    }

    public void loadMRUFile(String name) {
        if (name.startsWith("open:")) {
            String fileName = name.substring(5);
            /* if (m_file != null) {
            if (!confirmSave("When you open a file you will lose any unsaved data."))
            return;
            }*/

            File f = new File(fileName);
            PathManager.setCurrentPath(f.getParentFile());
            loadFile(f);
        }
    }

    // import mols from file...add then to current list of mols
    // if there is a header then ignore it...if the new mols were
    // assigned to any groups then remove those properties
    private Object startImport(ProgressReporter pd, Object input) throws
            Exception {
        File f = (File) input;

        // read the mols
        // ArrayList mols = MolFileReader.getUniqueMols(f, pd);

        //return (Object) mols;
        return input;
    }

    // start load
    private Object startLoad(ProgressReporter pd, Object input) throws Exception {
        // start loading mols
        File f = (File) ((Object[]) input)[0];
        String uid = (String) ((Object[]) input)[1];

        // we USED to drop dup mols...now we dont...we handle them later on in postLoad
        //ArrayList mols = MolFileReader.getUniqueMols(f, uid, pd);
        //ArrayList mols = MolFileReader.getMols(f, uid, pd);
        System.out.println("finished init");

        // return (Object) mols;
        return input;
    }

    // done importing
    private void stopImport(Object output) {
        //do nothing
    }

    // load stopped
    private void stopLoad(Object output) {
        //nothing to do here;
    }

    //called after mols are loaded. initialize DisplayProperties, set Mols etc.
    public void postProcessLoad(ArrayList mols) {

        // test the first to see if it is a header
        Datum headerMol = null;
        if (mols.size() > 0) {
            headerMol = (Datum) mols.get(0);
            if (headerMol.getName().indexOf(PFREDConstant.PFRED_HEADER_PROPERTY) >= 0) {
                // this is header so remove it
                mols.remove(headerMol);
            } else {
                headerMol = null;
            }
        }

        System.out.println("DEBUG: Postprocessing load. ");

                 long   start = System.currentTimeMillis();


        // process header before adding groups to props so
        // groups will be defined before group data itself is
        // read from the mols...same deal with clusters becuase
        // some files will NOT have the "cluster_" data in the header
        ArrayList dataToHide = new ArrayList();



        // add the molecules to the data model
        CustomListModel list_model = context.getDataStore().getOligoListModel();
        list_model.setDataIsChanging(true);
        list_model.clear();
        list_model.addData(mols);
        list_model.setDataIsChanging(false); //check for the consequences with the ClusterModel

        //System.out.println("done postprocessload1");
        // set the cluster infos to the cluster model, this step is not really neccessary.
        //ProcessHeader in theory should have generated all the groupInfo already
        ArrayList groups = GroupInfoHelper.getGroupInfosFromMolData(mols);
        GroupListModel group_model = context.getDataStore().getGroupListModel();
        group_model.setDataIsChanging(true);
        group_model.addGroupInfo(groups);
        group_model.updateMolColorsFromGroupInfo(); //show the group color
        group_model.setDataIsChanging(false);
        //System.out.println("done postprocessload2");

        if (dataToHide.size() > 0) {
            list_model.setDataIsChanging(true);
            list_model.setVisible(dataToHide, false);
            list_model.setDataIsChanging(false,
                    CustomListDataEvent.TYPE_VISIBILITY_CHANGED,
                    0, mols.size() - 1);
        }


           long end = System.currentTimeMillis();
           System.out.println("******************Post processing took " + (end - start) / 1000 + "sec");


    }



    // save current file...if none specified then perform save as
    public void save() {
        CustomListModel compounds = context.getDataStore().getOligoListModel();
        ArrayList mols = compounds.getAllData();

        // nothing to save
        if (mols.size() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "There are no molecules loaded so there is nothing to save.");

            return;
        }

        // if we dont have a current file...then do a save as
        if (m_file == null) {
            saveAs();
            return;
        }

        if (/*!m_file.getName().endsWith(".sdf") &&
                !m_file.getName().endsWith(".sdf.gz") &&*/!m_file.getName().endsWith(".fred")) {
            saveAs();
            return;
        }

        // save all mols to current file name (which better be .sdf or .sdf.gz :-)
        //saveToFile(props.getSortedMols(), m_file);
        saveToFile(context, m_file);
        parent.repaint(); 
    }

    // save as a different name
    public void saveAs() {
        saveAs(1);
    }

    // test multiplyer is there for testing only
    // to CREATE large files by duplication of the data
    // for normal use this is always 1
    public void saveAs(int testMultiplyer) {


        // nothing to save
        if (context.getDataStore().getOligoListModel().size() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "There are no molecules loaded so there is nothing to save.");
            return;
        }

      
        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
        fc.setFileFilter(new PFREDFilter());

        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fc.showDialog(parent, "Save As") == JFileChooser.APPROVE_OPTION) {
            // this is now the current file
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".fred")) {
                f = forceFileExtension(f, ".fred");
            }

            // confirm overwrite of file that exists
            if (f.exists()) {
                int option = JOptionPane.showConfirmDialog(parent,
                        "The file you specified already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            PathManager.setCurrentPath(f.getParentFile());

            m_file = f;
            String title = "PFRED";
            if (parent instanceof PFREDView) {
                title = ((PFREDView) parent).frame_name;
            }
            parent.setTitle(title + ": " + f.getName());
            
            saveToFile(context, m_file);
            parent.repaint(); 
        }
    }

    // force a given file extension
    public File forceFileExtension(File f, String ext, String ext2, String ext3) {
        String name = f.getAbsolutePath();
        if (name.endsWith(ext)) {
            return f;
        }
        if (name.endsWith(ext2)) {
            return f;
        }
        if (name.endsWith(ext3)) {
            return f;
        }
        // strip extension (2nd extension if it has one)
        int slash_idx = name.lastIndexOf('\\');
        int index = name.lastIndexOf('.');
        if (index > 0 && slash_idx <= index) {
            name = name.substring(0, index);
        }
        // strip extension
        index = name.lastIndexOf('.');
        if (index > 0 && slash_idx <= index) {
            name = name.substring(0, index);
        }
        File newFile = new File(name + ext2);
        return newFile;
    }

    // force a given file extension
    public File forceFileExtension(File f, String ext) {
        String name = f.getAbsolutePath();
        if (name.endsWith(ext)) {
            return f;
        }

        // strip extension (2nd extension if it has one)
        int slash_idx = name.lastIndexOf('\\');
        int index = name.lastIndexOf('.');
        if (index > 0 && slash_idx <= index) {
            name = name.substring(0, index);
        }
        // strip extension
        index = name.lastIndexOf('.');
        if (index > 0 && slash_idx <= index) {
            name = name.substring(0, index);
        }
        File newFile = new File(name + ext);
        return newFile;
    }

    // save to .pfred file
    public void saveToFile(PFREDContext context, File f) {
        PFREDFileWriter writer = new PFREDFileWriter(parent);
        try {
            writer.write(f, context, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //cache the filename to MRUFIle list
        context.getUIManager().getPFREDFrame().addMRUFile(f.getAbsolutePath());

    }

    // save stopped
    void stopSave(Object output) {
        // nothing to do
    }

    // start save
    private Object startSave(ProgressReporter pd, Object input) throws Exception {
        // when saving to .sdf file...export the header info as well

        Object[] inputArray = (Object[]) input;
        File f = (File) inputArray[0];
        ArrayList mols = (ArrayList) inputArray[1];
        ArrayList propNames = (ArrayList) inputArray[2];

        return null;
    }



    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();

            if (name.equals("new")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                clearContext();
                System.setSecurityManager(sm);
            }
            if (name.equals("open")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                loadFile();
                System.setSecurityManager(sm);
            } else if (name.equals("openSeq")) {
                enumOligoFromTarget();
            } else if (name.equals("openOligoCSV")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                loadOligoCSVFile();
                System.setSecurityManager(sm);
//            } else if (name.equals("openCSV")) {
//                SecurityManager sm = System.getSecurityManager();
//                System.setSecurityManager(null);
//                loadCSVFile();
//                System.setSecurityManager(sm);
//            }
            } else if (name.equals("importProperties")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                importProperties();
                System.setSecurityManager(sm);
            } else if (name.equals("save")) {
                save();
            } else if (name.equals("saveAs")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                saveAs();
                System.setSecurityManager(sm);
            } else if (name.startsWith("open:")) {
                loadMRUFile(name);
            } 


        }
    }

    //
    // ProgressWorker interface
    // any processing lasting long enough to require a progress bar / cancel button
    // some time consuming task has finished (normally or canceled or exception)
    public Object startWork(ProgressReporter pd, String name, Object input) throws
            Exception {
        if (name.equalsIgnoreCase("Loading")) {
            return startLoad(pd, input);
        } else if (name.equalsIgnoreCase("Saving")) // and Exporting
        {
            return startSave(pd, input);
        } else if (name.equalsIgnoreCase("Importing")) {
            return startImport(pd, input);
        }
//        else if (name.equalsIgnoreCase("LoadingCSV")) {
//            return startLoadCSV(pd, input);
//        }
        return null;
    }

    // convenience
    public void stopWork(ProgressReporter pd, String name, Object output,
            Exception e) {
        if (name.equalsIgnoreCase("Loading")) {
            stopLoad(output);
        } else if (name.equalsIgnoreCase("Saving")) {
            stopSave(output);
        } else if (name.equalsIgnoreCase("Importing")) {
            stopImport(output);
        }

    }

    // some time consuming task has stopped
    public void workStopped(ProgressReporter pd, String name, Object output,
            Exception e) {
        // handle error
        if (e != null) {


            if (e instanceof ProgressCanceledException) {
                // dont do anything for canceled
            } else {
                JOptionPane.showMessageDialog(parent,
                        "Error: " + name + ": " +
                        e.getLocalizedMessage());
                e.printStackTrace();
            }
            return;
        }

        // handle standard case
        stopWork(pd, name, output, e);
    }

//    public void loadCSVFile(File f, boolean confirmSave, boolean setFilenameAsTitle) {
//        if (confirmSave && m_file != null) {
//            if (!confirmSave("When you open a file you will lose any unsaved data.")) {
//                return;
//            }
//        }
//
//        //it should really be from structure look up
//        loadCSVFile(f, setFilenameAsTitle);
//    }

    public void importPropertiesFromClipboard() {
        //TODO
    }

    public boolean loadProperties(String[] lines) {
        OligoListModel oligo_model = context.getDataStore().getOligoListModel();
        // for now there MUST be a header
        if (lines.length < 2) {
            return false;
        }

        //check out the property names to avoid illegal characters first
        lines[0] = NameStdizer.correctProperName(lines[0]);

        //Convert lines[0] into String Array with delimiter.
        String delim = FileUtil.calcDelim2(lines[0]);
        StringTokenizer hst = new StringTokenizer(lines[0], delim);
        String[] colName = new String[hst.countTokens()];

        for (int i = 0; i < colName.length; i++) {
            colName[i] = NameStdizer.correctProperName(hst.nextToken().trim());
        }

        StringBuffer failedList = new StringBuffer();
        oligo_model.setDataIsChanging(true);

        if (!oligo_model.setProperties(lines, 1, lines[0], delim, failedList)) {
            // if some of these compounds are not loaded
            ErrorDialog.showErrorDialog(parent,
                    "One or more entries in this property file cannot be loaded in PFRED.\n\n" + lines[0] + "\n" + failedList);
        } else {
            JOptionPane.showMessageDialog(parent,
                    "Properties imported successfully.");
        }

        oligo_model.setDataIsChanging(false);

        return true;

    }

    public boolean loadPropertiesFromTempCSVFile(File f) {

        String[] lines = FileUtil.readStringColumnCSV(f);
        return loadProperties(lines);
    }

    private void importProperties() {
        CustomListModel cmpd_model = context.getDataStore().getOligoListModel();
        if (cmpd_model.getSize() == 0) {
            JOptionPane.showMessageDialog(
                    parent,
                    "This feature lets you load mol properties from a delimited text file,\n" + "but there are no molecules loaded. You must open a file\n" + "before you can Load Properties. ");
            return;
        }
        //provide some information on the format
        JOptionPane.showMessageDialog(
                parent,
                "This feature lets you load molecule properties from a comma or tab\n" +
                "delimited text file. The first line in the file must be the property\n" + "names and the first column must be the compound ids.\n");

        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
        fc.setFileFilter(new CSVFilter());

        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showDialog(parent, "Load Properties") == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            PathManager.setCurrentPath(f.getParentFile());

            PropertyImporter importer = new PropertyImporter(context, parent);
            importer.loadProperties(f);
        }
    }

    public boolean checkPropFileFormat(String[] lines, StringBuffer errMsg) {
        //1. make sure the number of columns are consistent at every line
        String delim = FileUtil.calcDelim2(lines[0]);
        int num_col = FileUtil.getFields(lines[0], delim).length;
        for (int i = 1; i < lines.length; i++) {
            int curr_num_col = FileUtil.getFields(lines[i], delim).length;
            if (curr_num_col != num_col) {
                errMsg.append("error reading the following line:\n");
                errMsg.append(lines[i]);
                errMsg.append("\n");
                errMsg.append("number of properties doesn't match with number of properties specified in the header row\n");
                return false;
            }
        }
        return true;
    }

    class AllFilter
            extends FileFilter {

        public boolean accept(File f) {
            String name = f.getName().trim();
            return name.length() > 0 || f.isDirectory();
        }

        public String getDescription() {
            return "All files (*.*)";
        }
    }

    class PFREDFilter
            extends FileFilter {

        public boolean accept(File f) {
            String name = f.getName().trim().toLowerCase();
            return name.endsWith(".fred") || f.isDirectory();
        }

        public String getDescription() {
            return "PFRED (*.fred)";
        }
    }

    class CSVFilter
            extends FileFilter {

        public boolean accept(File f) {
            String name = f.getName().trim().toLowerCase();
            return name.endsWith(".csv") || name.endsWith(".txt") || f.isDirectory();
        }

        public String getDescription() {
            return "Comma Separated (*.csv), Tab Delimited (*.txt)";
        }
    }
}
