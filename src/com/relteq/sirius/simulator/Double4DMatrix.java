package com.relteq.sirius.simulator;

/** 4D matrix class used for representing time-invariant split ratio matrices.
 * 
* @author Gabriel Gomes
*/
public class Double4DMatrix {
	
	protected Node myNode;	
	protected boolean isempty;		// true if there is no data;
	protected Double3DMatrix [] data;	// indexed by destination network (node index)

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
	public Double4DMatrix(Node myNode,double value) {
		this.myNode = myNode;
		this.isempty = true;
		this.data = new Double3DMatrix[myNode.numDNetworks];
		
		int numVTypes = myNode.myNetwork.myScenario.getNumVehicleTypes();
		for(int i=0;i<myNode.numDNetworks;i++)
			data[i] = new Double3DMatrix(myNode.getnIn(i),myNode.getnOut(i),numVTypes,value);
	}
	
	/////////////////////////////////////////////////////////////////////
	// public get/set
	/////////////////////////////////////////////////////////////////////
    
	public void setValue(int dn_node_index,int in_index,int out_index,int vt_index,double value){
		data[dn_node_index].set(in_index,out_index,vt_index,value);
	}

	public double getValue(int dn_node_index,int in_index,int out_index,int vt_index){
		return data[dn_node_index].get(in_index,out_index,vt_index);
	}
	
	/////////////////////////////////////////////////////////////////////
	// public
	/////////////////////////////////////////////////////////////////////
    
	public int getNumDNetwork(){
		return data.length;
	}

    public void copydata(Double4DMatrix in) {
    	if(in.getNumDNetwork()!=getNumDNetwork())
    		return;
    	for(int i=0;i<getNumDNetwork();i++)
    		data[i].copydata(in.data[i]);	  
    }
    
	public double getSumOverTypes(int dn_index,int in_index,int out_index){
		return data[dn_index].getSumOverTypes(in_index,out_index);
	}


}
