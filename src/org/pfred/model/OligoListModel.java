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

package org.pfred.model;

import java.util.ArrayList;
import java.util.HashMap;
import com.pfizer.rtc.util.FileUtil;
import org.pfred.PFREDConstant;
import org.pfred.util.NameStdizer;
import java.util.Arrays;

import java.util.StringTokenizer;
import java.util.Hashtable;



public class OligoListModel
extends CustomListModel {
	public OligoListModel() {
		super();
	}


	//return TRUE if all named compounds are currently loaded
	public boolean getAllMolsFromList(String[] xlist, ArrayList mols)
	{
		int numLoaded = 0;
		String [] names = new String[xlist.length];
		//first standardize the names from input
		for (int i=0; i<xlist.length; i++)
		{
			names[i] = NameStdizer.stdPfizerCpName(xlist[i]);
		}

		ArrayList sorted = getAllData();
		String [] molnames = new String [sorted.size()];
		for (int i=0; i<molnames.length; i++)
		{
			molnames[i] = ( (Datum) sorted.get(i) ).getName();
		}
		Arrays.sort(molnames);
		Arrays.sort(names);

		if (names.length==0 )
			return true;
		if (molnames.length==0)
			return false;

		int i=0; int j=0;
		Datum mol;
		String tmpName = null;
		String tmpMolName = null;
		String lastMatchName = null;

		while (i<names.length && j<molnames.length)
		{
			if (tmpName == null)
				tmpName = NameStdizer.getParentName(names[i]);

			if (tmpMolName == null)
				tmpMolName = NameStdizer.getParentName(molnames[j]);

			if ( tmpName.compareTo(tmpMolName)==0 )
			{
				mol = (Datum) getDatum(molnames[j]); //this code needs to be cleaned up.
				mols.add(mol);
				numLoaded++;
				i++;
				j++;
				lastMatchName = tmpName; //we cache this to avoid duplicates
				tmpName = null;
				tmpMolName = null;
			}
			else if (tmpName.compareTo(tmpMolName)<0)
			{
				if (lastMatchName!=null && tmpName.equals(lastMatchName))
					numLoaded++; //duplicated name in the xlist
				i++;
				tmpName = null;
			}
			else
			{
				if (lastMatchName!=null && tmpMolName.equals(lastMatchName))
				{
					mol = (Datum) getDatum(molnames[j]); //this code needs to be cleaned up.
					mols.add(mol); //duplicated name in the current loaded mols, e.g. compounds with different salt code. We just select them all
				}
				j++;
				tmpMolName = null;
			}
		}


		// return true if we loaed them all
		return numLoaded == names.length;
	}


	//Added for setting properties with user selection
	// determine delim automatically
	public boolean setProperties(String[] names, int offset, String header,
			StringBuffer failedList, String[] selected_cols,
			int nameColIndex,
			String selMappedProp, int mergeOption, boolean isNamePfizerId) {
		return setProperties(names, offset, header, FileUtil.calcDelim2(header),
				failedList, selected_cols, nameColIndex,
				selMappedProp, mergeOption, isNamePfizerId);
	}


	// set properties from array of strings a separate string
	// gives the property column names
	public boolean setProperties(String[] lines, int offset, String header,
			String delim, StringBuffer failedList,
			String[] selected_cols,
			int nameColIndex, String selMappedProp,
			int mergeOption, boolean isNamePfizerId) {

		boolean wasError = false;


		ArrayList sorted = this.getAllData();

		// get header info...assume compound name is first column
		String[] colNames = FileUtil.getFields(header, delim);
		int[] selected_col_indices =new int[selected_cols.length];
		for (int i=0; i<selected_cols.length; i++){
			boolean found =false;
			for (int j=0; j<colNames.length; j++){
				if (selected_cols[i].equals(colNames[j])){
					selected_col_indices[i] = j;
					found =true;
					break;
				}
			}
			if (!found) {
				//this should not happen but just in case we skip it
				selected_col_indices[i]=-1;
			}
		}


		for (int i = 0; i < colNames.length; i++) {
			colNames[i] = NameStdizer.correctProperName(colNames[i]);
		}
		for (int i = 0; i < selected_cols.length; i++) {
			selected_cols[i] = NameStdizer.correctProperName(selected_cols[i]);
		}

		//Validate fields[nameColIndex] to be a unique value
		//in all the lines to be processed.
		//at the same time create the name hashes for incoming lines
		int numlinesChecked = 0;

		HashMap names2data = new HashMap();//this one holds the incoming data
		HashMap parentnames2data = new HashMap();

		for (int linesindex = offset; linesindex < lines.length; linesindex++) {
			if (lines[linesindex].length() == 0) { //skip empty lines
				numlinesChecked++;
				continue;
			}
			String[] fieldstocheck = FileUtil.getFields(lines[linesindex], delim);
			String nametocheck = NameStdizer.stdPfizerCpName(fieldstocheck[nameColIndex]);

			if (names2data.containsKey(nametocheck)) {

				//Error
				String msg = lines[linesindex] + "\tFail to import properties. "
				+ nametocheck + " :Duplicate entries in the input file.\n";
				failedList.append(msg);
				wasError = true;
				break;

			}
			else {
				names2data.put(nametocheck, lines[linesindex]);
				if (isNamePfizerId)
					parentnames2data.put(NameStdizer.getParentName(nametocheck), lines[linesindex] );
			}

		}

		if (wasError) {
			return false;
		}

		// try to add these properties to each in list
		int numProcessed = 0;
		int size = sorted.size();
		HashMap imported = new HashMap();
		// now scan through all data in current data model and merge corresponding value
		for (int i=0; i<size; i++) {
			Datum mol = (Datum) sorted.get(i);
			String mapped_value = (String) mol.getProperty(selMappedProp);
			
			if (mapped_value==null|| mapped_value.trim().length()==0) continue;

			//first look up with the incoming data with the exact value
			String line = (String) names2data.get(mapped_value);
			if (line==null){
				//check for standardized name
				line = (String) names2data.get(NameStdizer.stdPfizerCpName(mapped_value));
			}
			//if that fails and it is a pfizer id value, try parent name look up
			if (line==null && isNamePfizerId) {
				line = (String) parentnames2data.get(NameStdizer.getParentName(mapped_value));//

				//in case for a parent name match, check further if explicit salt code field matches
				//this is to ensure cmpds with the same parent name but different salt codes won't match
				if (line != null) {
					String[] tmpfields = FileUtil.getFields(line, delim);
					if (tmpfields.length <= nameColIndex)
						continue;
					String nameCol = tmpfields[nameColIndex];
					if (!NameStdizer.equals(nameCol, mapped_value)) {
						line = null;
					}
				}
			}

			if (line == null) {
				continue; //no data for this molecule to merge
			}

			if (line!=null) {
				imported.put(line, "1"); //mark we imported this line;
				numProcessed++;
			}


			//Loop thorugh to set the properties on each Mols.
			String[] fields = FileUtil.getFields(line, delim);
			// set properties for this mol
			for (int j = 0; j < selected_col_indices.length; j++) {
				int idx = selected_col_indices[j];
				String propName = selected_cols[j];
				if (propName.equals(selMappedProp)) continue; //avoid resetting reformated name
				if (idx >= 0 && idx < fields.length) {

					if (mergeOption==PFREDConstant.REPLACE){
						mol.setProperty(propName, fields[idx]);
					}else if (mergeOption==PFREDConstant.MERGE){
						Object tmp=(Object) mol.getProperty(propName);
						if (tmp!=null && !(tmp instanceof String)){
							continue;
						}

						String currValue=(String)tmp;
						if (currValue==null||currValue.trim().length()==0){
							currValue=fields[idx];
						}else
							currValue=currValue+PFREDConstant.PFRED_CONCATENATE_SEPARATOR+fields[idx]; //use ; as the separator for concatenated records

						mol.setProperty(propName, currValue);
					}else{
						//keep current value;
						//check if property exists. If it does, leave it otherwise add the new property
						Object tmp=(Object) mol.getProperty(propName);
						if (tmp==null){
							mol.setProperty(propName, fields[idx]);
						}
					}
				}
			}

		}

		if (numProcessed == names2data.size())
			return true; //we are done we have imported all


		for (int i=offset; i<lines.length; i++){
			String line = lines[i];
			if (!imported.containsKey(line)){
				String msg = lines[i] + "\tFail to import properties. Corresponding name is not found in sdf file.\n";
				failedList.append(msg);
				wasError = true;

			}
		}
		return !wasError;

	}

	public boolean setProperties(String[] lines, int offset,
			String header, String delim,
			StringBuffer failedList)
	{
		boolean wasError = false;

		//we create a temporatory hashtable that hashes molecules on their parent names
		ArrayList sorted = this.getAllData();
		Hashtable parent_names = new Hashtable(this.size());
		for (int i=0; i<sorted.size();i++)
		{
			Oligo tmp = (Oligo) sorted.get(i);
			parent_names.put(NameStdizer.getParentName(tmp.getName()), tmp);
		}


		// get header info...assume compound name is first column
		StringTokenizer hst = new StringTokenizer(header,delim);
		String[] colName = new String[hst.countTokens()];
		for (int i=0; i<colName.length; i++)
			colName[i] = NameStdizer.correctProperName(hst.nextToken().trim());

		// try to add these properties to each in list
		int numProcessed = 0;
		for (int i=offset; i<lines.length; i++)
		{
			if (lines[i]==null || lines[i].length()==0) //skip empty lines
			{
				numProcessed++;
				continue;
			}
			String[] fields = FileUtil.getFields(lines[i],delim);
			//in version 1 we force field length being the same as the ones in the header
			//this causes problem when some csv file generated from excel
			//with missing value in the last column which ends up with fewer data fields
			//we therefore don't enforce this constraint anymore. The missing value in the end
			//is replace with empty string
			/*
        if (fields.length != colName.length)//could cause problem for rows with empty fields.
        {
          wasError = true;
          String msg = lines[i]+ "\tFailed to import properties. "
              + fields.length+ " properties found. It should be "+colName.length + ".\n";
          failedList.append(msg);
          System.out.println(msg);
          continue;
        }*/
			String name = NameStdizer.stdPfizerCpName(fields[0]);//stdize the pfizer compound name
			Oligo oligo = (Oligo) this.getDatum(name);
			//now check for compounds with the same parent name
			if (!this.hasDatum(name) && parent_names.containsKey(NameStdizer.getParentName(name)))
			{
				oligo = (Oligo) parent_names.get(NameStdizer.getParentName(name));
				if (!NameStdizer.equals(oligo.getName(), name))
					oligo=null; //this could be the case where two compounds have the same parent name but two explicit salt codes
			}

			if (oligo == null)
			{
				String msg = lines[i] + "\tFail to import properties. "+name+" is not found in sdf file.\n";
				failedList.append(msg);
				System.out.println(msg);
				continue;
			}

			// set properties for this mol
			for (int j=1; j<colName.length; j++)
			{
				if (j<fields.length)
					oligo.setProperty(colName[j],fields[j]);
				else {
					oligo.setProperty(colName[j], "");
				}
			}
			numProcessed++;
		}


		return !wasError && (numProcessed == (lines.length-offset));
	}
	
	
	public ArrayList findMissingNames(String[] names, boolean isIdPfizerName){
		ArrayList missing=new ArrayList();

		//we create a temporatory hashtable that hashes molecules on their parent names
		ArrayList sorted = this.getAllData();
		Hashtable nameHash = new Hashtable(this.size());
		for (int i=0; i<sorted.size(); i++)
		{
			Oligo tmp = (Oligo) sorted.get(i);
			if (isIdPfizerName){
				String name = NameStdizer.stdPfizerCpName(tmp.getName());
				nameHash.put(NameStdizer.getParentName(name), "1");
			}else{
				nameHash.put(tmp.getName(), "1");
			}
		}


		// get header info...assume compound name is first column
		for (int i=0; i<names.length; i++){
			if (names[i]==null){
				continue;
			}
			if (isIdPfizerName){
				String name = NameStdizer.stdPfizerCpName(names[i]);
				if (!nameHash.containsKey(NameStdizer.getParentName(name))) {
					missing.add(names[i]);
				}
			}
			else if (!nameHash.containsKey(names[i])){
				missing.add(names[i]);
			}
		}
		return missing;
	}

}
