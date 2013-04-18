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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.ui.TextAnchor;

public class BarChartFactory{
	public static Color[] defaultColors=new Color[]{ new Color(153,0,102),  new Color(204,204,255),new Color(0,255,255),Color.white, Color.gray,};
	
	public static JPanel createSimpleStatBarChartPanel(String[] series, double[]data,
			double[] error, String data_name, String title, boolean horizontal, double lower_range, double upper_range, double margin){
		
		return new ChartPanel(createSimpleStatBarChart(series, data, error, data_name, title, horizontal, lower_range, upper_range, margin));
	}
	
	public static JPanel createSimpleStatBarChartPanel(String[] series, double[][]data,
			double[][] error, String[] row_names, String title, 
			boolean horizontal, double lower_range, double upper_range, double margin){
		
		return new ChartPanel(createSimpleStatBarChart(series, data, error, row_names, title, horizontal, lower_range, upper_range, margin));
	}
	/**
	 * Data associated with one cateogry value (e.g. activity data associated with one cmpd)
	 * 
	 * @param series
	 * @param data
	 * @param error
	 * @param data_name
	 * @param title
	 * @param horizontal
	 * @return
	 */
	public static JFreeChart createSimpleStatBarChart(String[] series, double[]data,
			double[] error, String data_name, String title, boolean horizontal, double lower_range, double upper_range, double margin){
		DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		for (int i=0; i<data.length; i++){
			dataset.add(data[i], error[i], series[i], data_name);
		}
		
		// create the chart...
        JFreeChart chart = ChartFactory.createLineChart(
            title, // chart title
            null,                         // domain axis label
            null,                        // range axis label
            dataset,                        // data
            PlotOrientation.HORIZONTAL,       // orientation
            true,                           // include legend
            true,                           // tooltips
            false                           // urls
        );
        

        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);

        // customise the range axis...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(lower_range, upper_range);
        
        //customize the category
        CategoryAxis domain_axis=plot.getDomainAxis();
        domain_axis.setUpperMargin(margin);
        domain_axis.setLowerMargin(margin);
        

        // customise the renderer...
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setErrorIndicatorPaint(Color.black);
        renderer.setIncludeBaseInRange(false);
        renderer.setItemMargin(0.0);
        //renderer.setMaximumBarWidth(0.2);
        plot.setRenderer(renderer);

        renderer.setBaseItemLabelGenerator(
                new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelPaint(Color.black);
     
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        
        renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));

       // renderer.setItemLabelAnchorOffset(7.0);

        // set up gradient paints for series...
        /*if (series.length>5)
        	renderer.setAutoPopulateSeriesFillPaint(true);
        else{
        	for (int i=0;i<series.length;i++){
        		renderer.setSeriesPaint(i,defaultColors[i]);
        	}
        }*/
        renderer.setAutoPopulateSeriesFillPaint(true);
		
		return chart;
	}
	
	
	public static JFreeChart createSimpleStatBarChart(String[] series, double[][]data,
			double[][] error, String[] row_names, String title, 
			boolean horizontal, double lower_range, double upper_range, double margin){
		DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		for (int i=0; i<data.length; i++){
			for (int j=0; j<data[i].length; j++)
				dataset.add(data[i][j], error[i][j],series[j],row_names[i]);
		}
		
		// create the chart...
        JFreeChart chart = ChartFactory.createLineChart(
            title, // chart title
            null,                         // domain axis label
            null,                        // range axis label
            dataset,                        // data
            PlotOrientation.HORIZONTAL,       // orientation
            true,                           // include legend
            true,                           // tooltips
            false                           // urls
        );
        

        chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);

        // customise the range axis...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(lower_range, upper_range);
        
        //customize the category
        CategoryAxis domain_axis=plot.getDomainAxis();
        domain_axis.setUpperMargin(margin);
        domain_axis.setLowerMargin(margin);
        

        // customise the renderer...
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setErrorIndicatorPaint(Color.black);
        renderer.setIncludeBaseInRange(false);
        renderer.setItemMargin(0.0);
        //renderer.setMaximumBarWidth(0.2);
        plot.setRenderer(renderer);

        renderer.setBaseItemLabelGenerator(
                new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelPaint(Color.black);
        
        renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        
        renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.CENTER, TextAnchor.CENTER));

       // renderer.setItemLabelAnchorOffset(7.0);

        // set up gradient paints for series...
        /*
        if (series.length>5)
        	renderer.setAutoPopulateSeriesFillPaint(true);
        else{
        	for (int i=0;i<series.length;i++){
        		renderer.setSeriesPaint(i,defaultColors[i]);
        	}
        }*/
        renderer.setAutoPopulateSeriesFillPaint(true);
		
		return chart;
	}
	
	public static void main(String[] args){
		String[] series=new String[]{"0.1uM", "1uM", "10uM", "100uM"};
		double[][] data=new double[][]{{30.1,60.33333,-10,90},{30.1,60.33333,-10,90}};
		double[][] error=new double[][]{{3.2, 6, 2.2, 2},{3.2, 6, 2.2, 2}};
		JFreeChart chart=BarChartFactory.createSimpleStatBarChart(series, data, error, new String[]{"test1","test2"}, null, true, -10, 100, 0.1);
		JPanel panel=new ChartPanel(chart);
		JFrame f=new JFrame();
		f.add(panel);
		f.pack();
		f.setSize(new Dimension(400, 400));
		f.setVisible(true);
	}
	
	
}
