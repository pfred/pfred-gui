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

import org.pfred.OligoHelper;

import com.pfizer.rtc.notation.tools.ComplexNotationParser;

import com.pfizer.rtc.notation.model.*;
import com.pfizer.rtc.util.OrderedProperties;
import java.awt.Color;
import java.util.*;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;

public class Oligo implements Datum {

   public final static String DNA_OLIGO_PROP = "dna_oligo";
   public final static String ANTISENSE_OLIGO_PROP="antisense_oligo";
   public final static String SENSE_OLIGO_PROP="sense_oligo";
   public final static String PARENT_SENSE_OLIGO_PROP = "parent_sense_oligo";
   public final static String PARENT_ANTISENSE_OLIGO_PROP = "parent_antisense_oligo";
   public final static String PARENT_DNA_OLIGO_PROP = "parent_dna_oligo";
   public final static String REGISTERED_SENSE_OLIGO_PROP = "registered_sense_oligo";
   public final static String REGISTERED_ANTISENSE_OLIGO_PROP = "registered_antisense_oligo";
   public final static String SIRNA_DESIGN_PROP="siRNA_design";

   public final static String TARGET_PROP = "target";
   public final static String START_POS_PROP = "start";
   public final static String END_POS_PROP = "end";
   public final static String TARGET_NAME_PROP = "target_name";
   public final static String NAME_PROP = "name";
   public final static String RNA_NOTATION_PROP="RNA_notation";
   public final static int TYPE_DNA_OLIGO=0;
   public final static int TYPE_ANTISENSE_OLIGO=1;
   public final static int TYPE_SENSE_OLIGO=2;
   public final static int TYPE_RNANOTATION=3;
   public final static int TYPE_PARENT_DNA_OLIGO=0;
   public final static int TYPE_PARENT_ANTISENSE_OLIGO=1;
   public final static int TYPE_PARENT_SENSE_OLIGO=2;

   private String name = "";

   private String target_id="";


   private boolean invisible = false;
   private Color color;

   private OrderedProperties p = new OrderedProperties();

   public Oligo() {
   }

   public Oligo(String name, String seq, int type){
     this.name=name;
   }

   public Oligo(Oligo datum) {
      OrderedProperties p = datum.getProperties();
      if (p != null) {
         setProperties(p);
      }
      setName(datum.getName());
      //parent_antisense_oligo = getSeq(TYPE_ANTISENSE_OLIGO);
      //parent_sense_oligo = getSeq(TYPE_SENSE_OLIGO);
      //parent_dna_oligo = getSeq(TYPE_DNA_OLIGO);
   }

   public OrderedProperties getProperties() {
      return p;
   }

   public void setProperties(OrderedProperties p1) {
      p = p1;
   }

   public boolean isInvisible() {
      return invisible;
   }

   public void setInvisible(boolean invisible) {
      this.invisible = invisible;
   }

   public Color getColor() {
      return color;
   }

   public void setColor(Color c) {
      color = c;
   }

   public void setProperty(String name, Object value) {
      if (value == null) {
         return;
      }
      if (value instanceof String &&
    		  (name.equals(this.REGISTERED_ANTISENSE_OLIGO_PROP)||name.equals(this.REGISTERED_SENSE_OLIGO_PROP )||
    				  name.equals(this.RNA_NOTATION_PROP))
    		  ){
    	  RNAPolymer polymer=new RNAPolymer((String)value);
    	  p.setProperty(name, polymer);
      }
      else {
    	  if (value instanceof String && ((String) value).length() < 40) {
    		  setPropertyIntern(name.intern(), value.toString().intern(), false);//to reuse strings
    	  } else {
    		  setPropertyIntern(name.intern(), value, false); //to reuse strings
    	  }
      }
   }

   private void setPropertyIntern(String name, Object value, boolean first) {
      p.setProperty(name, value, first);
   }

   public void setProperty(String name, Object value, boolean first) {
      if (value == null) {
         return;
      }
      if (value instanceof String && ((String) value).length() < 40) {
         setPropertyIntern(name.intern(), value.toString().intern(), first);//to reuse strings
      } else {
         setPropertyIntern(name.intern(), value, first); //to reuse strings
      }
   }

