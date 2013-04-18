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

package org.pfred.io;


import java.io.*;
import java.util.zip.*;
import org.pfred.PFREDContext;
import java.util.ArrayList;
import javax.swing.JFrame;

import com.pfizer.rtc.task.ProgressDialog;
import com.pfizer.rtc.task.SimpleProgress;
import com.pfizer.rtc.task.ProgressWorker;
import com.pfizer.rtc.task.ProgressReporter;
import java.util.HashMap;

import org.pfred.OligoHelper;
import org.pfred.PFREDConstant;
import org.pfred.group.GroupInfo;
import org.pfred.group.GroupInfoHelper;
import org.pfred.group.GroupListModel;
import org.pfred.model.Datum;
import org.pfred.model.Oligo;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;
import org.pfred.property.PropertyDisplayManager;
import org.pfred.table.CustomTablePanel;

public class PFREDFileWriter implements ProgressWorker{

  private JFrame parent=null;
  public PFREDFileWriter(JFrame parent) {
    this.parent=parent;
  }

  public void write(File filename, PFREDContext context,
                    boolean showProgress) throws Exception {

    FileOutputStream fos=new FileOutputStream(filename);
    ZipOutputStream zos=new ZipOutputStream(fos);
    //1. first write out the meta file
    // the meta file will tell basically which file is which in the zip file
    ZipEntry ze=new ZipEntry(PFREDConstant.META_FILENAME);
    zos.putNextEntry(ze);
    writeMetaFile(zos);


    ze=new ZipEntry(PFREDConstant.DEFAULT_OLIGO_FILENAME);
    zos.putNextEntry(ze);
    ArrayList oligos=context.getDataStore().getOligoListModel().getAllData();
    writeListFile(zos, oligos, null,false, showProgress); //we want to reserve the RNANotation

    ze=new ZipEntry(PFREDConstant.DEFAULT_TARGET_FILENAME);
    zos.putNextEntry(ze);
    ArrayList targets=context.getDataStore().getTargetListModel().getAllData();
    writeListFile(zos, targets, null, false, showProgress);

    ze=new ZipEntry(PFREDConstant.DEFAULT_CONFIG_FILENAME);
    zos.putNextEntry(ze);
    writeConfigFile(zos, context);

    zos.flush();
    zos.close();
  }

  private void writeListFile(OutputStream os, ArrayList data, String[] props, boolean convertNotation2Seq, boolean showProgress)
  throws Exception {
    String delimiter="\t";
    if (!showProgress) {

      writeCSV(parent, data, os, delimiter, props, convertNotation2Seq, new SimpleProgress());
    }
    else {
      Object[] inputs = new Object[] {
          parent, data, os, delimiter, new Boolean(convertNotation2Seq)};
      ProgressDialog pd = new ProgressDialog(this, parent,
                                             "Writing CSV file", true,
                                             inputs);
      if (!pd.isJobSuccessful()) {

        Exception ex = pd.getException();
        if (ex != null)
          throw ex; //propagate
      }
    }

  }

  private void writeConfigFile(OutputStream os, PFREDContext context){
    //store all the configuration of oligo table, group
    //configuration file looks pretty much like the basic fasta file
    //e.g. >name1
    //     value1
    //     >name2
    //     value2
    PrintStream ps=new PrintStream(os);
    ps.println("#### PFRED visual configuration to be added ########");
    //first group info
    // set a property for each group... in a reversed order

    GroupListModel group_model = context.getDataStore().getGroupListModel();
    ArrayList groups = group_model.getAllGroupInfos();

    if (groups.size()>0){
      ps.println(">"+PFREDConstant.GROUP_CONFIG);
      for (int i = 0; i < groups.size(); i++) {
        GroupInfo info = (GroupInfo) groups.get(i);
        String value = GroupInfoHelper.getValueString(info);
        String keyName = info.name.replace(' ', '_');

        // save this property
        ps.println(keyName+"="+value);
      }
    }

    //gui config for the oligo table
    CustomTablePanel oligo_table=context.getUIManager().getOligoTablePane();
    ps.println("#### Configuration for oligo table");
    ps.println(">"+PFREDConstant.OLIGO_TABLE_DISP_OPT);
    PropertyDisplayManager pdm=context.getDataStore().getOligoTableModel().getPropertyDisplayManager();
    ps.println(pdm.displayOptsToString());
    ps.println(">"+PFREDConstant.OLIGO_TABLE_OPTS);
    ps.println(oligo_table.optsToString());
  }


  private void writeMetaFile(OutputStream os) throws Exception {
    PrintStream ps=new PrintStream(os);
    ps.println(PFREDConstant.OLIGO_FILE+"="+PFREDConstant.DEFAULT_OLIGO_FILENAME);
    ps.println(PFREDConstant.TARGET_FILE+"="+PFREDConstant.DEFAULT_TARGET_FILENAME);
    ps.println(PFREDConstant.CONFIG_FILE+"="+PFREDConstant.DEFAULT_CONFIG_FILENAME);
    ps.flush();
  }




