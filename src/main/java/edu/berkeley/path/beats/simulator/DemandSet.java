/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.simulator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

final public class DemandSet extends edu.berkeley.path.beats.jaxb.DemandSet {

	private Scenario myScenario;
	private Map<Long,DemandProfile> link_id_to_demandprofile;
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {
		
		this.myScenario = myScenario;

		if(getDemandProfile().isEmpty())
			return;
		
		// link to demand profile map
		link_id_to_demandprofile = new HashMap<Long,DemandProfile>();
		for(edu.berkeley.path.beats.jaxb.DemandProfile dp : getDemandProfile()){
			
			DemandProfile sdp = (DemandProfile) dp;
			
			link_id_to_demandprofile.put(new Long(dp.getLinkIdOrg()), sdp);

			// populate demand profile
			sdp.populate(myScenario);
		}
	}

	protected void reset() {
		for(edu.berkeley.path.beats.jaxb.DemandProfile dp : getDemandProfile())
			((DemandProfile) dp).reset();
	}
	
	protected void validate() {

		if(getDemandProfile()==null)
			return;
		
		if(getDemandProfile().isEmpty())
			return;
		
		for(edu.berkeley.path.beats.jaxb.DemandProfile dp : getDemandProfile())
			((DemandProfile)dp).validate();		
	}

	protected void update() {
    	for(edu.berkeley.path.beats.jaxb.DemandProfile dp : getDemandProfile())
    		((DemandProfile) dp).update(false);	
	}
	
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////
	
	public double[] getFutureTotalDemandInVeh_NoNoise(long link_id,double dt_in_seconds,int num_steps) throws BeatsException{
		
		if(!BeatsMath.isintegermultipleof(dt_in_seconds,myScenario.getSimdtinseconds()))
			throw new BeatsException("dt_in_seconds must be an integer multiple of simulation dt.");
		
		// demand profile for this link
		DemandProfile dp = link_id_to_demandprofile.get(link_id);

		if(dp==null)
			throw new BeatsException("requested demand for non-existent link.");
		
		// simulation dt per output dt
		int num_times_per_sample = BeatsMath.round(dt_in_seconds / myScenario.getSimdtinseconds());

		// current simulation time step
		int current_step = myScenario.getClock().getCurrentstep();
		
		// return value
		double [] X = new double [num_steps];

		int n,j,k,step;
		double nth_demand;
		for(n=0;n<num_steps;n++){
			nth_demand = 0d;
			for(j=0;j<myScenario.getNumVehicleTypes();j++) {
				for(k=0;k<num_times_per_sample;k++){
					if(dp.getSamplesteps()>0)
						step = BeatsMath.floor((current_step-dp.getStepinitial())/((float)dp.getSamplesteps()));
					else
						step = 0;
					step = Math.min(step,dp.getProfile_length()-1);
					double x = dp.getTotalForStep(step);
					x = dp.applyKnob(x);
					nth_demand += x;
					current_step++;
				}
			}
			X[n] = nth_demand;
		}
		
		return X;
	}
	
}
