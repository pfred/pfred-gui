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
package org.pfred.property;

import com.pfizer.rtc.util.ErrorDialog;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import com.pfizer.rtc.util.FileUtil;
import java.util.HashMap;

import org.pfred.OligoHelper;
import org.pfred.PFREDConstant;
import org.pfred.PFREDContext;
import org.pfred.model.Oligo;
import org.pfred.model.OligoListModel;
import org.pfred.util.NameStdizer;
import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import org.pfred.dialog.DuplicatedRecordDialog;
import org.pfred.dialog.ImportCompoundPropertyListDialog2;


public class PropertyImporter {
  PFREDContext context;
  JFrame parent;

  public PropertyImporter(PFREDContext context, JFrame parent) {
    this.context = context;
    this.parent = parent;
  }


  public boolean loadProperties(File f){
     String[] lines = FileUtil.readStringColumnCSV(f);
     return loadProperties(lines);
  }

  public boolean processClipboardImportData(String copiedData) {
    // System.out.println("Inside processClipboardImportData");
    //  System.out.println("Data : "+copiedData);
    StringTokenizer hst = null;

    hst = new StringTokenizer(copiedData, System.getProperty("line.separator"));
    String[] lines = new String[hst.countTokens()];

    for (int countindex = 0; countindex < lines.length; countindex++)
      lines[countindex] = (hst.nextToken().trim());
    return loadProperties(lines);
  }

  public void loadPropertiesFromStandardFormat(String[] lines, String delim) {
    if (lines == null || lines.length <= 1)return;

    OligoListModel oligo_model = context.getDataStore().getOligoListModel();
    oligo_model.setDataIsChanging(true);
    StringBuffer failedList = new StringBuffer();
    boolean succeeded=oligo_model.setProperties(lines, 1, lines[0], delim, failedList);
    oligo_model.setDataIsChanging(false);
    if (!succeeded) {
      ErrorDialog.showErrorDialog(parent,
                                  "One or more entries in this property file cannot be loaded in PFRED.\n\n"
                                  + lines[0] + "\n" + failedList);
    }
    else {
      JOptionPane.showMessageDialog(parent,
                                    "Properties imported successfully.");
    }

  }


  public boolean loadProperties(String[] lines){
    return loadProperties(lines, true, true);
  }

