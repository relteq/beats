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

final class Double1DVector {
	
	private Double [] data;
    
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
    public Double1DVector(int n,Double val) {
    	data = new Double[n];
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
		data = new Double[slicesX.countTokens()];
		while (slicesX.hasMoreTokens()) {			
			try {
				Double value = Double.parseDouble(slicesX.nextToken());
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
         
    // initialize a 1D vector from comma separated string of positive numbers
    // negative numbers get replaced with nan.
//    public Double1DVector(String str) {
//
//    	int numtokens,i,j;
//		boolean issquare = true;
//    	nTime = 0;
//    	nVTypes = 0;
//    	
//    	if ((str.isEmpty()) || (str.equals("\n")) || (str.equals("\r\n"))){
//    		isempty = true;
//			return;
//    	}
//    	
//    	str.replaceAll("\\s","");
//    	
//    	// populate data
//		StringTokenizer slicesX = new StringTokenizer(str, ",");
//		nTime = slicesX.countTokens();
//		i=0;
//		boolean allnan = true;
//		while (slicesX.hasMoreTokens() && issquare) {
//			String sliceX = slicesX.nextToken();
//			StringTokenizer slicesXY = new StringTokenizer(sliceX, ":");
//			
//			// evaluate nVTypes, check squareness
//			numtokens = slicesXY.countTokens();
//			if(nVTypes==0){ // first time here
//				nVTypes = numtokens;
//				data = new Double[nTime][nVTypes];
//			}
//			else{
//				if(nVTypes!=numtokens){
//					issquare = false;
//					break;
//				}
//			}
//			
//			j=0;
//			while (slicesXY.hasMoreTokens() && issquare) {				
//				try {
//					Double value = Double.parseDouble(slicesXY.nextToken());
//					if(value>=0){
//						data[i][j] = value;
//						allnan = false;
//					}
//					else
//						data[i][j] = Double.NaN;
//				} catch (NumberFormatException e) {
//					data[i][j] = Double.NaN;
//				}
//				j++;
//			}
//			i++;
//		}
//		
//		if(allnan){
//			data = null;
//	    	isempty = true;
//			return;
//		}
//		
//		if(!issquare){
//			BeatsErrorLog.addError("Data is not square.");
//			data = null;
//	    	isempty = true;
//			return;
//		}
//		
//    	isempty = nTime==0 && nVTypes==0;
//		
//    }    
    
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  

	public boolean isEmpty() {
		return data.length==0;
	}

	public Integer getLength() {
		return data.length;
	}
	
    public Double [] getData(){
    	return data;
    }
    
    public Double get(int i){
    	if(data.length==0)
    		return Double.NaN;
   		return data[i];
    }

	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  
    
    public void set(int i,Double f){
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
			if(data[i].isNaN())
				return true;
    	return false;
    }
    
}
