package edu.berkeley.path.beats.simulator;



public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {

	protected InterfaceActuator implementor;
	protected Object command;
	

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject) {
		return;
	}

	protected void validate() {
	}

	protected void reset() throws BeatsException {
		return;
	}

	protected void update() throws BeatsException {
		return;
	}


	/////////////////////////////////////////////////////////////////////
	// deploy
	/////////////////////////////////////////////////////////////////////
	
	public void delpoy(Object command){};
	
}