  // load properties from a delimited text file
  public boolean loadProperties(String[] lines, boolean promptForCLSLookup,
                                boolean promptForFailed) {
    OligoListModel cmpd_model = context.getDataStore().getOligoListModel();


    // for now there MUST be a header
    if (lines==null||lines.length < 2)
      return false;

    //check out the property names to avoid illegal characters first
    lines[0] = NameStdizer.correctProperName(lines[0]);
    String delim = FileUtil.calcDelim2(lines[0]);
    //Convert lines[0] into String Array with delimiter.

    StringTokenizer hst = new StringTokenizer(lines[0], delim);
    String[] colName = new String[hst.countTokens()];

    for (int i = 0; i < colName.length; i++)
      colName[i] = NameStdizer.correctProperName(hst.nextToken());



    String[] availablePropnames = cmpd_model.getPropertyManager().
        getAllPropertyNames();



    ImportCompoundPropertyListDialog2 selectDialog = new
        ImportCompoundPropertyListDialog2(parent,
                                          "Select Id and Properties to Import",
                                          "New properties: Select unique ID field:",
                                          "Existing properties: Select unique ID field:",
                                          "Please Select New Properties to Import:",
                                          colName,
                                          availablePropnames);

    //User selected columns from CSV File columns
    String[] selected_cols = selectDialog.getSelColumn();
    //System.out.println("selCol set:" + selCol);

    if (selectDialog.isCanceled() || selected_cols == null)
      return false;

    //User selected id column from existing Table for mapping
    String selMappedProp = selectDialog.getMappedPropertySelected();
    boolean isNamePfizerId = selectDialog.isNamePfizerId();
    int mergeOption = selectDialog.getMergeOption();
    //System.out.println("selMappedProp:" + selMappedProp);

    StringBuffer failedList = new StringBuffer();
    cmpd_model.setDataIsChanging(true);

    int nameColIndex = -1;
    //Get nameColIndex by getting 1st value in set and comparing the String array and getting the index
    // String[] selColName = (String[])selCol.toArray(new String[selCol.size()]);

    //Identify the user selected Id column's position from CSV file columns
    //Checks what is the column position of the selected id column
    for (int i = 0; i < colName.length; i++) {
      //System.out.println(colName[i4]);
      if (colName[i].equals(selectDialog.getNameFieldSelected())) {
        nameColIndex = i;
        break;
      }
    }
    //System.out.println("NameColIndex:" + nameColIndex);



    //deduplicate in the inputs
    String[] newLines=deduplicate2(lines, nameColIndex,isNamePfizerId, delim);
    if (newLines==null) return false; //cancel the import

    //check for cmpd ids to make sure we look up structures that are missing in the file currently
    String[] names= new String[newLines.length-1];
    for (int i=1; i<newLines.length; i++){

      String line = newLines[i];
      if (line==null ||line.trim().length()==0) continue;

      String[] fields = FileUtil.getFields(line, delim);
      if (fields.length<=nameColIndex) {
        System.err.println("invalid line: "+line);//we shouldn't get to this
        continue;
      }
      names[i-1]=fields[nameColIndex];
    }

    //todo check for structure applying structure comparison rules
    ArrayList<String> missingNames=cmpd_model.findMissingNames(names, isNamePfizerId);
    


//    if (missingNames.size()!=0 && promptForCLSLookup && hasPfizerPFNames) {
//
//    	//prompt user to lookup data
//    	int option = JOptionPane.showConfirmDialog(parent,
//    			"Some input ids are not found in the current PFRED session, do you want to look up their structures?",
//    			"RNANotation lookup", JOptionPane.YES_NO_OPTION);
//
//    	if (option == JOptionPane.OK_OPTION) {
//
//    		ArrayList mols = null;
//    		try {
//    			String[] ids=new String[missingNames.size()];
//    			for (int i=0; i<ids.length; i++){
//    				ids[i]=missingNames.get(i);
//    			}
//    			RNANotationLookup service = new RNANotationLookup(parent);
//    			mols = service.id2Oligos(parent, ids, true);
//    			if (mols!=null)
//    				cmpd_model.addData(mols); //add additional mols into the model
//    		}
//    		catch (Exception ex) {
//    			JOptionPane.showMessageDialog(parent,
//    					"Failed to look up structure. " +ex.getMessage(),
//    					"Failed to look up structure",
//    					JOptionPane.ERROR_MESSAGE);
//    			ex.printStackTrace();
//    		}
//
//
//    	}
//    }



    /* if (!cmpd_model.setProperties(lines, 1, lines[0], failedList, selected_cols,
     nameColIndex, selMappedProp, isNamePfizerId)) {
     */
    if (!cmpd_model.setProperties(newLines, 1, newLines[0], failedList,
                                  selected_cols,
                                  nameColIndex, selMappedProp, mergeOption, isNamePfizerId)) { // if some of these compounds are not loaded
      if (promptForFailed){
        ErrorDialog.showErrorDialog(parent,
                                    "One or more entries in this property file cannot be loaded in PFRED.\n\n"
                                    + lines[0] + "\n" + failedList);
      }
    }
    else {
      JOptionPane.showMessageDialog(parent,
                                    "Properties imported successfully.");
    }
    cmpd_model.setDataIsChanging(false);

    return true;
  }
  
  
  	public void mergeOligos(ArrayList<Oligo> oligos){
  		//string merge data by target site
  		OligoListModel oligo_model = context.getDataStore().getOligoListModel();
  		HashMap<String,Oligo> oligoHash=new HashMap();
  		int size=oligos.size();
  		
  		for (int i=0; i<size; i++){
  			Oligo o=oligos.get(i);
  			String target_site=o.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO);
  			oligoHash.put(target_site, o);
  		}
  		
  		
  		size=oligo_model.size();
  		oligo_model.setDataIsChanging(true);
  		for (int i=0; i<size; i++){
  			Oligo o= (Oligo) oligo_model.getDatum(i);
  			String target_site=o.getSingleLetterSeq(Oligo.TYPE_PARENT_DNA_OLIGO);
  			if (oligoHash.containsKey(target_site)){
  				Oligo input_o=oligoHash.get(target_site);
  				//merge in the data
  				OligoHelper.mergeOligo(o, input_o);
  			}
  		}
  		oligo_model.setDataIsChanging(false);
  	}
  
  	
  	
  	
  
   public String[] deduplicate2(String[] lines, int nameColIndex,
                               boolean isNamePfizerId, String delim){
     int numlinesChecked = 0;

     HashMap names2data = new HashMap(); //this one holds the incoming data
     ArrayList names=new ArrayList();    //keep track of the original sequence
     ArrayList dup_names=new ArrayList();

     HashMap duplicateDataMap = new HashMap();

     String header = lines[0];
     String[] headerFields=FileUtil.getFields(header, delim);

     for (int linesindex = 1; linesindex < lines.length; linesindex++) {
       if (lines[linesindex]==null || lines[linesindex].length() == 0) { //skip empty lines
         numlinesChecked++;
         continue;
       }
       String[] fieldstocheck = FileUtil.getFields(lines[linesindex], delim);
       String nametocheck=fieldstocheck[nameColIndex];
       if (isNamePfizerId){
          nametocheck= NameStdizer.stdPfizerCpName(nametocheck);
       }
       if (names2data.containsKey(nametocheck)) {
         if (duplicateDataMap.containsKey(nametocheck)) {
           ArrayList tempList = (ArrayList) duplicateDataMap.get(nametocheck);
           tempList.add(lines[linesindex]);
           duplicateDataMap.put(nametocheck, tempList);
         }
         else {
           String line = (String) names2data.get(nametocheck); //add all the duplicated records
           ArrayList tempList = new ArrayList();
           tempList.add(line);
           tempList.add(lines[linesindex]);
           duplicateDataMap.put(nametocheck, tempList);
         }
       }
       else { //cache first occurrence of all names
         names2data.put(nametocheck, lines[linesindex]);
         names.add(nametocheck);
       }
     }


     //Show duplicate dialog
     if (duplicateDataMap.size()!=0) {

       DuplicatedRecordDialog optionDialog = new DuplicatedRecordDialog(parent);


       if (optionDialog.isCanceled()) return null;//nothing to do;

       int option=optionDialog.getOption();
       Iterator keyIter = duplicateDataMap.keySet().iterator();
       while (keyIter.hasNext()) {

         //Get key
         String namekey = (String) keyIter.next();

         if (option == DuplicatedRecordDialog.SKIP) {
           names2data.remove(namekey);
         }
         else if (option == DuplicatedRecordDialog.KEEP_LAST) {
           ArrayList tempList = (ArrayList) duplicateDataMap.get(namekey);
           String templine = (String) tempList.get(tempList.size() - 1);
           names2data.put(namekey, templine);
         }
         else if (option == DuplicatedRecordDialog.KEEP_FIRST) {
           //the last row is already kept automatically
         }
         else if (option == DuplicatedRecordDialog.KEEP_SEPARATE) {
           //rename the duplicated rows and add to the list
           ArrayList tempList = (ArrayList) duplicateDataMap.get(namekey);
           int tempSize = tempList.size();
           for (int i = 1; i < tempSize; i++) { //skip the first one as it is already kept
             String templine = (String) tempList.get(i);
             String[] fieldstocheck = FileUtil.getFields(templine,
                 delim);
             String nametocheck = fieldstocheck[nameColIndex];
             if (isNamePfizerId) {
               nametocheck = NameStdizer.stdPfizerCpName(nametocheck);
             }
             String newname =NameStdizer.addDuplicationSuffix(nametocheck, i); //this will add _dup1, _dup2 .... to the duplicated names
             dup_names.add(newname);
             fieldstocheck[nameColIndex]=newname;
             String newline=FileUtil.array2String(fieldstocheck, delim);
             names2data.put(newname, newline);
           }

         }else if (option == DuplicatedRecordDialog.MERGE ){
           //merge overlapping fields with ';' as the separator
           //the first line is the header line
           String[] newfields=new String[headerFields.length];
           newfields[nameColIndex]=namekey;
             ArrayList tempList = (ArrayList) duplicateDataMap.get(namekey);
             int tempSize = tempList.size();

             for (int i = 0; i < tempSize; i++) {
                String templine = (String) tempList.get(i);
               //merge in
               String [] tempFields=FileUtil.getFields(templine, delim);

               for (int j=0; j<tempFields.length; j++){
                 if (j==nameColIndex) continue; //avoid merging the name column
                 if (tempFields[j]==null || tempFields[j].length()==0) continue; //skip empty fields;
                 if (newfields[j]==null){
                   newfields[j]=tempFields[j];
                 }else{
                   newfields[j] = newfields[j] +
                       PFREDConstant.PFRED_CONCATENATE_SEPARATOR + tempFields[j];
                 }
               }
             }

             String newline=FileUtil.array2String(newfields, delim);
             names2data.put(namekey, newline);
         }
       }
     } else { //no duplicates return the original line
       return lines;
     }

 
     ArrayList newlines=new ArrayList();
     //first add the header line
     newlines.add(header+delim+PFREDConstant.PFRED_ORIGINAL_NAME);
     //then add the unique names
     int uniq_name_count=names.size();
     for (int i=0; i<uniq_name_count; i++){
       String name=(String)names.get(i);
       if (names2data.containsKey(name)){ //some records might be skipped if user choose the skip option
         String newline=((String)names2data.get(names.get(i)))+delim+name;
         newlines.add(newline);
       }
     }
     //then add the names with _dup suffix
     int dup_name_count=dup_names.size();
     for (int i=0; i<dup_name_count; i++){
       String dup_name=(String)dup_names.get(i);
       if (names2data.containsKey(dup_name)){
         String newline=(String)names2data.get(dup_name)+delim+NameStdizer.removeDuplicationSuffix(dup_name);// append the original name to the end so we still have the original name information
         newlines.add(newline);
       }
     }

     int newlines_size=newlines.size();
     String[] result = new String[newlines_size];
     for (int i=0; i<newlines_size; i++){
       result[i]=(String) newlines.get(i);
     }
     return result;
  }

}
