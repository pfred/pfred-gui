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
package org.pfred.sort;

public class SelSortCompPropDTO {

	 private String opName = null;
	 private String propName = null;
	 private boolean numeric = false;
	 private boolean wasErrorSorting = false;


	public SelSortCompPropDTO(){
	}
/*
	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}*/

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public boolean isWasErrorSorting() {
		return wasErrorSorting;
	}

	public void setWasErrorSorting(boolean wasErrorSorting) {
		this.wasErrorSorting = wasErrorSorting;
	}

	public String toString() {

		String line_separator = System.getProperty("line.separator");

		StringBuffer buff = new StringBuffer();
		buff.append("Property Name: "+propName);
		buff.append(line_separator);
		buff.append("Operator Name: "+opName);
		buff.append(line_separator);
		buff.append("isNumeric: "+numeric);
		buff.append(line_separator);

		return buff.toString();
	}
}
