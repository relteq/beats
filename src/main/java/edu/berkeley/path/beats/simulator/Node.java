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

/** Node class.
*
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public class Node extends edu.berkeley.path.beats.jaxb.Node {
		   
	protected Network myNetwork;

	// connectivity
	protected int nIn;
	protected int nOut;
	protected boolean istrivialsplit;
	protected boolean isTerminal;
	
	// link references
	protected Link [] output_link;
	protected Link [] input_link;
	
	// split ratio from profile
	protected SplitRatioProfile mySplitRatioProfile;
//	private Double3DMatrix splitFromProfile;
	protected boolean hasSRprofile;
	
	// split ratio from event
	protected boolean hasactivesplitevent;	// split ratios set by events take precedence over
	
	// split ratio from profile or event
	protected Double3DMatrix splitratio_selected;
	
	// split ratio applied, after resolving unknown terms
//	protected Double3DMatrix splitratio_applied;
	
//	private Signal mySignal = null;

    // controller
//	private boolean hascontroller;
//	private boolean controlleron;
	
	// input to node model, copied from link suppy/demand
	protected Double [][][] inDemand;		// [ensemble][nIn][nTypes]
	protected double [][] outSupply;		// [ensemble][nOut]
	
	// output from node model (inDemand gets scaled)
	protected Double [][][] outFlow; 		// [ensemble][nOut][nTypes]
	
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	protected Node(){}

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
    
	protected void populate(Network myNetwork) {
    	// Note: It is assumed that this comes *before* SplitRatioProfile.populate
		
		this.myNetwork = myNetwork;
		
		nOut = 0;
		if(getOutputs()!=null){
			nOut = getOutputs().getOutput().size();
			output_link = new Link[nOut];
			for(int i=0;i<nOut;i++){
				edu.berkeley.path.beats.jaxb.Output output = getOutputs().getOutput().get(i);
				output_link[i] = myNetwork.getLinkWithId(output.getLinkId());
			}
		}

		nIn = 0;
		if(getInputs()!=null){
			nIn = getInputs().getInput().size();
			input_link = new Link[nIn];
			for(int i=0;i<nIn;i++){
				edu.berkeley.path.beats.jaxb.Input input = getInputs().getInput().get(i);
				input_link[i] = myNetwork.getLinkWithId(input.getLinkId());
			}
		}
		
		isTerminal = nOut==0 || nIn==0;

    	if(isTerminal)
    		return;

//		iscontributor = new boolean[nIn][nOut];
		istrivialsplit = nOut==1;
		hasSRprofile = false;
		
		// initialize the split ratio matrix
		//splitFromProfile = new Double3DMatrix(nIn,nOut,myNetwork.getMyScenario().getNumVehicleTypes(),0d);
		splitratio_selected = new Double3DMatrix(nIn,nOut,myNetwork.getMyScenario().getNumVehicleTypes(),0d);
		normalizeSplitRatioMatrix(splitratio_selected);

//		hascontroller = false;
//		controlleron = false;
		hasactivesplitevent = false;
	}
    
	protected void validate() {
				
		if(isTerminal)
			return;
		
		if(output_link!=null)
			for(Link link : output_link)
				if(link==null)
					BeatsErrorLog.addError("Incorrect output link id in node id=" + getId());

		if(input_link!=null)
			for(Link link : input_link)
				if(link==null)
					BeatsErrorLog.addError("Incorrect input link id in node id=" + getId());
		
		if(nIn==0)
			BeatsErrorLog.addError("No inputs into non-terminal node id=" + getId());

		if(nOut==0)
			BeatsErrorLog.addError("No outputs from non-terminal node id=" + getId());
		
	}
	
	protected void reset() {	
		int numVehicleTypes = myNetwork.getMyScenario().getNumVehicleTypes();
    	int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();		
    	inDemand 		= new Double[numEnsemble][nIn][numVehicleTypes];
		outSupply 		= new double[numEnsemble][nOut];
		outFlow 		= new Double[numEnsemble][nOut][numVehicleTypes];
	}
	
	protected void update() {
		
        if(isTerminal)
            return;

        int e,i,j;        
        int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();
        
        // collect input demands and output supplies ...................
        for(e=0;e<numEnsemble;e++){        
    		for(i=0;i<nIn;i++)
    			inDemand[e][i] = input_link[i].getOutflowDemand(e);
    		for(j=0;j<nOut;j++)
    			outSupply[e][j] = output_link[j].getSpaceSupply(e);
        }

        Double3DMatrix splitratio_applied = xxx();
        
        // compute node flows ..........................................
        computeLinkFlows(splitratio_applied);
        
        // assign flow to input links ..................................
		for(e=0;e<numEnsemble;e++)
	        for(i=0;i<nIn;i++)
	            input_link[i].setOutflow(e, inDemand[e][i]);
        
        // assign flow to output links .................................
		for(e=0;e<numEnsemble;e++)
	        for (j=0;j<nOut;j++)
	            output_link[j].setInflow(e, outFlow[e][j]);
	}


	protected Double3DMatrix xxx(){
		return null;
	}
	
//	private void xxx(){
//
//		int e,j,k;
//        int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();
//
//        
//		// solve unknown split ratios if they are non-trivial ..............
//		if(!istrivialsplit){	
//
//	        // Take current split ratio from the profile if the node is
//			// not actively controlled. Otherwise the mat has already been 
//			// set by the controller.
//			if(hasSRprofile  && !hasactivesplitevent ) //&& !controlleron)
//				splitratio_selected = this.mySplitRatioProfile.getCurrentSplitRatio();
////				splitratio.copydata(splitFromProfile);
//			
//	        // compute known output demands ................................
//			for(e=0;e<numEnsemble;e++)
//		        for(j=0;j<nOut;j++){
//		        	outDemandKnown[e][j] = 0f;
//		        	for(i=0;i<nIn;i++)
//		        		for(k=0;k<myNetwork.getMyScenario().getNumVehicleTypes();k++)
//		        			if(!splitratio_selected.get(i,j,k).isNaN())
//		        				outDemandKnown[e][j] += splitratio_selected.get(i,j,k) * inDemand[e][i][k];
//		        }
//	        
//	        // compute and sort output demand/supply ratio .................
//			for(e=0;e<numEnsemble;e++)
//		        for(j=0;j<nOut;j++)
//		        	dsratio[e][j] = outDemandKnown[e][j] / outSupply[e][j];
//	                
//	        // fill in unassigned split ratios .............................
//			splitratio_applied = resolveUnassignedSplits_A(splitratio_selected);
//		}
//		else
//			splitratio_applied = new Double3DMatrix(getnIn(),getnOut(),getMyNetwork().getMyScenario().getNumVehicleTypes(),1d);
//		
//		
//	}
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	// split ratio profile ..............................................

	protected void setMySplitRatioProfile(SplitRatioProfile mySplitRatioProfile) {
		this.mySplitRatioProfile = mySplitRatioProfile;
		if(!istrivialsplit){
			this.hasSRprofile = true;
			//this.splitFromProfile = new Double3DMatrix(nIn,nOut,myNetwork.getMyScenario().getNumVehicleTypes(),0d);
			//normalizeSplitRatioMatrix(this.splitFromProfile);	// GCG REMOVE THIS AFTER CHANGING 0->NaN
		}
	}
	
//	protected void setHasSRprofile(boolean hasSRprofile) {
//		if(!istrivialsplit){
//			this.hasSRprofile = hasSRprofile;
//			this.splitFromProfile = new Double3DMatrix(nIn,nOut,myNetwork.getMyScenario().getNumVehicleTypes(),0d);
//			normalizeSplitRatioMatrix(this.splitFromProfile);	// GCG REMOVE THIS AFTER CHANGING 0->NaN
//		}
//	}

//	protected void setSampledSRProfile(Double3DMatrix s){
//    	splitFromProfile = s;;
//    }
	
	// controllers ......................................................

//	protected void setControllerOn(boolean controlleron) {
//		if(hascontroller){
//			this.controlleron = controlleron;
//			if(!controlleron)
//				resetSplitRatio();
//		}
//	}
//
//	protected boolean registerController(){
//		if(hascontroller)		// used to detect multiple controllers
//			return false;
//		else{
//			hascontroller = true;
//			controlleron = true;
//			return true;
//		}
//	}
	
	// events ..........................................................

	// used by Event.setNodeEventSplitRatio
	protected void applyEventSplitRatio(Double3DMatrix x) {		
		splitratio_selected.copydata(x);
		normalizeSplitRatioMatrix(splitratio_selected);
		hasactivesplitevent = true;
	}

	// used by Event.revertNodeEventSplitRatio
	protected void removeEventSplitRatio() {
//		splitratio = new Double3DMatrix(nIn,nOut,myNetwork.getMyScenario().getNumVehicleTypes(),0d);
//		normalizeSplitRatioMatrix(splitratio);
		hasactivesplitevent = false;
	}
	
	// used by Event.revertNodeEventSplitRatio
	protected boolean isHasActiveSplitEvent() {
		return hasactivesplitevent;
	}

	// used by Event.setNodeEventSplitRatio
    protected Double3DMatrix getSplitratio() {
		return splitratio_selected;
	}
    
	/////////////////////////////////////////////////////////////////////
	// operations on split ratio matrices
	/////////////////////////////////////////////////////////////////////
	
	protected boolean validateSplitRatioMatrix(Double3DMatrix X){

		int i,j,k;
		Double value;
		
		// dimension
		if(X.getnIn()!=nIn || X.getnOut()!=nOut || X.getnVTypes()!=myNetwork.getMyScenario().getNumVehicleTypes()){
			BeatsErrorLog.addError("Split ratio for node " + getId() + " has incorrect dimensions.");
			return false;
		}
		
		// range
		for(i=0;i<X.getnIn();i++){
			for(j=0;j<X.getnOut();j++){
				for(k=0;k<X.getnVTypes();k++){
					value = X.get(i,j,k);
					if( !value.isNaN() && (value>1 || value<0) ){
						BeatsErrorLog.addError("Invalid split ratio values for node id=" + getId());
						return false;
					}
				}
			}
		}
		return true;
	}
	
    protected void normalizeSplitRatioMatrix(Double3DMatrix X){

    	int i,j,k;
		boolean hasNaN;
		int countNaN;
		int idxNegative;
		double sum;
    	
    	for(i=0;i<X.getnIn();i++)
    		for(k=0;k<myNetwork.getMyScenario().getNumVehicleTypes();k++){
				hasNaN = false;
				countNaN = 0;
				idxNegative = -1;
				sum = 0.0f;
				for (j = 0; j < X.getnOut(); j++)
					if (X.get(i,j,k).isNaN()) {
						countNaN++;
						idxNegative = j;
						if (countNaN > 1)
							hasNaN = true;
					}
					else
						sum += X.get(i,j,k);
				
				if (countNaN==1) {
					X.set(i,idxNegative,k,Math.max(0f, (1-sum)));
					sum += X.get(i,idxNegative,k);
				}
				
				if ( !hasNaN && BeatsMath.equals(sum,0.0) ) {	
					X.set(i,0,k,1d);
					//for (j=0; j<n2; j++)			
					//	data[i][j][k] = 1/((double) n2);
					continue;
				}
				
				if ((!hasNaN) && (sum<1.0)) {
					for (j=0;j<X.getnOut();j++)
						X.set(i,j,k,(double) (1/sum) * X.get(i,j,k));
					continue;
				}
				
				if (sum >= 1.0)
					for (j=0; j<X.getnOut(); j++)
						if (X.get(i,j,k).isNaN())
							X.set(i,j,k,0d);
						else
							X.set(i,j,k,(double) (1/sum) * X.get(i,j,k));
    		}
    }
    
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
    protected void computeLinkFlows(final Double3DMatrix sr){
    	
    }
    
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

    public boolean isTerminal() {
		return isTerminal;
	}

	/** network that containts this node */ 	
	public Network getMyNetwork() {
		return myNetwork;
	}
	    
	/** List of links exiting this node */ 
    public Link[] getOutput_link() {
		return output_link;
	}

    /** List of links entering this node */ 
	public Link[] getInput_link() {
		return input_link;
	}

    /** Index of link with given id in the list of input links of this node */ 
	public int getInputLinkIndex(String id){
		if(id==null)
			return -1;
		for(int i=0;i<getnIn();i++){
			if(input_link[i]!=null)
				if(input_link[i].getId().equals(id))
					return i;
		}
		return -1;
	}
	
    /** Index of link with given id in the list of output links of this node */ 
	public int getOutputLinkIndex(String id){
		if(id==null)
			return -1;
		for(int i=0;i<getnOut();i++){
			if(output_link[i]!=null)
				if(output_link[i].getId().equals(id))
					return i;
		}
		return -1;
	}
	
    /** Number of links entering this node */ 
	public int getnIn() {
		return nIn;
	}

    /** Number of links exiting this node */ 
	public int getnOut() {
		return nOut;
	}
    
    /** <code>true</code> iff there is a split ratio controller attached to this link */
