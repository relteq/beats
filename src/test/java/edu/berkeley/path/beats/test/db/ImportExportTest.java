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

package edu.berkeley.path.beats.test.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.util.scenario.ScenarioLoader;
import edu.berkeley.path.beats.util.scenario.ScenarioSaver;

@RunWith(Parameterized.class)
public class ImportExportTest {
	/** scenario file */
	private File conffile;
	/** DB connection parameters */
	private static edu.berkeley.path.beats.db.Parameters params;

	/**
	 * Creates a temporary database and initializes the DB service
	 * @throws SQLException
	 * @throws IOException
	 * @throws BeatsException
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void createDatabase() throws SQLException, IOException, BeatsException, ClassNotFoundException {
		params = new edu.berkeley.path.beats.db.Parameters();
		params.setDriver("derby");
		params.setDBName(("sirius-" + edu.berkeley.path.beats.util.UUID.generate()).replace('-', '_'));
		logger.info("Initializing the database");
		edu.berkeley.path.beats.db.Admin.init(params);

		params.setCreate(false);
		edu.berkeley.path.beats.db.Service.init(params);
		clearErrors();
	}

	/**
	 * Shuts down the DB service and removes the temporary database
	 */
	@AfterClass
	public static void removeDatabase() {
		edu.berkeley.path.beats.db.Service.shutdown();
		edu.berkeley.path.beats.db.Admin.drop(params);
		clearErrors();
	}

	private static void clearErrors() {
		if (BeatsErrorLog.haserror()) {
			BeatsErrorLog.print();
			BeatsErrorLog.clearErrorMessage();
		}
	}

	/**
	 * Initializes the testing environment
	 * @param conffile File the configuration file
	 */
	public ImportExportTest(File conffile) {
		this.conffile = conffile;
	}

	/**
	 * Retrieves a list of scenario files
	 * @return
	 */
	@Parameters
	public static Vector<Object[]> conffiles() {
		return edu.berkeley.path.beats.test.simulator.BrokenScenarioTest.getWorkingConfigs();
	}

	private static Logger logger = Logger.getLogger(ImportExportTest.class);

	/**
	 * Imports and exports a scenario
	 * @throws BeatsException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	@Test
	public void test() throws BeatsException, IOException, JAXBException, SAXException {
		logger.info("Importing " + conffile.getPath());
		Scenario scenario = ScenarioLoader.load(conffile.getPath());
		final Long id = ScenarioSaver.save(scenario);

		File outfile = File.createTempFile("scenario_", ".xml");
		logger.info("Exporting scenario " + id + " to " + outfile.getPath());
		scenario = ScenarioLoader.loadRaw(id);
		ScenarioSaver.save(scenario, outfile.getPath());
		outfile.delete();

		clearErrors();
	}

}
