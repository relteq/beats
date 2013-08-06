package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.simulator.ControlPolicyMaker;
import edu.berkeley.path.beats.simulator.Scenario;

import java.util.Map;

public class ControllerAlgorithm_CRM_ACTM_LP implements ControlPolicyMaker {

	public ControllerAlgorithm_CRM_ACTM_LP() {
		super();
	}

    @Override
    public Map<Long, Double[]> compute(Map<Long, Double> initialDensity, Map<Long, Double[]> splitRatios, Map<Long, Double[]> rampDemands, Scenario scenario) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
