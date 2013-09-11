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
 
package edu.berkeley.path.beats.test.simulator.output;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;

@RunWith(Parameterized.class)
public class XMLOutputWriterTest {
	/** output file name prefix */
	private static String OUT_PREFIX = "output_";
	/** output file name suffix */
	private static String OUT_SUFFIX = "_0.xml";
	/** configuration file name suffix */
	private static String CONF_SUFFIX = ".xml";

	/** configuration (scenario) file */
	private File conffile;
	/** configuration (scenario) schema */
	private static Schema ischema;
	/** simulator output schema */
	private static Schema oschema;

	/**
	 * Loads scenario and simulator output schemas
	 * @throws SAXException
	 */
	@BeforeClass
	public static void loadSchemas() throws SAXException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		ClassLoader classLoader = XMLOutputWriterTest.class.getClassLoader();
		ischema = factory.newSchema(classLoader.getResource("beats.xsd"));
		oschema = factory.newSchema(classLoader.getResource("sirius_output.xsd"));
	}

	/**
	 * Initializes testing environment
	 * @param conffile File the configuration file
	 */
	public XMLOutputWriterTest(File conffile) {
		this.conffile = conffile;
	}

	/**
	 * Lists configuration files
	 * @return a Vector of configuration files <code>data/config/*.xml</code>
	 */
	@Parameters
	public static Vector<Object[]> conffiles() {
		return edu.berkeley.path.beats.test.simulator.BrokenScenarioTest.getWorkingConfigs();
	}

	private static Logger logger = Logger.getLogger(XMLOutputWriterTest.class);

	/**
	 * Validates the configuration file, runs a simulation, validates the output.
	 * @throws IOException
	 * @throws SAXException
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws BeatsException
	 */
	@Test
	public void testOutputWriter() throws IOException, SAXException, XMLStreamException, FactoryConfigurationError, BeatsException {
		logger.info("CONFIG: " + conffile.getPath());
		validate(conffile, ischema);
		String confname = conffile.getName();
		logger.info("Config " + confname + " validated");

		String out_prefix = OUT_PREFIX + confname.substring(0, confname.length() - CONF_SUFFIX.length()) + "_";
		File outfile = File.createTempFile(out_prefix, OUT_SUFFIX);
		
		
		double dt = Defaults.getTimestepFor(confname);
		runBeats(conffile.getPath(), outfile.getAbsolutePath(),dt);
		logger.info("Simulation completed");

		validate(outfile, oschema);
		logger.info("Output validated");

		outfile.delete();
		logger.debug(outfile.getAbsolutePath() + " removed");
	}

	/**
	 * Validates an XML file
	 * @param xmlfile a File to be validated
	 * @param schema a Schema to validate against
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * */
	protected static void validate(File xmlfile, Schema schema) throws XMLStreamException, FactoryConfigurationError, SAXException, IOException {
		Validator validator = schema.newValidator();
		XMLStreamReader xmlsr = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(xmlfile));
		validator.validate(new StAXSource(xmlsr));
	}

	/**
	 * Runs a simulation
	 * @param confpath String a configuration file path
	 * @param outpath String an output file path
	 * @throws BeatsException
	 */
	private void runBeats(String confpath, String outpath,double simdt) throws BeatsException {
		// output writer properties
		if (!outpath.endsWith(OUT_SUFFIX)) fail("Incorrect output file path: " + outpath);
		String outprefix = outpath.substring(0, outpath.length() - OUT_SUFFIX.length());
		String outtype = "xml";

		// load the scenario
		Scenario scenario = ObjectFactory.createAndLoadScenario(confpath);
		if (null == scenario) fail("The scenario was not loaded");

		// simulation settings
		double timestart = 0d;
		if (null != scenario.getInitialDensitySet())
			timestart = scenario.getInitialDensitySet().getTstamp();
		double duration = 3600d;
		double outDt = 600d;
		int numReps = 1;
		int numEnsemble = 1;
		
		// initialize
		scenario.initialize(simdt,timestart,timestart+duration,outDt,outtype,outprefix,numReps,numEnsemble);
		
		// run the scenario
		logger.info("Running a simulation");
		scenario.run();

		if (BeatsErrorLog.haserror()) {
			BeatsErrorLog.print();
			BeatsErrorLog.clearErrorMessage();
		}
	}

}
