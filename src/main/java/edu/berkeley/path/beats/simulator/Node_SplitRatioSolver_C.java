package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.Node_FlowSolver.SupplyDemand;

public class Node_SplitRatioSolver_C extends Node_SplitRatioSolver {

	public Node_SplitRatioSolver_C(Node myNode) {
		super(myNode);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Double3DMatrix computeAppliedSplitRatio(
			Double3DMatrix splitratio_selected, SupplyDemand demand_supply) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}


    /*
    private Float3DMatrix resolveUnassignedSplits(SR){
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
	
}
