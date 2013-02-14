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

/** Node class.
*
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public class Node extends edu.berkeley.path.beats.jaxb.Node {
		   
	/** @y.exclude */ 	protected Network myNetwork;
	
	// incident links
	/** @y.exclude */ 	protected Link [] output_link;
	/** @y.exclude */ 	protected Link [] input_link;
	/** @y.exclude */ 	protected int nIn;
	/** @y.exclude */ 	protected int nOut;
	/** @y.exclude */ 	protected boolean isTerminal;
	
	// split ratio matrix
	/** @y.exclude */ 	protected Double3DMatrix sampledSRprofile;
	/** @y.exclude */ 	protected Double3DMatrix splitratio;
	/** @y.exclude */ 	protected boolean istrivialsplit;
	/** @y.exclude */ 	protected boolean hasSRprofile;
	
	// control
	/** @y.exclude */ 	protected Signal mySignal = null;
	/** @y.exclude */ 	protected boolean hascontroller;
	/** @y.exclude */ 	protected boolean controlleron;
	
	// event
	/** @y.exclude */ 	protected boolean hasactivesplitevent;	// split ratios set by events take precedence over
																// controller split ratios
    // data
	/** @y.exclude */ 	protected Double [][][] inDemand;		// [ensemble][nIn][nTypes]
	/** @y.exclude */ 	protected double [][] outSupply;		// [ensemble][nOut]
	/** @y.exclude */ 	protected Double [][][] inFlow;			// [ensemble][nIn][nTypes]
	/** @y.exclude */ 	protected Double [][][] outFlow; 		// [ensemble][nOut][nTypes]

	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Node(){}
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	/** @y.exclude */ 	
	protected boolean registerController(){
		if(hascontroller)		// used to detect multiple controllers
			return false;
		else{
			hascontroller = true;
			controlleron = true;
			return true;
		}
	}
	
	/** @y.exclude */ 	
    protected void setSampledSRProfile(Double3DMatrix s){
    	sampledSRprofile = s;
    }

    /** @y.exclude */ 	
	protected void setHasSRprofile(boolean hasSRprofile) {
		if(!istrivialsplit){
			this.hasSRprofile = hasSRprofile;
			this.sampledSRprofile = new Double3DMatrix(nIn,nOut,myNetwork.myScenario.getNumVehicleTypes(),0d);
			normalizeSplitRatioMatrix(this.sampledSRprofile);	// GCG REMOVE THIS AFTER CHANGING 0->NaN
		}
	}

	/** @y.exclude */ 	
	protected void setControllerOn(boolean controlleron) {
		if(hascontroller){
			this.controlleron = controlleron;
			if(!controlleron)
				resetSplitRatio();
		}
	}

	/** @y.exclude */ 	
    protected void resetSplitRatio(){

		//splitratio = new Float3DMatrix(nIn,nOut,Utils.numVehicleTypes,1f/((double)nOut));
		
		//////
		splitratio = new Double3DMatrix(nIn,nOut,myNetwork.myScenario.getNumVehicleTypes(),0d);
		normalizeSplitRatioMatrix(splitratio);
		//////
    }
    
    /** @y.exclude */ 	
	protected void setSplitratio(Double3DMatrix x) {
		splitratio.copydata(x);
		normalizeSplitRatioMatrix(splitratio);
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate
	/////////////////////////////////////////////////////////////////////
    
	/** @y.exclude */
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

		istrivialsplit = nOut==1;
		hasSRprofile = false;
		sampledSRprofile = null;
		
		resetSplitRatio();
		
		hascontroller = false;
		controlleron = false;
		hasactivesplitevent = false;
	}
    
	/** @y.exclude */
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

	/** @y.exclude */
	protected void reset() {	
		int numVehicleTypes = myNetwork.myScenario.getNumVehicleTypes();
    	int numEnsemble = myNetwork.myScenario.numEnsemble;		
		inFlow 			= new Double[numEnsemble][nIn][numVehicleTypes];
    	inDemand 		= new Double[numEnsemble][nIn][numVehicleTypes];
		outSupply 		= new double[numEnsemble][nOut];
		outFlow 		= new Double[numEnsemble][nOut][numVehicleTypes];
	}
	
	/////////////////////////////////////////////////////////////////////
	// update
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected void update() {

        if(isTerminal)
            return;
        
        int e,i,j;        
        int numEnsemble = myNetwork.myScenario.numEnsemble;
        
        // collect input demands and output supplies ...................
        for(e=0;e<numEnsemble;e++){        
    		for(i=0;i<nIn;i++)
    			inDemand[e][i] = input_link[i].outflowDemand[e];
    		for(j=0;j<nOut;j++)
    			outSupply[e][j] = output_link[j].spaceSupply[e];
        }
        
        // in/out flow computation by subclass
        computeInOutFlows();
        
        // assign flow to input links ..................................
		for(e=0;e<numEnsemble;e++)
	        for(i=0;i<nIn;i++)
	            input_link[i].outflow[e]=inFlow[e][i];
        
        // assign flow to output links .................................
		for(e=0;e<numEnsemble;e++)
	        for (j=0;j<nOut;j++)
	            output_link[j].inflow[e] = outFlow[e][j];
	}

	/** @y.exclude */
	protected void computeInOutFlows(){
		System.out.println("Must be overriden by subclass.");
	}
	
	/////////////////////////////////////////////////////////////////////
	// operations on split ratio matrices
	/////////////////////////////////////////////////////////////////////
	
	/** @y.exclude */
	protected boolean validateSplitRatioMatrix(Double3DMatrix X){

		int i,j,k;
		Double value;
		
		// dimension
		if(X.getnIn()!=nIn || X.getnOut()!=nOut || X.getnVTypes()!=myNetwork.myScenario.getNumVehicleTypes()){
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
	
	/** @y.exclude */
    protected void normalizeSplitRatioMatrix(Double3DMatrix X){

    	int i,j,k;
		boolean hasNaN;
		int countNaN;
		int idxNegative;
		double sum;
    	
    	for(i=0;i<X.getnIn();i++)
    		for(k=0;k<myNetwork.myScenario.getNumVehicleTypes();k++){
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
	// public API
	/////////////////////////////////////////////////////////////////////

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
	public boolean hasController() {
		return hascontroller;
	}
	
	public Double [][][] getSplitRatio(){
		if(splitratio==null)
			return null;
		else{
			return splitratio.cloneData();
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
		if(splitratio==null)
			return Double.NaN;
		else{
			return splitratio.get(inLinkInd, outLinkInd, vehTypeInd);
		}
	}

}
