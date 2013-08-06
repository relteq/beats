package edu.berkeley.path.beats.util;

import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.util.scenario.ScenarioLoader;


@SuppressWarnings("restriction")
public class ScenarioUtil {

	private static Logger logger = Logger.getLogger(ScenarioUtil.class);

	/**
	 * Loads an XML schema as a resource
	 * @param resourceName the resource path
	 * @return the schema
	 * @throws BeatsException
	 */
	private static javax.xml.validation.Schema getSchema(String resourceName) throws BeatsException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(ScenarioUtil.class.getClassLoader().getResource(resourceName));
		} catch (org.xml.sax.SAXException exc) {
			throw new BeatsException("Failed to load a schema '" + resourceName + "'", exc);
		}
	}

	/**
	 * Loads the scenario XML schema
	 * @return the schema
	 * @throws BeatsException
	 */
	public static javax.xml.validation.Schema getSchema() throws BeatsException {
		return getSchema("beats.xsd");
	}

	/**
	 * Loads the XML output schema
	 * @return the output schema
	 * @throws BeatsException
	 */
	public static javax.xml.validation.Schema getOutputSchema() throws BeatsException {
		return getSchema("sirius_output.xsd");
	}

	private static String getSchemaVersion(String resourceName) throws BeatsException {
		XMLStreamReader xmlsr;
		try {
			xmlsr = XMLInputFactory.newInstance().createXMLStreamReader(ScenarioUtil.class.getClassLoader().getResourceAsStream(resourceName));
		} catch (XMLStreamException exc) {
			throw new BeatsException(exc);
		} catch (FactoryConfigurationError exc) {
			throw new BeatsException(exc);
		}
		try {
			while (xmlsr.hasNext()) {
				if (XMLStreamConstants.START_ELEMENT == xmlsr.getEventType()) {
					javax.xml.namespace.QName qname = xmlsr.getName();
					if ("schema".equals(qname.getLocalPart()) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(qname.getNamespaceURI()))
						return xmlsr.getAttributeValue(null, "version");
				}
				xmlsr.next();
			}
		} catch (XMLStreamException exc) {
			throw new BeatsException(exc);
		} finally {
			try {
				xmlsr.close();
			} catch (XMLStreamException exc) {
				logger.error("Error closing XML stream for resource '" + resourceName + "'", exc);
			}
		}
		return null;
	}

	/**
	 * @return the input (scenario) XML schema version
	 * @throws BeatsException
	 */
	public static String getSchemaVersion() throws BeatsException {
		return getSchemaVersion("beats.xsd");
	}

	/**
	 * @return the output XML schema version
	 * @throws BeatsException
	 */
	public static String getOutputSchemaVersion() throws BeatsException {
		return getSchemaVersion("sirius_output.xsd");
	}

	/**
	 * Reports an error if the scenario schemaVersion attribute value
	 * differs from the input schema version
	 * @param scenario the scenario to check
	 */
	public static void checkSchemaVersion(edu.berkeley.path.beats.jaxb.Scenario scenario) {
		String schema_version = null;
		try {
			schema_version = getSchemaVersion();
		} catch (BeatsException exc) {
			logger.error("Failed to retrieve a schema version");
			return;
		}
		if (null == schema_version)
			logger.warn("Schema version is NULL");
		else if (null == scenario.getSchemaVersion())
			logger.warn("Scenario schema version is NULL");
		else if (!scenario.getSchemaVersion().equals(schema_version))
			logger.warn("Scenario schema version " + scenario.getSchemaVersion() + " is incorrect. Should be: " + schema_version);
	}

	/**
	 * Restores a scenario from the database
	 * @param id the scenario id
	 * @return the restored scenario
	 * @throws BeatsException
	 */
	public static edu.berkeley.path.beats.simulator.Scenario getScenario(long id) throws BeatsException {
		return ScenarioLoader.load(id);
	}

	/**
	 * runs a scenario simulation
	 * @param scenario
	 * @param startTime simulation start time, sec
	 * @param endTime simulation end time, sec
	 * @param outDt output frequency
	 * @throws BeatsException
	 */
	public static void runScenario(edu.berkeley.path.beats.simulator.Scenario scenario, double startTime, double endTime, double outDt) throws BeatsException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		scenario.run(startTime,endTime,outDt,"db",null,1);
	}

	/**
	 * runs a scenario simulation once
	 * @param id the scenario id
	 * @param startTime simulation start time, sec
	 * @param endTime simulation end time, sec
	 * @param outDt output frequency
	 * @throws BeatsException
	 */
	public static void runScenario(long id, double startTime, double endTime, double outDt) throws BeatsException {
		runScenario(getScenario(id), startTime, endTime, outDt);
	}

}
