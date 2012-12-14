/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

/****************************************************************************/
/************        Author: Alexey Goder alexey@goder.com  *****************/
/************                    Dec 12, 2012               *****************/
/****************************************************************************/

package edu.berkeley.path.beats.processor;

import java.awt.Color; 

import javax.swing.JPanel; 

import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis; 
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.DomainOrder; 
import org.jfree.data.general.DatasetChangeListener; 
import org.jfree.data.general.DatasetGroup; 
import org.jfree.data.xy.XYZDataset; 
//import org.jfree.experimental.chart.renderer.GrayPaintScale; 
//import org.jfree.experimental.chart.renderer.PaintScale; 
//import org.jfree.experimental.chart.renderer.xy.XYBlockRenderer; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities; 

import com.itextpdf.text.Font;

/** 
 * TODO: The chart needs a display showing the value scale. 
 */ 
public class ContourChart extends ApplicationFrame { 

	
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
String title;  
   private double[] x= new double[10000];
   private double[] y= new double[10000];
   private double[] z= new double[10000];
   
   private int itemCount;
   private int currentItem;
   private double min;
   private double max;
   private int stepNumber;
   
    public ContourChart(String t) { 
        super(t); 
        
        title = t;
        itemCount = -1;
        currentItem = 0;
        min = 0.0;
        max = 1.0;
		stepNumber = 50;

    } 

    public void setItemCount (int count) { itemCount = count; }
    public void setCurrentItem (int count) { currentItem = count; }
    public void setMin (double m) { min = m; }
    public void setMax (double m) { max = m; }
    
    public void setXYZ(double xx, double yy, double zz) {
    	if (currentItem++ >= 10000 ) return;
    	
    	if ( currentItem > itemCount ) itemCount = currentItem;
    	x[currentItem]=xx;
    	y[currentItem]=yy;
    	z[currentItem]=zz;
    	return;
    }
 
