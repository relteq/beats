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

final class FundamentalDiagram extends edu.berkeley.path.beats.jaxb.FundamentalDiagram{

	protected Link myLink;
	protected double lanes;
	protected Double _densityJam;     		// [veh] 
	protected Double _capacity;   			// [veh] 
	protected Double _capacityDrop;     	// [veh] 
	protected Double _vf;                	// [-]
	protected Double _w;                	// [-]
	protected Double std_dev_capacity;		// [veh]
	protected Double density_critical;		// [veh]

	/////////////////////////////////////////////////////////////////////
	// construction 
	/////////////////////////////////////////////////////////////////////

	public FundamentalDiagram(){};
	
	public FundamentalDiagram(Link myLink){
		this.myLink       = myLink;
		this.lanes 		  = myLink==null ? Double.NaN : myLink._lanes;
		_densityJam 	  = Double.NaN;  
	    _capacity  		  = Double.NaN;
		_capacityDrop 	  = Double.NaN; 
	    _vf 			  = Double.NaN; 
	    _w 				  = Double.NaN; 
	    std_dev_capacity  = Double.NaN;
	    density_critical  = Double.NaN;
	}
	
	// fundamental diagram created from jaxb objects must have all values filled in. 
	public FundamentalDiagram(Link myLink,edu.berkeley.path.beats.jaxb.FundamentalDiagram jaxbfd){
		this.myLink       = myLink;
		this.lanes 		  = myLink==null ? Double.NaN : myLink._lanes;
		_densityJam 	  = Double.NaN;  
	    _capacity  		  = Double.NaN;
		_capacityDrop 	  = Double.NaN; 
	    _vf 			  = Double.NaN; 
	    _w 				  = Double.NaN; 
	    std_dev_capacity  = Double.NaN;
	    density_critical  = Double.NaN;
	    
	    if(myLink==null)
	    	return;
	    
	    // procedure for determining fd parameters.
	    // jaxbfd may contain up to 4 of: densityJam, capacity, vf, and w. 
	    // if any of these is missing, assume the fd is triangular. 
	    // if more than one is missing, use default values up to 3 values, then define the fourth with triangle. 
	    // The order of default application is capacity, v, w, densityjam.
	    
	    // record jaxbfd values and undefined parameters
	    int nummissing = 0;
	    boolean missing_capacity, missing_vf, missing_w, missing_densityJam;
	    double value;
	    double simDtInSeconds = myLink.myNetwork.myScenario.getSimDtInSeconds();
	    
		if(jaxbfd.getCapacity()!=null){
			value = jaxbfd.getCapacity().doubleValue();			// [veh/second/lane]
			_capacity = value * lanes * simDtInSeconds;
			missing_capacity = false;
		} 
		else{
			nummissing++;
			missing_capacity = true;
		}
	    
		if(jaxbfd.getFreeFlowSpeed()!=null){
			value = jaxbfd.getFreeFlowSpeed().doubleValue();		// [meters/second]
			_vf = value * simDtInSeconds / myLink.getLengthInMeters();
			missing_vf = false;
		} 
		else{
			nummissing++;
			missing_vf = true;
		}
	
		if(jaxbfd.getCongestionSpeed()!=null){
			value = jaxbfd.getCongestionSpeed().doubleValue();		// [meters/second]
			_w = value * simDtInSeconds / myLink.getLengthInMeters();
			missing_w = false;
		} 
		else{
			nummissing++;
			missing_w = true;
		}
	
		if(jaxbfd.getJamDensity()!=null){
			value = jaxbfd.getJamDensity().doubleValue();		// [veh/meter/lane]
			_densityJam = value * lanes * myLink.getLengthInMeters();
			missing_densityJam = false;
		} 
		else{
			nummissing++;
			missing_densityJam = true;
		}

		// in order, check for missing values and fill in until we are able to make triangle
		if(missing_capacity && nummissing>1){
			_capacity = Defaults.capacity * lanes * simDtInSeconds;
			nummissing--;
		}

		if(missing_vf && nummissing>1){
			_vf = Defaults.vf * simDtInSeconds / myLink.getLengthInMeters();
			nummissing--;
		}

		if(missing_w && nummissing>1){
			_w = Defaults.w * simDtInSeconds / myLink.getLengthInMeters();
			nummissing--;
		}

		if(missing_densityJam && nummissing>1){
			_densityJam = Defaults.densityJam * lanes * myLink.getLengthInMeters();
			nummissing--;
		}
	    
	    // now there should be no more than one missing value
		if(nummissing>1)
			System.out.println("BIG MISTAKE!!!!");
	    
		// if there is one missing, compute it with triangular assumption
		
		if(nummissing==1){
			if(missing_capacity)
				_capacity = _densityJam / (1.0/_vf + 1.0/_w);
			if(missing_vf)
				_vf = 1.0 / ( _densityJam/_capacity - 1.0/_w );
			if(missing_w)
				_w = 1.0 / ( _densityJam/_capacity - 1.0/_vf );
			if(missing_densityJam)
				_densityJam = _capacity*(1.0/_vf + 1.0/_w);
		}
		
		// set capacity drop and capacity uncertainty
		if(jaxbfd.getStdDevCapacity()!=null){
			value = jaxbfd.getStdDevCapacity().doubleValue();	// [veh/second/lane]
			std_dev_capacity = value * lanes * simDtInSeconds;
		}
		else{
			std_dev_capacity = 0.0;
		}
		
		if(jaxbfd.getCapacityDrop()!=null){
			value = jaxbfd.getCapacityDrop().doubleValue();		// [veh/second/lane]
			_capacityDrop = value * lanes * simDtInSeconds;
		} 
		else{
			_capacityDrop = Defaults.capacityDrop * lanes * simDtInSeconds;
		}
        
		// set critical density
		density_critical = _capacity/_vf;
	}
	