   public Object getProperty(String name) {
      return p.getProperty(name);
   }

   public Object removeProperty(String name) {
      return p.removeProperty(name);
   }

   public ArrayList propertyNames() {
      return p.propertyNames();
   }

   public void clearProperties() {
      p.clear();
   }

   public int propertyCount() {
      return p.propertyCount();
   }

   public String toString() {
      return name;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      // early out
      if (this.name.equalsIgnoreCase(name)) {
         return;
      }
      this.name = name;
      p.setProperty("name", name);
   }

   public String getDisplayName() {
      return name;
   }

   public void setSeq(String seq, int type) {

   	if (type==TYPE_RNANOTATION){
   		setNotation(seq);
   		return;
   	}


	 String parent_antisense_oligo="";
	 String parent_sense_oligo="";
	 String parent_dna_oligo="";
	 String oligo_notation;

     switch (type){

       case TYPE_PARENT_DNA_OLIGO:
         parent_dna_oligo=OligoHelper.U2T(seq);
         parent_antisense_oligo=OligoHelper.dna2antisense_rna(seq);
         parent_sense_oligo=OligoHelper.dna2sense_rna(seq);

         break;
       case TYPE_PARENT_ANTISENSE_OLIGO:
         parent_antisense_oligo=seq;
         parent_dna_oligo=OligoHelper.antisense_rna2dna(seq);
         parent_sense_oligo=OligoHelper.dna2sense_rna(parent_dna_oligo);
         break;
       case TYPE_PARENT_SENSE_OLIGO:
         parent_sense_oligo=OligoHelper.T2U(seq);
         parent_dna_oligo=OligoHelper.sense_rna2dna(seq);
         parent_antisense_oligo=OligoHelper.sense_rna2antisense_rna(seq);
         break;
     }
     if (parent_dna_oligo!=null){
       setProperty(PARENT_DNA_OLIGO_PROP, parent_dna_oligo);
       setProperty(PARENT_ANTISENSE_OLIGO_PROP, parent_antisense_oligo);
       setProperty(PARENT_SENSE_OLIGO_PROP, parent_sense_oligo);
     }


    	 //oligo_notation=PMEAdaptor.getNucleotideSequenceNotation(parent_antisense_oligo);
    	 oligo_notation=OligoHelper.getRNANotationFromSequence(parent_sense_oligo, parent_antisense_oligo);
    	 if (oligo_notation!=null )
    		 setProperty(RNA_NOTATION_PROP, oligo_notation);

   }


   public String getSingleLetterSeq(int type) {
	   String polymer=null;
     switch (type){
       case TYPE_PARENT_DNA_OLIGO:
    	   polymer=(String)getProperty(PARENT_DNA_OLIGO_PROP);
    	   break;
       case TYPE_PARENT_ANTISENSE_OLIGO:
    	   polymer=(String) getProperty(PARENT_ANTISENSE_OLIGO_PROP);
    	   break;
       case TYPE_PARENT_SENSE_OLIGO:
    	   polymer=(String) getProperty(PARENT_SENSE_OLIGO_PROP);
     }

     if (polymer==null)
    	 return null;
     return polymer;
   }

   /*
   public String getSequence(int type) {
	   String polymer=null;
     switch (type){
       case TYPE_PARENT_DNA_OLIGO:
    	   polymer=(String)getProperty(PARENT_DNA_OLIGO_PROP);
       case TYPE_PARENT_ANTISENSE_OLIGO:
    	   polymer=(String) getProperty(PARENT_ANTISENSE_OLIGO_PROP);
       case TYPE_PARENT_SENSE_OLIGO:
    	   polymer=(String) getProperty(PARENT_SENSE_OLIGO_PROP);

     }

     if (polymer==null)
    	 return null;
     return polymer;
   }*/

