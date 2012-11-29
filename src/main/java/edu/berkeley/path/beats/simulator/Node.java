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
import java.util.Arrays;

import edu.berkeley.path.beats.simulator.DestinationNetworkBLA;

// NOTE
// SEE IF THE ENSEMBLE DIMENSION CAN BE ELIMINATED FOR OUTDEMANDKNOWN AND DSRATIO

/** Node class.
*
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public final class Node extends edu.berkeley.path.beats.jaxb.Node {
		   
	/** @y.exclude */ 	protected Network myNetwork;

	// connectivity
	/** @y.exclude */ 	protected Link [] output_link;
	/** @y.exclude */ 	protected Link [] input_link;
	/** @y.exclude */ 	protected int nIn;
	/** @y.exclude */ 	protected int nOut;
	/** @y.exclude */ 	protected boolean isTerminal;
	/** @y.exclude */ 	protected boolean isSingleOut;
	
	// split ratios
	/** @y.exclude */ 	protected Double4DMatrix sampledSRprofile;
	/** @y.exclude */ 	protected Double4DMatrix splitratio;
	/** @y.exclude */ 	protected boolean hasSRprofile;
	
	// destination networks (populated by DestinationNetwork.populate)
	/** @y.exclude */ 	protected int numDNetworks = 0;
	/** @y.exclude */ 	protected ArrayList<Integer> myDNGlobalIndex = new ArrayList<Integer>();	// list of DN that use this node, including background
	/** @y.exclude */   protected ArrayList<ArrayList<Integer>> dn2outlinkindex = new ArrayList<ArrayList<Integer>>();	// list of indices to output links in output_link used by each d.n.
	/** @y.exclude */   protected ArrayList<ArrayList<Integer>> dn2inlinkindex = new ArrayList<ArrayList<Integer>>();		// list of indices to inpt links in input_link used by each d.n.
	/** @y.exclude */ 	protected ArrayList<Boolean> dn_isSingleOut = new ArrayList<Boolean>();	// [dnindex] true if there is one in and one out
	
	// signal
	/** @y.exclude */ 	protected Signal mySignal = null;

    // controller
	/** @y.exclude */ 	protected boolean hascontroller;
	/** @y.exclude */ 	protected boolean controlleron;
	
	// split event
	/** @y.exclude */ 	protected boolean hasactivesplitevent;	// split ratios set by events take precedence over
																// controller split ratios
    // used in update()
	/** @y.exclude */ 	protected double [][][][] inDemand;		// [ensemble][nIn][numDN][nTypes]
	/** @y.exclude */ 	protected double [][] outSupply;		// [ensemble][nOut]
	/** @y.exclude */ 	protected double [][] outDemandKnown;	// [ensemble][nOut]
	/** @y.exclude */ 	protected double [][] dsratio;			// [ensemble][nOut]
	/** @y.exclude */ 	protected double [][][][] outFlow; 		// [ensemble][nOut][numDN][nTypes]
	/** @y.exclude */ 	protected boolean [][] iscontributor;	// [nIn][nOut]
	/** @y.exclude */ 	protected ArrayList<Integer> unknownind = new ArrayList<Integer>();		// [unknown splits]
	/** @y.exclude */ 	protected ArrayList<Double> unknown_dsratio = new ArrayList<Double>();	// [unknown splits]	
	/** @y.exclude */ 	protected ArrayList<Integer> minind_to_nOut= new ArrayList<Integer>();	// [min unknown splits]
	/** @y.exclude */ 	protected ArrayList<Integer> minind_to_unknown= new ArrayList<Integer>();	// [min unknown splits]
	/** @y.exclude */ 	protected ArrayList<Double> sendtoeach = new ArrayList<Double>();			// [min unknown splits]

	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Node(){}
							  
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	/** @y.exclude */ 
	protected void addDestinationNetwork(int dest_index,ArrayList<Link> inlinks,ArrayList<Link> outlinks){
		
		if(isTerminal)
			return;

		// special case for destination networks (signaled by inlinks==null && outlinks==null)
		if(inlinks==null)
			inlinks = new ArrayList<Link>(Arrays.asList(input_link));
		if(outlinks==null)
			outlinks = new ArrayList<Link>(Arrays.asList(output_link));
			
		numDNetworks++;
		myDNGlobalIndex.add(dest_index);
		dn_isSingleOut.add(outlinks.size()==1);
		
		// find indices for input and output links in this destination network
		boolean foundit;
		
		// input links
		ArrayList<Integer> inlink_index = new ArrayList<Integer>();
		for(Link link : inlinks){
			foundit=false;
			for(int i=0;i<input_link.length;i++){
				if(input_link[i]!=null)
					if(input_link[i].getId().equals(link.getId())){
						foundit = true;
						inlink_index.add(i);
						break;
					}
			}
			if(!foundit)
				inlink_index.add(-1);
		}
		
		// output links
		ArrayList<Integer> outlink_index = new ArrayList<Integer>();
		for(Link link : outlinks){
			foundit=false;
			for(int i=0;i<output_link.length;i++){
				if(output_link[i]!=null)
					if(output_link[i].getId().equals(link.getId())){
						foundit = true;
						outlink_index.add(i);
						break;
					}
			}
			if(!foundit)
				outlink_index.add(-1);
		}

		// add them to the node
		dn2inlinkindex.add(inlink_index);
		dn2outlinkindex.add(outlink_index);
	}
	
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
    protected void setSampledSRProfile(Double4DMatrix s){
    	sampledSRprofile = s;
    }

    /** @y.exclude */ 	
	protected void setHasSRprofile(boolean hasSRprofile) {
		this.hasSRprofile = hasSRprofile;
		this.sampledSRprofile = new Double4DMatrix(this,Double.NaN);
		//normalizeSplitRatioMatrix(this.sampledSRprofile);	// GCG REMOVE THIS AFTER CHANGING 0->NaN
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
		splitratio = new Double4DMatrix(this,Double.NaN);
		normalizeSplitRatioMatrix(splitratio);
    }
    
    /** @y.exclude */ 	
	protected void setSplitratio(Double4DMatrix x) throws SiriusException {
		splitratio.copydata(x);
		normalizeSplitRatioMatrix(splitratio);
	}
	
	/** Returns the index of the link in this.dn2inlinkindex
	 * 
	 * @param dn_index
	 * @param linkid
	 * @return
	 */
	protected int getInputLinkIndex(int dn_index,String linkid){
		try {
			for(int i=0;i<dn2inlinkindex.get(dn_index).size();i++){
				int index = dn2inlinkindex.get(dn_index).get(i);
				if(input_link[index].getId().equals(linkid))
					return i;	
			}
			return -1;
		} catch (Exception e) {
			return -1;
		}
	}
	
	/** Returns the index of the link in input_link
	 * 
	 * @param linkid
	 * @return
	 */
	protected int getInputLinkIndex(String linkid){
		if(input_link==null)
			return -1;
		for(int i=0;i<input_link.length;i++){
			if(input_link[i].getId().equals(linkid))
				return i;	
		}
		return -1;
	}

	protected int getNumInputLink(int dn_index){
		try{
			return dn2inlinkindex.get(dn_index).size();
		} catch (Exception e) {
			return -1;
		}
	}
	
	/** Returns the index of the link in this.dn2outlinkindex
	 * 
	 * @param dn_index
	 * @param linkid
	 * @return
	 */
	protected int getOutputLinkIndex(int dn_index,String linkid){
		try {
			for(int i=0;i<dn2outlinkindex.get(dn_index).size();i++){
				int index = dn2outlinkindex.get(dn_index).get(i);
				if(output_link[index].getId().equals(linkid))
					return i;	
			}
			return -1;
		} catch (Exception e) {
			return -1;
		}
	}
	
	/** Returns the index of the link in output_link
	 * 
	 * @param linkid
	 * @return
	 */
	protected int getOutputLinkIndex(String linkid){
		if(output_link==null)
			return -1;
		for(int i=0;i<output_link.length;i++){
			if(output_link[i].getId().equals(linkid))
				return i;	
		}
		return -1;
	}
	
	protected int getNumOutputLink(int dn_index){
		try{
			return dn2outlinkindex.get(dn_index).size();
		} catch (Exception e) {
			return -1;
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
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
    	
		isSingleOut = nOut==1;

//        // add background destination
//        if(myNetwork.myScenario.has_background_flow){
//        	numDNetworks++;
//        	myDNGlobalIndex.add(-1); // -1 means background flow
//        	
//        	// background has all indices
//        	ArrayList<Integer> outlinks = new ArrayList<Integer>();
//        	for(int i=0;i<output_link.length;i++)
//        		outlinks.add(i);
//        	dn2outlinkindex.add(outlinks);
//
//        	ArrayList<Integer> inlinks = new ArrayList<Integer>();
//        	for(int i=0;i<input_link.length;i++)
//        		inlinks.add(i);
//        	dn2inlinkindex.add(inlinks);
//        	
//        	dn_isSingleOut.add(output_link.length==1);
//        }
        
		iscontributor 		= new boolean[nIn][nOut];
		hasSRprofile 		= false;
		sampledSRprofile 	= null;
		hascontroller 		= false;
		controlleron 		= false;
		hasactivesplitevent = false;
	}
    
	/** @y.exclude */ 	
	protected void validate() {
				
		if(isTerminal)
			return;
		
		// TEMPORARY WHILE THERE IS NO PROCEDURE FOR UNKNOWN SPLITS
		if(!hasSRprofile)
			for(Boolean b:dn_isSingleOut)
				if(!b)
					SiriusErrorLog.addError("No split ratio profile assigned to node id=" + getId());
		
		if(output_link!=null)
			for(Link link : output_link)
				if(link==null)
					SiriusErrorLog.addError("Incorrect output link id in node id=" + getId());

		if(input_link!=null)
			for(Link link : input_link)
				if(link==null)
					SiriusErrorLog.addError("Incorrect input link id in node id=" + getId());
		
		if(nIn==0)
			SiriusErrorLog.addError("No inputs into non-terminal node id=" + getId());

		if(nOut==0)
			SiriusErrorLog.addError("No outputs from non-terminal node id=" + getId());
		
	}

	/** @y.exclude */ 	
	protected void update() {
		
        if(isTerminal)
            return;

        int e,d,i,j;  
        int node_dn_index;
        int numEnsemble = myNetwork.myScenario.numEnsemble;
        
        // collect input demands and output supplies ...................
        for(e=0;e<numEnsemble;e++){     

        	// step through input links, attach the flow demand to the appropriate node channel
    		for(i=0;i<nIn;i++){
    			for(d=0;d<input_link[i].numDNetworks;d++){
    				node_dn_index = input_link[i].dn_endNodeMap.get(d);
    				inDemand[e][i][node_dn_index] = input_link[i].outflowDemand[e][d];
    			}
    		}
 
        	// same for output links and supplies
    		for(j=0;j<nOut;j++)
    			outSupply[e][j] = output_link[j].spaceSupply[e];
        }

        // Take current split ratio from the profile if the node is
		// not actively controlled. Otherwise the mat has already been 
		// set by the controller.
		if(!isSingleOut && hasSRprofile && !controlleron && !hasactivesplitevent)
			splitratio.copydata(sampledSRprofile);
		
		// solve unknown split ratios if they are non-trivial ..............
        /* 
         * NOTE (GG,8/20/2012): TEMPORARILY COMMENTED OUT. 
         * STILL NEED TO THINK ABOUT HOW TO DO THIS WITH DESTINATION NETWORKS. 
         * 
		if(!istrivialsplit){	


			
	        // compute known output demands ................................
			for(e=0;e<numEnsemble;e++)
		        for(j=0;j<nOut;j++){
		        	outDemandKnown[e][j] = 0f;
		        	for(i=0;i<nIn;i++)
		        		for(k=0;k<myNetwork.myScenario.getNumVehicleTypes();k++)
		        			if(!splitratio.get(i,j,k).isNaN())
		        				outDemandKnown[e][j] += splitratio.get(i,j,k) * inDemand[e][i][k];
		        }
	        
	        // compute and sort output demand/supply ratio .................
			for(e=0;e<numEnsemble;e++)
		        for(j=0;j<nOut;j++)
		        	dsratio[e][j] = outDemandKnown[e][j] / outSupply[e][j];
	                
	        // fill in unassigned split ratios .............................
	        resolveUnassignedSplits_A();
		}
		*/
		
        // compute node flows ..........................................
        computeLinkFlows();
        
        // assign flow to input and output links ..................................
		for(e=0;e<numEnsemble;e++){

			for(i=0;i<nIn;i++){
				for(d=0;d<input_link[i].numDNetworks;d++){
					node_dn_index = input_link[i].dn_endNodeMap.get(d);							
					input_link[i].outflow[e][d] = inDemand[e][i][node_dn_index];
				}
			}
			
	        for (j=0;j<nOut;j++){
				for(d=0;d<output_link[j].numDNetworks;d++){
					node_dn_index = output_link[j].dn_beginNodeMap.get(d);
		            output_link[j].inflow[e][d] = outFlow[e][j][node_dn_index];
				}
	        }
			
		}
        
//        // assign flow to input and output links ..................................
//		for(e=0;e<numEnsemble;e++)
//	        for(i=0;i<nIn;i++)
//	            input_link[i].outflow[e]=inDemand[e][i];
//        
//        // assign flow to output links .................................
//		for(e=0;e<numEnsemble;e++)
//	        for (j=0;j<nOut;j++)
//	            output_link[j].inflow[e] = outFlow[e][j];
	}

	/** @y.exclude */ 	
	protected void reset() {	

        if(isTerminal)
            return;
        
		int numVehicleTypes = myNetwork.myScenario.getNumVehicleTypes();
    	int numEnsemble = myNetwork.myScenario.numEnsemble;	
    	
    	inDemand 		= SiriusMath.zeros(numEnsemble,nIn,numDNetworks,numVehicleTypes);
		outSupply 		= SiriusMath.zeros(numEnsemble,nOut);
		outDemandKnown 	= SiriusMath.zeros(numEnsemble,nOut);
		dsratio 		= SiriusMath.zeros(numEnsemble,nOut);
		outFlow 		= SiriusMath.zeros(numEnsemble,nOut,numDNetworks,numVehicleTypes);
		resetSplitRatio();
	}

	/////////////////////////////////////////////////////////////////////
	// operations on split ratio matrices
	/////////////////////////////////////////////////////////////////////
	
	/** Used to check slit ratio events.
	 * Check a split ratio matrix for dimencios and range.
	 * I am removing it because it does not work in the context of destination networks, since the dimension check no longer applies;
	 * @y.exclude 
	protected boolean validateSplitRatioMatrix(Double4DMatrix SR){

		int d,i,j,k;
		Double value;
		
		for(d=0;d<SR.getNumDNetwork();d++){
			
			Double3DMatrix X = SR.data[d];
			
			// dimension
			if(X.getnIn()!=nIn || X.getnOut()!=nOut || X.getnVTypes()!=myNetwork.myScenario.getNumVehicleTypes()){
				SiriusErrorLog.addError("Split ratio for node " + getId() + " has incorrect dimensions.");
				return false;
			}
			
			// range
			for(i=0;i<X.getnIn();i++){
				for(j=0;j<X.getnOut();j++){
					for(k=0;k<X.getnVTypes();k++){
						value = X.get(i,j,k);
						if( !value.isNaN() && (value>1 || value<0) ){
							SiriusErrorLog.addError("Invalid split ratio values for node id=" + getId());
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
	*/
	
	/** Goes through the 4D matrix and replaces single NaNs in each row with 
	 * 1-sum(row). Also it scales the row sum to 1. Multiple NaNs are left unaltered.
	 * @y.exclude */ 	
    protected boolean normalizeSplitRatioMatrix(Double4DMatrix SR){

    	int d,i,j,k;
		boolean anyHasNaN = false;
		int countNaN;
		int idxNegative;
		double sum;
		
		for(d=0;d<SR.getNumDNetwork();d++){
			Double3DMatrix X = SR.data[d];
			
			for(i=0;i<X.getnIn();i++){
	    		for(k=0;k<myNetwork.myScenario.getNumVehicleTypes();k++){
	    			boolean thisHasNaN = false;
					countNaN = 0;
					idxNegative = -1;
					sum = 0.0f;
					for (j = 0; j < X.getnOut(); j++){
						if (Double.isNaN(X.get(i,j,k))) {
							countNaN++;
							idxNegative = j;
							if (countNaN > 1)
								thisHasNaN = true;
						}
						else
							sum += X.get(i,j,k);
					}
					
					anyHasNaN |= thisHasNaN;
					
					if (countNaN==1) {
						X.set(i,idxNegative,k,Math.max(0f, (1-sum)));
						sum += X.get(i,idxNegative,k);
					}
					
					if ( !thisHasNaN && SiriusMath.equals(sum,0.0) ) {	
						X.set(i,0,k,1d);
						//for (j=0; j<n2; j++)			
						//	data[i][j][k] = 1/((double) n2);
						continue;
					}
					
					if ((!thisHasNaN) && (sum<1.0)) {
						for (j=0;j<X.getnOut();j++)
							X.set(i,j,k,(double) (1/sum) * X.get(i,j,k));
						continue;
					}
					
					if (sum >= 1.0){
						for (j=0; j<X.getnOut(); j++)
							if (Double.isNaN(X.get(i,j,k)))
								X.set(i,j,k,0d);
							else
								X.set(i,j,k,(double) (1/sum) * X.get(i,j,k));
					}
	    		}
			}
		}
		return anyHasNaN;
    }
    
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	private void computeLinkFlows(){
        
    	int e,d,i,j,k;
        int i_index,o_index;

    	int numEnsemble = myNetwork.myScenario.numEnsemble;
    	int numVehicleTypes = myNetwork.myScenario.getNumVehicleTypes();

        // input i contributes to output j .............................
    	for(i=0;i<nIn;i++)
        	for(j=0;j<nOut;j++)
        		iscontributor[i][j] = isSingleOut ? true : false;
    	
    	if(!isSingleOut)
	        for(d=0;d<numDNetworks;d++){
	        	for(j=0;j<dn2outlinkindex.get(d).size();j++){
	        		o_index = dn2outlinkindex.get(d).get(j);
	        		for(i=0;i<dn2inlinkindex.get(d).size();i++){
	        			i_index = dn2inlinkindex.get(d).get(i);
	        			double value = splitratio.getSumOverTypes(d,i,j);
	        			iscontributor[i_index][o_index] |= value>0;
	        		}
	        	}
	        }
        
        double [] applyratio = new double[nIn];

        // for each ensemble...
        for(e=0;e<numEnsemble;e++){

        	// initialize applyratio
	        for(i=0;i<nIn;i++)
	        	applyratio[i] = Double.NEGATIVE_INFINITY;
	        
	        // compute total demand on each output
	        for(j=0;j<nOut;j++)        	
				outDemandKnown[e][j] = 0d;
	        for(d=0;d<numDNetworks;d++){
	        	for(j=0;j<dn2outlinkindex.get(d).size();j++){
	        		o_index = dn2outlinkindex.get(d).get(j);
	        		for(i=0;i<dn2inlinkindex.get(d).size();i++){
	        			i_index = dn2inlinkindex.get(d).get(i);
		            	for(k=0;k<numVehicleTypes;k++)
		            		if(dn_isSingleOut.get(d))
		            			outDemandKnown[e][o_index] += inDemand[e][i_index][d][k];
		            		else
		            			outDemandKnown[e][o_index] += inDemand[e][i_index][d][k]*splitratio.getValue(d,i,j,k);
	        		}
	        	}
	        }

	        for(j=0;j<nOut;j++){
	            // compute and sort output demand/supply ratio .............
	            if(SiriusMath.greaterthan(outSupply[e][j],0d))
	            	dsratio[e][j] = Math.max( outDemandKnown[e][j] / outSupply[e][j] , 1d );
	            else
	            	dsratio[e][j] = 1d;
	            
	            // reflect ratios back on inputs
	            for(i=0;i<nIn;i++)
	            	if(iscontributor[i][j])
	            		applyratio[i] = Math.max(dsratio[e][j],applyratio[i]);
	            
	        }
	        
	        // scale down input demands	                               
	        for(i=0;i<nIn;i++)
	        	for(d=0;d<numDNetworks;d++)
		            for(k=0;k<numVehicleTypes;k++)
		                inDemand[e][i][d][k] /= applyratio[i];
        	
	        // compute out flows ...........................................  
	        for(j=0;j<nOut;j++)
	        	for(d=0;d<numDNetworks;d++)
		        	for(k=0;k<numVehicleTypes;k++)
		        		outFlow[e][j][d][k] = 0d;
	        
	        for(d=0;d<numDNetworks;d++){
	        	for(j=0;j<dn2outlinkindex.get(d).size();j++){
	        		o_index = dn2outlinkindex.get(d).get(j);
	        		for(i=0;i<dn2inlinkindex.get(d).size();i++){
	        			i_index = dn2inlinkindex.get(d).get(i);	
	        			for(k=0;k<numVehicleTypes;k++)
	        				if(dn_isSingleOut.get(d))
	        					outFlow[e][o_index][d][k] += inDemand[e][i_index][d][k];
	        				else
	        					outFlow[e][o_index][d][k] += inDemand[e][i_index][d][k]*splitratio.getValue(d,i,j,k);
	        		}
	        	}
	        }
        }
        
        
//        for(e=0;e<numEnsemble;e++)
//	        for(i=0;i<nIn;i++)
//	        	applyratio[e][i] = Double.NEGATIVE_INFINITY;
        
//        for(e=0;e<numEnsemble;e++)
//	        for(j=0;j<nOut;j++){
//	        	
//	        	// re-compute known output demands .........................
//				outDemandKnown[e][j] = 0d;
//	            for(i=0;i<nIn;i++)
//	            	for(k=0;k<numVehicleTypes;k++)
//	           		outDemandKnown[e][j] += inDemand[e][i][k]*splitratio.get(i,j,k);
//	            
//	            // compute and sort output demand/supply ratio .............
//	            if(SiriusMath.greaterthan(outSupply[e][j],0d))
//	            	dsratio[e][j] = Math.max( outDemandKnown[e][j] / outSupply[e][j] , 1d );
//	            else
//	            	dsratio[e][j] = 1d;
//	            
//	            // reflect ratios back on inputs
//	            for(i=0;i<nIn;i++)
//	            	if(iscontributor[i][j])
//	            		applyratio[e][i] = Math.max(dsratio[e][j],applyratio[e][i]);
//	            	
//	        }
//
//        // scale down input demands
//        for(e=0;e<numEnsemble;e++)
//	        for(i=0;i<nIn;i++)
//	            for(k=0;k<numVehicleTypes;k++)
//	                inDemand[e][i][k] /= applyratio[e][i];
        
//        // compute out flows ...........................................   
//        for(e=0;e<numEnsemble;e++)
//	        for(j=0;j<nOut;j++){
//	        	for(k=0;k<numVehicleTypes;k++){
//	        		outFlow[e][j][k] = 0d;
//	            	for(i=0;i<nIn;i++){
//	            		outFlow[e][j][k] += inDemand[e][i][k]*splitratio.get(i,j,k);	            		
//	            	}
//	        	}
//	        }
    }

	/** REMOVING THIS TEMPORARILY 
    private void resolveUnassignedSplits_A(){
    	
    	int e,i,j,k;
    	int numunknown;	
    	double dsmax, dsmin;
    	double [] sr_new = new double[nOut];
    	double remainingSplit;
    	double num;
    	
    	// SHOULD ONLY BE CALLED WITH numEnsemble=1!!!
    	
    	for(e=0;e<myNetwork.myScenario.numEnsemble;e++){
	    	for(i=0;i<nIn;i++){
		        for(k=0;k<myNetwork.myScenario.getNumVehicleTypes();k++){
		            
		        	// number of outputs with unknown split ratio
		        	numunknown = 0;
		        	for(j=0;j<nOut;j++)
		        		if(splitratio.get(i,j,k).isNaN())
		        			numunknown++;
		        	
		            if(numunknown==0)
		                continue;
		            
		        	// initialize sr_new, save location of unknown entries, compute remaining split
		        	unknownind.clear();
		        	unknown_dsratio.clear();
		        	remainingSplit = 1f;
		        	for(j=0;j<nOut;j++){
		        		Double sr = splitratio.get(i,j,k);
		        		if(sr.isNaN()){
		        			sr_new[j] = 0f;
		        			unknownind.add(j);						// index to unknown output
		        			unknown_dsratio.add(dsratio[e][j]);		// dsratio for unknown output
		        		}
		        		else {
		        			sr_new[j] = sr;
		        			remainingSplit -= sr;
		        		}
		        	}
		            
		        	// distribute remaining split until there is none left or 
		        	// all dsratios are equalized
		            while(remainingSplit>0){
		                
		            	// find most and least "congested" destinations
		            	dsmax = Double.NEGATIVE_INFINITY;
		            	dsmin = Double.POSITIVE_INFINITY;
		            	for(Double r : unknown_dsratio){
		            		dsmax = Math.max(dsmax,r);
		            		dsmin = Math.min(dsmax,r);
		            	}
		                
		                if(SiriusMath.equals(dsmax,dsmin))
		                    break;
		                    
	                	// indices of smallest dsratio
	                	minind_to_nOut.clear();
	                	minind_to_unknown.clear();
		            	sendtoeach.clear();		// flow needed to bring each dsmin up to dsmax
		            	double sumsendtoeach = 0f;
		            	for(int z=1;z<numunknown;z++)
		            		if( SiriusMath.equals(unknown_dsratio.get(z),dsmin) ){
		            			int index = unknownind.get(z);
		            			minind_to_nOut.add(index);
		            			minind_to_unknown.add(z);
		            			num = dsmax*outSupply[e][index] - outDemandKnown[e][index];
		            			sendtoeach.add(num);		            			
		            			sumsendtoeach += num;
		            		}
	
	                    // total that can be sent
		            	double sendtotal = Math.min(inDemand[e][i][k]*remainingSplit , sumsendtoeach );
	                    
	                    // scale down sendtoeach
	                    // store split ratio
	                    for(int z=0;z<minind_to_nOut.size();z++){
	                    	double send = sendtoeach.get(z)*sendtotal/sumsendtoeach;  
	                    	double addsplit = send/inDemand[e][i][k];
	                    	int ind_nOut = minind_to_nOut.get(z);
	                    	int ind_unknown = minind_to_unknown.get(z);
	                    	sr_new[ind_nOut] += addsplit;
	                    	remainingSplit -= addsplit;
		                    outDemandKnown[e][ind_nOut] += send;
		                    unknown_dsratio.set( ind_unknown , outDemandKnown[e][ind_nOut]/outSupply[e][ind_nOut] );
	                    }	                    
		                
		            }
		            
		            // distribute remaining splits proportionally to supplies
		            if(remainingSplit>0){
		            	
//		            	double totalcapacity = 0f;
//		            	double splitforeach;
//	                    for(Integer jj : unknownind)
//	                    	totalcapacity += output_link[jj].capacity;
//	                    for(Integer jj : unknownind){
//	                    	splitforeach = remainingSplit*output_link[jj].capacity/totalcapacity;
//	                    	sr_new[jj] += splitforeach;
//	                    	outDemandKnown[jj] += inDemand[i][k]*splitforeach;
//	                    }
//	                    remainingSplit = 0;
	                    
		            	double totalsupply = 0f;
		            	double splitforeach;
	                    for(Integer jj : unknownind)
	                    	totalsupply += outSupply[e][jj];
	                    for(Integer jj : unknownind){
	                    	splitforeach = remainingSplit*outSupply[e][jj]/totalsupply;
	                    	sr_new[jj] += splitforeach;
	                    	outDemandKnown[e][jj] += inDemand[e][i][k]*splitforeach;
	                    }
	                    remainingSplit = 0;
		            }
		            
		            // copy to SR
		            for(j=0;j<nOut;j++)
		            	splitratio.set(i,j,k,sr_new[j]);
		        }
	    	}
    	}
    
    }
    */

    /*
    private Float3DMatrix resolveUnassignedSplits_B(SR){

        // GCG: take care of case single class
        
        for(i=0;i<nIn;i++){
            for(k=0;k<nTypes;k++){
                
                sr_j = SR(i,:,k);                            // 1 x nOut
                
                if(~any(sr_j<0))
                    continue;
                
                sr_pos = sr_j;
                sr_pos(sr_pos<0) = 0;
                phi = find(sr_j<0);             // possible destinations
                
                phi_dsratio = dsratio(phi);
                
                // classes are sorted in order of increasing congestion
                dsratio_class = sort(unique(phi_dsratio),2,'ascend');
                
                // class z has members phi(isinclass(z,:))
                numclasses = length(dsratio_class);
                isinclass = false(numclasses,length(phi));
                for(z=0;z<numclasses;z++)
                    isinclass(z,phi_dsratio==dsratio_class(z)) = true;
                
                // for each class compute the demand needed to get to the next class
                Delta = zeros(numclasses-1,1);
                for(z=0;z<numclasses-1;z++){
                    myphi = phi(isinclass(z,:));
                    Delta(z) = sum( outSupply(myphi)*dsratio_class(z+1) - outDemandKnown(myphi) );
                }
                
                // flow needed to raise classes
                if(numclasses==1)
                    flowtolevel = inf;
                else
                    flowtolevel = [cumsum(Delta.*(1:numclasses-1)) inf];    // 1xnumclasses
                
                // numclassups = n then remainingDemand is sufficient to unite classes 1..n, but not {1..n} and n+1
                remainingSplit = 1-sum(sr_pos);
                remainingDemand = inDemand(i,k)*remainingSplit;
                numclassmerge = find(remainingDemand<flowtolevel,1,'first');
                
                // flowtolevel(numclassmerge-1) is flow used to
                // level off classes. Distribute the remainder
                // equally among unassigned outputs
                if(numclassmerge>1)
                    levelflow = flowtolevel(numclassmerge-1);
                else
                    levelflow = 0;
                
                leftoverperclass = (remainingDemand-levelflow)/numclassmerge;
                
                for(z=0;z<numclasses;z++){
                    
                    flowtoclass = 0;
                    if(numclassmerge>z)
                        flowtoclass = sum(Delta(z:end));
                    
                    if(numclassmerge>=z)
                        flowtoclass = flowtoclass + leftoverperclass;
                    
                    // distribute among class members
                    myphi = phi(isinclass(z,:));
                    phishare = outSupply(myphi)/sum(outSupply(myphi));
                    flowtophi = flowtoclass*phishare;
                    
                    // save in SR matrix
                    if(inDemand(i,k)>0)
                        SR(i,myphi,k) = flowtophi/inDemand(i,k);
                    else{
                        SR(i,myphi,k) = 0;
                        s = sum(SR(i,:,k));
                        if(s>0)
                            SR(i,:,k) = SR(i,:,k)/s;
                        else
                            SR(i,1,k) = 1;
                    }
                }
            }
        }
    }
*/
    

/*
    private Float3DMatrix resolveUnassignedSplits_C(SR){
    	for(int i=0;i<nIn;i++){
	        for(int k=0;k<nTypes;k++){
	            sr_j = SR(i,:,k);
	            if(~any(sr_j<0))
	                continue;
	            phi = find(sr_j<0);
	            remainingSplit = 1-sum(sr_j(sr_j>=0));
	            phi_dsratio = dsratio(phi);
	            SR(i,phi,k) = phi_dsratio/sum(phi_dsratio)*remainingSplit;
	        }
    	}
    }    
    */
	
    
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
		
    /** Index of link with given id in the list of output links of this node 
     * Returns -1 if the input string is not a valid name.
     * */ 
	public int getDestinationNetworkIndex(String id){
		
		// background flow
		if(id==null)
			if(myNetwork.myScenario.has_background_flow)
				return 0;
			else
				return -1;
		// no destination networks and not background flow (this should never happen)
		if(myNetwork.myScenario.destination_networks==null)
			return -1;
		for(int i=0;i<numDNetworks;i++){
			DestinationNetwork dnetwork = myNetwork.myScenario.destination_networks.get(myDNGlobalIndex.get(i)).dnetwork;
			if(dnetwork!=null)
				if(dnetwork.getId().equals(id))
					return i;
		}
		return -1;
	}
	
    /** Total number of links entering this node */ 
	public int getnIn() {
		return nIn;
	}
	
	/** Number of links entering this node for a global destination network index  */
	public int getnIn(int dn_global_index){
		try {
			int dn_node_index = myDNGlobalIndex.indexOf(dn_global_index);
			return dn2inlinkindex.get(dn_node_index).size();
		} catch (Exception e) {
			return 0;
		}
	}
	
    /** Total number of links exiting this node */ 
	public int getnOut() {
		return nOut;
	}
	
	/** Number of links exiting this node for aglobal  destination network index  */
	public int getnOut(int dn_global_index){
		try {
			int dn_node_index = myDNGlobalIndex.indexOf(dn_global_index);
			return dn2outlinkindex.get(dn_node_index).size();
		} catch (Exception e) {
			return 0;
		}
	}
    
    /** <code>true</code> iff there is a split ratio controller attached to this link */
	public boolean hasController() {
		return hascontroller;
	}
	
	/** ADDED TEMPORARILY FOR MANUEL'S DTA WORK 
	 * @throws SiriusException */
//	public void setSplitRatioMatrix(double [][][] x) throws SiriusException {
//		if(x.length!=splitratio.getnIn())
//			throw new SiriusException("Node.setSplitRatioMatrix, bad first dimension.");
//		if(x[0].length!=splitratio.getnOut())
//			throw new SiriusException("Node.setSplitRatioMatrix, bad second dimension.");
//		if(x[0][0].length!=splitratio.getnVTypes())
//			throw new SiriusException("Node.setSplitRatioMatrix, bad third dimension.");
//		int i,j,k;
//		for(i=0;i<splitratio.getnIn();i++)
//			for(j=0;j<splitratio.getnOut();j++)
//				for(k=0;k<splitratio.getnVTypes();k++)
//					splitratio.set(i, j, k, x[i][j][k]);
//		normalizeSplitRatioMatrix(splitratio);
//	}

	public double [][][] getSplitRatio(){
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
	public double getSplitRatio(int inLinkInd, int outLinkInd, int vehTypeInd) {
		return splitratio.get(inLinkInd, outLinkInd, vehTypeInd);
	}

}
