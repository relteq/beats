package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Signal;
import edu.berkeley.path.beats.simulator.Signal.Command;
import edu.berkeley.path.beats.simulator.Signal.NEMA;
import edu.berkeley.path.beats.simulator.SignalPhase;

public class SignalTest {

	private static Signal signal;
	private static String config_folder = "data/config/";
	private static String config_file = "Albany-and-Berkeley.xml";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");
		signal = (Signal) scenario.getSignalWithId("-12");
	}

	@Test
	public void test_getPhaseByNEMA() {
		
		assertNotNull(signal.getPhaseByNEMA(NEMA._2));
		assertNull(signal.getPhaseByNEMA(NEMA.NULL));
		
		// edge case
		assertNull(signal.getPhaseByNEMA(null));
	}

	@Test
	public void test_requestCommand() {
		ArrayList<Signal.Command> command = new ArrayList<Signal.Command>();
		NEMA nema = Signal.NEMA._2;
		SignalPhase phase = signal.getPhaseByNEMA(nema);
		command.add( new Command(Signal.CommandType.forceoff,nema,10f,20f,30f) );
		signal.requestCommand(command);
		assertEquals(phase.getActualredcleartime(),30,1e-4);
		assertEquals(phase.getActualyellowtime(),20,1e-4);
	}

}
