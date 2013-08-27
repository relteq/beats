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

final class SplitRatioProfile extends edu.berkeley.path.beats.jaxb.SplitRatioProfile {

	private Scenario myScenario;
	private Node myNode;
	private double dtinseconds;				// not really necessary
	private int samplesteps;
	
//	private Double2DMatrix [][] profile;		// profile[i][j] is the 2D split matrix for
//												// input link i, output link j. The first dimension 
//												// of the Double2DMatrix is time, the second in vehicle type.
	
	private BeatsTimeProfile [][][] profile; 	// profile[i][j][v] is the split ratio profile for
												// input link i, output link j, vehicle type v.
	
	private Double3DMatrix currentSplitRatio; 	// current split ratio matrix with dimension [inlink x outlink x vehicle type]
	private int laststep;
	private boolean isdone; 
	private int stepinitial;

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {

		if(getSplitratio().isEmpty())
			return;
		
		if(myScenario==null)
			return;
		
		this.myScenario = myScenario;
		
		// required
		myNode = myScenario.getNodeWithId(getNodeId());

		isdone = false;
		
		if(myNode==null)
			return;

		// optional dt
		if(getDt()!=null){
			dtinseconds = getDt().floatValue();					// assume given in seconds
			samplesteps = BeatsMath.round(dtinseconds/myScenario.getSimdtinseconds());
		}
		else{ 	// only allow if it contains only one fd
			if(getSplitratio().size()==1){
				dtinseconds = Double.POSITIVE_INFINITY;
				samplesteps = Integer.MAX_VALUE;
			}
			else{
				dtinseconds = -1.0;		// this triggers the validation error
				samplesteps = -1;
				return;
			}
		}
		
//		profile = new Double2DMatrix[myNode.getnIn()][myNode.getnOut()];
		profile = new BeatsTimeProfile[myNode.getnIn()][myNode.getnOut()][myScenario.getNumVehicleTypes()];
		int in_index,out_index,vt_index;
		laststep = 0;
		for(edu.berkeley.path.beats.jaxb.Splitratio sr : getSplitratio()){
			in_index = myNode.getInputLinkIndex(sr.getLinkIn());
			out_index = myNode.getOutputLinkIndex(sr.getLinkOut());
			vt_index = myScenario.getVehicleTypeIndexForId(sr.getVehicleTypeId());
			if(in_index<0 || out_index<0 || vt_index<0)
				continue; 
			profile[in_index][out_index][vt_index] = new BeatsTimeProfile(sr.getContent());
			if(!profile[in_index][out_index][vt_index].isEmpty())
				laststep = Math.max(laststep,profile[in_index][out_index][vt_index].getNumTime());
		}
		
		currentSplitRatio = new Double3DMatrix(myNode.getnIn(),myNode.getnOut(),myScenario.getNumVehicleTypes(),Double.NaN);
		
		// inform the node
		myNode.setMySplitRatioProfile(this);
//		myNode.setHasSRprofile(true);
		
	}

	protected void reset() {
		
		// read start time, convert to stepinitial
		double starttime;
		if( Double.isInfinite(getStartTime()))
			starttime = 0d;
		else
			starttime = getStartTime();	// assume given in seconds
		
		stepinitial = BeatsMath.round((starttime-myScenario.getTimeStart())/myScenario.getSimdtinseconds());
	}

