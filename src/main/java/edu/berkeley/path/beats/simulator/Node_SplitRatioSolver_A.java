package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;

public class Node_SplitRatioSolver_A extends Node_SplitRatioSolver {

	protected double [][] outDemandKnown;	// [ensemble][nOut]
	protected double [][] dsratio;			// [ensemble][nOut]

	public Node_SplitRatioSolver_A(Node myNode) {
		super(myNode);
	}

	@Override
	protected void reset() {
    	int numEnsemble = myNode.getMyNetwork().getMyScenario().getNumEnsemble();	
		dsratio 		= new double[numEnsemble][myNode.nOut];
		outDemandKnown 	= new double[numEnsemble][myNode.nOut];
	}
	
	@Override
	protected Double3DMatrix computeAppliedSplitRatio(final Double3DMatrix splitratio_selected,final Node_FlowSolver.SupplyDemand demand_supply) {

    	int e,i,j,k;
    	int numunknown;	
    	double dsmax, dsmin;
		int nIn = myNode.nIn;
		int nOut = myNode.nOut;        
    	int numEnsemble = myNode.myNetwork.getMyScenario().getNumEnsemble();
    	int numVehicleTypes = myNode.myNetwork.getMyScenario().getNumVehicleTypes();
    	Double3DMatrix splitratio_new = new Double3DMatrix(splitratio_selected.getData());
    	double [] sr_new = new double[myNode.nOut];
    	double remainingSplit;
    	double num;
    	
        if(myNode.istrivialsplit)
        	return new Double3DMatrix(nIn,nOut,numVehicleTypes,1d);

    	ArrayList<Integer> unknownind = new ArrayList<Integer>();		// [unknown splits]
    	ArrayList<Double> unknown_dsratio = new ArrayList<Double>();	// [unknown splits]	
    	ArrayList<Integer> minind_to_nOut= new ArrayList<Integer>();	// [min unknown splits]
    	ArrayList<Integer> minind_to_unknown= new ArrayList<Integer>();	// [min unknown splits]
    	ArrayList<Double> sendtoeach = new ArrayList<Double>();			// [min unknown splits]
        
		// solve unknown split ratios if they are non-trivial ..............

        // compute known output demands ................................
		for(e=0;e<numEnsemble;e++)
	        for(j=0;j<nOut;j++){
	        	outDemandKnown[e][j] = 0f;
	        	for(i=0;i<nIn;i++)
	        		for(k=0;k<numVehicleTypes;k++)
	        			if(!splitratio_selected.get(i,j,k).isNaN())
	        				outDemandKnown[e][j] += splitratio_selected.get(i,j,k) * demand_supply.getDemand(e,i,k);
	        }
        
        // compute and sort output demand/supply ratio .................
		for(e=0;e<numEnsemble;e++)
	        for(j=0;j<nOut;j++)
	        	dsratio[e][j] = outDemandKnown[e][j] / demand_supply.getSupply(e,j);
                
        // fill in unassigned split ratios .............................

    	for(e=0;e<numEnsemble;e++){
	    	for(i=0;i<nIn;i++){
		        for(k=0;k<numVehicleTypes;k++){
		            
		        	// number of outputs with unknown split ratio
		        	numunknown = 0;
		        	for(j=0;j<nOut;j++)
		        		if(splitratio_selected.get(i,j,k).isNaN())
		        			numunknown++;
		        	
		            if(numunknown==0)
		                continue;
		            
		        	// initialize sr_new, save location of unknown entries, compute remaining split
		        	unknownind.clear();
		        	unknown_dsratio.clear();
		        	remainingSplit = 1f;
		        	for(j=0;j<nOut;j++){
		        		Double sr = splitratio_selected.get(i,j,k);
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
		            			num = dsmax*demand_supply.getSupply(e,index) - outDemandKnown[e][index];
		            			sendtoeach.add(num);		            			
		            			sumsendtoeach += num;
		            		}
	
	                    // total that can be sent
		            	double sendtotal = Math.min(demand_supply.getDemand(e,i,k)*remainingSplit , sumsendtoeach );
	                    
	                    // scale down sendtoeach
	                    // store split ratio
	                    for(int z=0;z<minind_to_nOut.size();z++){
	                    	double send = sendtoeach.get(z)*sendtotal/sumsendtoeach;  
	                    	double addsplit = send/demand_supply.getDemand(e,i,k);
	                    	int ind_nOut = minind_to_nOut.get(z);
	                    	int ind_unknown = minind_to_unknown.get(z);
	                    	sr_new[ind_nOut] += addsplit;
	                    	remainingSplit -= addsplit;
		                    outDemandKnown[e][ind_nOut] += send;
		                    unknown_dsratio.set( ind_unknown , outDemandKnown[e][ind_nOut]/demand_supply.getSupply(e,ind_nOut) );
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
	                    	totalsupply += demand_supply.getSupply(e,jj);
	                    for(Integer jj : unknownind){
	                    	splitforeach = remainingSplit*demand_supply.getSupply(e,jj)/totalsupply;
	                    	sr_new[jj] += splitforeach;
	                    	outDemandKnown[e][jj] += demand_supply.getDemand(e,i,k)*splitforeach;
	                    }
	                    remainingSplit = 0;
		            }
		            
		            // copy to SR
		            for(j=0;j<nOut;j++)
		            	splitratio_new.set(i,j,k,sr_new[j]);
		        }
	    	}
    	}
    	
    	return splitratio_new;

	}


}
