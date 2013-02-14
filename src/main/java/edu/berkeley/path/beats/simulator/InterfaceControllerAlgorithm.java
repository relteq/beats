package edu.berkeley.path.beats.simulator;

public interface InterfaceControllerAlgorithm {

	public void compute() throws BeatsException;

	public void validate(Controller parent);
	
}
