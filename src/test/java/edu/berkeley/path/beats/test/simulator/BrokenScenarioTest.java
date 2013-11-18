package edu.berkeley.path.beats.test.simulator;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioValidationError;
import edu.berkeley.path.beats.simulator.BeatsException;

@RunWith(Parameterized.class)
@Ignore
public class BrokenScenarioTest {
	
	/** configuration file name suffix */
	private static String CONF_SUFFIX = ".xml";

	private static Logger logger = Logger.getLogger(BrokenScenarioTest.class);
	private File config;
	
	private static String[] working_config_names = { "_smalltest",
													"_scenario_constantsplits",
													"_smalltest_multipletypes",
		 											 "_scenario_2009_02_12",
													 "testfwy_w",
													 "test_event"
													 };
	
//	private static String[] broken_config_names = { "_smalltest_nocontrol_broken, " +
//													"Albany-and-Berkeley"};

	/**
	 * Lists working configuration files
	 * @return a Vector of working configuration files
	 */
	public static Vector<Object[]> getWorkingConfigs() {
		Vector<Object[]> res = new Vector<Object[]>();
		for(String name : working_config_names){
			File file = new File("data" + File.separator + "config" + File.separator + name + CONF_SUFFIX);
			if(file.exists())
				res.add(new Object[] {file});
		}
		return res;
	}

//	@Parameters
//	public static Vector<Object[]> getBrokenConfigs() {
//		Vector<Object[]> res = new Vector<Object[]>();
//		for(String name : broken_config_names){
//			File file = new File("data" + File.separator + "config" + File.separator + name + CONF_SUFFIX);
//			if(file.exists())
//				res.add(new Object[] {file});
//		}
//		return res;
//	}

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
		Scenario scenario = ObjectFactory.createAndLoadScenario(config.getPath());
		
		// initialize
		double timestep = Defaults.getTimestepFor(config.getName());
		double starttime = 0;
		double endtime = 300;
		int numEnsemble = 1;
		scenario.initialize(timestep,starttime,endtime,numEnsemble);
		
	}
}
