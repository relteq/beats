package edu.berkeley.path.beats.simulator;

import java.util.Map;

public class ControlAlgorithm {
	
	private String name;

	public ControlAlgorithm() {
	}
	
	public void setName(String name){
		this.name = name;
	}
	
//	/** returns the array of metering rates **/
//	public ArrayList<ArrayList<Double>> compute(){
//		System.out.println("This method has not been implemented by "+name);
//		return null;
//	}
//
//	/** returns the array of metering rates **/
//	public ArrayList<ArrayList<Double>> compute(Scenario scenario,double opt_horizon,ArrayList<ScenarioElement> targets){
//		System.out.println("This method has not been implemented by "+name);
//		return null;
//	}
	
	/** returns the array of metering rates **/
    public Map<String,Double[]> compute(Map<String, Double> initialDensity,Map<String,Double[]> splitRatios,Map<String,Double[]> rampDemands,Network network ){
		System.out.println("This method has not been implemented by "+name);
		return null;    
	}
    
}
