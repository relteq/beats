package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Sensor;

public class SensorTest {

	private static Scenario scenario;
	private static Sensor sensor;
	private static String config_folder = "data/config/";
	private static String config_file = "complete.xml";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");
		sensor = scenario.getSensorWithId("1");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_getMyType() {
		assertTrue(sensor.getMyType().compareTo(Sensor.Type.loop)==0);
	}

	@Test
	public void test_getMyLink() {
		assertEquals(sensor.getMyLink().getId(),"1");
	}

}
