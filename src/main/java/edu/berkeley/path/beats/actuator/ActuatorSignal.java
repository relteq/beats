package edu.berkeley.path.beats.actuator;

import java.util.List;

import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;

public class ActuatorSignal extends Actuator {

	public void setGreenTimes(List<Double> green_times){
		this.command = green_times;
	}


	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public ActuatorSignal(Controller C,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
		super(C);
	}
	
	
	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		return;
	}

	@Override
	protected void validate() {
	}

	@Override
	protected void reset() throws BeatsException {
		return;
	}

	@Override
	public void deploy() {
		this.implementor.deploy_green_times((List<Double>) command);
	}

}
