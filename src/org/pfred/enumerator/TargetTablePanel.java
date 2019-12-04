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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.table.TableColumnModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.Dimension;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import java.awt.Component;
import java.util.ArrayList;


public class TargetTablePanel extends JPanel implements TableModelListener, ActionListener {
    JTable targetTable=new JTable();
    SimpleTableSorter sorter=null;
    JScrollPane jsp_center=null;
    TargetTableModel model=null;
    JButton bttn_addTranscript=null;
    JButton bttn_deleteTranscript=null;
    String runDir=null;

    public TargetTablePanel(String runName, String[] targetData, String delim, int id_colIdx, int length_colIdx) throws Exception{//exception when there are data format problem in targetData
        JTextArea jta_desc=new JTextArea("Pick the primary and secondary targets. siRNA oligos will be enumerated from the primary target"+
                                         " and their occurrence in secondary targets (orthologs) will be reported. By default, the longest human transcript is selected"+
                                         " as the primary target. Any other transcripts longer than half the length of the primary transcript are selected as secondary transcripts\n");

        runDir = runName;
        jta_desc.setEditable(false);
        jta_desc.setWrapStyleWord(true);

        jta_desc.setLineWrap(true);
        jta_desc.setBackground(new JLabel().getBackground());

        this.setLayout(new BorderLayout());
        this.add(jta_desc, BorderLayout.NORTH);

        model=new TargetTableModel(targetData, delim, id_colIdx, length_colIdx);
        model.addTableModelListener(this);
        sorter = new SimpleTableSorter(model);
        targetTable.setDragEnabled(true);
        targetTable.setModel(sorter);
        targetTable.createDefaultColumnsFromModel();
        targetTable.addMouseListener(new TargetTableMouseAdaptor());

        sorter.setTableHeader(targetTable.getTableHeader());
        jsp_center=new JScrollPane(targetTable);
        Dimension d=jsp_center.getViewport().getViewSize();
        d.height=300;
        jsp_center.getViewport().setViewSize(d);
        this.add(jsp_center, BorderLayout.CENTER);


        setupCellRenderer();

        JPanel bttn_pane=new JPanel();
        bttn_pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        bttn_addTranscript=new JButton("Add Transcripts");
        bttn_pane.add(bttn_addTranscript);
        bttn_addTranscript.addActionListener(this);
        bttn_deleteTranscript=new JButton("Delete Selected");
        bttn_deleteTranscript.addActionListener(this);
        bttn_pane.add(bttn_deleteTranscript);
        this.add(bttn_pane, BorderLayout.SOUTH);
        this.setPreferredSize(d);
    }

    private void setupCellRenderer(){
        TableColumnModel col_model = targetTable.getColumnModel();
        //col_model.getColumn(0).setE
        col_model.getColumn(0).setCellRenderer(new RadioButtonCellRenderer());
        col_model.getColumn(0).setCellEditor(new RadioButtonCellEditor());
        col_model.getColumn(1).setCellRenderer(new CheckBoxCellRenderer());
        col_model.getColumn(1).setCellEditor(new CheckBoxCellEditor());
        col_model.getColumn(2).setCellRenderer(new HTMLCellRenderer());
        //html render

    }


    public void tableChanged(TableModelEvent e) {
        targetTable.repaint();
    }

    public TargetTableModel getTargetTableModel(){
        return model;
    }

    public void actionPerformed(ActionEvent evt){
        Object src = evt.getSource();
        if (src==bttn_addTranscript){
            addTranscripts();
        }else if (src==bttn_deleteTranscript){
            deleteTranscripts();
        }

    }

    private void deleteTranscripts(){
        int[]selected=targetTable.getSelectedRows();
        model.removeRows(selected);
        model.fireTableDataChanged();
    }

    private void addTranscripts(){
        //1. show a simple dialog to take in fasta sequences
        EnterTranscriptsDialog dialog=new EnterTranscriptsDialog(getDialogForComponent(this));
        dialog.setVisible(true);
        if (dialog.isCanceled()) return;
        ArrayList transcripts=dialog.getTranscripts();
        //2. add to table
        model.addTranscript(runDir, transcripts);
        model.fireTableDataChanged();
    }

    public static JDialog getDialogForComponent(Component c) {
        if (c == null)
            return null;
        if (c instanceof JDialog)
            return (JDialog) c;
        return getDialogForComponent(c.getParent());
    }

}
