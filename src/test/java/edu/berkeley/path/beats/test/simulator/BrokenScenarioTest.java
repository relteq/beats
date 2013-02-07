package edu.berkeley.path.beats.test.simulator;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.ScenarioValidationError;
import edu.berkeley.path.beats.simulator.BeatsException;

@RunWith(Parameterized.class)
public class BrokenScenarioTest {
	/** configuration file name suffix */
	private static String CONF_SUFFIX = ".xml";

	/**
	 * Lists all configuration files in data/config/
	 * @return a Vector of configuration files
	 */
	private static Vector<File> getConfigFiles() {
		File [] files = new File("data" + File.separator + "config").listFiles();
		Vector<File> conf_l = new Vector<File>(files.length);
		for (File file : files)
			if (file.getName().endsWith(CONF_SUFFIX))
				conf_l.add(file);
		return conf_l;
	}

	private static String[] broken_config_names = {"_smalltest", "complete_bad", "scenario_twotypes"};

	/**
	 * Determines if the configuration file is working
	 * @param file the configuration file
	 * @return false, if the scenario is broken; true, if it is OK
	 */
	private static boolean isWorkingConfig(File file) {
		for (String name : broken_config_names)
			if (file.getName().equals(name + CONF_SUFFIX)) return false;
		return true;
	}

	/**
	 * Lists working configuration files
	 * @return a Vector of working configuration files
	 */
	public static Vector<Object[]> getWorkingConfigs() {
		Vector<File> conffiles = getConfigFiles();
		Vector<Object[]> res = new Vector<Object[]>();
		for (File file : conffiles)
			if (isWorkingConfig(file)) res.add(new Object[] {file});
		return res;
	}

	/**
	 * Lists broken configuration files
	 * @return a Vector of broken configuration files
	 */
	@Parameters
	public static Vector<Object[]> getBrokenConfigs() {
		Vector<File> conffiles = getConfigFiles();
		Vector<Object[]> res = new Vector<Object[]>();
		for (File file : conffiles)
			if (!isWorkingConfig(file)) res.add(new Object[] {file});
		return res;
	}

	private static Logger logger = Logger.getLogger(BrokenScenarioTest.class);
	private File config;

	/**
	 * Prepares a testing environment
	 * @param config the configuration file
	 */
	public BrokenScenarioTest(File config) {
		this.config = config;
	}

	/**
	 * Checks if the configuration file validation fails
	 * @throws BeatsException
	 */
	@Test(expected=ScenarioValidationError.class)
	public void ensureValidationError() throws BeatsException {
		logger.info("CONFIG: " + config.getPath());
		ObjectFactory.createAndLoadScenario(config.getPath());
	}
}
