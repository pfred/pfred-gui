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

import org.pfred.rest.RestServiceClient;
import javax.swing.table.*;
import com.pfizer.rtc.util.FileUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

public class TargetTableModel
        extends AbstractTableModel {

    String[] header;
    ArrayList data = new ArrayList();
    int id_colIdx = 2;

    public String getPrimaryTargetID() {
        int size = data.size();
        if (size == 0l) {
            return null;
        }

        for (int i = 0; i < size; i++) {
            if (data.get(i) == null) {
                continue;
            }
            Transcript row = (Transcript) data.get(i);
            if (row.getProperty(header[0]).equals("true")) {
                return row.getName();
            }
        }
        return null;
    }

    public String getSecondaryTargetIDs(String delim) {
        StringBuffer ids = new StringBuffer();
        if (data == null) {
            return null;
        }
        int size = data.size();
        for (int i = 0; i < size; i++) {
            Transcript t = getRow(i);
            if (t == null) {
                continue;
            }

            if (t.getIsSecondaryTranscript().equals("true")) {
                if (ids.length() != 0) {
                    ids.append(delim);
                }
                ids.append(t.getName());
            }
        }
        return ids.toString();
    }
//species=human,human,rat,mouse,mouse
//ids=ENST00000378474,ENSRNOT00000004305,ENSMUST00000115524

    public String getListOfSpecies(String delim) {
        StringBuffer species = new StringBuffer();
        if (data == null) {
            return null;
        }
        int size = data.size();
        for (int i = 0; i < size; i++) {
            Transcript t = getRow(i);
            if (t == null) {
                continue;
            }

            if (t.getIsSecondaryTranscript().equals("true") || t.getIsPrimaryTranscript().equals("true")) {

                if (species.indexOf(t.getSpecies()) < 0) {

                    String s = t.getSpecies();
                    if (s.toUpperCase().contains("HOMO_")) {
                        s = "human";
                    }

                    if (s.toUpperCase().contains("MUS_")) {
                        s = "mouse";
                    }
                    if (s.toUpperCase().contains("RATTUS_")) {
                        s = "rat";
                    }
                    //bowtie only works for human
                    if (s == "human" || s == "mouse" || s == "rat") {
                        if (species.indexOf(s) < 0) { //new
                            if (species.length() != 0) {
                                species.append(delim);
                            }
                            species.append(s);
                        }
                    }
                }
            }
        }

        return species.toString();
    }

    public String getListOfTranscripts(String delim) {
        StringBuffer species = new StringBuffer();
        StringBuffer ids = new StringBuffer();
        if (data == null) {
            return null;
        }
        int size = data.size();
        for (int i = 0; i < size; i++) {
            Transcript t = getRow(i);
            if (t == null) {
                continue;
            }

            if (t.getIsSecondaryTranscript().equals("true") || t.getIsPrimaryTranscript().equals("true")) {

                if (species.indexOf(t.getSpecies()) < 0) {
                    if (species.length() != 0) {
                        species.append(delim);
                    }
                    species.append(t.getSpecies());
                    //bowtie only works for human
                    if (t.getSpecies().toUpperCase().contains("HOMO_")
                            || t.getSpecies().toUpperCase().contains("MUS_")
                            || t.getSpecies().toUpperCase().contains("RATTUS_")) {
                        if (ids.length() != 0) {
                            ids.append(delim);
                        }
                        ids.append(t.getName());
                    }

                }
            }
        }

        return ids.toString();
    }

    public ArrayList getUserSpecifiedTranscripts() {
        int size = data.size();
        ArrayList userSpecified = new ArrayList();
        for (int i = 0; i < size; i++) {
            Transcript t = (Transcript) data.get(i);
            if (t.isUserSpecified()) {
                userSpecified.add(t);
            }
        }
        return userSpecified;
    }

    public String getUserSpecifiedTranscriptsAsText() {
        int size = data.size();
        StringBuffer userSpecified = new StringBuffer();
        for (int i = 0; i < size; i++) {
            Transcript t = (Transcript) data.get(i);
            if (t.isUserSpecified()) {
                if (userSpecified.length() > 0) {
                    userSpecified.append("\n");
                }
                userSpecified.append(">");
                userSpecified.append(t.getName());
                // userSpecified.append(",");
                // userSpecified.append(t.getSpecies());
                // userSpecified.append(",");
                // userSpecified.append(t.getSequence().length());
                // userSpecified.append(",");
                // userSpecified.append(t.getSource());
                userSpecified.append("\n");
                userSpecified.append(t.getSequence());

            }
        }
        return userSpecified.toString();
    }

    public void addTranscript(String[] names, String[] sequences, String[] species) {
        for (int i = 0; i < names.length; i++) {
            addTranscript(names[i], sequences[i], species[i]);
        }
    }

    public void addTranscript(String name, String sequence, String species) {
        Transcript row = new Transcript(name);
        row.setProperty(header[0], "false");
        row.setProperty(header[1], "true");
        row.setProperty("name", name);
        row.setProperty("length", "" + sequence.length());
        row.setProperty("species", species);
        row.setProperty("source", Transcript.TRANSCRIPT_SRC_USER_SPECIFIED);

        data.add(row);
    }

    public void addTranscript(String runName, ArrayList transcripts) {
        HashMap names = new HashMap();
        int size = transcripts.size();
        String fastafile = "sequence.fa";
        String msg = null;

        msg = "";
        for (int i = 0; i < size; i++) {
            Transcript t = (Transcript) transcripts.get(i);
            names.put(t.getName(), t);
            msg +="\n";
            // add transcript and sequence to sequence file here
            msg += ">" + t.getName() + "\n";
            msg += t.getSequence();
        }

        String Respond = RestServiceClient.runAddToFileUtilityService(runName, fastafile, msg);
        size = data.size();
        for (int i = size - 1; i >= 0; i--) {
            Transcript row = (Transcript) data.get(i);
            if (names.containsKey(row.getName())) {
                data.remove(i);
            }
        }

        data.addAll(transcripts);
    }

    public void addTranscript(Transcript t) {
        int size = data.size();
        for (int i = size - 1; i >= 0; i--) {
            Transcript row = (Transcript) data.get(i);
            if (row.getName().equals(t.getName())) {
                data.remove(i);
            }
        }
        data.add(t);
    }

    /**
     * make sure there is no empty line the rows. Rows need to well formatted before coming in
     *
     * @param rows String[]
     * @param delim String
     */
    public TargetTableModel(String[] rows, String delim, int id_colIdx, int length_colIdx) throws Exception {// just in case we have format problem
        super();
        this.id_colIdx = id_colIdx;
        String line = rows[0];
        String[] header_fields = FileUtil.getFields(line, delim);//add to more
        header = new String[header_fields.length + 2];
        header[0] = "PrimaryTarget";
        header[1] = "SecondaryTarget";
        for (int i = 2; i < header.length; i++) {
            header[i] = header_fields[i - 2];
        }
        int nrows = rows.length - 1;
        int ncols = header.length;
        data = new ArrayList();
        //first find the longest human transcript
        int idx_primaryTarget = 0;
        int longest_length = 0;
        for (int i = 1; i < rows.length; i++) {
            String[] fields = FileUtil.getFields(rows[i], delim);
            int length = Integer.parseInt(fields[length_colIdx]);
            String id = fields[id_colIdx];
            if (id.startsWith("ENST") && length > longest_length) {
                longest_length = length;
                idx_primaryTarget = i;
            }
        }

        //enter in all transcripts
        for (int i = 1; i < rows.length; i++) {
            String[] fields = FileUtil.getFields(rows[i], delim);
            Transcript t = new Transcript();
            data.add(t);
            for (int j = 0; j < ncols; j++) {
                //the first column is to mark primary sequence
                if (j == 0) {
                    if (i == idx_primaryTarget) {
                        t.setProperty(header[j], "true");
                    } else {
                        t.setProperty(header[j], "false");
                    }
                } //the second column is to let user select secondary target
                else if (j == 1) {
                    //set the ones with length greater than half the primary transcript
                    int length = Integer.parseInt(fields[length_colIdx]);
                    if (length > longest_length / 2) {
                        t.setProperty(header[j], "true");
                    } else {
                        t.setProperty(header[j], "false");
                    }
                } /* else if (header[j].equalsIgnoreCase(this.TRANSCRIPT_SRC){

                }*/ else if (j - 2 < fields.length) {
                    t.setProperty(header[j], fields[j - 2]);
                }
            }

        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= data.size() || columnIndex >= header.length) {
            return "";
        }
        Transcript row = getRow(rowIndex);
        return row.getProperty(header[columnIndex]);
    }

    public String getColumnName(int columnIndex) {
        return header[columnIndex];
    }

    public Class getColumnClass(int columnIndex) {
        //String, number, date,
        return String.class; //todo check for column type
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return header.length;
    }

    public boolean isCellEditable(int rowIndex,
            int columnIndex) {
        if (columnIndex == 0 || columnIndex == 1) {
            return true;
        }
        return false;
    }

    public Transcript getRow(int i) {
        return (Transcript) data.get(i);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null) {
            return;
        }
        if (columnIndex == 0) {
            //set primary
            //if aValue is true de-select first
            if (aValue.equals("true")) {
                int size = data.size();
                for (int i = 0; i < size; i++) {
                    Transcript row = getRow(i);
                    row.setProperty(header[0], "false");
                }

                getRow(rowIndex).setProperty(header[0], "true");
            }
        }

        if (columnIndex == 1) {
            //set all secondary target
            getRow(rowIndex).setProperty(header[1], aValue.toString());
        }
        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    public String getSelectedPrimaryTarget() {
        return null;
    }

    public String[] getSelectedSecondaryTargets() {
        return null;
    }

    public void removeRows(int[] rows) {
        if (rows == null) {
            return;
        }
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; i--) {
            data.remove(rows[i]);
        }
    }
}
