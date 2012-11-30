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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import edu.berkeley.path.beats.simulator.SiriusErrorLog;
import edu.berkeley.path.beats.simulator.SiriusException;

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
	 * @throws SiriusException
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void createDatabase() throws SQLException, IOException, SiriusException, ClassNotFoundException {
		params = new edu.berkeley.path.beats.db.Parameters();
		params.setDriver("derby");
		params.setDBName(("sirius-" + edu.berkeley.path.beats.util.UUID.generate()).replace('-', '_'));
		System.out.println("Initializing the database");
		edu.berkeley.path.beats.db.Admin.init(params);
		System.out.println("Created a temporary database '" + params.getDBName() + "'");
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
		if (SiriusErrorLog.haserror()) {
			SiriusErrorLog.print();
			SiriusErrorLog.clearErrorMessage();
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
		return edu.berkeley.path.beats.test.simulator.output.XMLOutputWriterTest.conffiles();
	}

	/**
	 * Imports and exports a scenario
	 * @throws SiriusException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	@Test
	public void test() throws SiriusException, IOException, JAXBException, SAXException {
		System.out.println("Importing " + conffile.getPath());
		edu.berkeley.path.beats.om.Scenarios db_scenario = edu.berkeley.path.beats.db.ScenarioImporter.load(conffile.getPath());

		File outfile = File.createTempFile("scenario_", ".xml");
		System.out.println("Exporting scenario " + db_scenario.getId() + " to " + outfile.getPath());
		edu.berkeley.path.beats.db.ScenarioExporter.export(db_scenario.getId(), outfile.getPath());
		outfile.delete();

		clearErrors();
	}

}