   public String getNotation(){
	   String oligo_notation=null;
	   if (getProperty(RNA_NOTATION_PROP)!=null)
		   oligo_notation =getProperty(RNA_NOTATION_PROP).toString();

	   if (oligo_notation==null){
		   String parent_antisense_oligo=(String) getProperty(PARENT_ANTISENSE_OLIGO_PROP);
		   String parent_sense_oligo=(String) getProperty(PARENT_SENSE_OLIGO_PROP);

		   //oligo_notation=PMEAdaptor.getNucleotideSequenceNotation(parent_antisense_oligo); //can we make one with sense/antisense
		   oligo_notation=OligoHelper.getRNANotationFromSequence(parent_sense_oligo, parent_antisense_oligo);

	   }

	   return oligo_notation;
   }

   public void setNotation(String notation){

	   String parent_antisense_oligo="";
	   String registered_antisense_oligo="";
	   String registered_antisense_mod_oligo=null;
	   String parent_sense_oligo="";
	   String registered_sense_oligo="";
	   String registered_sense_mod_oligo=null;
	   String parent_dna_oligo="";
	   //String notation="";
	   StringBuffer buff=new StringBuffer();
	   if (notation.indexOf("$")<0){
		   buff.append("RNA1{");
		   buff.append(notation);
		   buff.append("}$$$$");
		   notation=buff.toString();
	   }
	   try {
		   //seq=PMEAdaptor.getNucleotideSequenceFromNotation(notation);
		   List<RNAPolymerNode> l = ComplexNotationParser.getRNAPolymerNodeList(notation);

		   int size=l.size();
		   if (l==null ||size==0){
			   return;
		   }

           for (int i=0; i<size; i++) {
               RNAPolymerNode node=l.get(i);

               String annotation=node.getAnotation();

               if (annotation!=null && annotation.equals("as")){
            	   registered_antisense_oligo=node.getSequence();//what do we get for modified sequences?
            	   registered_antisense_mod_oligo=OligoHelper.getRNANotationFromRNAPolymerNode(node, "as");

               }
               else if (annotation!=null && annotation.equals("ss")){
            	   registered_sense_oligo=node.getSequence();
            	   registered_sense_mod_oligo=OligoHelper.getRNANotationFromRNAPolymerNode(node, "ss");
               }else if (annotation==null && size==1 ){
            	   //single stranded defaults to antisense --- we should ask people to put in as/ss label whenever it is possible but otherwise a lot of things won't work
            	   registered_antisense_oligo=node.getSequence();
            	   registered_antisense_mod_oligo=OligoHelper.getRNANotationFromRNAPolymerNode(node, "as");
               }else if (annotation==null && size==2){
            	   if (i==0){
            		   //defaults to sense for the first strand
            		   registered_sense_oligo=node.getSequence();
                	   registered_sense_mod_oligo=OligoHelper.getRNANotationFromRNAPolymerNode(node, "ss");
            	   }else if (i==1){
            		 //defaults to antisense for the first strand
            		   registered_antisense_oligo=node.getSequence();//what do we get for modified sequences?
                	   registered_antisense_mod_oligo=OligoHelper.getRNANotationFromRNAPolymerNode(node, "as");
            	   }
               }
           }


	   }catch (Exception ex){
		   System.err.println("unable to convert oligo_notation to single string for "+ notation);
	   }

	   //set the dna_oligo sequence to 19 mer. A hard-coded option......
	   int s_len=0;
	   int as_len=0;
	   if (registered_sense_oligo!=null ){
		   s_len=registered_sense_oligo.length();
	   }
	   if (registered_antisense_oligo!=null ){
		   as_len=registered_antisense_oligo.length();
	   }

	   String design="";
	   if (as_len==21 && s_len==21){
		   parent_dna_oligo=OligoHelper.antisense_rna2dna(registered_antisense_oligo);
		   parent_dna_oligo=parent_dna_oligo.substring(2, 21);

		   if (registered_antisense_oligo.equalsIgnoreCase(OligoHelper.sense_rna2antisense_rna(registered_sense_oligo))){
			   //these two match up, that means it is 21 blunt
			   design="21_blunt";
		   }else{
			   //see if it is 19+2
			   String as_19mer=registered_antisense_oligo.substring(0,19);
			   String s_19mer=registered_sense_oligo.substring(0,19);
			   if (as_19mer.equalsIgnoreCase(OligoHelper.sense_rna2antisense_rna(s_19mer))){
				   design="19p2";
			   }
		   }

	   }else if (as_len==21){
		   parent_dna_oligo=OligoHelper.antisense_rna2dna(registered_antisense_oligo);
		   parent_dna_oligo=parent_dna_oligo.substring(2, 21);
	   }
	   else if (s_len==21){
		   parent_dna_oligo=OligoHelper.sense_rna2dna(registered_sense_oligo);
		   parent_dna_oligo=parent_dna_oligo.substring(2, 21);
	   }
	   else if (as_len==27){ //dicer R
		   parent_dna_oligo=registered_antisense_oligo.substring(6,25);
		   parent_dna_oligo=OligoHelper.antisense_rna2dna(parent_dna_oligo);
		   design="Dicer_R";
	   }else if (s_len==27){ //dicer L
		   parent_dna_oligo=OligoHelper.sense_rna2dna(registered_sense_oligo);
		   parent_dna_oligo=parent_dna_oligo.substring(6,25);
		   design="Dicer_L";
	   }else if (as_len>0 && s_len==0){ //CJL: just antisense
               	parent_dna_oligo=OligoHelper.U2T(registered_antisense_oligo).toUpperCase();
                parent_dna_oligo=OligoHelper.rc(parent_dna_oligo);
	   }

	   if (parent_dna_oligo!=null && parent_dna_oligo.length()!=0){
		   setProperty(PARENT_DNA_OLIGO_PROP, parent_dna_oligo);
		   parent_antisense_oligo=OligoHelper.dna2antisense_rna(parent_dna_oligo);
	       parent_sense_oligo=OligoHelper.dna2sense_rna(parent_dna_oligo);

	       setProperty(PARENT_ANTISENSE_OLIGO_PROP, parent_antisense_oligo);
	       setProperty(PARENT_SENSE_OLIGO_PROP, parent_sense_oligo);
	   }


	   if (registered_antisense_mod_oligo!=null)
		   setProperty(REGISTERED_ANTISENSE_OLIGO_PROP, registered_antisense_mod_oligo);

	   if (registered_sense_mod_oligo!=null)
		   setProperty(REGISTERED_SENSE_OLIGO_PROP, registered_sense_mod_oligo);



	   //oligo_notation=notation; //need to update sequence
	   setProperty(Oligo.RNA_NOTATION_PROP, notation);

	   if (design!=null){
		   setProperty(SIRNA_DESIGN_PROP, design);
	   }

   }

