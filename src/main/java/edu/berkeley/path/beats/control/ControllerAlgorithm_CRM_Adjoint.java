package edu.berkeley.path.beats.control;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.berkeley.path.beats.simulator.ControlAlgorithm;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Network;

public class ControllerAlgorithm_CRM_Adjoint extends ControlAlgorithm {

	public ControllerAlgorithm_CRM_Adjoint() {
		super();
	}

	@Override
	public Map<String, Double[]> compute(Map<String, Double> initialDensity,Map<String, Double[]> splitRatios,Map<String, Double[]> rampDemands, Network network) {
		
		int numTime=0;
		
		// print out initial densities
		System.out.println("Initial Densities");
        for(Map.Entry<String, Double> entry : initialDensity.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            System.out.printf("\t%s = %s%n", key, value);
        }
		
		// print out ramp demands
		System.out.println("Ramp demands");
        for(Map.Entry<String, Double[]> entry : rampDemands.entrySet()) {
            String key = entry.getKey();
            Double [] value = entry.getValue();
            numTime = value.length;
            System.out.printf("\t%s = %s%n", key, Arrays.toString(value));
        }
        
		
		// populate metering rate
		Map<String, Double[]> metering_rate = new HashMap<String, Double[]>();
		for(edu.berkeley.path.beats.jaxb.Link link : network.getListOfLinks())
			if( ((Link)link).getMyType().compareTo(Link.Type.onramp)==0) {
				Double [] rates = new Double[numTime];
				for(int k=0;k<numTime;k++)
					rates[k] = 100d;
				metering_rate.put(link.getId(),rates);
			}
		
		return metering_rate;
	}
	
}
