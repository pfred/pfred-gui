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


public class Target
    extends Oligo {
  public final static String TARGET_SEQ_PROP="target_seq";
  public Target() {
    super();
  }
  public Target(Target t){
    super(t);
  }

  public String getTargetSeq(){
    return this.getSingleLetterSeq(this.TYPE_PARENT_DNA_OLIGO);
  }
  
  public void setSeq(String seq, int type){

	  switch (type){
	  case TYPE_PARENT_DNA_OLIGO:
		  setProperty(PARENT_DNA_OLIGO_PROP, seq);

	  case TYPE_PARENT_ANTISENSE_OLIGO:
		  setProperty(PARENT_ANTISENSE_OLIGO_PROP, seq);
		  break;
	  case TYPE_PARENT_SENSE_OLIGO:
		  setProperty(PARENT_SENSE_OLIGO_PROP, seq);
		  break;
	  }	
  }
}
