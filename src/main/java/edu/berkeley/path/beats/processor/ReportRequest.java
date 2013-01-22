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
/************                    Dec 10, 2012               *****************/
/****************************************************************************/

package edu.berkeley.path.beats.processor;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.lowagie.text.pdf.AcroFields.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.model.*;

public class ReportRequest {
	
	private List<Content> contents;
	
	private long start_time;
	private long duration;
	private Boolean color;
	private String aggregation;
	private Boolean detailed;
	private Boolean linkPerformance;
	private Boolean linkData;
	private Boolean onRampPerformance;
	private Boolean onRampData;
	private Boolean routePerformance;
	private Boolean routeData;
	private Boolean networkPerformance;
	private String units;

	int chartID=0;
	private double travelTimeMultiplier=1.0;
	private double linkLength=1.0;
	private SlideShow ppt;
	private String title;
	private String keys;
	
	public String	getKeys() { return keys; }
	public void 	setKeys(String k) { keys = k; }
	public void 	addKeys(String k) { 		
		 if ( keys == null ) {			
			keys = k;
			return;
		} else {			
			keys += (";   " + k); 
			return;
		}
	}
	
	public String 	getTitle() { return title ; }
	public void 	setTitle(String t) { title = t; }
	public SlideShow getPpt() { return ppt; }
	public void 	setPpt(SlideShow p) { ppt = p; }
	public void 	setLinkLength(double l) { linkLength=l; }
	public double 	getLinkLength() { return linkLength; }
	public double 	getMultiplier() { return travelTimeMultiplier; }
	public void 	setMultiplier(double m) { travelTimeMultiplier = m; }
	
	public Long 	getStartTimeInMilliseconds() { return start_time*1000; }
	public Long 	getDurationInMilliseconds() { return duration*1000; }
	public Long 	getStartTime() { return start_time; }
	public Long 	getDuration() { return duration; }
	public Boolean 	getColor() { return color; }
	public Boolean 	getDetailed() { return detailed; }
	public Boolean 	getLinkPerformance() { return linkPerformance; }
	public Boolean 	getLinkData() { return linkData; }
	public Boolean 	getOnRampPerformance() { return onRampPerformance; }
	public Boolean 	getOnRampData() { return onRampData; }
	public Boolean 	getRouteData() { return routeData; }
	public Boolean 	getRoutePerformance() { return routePerformance; }
	public Boolean	getNetworkPerformance() { return networkPerformance; }
	
	public String 	getAggregation() { return aggregation; }
	public String 	getUnits() { return units; }
	
	public List<Content> getContent() { return contents; }
	
	public int 		getChartId() { return chartID; }
	public void 	setChartId(int i) { chartID = i; }
	public void 	incrementChartId() { chartID++; }
	
