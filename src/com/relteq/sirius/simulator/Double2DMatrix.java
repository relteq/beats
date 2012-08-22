/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.StringTokenizer;

/** Matrix class for reading and manipulating data read from ":," format. The hierarchy of delimiters is first ":"
 * then ",". The convention is to use ":" for vehicle types and "," for time. For example the "1:1:1,2:2:2" 
 * assigns the value 1 to three vehicle types for the first time interval, and 2 to three vehicle types for the 
 * second time interval. 
 * 
* @author Gabriel Gomes
*/
public final class Double2DMatrix {
	
	private int nTime;			// number of time slices (1st dimension)
	private int nVTypes;		// number of vehicle types (2nd dimension)
	private boolean isempty;	// true if there is no data;
	private double [][] data;
    
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

    public Double2DMatrix(int nTime,int nVTypes,double val) {
    	this.nTime = nTime;
    	this.nVTypes = nVTypes;
    	data = new double[nTime][nVTypes];
    	for(int i=0;i<nTime;i++)
        	for(int j=0;j<nVTypes;j++)
        		data[i][j] = val;
    	this.isempty = nTime==0 && nVTypes==0;
    }
    
    public Double2DMatrix(double [][] val) {
    	this.nTime = val.length;
    	this.nVTypes = val.length>0 ? val[0].length : 0;
    	data = new double[nTime][nVTypes];
    	for(int i=0;i<nTime;i++)
        	for(int j=0;j<nVTypes;j++)
        		data[i][j] = val[i][j];
    	this.isempty = nTime==0 && nVTypes==0;
    }
    
    /** initialize a 2D matrix from comma/colon separated string of positive number 
     * negative numbers get replaced with nan.
     * Example: "0.1:0.2:0.3,0.4:0.5:0.6" defines profiles for 3 vehicle types and
     * 2 time intervals. type 1: [0.1 0.4], type 2: [0.2 0.5], type 3: [0.3 0.6]
     */
    public Double2DMatrix(String str) {

    	int numtokens,i,j;
		boolean isrectangular = true;
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
		while (slicesX.hasMoreTokens() && isrectangular) {
			String sliceX = slicesX.nextToken();
			StringTokenizer slicesXY = new StringTokenizer(sliceX, ":");
			
			// evaluate nVTypes, check squareness
			numtokens = slicesXY.countTokens();
			if(nVTypes==0){ // first time here
				nVTypes = numtokens;
				data = new double[nTime][nVTypes];
			}
			else{
				if(nVTypes!=numtokens){
					isrectangular = false;
					break;
				}
			}
			
			j=0;
			while (slicesXY.hasMoreTokens() && isrectangular) {				
				try {
					double value = Double.parseDouble(slicesXY.nextToken());
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
		
    	isempty = nTime==0 && nVTypes==0;
		
		if(allnan)
	    	isempty = true;
		
		if(!isrectangular){
			SiriusErrorLog.addError("Data is not square.");
	    	isempty = true;
		}
		
    	if(isempty){
    		data = null;
	    	nTime = 0;
	    	nVTypes = 0;	
    	}
		
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

	public double [] sampleAtTime(int k,Integer [] vehicletypeindex){
		double [] x = new double[nVTypes];
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
	public double get(int timeslice, int vehicletypeindex) {
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
				if(Double.isNaN(data[i][j]))
					return true;
    	
    	return false;
    }
    
}
