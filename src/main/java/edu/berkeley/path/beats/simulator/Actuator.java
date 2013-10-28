package edu.berkeley.path.beats.simulator;



//public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {
public class Actuator {

	protected edu.berkeley.path.beats.jaxb.Actuator jaxbA;
	protected Controller myController;
	protected InterfaceActuator implementor;
	protected Object command;
	

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Actuator (){
	}
	
	public Actuator (Controller C,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
		this.jaxbA = jaxbA;
		this.myController = C;
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
