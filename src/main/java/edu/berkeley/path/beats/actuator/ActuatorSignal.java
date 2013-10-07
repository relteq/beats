package edu.berkeley.path.beats.actuator;

import java.util.List;

import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsException;

public class ActuatorSignal extends Actuator {

	public void setGreenTimes(List<Double> green_times){
		this.command = green_times;
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / update
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
	protected void update() throws BeatsException {
		return;
	}

	/////////////////////////////////////////////////////////////////////
	// deploy
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public void delpoy(Object command) {
		this.implementor.deploy_green_times((List<Double>) command);
	}

}
