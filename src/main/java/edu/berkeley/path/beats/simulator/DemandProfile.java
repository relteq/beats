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

final public class DemandProfile extends edu.berkeley.path.beats.jaxb.DemandProfile {

	// does not change ....................................
	private Scenario myScenario;
	private boolean isOrphan;
	private double dtinseconds;				// not really necessary
	private int samplesteps;				// [sim steps] profile sample period
	private int stepinitial;
	private BeatsTimeProfile [] demand_nominal;	// [veh] demand profile per vehicle type
	private int [] vehicle_type_index;		// vehicle type indices for demand_nominal
	private double std_dev_add;				// [veh]
	private double std_dev_mult;			// [veh]
	private boolean isdeterministic;		// true if the profile is deterministic
	private int profile_length;
	private boolean all_demands_scalar;		// true if all demand profiles have length 1.	
	
	// does change ........................................
	private boolean isdone; 
	private double [] current_sample;		// sample per vehicle type
	private double _knob;

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	protected void set_knob(double _knob) {
		this._knob = Math.max(_knob,0.0);
		
		// resample the profile
		update(true);
	}

	protected boolean isOrphan() {
		return isOrphan;
	}

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	protected void populate(Scenario myScenario) {

		this.myScenario = myScenario;
		
		isdone = false;
		
		// required
		Link myLink = myScenario.getLinkWithId(getLinkIdOrg());
		isOrphan = myLink==null;
		
		if(isOrphan)
			return;
		
		// attach to link
		myLink.setMyDemandProfile(this);
		
		// sample demand distribution, convert to vehicle units
		int numdemand = getDemand().size();
		demand_nominal = new BeatsTimeProfile[numdemand];
		vehicle_type_index = new int[numdemand];
		for(int i=0;i<numdemand;i++){
			edu.berkeley.path.beats.jaxb.Demand d = getDemand().get(i);
			vehicle_type_index[i] = myScenario.getVehicleTypeIndexForId(d.getVehicleTypeId());
			if(vehicle_type_index[i]<0)
				continue;
			if(d.getContent()!=null){
				demand_nominal[i] = new BeatsTimeProfile(d.getContent());
				demand_nominal[i].multiplyscalar(myScenario.getSimdtinseconds());
			}
		}
		
		// check whether all demands are scalar
		all_demands_scalar = true;
		profile_length = -1;
		for(BeatsTimeProfile d : demand_nominal)
			if(d!=null){
				all_demands_scalar &= d.getNumTime()<=1;
				profile_length = d.getNumTime();		// will check in validation that they are all the same
			}
		
		// optional dt
		if(getDt()!=null){
			dtinseconds = getDt().floatValue();					// assume given in seconds
			samplesteps = BeatsMath.round(dtinseconds/myScenario.getSimdtinseconds());
		}
		else{ 	// allow only if it contains one time step
			if(all_demands_scalar){
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
			std_dev_add = getStdDevAdd().doubleValue() * myScenario.getSimdtinseconds();
		else
			std_dev_add = Double.POSITIVE_INFINITY;		// so that the other will always win the min
		
		if(getStdDevMult()!=null)
			std_dev_mult = getStdDevMult().doubleValue() * myScenario.getSimdtinseconds();
		else
			std_dev_mult = Double.POSITIVE_INFINITY;	// so that the other will always win the min
		
		isdeterministic = (getStdDevAdd()==null || std_dev_add==0.0) && 
						  (getStdDevMult()==null || std_dev_mult==0.0);
		
		_knob = getKnob();
		stepinitial = BeatsMath.round((getStartTime()-myScenario.getTimeStart())/myScenario.getSimdtinseconds());

	}

	protected void validate() {
		
		int i;
		
		if(demand_nominal==null || demand_nominal.length==0){
			BeatsErrorLog.addWarning("Demand profile id=" + getId() + " has no data.");
			return;
		}
		
		if(isOrphan){
			BeatsErrorLog.addWarning("Bad origin link id=" + getLinkIdOrg() + " in demand profile.");
			return;
		}
		
		// check all demands have same length
		for(BeatsTimeProfile d : demand_nominal)
			if(d!=null && d.getNumTime()!=profile_length){
				BeatsErrorLog.addError("In demand profile for link id=" + getLinkIdOrg() + ", not all demands have the same length.");
				break;
			}
		
		for(i=0;i<demand_nominal.length;i++)
			if(vehicle_type_index[i]<0)
				BeatsErrorLog.addError("Bad vehicle type id " + getDemand().get(i).getVehicleTypeId() + " in demand profile for link id=" + getLinkIdOrg());
		
		// check dtinseconds
		if( dtinseconds<=0 && !all_demands_scalar )
			BeatsErrorLog.addError("Non-positive time step in demand profile for link id=" + getLinkIdOrg());
		
		if(!BeatsMath.isintegermultipleof(dtinseconds,myScenario.getSimdtinseconds()) && !all_demands_scalar )
			BeatsErrorLog.addError("Demand time step in demand profile for link id=" + getLinkIdOrg() + " is not a multiple of simulation time step.");
		
		// check non-negative
		for(i=0;i<demand_nominal.length;i++)
			if(demand_nominal[i]!=null && demand_nominal[i].hasNaN())
				BeatsErrorLog.addError("Illegal values in demand profile for link id=" + getLinkIdOrg() + ", vehicle type id " + getDemand().get(i).getVehicleTypeId());

	}

	protected void reset() {
		
		if(isOrphan)
			return;
					
		isdone = false;

		// set current sample to zero
		current_sample = BeatsMath.zeros(myScenario.getNumVehicleTypes());
		
		// set knob back to its original value
		_knob = getKnob();	
	}
	
	protected void update(boolean forcesample) {
		
		if(isOrphan)
			return;
		
		if(demand_nominal==null || demand_nominal.length==0)
			return;
		
		if(isdone && !forcesample)
			return;
		
		if(forcesample || myScenario.getClock().istimetosample(samplesteps,stepinitial)){
			
			// REMOVE THESE
			int n = profile_length-1;
			int step = myScenario.getClock().sampleindex(stepinitial, samplesteps);
			int i;
			
			// forced sample due to knob change
			if(forcesample){
				for(i=0;i<demand_nominal.length;i++)
					current_sample[vehicle_type_index[i]] = sample_finalTime_addNoise_applyKnob(i);
				return;
			}
			
			// demand is zero before stepinitial
			if(myScenario.getClock().getCurrentstep()<stepinitial)
				return;
			
			// sample the profile
			if(step<n){
				for(i=0;i<demand_nominal.length;i++)
					current_sample[vehicle_type_index[i]] = sample_currentTime_addNoise_applyKnob(i);
				return;
			}
			
			// last sample
			if(step>=n && !isdone){
				for(i=0;i<demand_nominal.length;i++)
					current_sample[vehicle_type_index[i]] = sample_finalTime_addNoise_applyKnob(i);
				isdone = true;
				return;
			}
			
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////

    private double applyKnob(final double demandvalue){
        return demandvalue*myScenario.getGlobal_demand_knob()*_knob;
    }

	private double sample_finalTime_addNoise_applyKnob(int vtype_index){
		return sample_KthTime_addNoise_applyKnob(profile_length-1,vtype_index);
	}
	
	private double sample_currentTime_addNoise_applyKnob(int vtype_index){
		int step = myScenario.getClock().sampleindex(stepinitial, samplesteps);
		return sample_KthTime_addNoise_applyKnob(step,vtype_index);
	}

	/** sample the k-th entry of the profile, add noise **/
	private double sample_KthTime_addNoise_applyKnob(int k,int vtype_index){
				
		double demandvalue =  demand_nominal[vtype_index].get(k);
		
		// add noise
		if(!isdeterministic)
			addNoise(demandvalue);

		// apply the knob 
		demandvalue = applyKnob(demandvalue);
		
		return demandvalue;
	}

    private double addNoise(final double demandvalue){

        double noisy_demand = demandvalue;

        // use smallest between multiplicative and additive standard deviations
        double std_dev_apply = Double.isInfinite(std_dev_mult) ? std_dev_add :
                Math.min( noisy_demand*std_dev_mult , std_dev_add );

        // sample the distribution
        switch(myScenario.getUncertaintyModel()){

            case uniform:
                for(int j=0;j<myScenario.getNumVehicleTypes();j++)
                    noisy_demand += BeatsMath.sampleZeroMeanUniform(std_dev_apply);
                break;

            case gaussian:
                for(int j=0;j<myScenario.getNumVehicleTypes();j++)
                    noisy_demand += BeatsMath.sampleZeroMeanGaussian(std_dev_apply);
                break;
        }

        // non-negativity
        noisy_demand = Math.max(0.0,noisy_demand);

        return noisy_demand;

    }

	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////

	public double [] getCurrentValue(){
		return current_sample;
	}

    public double [] predictTotal(double start_time,double time_step,int num_steps){

        double [] val = BeatsMath.zeros(num_steps);

        if(demand_nominal==null || demand_nominal.length==0)
            return val;

        for(int i=0;i<num_steps;i++){

            // time in seconds after midnight
            double time = start_time + i*time_step + 0.5*time_step;

            // corresponding profile step
            int profile_step = BeatsMath.floor( (time-getStartTime())/getDt().floatValue() );
            if(profile_step>=0){
                profile_step = Math.min(profile_step,demand_nominal[0].getNumTime()-1);
                for(int x=0;x<demand_nominal.length;x++)
                    val[i] +=  demand_nominal[x].get(profile_step);
                val[i] = applyKnob(val[i]);
            }

        }
        return val;
    }


}
