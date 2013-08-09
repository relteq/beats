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
//	private Integer [] vehicletypeindex; 	// index of vehicle types into global list
	private Map<Long,Integer[]> link_id_to_demandprofile_index;
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////

//	protected Integer[] getVehicletypeindex() {
//		return vehicletypeindex;
//	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {
		
		this.myScenario = myScenario;

		if(getDemandProfile().isEmpty())
			return;
		
		link_id_to_demandprofile_index = new HashMap<Long,Integer[]>();
		
//		vehicletypeindex = myScenario.getVehicleTypeIndices(getVehicleTypeOrder());

		for(int i=0;i<getDemandProfile().size();i++){
			DemandProfile dp = (DemandProfile) getDemandProfile().get(i);
			
			// populate demand profile
			dp.populate(myScenario);

			// add to link map
			long myLink_id =dp.getLinkIdOrigin();
			int veh_type_ind = dp.getVehicle_type_index();
			if(!dp.isOrphan() && veh_type_ind>=0){
				Integer [] dp_index = link_id_to_demandprofile_index.get(myLink_id);
				if(dp_index==null)
					dp_index = new Integer [myScenario.getNumVehicleTypes()];
				dp_index[veh_type_ind] = i;
			}
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
		
		// all profiles for a link should have the same start time, sample rate, and length
		for (Map.Entry<Long,Integer[]> entry : link_id_to_demandprofile_index.entrySet()) {
			Long link_id = entry.getKey();
			Integer [] dp_index_array = entry.getValue();
			if(dp_index_array!=null){
				HashSet<BigDecimal> unique_dt = new HashSet<BigDecimal>();
				HashSet<BigDecimal> unique_start_time = new HashSet<BigDecimal>();
				HashSet<Integer> unique_length = new HashSet<Integer>();
				for(Integer dp_index : dp_index_array){
					DemandProfile thisdp = (DemandProfile) demandProfile.get(dp_index);
					unique_dt.add(thisdp.getDt());
					unique_start_time.add(thisdp.getStartTime());
					unique_length.add(thisdp.get_demand_nominal().getNumTime());
				}
				if(unique_dt.size()!=1)
					BeatsErrorLog.addError("different dts found in demands for link id=" + link_id);
				if(unique_start_time.size()!=1)
					BeatsErrorLog.addError("different start times found in demands for link id=" + link_id);
				if(unique_length.size()!=1)
					BeatsErrorLog.addError("different profile lengths found in demands for link id=" + link_id);
			}
		}
		
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
	
	public Double[] getFutureTotalDemandInVeh_NoNoise(long link_id,double dt_in_seconds,int num_steps) throws BeatsException{
		
		if(!BeatsMath.isintegermultipleof(dt_in_seconds,myScenario.getSimdtinseconds()))
			throw new BeatsException("dt_in_seconds must be an integer multiple of simulation dt.");
		
		// list of demand profile indices for each vehicle type
		Integer [] dp_index = link_id_to_demandprofile_index.get(link_id);

		// simulation dt per output dt
		int num_times_per_sample = BeatsMath.round(dt_in_seconds / myScenario.getSimdtinseconds());

		// current simulation time step
		int current_step = myScenario.getClock().getCurrentstep();
		
		// return value
		Double [] X = new Double [num_steps];

		int n,j,k,step;
		double nth_demand;
		for(n=0;n<num_steps;n++){
			nth_demand = 0d;
			
			for(j=0;j<myScenario.getNumVehicleTypes();j++) {
				if(dp_index[j]==null)
					continue;
				DemandProfile dp = (DemandProfile) demandProfile.get(dp_index[j]);
				for(k=0;k<num_times_per_sample;k++){
					if(dp.getSamplesteps()>0)
						step = BeatsMath.floor((current_step-dp.getStepinitial())/((float)dp.getSamplesteps()));
					else
						step = 0;
					step = Math.min(step,dp.getNumTime()-1);
					Double x = dp.getValueForStep(step);
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
