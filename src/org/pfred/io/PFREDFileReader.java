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

import java.util.ArrayList;
import javax.swing.JFrame;
import com.pfizer.rtc.task.ProgressDialog;
import com.pfizer.rtc.task.SimpleProgress;
import com.pfizer.rtc.task.ProgressWorker;
import com.pfizer.rtc.task.ProgressReporter;
import java.util.HashMap;
import org.pfred.PFREDConstant;

import org.pfred.model.Oligo;
import org.pfred.model.Target;


import com.pfizer.rtc.util.FileUtil;

public class PFREDFileReader implements ProgressWorker  {
   private JFrame parent=null;
   private ArrayList oligos=null;
   private ArrayList targets=null;
   private HashMap configs=null;

//   private static Logger logger = Logger.getLogger("com.pfizer.pfred.io.PFREDFileReader");

  public PFREDFileReader(JFrame parent) {
    this.parent=parent;
  }

  public ArrayList getOligos(){
    return oligos;
  }

  public ArrayList getTargets(){
    return targets;
  }

  public HashMap getConfiguration(){

    return configs;
  }


  public void read(File filename,
                    boolean showProgress) throws Exception {
     oligos=null;
     targets=null;
     configs=null;

     long start, end;

     start=System.currentTimeMillis();
     ZipFile zfile = new ZipFile(filename);
     ZipEntry ze= zfile.getEntry(PFREDConstant.DEFAULT_OLIGO_FILENAME);
     InputStream oligo_is=zfile.getInputStream(ze);


     if (!showProgress)
       oligos = readOligoListFile(oligo_is, false, new SimpleProgress());
     else {
       Object[] inputs = new Object[] {oligo_is, new Boolean(false)};
       ProgressDialog pd = new ProgressDialog(this, parent,
                                              "Opening file", true, inputs);

       if (pd.isJobSuccessful())
         oligos=(ArrayList) pd.getOutput();

     }
     end=System.currentTimeMillis();
     //logger.info("Load pfred file took "+(end-start)/1000 + "sec");
     System.out.println("Load oligo file took "+(end-start)/1000 + "sec");

     oligo_is.close();


     start=System.currentTimeMillis();
     ze = zfile.getEntry(PFREDConstant.DEFAULT_TARGET_FILENAME);
     if (ze==null){
       return;
     }
     InputStream target_is=zfile.getInputStream(ze);

     targets=readOligoListFile(target_is,true, new SimpleProgress());

     target_is.close();
     end=System.currentTimeMillis();
     //logger.info("Load pfred file took "+(end-start)/1000 + "sec");
     System.out.println("Load target file took "+(end-start)/1000 + "sec");

     //read configuration

     ze = zfile.getEntry(PFREDConstant.DEFAULT_CONFIG_FILENAME);
     if (ze == null) {
       return;
     }
     InputStream config_is = zfile.getInputStream(ze);
     configs=readConfigFile(config_is, new SimpleProgress());

  }

  public HashMap readConfigFile(InputStream is, ProgressReporter pd)throws Exception {
   HashMap configs=new HashMap();
   BufferedReader br=new BufferedReader(new InputStreamReader(is));
   String line=br.readLine();
   String name=null;
   String value=null;
   while (line != null) {
     if (line.startsWith("#")) {
       line = br.readLine();
       continue;
     }
     if (line.startsWith(">")) {
       if (name != null && value != null) {
         configs.put(name, value);
       }
       name = line.substring(1);
       value = null;
     }
     else {
       if (value == null)
         value = line;
       else
         value = value + "\n" + line;
     }
     line = br.readLine();

   }
    if (name != null && value != null) {
      configs.put(name, value); //put the last set there
    }
   return configs;
 }


  public ArrayList readOligoListFile(InputStream is, boolean isTarget, ProgressReporter pd)throws Exception {
    String delim="\t";
    ArrayList result=new ArrayList();
    BufferedReader br=new BufferedReader(new InputStreamReader(is));

    String header =br.readLine();
    if (header==null) {
      return result;
    }
    String[] header_fields=FileUtil.getFields(header, delim);
    //check out the headers
    String line=br.readLine();
    while (line!=null){
      pd.workComplete("Reading input: ", 0, 0);
      String[]fields = FileUtil.getFields(line, delim);
      Oligo o=null;
      if (isTarget)
        o = new Target();
        else
          o = new Oligo();

      for (int i=0; i<fields.length; i++){
        if (i>=header_fields.length){
          //something is wrong
          throw new Exception("Extra fields found in the line\n"+line);
          //maybe in the future allow skipping these lines
        }else if (header_fields[i].equals(Oligo.NAME_PROP)){
          o.setName(fields[i]);
        }else if (header_fields[i].equals(Oligo.ANTISENSE_OLIGO_PROP) ||
        		header_fields[i].equals(Oligo.PARENT_ANTISENSE_OLIGO_PROP)){
          o.setSeq(fields[i],Oligo.TYPE_PARENT_ANTISENSE_OLIGO);
        }
        else if (header_fields[i].equals(Oligo.SENSE_OLIGO_PROP) ||
        		header_fields[i].equals(Oligo.PARENT_SENSE_OLIGO_PROP)) {
          o.setSeq(fields[i], Oligo.TYPE_PARENT_SENSE_OLIGO);
        }else if (header_fields[i].equals(Oligo.DNA_OLIGO_PROP) ||
        		header_fields[i].equals(Oligo.PARENT_DNA_OLIGO_PROP)) {
          o.setSeq(fields[i], Oligo.TYPE_PARENT_DNA_OLIGO);
        }
        else if (header_fields[i].equals("target_name")) {
          o.setTargetName(fields[i]);
        }
        else if (header_fields[i].equals("start")) {
          o.setStart(fields[i]);
        }
        else if (header_fields[i].equals("end")) {
          o.setEnd(fields[i]);
        }
        else {
          o.setProperty(header_fields[i], fields[i]);
        }
      }
      if (o.getName()!=null && o.getSingleLetterSeq(Oligo.TYPE_DNA_OLIGO)!=null){
        result.add(o);
      }

      line=br.readLine();
    }

    br.close();
    return result;
  }

  /*************** Implements ProgressWorker ***********************/

   // some time consuming task has finished (normally or canceled or exception)
   public Object startWork(ProgressReporter pd, String name, Object input) throws
       Exception {
     if (name.equalsIgnoreCase("Opening file")){
       Object[] inputs = (Object[]) input;
       InputStream is=(InputStream) inputs[0];
       boolean isTarget=((Boolean) inputs[1]).booleanValue();
       return readOligoListFile(is,isTarget, pd);
     }
     return input;
   }

 // convenience
   public void workStopped(ProgressReporter pd, String name, Object output,
                        Exception e) {
     if (name.equalsIgnoreCase("Opening file"))
     {;}

   }


}