	protected void validate() {

		if(getSplitratio().isEmpty())
			return;
		
		if(myNode==null){
			BeatsErrorLog.addWarning("Unknown node with id=" + getNodeId() + " in split ratio profile.");
			return; // this profile will be skipped but does not cause invalidation.
		}
		
		// check link ids
		int index;
		for(edu.berkeley.path.beats.jaxb.Splitratio sr : getSplitratio()){
			index = myNode.getInputLinkIndex(sr.getLinkIn());
			if(index<0)
				BeatsErrorLog.addError("Bad input link id=" + sr.getLinkIn() + " in split ratio profile with node id=" + getNodeId());

			index = myNode.getOutputLinkIndex(sr.getLinkOut());
			if(index<0)
				BeatsErrorLog.addError("Bad output link id=" + sr.getLinkOut() + " in split ratio profile with node id=" + getNodeId());

		}

		boolean all_scalar = true;
		int i,j,v;
		for(i=0;i<profile.length;i++)
			if(profile[i]!=null)
				for(j=0;j<profile[i].length;j++)
					if(profile[i][j]!=null)
						for(v=0;v<profile[i][j].length;v++)
							if(profile[i][j][v]!=null)
								all_scalar &= profile[i][j][v].getNumTime()<=1;

		if(!all_scalar){
			// dtinseconds must be positive if any profile has >1 element
			if(dtinseconds<=0)
				BeatsErrorLog.addError("Invalid time step =" + getDt() +  " in split ratio profile for node id=" + getNodeId());

			// dtinseconds must be multiple of simdt if any profile has >1 element
			if( !BeatsMath.isintegermultipleof(dtinseconds,myScenario.getSimdtinseconds()))
				BeatsErrorLog.addError("Time step = " + getDt() + " for split ratio profile of node id=" + getNodeId() + " is not a multiple of the simulation time step (" + myScenario.getSimdtinseconds() + ")"); 
		}
		
//		// check split ratio dimensions and values
//		for(i=0;i<profile.length;i++)
//			if(profile[i]!=null)
//				for(j=0;j<profile[i].length;j++)
//					if(profile[i][j]!=null){
//
//						// check dimensions
//						if(profile[i][j].getnVTypes()!=myScenario.getNumVehicleTypes())
//							BeatsErrorLog.addError("Split ratio profile for node id=" + getNodeId() + " does not contain values for all vehicle types: ");
//
//						// check values
//						for(k=0;k<profile[i][j].getnTime();k++)
//							for(v=0;v<profile[i][j].getnVTypes();v++)
//								if( BeatsMath.greaterthan( profile[i][j].get(k,v) , 1.0 ) | BeatsMath.lessthan( profile[i][j].get(k,v) , 0.0 ) )
//									BeatsErrorLog.addError("Split ratio profile for node id=" + getNodeId() + " is out of range.");
//					}		
	}

	protected void update() {
		if(profile==null)
			return;
		if(myNode==null)
			return;
		if(isdone)
			return;
		if(myScenario.getClock().istimetosample(samplesteps,stepinitial)){
			
			int step = myScenario.getClock().sampleindex(stepinitial, samplesteps);

			// zeroth sample extends to the left
			step = Math.max(0,step);
			
			// sample
			currentSplitRatio = sampleAtTimeStep( Math.min( step , laststep-1) );
			
			// assign
			myNode.normalizeSplitRatioMatrix(currentSplitRatio);
			//myNode.setSampledSRProfile(currentSplitRatio);
			
			// stop sampling after laststep
			isdone = step>=laststep-1;
		}		
	}

	/////////////////////////////////////////////////////////////////////
	// protected getter
	/////////////////////////////////////////////////////////////////////

	protected Double3DMatrix getCurrentSplitRatio() {
		return currentSplitRatio;
	}
	
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	// for time sample k, returns a 3D matrix with dimensions inlink x outlink x vehicle type
	private Double3DMatrix sampleAtTimeStep(int k){
		if(myNode==null)
			return null;
		Double3DMatrix X = new Double3DMatrix(myNode.getnIn(),myNode.getnOut(),
				myScenario.getNumVehicleTypes(),Double.NaN);	// initialize all unknown
		
		// get vehicle type order from SplitRatioProfileSet
//		Integer [] vehicletypeindex = null;
//		if(myScenario.getSplitRatioSet()!=null)
//			vehicletypeindex = ((SplitRatioSet)myScenario.getSplitRatioSet()).getVehicletypeindex();
		
		int i,j,v,lastk;
		for(i=0;i<myNode.getnIn();i++){
			for(j=0;j<myNode.getnOut();j++){
				for(v=0;v<myScenario.getNumVehicleTypes();v++){
					if(profile[i][j][v]==null)						// nan if not defined
						continue;
					if(profile[i][j][v].isEmpty())					// nan if no data
						continue;
					lastk = Math.min(k,profile[i][j][v].getNumTime()-1);	// hold last value
					X.set(i,j,v,profile[i][j][v].get(lastk));
				}
			}
		}
		return X;
	}

}