	/**
	 * Retrieve list of scenarios and runs from the xml request file
	 * @param configFile
	 * @return List of Content
	 */
	public List<Content> readContentFromFile(String configFile) {

//http://www.vogella.com/articles/JavaXML/article.html
		
	    List<Content> contentList = new ArrayList<Content>();
	    
	    try {
		      // First create a new XMLInputFactory
		      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		      // Setup a new eventReader
		      InputStream in = new FileInputStream(configFile);
		      XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		      
		      // Read the XML document
		      
		      Content  item = null;

		      while (eventReader.hasNext()) {
		    	  XMLEvent event = eventReader.nextEvent();

		    	  if (event.isStartElement()) {
		    		  	StartElement startElement = event.asStartElement();
		    		  	// If we have a item element we create a new item
		    		  	if (startElement.getName().getLocalPart() == "scenario") {
		    		  		
		    		  			item = new Content();
		    		  			
		    		  			// We read the attributes from this tag and add the date
		    		  			// attribute to our object
		    		  			@SuppressWarnings("unchecked")
		    		  			Iterator<Attribute> attributes = startElement.getAttributes();
		    		  			while (attributes.hasNext()) {
		    		  					Attribute attribute = attributes.next();
		    		  						if (attribute.getName().toString().equals("id")) {
		    		  							
		    		  							item.setScenarioId(attribute.getValue());
		    		  							
		    		  							AggregateData.reportToStandard("Scenario iD: " + item.getScenarioId());
		    		  							
		    		  						}

		    		  			}
		    		  	}
		    

	          if (event.isStartElement()) {
	            if (event.asStartElement().getName().getLocalPart()
	                .equals("run_numbers")) {
	              event = eventReader.nextEvent();
	              
	              item.setRuns(event.asCharacters().getData());
	              
	              AggregateData.reportToStandard("run_numbers: " + item.getRuns());
	              
	              contentList.add(item);
	              continue;
	            }
	          }
	          
	          if (event.isStartElement()) {
		            if (event.asStartElement().getName().getLocalPart()
		                .equals("units")) {
		              event = eventReader.nextEvent();
		              
		              units = (event.asCharacters().getData());
		              
		              AggregateData.reportToStandard("Units: " + units);
		              
		              contentList.add(item);
		              continue;
		            }
		          }
	          
	        }

	      }

	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (XMLStreamException e) {
	      e.printStackTrace();
	    }
	    return contentList;
	  }

	/**
	 * Read report parameters from the xml request file
	 * @param fileName
	 */
	public void readXMLFile(String fileName) {
		try {
			 
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			start_time = 	getLongAttributeValue	(doc,"time_range", "start_time");
			duration = 		getLongAttributeValue	(doc,"time_range", "duration");
			
			aggregation = 	getStringAttributeValue	(doc, "display", "aggregation");
			//units = 		getStringTagValue		(doc, "sirius_report", "units");
			color = 		getBolleanAttributeValue(doc, "display", "color");
			detailed = 		getBolleanAttributeValue(doc, "display", "detailed");
			linkData = 		getBolleanAttributeValue(doc, "links", "data");
			
			onRampPerformance = getBolleanAttributeValue(doc, "onramps", "performance");
			onRampData = 		getBolleanAttributeValue(doc, "onramps", "data");
						
			routePerformance = 	getBolleanAttributeValue(doc, "routes", "performance");
			routeData = 		getBolleanAttributeValue(doc,"routes", "data");
			
			linkPerformance = 	getBolleanAttributeValue(doc, "links", "performance");
			linkData = 			getBolleanAttributeValue(doc,"links", "data");	
			networkPerformance= getBolleanAttributeValue(doc, "networks", "performance");
			contents = 			readContentFromFile(fileName);

		} catch (Exception e) {
			e.printStackTrace();
		  }
		 
	}
	
	public static String getStringAttributeValue(Document doc, String tagName, String name) {
			
		NodeList nList = doc.getElementsByTagName(tagName);
				 
		Node nNode = nList.item(0);
		try {
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) nNode;
				AggregateData.reportToStandard("String " + name + "=" + eElement.getAttribute(name));
				return (String)eElement.getAttribute(name);
		
			}	else
				
				return "";		
		} catch (Exception e) {
			return "";
		  }
	}
	
	public static long getLongAttributeValue(Document doc, String tagName, String name) {
		
		long l=0;
		
		NodeList nList = doc.getElementsByTagName(tagName);
				 
		Node nNode = nList.item(0);
		try {
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) nNode;
				String temp = (String)eElement.getAttribute(name);
				
				try {
					l = Long.parseLong(temp);
					AggregateData.reportToStandard(name + "=" + l);
				}
				catch (NumberFormatException nfe)
			    {
			      return 0;
			    }
		
			}	
		} catch (Exception e) {
			return 0;
		  }
			
			return l;					
	}
	
	public static Boolean getBolleanAttributeValue(Document doc, String tagName, String name) {
		
		NodeList nList = doc.getElementsByTagName(tagName);
				 
		Node nNode = nList.item(0);
		try {
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) nNode;
				String s = eElement.getAttribute(name);
				// AggregateData.reportToStandard(tagName + " " + name + " " + ( eElement.getAttribute(name).indexOf("true") > 0 ) );
				AggregateData.reportToStandard(tagName + " " + name + "=" + s );
				if ( s.indexOf("true") >= 0 )   return true;
				else return false;
		
			}	else
				
				return false;
		} catch (Exception e) {
			return false;
		  }
	}


	public static String getStringTagValue(Document doc, String tagName, String name) {
		
		NodeList nList = doc.getElementsByTagName(tagName);
				 
		Node nNode = nList.item(0);
		try {
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) nNode;
				
				AggregateData.reportToStandard(name + "=" + eElement.getAttribute(name));
				
				return (String)getTagValue(name, eElement);
		
			}	else
				
				return "";	
		} catch (Exception e) {
			return "";
		  }
	}
	
	private static String getTagValue(String sTag, Element eElement) {
			NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		 
		        Node nValue = (Node) nlList.item(0);
		 
			return nValue.getNodeValue();
	}
	


}