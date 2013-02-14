package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;

public class ControlAlgorithm {
	
	protected String name;

	public ControlAlgorithm() {
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void compute(double[] metering_rate){
		System.out.println("This method has not been implemented by "+name);
	}

	public void compute(Scenario scenario,double opt_horizon,ArrayList<ScenarioElement> targets,double[][] metering_rate){
		System.out.println("This method has not been implemented by "+name);
	}
	
}
