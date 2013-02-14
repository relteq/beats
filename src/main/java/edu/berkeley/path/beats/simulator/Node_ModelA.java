package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;

/** Priorities are porportional to demands. All splits are assumed given **/
public class Node_ModelA extends Node {

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

	public Node_ModelA() {
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
	        resolveUnassignedSplits_A();
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
		                
		                if(BeatsMath.equals(dsmax,dsmin))
		                    break;
		                    
	                	// indices of smallest dsratio
	                	minind_to_nOut.clear();
	                	minind_to_unknown.clear();
		            	sendtoeach.clear();		// flow needed to bring each dsmin up to dsmax
		            	double sumsendtoeach = 0f;
		            	for(int z=1;z<numunknown;z++)
		            		if( BeatsMath.equals(unknown_dsratio.get(z),dsmin) ){
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
		            	/*
		            	double totalcapacity = 0f;
		            	double splitforeach;
	                    for(Integer jj : unknownind)
	                    	totalcapacity += output_link[jj].capacity;
	                    for(Integer jj : unknownind){
	                    	splitforeach = remainingSplit*output_link[jj].capacity/totalcapacity;
	                    	sr_new[jj] += splitforeach;
	                    	outDemandKnown[jj] += inDemand[i][k]*splitforeach;
	                    }
	                    remainingSplit = 0;
	                    */
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
    
}
