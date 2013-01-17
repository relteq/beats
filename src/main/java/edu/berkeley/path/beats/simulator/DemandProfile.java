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

final class DemandProfile extends edu.berkeley.path.beats.jaxb.DemandProfile {

	protected Scenario myScenario;
	protected Link myLinkOrigin;
	protected double dtinseconds;				// not really necessary
	protected int samplesteps;					// [sim steps] profile sample period
	protected Double2DMatrix demand_nominal;	// [veh]
	protected boolean isdone; 
	protected int stepinitial;
	protected double _knob;
	protected Double std_dev_add;				// [veh]
	protected Double std_dev_mult;			// [veh]
	protected boolean isdeterministic;		// true if the profile is deterministic

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	protected void set_knob(double _knob) {
		this._knob = Math.max(_knob,0.0);
		
		// resample the profile
		update(true);
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {

		this.myScenario = myScenario;
		
		isdone = false;
		
		// required
		if(getLinkIdOrigin()!=null)
			myLinkOrigin = myScenario.getLinkWithId(getLinkIdOrigin());

		// sample demand distribution, convert to vehicle units
		if(getContent()!=null){
			demand_nominal = new Double2DMatrix(getContent());
			demand_nominal.multiplyscalar(myScenario.getSimDtInSeconds());
		}
		
		// optional dt
		if(getDt()!=null){
			dtinseconds = getDt().floatValue();					// assume given in seconds
			samplesteps = SiriusMath.round(dtinseconds/myScenario.getSimDtInSeconds());
		}
		else{ 	// allow only if it contains one time step
			if(demand_nominal.getnTime()==1){
				dtinseconds = Double.POSITIVE_INFINITY;
				samplesteps = Integer.MAX_VALUE;
			}
			else{
				dtinseconds = -1.0;		// this triggers the validation error
				samplesteps = -1;
				return;
			}
		}
		
		// optional uncertainty model
		if(getStdDevAdd()!=null)
			std_dev_add = getStdDevAdd().doubleValue() * myScenario.getSimDtInSeconds();
		else
			std_dev_add = Double.POSITIVE_INFINITY;		// so that the other will always win the min
		
		if(getStdDevMult()!=null)
			std_dev_mult = getStdDevMult().doubleValue() * myScenario.getSimDtInSeconds();
		else
			std_dev_mult = Double.POSITIVE_INFINITY;	// so that the other will always win the min
		
		isdeterministic = (getStdDevAdd()==null || std_dev_add==0.0) && 
						  (getStdDevMult()==null || std_dev_mult==0.0);
		
		_knob = getKnob().doubleValue();
		
	}

	protected void validate() {
		
		if(demand_nominal.isEmpty())
			return;
		
		if(myLinkOrigin==null)
			SiriusErrorLog.addError("Bad origin link id=" + getLinkIdOrigin() + " in demand profile.");
		
		// check dtinseconds
		if( dtinseconds<=0 )
			SiriusErrorLog.addError("Non-positive time step in demand profile for link id=" + getLinkIdOrigin());
		
		if(!SiriusMath.isintegermultipleof(dtinseconds,myScenario.getSimDtInSeconds()))
			SiriusErrorLog.addError("Demand time step in demand profile for link id=" + getLinkIdOrigin() + " is not a multiple of simulation time step.");
		
		// check dimensions
		if(demand_nominal.getnVTypes()!=myScenario.getNumVehicleTypes())
			SiriusErrorLog.addError("Incorrect dimensions for demand for link id=" + getLinkIdOrigin());
		
		// check non-negative
		if(demand_nominal.hasNaN())
			SiriusErrorLog.addError("Illegal values in demand profile for link id=" + getLinkIdOrigin());

	}

	protected void reset() {
		isdone = false;
		
		// read start time, convert to stepinitial
		double starttime;	// [sec]
		if( getStartTime()!=null)
			starttime = getStartTime().floatValue();
		else
			starttime = 0f;

		stepinitial = SiriusMath.round((starttime-myScenario.getTimeStart())/myScenario.getSimDtInSeconds());
		
		// set knob back to its original value
		_knob = getKnob().doubleValue();	
	}
	
	protected void update(boolean forcesample) {
		if(myLinkOrigin==null)
			return;
		if(isdone && !forcesample)
			return;
		if(demand_nominal.isEmpty())
				return;
		if(forcesample || myScenario.clock.istimetosample(samplesteps,stepinitial)){
			
			int n = demand_nominal.getnTime()-1;
			int step = myScenario.clock.sampleindex(stepinitial, samplesteps);
			
			// forced sample due to knob change
			if(forcesample){
				myLinkOrigin.setSourcedemandFromVeh( sampleAtTimeStep(n) );
				return;
			}
			
			// demand is zero before stepinitial
			if(step<0)
				return;
			
			// sample the profile
			if(step<n){
				myLinkOrigin.setSourcedemandFromVeh( sampleAtTimeStep(step) );
				return;
			}
			
			// last sample
			if(step>=n && !isdone){
				myLinkOrigin.setSourcedemandFromVeh( sampleAtTimeStep(n) );
				isdone = true;
				return;
			}
			
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	private Double [] sampleAtTimeStep(int k){
		
		// get vehicle type order from SplitRatioProfileSet
		Integer [] vehicletypeindex = null;
		if(myScenario.getSplitRatioProfileSet()!=null)
			vehicletypeindex = ((DemandProfileSet)myScenario.getDemandProfileSet()).vehicletypeindex;
		
		Double [] demandvalue = demand_nominal.sampleAtTime(k,vehicletypeindex);
		
		if(!isdeterministic){
			
			// use smallest between multiplicative and additive standard deviations
			Double [] std_dev_apply = new Double [myScenario.getNumVehicleTypes()];
			for(int j=0;j<myScenario.getNumVehicleTypes();j++)
				std_dev_apply[j] = Math.min( demandvalue[j]*std_dev_mult , std_dev_add );
			
			// sample the distribution
			switch(myScenario.uncertaintyModel){
			case uniform:
				for(int j=0;j<myScenario.getNumVehicleTypes();j++)
					demandvalue[j] += SiriusMath.sampleZeroMeanUniform(std_dev_apply[j]);
				break;
	
			case gaussian:
				for(int j=0;j<myScenario.getNumVehicleTypes();j++)
					demandvalue[j] += SiriusMath.sampleZeroMeanGaussian(std_dev_apply[j]);
				break;
			}
		}

		// apply the knob and non-negativity
		for(int j=0;j<myScenario.getNumVehicleTypes();j++)
			demandvalue[j] = Math.max(0.0,myScenario.global_demand_knob*demandvalue[j]*_knob);
		
		return demandvalue;
	}

	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////
	
	public Double2DMatrix get_demand_nominal(){
		return demand_nominal;
	}
	
}
