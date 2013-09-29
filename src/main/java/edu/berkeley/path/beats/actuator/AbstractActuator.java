package edu.berkeley.path.beats.actuator;


public abstract class AbstractActuator {

	protected InterfaceActuator implementor;
	protected Object command;
	
	public void delpoy(Object command){};
	
}
