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

final class CapacityProfile extends edu.berkeley.path.beats.jaxb.CapacityProfile {

	protected Scenario myScenario;
	protected Link myLink;
	protected double dtinseconds;			// not really necessary
	protected int samplesteps;
	protected Double1DVector capacity;		// [veh]
	protected boolean isdone;
	protected int stepinitial;

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {
		if(myScenario==null)
			return;
		this.myScenario = myScenario;
		myLink = myScenario.getLinkWithId(getLinkId());
		dtinseconds = getDt().floatValue();					// assume given in seconds
		samplesteps = BeatsMath.round(dtinseconds/myScenario.getSimDtInSeconds());
		isdone = false;
		
		// read capacity and convert to vehicle units
		String str = getContent();
		if(!str.isEmpty()){
			capacity = new Double1DVector(getContent(),",");	// true=> reshape to vector along k, define length
			capacity.multiplyscalar(myScenario.getSimDtInSeconds() * myLink.get_Lanes());
		}
			
	}
	
	protected void validate() {
		
		if(capacity==null)
			return;
		
		if(capacity.isEmpty())
			return;
		
		if(myLink==null){
			BeatsErrorLog.addWarning("Unknown link id=" + getLinkId() + " in capacity profile.");
			return;
		}
		
		// check dtinseconds
		if( dtinseconds<=0  && capacity.getLength()>1)
			BeatsErrorLog.addError("Non-positive time step in capacity profile for link id=" + getLinkId());

		if(!BeatsMath.isintegermultipleof(dtinseconds,myScenario.getSimDtInSeconds()) && capacity.getLength()>1)
			BeatsErrorLog.addError("Time step for capacity profile of link id=" + getLinkId() + " is not a multiple of simulation time step.");
		
		// check non-negative
		if(capacity.hasNaN())
			BeatsErrorLog.addError("Capacity profile for link id=" +getLinkId()+ " has illegal values.");

	}

	protected void reset() {
		isdone = false;
		
		// read start time, convert to stepinitial
		double starttime;
		if( getStartTime()!=null)
			starttime = getStartTime().floatValue();
		else
			starttime = 0f;

		stepinitial = (int) Math.round((starttime-myScenario.getTimeStart())/myScenario.getSimDtInSeconds());

	}
	
	protected void update() {
		if(myLink==null)
			return;
		if(capacity==null)
			return;
		if(isdone || capacity.isEmpty())
			return;
		if(myScenario.clock.istimetosample(samplesteps,stepinitial)){
			
			int n = capacity.getLength()-1;
			int step = myScenario.clock.sampleindex(stepinitial, samplesteps);

			// zeroth sample extends to the left
			step = Math.max(0,step);

			// sample the profile
			if(step<n){
				myLink.setCapacityFromVeh(capacity.get(step));
				return;
			}

			// last sample
			if(step>=n && !isdone){
				myLink.setCapacityFromVeh(capacity.get(n));
				isdone = true;
				return;
			}
		}
	}

}
