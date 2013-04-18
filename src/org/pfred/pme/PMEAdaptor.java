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
package org.pfred.pme;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.pfred.PFREDContext;
import org.pfred.model.CustomListSelectionModel;
import org.pfred.model.Oligo;
import com.pfizer.rtc.notation.editor.editor.MacroMoleculeViewer;

import com.pfizer.rtc.notation.editor.utility.NotationParser;


public class PMEAdaptor implements ListSelectionListener{
	private PFREDContext context;
	private MacroMoleculeViewer mmv;
	private CustomListSelectionModel sel_model;
	public PMEAdaptor(PFREDContext context, MacroMoleculeViewer viewer){
		this.context=context;
		this.mmv=viewer;
		this.sel_model=context.getDataStore().getOligoListSelectionModel();
		sel_model.addListSelectionListener(this);
	}
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		if (e.getSource()!=sel_model)return;

		int count=sel_model.getSelectedDataCount();
		if (count==0){
			//mmv.setNotation("");
		}
		if (count!=1) return;
		
		int start = sel_model.getMinSelectionIndex();
		
		Oligo o=(Oligo) sel_model.getSelectedData().get(0);
		String notation=o.getNotation();
		if (notation!=null && notation.length()!=0){
			try {
				mmv.setNotation(notation);
			}catch (Exception ex){
				System.out.println();
			}
		}
	}
	
	public static String getNucleotideSequenceNotation(String seq){
		try{
			String notation= NotationParser.getNucleotideSequenceNotation(seq);
			return notation;
		}
		catch (Exception ex){
			return null;
		}
	}
	
	public static  String getNucleotideSequenceFromNotation(String notation){
		
		return NotationParser.getSimpleNucleotideSequenceNotation(notation);
	}
	
}
