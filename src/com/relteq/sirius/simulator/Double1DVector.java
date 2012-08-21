/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.StringTokenizer;

public final class Double1DVector {
	
	private double [] data;
    
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
    public Double1DVector(int n,double val) {
    	data = new double[n];
    	for(int i=0;i<n;i++)
            data[i] = val;
    }
    
    public Double1DVector(String str,String delim) {

      	if ((str.isEmpty()) || (str.equals("\n")) || (str.equals("\r\n"))){
			return;
    	}
    	
    	str.replaceAll("\\s","");
    	
    	// populate data
		StringTokenizer slicesX = new StringTokenizer(str,delim);
		int i=0;
		boolean allnan = true;
		data = new double[slicesX.countTokens()];
		while (slicesX.hasMoreTokens()) {			
			try {
				double value = Double.parseDouble(slicesX.nextToken());
				if(value>=0){
					data[i] = value;
					allnan = false;
				}
				else
					data[i] = Double.NaN;
			} catch (NumberFormatException e) {
				data[i] = Double.NaN;
			}
			i++;
		}
		if(allnan)
			data = null;
    }
         
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  

	public boolean isEmpty() {
		return data.length==0;
	}

	public Integer getLength() {
		return data.length;
	}
	
    public double [] getData(){
    	return data;
    }
    
    public double get(int i){
    	if(data.length==0)
    		return Double.NaN;
    	else
    		return data[i];
    }

	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  
    
    public void set(int i,double f){
    	data[i] = f;
    }
    
    public void multiplyscalar(double value){
    	int i;
    	for(i=0;i<data.length;i++)
    		data[i] *= value;	
    }
    
    public void addscalar(double value){
    	int i;
    	for(i=0;i<data.length;i++)
    		data[i] += value;	
    }
    
    public void copydata(Double1DVector in){
    	if(in.data.length!=data.length)
    		return;
    	int i;
    	for(i=0;i<data.length;i++)
    		data[i] = in.data[i];	  
    }
    
	/////////////////////////////////////////////////////////////////////
	// check data
	/////////////////////////////////////////////////////////////////////  
    
    public boolean hasNaN(){
    	if(data.length==0)
    		return false;
    	int i;
    	for(i=0;i<data.length;i++)
			if(Double.isNaN(data[i]))
				return true;
    	return false;
    }
    
}
