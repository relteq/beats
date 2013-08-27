package edu.berkeley.path.beats.simulator;

public class BeatsTimeProfile {

	private double start_time;	// [seconds after midnight]
	private double time_step;	// [seconds]
	private double [] data;
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
    public BeatsTimeProfile(int n,double val) {
    	data = new double[n];
    	for(int i=0;i<n;i++)
            data[i] = val;
    }

    public BeatsTimeProfile(String str) {    	
    	data = BeatsFormatter.readCSVstring(str,",");
    }    

    // initialize a 1D vector from comma separated string of positive numbers
    // negative numbers get replaced with nan.
    public BeatsTimeProfile(String str,String delim) {
    	
    	data = BeatsFormatter.readCSVstring(str,delim);
    	
//      	if ((str.isEmpty()) || (str.equals("\n")) || (str.equals("\r\n"))){
//			return;
//    	}
//    	
//    	str.replaceAll("\\s","");
//    	
//    	// populate data
//		StringTokenizer slicesX = new StringTokenizer(str,delim);
//		int i=0;
//		boolean allnan = true;
//		data = new double[slicesX.countTokens()];
//		while (slicesX.hasMoreTokens()) {			
//			try {
//				double value = Double.parseDouble(slicesX.nextToken());
//				if(value>=0){
//					data[i] = value;
//					allnan = false;
//				}
//				else
//					data[i] = Double.NaN;
//			} catch (NumberFormatException e) {
//				data[i] = Double.NaN;
//			}
//			i++;
//		}
//		if(allnan)
//			data = null;
    }
    
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  

	public boolean isEmpty() {
		return data.length==0;
	}

	public Integer getNumTime() {
		return data.length;
	}
	
    public double [] getData(){
    	return data;
    }
    
    public double get(int i){
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
    
    public void copydata(BeatsTimeProfile in){
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
    	for(int i=0;i<data.length;i++)
			if(Double.isNaN(data[i]))
				return true;
    	return false;
    }
    

}
