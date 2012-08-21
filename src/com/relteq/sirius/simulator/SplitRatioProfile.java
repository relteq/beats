/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.ArrayList;

final class SplitRatioProfile extends com.relteq.sirius.jaxb.SplitratioProfile {

	protected Scenario myScenario;
	protected Node myNode;
	protected int myDestinationNetworkIndex;	// index in myNode
	
	protected double dtinseconds;				// not really necessary
	protected int samplesteps;
	
	protected ArrayList<Double4DMatrix> profile;	// time array of 4D split ratio matrices
													// each 4D matrix contains information per destination network, inlink, outlink, vehicle type
	
	protected Double4DMatrix currentSplitRatio; 	// current split ratio matrix with dimension [inlink x outlink x vehicle type]
		
	protected boolean isdone; 
	protected int stepinitial;

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
		
		profile = new ArrayList<Double4DMatrix>();
		

		// get vehicle type order from SplitRatioProfileSet
		Integer [] vehicletypeindex = null;
		if(myScenario.getSplitRatioProfileSet()!=null)
			vehicletypeindex = ((SplitRatioProfileSet)myScenario.getSplitRatioProfileSet()).vehicletypeindex;
		
		int j,k,in_index,out_index,dn_node_index,vt_index;
		Double value;
		for(com.relteq.sirius.jaxb.Splitratio sr : getSplitratio()){
			
			Double2DMatrix data = new Double2DMatrix(sr.getContent());
			
			// extend the profile if necessary
			int numTime = data.getnTime();
			if(numTime>profile.size())
				for(k=profile.size();k<numTime;k++)	
					profile.add(new Double4DMatrix(myNode,Double.NaN));
			
			// fold the data into the profile
			for(k=0;k<numTime;k++){
				for(j=0;j<data.getnVTypes();j++){
					
					value = data.get(k,j);
					
					// destination network index in the node
					dn_node_index = myNode.getDestinationNetworkIndex(sr.getDestinationNetworkId());	
					if(dn_node_index<0)
						continue; 
					
					// input and output link index w.r.t. this node and destination network
					in_index = myNode.getInputLinkIndex(dn_node_index,sr.getLinkIn());
					out_index = myNode.getOutputLinkIndex(dn_node_index,sr.getLinkOut());
					vt_index = vehicletypeindex[j];
					if(in_index<0 || out_index<0 || vt_index<0)
						continue; 

					profile.get(k).setValue(dn_node_index,in_index,out_index,vt_index,value);
				}
			}
		}
		
		// normalize
		for(k=0;k<profile.size();k++)
			myNode.normalizeSplitRatioMatrix(profile.get(k));
		
		// optional dt
		if(getDt()!=null){
			dtinseconds = getDt().floatValue();					// assume given in seconds
			samplesteps = SiriusMath.round(dtinseconds/myScenario.getSimDtInSeconds());
		}
		else{ 	// only allow if it contains only one fd
			if(profile.size()<=1){
				dtinseconds = Double.POSITIVE_INFINITY;
				samplesteps = Integer.MAX_VALUE;
			}
			else{
				dtinseconds = -1d;		// this triggers the validation error
				samplesteps = -1;
				return;
			}
		}
		
		currentSplitRatio = new Double4DMatrix(myNode,Double.NaN);
		
		// inform the node
		myNode.setHasSRprofile(true);
		
		
	}

	protected void reset() {
		
		// read start time, convert to stepinitial
		double starttime;
		if( getStartTime()!=null)
			starttime = getStartTime().floatValue();	// assume given in seconds
		else
			starttime = 0f;
		
		stepinitial = SiriusMath.round((starttime-myScenario.getTimeStart())/myScenario.getSimDtInSeconds());
	}

	protected void validate() {

		if(getSplitratio().isEmpty())
			return;
		
		if(myNode==null){
			SiriusErrorLog.addWarning("Unknown node with id=" + getNodeId() + " in split ratio profile.");
			return; // this profile will be skipped but does not cause invalidation.
		}
		
		// check link ids
		int index;
		for(com.relteq.sirius.jaxb.Splitratio sr : getSplitratio()){
			index = myNode.getInputLinkIndex(sr.getLinkIn());
			if(index<0)
				SiriusErrorLog.addError("Bad input link id=" + sr.getLinkIn() + " in split ratio profile with node id=" + getNodeId());

			index = myNode.getOutputLinkIndex(sr.getLinkOut());
			if(index<0)
				SiriusErrorLog.addError("Bad output link id=" + sr.getLinkOut() + " in split ratio profile with node id=" + getNodeId());

		}

		// check dtinhours
		if( dtinseconds<=0 )
			SiriusErrorLog.addError("Invalid time step =" + getDt() +  " in split ratio profile for node id=" + getNodeId());

		if(!SiriusMath.isintegermultipleof(dtinseconds,myScenario.getSimDtInSeconds()))
			SiriusErrorLog.addError("Time step = " + getDt() + " for split ratio profile of node id=" + getNodeId() + " is not a multiple of the simulation time step (" + myScenario.getSimDtInSeconds() + ")"); 
		
//		// check split ratio dimensions and values
//		int dn_index, in_index, out_index;
//		if(profile!=null)
//			for(Double4DMatrix SR : profile){
//				for(dn_index=0;dn_index<SR.getNumDNetwork();dn_index++){
//					Double3DMatrix SR3D = SR.data[dn_index];
//					
//					
//					SiriusErrorLog.addError("Split ratio profile for node id=" + getNodeId() + " does not contain values for all vehicle types: ");
//
//					
//				}
//			}
		
//		if(profile!=null)
//			for(dn_index=0;dn_index<profile.length;dn_index++)
//				if(profile[dn_index]!=null)
//					
//						if(profile[dn_index][in_index]!=null)
//							for(out_index=0;out_index<profile[dn_index][in_index].length;out_index++)
//								if(profile[dn_index][in_index][out_index]!=null)
//									if(profile[dn_index][in_index][out_index].getnVTypes()!=myScenario.getNumVehicleTypes())
	}

	protected void update() {
		if(profile==null)
			return;
		if(myNode==null)
			return;
		if(isdone)
			return;
		if(myScenario.clock.istimetosample(samplesteps,stepinitial)){
			int step = samplesteps>0 ? SiriusMath.floor((myScenario.clock.getCurrentstep()-stepinitial)/samplesteps) : 0;
			step = Math.max(0,step);
			currentSplitRatio = sampleAtTimeStep( Math.min( step ,profile.size()-1) );
			//myNode.normalizeSplitRatioMatrix(currentSplitRatio);
			myNode.setSampledSRProfile(currentSplitRatio);
			isdone = step>=profile.size()-1;
		}		
	}

	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////

	// for time sample k, returns a 3D matrix with dimensions inlink x outlink x vehicle type
	private Double4DMatrix sampleAtTimeStep(int k){
		
		if(profile==null)
			return null;
				
		int step = Math.min( Math.max(k,0) , profile.size()-1 );
		
		return profile.get(step);
		
//		
//		int i,j,lastk;
//		for(i=0;i<myNode.nIn;i++){
//			for(j=0;j<myNode.nOut;j++){
//				if(profile[i][j]==null)						// nan if not defined
//					continue;
//				if(profile[i][j].isEmpty())					// nan if no data
//					continue;
//				lastk = Math.min(k,profile[i][j].getnTime()-1);	// hold last value
//				X.setAllVehicleTypes(i,j,profile[i][j].sampleAtTime(lastk,vehicletypeindex));
//			}
//		}
//		return X;
	}

}
