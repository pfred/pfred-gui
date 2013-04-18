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

import com.pfizer.rtc.util.OrderedProperties;
import java.util.ArrayList;

import org.pfred.OligoHelper;

public class Transcript {
  private String name=null;
  public OrderedProperties p=new OrderedProperties();
  public final static String DNA_OLIGO_PROP = "dna_oligo";
  public final static String ANTISENSE_OLIGO_PROP = "antisense_oligo";
  public final static String SENSE_OLIGO_PROP = "sense_oligo";
  public final static String SPECIES_PROP = "species";
  public final static String LENGTH_PROP = "length";
  public final static String SOURCE_PROP = "source";
  public final static String PRIMARY_TRANSCRIPT_PROP = "PrimaryTarget";
  public final static String SECONDARY_TRANSCRIPT_PROP = "SecondaryTarget";
  public final static String TRANSCRIPT_SRC_USER_SPECIFIED="User_Specified";


  public Transcript(){
  }
  public Transcript(String name) {
    this.name=name;
    p.setProperty("name", name);
  }

  public void setName(String name){
    this.name=name;
    p.setProperty("name", name);
  }

  public String getName(){
    if (name==null)
      name=(String) p.getProperty("name");
    return name;
  }

  public void setAsPrimaryTranscript(boolean set){
    if (!set){
      p.setProperty(PRIMARY_TRANSCRIPT_PROP, "false");
    }else
      p.setProperty(PRIMARY_TRANSCRIPT_PROP, "true");
  }

  public String getIsPrimaryTranscript(){
    return (String) p.getProperty(PRIMARY_TRANSCRIPT_PROP);
  }

  public void setAsSecondaryTranscript(boolean set){
    if (!set){
      p.setProperty(SECONDARY_TRANSCRIPT_PROP, "false");
    }else
      p.setProperty(SECONDARY_TRANSCRIPT_PROP, "true");
  }

  public String getIsSecondaryTranscript(){
     return (String) p.getProperty(SECONDARY_TRANSCRIPT_PROP);
  }

  public String getSequence(){
    return (String) p.getProperty(DNA_OLIGO_PROP);
  }

  public void setSequence(String seq){
    String dna_seq=OligoHelper.U2T(seq);
    p.setProperty(DNA_OLIGO_PROP, dna_seq);
    p.setProperty(LENGTH_PROP, ""+seq.length());
  }

  public String getSpecies() {
    return (String) p.getProperty(SPECIES_PROP);
  }

  public void setSpecies(String species) {
    p.setProperty(SPECIES_PROP, species);
  }

  public String getSource(){
    return (String)p.getProperty(this.SOURCE_PROP);
  }

  public void setSource(String src){
    p.setProperty(this.SOURCE_PROP, src);
  }

  public boolean isUserSpecified(){
    String src=(String)p.getProperty(this.SOURCE_PROP);
    if (src!=null && src.equals(this.TRANSCRIPT_SRC_USER_SPECIFIED))
      return true;
    return false;
  }


  public void setProperty(String name, Object value) {
     if (value == null) {
        return;
     }
     if (value instanceof String && ((String) value).length() < 40) {
        setPropertyIntern(name.intern(), value.toString().intern(), false);//to reuse strings
     } else {
        setPropertyIntern(name.intern(), value, false); //to reuse strings
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

}
