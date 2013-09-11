package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Defaults;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;

public class ScenarioTest {

	private static Scenario static_scenario;
	private static String config_folder = "data/config/";
//	private static String quarantine_folder = "data/config.quarantine/";
	private static String output_folder = "data/test/output/";
	private static String fixture_folder = "data/test/fixture/";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			String config_file = "_smalltest.xml";
			static_scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
			if(static_scenario==null)
				fail("scenario did not load");

			// initialize
			double timestep = Defaults.getTimestepFor(config_file);
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 10;
			static_scenario.initialize(timestep,starttime,endtime,numEnsemble);
			static_scenario.reset();
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

	@Test
	public void test_initialize_run_advanceNSeconds() {
		try {
			String config_file = "_smalltest.xml";
			Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
			if(scenario==null)
				fail("scenario did not load");

			// initialize
			double timestep = Defaults.getTimestepFor(config_file);
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 10;
			scenario.initialize(timestep,starttime,endtime,numEnsemble);
			scenario.reset();

			assertEquals(scenario.getCurrentTimeInSeconds(),300d,1e-4);
			assertEquals(scenario.getNumEnsemble(),10,1e-4);
			
			scenario.advanceNSeconds(300d);
			assertEquals(scenario.getCurrentTimeInSeconds(),600d,1e-4);
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

//	@Test
//	public void test_saveToXML() {
//		try {
//			String test_file = "test_saveXML.xml";
//			String config_file = "_smalltest_nocontrol.xml";
//			Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
//			if(scenario==null)
//				fail("scenario did not load");
//			
//			scenario.saveToXML(output_folder+test_file);
//			
//			File f1 = new File(output_folder+test_file);
//			File f2 = new File(fixture_folder+test_file);
//			assertTrue("The files differ!", FileUtils.contentEquals(f1, f2));
//			
//		} catch (BeatsException e) {
//			fail("initialization failure.");
//		} catch (IOException e) {
//			fail("IOException.");
//		}
//	}

	@Test
	public void test_time_getters() {
		assertEquals(static_scenario.getCurrentTimeInSeconds(),300d,1e-4);
		assertEquals(static_scenario.getTimeElapsedInSeconds(),0d,1e-4);
		assertEquals(static_scenario.getCurrentTimeStep(),0,1e-4);
		assertEquals(static_scenario.getTotalTimeStepsToSimulate(),-1,1e-4);
	}

	@Test
	public void test_getNumVehicleTypes() {
		assertEquals(static_scenario.getNumVehicleTypes(),1,1e-4);
	}

	@Test
	public void test_getNumEnsemble() {
		assertEquals(static_scenario.getNumEnsemble(),10,1e-4);
	}

	@Test
	public void test_getVehicleTypeIndex() {
		assertEquals(static_scenario.getVehicleTypeIndexForName("car"),0);
		assertEquals(static_scenario.getVehicleTypeIndexForName("xxx"),-1);
		
		// edge case
		assertEquals(static_scenario.getVehicleTypeIndexForName(null),-1);
	}
	
	@Test
	public void test_getSimDtInSeconds() {
		assertEquals(static_scenario.getSimdtinseconds(),5,1e-4);
	}

	@Test
	public void test_getTimeStart() {
		assertEquals(static_scenario.getTimeStart(),300d,1e-4);
	}

	@Test
	public void test_getTimeEnd() {
		assertTrue(Double.isInfinite(static_scenario.getTimeEnd()));
	}

	@Test
	public void test_getConfigFilename() {
		assertEquals(static_scenario.getConfigFilename(),config_folder+"_smalltest.xml");
	}

	@Test
	public void test_getVehicleTypeNames() {
		String [] names = static_scenario.getVehicleTypeNames();
		assertEquals(names[0],"car");
	}

//	@Test
//	public void test_getInitialDensityForNetwork() {
//		double x =  static_scenario.getInitialDensityForNetwork(-1)[0][0];
//		double exp = 0.0;
//		assertEquals(x,exp,1e-4);
//		
//		// edge cases
//		assertNull(static_scenario.getInitialDensityForNetwork(-100000));
//		//x =  static_scenario.getInitialDensityForNetwork(null)[0][0];	// null works for single networks
//		//assertEquals(x,exp,1e-4);
//	}

	@Test
	public void test_getDensityForNetwork() {
		double x = static_scenario.getDensityForNetwork(-1,0)[0][0];
		double exp =0.4445728212287675;
		assertEquals(x,exp,1e-4);

		//x = static_scenario.getDensityForNetwork(null,0)[0][0];	// null works for single networks
		//assertEquals(x,exp,1e-4);
		
		// edge cases
		assertNull(static_scenario.getDensityForNetwork(-100000,0));
		assertNull(static_scenario.getDensityForNetwork(-1,-1));
		assertNull(static_scenario.getDensityForNetwork(-1,100));
	}

	@Test
	public void test_getLinkWithId() {
		Link link = static_scenario.getLinkWithId(-1);
		double x = link.getLengthInMeters();
		double exp = 429.2823615191171;
		assertEquals(x,exp,1e-4);
		
		// edge cases
		assertNull(static_scenario.getLinkWithId(-100000));
	}
	
	@Test
	public void test_getNodeWithId() {
		Node node =  static_scenario.getNodeWithId(-2);
		double x = node.getPosition().getPoint().get(0).getLat();
		double exp  =37.8437831193107;
		assertEquals(x,exp,1e-4);
		
		// edge cases
		assertNull(static_scenario.getNodeWithId(-100000));
	}

	@Test
	public void test_get_Controller_Event_Sensor_WithId() {
		try {
			String config_file = "complete.xml";
			Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
			if(scenario==null)
				fail("scenario did not load");

			// initialize
			double timestep = Defaults.getTimestepFor(config_file);
			double starttime = 300d;
			double endtime = Double.POSITIVE_INFINITY;
			int numEnsemble = 10;
			scenario.initialize(timestep,starttime,endtime,numEnsemble);
			scenario.reset();
			
			assertNotNull(scenario.getControllerWithId(1));
			assertNotNull(scenario.getEventWithId(1));
			assertNotNull(scenario.getSensorWithId(1));

			assertNull(scenario.getControllerWithId(-100000));
			assertNull(scenario.getEventWithId(-100000));
			assertNull(scenario.getSensorWithId(-100000L));
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
	}

	@Test
	public void test_getSignalWithId_getSignalWithNodeId() {
		try {
			String config_file = "Albany-and-Berkeley.xml";
			Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
			if(scenario==null)
				fail("scenario did not load");
			
			assertNotNull(scenario.getSignalWithId(-12));
			assertNull(scenario.getSignalWithId(-100000));

			assertNotNull(scenario.getSignalWithNodeId(-62));
			assertNull(scenario.getSignalWithNodeId(-100000));
			
		} catch (BeatsException e) {
			fail("initialization failure.");
		}
		
	}

	@Test
	public void test_addController() {
		
	}

	@Test
	public void test_addEvent() {
	}

	@Test
	public void test_addDemandProfile() {
	}

	@Test
	public void test_loadSensorData() {
	}

	@Test
	public void test_calibrate_fundamental_diagrams() {
	}
	
}
