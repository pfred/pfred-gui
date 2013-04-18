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

package org.pfred.subset;


import java.util.ArrayList;
import java.util.Arrays;

import org.pfred.model.Oligo;
import com.pfizer.rtc.task.ProgressReporter;

public class LeaderSubsetting {
  public LeaderSubsetting() {
  }

  public static ArrayList subset(ArrayList oligos, String propName, boolean asc, int topN, int min_distance,  ProgressReporter pd) throws Exception{
    ArrayList selected=new ArrayList();
    if (oligos==null) return selected;
    Object[] tobesorted=oligos.toArray();
    int total=oligos.size();
    pd.workComplete("Running....",0,total);

    if (propName!=null){
      Arrays.sort(tobesorted, new OligoComparator(propName, asc));
    }

    ArrayList inputs=new ArrayList();

    for (int i=0; i<tobesorted.length; i++){
      inputs.add(tobesorted[i]);
    }
    int size=inputs.size();
    for (int i=0; i<size; i++){
      Oligo oligo_i=(Oligo)inputs.get(i);
      int oligo_i_start=oligo_i.getStart();
      int oligo_i_end=oligo_i.getEnd();
      int selected_size=selected.size();
      boolean rejected=false;

      for (int j=0; j<selected_size; j++){
        Oligo oligo_j=(Oligo) selected.get(j);
        int oligo_j_start=oligo_j.getStart();
        int oligo_j_end=oligo_j.getEnd();

        int distance=Math.abs(oligo_i_start-oligo_j_start);
        /*if (oligo_j_end>=oligo_i_start && oligo_i_end>=oligo_j_start){
          distance=0;
        }else if (oligo_j_end<oligo_i_start){
          distance=oligo_i_start-oligo_j_end;
        }else if (oligo_i_end<oligo_j_start){
          distance=oligo_j_start-oligo_i_end;
        }*/

        if (distance<min_distance){
          rejected=true; break;
        }

      }
      if (!rejected)
        selected.add(oligo_i);
      pd.workComplete("Running....", i, total);
      if (selected.size()>=topN){
        break;
      }
    }

    return selected;
  }



}
