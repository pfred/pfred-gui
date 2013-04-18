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

import java.util.Comparator;
import java.text.DecimalFormat;

public final class NUMERIC_TEXT_COMPARATOR implements Comparator {
  public DecimalFormat formater = new DecimalFormat();

	public NUMERIC_TEXT_COMPARATOR() {

	}

	public int compare(Object o1, Object o2) {

		//Vidhya - added - start
		String obj1Str = o1.toString();
		String obj2Str = o2.toString();
		/*
		System.out.println("NUMERIC_TEXT_COMPARATOR inside compare: o1: "
				+ obj1Str + "o2: " + obj2Str);
        */
		if (isLessThanSignExist(obj1Str)&& isGreaterThanSignExist(obj2Str)) {
			//System.out.println("<obj1Str and >obj2Str and return -1");
			return -1;
		}

		if (isGreaterThanSignExist(obj1Str)&& isLessThanSignExist(obj2Str)) {
			//System.out.println(">obj1Str and <obj2Str and return 1");
			return 1;
		}

		if (isLessThanSignExist(obj1Str)&& isLessThanSignExist(obj2Str)) {
			//System.out.println("<obj1Str and <obj2Str");
			obj1Str = dataAfterSign(obj1Str,"<");
			obj2Str = dataAfterSign(obj2Str,"<");
			return compare2Values(obj1Str,obj2Str);
		}

		if (isLessThanSignExist(obj1Str)&& (!isLessThanSignExist(obj2Str))) {
			//System.out.println("<obj1Str and obj2Str and return -1");
			return -1;
		}

		if ((!isLessThanSignExist(obj1Str))&& isLessThanSignExist(obj2Str)) {
			//System.out.println("obj1Str and <obj2Str and return 1");
			return 1;
		}

		if (isGreaterThanSignExist(obj1Str)&& isGreaterThanSignExist(obj2Str)) {
			//System.out.println(">obj1Str and >obj2Str");
			obj1Str = dataAfterSign(obj1Str,">");
			obj2Str = dataAfterSign(obj2Str,">");

			return compare2Values(obj1Str,obj2Str);
		}

		if (isGreaterThanSignExist(obj1Str)&& (!isGreaterThanSignExist(obj2Str))) {
			//System.out.println(">obj1Str and obj2Str and return 1");
			return 1;
		}

		if ((!isGreaterThanSignExist(obj1Str))&& isGreaterThanSignExist(obj2Str)) {
			//System.out.println("obj1Str and >obj2Str and return -1");
			return -1;
		}

		if (isPercentageExists(obj1Str)) {
			obj1Str = dataBeforePerc(obj1Str);
			//System.out.println("obj1Str after removing percentage: " + obj1Str);
		}

		if (isPercentageExists(obj2Str)) {
			obj2Str = dataBeforePerc(obj2Str);
			//System.out.println("obj2Str after removing percentage: " + obj2Str);
		}

		o1 = obj1Str;
		o2 = obj2Str;

		//System.out.println("NUMERIC_TEXT_COMPARATOR inside compare: obj1Str: "
		//		+ obj1Str + "obj2Str: " + obj2Str);

		//Vidhya - added - end

		return compare2Values(o1,o2);

	}



	//Vidhya - added - start

	private boolean isLessThanSignExist(String objStr) {
		int perPos = objStr.indexOf("<");
		return (perPos == 0);
	}

	private boolean isGreaterThanSignExist(String objStr) {
		int perPos = objStr.indexOf(">");
		return (perPos == 0);
	}

	private boolean isPercentageExists(String objStr) {
		int perPos = objStr.indexOf("%");
		return (perPos > 0);

	}

	private String dataBeforePerc(String objStr) {
		int perPos = objStr.indexOf("%");
		String data = null;
		if (perPos > 0) {
			data = objStr.substring(0, perPos);
		}
		return data;
	}

	private String dataAfterSign(String objStr, String sign)
	{
		//System.out.println("dataAfterSign........");
		int signPos = objStr.indexOf(sign);
		//System.out.println("signPos"+signPos);
		String data = null;
		if (signPos == 0)
		{
			//System.out.println("Inside signPos");
			data = objStr.substring(1,objStr.length());
			//System.out.println("data"+data);
		}
		return data;
	}

	//Vidhya - added - end
   private int compare2Values(Object o1, Object o2) {

		float i1 = 0;
		float i2 = 0;
                boolean i1NaN=false;
                boolean i2NaN=false;
		try {
			if (!(o2 instanceof Float))
				i2 = getNumber((String)o2);
			else
				i2 = ((Float) o2).floatValue();
		} catch (Exception e) {
			//System.out.println("exception:" + e.getMessage() + " return 1");
			i1NaN=true;
		}

		try {
			if (!(o1 instanceof Float))
				i1 = getNumber((String)o1);
			else
				i1 = ((Float) o1).floatValue();
		} catch (Exception e) {
			i2NaN=true;
		}
                if (i2NaN &&i1NaN){
                  return 0; //don't move
                }
                if (i1NaN) return -1;
                if (i2NaN) return 1;

		if (i1 == i2)
			return 0;

		if (i1 < i2)
			return -1;

		return 1;
	}

        private float getNumber(String text) throws Exception {
          text = text.trim();
          String[] fields = text.split("\\s");
          String value = fields[0]; //only look at the first value

          return formater.parse(value).floatValue();

        }


}
