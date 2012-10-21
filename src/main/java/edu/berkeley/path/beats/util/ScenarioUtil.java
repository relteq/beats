package edu.berkeley.path.beats.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.simulator.SiriusException;


public class ScenarioUtil {

	private static Logger logger = Logger.getLogger(ScenarioUtil.class);

	/**
	 * Loads the XML schema as a resource
	 * @return the schema
	 * @throws SiriusException
	 */
	public static javax.xml.validation.Schema getSchema() throws SiriusException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			return factory.newSchema(ScenarioUtil.class.getClassLoader().getResource("sirius.xsd"));
		} catch (org.xml.sax.SAXException exc) {
			throw new SiriusException(exc);
		}
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
			return (edu.berkeley.path.beats.jaxb.Scenario) unmarshaller.unmarshal(new FileInputStream(filename));
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		} catch (FileNotFoundException exc) {
			throw new SiriusException(exc);
		}
	}

	/**
	 * Saves a scenario to an XML file
	 * @param scenario the scenario
	 * @param filename output file name
	 * @throws SiriusException
	 */
	public static void save(edu.berkeley.path.beats.jaxb.Scenario scenario, String filename) throws SiriusException {
		if (null == scenario.getSchemaVersion()) {
			String schemaVersion = edu.berkeley.path.beats.Version.get().getSchemaVersion();
			logger.debug("Schema version was not set. Assuming current version: " + schemaVersion);
			scenario.setSchemaVersion(schemaVersion);
		}
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(edu.berkeley.path.beats.jaxb.ObjectFactory.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setSchema(getSchema());
			marshaller.marshal(scenario, new File(filename));
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		}
	}

}
