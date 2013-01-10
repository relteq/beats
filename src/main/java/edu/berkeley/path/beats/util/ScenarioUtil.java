package edu.berkeley.path.beats.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

import edu.berkeley.path.beats.simulator.SimulationSettings;
import edu.berkeley.path.beats.simulator.SiriusException;


@SuppressWarnings("restriction")
public class ScenarioUtil {

	private static Logger logger = Logger.getLogger(ScenarioUtil.class);

	/**
	 * Loads an XML schema as a resource
	 * @param resourceName the resource path
	 * @return the schema
	 * @throws SiriusException
	 */
	private static javax.xml.validation.Schema getSchema(String resourceName) throws SiriusException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(ScenarioUtil.class.getClassLoader().getResource(resourceName));
		} catch (org.xml.sax.SAXException exc) {
			throw new SiriusException("Failed to load a schema '" + resourceName + "'", exc);
		}
	}

	/**
	 * Loads the scenario XML schema
	 * @return the schema
	 * @throws SiriusException
	 */
	public static javax.xml.validation.Schema getSchema() throws SiriusException {
		return getSchema("sirius.xsd");
	}

	/**
	 * Loads the XML output schema
	 * @return the output schema
	 * @throws SiriusException
	 */
	public static javax.xml.validation.Schema getOutputSchema() throws SiriusException {
		return getSchema("sirius_output.xsd");
	}

	private static String getSchemaVersion(String resourceName) throws SiriusException {
		XMLStreamReader xmlsr;
		try {
			xmlsr = XMLInputFactory.newInstance().createXMLStreamReader(ScenarioUtil.class.getClassLoader().getResourceAsStream(resourceName));
		} catch (XMLStreamException exc) {
			throw new SiriusException(exc);
		} catch (FactoryConfigurationError exc) {
			throw new SiriusException(exc);
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
			throw new SiriusException(exc);
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
	 * @throws SiriusException
	 */
	public static String getSchemaVersion() throws SiriusException {
		return getSchemaVersion("sirius.xsd");
	}

	/**
	 * @return the output XML schema version
	 * @throws SiriusException
	 */
	public static String getOutputSchemaVersion() throws SiriusException {
		return getSchemaVersion("sirius_output.xsd");
	}

	/**
	 * Loads a scenario from an XML file
	 * @param filename input file name
	 * @return the scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.jaxb.Scenario load(String filename) throws SiriusException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(edu.berkeley.path.beats.jaxb.ObjectFactory.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			javax.xml.validation.Schema schema = getSchema();
			unmarshaller.setSchema(schema);
			edu.berkeley.path.beats.simulator.ObjectFactory.setObjectFactory(unmarshaller, new edu.berkeley.path.beats.jaxb.ObjectFactory());
			edu.berkeley.path.beats.jaxb.Scenario scenario = (edu.berkeley.path.beats.jaxb.Scenario) unmarshaller.unmarshal(new FileInputStream(filename));
			checkSchemaVersion(scenario);
			return scenario;
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		} catch (FileNotFoundException exc) {
			throw new SiriusException(exc);
		}
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
		} catch (SiriusException exc) {
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
	 * Sets the scenario schema version if it has not been set yet
	 * @param scenario
	 * @throws SiriusException
	 */
	private static void ensureSchemaVersion(edu.berkeley.path.beats.jaxb.Scenario scenario) throws SiriusException {
		if (null == scenario.getSchemaVersion()) {
			String schemaVersion = getSchemaVersion();
			logger.debug("Schema version was not set. Assuming current version: " + schemaVersion);
			scenario.setSchemaVersion(schemaVersion);
		}
	}

	/**
	 * Saves a scenario to an XML file
	 * @param scenario the scenario
	 * @param filename output file name
	 * @throws SiriusException
	 */
	public static void save(edu.berkeley.path.beats.jaxb.Scenario scenario, String filename) throws SiriusException {
		ensureSchemaVersion(scenario);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(edu.berkeley.path.beats.jaxb.ObjectFactory.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setSchema(getSchema());
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(scenario, new File(filename));
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		}
	}

	/**
	 * Saves a scenario as a JSON file
	 * @param scenario the scenario
	 * @param filename the output file name
	 * @throws SiriusException
	 */
	public static void saveJSON(edu.berkeley.path.beats.jaxb.Scenario scenario, String filename) throws SiriusException {
		try {
			saveJSON(scenario, new java.io.FileWriter(filename));
		} catch (java.io.IOException exc) {
			throw new SiriusException("Could not open output file " + filename, exc);
		}
	}

	/**
	 * Serializes a scenario in JSON format to an output stream
	 * @param scenario
	 * @param stream the output stream
	 * @throws SiriusException
	 */
	public static void saveJSON(edu.berkeley.path.beats.jaxb.Scenario scenario, java.io.OutputStream stream) throws SiriusException {
		saveJSON(scenario, new java.io.OutputStreamWriter(stream));
	}

	/**
	 * Serializes a scenario in JSON format
	 * @param scenario
	 * @param writer
	 * @throws SiriusException
	 */
	public static void saveJSON(edu.berkeley.path.beats.jaxb.Scenario scenario, java.io.Writer writer) throws SiriusException {
		ensureSchemaVersion(scenario);
		try {
			MappedNamespaceConvention nsConvention = new MappedNamespaceConvention(new org.codehaus.jettison.mapped.Configuration());
			XMLStreamWriter xmlsw = new MappedXMLStreamWriter(nsConvention, writer);
			JAXBContext jaxbContext = JAXBContext.newInstance(edu.berkeley.path.beats.jaxb.ObjectFactory.class);
			jaxbContext.createMarshaller().marshal(scenario, xmlsw);
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		}
	}

	/**
	 * Restores a scenario from the database
	 * @param id the scenario id
	 * @return the restored scenario
	 * @throws SiriusException
	 */
	public static edu.berkeley.path.beats.simulator.Scenario getScenario(long id) throws SiriusException {
		return edu.berkeley.path.beats.db.ScenarioExporter.getScenario(id);
	}

	/**
	 * runs a scenario simulation
	 * @param scenario
	 * @param startTime simulation start time, sec
	 * @param endTime simulation end time, sec
	 * @param outDt output frequency
	 * @throws SiriusException
	 */
	public static void runScenario(edu.berkeley.path.beats.simulator.Scenario scenario, double startTime, double endTime, double outDt) throws SiriusException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		SimulationSettings simsettings = new SimulationSettings(startTime, endTime - startTime, outDt, 1);
		Properties owr_props = new Properties();
		owr_props.setProperty("type", "db");
		scenario.run(simsettings, owr_props);
	}

	/**
	 * runs a scenario simulation once
	 * @param id the scenario id
	 * @param startTime simulation start time, sec
	 * @param endTime simulation end time, sec
	 * @param outDt output frequency
	 * @throws SiriusException
	 */
	public static void runScenario(long id, double startTime, double endTime, double outDt) throws SiriusException {
		runScenario(getScenario(id), startTime, endTime, outDt);
	}

}