   public Object getDisplayObject() {
      return this;
   }

/*
   public void setTargetSeq(String seq) {
      setProperty(TARGET_PROP, seq.intern());
   }
*/

   public void setTargetName(String target_name) {
      setProperty(TARGET_NAME_PROP, target_name.intern());
      target_id=target_name;
   }

   public String getTargetName() {
      if (target_id == null) {
         return "";
      }
      return target_id;
   }

   public void setStart(int start_pos) {
      setProperty(START_POS_PROP, start_pos + "");

   }

   public void setStart(String start_pos) {

      try {
        setProperty(START_POS_PROP, start_pos);
      }catch (Exception ex){
        //do nothing
      }
   }

/*
   public String getStartAsString() {
      String start = (String) getProperty(START_POS_PROP);
      if (start == null) {
         return "";
      }
      return start;
   }
*/

   public int getStart() {
	   String start = (String) getProperty(START_POS_PROP);
	   int start_pos=-1;
	   try {
		   start_pos=Integer.parseInt(start);
	   }catch (Exception ex){
		   ;
	   }
     return start_pos;
  }


   public void setEnd(int end_pos) {
      setProperty(END_POS_PROP, end_pos + "");

   }

   public void setEnd(String end_pos) {
     try{

       setProperty(END_POS_PROP, end_pos);
     }catch (Exception ex){
       ;//nothing to do
     }
   }

/*
   public String getEnd() {
      String end = (String) getProperty(END_POS_PROP);
      if (end == null) {
         return "";
      }
      return end;
   }
*/
   public int getEnd(){
	   String end = (String) getProperty(END_POS_PROP);
	   int end_pos=-1;
	   try {
		   end_pos=Integer.parseInt(end);
	   }catch (Exception ex){
		   ;
	   }
     return end_pos;
   }

}
