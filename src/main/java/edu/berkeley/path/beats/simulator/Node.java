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

import edu.berkeley.path.beats.simulator.Node_FlowSolver.SupplyDemand;

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
	protected boolean hasSRprofile;
	
	// split ratio from event
	protected boolean hasactivesplitevent;	// split ratios set by events take precedence over
	
	// split ratio from profile or event
	private Double3DMatrix splitratio_selected;
	
	// node behavior
	protected Node_SplitRatioSolver node_sr_solver;
	protected Node_FlowSolver node_flow_solver;
	
//	private Signal mySignal = null;

    // controller
//	private boolean hascontroller;
//	private boolean controlleron;
	
	// output from node model (inDemand gets scaled)
	//protected Double [][][] outFlow; 		// [ensemble][nOut][nTypes]
	
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

		istrivialsplit = nOut==1;
		hasSRprofile = false;
		
		// initialize the split ratio matrix
		splitratio_selected = new Double3DMatrix(nIn,nOut,myNetwork.getMyScenario().getNumVehicleTypes(),0d);
		normalizeSplitRatioMatrix(splitratio_selected);

//		hascontroller = false;
//		controlleron = false;
		hasactivesplitevent = false;
		
		// create node flow solver
		switch(getMyNetwork().getMyScenario().getNodeFlowSolver()){
			case proportional:
				node_flow_solver = new Node_FlowSolver_LNCTM(this);
				break;
			case symmetric:
				node_flow_solver = new Node_FlowSolver_Symmetric(this);
				break;
		}
		
		// create node split ratio solver
		switch(getMyNetwork().getMyScenario().getNodeSRSolver()){
			case A:
				node_sr_solver = new Node_SplitRatioSolver_A(this);
				break;
			case B:
				node_sr_solver = new Node_SplitRatioSolver_B(this);
				break;
			case C:
				node_sr_solver = new Node_SplitRatioSolver_C(this);
				break;
		}
		
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
    	if(isTerminal)
    		return;
		node_flow_solver.reset();
		node_sr_solver.reset();
	}
	
	protected void update() {
		
        if(isTerminal)
            return;

        int e,i,j;        
        int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();
        int numVehicleTypes = myNetwork.getMyScenario().getNumVehicleTypes();
        
        // collect input demands and output supplies ...................
        Node_FlowSolver.SupplyDemand demand_supply = new SupplyDemand(numEnsemble,nIn,nOut,numVehicleTypes);
        for(e=0;e<numEnsemble;e++){        
    		for(i=0;i<nIn;i++)
    			demand_supply.setDemand(e,i, input_link[i].getOutflowDemand(e) );
    		for(j=0;j<nOut;j++)
    			demand_supply.setSupply(e,j,output_link[j].getSpaceSupply(e));
        }
        
        // Select a split ratio from profile, event, or controller
		if(!istrivialsplit){
			if(hasSRprofile && !hasactivesplitevent) // && !controlleron
				splitratio_selected = mySplitRatioProfile.getCurrentSplitRatio();
		}
		else
			splitratio_selected = new Double3DMatrix(getnIn(),getnOut(),getMyNetwork().getMyScenario().getNumVehicleTypes(),1d);
		
        // compute applied split ratio matrix
        Double3DMatrix splitratio_applied = node_sr_solver.computeAppliedSplitRatio(splitratio_selected,demand_supply);
        
        // compute node flows ..........................................
        Node_FlowSolver.IOFlow IOflow = node_flow_solver.computeLinkFlows(splitratio_applied,demand_supply);
        
        if(IOflow==null)
        	return;
        	
        // assign flow to input links ..................................
		for(e=0;e<numEnsemble;e++)
	        for(i=0;i<nIn;i++)
	            input_link[i].setOutflow(e,IOflow.getIn(e,i));
        
        // assign flow to output links .................................
		for(e=0;e<numEnsemble;e++)
	        for (j=0;j<nOut;j++)
	            output_link[j].setInflow(e,IOflow.getOut(e,j));
	}

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
//
//	protected class SupplyDemand {
//		// input to node model, copied from link suppy/demand
//		protected Double [][][] demand;		// [ensemble][nIn][nTypes]
//		protected double [][] supply;		// [ensemble][nOut]
//		
//		public SupplyDemand(int numEnsemble,int nIn,int nOut,int numVehicleTypes) {
//			super();
//	    	demand = new Double[numEnsemble][nIn][numVehicleTypes];
//			supply = new double[numEnsemble][nOut];
//		}
//		
//		public void setDemand(int nE,int nI,Double [] val){
//			demand[nE][nI] = val;
//		}
//		
//		public void setSupply(int nE,int nO, double val){
//			supply[nE][nO]=val;
//		}
//		
//		public double getDemand(int nE,int nI,int nK){
//			return demand[nE][nI][nK];
//		}
//		
//		public double getSupply(int nE,int nO){
//			return supply[nE][nO];
//		}
//
//		public double [] getSupply(int nE){
//			return supply[nE];
//		}
//	}
	
//	protected class IOFlow {
//		// input to node model, copied from link suppy/demand
//		protected Double [][][] in;		// [ensemble][nIn][nTypes]
//		protected Double [][][] out;	// [ensemble][nOut][nTypes]
//		
//		public IOFlow(int numEnsemble,int nIn,int nOut,int numVehicleTypes) {
//			super();
//	    	in = new Double[numEnsemble][nIn][numVehicleTypes];
//			out = new Double[numEnsemble][nOut][numVehicleTypes];
//		}
//
//		public void setIn(int nE,int nI,int nV,double val){
//			in[nE][nI][nV] = val;
//		}
//		
//		public void setOut(int nE,int nO,int nV,double val){
//			out[nE][nO][nV]=val;
//		}
//		
//		public Double [] getIn(int nE,int nI){
//			return in[nE][nI];
//		}
//
//		public double getIn(int nE,int nI,int nV){
//			return in[nE][nI][nV];
//		}
//		
//		public Double [] getOut(int nE,int nO){
//			return out[nE][nO];
//		}
//		
//		public void addOut(int nE,int nO,int nV,double val){
//			out[nE][nO][nV] += val;
//		}
//		
//	}
	
}
