package edu.berkeley.path.beats.control;

import java.util.ArrayList;

import edu.berkeley.path.beats.simulator.ControlAlgorithm;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;

public class ControllerAlgorithm_CRM_Fake extends ControlAlgorithm {

	public ControllerAlgorithm_CRM_Fake() {
		super();
	}

	@Override
	public void compute(Scenario scenario,double opt_horizon,ArrayList<ScenarioElement> targets,double[][] metering_rate){

		int numTime = metering_rate.length;
		int numTargets = metering_rate[0].length;
		
		double current_time = scenario.getCurrentTimeInSeconds();
		
		int k,i;
		for(k=0;k<numTime;k++)
			for(i=0;i<numTargets;i++)
				metering_rate[k][i] = current_time + k*numTargets + i;
	
	}
}
