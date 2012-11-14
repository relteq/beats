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

import java.util.ArrayList;

final class FundamentalDiagramProfile extends edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile {

	protected Scenario myScenario;
	protected Link myLink;
	protected double dtinseconds;			// not really necessary
	protected int samplesteps;
	protected ArrayList<FundamentalDiagram> FD;
	protected boolean isdone; 
	protected int stepinitial;

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	// scale present and future fundamental diagrams to new lane value
	protected void set_Lanes(double newlanes){
		if(newlanes<=0 || FD.isEmpty())
			return;
		int step = SiriusMath.floor((myScenario.clock.getCurrentstep()-stepinitial)/samplesteps);
		step = Math.max(0,step);
		for(int i=step;i<FD.size();i++){
			FD.get(i).setLanes(newlanes);
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	protected void populate(Scenario myScenario) throws SiriusException {
		
		this.myScenario = myScenario;
		isdone = false;
		FD = new ArrayList<FundamentalDiagram>();
		
		// required
		myLink = myScenario.getLinkWithId(getLinkId());
		
		if(myLink!=null)
			myLink.setFundamentalDiagramProfile(this);
		
		// optional dt
		if(getDt()!=null){
			dtinseconds = getDt().floatValue();					// assume given in seconds
			samplesteps = SiriusMath.round(dtinseconds/myScenario.getSimDtInSeconds());
		}
		else{ 	// only allow if it contains only one fd
			if(getFundamentalDiagram().size()==1){
				dtinseconds = Double.POSITIVE_INFINITY;
				samplesteps = Integer.MAX_VALUE;
			}
			else{
				dtinseconds = -1.0;		// this triggers the validation error
				samplesteps = -1;
				return;
			}
		}
		
		//  read fundamental diagrams
		for(edu.berkeley.path.beats.jaxb.FundamentalDiagram fd : getFundamentalDiagram()){
			FundamentalDiagram _fd = new FundamentalDiagram(myLink,fd);	// create empty fd
//	        _fd.settoDefault();					// set to default
//			_fd.copyfrom(fd);					// copy and normalize
			FD.add(_fd);
		}
		
	}
	
	protected void validate() {
		
		if(myLink==null)
			SiriusErrorLog.addError("Bad link id=" + getLinkId() + " in fundamental diagram.");
		
		// check dtinseconds
		if( SiriusMath.lessthan(dtinseconds,0d) )
			SiriusErrorLog.addError("Negative dt in fundamental diagram profile for link id=" + getLinkId() + ".");
		
		// check dtinseconds
		if( SiriusMath.equals(dtinseconds,0d) && FD.size()>1 )
			SiriusErrorLog.addError("dt=0 in fundamental diagram profile for link id=" + getLinkId() + ".");
		
		if(!SiriusMath.isintegermultipleof(dtinseconds,myScenario.getSimDtInSeconds()))
			SiriusErrorLog.addError("Time step in fundamental diagram profile for link id=" + getLinkId() + " is not a multiple of simulation time step.");
		
		// check fundamental diagrams
		for(FundamentalDiagram fd : FD)
			fd.validate();

	}

	protected void reset() throws SiriusException {
		isdone = false;
		
		// read start time, convert to stepinitial
		double profile_starttime;	// [sec]
		
		profile_starttime = getStartTime()==null ? 0f : getStartTime().floatValue();
		stepinitial = SiriusMath.round((profile_starttime-myScenario.getTimeStart())/myScenario.getSimDtInSeconds());
		
		if(FD!=null)
			for(FundamentalDiagram fd : FD)
				fd.reset(myScenario.uncertaintyModel);
		
		// assign the fundamental diagram to the link
		//update();	
		
	}

	protected void update() throws SiriusException {
		if(myLink==null)
			return;
		if(isdone || FD.isEmpty())
			return;
		if(myScenario.clock.istimetosample(samplesteps,stepinitial)){
			int n = FD.size()-1;
			int step = samplesteps>0 ? SiriusMath.floor((myScenario.clock.getCurrentstep()-stepinitial)/samplesteps) : 0;
			step = Math.max(0,step);
			if(step<n)
				myLink.setFundamentalDiagramFromProfile( FD.get(step) );
			if(step>=n && !isdone){
				myLink.setFundamentalDiagramFromProfile( FD.get(n) );
				isdone = true;
			}
		}
	}
	
}
