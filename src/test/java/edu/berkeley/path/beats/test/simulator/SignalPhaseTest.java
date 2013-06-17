package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Signal.NEMA;
import edu.berkeley.path.beats.simulator.SignalPhase;

public class SignalPhaseTest {

	private static SignalPhase signalphase;
	private static String config_folder = "data/config/";
	private static String config_file = "Albany-and-Berkeley.xml";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");
		signalphase = scenario.getSignalWithId("-12").getPhaseByNEMA(NEMA._2);
	}
	
	@Test
	public void test_getYellowtime() {
		assertEquals(signalphase.getYellowtime(),4.0,1e-4);
	}

	@Test
	public void test_getRedcleartime() {
		assertEquals(signalphase.getRedcleartime(),3.0,1e-4);
	}

	@Test
	public void test_getMingreen() {
		assertEquals(signalphase.getMingreen(),0.0,1e-4);
	}
	
	@Test
	public void test_getMyNEMA() {
		assertEquals(signalphase.getNEMA().compareTo(NEMA._2),0);
	}

	@Test
	public void test_getActualyellowtime() {
		assertEquals(signalphase.getActualyellowtime(),4.0,1e-4);
	}

	/*
	@Test
	public void test_setActualyellowtime() {
		float oldyellowtime = signalphase.getActualyellowtime();
		signalphase.setActualyellowtime(oldyellowtime+1);
		assertEquals(signalphase.getActualyellowtime(),oldyellowtime+1,1e-4);
		
		// edge cases
		signalphase.setActualyellowtime(oldyellowtime);
		signalphase.setActualyellowtime(-1);	// should do nothing
		assertEquals(signalphase.getActualyellowtime(),oldyellowtime,1e-4);
	}
	*/

	@Test
	public void test_getActualredcleartime() {
		assertEquals(signalphase.getActualredcleartime(),3.0,1e-4);
	}

	/*
	@Test
	public void test_setActualredcleartime() {
		float oldredcleartime = signalphase.getActualredcleartime();
		signalphase.setActualredcleartime(oldredcleartime+1);
		assertEquals(signalphase.getActualredcleartime(),oldredcleartime+1,1e-4);
		
		// edge cases
		signalphase.setActualredcleartime(oldredcleartime);
		signalphase.setActualredcleartime(-1);	// should do nothing
		assertEquals(signalphase.getActualredcleartime(),oldredcleartime,1e-4);
	}
	*/
	
}
