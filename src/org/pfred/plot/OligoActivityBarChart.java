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
package org.pfred.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import javax.swing.JPanel;

import org.pfred.model.Oligo;
import com.pfizer.rtc.notation.editor.data.RNAPolymer;
import org.pfred.table.BarChartCustomData;
import com.pfizer.rtc.notation.editor.renderer.RNATableCellRenderer;


public class OligoActivityBarChart extends JPanel {
	BarChartCustomData barchartOptions=null;
	ArrayList<Oligo> oligos=null;
	Font lucidiaFont=new Font("Lucida Console", Font.PLAIN, 15);
	RNATableCellRenderer oligoRenderer=new RNATableCellRenderer();

	public OligoActivityBarChart(ArrayList<Oligo> oligos, BarChartCustomData barchartOptions){
		this.setLayout(new BorderLayout());
		//sequence panel to the left

		//chart panel to the right
		this.barchartOptions=barchartOptions;
		this.oligos=oligos;
		oligoRenderer.setDisplayMode(RNATableCellRenderer.SIMPLE_BLOCK_DISPLAY_MODE);
		createBarChart(oligos, barchartOptions);
		createOligoPanel(oligos);
	}

	private void createOligoPanel(ArrayList<Oligo> oligos){
		OligoPanel oligoPanel=new OligoPanel();

		this.add(oligoPanel, BorderLayout.WEST);
	}

	private void createBarChart(ArrayList<Oligo> oligos, BarChartCustomData customData){
		String[] dataPropNames=customData.getDataPropNames();
		String[] errorPropNames=customData.getErrorPropNames();
		double lower_range=customData.getLowerRange();
		double upper_range=customData.getUpperRange();

		//now get the data for plotting
		//bar chart doesn't take much memory - so for simplicity we just duplicate the data
		int size=oligos.size();
		double[][] values=new double[size][dataPropNames.length];
		double[][] errors=new double[size][errorPropNames.length];
		String[] dataNames=new String[size];
		//double[] values=new double[dataPropNames.length];
		//double[] errors=new double[errorPropNames.length];
		for (int i=0; i<size; i++){
			Oligo oligo=oligos.get(i);
			dataNames[i]=oligo.getName();
			for (int j=0; j<dataPropNames.length; j++){
				try{
					String v=(String)oligo.getProperty(dataPropNames[j]);
					if (v==null) continue;
					values[i][j]=Double.parseDouble(v);
				}catch(Exception ex){
					;//it will be set to zero - meaning ignored
				}
			}

			for (int j=0; j<errorPropNames.length; j++){
				try{
					String v=(String)oligo.getProperty(errorPropNames[j]);
					if (v==null) continue;
					errors[i][j]=Double.parseDouble(v);
				}catch(Exception ex){
					;
				}
			}
		}

		JPanel chartPanel=BarChartFactory.createSimpleStatBarChartPanel(dataPropNames, values,
				errors, dataNames, null, true, lower_range, upper_range, 0);

		this.add(chartPanel, BorderLayout.CENTER);
	}

	public class OligoPanel extends JPanel {
		int top_padding=20;
		int bottom_padding=30;
		int panel_width=250;

		public OligoPanel(){
			this.setBackground(Color.white);
			OligoPanel.this.setPreferredSize(new Dimension(panel_width,300));//just need the width
		}

		public void paint(Graphics g){
			super.paint(g);
			Graphics2D g2DObject=(Graphics2D) g;
			Dimension d=OligoPanel.this.getSize();

			//g2DObject.setFont(lucidiaFont);

			//write the title
			g2DObject.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
			g2DObject.drawString("Oligo Structure", d.width/2, 20);

			//

			int size=oligos.size();
			int unit_height=(d.height-top_padding-bottom_padding)/size;

			for (int i=0; i<size; i++){

				Oligo o=oligos.get(i);
				Object value=o.getProperty(Oligo.RNA_NOTATION_PROP);



				RNAPolymer oligo=null;
				if (value==null)
					continue;
				if (value instanceof String) {
					oligo = new RNAPolymer((String)value);
				}else if (value instanceof RNAPolymer){
					oligo = (RNAPolymer) value;
				}

				//get the oligo length
				int len=getFullLength(oligo);



				Rectangle r=new Rectangle();
				r.width=panel_width;
				r.height=unit_height;
				r.y=top_padding+unit_height*i;

				r.x=d.width-8*len-5; //5 is the padding
				g2DObject.setColor(Color.white);
				oligoRenderer.paint(g2DObject, oligo, r, RNATableCellRenderer.SIMPLE_BLOCK_DISPLAY_MODE);

			/*
				String[]codes=oligo.getSugarCodes(0);
				if (codes==null) return;

				int start_x=d.width-8*codes.length-5; //5 is the padding
				if (start_x<0) start_x=5;
				int block_width=7;
				int block_height=7;
				int start_y=top_padding+unit_height*i+unit_height/2;

				oligoRenderer.drawBlock(g2DObject, codes, RNANotationRenderer.colorMap,
						start_x, start_y, block_width, block_height, true);
						*/
			}

			//draw legend
			int start_x=d.width-2*50;
			if (start_x<0) start_x=5;

			drawLegend(g2DObject, oligoRenderer.getColorMap(), start_x, d.height-30);
		}

		private void drawLegend(Graphics2D g, HashMap<String, Color> colorMap, int start_x, int start_y ){
			Iterator iter=colorMap.keySet().iterator();
			int i=0;
			int itemsPerRow=2;
			while (iter.hasNext()){
				String key=(String)iter.next();
				Color c=colorMap.get(key);
				g.setColor(c);

				if (i%2==0)
					start_y=start_y+15*(i/2);

				g.fillRect(start_x+50*(i%2), start_y, 7, 7);
				g.drawString(key, start_x+50*(i%2)+10, start_y+10);

				i++;
			}
		}

		private int getFullLength(RNAPolymer oligo){
			int numOfStrands=oligo.getNumberOfStrands();
			int len=oligo.getLength();
			if (numOfStrands==2){
				int[][] pairing=oligo.getBasePairing();
				if (pairing==null){
					//it could be that it is not specified in the RNA notation, just quit on it for now
					return len;
				}

				int top_length=oligo.getLength(0);
				int[]top_overhangs=getOverHangLength(pairing,top_length, 0);

				int bottom_length=oligo.getLength(1);
				int[] bottom_overhangs=getOverHangLength(pairing,bottom_length, 1);
				len=Math.max(top_overhangs[0], bottom_overhangs[1])+Math.max(bottom_overhangs[0], top_overhangs[1])+pairing.length;
			}
			return len;
		}

		public int[] getOverHangLength(int[][] basepairing, int length, int strand){
			//find the min and max base paired position
			int min=0;
			int max=0;
			for (int i=0; i<basepairing.length; i++){
				if (min>basepairing[i][strand]) min=basepairing[i][strand];
				if (max<basepairing[i][strand]) max=basepairing[i][strand];
			}
			int[] overhangs=new int[2];
			overhangs[0]=min;
			overhangs[1]=length-max-1;
			return overhangs;
		}
	}

}
