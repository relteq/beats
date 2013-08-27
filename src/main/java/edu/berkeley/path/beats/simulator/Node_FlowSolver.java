package edu.berkeley.path.beats.simulator;

public abstract class Node_FlowSolver {

	protected Node myNode;
	
    protected abstract IOFlow computeLinkFlows(final Double3DMatrix sr,final SupplyDemand demand_supply);
    protected abstract void reset();
    
	public Node_FlowSolver(Node myNode) {
		super();
		this.myNode = myNode;
	}

	protected static class SupplyDemand {
		// input to node model, copied from link suppy/demand
		protected double [][][] demand;		// [ensemble][nIn][nTypes]
		protected double [][] supply;		// [ensemble][nOut]
		
		public SupplyDemand(int numEnsemble,int nIn,int nOut,int numVehicleTypes) {
			super();
	    	demand = new double[numEnsemble][nIn][numVehicleTypes];
			supply = new double[numEnsemble][nOut];
		}
		
		public void setDemand(int nE,int nI,double [] val){
			demand[nE][nI] = val;
		}
		
		public void setSupply(int nE,int nO, double val){
			supply[nE][nO]=val;
		}
		
		public double getDemand(int nE,int nI,int nK){
			return demand[nE][nI][nK];
		}
		
		public double getSupply(int nE,int nO){
			return supply[nE][nO];
		}

		public double [] getSupply(int nE){
			return supply[nE];
		}
	}
	
	protected static class IOFlow {
		// input to node model, copied from link suppy/demand
		protected double [][][] in;		// [ensemble][nIn][nTypes]
		protected double [][][] out;	// [ensemble][nOut][nTypes]
		
		public IOFlow(int numEnsemble,int nIn,int nOut,int numVehicleTypes) {
			super();
	    	in = new double[numEnsemble][nIn][numVehicleTypes];
			out = new double[numEnsemble][nOut][numVehicleTypes];
		}

		public void setIn(int nE,int nI,int nV,double val){
			in[nE][nI][nV] = val;
		}
		
		public void setOut(int nE,int nO,int nV,double val){
			out[nE][nO][nV]=val;
		}
		
		public double [] getIn(int nE,int nI){
			return in[nE][nI];
		}

		public double getIn(int nE,int nI,int nV){
			return in[nE][nI][nV];
		}
		
		public double [] getOut(int nE,int nO){
			return out[nE][nO];
		}
		
		public void addOut(int nE,int nO,int nV,double val){
			out[nE][nO][nV] += val;
		}
		
	}
}
