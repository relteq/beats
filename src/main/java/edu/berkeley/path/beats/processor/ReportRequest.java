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

public class ReportRequest {
	
	private List<Content> contents;
	
	private long start_time;
	private long duration;
	private Boolean color;
	String aggregation;
	Boolean detailed;
	Boolean linkPerformance;
	Boolean linkData;
	Boolean onRampPerformance;
	Boolean onRampData;
	Boolean routePerformance;
	Boolean routeData;
	Boolean networkPerformance;
	String units;

	int chartID=0;
	
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
			//units = 		getStringTagValue		(doc, "data_sources", "units");
			color = 		getBolleanAttributeValue(doc, "display", "color");
			detailed = 		getBolleanAttributeValue(doc, "display", "detailed");
			linkData = 		getBolleanAttributeValue(doc, "links", "data");
			
			onRampPerformance = getBolleanAttributeValue(doc, "onramps", "performance");
			onRampData = 		getBolleanAttributeValue(doc, "onramps", "data");
						
			routePerformance = 	getBolleanAttributeValue(doc, "routes", "performance");
			routeData = 		getBolleanAttributeValue(doc,"routes", "data");
			
			linkPerformance = 	getBolleanAttributeValue(doc, "links", "performance");
			linkData = 			getBolleanAttributeValue(doc,"links", "data");	
			
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
				AggregateData.reportToStandard("String " + name + " " + eElement.getAttribute(name));
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
					AggregateData.reportToStandard(name + " " + l);
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
				
				AggregateData.reportToStandard(tagName + " " + name + " " + ( eElement.getAttribute(name).indexOf("true") > 0 ) );
				
				if ( eElement.getAttribute(name).indexOf("true") > 0 )   return true;
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
				
				AggregateData.reportToStandard(name + " " + eElement.getAttribute(name));
				
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