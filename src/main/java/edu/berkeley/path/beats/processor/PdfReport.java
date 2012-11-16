package edu.berkeley.path.beats.processor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.itextpdf.awt.geom.Rectangle;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class PdfReport {

	private static Font headFont = new Font(Font.FontFamily.TIMES_ROMAN, 24,
			Font.BOLD);
	private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
			Font.BOLD);
	private static Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 10,
			Font.NORMAL);

	private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 9,
			Font.NORMAL);
	private static Font smallFont = new Font(Font.FontFamily.HELVETICA, 9,
			Font.NORMAL);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
			Font.BOLD);
	/**
     * Inner class to add a table as header.
     */
    class TableHeader extends PdfPageEventHelper {
        /** The header text. */
        String header;
        /** The template with the total number of pages. */
        PdfTemplate total;
 
        /**
         * Allows us to change the content of the header.
         * @param header The new header String
         */
        public void setHeader(String header) {
            this.header = header;
        }
 
        /**
         * Creates the PdfTemplate that will hold the total number of pages.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }
 
        /**
         * Adds a header to every page
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onEndPage(PdfWriter writer, Document document) {
        	
        	if (writer.getPageNumber() < 2 ) return;
        	
            PdfPTable table = new PdfPTable(3);
            try {
                table.setWidths(new int[]{24, 24, 2});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                //table.getDefaultCell().setFixedHeight(12);
                
                table.getDefaultCell().setBorder(0);
                table.addCell(header);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                table.addCell(new Phrase("Page "+writer.getPageNumber()+" of",smallFont));
                //table.addCell(String.format("Page %d of", writer.getPageNumber()));
                PdfPCell cell = new PdfPCell(Image.getInstance(total));
                
                cell.setBorder(0);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 48, 824, writer.getDirectContent());
            }
            catch(DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
 
        /**
         * Fills out the total number of pages before the document is closed.
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
         *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1)),
                    2, 2, 0);
        }
    }
 
    
    
	public void outputPdf(String table) {
		
		ReportRequest rr = new  ReportRequest();
		rr.readXMLFile("report_request.xml");
		
		try {
			Document document = new Document();
			//PdfWriter.getInstance(document, new FileOutputStream(table+".pdf"));
			
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(table+".pdf"));
	        
			
			TableHeader event = new TableHeader();
	        writer.setPageEvent(event);
	        
			document.open();
			addMetaData(document);
			addTitlePage(document, table);
			addContent(document, table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// iText allows to add metadata to the PDF which can be viewed in your Adobe
	// Reader
	// under File -> Properties
	private static void addMetaData(Document document) {
		document.addTitle("INTEGRATED CORRIDOR MANAGEMENT PROJECT");
		document.addSubject("Java Generated");
		document.addCreator("Alexey Goder");
	}
	
	/**
	 * return center aligned paragraph
	 * @param s
	 * @param f
	 * @return
	 */
	public static Paragraph addCenter(String s, Font f) {
		Paragraph p = new Paragraph(s,f);
		p.setAlignment(Element.ALIGN_CENTER);
		return p;
	}
	
	/**
	 * return left aligned paragraph
	 * @param s
	 * @param f
	 * @return
	 */
	public static Paragraph addLeft(String s, Font f) {
		Paragraph p = new Paragraph(s,f);
		p.setAlignment(Element.ALIGN_LEFT);
		return p;
	}

	private static void addTitlePage(Document document, String table) throws DocumentException {
		
		Paragraph preface = new Paragraph();
		
		// We add one empty line
		addEmptyLine(preface, 1);
		
		// Lets write a big header
		preface.add(addCenter("UC Berkeley", headFont));
		addEmptyLine(preface, 8);

		preface.add(addCenter("INTEGRATED CORRIDOR MANAGEMENT PROJECT", catFont));

		addEmptyLine(preface, 1);
		
		//Date
		
		java.util.Date today = new java.util.Date();	
		preface.add(addCenter("Report generated: " + new java.sql.Timestamp(today.getTime()), smallBold));
		
		addEmptyLine(preface, 3);
		
		preface.add(addCenter("Tables used for this report: " + table, smallBold));

		addEmptyLine(preface, 8);

		document.add(preface);
		
		// Start a new page
		document.newPage();
	}

	private static void addContent(Document document, String table) throws DocumentException, TorqueException, DataSetException, IOException {
		
		
		AggregateData.reportToStandard("Report for table: " +table);

		
		// Get a list of column names
		java.util.List listOfColumnNames =  BasePeer.executeQuery("SELECT COLUMNNAME FROM SYS.SYSCOLUMNS WHERE REFERENCEID IN (SELECT TABLEID FROM SYS.SYSTABLES WHERE TABLENAME=\'" + table.toUpperCase()  + "\' ) ORDER BY COLUMNNUMBER ASC");
		AggregateData.reportToStandard("SELECT COLUMNNAME FROM SYS.SYSCOLUMNS WHERE REFERENCEID IN (SELECT TABLEID FROM SYS.SYSTABLES WHERE TABLENAME=\'" + table.toUpperCase()  + "\' ) ORDER BY COLUMNNUMBER ASC");
		
		// Get a list of keys 	
			
		java.util.List listOfKeys = BasePeer.executeQuery("select distinct " + AggregateData.getListOfKeys(table) + " from "  + table + " where aggregation=\'15min\'");
		AggregateData.reportToStandard("Unique key combinations: " + listOfKeys.size());

		for (int i=0; i < listOfKeys.size(); i++ ) {
			
			Paragraph contentPage = new Paragraph();
			
			contentPage.add(addLeft("TABLE: " + table.toUpperCase(), subFont));
			
			String query = AggregateData.setKeys("select COUNT(ts) from " + table +" WHERE aggregation=\'15min'", table, (Record)listOfKeys.get(i) );
	
			
			if (  (  (Record)(BasePeer.executeQuery(query).get(0))  ).getValue(1).asInt()  > 0 ) {
			
				contentPage.add( addLeft(formatKeys( AggregateData.setKeys("", table, (Record)listOfKeys.get(i)) ), subFont ) );
				contentPage.add( addLeft(" aggregation=15min", subFont ) );

				addEmptyLine(contentPage, 1);
				
				query =  AggregateData.setKeys("select * from " + table + " WHERE aggregation=\'15min\'", table, (Record)listOfKeys.get(i) );				
				
				java.util.List data = BasePeer.executeQuery(query);
				//AggregateData.reportToStandard("Size " + data.size() );
				
				// Add generated chart and table
				contentPage.add(createTable(listOfColumnNames, data));

			} 
						
			// Now add all this to the document
			document.add(contentPage);
			document.newPage();
		}

		
	}
	
	/**
	 * returns column number from the list
	 * @param names
	 * @param name
	 * @return
	 * @throws DataSetException
	 */
	public static int getColumnNumber(java.util.List names, String name) throws DataSetException {
		
		for (int i=0; i<names.size(); i++) {
			if ( name.equals(  ((Record)names.get(i)).getValue(1).asString() ) ) {
				return i;
			}
		}
		
		return 0;
	}

	/**
	 * remove ANS=D from the key string
	 * @param s
	 * @return formatted string
	 */
	public static String formatKeys(String s) {
		

		if ( s.indexOf("WHERE") >= 0 ) {
			
			s = s.replace("WHERE", "");
		}

		if ( s.indexOf("AND") >= 0 ) {
			
			return s.replace("AND", "\n");
		}
			
		return s;
	}
	
	private static Paragraph createTable(java.util.List listOfColumnNames, java.util.List data)
			throws DataSetException, DocumentException, IOException {

		XYSeriesCollection chartData = new XYSeriesCollection();
		java.util.List<XYSeries> curves = new ArrayList<XYSeries>();
		Paragraph section = new Paragraph();
		
		int numberOfColumns = 6;
		PdfPTable table = new PdfPTable(numberOfColumns);
		
		table.setTotalWidth(new float[] {80f,48f,48f, 48f,48f,48f});

		
		// t.setBorderColor(BaseColor.GRAY);
		// t.setPadding(4);
		// t.setSpacing(4);
		// t.setBorderWidth(1);


		table.addCell(new PdfPCell(new Phrase("Time Stamp",subFont)));

		for (int i=0; i<numberOfColumns-1; i++ ) {
			
			String name = new String(((Record)listOfColumnNames.get(i+5)).getValue(1).asString().toLowerCase());
			table.addCell(new PdfPCell(new Phrase(name,subFont)));
			curves.add(i,new XYSeries(name) );
		}

		table.setHeaderRows(1);

		// Get first time stamp in milliseconds
		long startOfTheChart = ((Record)data.get(0)).getValue(getColumnNumber(listOfColumnNames,"TS")+1).asTimestamp().getTime();
		
		for (int row=0; row< data.size(); row++) {
			
			table.addCell(new PdfPCell(new Phrase(((Record)data.get(row)).getValue(getColumnNumber(listOfColumnNames,"TS")+1).asString().toLowerCase(),subFont)));
			long t = ((Record)data.get(row)).getValue(getColumnNumber(listOfColumnNames,"TS")+1).asTimestamp().getTime();
			
			for (int i=0; i<numberOfColumns-1; i++ ) {
				
				BigDecimal d = ((Record)data.get(row)).getValue(i+6).asBigDecimal();
				
				if ( d == null ) {
					
					table.addCell(new PdfPCell(new Phrase(" ",subFont)));
					
				} else {	
					
					// Add to the chart 
					curves.get(i).add(((double)(t - startOfTheChart))/1000.0/60.0/60.0, d.doubleValue());
					
					// Add to the table
					d=d.setScale(8,BigDecimal.ROUND_HALF_UP);
					table.addCell(new PdfPCell(new Phrase(d.toString(),subFont)));

				}
						
			}	
		}
		
		for (int i=0; i<numberOfColumns-1; i++ ) {
			
			chartData.addSeries(curves.get(i));
		}
		
		section.add(addCenter("CHART", subFont));
		addEmptyLine(section, 1);
		section.add(createChart(chartData));
		addEmptyLine(section, 1);
		section.add(addCenter("DATA", subFont));
		addEmptyLine(section, 1);
		section.add(table);
		addEmptyLine(section, 1);
		return section;

	}


	/**
	 * create chart
	 * @param section
	 * @param chartData
	 * @throws IOException 
	 * @throws BadElementException 
	 */
	private static Paragraph createChart(XYSeriesCollection chartData) throws IOException, BadElementException {
		
		Paragraph section = new Paragraph();
		
		// Generate the chart
		JFreeChart chart = ChartFactory.createXYLineChart(
		null, // Title
		"HOURS", // x-axis Label
		" ", // y-axis Label
		chartData, // Data
		PlotOrientation.VERTICAL, // Plot Orientation
		true, // Show Legend
		true, // Use tooltips
		false // Configure chart to generate URLs?
		);
	       
	        chart.getXYPlot().setBackgroundPaint(Color.WHITE); // Change background to white
	        chart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY); // Change grid line color
	        chart.getXYPlot().setOutlineStroke(new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 50, new float[] { 1f, 2f }, 0));
	        chart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);	        
	        chart.getXYPlot().getDomainAxis().setLabelFont( chart.getXYPlot().getDomainAxis().getLabelFont().deriveFont(new Float(12f)) );
	        chart.getXYPlot().getRenderer().setSeriesOutlineStroke(1,  new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 50, new float[] { 1f, 2f }, 0));
	        chart.getXYPlot().getRangeAxis().setLabelFont( chart.getXYPlot().getRangeAxis().getLabelFont().deriveFont(new Float(14f)) );
	        chart.getXYPlot().getLegendItems().get(1).setLabelFont( chart.getXYPlot().getRangeAxis().getLabelFont().deriveFont(new Float(14f)) );
	                
	        chart.getXYPlot().getRenderer().setBaseOutlineStroke(new BasicStroke(4f));
	        chart.getXYPlot().getRenderer().setBaseStroke(new BasicStroke(4f));
	        chart.getXYPlot().getRenderer().setSeriesOutlineStroke(1, new BasicStroke(4f));
	        chart.getXYPlot().setWeight(1);
	        
	        //AggregateData.reportToStandard("Weight: " +  chart.getXYPlot().getWeight() );
	       

		java.io.File chartFile = new File("chart.jpg");
		
		org.jfree.chart.ChartUtilities.saveChartAsJPEG(chartFile, 1.0f, chart, 900, 600);
		
		Image chartImage = Image.getInstance("chart.jpg");
		
		chartImage.setAlignment(Element.ALIGN_CENTER);
		
		chartImage.scalePercent(60.0f);
		
		section.add(chartImage);
		
		return section;
		
        
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
}
