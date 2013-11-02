package edu.berkeley.path.beats.simulator;



public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {

	protected Controller myController;
	protected InterfaceActuator implementor;
	protected Object command;
	

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public Actuator (Controller C){
		myController = C;
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject) {
		return;
	}

	protected void validate() {
	}

	protected void reset() throws BeatsException {
		return;
	}
	
	protected void deploy(){};
	
}
