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
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;

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
			throw new SiriusException(exc);
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
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(scenario, new File(filename));
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
