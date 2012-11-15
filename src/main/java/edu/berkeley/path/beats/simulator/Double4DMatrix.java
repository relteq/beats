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

/** 4D matrix class used for representing time-invariant split ratio matrices.
 * 
* @author Gabriel Gomes
*/
public class Double4DMatrix {
	
	protected Node myNode;	
//	protected boolean isempty;			// true if there is no data;
	protected Double3DMatrix [] data;	// indexed by destination network (node index)

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
	public Double4DMatrix(Node myNode,double value) {
		this.myNode = myNode;
//		this.isempty = true;
		this.data = new Double3DMatrix[myNode.numDNetworks];
		
		int numVTypes = myNode.myNetwork.myScenario.getNumVehicleTypes();
		int dn_network_index;
		for(int i=0;i<myNode.numDNetworks;i++){
			dn_network_index = myNode.myDNGlobalIndex.get(i);
			data[i] = new Double3DMatrix(myNode.getnIn(dn_network_index),myNode.getnOut(dn_network_index),numVTypes,value);
		}
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
