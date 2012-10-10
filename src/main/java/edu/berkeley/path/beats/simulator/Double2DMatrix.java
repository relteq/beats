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

import java.util.StringTokenizer;

public final class Double2DMatrix {
	
	private int nTime;			// number of time slices
	private int nVTypes;		// number of vehicle types
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
    
    public Double2DMatrix(double [][] val) {
    	this.nTime = val.length;
    	this.nVTypes = val.length>0 ? val[0].length : 0;
    	data = new Double[nTime][nVTypes];
    	for(int i=0;i<nTime;i++)
        	for(int j=0;j<nVTypes;j++)
        		data[i][j] = val[i][j];
    	this.isempty = nTime==0 && nVTypes==0;
    }
    
    // initialize a 2D matrix from comma/colon separated string of positive numbers
    // negative numbers get replaced with nan.
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
				try {
					Double value = Double.parseDouble(slicesXY.nextToken());
					if(value>=0){
						data[i][j] = value;
						allnan = false;
					}
					else
						data[i][j] = Double.NaN;
				} catch (NumberFormatException e) {
					data[i][j] = Double.NaN;
				}
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
			SiriusErrorLog.addError("Data is not square.");
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

	/**
	 * Retrieves the split ratio value for a given time slice and a given vehicle type index
	 * @param timeslice the time slice
	 * @param vehicletypeindex the vehicle type index
	 * @return the split ratio
	 */
	public Double get(int timeslice, int vehicletypeindex) {
		return data[timeslice][vehicletypeindex];
	}

    @Override
	public String toString() {
    	String str = "[";
    	if(nTime>0 && nVTypes>0){
	    	for(int i=0;i<data.length;i++){
	    		for(int j=0;j<data[i].length-1;j++){
	    			str += data[i][j] + ",";
	    		}
	    		str += data[i][data[i].length-1];
	    		if(i<data.length-1)
	    			str += ";";
	    	}
    	}
		return str+"]";
	}

    public void set(int timeslice,int vehicletypeindex,double value){
    	if(timeslice>=nTime || vehicletypeindex>=nVTypes)
    		return;
    	data[timeslice][vehicletypeindex] = value;
    }
    
	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  
    
	public void multiplyscalar(double value){
    	if(this.isempty)
    		return;
    	int i,j;
    	for(i=0;i<nTime;i++)
    		for(j=0;j<nVTypes;j++)
    			data[i][j] *= value;	
    }
    
	/////////////////////////////////////////////////////////////////////
	// check data
	//////////////////////////////////////////////////////////////////////  
    
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