  public static void writeCSV(JFrame jFrame, ArrayList data,
                              OutputStream os, String delimiter,
                              String[] props, boolean convertNotation2SimpleLetter,
                              ProgressReporter pd) throws Exception {

    PrintStream out = new PrintStream(os);
    int size = data.size();
    //find out all the propnames;
    HashMap nameHash = new HashMap();
    ArrayList names = new ArrayList();
    if (props==null){//when no prop explicitly specified, just get all the properties in the oligos
      for (int i = 0; i < size; i++) {
        Datum datum = (Datum) data.get(i);
        ArrayList propNames = datum.propertyNames();
        int count = propNames.size();
        for (int j = 0; j < count; j++) {
          if (nameHash.containsKey(propNames.get(j))) {
            continue;
          }
          names.add(propNames.get(j));
          nameHash.put(propNames.get(j), "1");
        }
      }
    }else{
      for (int i=0; i<props.length; i++){
        names.add(props[i]);
        nameHash.put(props[i], "1");
      }
    }
///
    int propCount = names.size();
    //write out the header
    StringBuffer header = new StringBuffer();
    header.append(Oligo.NAME_PROP);
    header.append(delimiter);

    header.append(Oligo.PARENT_DNA_OLIGO_PROP);
    header.append(delimiter);

    header.append(Oligo.PARENT_ANTISENSE_OLIGO_PROP);
    header.append(delimiter);
    header.append(Oligo.PARENT_SENSE_OLIGO_PROP);
    header.append(delimiter);

    header.append(Oligo.TARGET_NAME_PROP);
    header.append(delimiter);
    header.append(Oligo.START_POS_PROP);
    header.append(delimiter);
    header.append(Oligo.END_POS_PROP);


    for (int i = 0; i < propCount; i++) {
      String propName = (String) names.get(i);
      if (propName.equals(Oligo.NAME_PROP) ||
           propName.equals(Oligo.PARENT_ANTISENSE_OLIGO_PROP)||
           propName.equals(Oligo.PARENT_SENSE_OLIGO_PROP)||
           propName.equals(Oligo.PARENT_DNA_OLIGO_PROP)||
           propName.equals(Oligo.START_POS_PROP) ||
           propName.equals(Oligo.END_POS_PROP)||
           propName.equals(Oligo.TARGET_NAME_PROP)
           )
         continue;


      header.append(delimiter);
      header.append(propName);
    }
    out.println(header.toString());

    //writer out the value
    for (int i = 0; i < size; i++) {
      Oligo oligo = (Oligo) data.get(i);
      StringBuffer line = new StringBuffer();
      line.append(oligo.getName());
      line.append(delimiter);

      String seq=null;

      seq=oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO);
      if (seq==null)
       	seq="";
      line.append(seq);
      line.append(delimiter);

      seq=oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_ANTISENSE_OLIGO);
      if (seq==null)
     	seq="";
      line.append(seq);
      line.append(delimiter);

      seq=oligo.getSingleLetterSeq(Oligo.TYPE_PARENT_SENSE_OLIGO);
      if (seq==null)
       	seq="";
      line.append(seq);
      line.append(delimiter);



      String val=oligo.getTargetName();
      if (val==null)
       	val="";
      line.append(val);
      line.append(delimiter);

      line.append(oligo.getStart());
      line.append(delimiter);
      line.append(oligo.getEnd());


      for (int j = 0; j < propCount; j++) {
        String propName = (String) names.get(j);
        if (propName.equals(Oligo.NAME_PROP) ||
            propName.equals(Oligo.PARENT_ANTISENSE_OLIGO_PROP)||
            propName.equals(Oligo.PARENT_SENSE_OLIGO_PROP)||
            propName.equals(Oligo.PARENT_DNA_OLIGO_PROP)||
            propName.equals(Oligo.START_POS_PROP) ||
            propName.equals(Oligo.END_POS_PROP)||
            propName.equals(Oligo.TARGET_NAME_PROP)
            )
          continue;

        line.append(delimiter);
        Object value =  oligo.getProperty(propName);
        if (value == null)
          value = "";

        if ( OligoHelper.isOligoSeqProperty(propName) && OligoHelper.isRNANotation(value.toString())&& convertNotation2SimpleLetter){
        	RNAPolymer polymer=new RNAPolymer(value.toString());
        	value=polymer.getSingleLetterSeq();
        }
        line.append(value);
      }
      out.println(line.toString());
      // progress
      if (i % 100 == 0)
        pd.workComplete("writing...", i, size);

    }

    pd.workComplete("writing...", size, size); //completed
    out.flush();
  }


  /*************** Implements ProgressWorker ***********************/

// some time consuming task has finished (normally or canceled or exception)
  public Object startWork(ProgressReporter pd, String name, Object input) throws
      Exception {
    if (name.equalsIgnoreCase("Writing CSV file")) {
      Object[] inputs = (Object[]) input;
      JFrame frame = (JFrame) inputs[0];
      ArrayList data = (ArrayList) inputs[1];
      OutputStream os = (OutputStream) inputs[2];
      String delimiter = (String) inputs[3];
      boolean convertNotation2SimpleLetter=((Boolean)inputs[4]).booleanValue();
      writeCSV(frame, data, os, delimiter, null, convertNotation2SimpleLetter, pd);
    }

    return input;
  }

// convenience
  public void workStopped(ProgressReporter pd, String name, Object output,
                          Exception e) {
    if (name.equalsIgnoreCase("Writing CSV SMILES file")) {
      ;
    }

  }



}