	// fundamental diagrams created from other fundamental diagrams copy all values. 
	public FundamentalDiagram(Link myLink,edu.berkeley.path.beats.simulator.FundamentalDiagram fd){
		if(myLink==null)
			return;
		this.myLink = myLink;
		this.lanes = myLink._lanes;
		_densityJam 	  = Double.NaN;  
	    _capacity  = Double.NaN;
		_capacityDrop 	  = Double.NaN; 
	    _vf 			  = Double.NaN; 
	    _w 				  = Double.NaN; 
	    std_dev_capacity  = Double.NaN;
	    density_critical  = Double.NaN;
	    
	    this.copyfrom(fd);		// copy and normalize
	}
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////

	// we do not have to worry about getters returning NaN:
	// they are only called for fundamental diagrams belonging
	// to links, these are initialized with default values, and 
	// copyfrom only replaces with non-nan values.

	protected Double _getDensityJamInVeh() {
		return _densityJam;
	}

	protected Double _getCapacityInVeh() {
		return _capacity;
	}

	protected Double _getCapacityDropInVeh() {
		return _capacityDrop;
	}

	protected double getVfNormalized() {
		return _vf;
	}

	protected double getWNormalized() {
		return _w;
	}

	protected Double getDensityCriticalInVeh() {
		return density_critical;
	}

	protected void setLanes(double newlanes){
		if(newlanes<=0)
			return;
		if(BeatsMath.equals(newlanes,lanes))
			return;
		double alpha = newlanes/lanes;
		_densityJam 	  *= alpha; 
		_capacity  		  *= alpha; 
		_capacityDrop 	  *= alpha; 
		density_critical  *= alpha;
		lanes = newlanes;
	}
	
	/////////////////////////////////////////////////////////////////////
	// reset / validate
	/////////////////////////////////////////////////////////////////////

	// assign default values parameters and normalize
 	protected void settoDefault(){
		if(myLink==null)
			return;
		double simDtInSeconds = myLink.myNetwork.myScenario.getSimDtInSeconds();
		double lengthInMeters = myLink.getLengthInMeters();
		_densityJam 	  = Defaults.densityJam		* lanes * lengthInMeters;
		_capacity  		  = Defaults.capacity		* lanes * simDtInSeconds;
		_capacityDrop 	  = Defaults.capacityDrop	* lanes * simDtInSeconds;
		_vf = Defaults.vf * simDtInSeconds / lengthInMeters;
		_w  = Defaults.w  * simDtInSeconds / lengthInMeters;
		density_critical = _capacity / _vf;
	}

