/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.StringTokenizer;

final class Double2DMatrix {
	
	private int nTime;			// number of time slices (1st dimension)
	private int nVTypes;		// number of vehicle types (2nd dimension)
	private boolean isempty;	// true if there is no data;
	private Double [][] data;
    
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
    public Double2DMatrix(int nTime,int nVTypes,Double val) {
    	this.nTime = nTime;
    	this.nVTypes = nVTypes;
    	data = new Double[nTime][nVTypes];
    	for(int i=0;i<nTime;i++)
        	for(int j=0;j<nVTypes;j++)
        		data[i][j] = val;
    	this.isempty = nTime==0 && nVTypes==0;
    }
    
    // initialize a 2D matrix from comma/colon separated string of positive numbers
    // negative numbers get replaced with nan.
    // Example: "0.1:0.2:0.3,0.4:0.5:0.6" defines profiles for 3 vehicle types and
    // 2 time intervals. type 1: [0.1 0.4], type 2: [0.2 0.5], type 3: [0.3 0.6]
    public Double2DMatrix(String str) {

    	int numtokens,i,j;
		boolean issquare = true;
    	nTime = 0;
    	nVTypes = 0;
    	
    	if ((str.isEmpty()) || (str.equals("\n")) || (str.equals("\r\n"))){
    		isempty = true;
			return;
    	}
    	
    	str.replaceAll("\\s","");
    	
    	// populate data
		StringTokenizer slicesX = new StringTokenizer(str, ",");
		nTime = slicesX.countTokens();
		i=0;
		boolean allnan = true;
		while (slicesX.hasMoreTokens() && issquare) {
			String sliceX = slicesX.nextToken();
			StringTokenizer slicesXY = new StringTokenizer(sliceX, ":");
			
			// evaluate nVTypes, check squareness
			numtokens = slicesXY.countTokens();
			if(nVTypes==0){ // first time here
				nVTypes = numtokens;
				data = new Double[nTime][nVTypes];
			}
			else{
				if(nVTypes!=numtokens){
					issquare = false;
					break;
				}
			}
			
			j=0;
			while (slicesXY.hasMoreTokens() && issquare) {				
				Double value = Double.parseDouble(slicesXY.nextToken());
				if(value>=0){
					data[i][j] = value;
					allnan = false;
				}
				else
					data[i][j] = Double.NaN;
				j++;
			}
			i++;
		}
		
		if(allnan){
			data = null;
	    	isempty = true;
			return;
			
		}
		
		if(!issquare){
			SiriusErrorLog.addErrorMessage("Data is not square.");
			data = null;
	    	isempty = true;
			return;
		}
		
    	isempty = nTime==0 && nVTypes==0;
		
    }
     
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  

	public boolean isEmpty() {
		return isempty;
	}
	
	public int getnTime() {
		return nTime;
	}

	public int getnVTypes() {
		return nVTypes;
	}

	public Double [] sampleAtTime(int k,Integer [] vehicletypeindex){
		Double [] x = new Double[nVTypes];
		if(vehicletypeindex==null){
			for(int j=0;j<nVTypes;j++){
				x[j] = data[k][j];
			}
		}
		else{
			for(int j=0;j<nVTypes;j++){
				x[vehicletypeindex[j]] = data[k][j];
			}
		}
		return x;
	}

	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  
    
    public void multiplyscalar(double value){
    	int i,j;
    	for(i=0;i<nTime;i++)
    		for(j=0;j<nVTypes;j++)
    			data[i][j] *= value;	
    }
    
	/////////////////////////////////////////////////////////////////////
	// check data
	////////s/////////////////////////////////////////////////////////////  
    
    public boolean hasNaN(){
    	if(isempty)
    		return false;
    	int i,j;
    	for(i=0;i<nTime;i++)
    		for(j=0;j<nVTypes;j++)
				if(data[i][j].isNaN())
					return true;
    	return false;
    }
    
}