//	public boolean hasController() {
//		return hascontroller;
//	}
	
	/** ADDED TEMPORARILY FOR MANUEL'S DTA WORK 
	 * @throws BeatsException */
//	public void setSplitRatioMatrix(double [][][] x) throws BeatsException {
//		if(x.length!=splitratio.getnIn())
//			throw new BeatsException("Node.setSplitRatioMatrix, bad first dimension.");
//		if(x[0].length!=splitratio.getnOut())
//			throw new BeatsException("Node.setSplitRatioMatrix, bad second dimension.");
//		if(x[0][0].length!=splitratio.getnVTypes())
//			throw new BeatsException("Node.setSplitRatioMatrix, bad third dimension.");
//		int i,j,k;
//		for(i=0;i<splitratio.getnIn();i++)
//			for(j=0;j<splitratio.getnOut();j++)
//				for(k=0;k<splitratio.getnVTypes();k++)
//					splitratio.set(i, j, k, x[i][j][k]);
//		normalizeSplitRatioMatrix(splitratio);
//	}
	
	public Double [][][] getSplitRatio(){
		if(splitratio_selected==null)
			return null;
		else{
			return splitratio_selected.cloneData();
		}
	}

	/**
	 * Retrieves a split ratio for the given input/output link pair and vehicle type
	 * @param inLinkInd input link index
	 * @param outLinkInd output link index
	 * @param vehTypeInd vehicle type index
	 * @return the split ratio
	 */
	public Double getSplitRatio(int inLinkInd, int outLinkInd, int vehTypeInd) {
		if(splitratio_selected==null)
			return Double.NaN;
		else{
			return splitratio_selected.get(inLinkInd, outLinkInd, vehTypeInd);
		}
	}

}