 	// copy per lane parameters from jaxb and normalize
	protected void copyfrom(edu.berkeley.path.beats.jaxb.FundamentalDiagram fd){

		if(fd==null)
			return;
		if(myLink==null)
			return;
		
		double value;
		double simDtInSeconds = myLink.myNetwork.myScenario.getSimDtInSeconds();

		if(fd.getJamDensity()!=null){
			value = fd.getJamDensity().doubleValue();		// [veh/meter/lane]
			_densityJam = value * lanes * myLink.getLengthInMeters();
		} 

		if(fd.getCapacity()!=null){
			value = fd.getCapacity().doubleValue();			// [veh/second/lane]
			_capacity = value * lanes * simDtInSeconds;
		} 
		
		if(fd.getStdDevCapacity()!=null){
			value = fd.getStdDevCapacity().doubleValue();	// [veh/second/lane]
			std_dev_capacity = value * lanes * simDtInSeconds;
		}
		
		if(fd.getCapacityDrop()!=null){
			value = fd.getCapacityDrop().doubleValue();		// [veh/second/lane]
			_capacityDrop = value * lanes * simDtInSeconds;
		} 
		
		if(fd.getFreeFlowSpeed()!=null){
			value = fd.getFreeFlowSpeed().doubleValue();		// [meters/second]
			_vf = value * simDtInSeconds / myLink.getLengthInMeters();
		}

		if(fd.getCongestionSpeed()!=null){
			value = fd.getCongestionSpeed().doubleValue();		// [meters/second]
			_w = value * simDtInSeconds / myLink.getLengthInMeters();
		}

		density_critical =  _capacity / _vf;
        
	}

 	// clone a fd
	protected void copyfrom(edu.berkeley.path.beats.simulator.FundamentalDiagram that){
		if(that==null)
			return;
		this.myLink = that.myLink;
		this._capacity = that._capacity;
		this._capacityDrop = that._capacityDrop;
		this._densityJam = that._densityJam;
		this._vf = that._vf;
		this._w = that._w;
		this.density_critical = that.density_critical;
		this.lanes = that.lanes;
		this.std_dev_capacity = that.std_dev_capacity;
	}	
	
	protected void reset(Scenario.UncertaintyType uncertaintyModel){
		if(myLink==null)
			return;
		// set lanes back to original value
		setLanes(myLink.get_Lanes());
	}
	
	// produce a sample fundamental diagram with this one as expected value.
	protected FundamentalDiagram perturb(){
		if(myLink==null)
			return null;
		// make a copy of this fundamental diagram
		FundamentalDiagram samp = new FundamentalDiagram(myLink,this);
		
		// perturb it
		if(!std_dev_capacity.isNaN() && std_dev_capacity>0){
			switch(myLink.myNetwork.myScenario.uncertaintyModel){
			case uniform:
				samp._capacity += BeatsMath.sampleZeroMeanUniform(std_dev_capacity);
				break;

			case gaussian:
				samp._capacity += BeatsMath.sampleZeroMeanUniform(std_dev_capacity);
				break;
			}			
		}
		
		// adjustments to sampled fd:
		
		// non-negativity
		samp._capacity = Math.max(samp._capacity,0.0);
		
		// density_critical no greater than dens_crit_congestion
		double dens_crit_congestion = samp._densityJam-samp._capacity/samp._w;	// [veh]
		if(BeatsMath.greaterthan(samp.density_critical,dens_crit_congestion)){
			samp.density_critical = dens_crit_congestion;
			samp._capacity = samp._vf * samp.density_critical;
		}

		return samp;
	}
	
	protected void validate(){
				
		if(myLink==null)
			return;
		
//		if(_vf.isNaN() || _w.isNaN() || _densityJam.isNaN() || _capacity.isNaN() || _capacityDrop.isNaN())
//			BeatsErrorLog.addError("Undefined fundamental diagram parameters for link id=" + myLinkIdparameters in the fundamental diagram.");
		
		if(_vf<0 || _w<0 || _densityJam<0 || _capacity<0 || _capacityDrop<0)
			BeatsErrorLog.addError("Negative fundamental diagram parameters for link id=" + myLink.getId());

		double dens_crit_congestion = _densityJam-_capacity/_w;	// [veh]
			
		if(BeatsMath.greaterthan(density_critical,dens_crit_congestion))
			BeatsErrorLog.addError("Minimum allowable critical density for link " + myLink.getId() + " is " + dens_crit_congestion + "(current="+density_critical+")");
		
		if(_vf>1)
			BeatsErrorLog.addError("CFL condition violated, FD for link " + myLink.getId() + " has vf=" + _vf);

		if(_w>1)
			BeatsErrorLog.addError("CFL condition violated, FD for link " + myLink.getId() + " has w=" + _w);
		
		if(myLink!=null)
			for(int e=0;e<myLink.myNetwork.myScenario.numEnsemble;e++)
				if(myLink.getTotalDensityInVeh(e)>_densityJam)
					BeatsErrorLog.addError("Initial density=" + myLink.getTotalDensityInVeh(e) + " of link id=" + myLink.getId() + " exceeds jam density=" + _densityJam);
	}

}
