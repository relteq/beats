package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;

/** Priorities are porportional to demands. All splits are assumed given **/
public class Node_LNCTM_Base extends Node {

	/** @y.exclude */ 	protected boolean [][] iscontributor;	// [nIn][nOut]
	/** @y.exclude */ 	protected double [][] dsratio;			// [ensemble][nOut]
	/** @y.exclude */ 	protected ArrayList<Integer> unknownind = new ArrayList<Integer>();		// [unknown splits]
	/** @y.exclude */ 	protected ArrayList<Double> unknown_dsratio = new ArrayList<Double>();	// [unknown splits]	
	/** @y.exclude */ 	protected ArrayList<Integer> minind_to_nOut= new ArrayList<Integer>();	// [min unknown splits]
	/** @y.exclude */ 	protected ArrayList<Integer> minind_to_unknown= new ArrayList<Integer>();	// [min unknown splits]
	/** @y.exclude */ 	protected ArrayList<Double> sendtoeach = new ArrayList<Double>();			// [min unknown splits]
	/** @y.exclude */ 	protected double [][] outDemandKnown;	// [ensemble][nOut]
	
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
    	int numEnsemble = myNetwork.myScenario.numEnsemble;		
		dsratio 		= new double[numEnsemble][nOut];
		outDemandKnown 	= new double[numEnsemble][nOut];
	}
	
	@Override
	protected void computeInOutFlows() {
		
        if(isTerminal)
            return;
        
        int e,i,j,k;        
        int numEnsemble = myNetwork.myScenario.numEnsemble;
    	int numVehicleTypes = myNetwork.myScenario.getNumVehicleTypes();
        
		// solve unknown split ratios if they are non-trivial ..............
		if(!istrivialsplit){	

	        // Take current split ratio from the profile if the node is
			// not actively controlled. Otherwise the mat has already been 
			// set by the controller.
			if(hasSRprofile && !controlleron && !hasactivesplitevent)
				splitratio.copydata(sampledSRprofile);
			
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
	        resolveUnassignedSplits();
		}

        // input i contributes to output j .............................
    	for(i=0;i<splitratio.getnIn();i++)
        	for(j=0;j<splitratio.getnOut();j++)
        		iscontributor[i][j] = splitratio.getSumOverTypes(i,j)>0;
	
        double [][] applyratio = new double[numEnsemble][nIn];

        for(e=0;e<numEnsemble;e++)
	        for(i=0;i<nIn;i++)
	        	applyratio[e][i] = Double.NEGATIVE_INFINITY;
        
        for(e=0;e<numEnsemble;e++)
	        for(j=0;j<nOut;j++){
	        	
	        	// re-compute known output demands .........................
				outDemandKnown[e][j] = 0d;
	            for(i=0;i<nIn;i++)
	            	for(k=0;k<numVehicleTypes;k++)
	            		outDemandKnown[e][j] += inDemand[e][i][k]*splitratio.get(i,j,k);
	            
	            // compute and sort output demand/supply ratio .............
	            if(BeatsMath.greaterthan(outSupply[e][j],0d))
	            	dsratio[e][j] = Math.max( outDemandKnown[e][j] / outSupply[e][j] , 1d );
	            else
	            	dsratio[e][j] = 1d;
	            
	            // reflect ratios back on inputs
	            for(i=0;i<nIn;i++)
	            	if(iscontributor[i][j])
	            		applyratio[e][i] = Math.max(dsratio[e][j],applyratio[e][i]);
	            	
	        }

        // scale down input demands
        for(e=0;e<numEnsemble;e++)
	        for(i=0;i<nIn;i++)
	            for(k=0;k<numVehicleTypes;k++){
	            	inFlow[e][i][k] = inDemand[e][i][k] / applyratio[e][i];
	            }
        
        // compute out flows ...........................................   
        for(e=0;e<numEnsemble;e++)
	        for(j=0;j<nOut;j++){
	        	for(k=0;k<numVehicleTypes;k++){
	        		outFlow[e][j][k] = 0d;
	            	for(i=0;i<nIn;i++){
	            		outFlow[e][j][k] += inFlow[e][i][k]*splitratio.get(i,j,k);	            		
	            	}
	        	}
	        }
	}

	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	protected void resolveUnassignedSplits(){
	}
    
}
