package edu.berkeley.path.beats.simulator;

public class BeatsTimeProfile {

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
    }
    
	/////////////////////////////////////////////////////////////////////
	// public interface
	/////////////////////////////////////////////////////////////////////  

	public boolean isEmpty() {
		if(data==null)
			return true;
		return data.length==0;
	}

	public Integer getNumTime() {
		if(data==null)
			return 0;
		return data.length;
	}
	
    public double [] getData(){
    	return data;
    }
    
    public double get(int i){
    	if(data==null)
    		return Double.NaN;
    	if(data.length==0)
    		return Double.NaN;
   		return data[i];
    }

	/////////////////////////////////////////////////////////////////////
	// alter data
	/////////////////////////////////////////////////////////////////////  
    
    public void set(int i,double f){
    	if(data!=null)
    		data[i] = f;
    }
    
    public void multiplyscalar(double value){
    	if(data==null)
    		return;
    	int i;
    	for(i=0;i<data.length;i++)
    		data[i] *= value;	
    }
    
    public void addscalar(double value){
    	if(data==null)
    		return;
    	int i;
    	for(i=0;i<data.length;i++)
    		data[i] += value;	
    }
    
    public void copydata(BeatsTimeProfile in){
    	if(data==null)
    		return;
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
    	if(data==null)
    		return false;
    	for(int i=0;i<data.length;i++)
			if(Double.isNaN(data[i]))
				return true;
    	return false;
    }
    

}