    public JFreeChart createFreeChart(XYZDataset dataset, String title,
            String yAxisNaming, double dMinZ, double dMaxZ, ReportRequest rr) {
    	
        NumberAxis xAxis = new NumberAxis("Links");
         xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         xAxis.setLowerMargin(0.0);
         xAxis.setUpperMargin(0.0);
         NumberAxis yAxis = new NumberAxis(yAxisNaming);
         yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         yAxis.setLowerMargin(0.0);
         yAxis.setUpperMargin(0.0);
         yAxis.setAutoRangeIncludesZero(false);
         XYBlockRenderer renderer = new XYBlockRenderer();
         
         LookupPaintScale colorScale = new LookupPaintScale(Math.floor(min), Math.ceil(max), new Color(107, 63, 160)); 		
         double increment = (max - min)/stepNumber;
         for (int i = 0; i < stepNumber; i++) {
 			double value = (i * increment ) / (max - min);
 			float[] rgb = getRainbowColor(value);
 				colorScale.add(min + i * increment, new Color((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255)));

 		 }
         
         PaintScale scale = new GrayPaintScale(Math.floor(min),  Math.ceil(max)); 
         if ( rr.getColor() ) 
        	 renderer.setPaintScale(colorScale);
         else
        	 renderer.setPaintScale(scale);

         // paint legend

         NumberAxis numberaxis2 = new NumberAxis("scale");
         numberaxis2.setAxisLinePaint(Color.white);
         numberaxis2.setTickMarkPaint(Color.white);
         
         XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
         plot.setBackgroundPaint(Color.lightGray);
         plot.setDomainGridlinesVisible(true);
         plot.setRangeGridlinePaint(Color.white);
         JFreeChart chart = new JFreeChart(title, plot);
         chart.removeLegend();

         PaintScaleLegend paintscalelegend;
         if ( rr.getColor() )
        	 paintscalelegend= new PaintScaleLegend(colorScale, numberaxis2);
         else
        	 paintscalelegend= new PaintScaleLegend(scale, numberaxis2);
         
         paintscalelegend.setSubdivisionCount(20);
         paintscalelegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
         paintscalelegend.setAxisOffset(5D);
         paintscalelegend.setMargin(new RectangleInsets(5D, 5D, 5D, 5D));
         
         if ( rr.getColor() ) 
        	 paintscalelegend.setFrame(new BlockBorder(Color.red));
         
         paintscalelegend.setPadding(new RectangleInsets(10D, 10D, 10D, 10D));
         paintscalelegend.setStripWidth(10D);
         paintscalelegend.setPosition(RectangleEdge.RIGHT);
         chart.addSubtitle(paintscalelegend);

         
         return chart;
    }
    /** 
     * Creates a chart. 
     *  
     * @param dataset  the dataset. 
     *  
     * @return A chart. 
     */ 
    public JFreeChart createChart(XYZDataset dataset) { 
        NumberAxis xAxis = new NumberAxis("Link ID"); 
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 
        xAxis.setLowerMargin(0.0); 
        xAxis.setUpperMargin(0.0); 
        NumberAxis yAxis = new NumberAxis("Time"); 
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 
        yAxis.setLowerMargin(0.0); 
        yAxis.setUpperMargin(0.0); 
        XYBlockRenderer renderer = new XYBlockRenderer(); 
        //PaintScale scale = new GrayPaintScale(-2.0, 1.0);
        PaintScale scale = new GrayPaintScale(min, max); 
        	
        yAxis.setAutoRangeIncludesZero(false);
        
		double increment = (max - min)/stepNumber;
		
        LookupPaintScale colorScale = new LookupPaintScale(-1, 1, new Color(107, 63, 160));
		for (int i = 0; i < stepNumber; i++) {
			double value = (i * increment ) / (max - min);
			float[] rgb = getRainbowColor(value);
			//try {
				colorScale.add(min + i * increment, new Color((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255)));
			//} catch (Exception e) {
			//	System.out.println(rgb[0] + " " + rgb[1] + " " + rgb[2] + " " + value);
			//	e.printStackTrace();
			//}

		}
				
        renderer.setPaintScale(colorScale); 
        
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer); 
        plot.setBackgroundPaint(Color.lightGray); 
        plot.setDomainGridlinesVisible(false); 
        plot.setRangeGridlinePaint(Color.white); 
        
        
        JFreeChart chart = new JFreeChart(null, plot); 
        chart.removeLegend(); 
        chart.setBackgroundPaint(Color.white); 
        return chart; 
    } 
     
    /** 
     * Creates a sample dataset. 
     */ 
    public XYZDataset createDataset() { 
        return new XYZDataset() { 
            public int getSeriesCount() { 
                return 1; 
            } 
            public int getItemCount(int series) { 
                return 10000; 
            } 
            public Number getX(int series, int item) { 
                return new Double(getXValue(series, item)); 
            } 
            public double getXValue(int series, int item) { 
                return x[item]; 
            } 
            public Number getY(int series, int item) { 
                return new Double(getYValue(series, item)); 
            } 
            public double getYValue(int series, int item) { 
                return y[item]; 
            } 
            public Number getZ(int series, int item) { 
                return new Double(getZValue(series, item)); 
            } 
            public double getZValue(int series, int item) { 
            	return z[item];
            } 
            public void addChangeListener(DatasetChangeListener listener) { 
                // ignore - this dataset never changes 
            } 
            public void removeChangeListener(DatasetChangeListener listener) { 
                // ignore 
            } 
            public DatasetGroup getGroup() { 
                return null; 
            } 
            public void setGroup(DatasetGroup group) { 
                // ignore 
            } 
            public Comparable getSeriesKey(int series) { 
                return title; 
            } 
            public int indexOf(Comparable seriesKey) { 
                return 0; 
            } 
            public DomainOrder getDomainOrder() { 
                return DomainOrder.ASCENDING; 
            }         
        }; 
    } 
     
    public static float[] getRainbowColor(double value){
    	
		double red, green, blue;
		
		if (value <= 0){
			
			red = green = blue = 0;
			
		}else{
			
//			if (value <= 1.0 / 12){
////				red = value;
//				red = 0;
//				green = 0;
//				blue = value * 8 + 1.0 / 6;
//			}else 
			
			if (value <= 1.0 / 6){
//				red = value;
				red = 0;
				green = 0;
				blue = value * 5 + 1.0 / 6;
//				blue = value * 2 + 5.0 / 6;
				
			}else if (value <= 2.0 / 6){
				
//				red = 0;
				red = (value - 1.0 / 6) * 4;
//				green = value * 6 - 1;
//				green = value * 3 - 0.5;
				green = 0;
				blue = 1;
				
			}else if (value <= 3.0 / 6){
				
//				red = value * 3 - 1;
//				red = (value - 1.0 / 6) * 2;
//				red = 2 - value * 4;
				red = value + 1.0 / 3;
//				green = 3 - value * 6;
//				green = value * 3 - 0.5;
//				green = value * 6 - 2;
//				green = value * 2 - 2.0 / 3;
				green = 0;
//				blue = 2 - value * 3;
				blue = 2 - value * 3;
				
			}else if (value <= 4.0 / 6){
				
//				red = value * 3 - 1;
//				red = (value - 1.0 / 6) * 2;
//				red = value * 6 - 3;
//				red = value * 2 - 1.0 / 3;
				red = value + 1.0 / 3;
				green = 0;
//				green = 4 - value * 6;
//				green = 4.0 / 3 - value * 2;
				blue = 2 - value * 3;
//				blue = 4 - value * 6;
				
			}else if (value <= 5.0 / 6){
				
				red = 1;
//				red = value;
				green = value * 6 - 4;
				blue = 0;
				
			}else if (value <= 1){
				
				red = 1;
//				red = value;
				green = 1;
				blue = value * 6 - 5;
				
			}else {
				
				red = green = blue = 1;
			}
		}
		
		return new  float[] { (float)red, (float) green ,(float) blue};
	}
    
}