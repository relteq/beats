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
import java.util.Collections;
import java.util.List;

public final class InitialDensitySet extends edu.berkeley.path.beats.jaxb.InitialDensitySet {

	private Scenario myScenario;
	//private Double [][] initial_density; 	// [veh/meter] indexed by link and type
	//private Link [] link;					// ordered array of references
	private Integer [] vehicletypeindex; 	// index of vehicle types into global list
	protected double timestamp;
	private ArrayList<LinkDestinationIC> data;

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	protected void populate(Scenario myScenario){
		
		int i;
		
		this.myScenario = myScenario;
		int numDensity = getDensity().size();
		
		// allocate
		vehicletypeindex = myScenario.getVehicleTypeIndices(getVehicleTypeOrder());
		data = new ArrayList<LinkDestinationIC>();
		
		// copy profile information to arrays in extended object
		for(i=0;i<numDensity;i++)
			data.add(new LinkDestinationIC(getDensity().get(i),myScenario));
		
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

		// check that link/destination pairs are unique.
		Collections.sort(data);
	
		LinkDestinationIC previous = null;
		for(LinkDestinationIC current : data){	
			// stop if you get to invalid link ids (at the end of the sorted array)
			if(current.link==null)
				break;
			if(!current.isbackground && current.destination_network==null)
				break;
			if(previous!=null)
				if(current.equals(previous))
					SiriusErrorLog.addError("Initial density set contains two entries for link id="+ current.link.getId() + " and destination network id=" + current.destinationNetworkIdToString() ); 
			previous = current;
		}

		// validate each one
		for(LinkDestinationIC d : data)
			d.validate();
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	public double [][] getDensityPerDnAndVtForLinkIdInVeh(String linkid,ArrayList<Integer> dest_net_global_index){
		int numDestination = dest_net_global_index.size();
		double [][] d = SiriusMath.zeros(numDestination,myScenario.getNumVehicleTypes());
		for(LinkDestinationIC ld : data){
			if(ld.hasvalidrefs){				
				if(ld.link.getId().equals(linkid)){
					// find ld's destination in the array of destinations
					int dest_index = dest_net_global_index.indexOf(ld.dn_global_index);
					if(dest_index>=0)
						for(int j=0;j<vehicletypeindex.length;j++)
							d[dest_index][vehicletypeindex[j]] = ld.initial_density[j]*ld.link.getLengthInMeters();
				}
			}
		}
		return d;
	}

//	public double[][] getInitial_density() {
//		return initial_density;
//	}

//	public Link[] getLink() {
//		return link;
//	}

	public Integer[] getVehicletypeindex() {
		return vehicletypeindex;
	}

	public double getTimestamp() {
		return timestamp;
	}

	/////////////////////////////////////////////////////////////////////
	// nested class
	/////////////////////////////////////////////////////////////////////
	
	private class LinkDestinationIC implements Comparable<LinkDestinationIC> {
		private double [] initial_density; 	// [veh/mile] by type
		private Link link;
		private boolean isbackground;
		private DestinationNetworkBLA destination_network;
		private int dn_global_index;
		private boolean hasvalidrefs;

		public LinkDestinationIC(edu.berkeley.path.beats.jaxb.Density jaxbD,Scenario myScenario){
			// read destination link.
			// if not specified, this density applies to background flow.
			
			String dest_net_id = jaxbD.getDestinationNetworkId();
			isbackground = dest_net_id==null;
			destination_network = myScenario.getDestinationNetworkWithId(dest_net_id);
			
			if(isbackground)
				dn_global_index = 0;
			else
				dn_global_index = destination_network==null ? -1 : destination_network.myIndex;	
			
			link = myScenario.getLinkWithId(jaxbD.getLinkId());
			if(link!=null){
				Double1DVector D = new Double1DVector(jaxbD.getContent(),":");
				initial_density = D.getData();
			}

			// true if it has valid link reference and is either background or
			// has a valid destination reference
			hasvalidrefs = link!=null && (isbackground || dn_global_index>=0);
		}

		public String destinationNetworkIdToString(){
			if(isbackground)
				return "background";
			return destination_network==null ? "unknown destination network" : destination_network.dnetwork.getId();
		}
		
		protected void validate(){
			
			// check size of data
			if(link!=null)
				if(initial_density.length!=vehicletypeindex.length)
					SiriusErrorLog.addError("Number of density values does not match number of vehicle types in initial density profile id=" + getId());

			// check that values are between 0 and jam density			
			if(link==null)
				SiriusErrorLog.addWarning("Unknown link id in initial density set.");

			// bad network link id
			if(!isbackground && destination_network==null)
				SiriusErrorLog.addWarning("Destination network not found in destination networks in initial density set.");
			
			if(link!=null){
				double sum = 0.0;
				double x;
				for(int j=0;j<vehicletypeindex.length;j++){
					x = initial_density[j];
					if(x<0)
						SiriusErrorLog.addError("Negative value found in initial density set for link id=" + link.getId());
					if( Double.isNaN(x) )
						SiriusErrorLog.addError("Invalid value found in initial density set for link id=" + link.getId());
					sum += x;
				}
			}
			
//			if(link!=null)
//			if(!link.issource){	// does not apply to source links
				// NOTE: REMOVED THIS CHECK TEMPORARILY. NEED TO DECIDE HOW TO DO IT 
				// WITH ENSEMBLE FUNDAMENTAL DIAGRAMS
//					if(sum>link[i].getDensityJamInVPMPL())
//						SiriusErrorLog.addErrorMessage("Initial density exceeds jam density.");
//			}
		
	
		}

		@Override
		public int compareTo(LinkDestinationIC that) {

			if(that==null)
				return 1;
			
			int compare;
			
			// first ordering by reference validity
			Boolean thisisinvalid = !this.hasvalidrefs;
			Boolean thatisinvalid = !that.hasvalidrefs;
			compare = thisisinvalid.compareTo(thatisinvalid);
			if(compare!=0)
				return compare;
			
			if(thisisinvalid && thatisinvalid)
				return 0;
			
			// second ordering by background
			Boolean thisisbackground = this.isbackground;
			Boolean thatisbackground = that.isbackground;
			compare = thisisbackground.compareTo(thatisbackground);
			if(compare!=0)
				return compare;
			
			// third ordering by link id
			String thislink = this.link.getId();
			String thatlink = that.link.getId();
			compare = thislink.compareTo(thatlink);
			if(compare!=0)
				return compare;
			
			if(thisisbackground && thatisbackground)
				return 0;
			
			// fourth ordering by destination id
			String thisdestination = this.destination_network.dnetwork.getId();
			String thatdestination = that.destination_network.dnetwork.getId();
			compare = thisdestination.compareTo(thatdestination);
			if(compare!=0)
				return compare;
			
			return 0;
		}
	
	}	
	
}
