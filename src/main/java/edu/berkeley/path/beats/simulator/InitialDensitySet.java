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

import java.util.ArrayList;
import java.util.List;

public final class InitialDensitySet extends edu.berkeley.path.beats.jaxb.InitialDensitySet {

	private Scenario myScenario;
	private Double [][] initial_density; 	// [veh/meter] indexed by link and type
	private Link [] link;					// ordered array of references
	private Integer [] vehicletypeindex; 	// index of vehicle types into global list
	protected double timestamp;

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	protected void populate(Scenario myScenario){
		
		int i;
		
		this.myScenario = myScenario;
		
		// count links in initial density list that are also in the scenario
		int numLinks = getDensity().size();
		int numLinks_exist = 0;
		Link [] templink = new Link[numLinks];
		for(i=0;i<numLinks;i++){
			edu.berkeley.path.beats.jaxb.Density density = getDensity().get(i);
			templink[i] = myScenario.getLinkWithId(density.getLinkId());
			if(templink[i]!=null)
				numLinks_exist++;
		}
		
		// allocate
		initial_density = new Double [numLinks_exist][];
		link = new Link [numLinks_exist];
		vehicletypeindex = myScenario.getVehicleTypeIndices(getVehicleTypeOrder());

		// copy profile information to arrays in extended object
		int c = 0;
		for(i=0;i<numLinks;i++){
			if(templink[i]!=null){
				edu.berkeley.path.beats.jaxb.Density density = getDensity().get(i);
				link[c] = templink[i];
				Double1DVector D = new Double1DVector(density.getContent(),":");
				initial_density[c] = D.getData();
				c++;
			}
		}
		
		timestamp = 0.0;
		
	}

	protected void validate() {
		
		int i;
		
		// check that all vehicle types are accounted for
		if(vehicletypeindex.length!=myScenario.getNumVehicleTypes())
			SiriusErrorLog.addError("List of vehicle types in initial density profile id=" + getId() + " does not match that of settings.");
		
		// check that vehicle types are valid
		for(i=0;i<vehicletypeindex.length;i++)
			if(vehicletypeindex[i]<0)
				SiriusErrorLog.addError("Bad vehicle type name in initial density profile id=" + getId());
		
		// check size of data
		if(link!=null)
			for(i=0;i<link.length;i++)
				if(initial_density[i].length!=vehicletypeindex.length)
					SiriusErrorLog.addError("Number of density values does not match number of vehicle types in initial density profile id=" + getId());

		// check that values are between 0 and jam density
		int j;
		Double sum;
		Double x;
		for(i=0;i<initial_density.length;i++){
			
			if(link[i]==null){
				SiriusErrorLog.addWarning("Unknown link id in initial density profile");
				continue;
			}
			
			if(link[i].issource)	// does not apply to source links
				continue;
			
			sum = 0.0;
			for(j=0;j<vehicletypeindex.length;j++){
				x = initial_density[i][j];
				if(x<0)
					SiriusErrorLog.addError("Negative value found in initial density profile for link id=" + link[i].getId());
				if( x.isNaN())
					SiriusErrorLog.addError("Invalid value found in initial density profile for link id=" + link[i].getId());
				sum += x;
			}
			
			// NOTE: REMOVED THIS CHECK TEMPORARILY. NEED TO DECIDE HOW TO DO IT 
			// WITH ENSEMBLE FUNDAMENTAL DIAGRAMS
//			if(sum>link[i].getDensityJamInVPMPL())
//				SiriusErrorLog.addErrorMessage("Initial density exceeds jam density.");

		}		
	}

	protected void reset() {
	}

	protected void update() {
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	public Double [] getDensityForLinkIdInVeh(String network_id,String linkid){
		Double [] d = SiriusMath.zeros(myScenario.getNumVehicleTypes());
		for(int i=0;i<link.length;i++){
			if(link[i].getId().equals(linkid) && link[i].myNetwork.getId().equals(network_id)){
				for(int j=0;j<vehicletypeindex.length;j++)
					d[vehicletypeindex[j]] = initial_density[i][j] * link[i].getLengthInMeters();
				return d;
			}
		}
		return d;
	}

	public Double[][] getInitial_density() {
		return initial_density;
	}

	public Link[] getLink() {
		return link;
	}

	public Integer[] getVehicletypeindex() {
		return vehicletypeindex;
	}

	public double getTimestamp() {
		return timestamp;
	}

}
