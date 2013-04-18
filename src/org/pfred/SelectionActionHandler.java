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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;


import org.pfred.model.CustomListModel;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Datum;
import org.pfred.model.OligoListModel;
import com.pfizer.rtc.util.FileUtil;
import com.pfizer.rtc.util.PathManager;


public class SelectionActionHandler implements ActionListener, ClipboardOwner {

    PFREDContext context;
    JFrame parent;
    SearchFrame searchFrame = null;

    public SelectionActionHandler(PFREDContext context, JFrame parent) {
        this.context = context;
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src instanceof Component) {
            String name = ((Component) src).getName();
            if (name.equalsIgnoreCase("search")) {
                SearchFrame f = new SearchFrame(context.getDataStore(), parent, searchFrame);
                searchFrame = f; // save for next time...
                f.setLocationRelativeTo(parent);
                f.setResizable(false);
                f.setVisible(true);

            } else if (name.equals("import selection")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                importSelection();
                System.setSecurityManager(sm);
            } else if (name.equals("export selection")) {
                SecurityManager sm = System.getSecurityManager();
                System.setSecurityManager(null);
                exportSelection();
                System.setSecurityManager(sm);
            } else if (name.equals("copyToClipboard")) {
                copySelectionToClipboard();
            } else if (name.equals("selectFromClipboard")) {
                selectFromClipboard();
            } else if (name.equals("selectMatchingStructures")) {
                selectMatchingStructures();
            } else if (name.equals("selectAll")) {
                CustomListSelectionModel cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();
                CustomListModel cmpd_model = context.getDataStore().getOligoListModel();
                cmpd_sel_model.setValueIsAdjusting(true);
                cmpd_sel_model.clearSelection();
                cmpd_sel_model.setSelectionInterval(0, cmpd_model.getSize() - 1);
                cmpd_sel_model.setValueIsAdjusting(false);
            } else if (name.equals("inverseSelection")) {
                inverseSelection();
            }

        }
    }

    public void selectMatchingStructures() {
        //to be re-implemented to select by oligo sequence
    }

    public void inverseSelection() {
        CustomListSelectionModel cmpd_sel_model = context.getDataStore().getOligoListSelectionModel();
        cmpd_sel_model.setValueIsAdjusting(true);
        cmpd_sel_model.inverseSelection();
        cmpd_sel_model.setValueIsAdjusting(false);
    }

    private void copySelectionToClipboard() {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();

        if (sel_model.getSelectedDataCount() < 1) {
            JOptionPane.showMessageDialog(parent,
                    "There are no molecules selected. You must select some molecules \n" +
                    "before you can Copy an XList to the Clipboard. ");
            return;
        }
        // get the names of the selected mols as a string
        // separated by the given delimiter
        String names = sel_model.getSelectedMolsAsString("\n", true);

        // copy this text to the clipboard
        System.out.println("Copying to clipboard:");
        System.out.print(names);

        // copy text to clipboard
        StringSelection text = new StringSelection(names);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(text, this);
    }

    // ClipboardOwner interface
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // not worried
    }

    // set selection from clipboard contents
    private void selectFromClipboard() {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        OligoListModel model = context.getDataStore().getOligoListModel();

        if (sel_model == null) {
            JOptionPane.showMessageDialog(parent,
                    "There are no molecules loaded. You must open or import a file\n" +
                    "before you can Paste an XList selection. ");
            return;
        }

        // get selection from clipboard and turn into a list of names
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(this);
        if (trans == null) {
            return;
        }

        String text = null;
        try {
            text = (String) trans.getTransferData(DataFlavor.stringFlavor);
        } catch (IOException ioe) {
        } catch (UnsupportedFlavorException ufe) {
        }
        if (text == null) {
            JOptionPane.showMessageDialog(parent,
                    "This feature lets you set the selection from an XList in the clipboard,\n" +
                    "but the clipboard does not contain any compound names");

            return;
        }
        // see if it even looks like one
        if (!isCleanXlist(text)) {
            int options = JOptionPane.YES_NO_OPTION;
            int option =
                    JOptionPane.showConfirmDialog(parent,
                    "This feature lets you set the selection from an XList in the clipboard,\n" +
                    "but the clipboard does not appear to contain a valid XList.\n" +
                    "A valid XList will contain only compound names separated\n" +
                    "by either spaces, tabs, commas, carriage-returns, or line-feeds.\n" +
                    "Do you wish to attempt to Paste the clipboard contents anyway?",
                    "Paste Anyway?", options);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // turn this into array of names
        StringTokenizer st = new StringTokenizer(text, "\n");
        int total = st.countTokens();
        String[] names = new String[total];
        for (int i = 0; i < total; i++) {
            names[i] = st.nextToken();
        }

        /* if there are no compounds at all
        if (!props.anyExist(names))
        {
        JOptionPane.showMessageDialog(this,
        "This feature lets you set the selection from an XList in the clipboard,\n" +
        "but the clipboard does not contain any compound names");
        return;
        }*/

        // if some of these compounds are not loaded
        ArrayList mols = new ArrayList();

        if (!model.getAllMolsFromList(names, mols)) {
            JOptionPane.showMessageDialog(parent,
                    "One or more compounds in the clipboard are not loaded in PFRED and\n" +
                    "cannot be selected.");
        }
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(mols, false);
        sel_model.setValueIsAdjusting(false);
    }

    // TODO:
    // return false if data does not look like valid XList data
    private boolean isCleanXlist(String text) {
        return true;
    }

    private void importSelection() {
        CustomListModel model = context.getDataStore().getOligoListModel();
        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "This feature lets you set the selection from an XList file,\n" +
                    "but there are no molecules loaded. You must open a file\n" +
                    "before you can Load an XList. ");
            return;
        }

        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
        fc.setFileFilter(new XListFilter());

        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showDialog(parent, "Load Selection from XList") == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            PathManager.setCurrentPath(f.getParentFile());
            loadSelection(f);
        }
    }

    private void exportSelection() {
        CustomListModel model = context.getDataStore().getOligoListModel();
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();
        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "This feature lets you save the selected compound names to\n" +
                    "an XList file, but there are no molecules loaded. You must open\n" +
                    "or import a file before you can Save an XList. ");
            return;
        }

        if (sel_model.getSelectedDataCount() <= 0) {
            JOptionPane.showMessageDialog(parent,
                    "No compounds are selected. You must select the compounds you wish to Save as an Xlist");
            return;
        }

        JFileChooser fc = new JFileChooser(PathManager.getCurrentPath());
        fc.setFileFilter(new XListFilter());

        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showDialog(parent, "Save Selection As XList") == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (f.exists()) {
                if (JOptionPane.showConfirmDialog(parent, "A file with this name already exists. Overwrite?",
                        "Confirm Overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            PathManager.setCurrentPath(f.getParentFile());
            saveSelection(f);
        }
    }

    // load the selection from an text file containing compound names one per line
    public boolean loadSelection(File f) {
        OligoListModel model = context.getDataStore().getOligoListModel();
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();

        String[] names = FileUtil.readStringColumnCSV(f);

        ArrayList mols = new ArrayList();

        if (!model.getAllMolsFromList(names, mols)) {
            JOptionPane.showMessageDialog(parent,
                    "One or more compounds in the clipboard are not loaded in PFRED and\n" +
                    "cannot be selected.");
        }
        sel_model.setValueIsAdjusting(true);
        sel_model.selectData(mols, false);
        sel_model.setValueIsAdjusting(false);

        return true;
    }

    // save the selection from an text file containing compound names one per line
    public boolean saveSelection(File f) {
        CustomListSelectionModel sel_model = context.getDataStore().getOligoListSelectionModel();

        PrintStream ps = FileUtil.getPrintStream(f);
        ArrayList mols = sel_model.getSelectedData();
        for (int i = 0; i < mols.size(); i++) {
            Datum m = (Datum) mols.get(i);
            ps.println(m.getName());
        }
        ps.close();
        return true;
    }

    class XListFilter
            extends FileFilter {

        public boolean accept(File f) {
            String name = f.getName().trim().toLowerCase();
            return name.endsWith(".lst") || f.isDirectory();
        }

        public String getDescription() {
            return "XList (*.lst)";
        }
    }
}
