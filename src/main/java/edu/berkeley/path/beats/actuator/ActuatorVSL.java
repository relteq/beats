package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Controller;

public class ActuatorVSL extends Actuator {

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public ActuatorVSL(Controller C,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
		super(C);
	}
	
}
