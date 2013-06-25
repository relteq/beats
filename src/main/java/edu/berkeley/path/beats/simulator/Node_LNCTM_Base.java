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

import edu.berkeley.path.beats.simulator.Node.IOFlow;
import edu.berkeley.path.beats.simulator.Node.SupplyDemand;

public class Node_LNCTM_Base extends Node {

    // used in update()
	protected double [][] outDemandKnown;	// [ensemble][nOut]
	protected double [][] dsratio;		// [ensemble][nOut]
	protected boolean [][] iscontributor;	// [nIn][nOut]
	protected ArrayList<Integer> unknownind = new ArrayList<Integer>();		// [unknown splits]
	protected ArrayList<Double> unknown_dsratio = new ArrayList<Double>();	// [unknown splits]	
	protected ArrayList<Integer> minind_to_nOut= new ArrayList<Integer>();	// [min unknown splits]
	protected ArrayList<Integer> minind_to_unknown= new ArrayList<Integer>();	// [min unknown splits]
	protected ArrayList<Double> sendtoeach = new ArrayList<Double>();			// [min unknown splits]

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Node_LNCTM_Base() {
		super();
	}

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
    
	@Override
	protected void populate(Network myNetwork) {
		super.populate(myNetwork);

		iscontributor = new boolean[nIn][nOut];
	}

	@Override
	protected void reset() {
		super.reset();
    	int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();		
		dsratio 		= new double[numEnsemble][nOut];
		outDemandKnown 	= new double[numEnsemble][nOut];
	}

	/////////////////////////////////////////////////////////////////////
	// Override Node
	/////////////////////////////////////////////////////////////////////

	@Override
	protected Double3DMatrix computeAppliedSplitRatio(final SupplyDemand demand_supply){

		int e,i,j,k;
        int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();
        Double3DMatrix splitratio_applied;
        
		// solve unknown split ratios if they are non-trivial ..............
		if(!istrivialsplit){	

	        // Take current split ratio from the profile if the node is
			// not actively controlled. Otherwise the mat has already been 
			// set by the controller.
			if(hasSRprofile  && !hasactivesplitevent ) //&& !controlleron)
				splitratio_selected = this.mySplitRatioProfile.getCurrentSplitRatio();
//				splitratio.copydata(splitFromProfile);
			
	        // compute known output demands ................................
			for(e=0;e<numEnsemble;e++)
		        for(j=0;j<nOut;j++){
		        	outDemandKnown[e][j] = 0f;
		        	for(i=0;i<nIn;i++)
		        		for(k=0;k<myNetwork.getMyScenario().getNumVehicleTypes();k++)
		        			if(!splitratio_selected.get(i,j,k).isNaN())
		        				outDemandKnown[e][j] += splitratio_selected.get(i,j,k) * demand_supply.getDemand(e,i,k);
		        }
	        
	        // compute and sort output demand/supply ratio .................
			for(e=0;e<numEnsemble;e++)
		        for(j=0;j<nOut;j++)
		        	dsratio[e][j] = outDemandKnown[e][j] / demand_supply.getSupply(e,j);
	                
	        // fill in unassigned split ratios .............................
			splitratio_applied = resolveUnassignedSplits(splitratio_selected,demand_supply);
		}
		else
			splitratio_applied = new Double3DMatrix(getnIn(),getnOut(),getMyNetwork().getMyScenario().getNumVehicleTypes(),1d);
		
		return splitratio_applied;
		
	}
	
	@Override
	protected IOFlow computeLinkFlows(final Double3DMatrix sr,final SupplyDemand demand_supply){

    	int e,i,j,k;
    	int numEnsemble = myNetwork.getMyScenario().getNumEnsemble();
    	int numVehicleTypes = myNetwork.getMyScenario().getNumVehicleTypes();
    	    	
        // input i contributes to output j .............................
    	for(i=0;i<sr.getnIn();i++)
        	for(j=0;j<sr.getnOut();j++)
        		iscontributor[i][j] = sr.getSumOverTypes(i,j)>0;
	
        double [][] applyratio = new double[numEnsemble][nIn];

        for(e=0;e<numEnsemble;e++)
	        for(i=0;i<nIn;i++)
	        	applyratio[e][i] = Double.NEGATIVE_INFINITY;
        
        for(e=0;e<numEnsemble;e++){
	        for(j=0;j<nOut;j++){
	        	
	        	// re-compute known output demands .........................
				outDemandKnown[e][j] = 0d;
	            for(i=0;i<nIn;i++)
	            	for(k=0;k<numVehicleTypes;k++)
	            		outDemandKnown[e][j] += demand_supply.getDemand(e,i,k)*sr.get(i,j,k);
	            
	            // compute and sort output demand/supply ratio .............
	            if(BeatsMath.greaterthan(demand_supply.getSupply(e,j),0d))
	            	dsratio[e][j] = Math.max( outDemandKnown[e][j] / demand_supply.getSupply(e,j) , 1d );
	            else
	            	dsratio[e][j] = 1d;
	            
	            // reflect ratios back on inputs
	            for(i=0;i<nIn;i++)
	            	if(iscontributor[i][j])
	            		applyratio[e][i] = Math.max(dsratio[e][j],applyratio[e][i]);
	            	
	        }
        }
        
        IOFlow ioflow = new IOFlow(numEnsemble,nIn,nOut,numVehicleTypes);

        // scale down input demands
        for(e=0;e<numEnsemble;e++){
	        for(i=0;i<nIn;i++){
	            for(k=0;k<numVehicleTypes;k++){
	            	ioflow.setIn(e,i,k , demand_supply.getDemand(e,i,k) / applyratio[e][i] );
	            }
	        }
        }
        
        // compute out flows ...........................................   
        for(e=0;e<numEnsemble;e++){
	        for(j=0;j<nOut;j++){
	        	for(k=0;k<numVehicleTypes;k++){
	        		double val = 0d;
	            	for(i=0;i<nIn;i++){
	            		val += demand_supply.getDemand(e,i,k)*sr.get(i,j,k);	            		
	            	}
	            	ioflow.setOut(e,j,k,val);
	        	}
	        }
        }
        
        return ioflow;
	}

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////

	protected Double3DMatrix resolveUnassignedSplits(final Double3DMatrix splitratio,final SupplyDemand demand_supply){
		return null;
	}
	
}