package edu.berkeley.path.beats.processor;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;


public class ReportRequest {
	
	public long start_time;
	public long duration;
	Boolean color;
	String aggregation;
	Boolean detailed;
	Boolean linkPerformance;
	Boolean linkData;
	Boolean onrampPerformance;
	Boolean onrampData;
	Boolean routePerformance;
	Boolean routeData;
	Boolean networkPerformance;
	String units;
	

	public void readXMLFile(String fileName) {
		try {
			 
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			AggregateData.reportToStandard("Root element : " + doc.getDocumentElement().getNodeName());
			
			start_time = 	getLongAttributeValue	(doc,"time_range", "start_time");
			duration = 		getLongAttributeValue	(doc,"time_range", "duration");
			
			aggregation = 	getStringAttributeValue	(doc, "display", "aggregation");
			units = 		getStringTagValue		(doc, "data_sources", "units");
			color = 		getBolleanAttributeValue(doc, "display", "color");
			detailed = 		getBolleanAttributeValue(doc, "display", "detailed");
			linkData = 		getBolleanAttributeValue(doc, "links", "data");
			
			onrampPerformance = getBolleanAttributeValue(doc, "onramps", "performance");
			onrampData = 		getBolleanAttributeValue(doc, "onramps", "data");
						
			routePerformance = 	getBolleanAttributeValue(doc, "routes", "performance");
			routeData = 		getBolleanAttributeValue(doc,"routes", "data");
			
			linkPerformance = 	getBolleanAttributeValue(doc, "links", "performance");
			linkData = 			getBolleanAttributeValue(doc,"links", "data");		
			
			NodeList nList = doc.getElementsByTagName("time_range");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				   Node nNode = nList.item(temp);
				   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
				      Element eElement = (Element) nNode;
		 
				      AggregateData.reportToStandard("Start Time : " + eElement.getAttribute("start_time"));
				      AggregateData.reportToStandard("Duration : " + eElement.getAttribute("duration"));
		 //  System.out.println("Last Name : " + getTagValue("lastname", eElement));
				   }
				}
			
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
					AggregateData.reportToStandard("Long " + name + " " + l);
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