package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.berkeley.path.beats.control.Controller_IRM_Alinea;
import edu.berkeley.path.beats.event.Event_Link_Lanes;
import edu.berkeley.path.beats.sensor.SensorLoopStation;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.ScenarioElement;

public class ScenarioElementTest {
	
	@Test
	public void test_getMyType() {
		
		// controller
//		ScenarioElement se1 = ObjectFactory.createScenarioElement(new Controller_IRM_Alinea());		
//		assertTrue(se1.getMyType().compareTo(ScenarioElement.Type.controller)==0);
		
		// sensor
//		ScenarioElement se2 = ObjectFactory.createScenarioElement(new SensorLoopStation());		
//		assertTrue(se2.getMyType().compareTo(ScenarioElement.Type.sensor)==0);	
		
		// event
//		ScenarioElement se3 = ObjectFactory.createScenarioElement(new Event_Link_Lanes());		
//		assertTrue(se3.getMyType().compareTo(ScenarioElement.Type.event)==0);	
		
	}

}
