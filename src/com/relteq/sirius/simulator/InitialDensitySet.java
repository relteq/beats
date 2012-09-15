/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InitialDensitySet extends com.relteq.sirius.jaxb.InitialDensitySet {

	private Scenario myScenario;
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
		
		// round to the nearest decisecond
		if(getTstamp()!=null)
			timestamp = SiriusMath.round(getTstamp().doubleValue()*10.0)/10.0;
		else
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
	
	public double [][] getDensityForLinkIdInVeh(String linkid,ArrayList<Integer> dest_net_global_index){
		int numDestination = dest_net_global_index.size();
		double [][] d = SiriusMath.zeros(numDestination,myScenario.getNumVehicleTypes());
		for(LinkDestinationIC ld : data){
			if(ld.hasvalidrefs){				
				if(ld.link.getId().equals(linkid)){
					// find ld's destination in the array of destinations
					int dest_index = dest_net_global_index.indexOf(ld.dn_global_index);
					if(dest_index>=0)
						for(int j=0;j<vehicletypeindex.length;j++)
							d[dest_index][vehicletypeindex[j]] = ld.initial_density[j]*ld.link.getLengthInMiles();
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
	// database API
	/////////////////////////////////////////////////////////////////////
	
	public class Tuple {
		private String link_id;
		private String network_id;
		private int vehicle_type_index;
		private double density;
		public Tuple(String link_id, String network_id, int vehicle_type_index,
				double density) {
			this.link_id = link_id;
			this.network_id = network_id;
			this.vehicle_type_index = vehicle_type_index;
			this.density = density;
		}
		/**
		 * @return the link id
		 */
		public String getLinkId() {
			return link_id;
		}
		/**
		 * @return the network id
		 */
		public String getNetworkId() {
			return network_id;
		}
		/**
		 * @return the vehicle type index
		 */
		public int getVehicleTypeIndex() {
			return vehicle_type_index;
		}
		/**
		 * @return the density, in vehicles
		 */
		public double getDensity() {
			return density;
		}
	}

	/**
	 * Constructs a list of initial densities,
	 * along with the corresponding link identifiers and vehicle types
	 * @return a list of <code/><link id, network id, vehicle type index, density></code> tuples
	 */
	public List<Tuple> getData() {
		List<Tuple> tuples = new ArrayList<Tuple>(data.size() * vehicletypeindex.length);
		String network_id = "";
		for (int iii = 0; iii < data.size(); ++iii){
			LinkDestinationIC ic = data.get(iii);
			if(!ic.hasvalidrefs)
				continue;
			for (int jjj = 0; jjj < vehicletypeindex.length; ++jjj){
				tuples.add(new Tuple(ic.link.getId(), network_id,
						vehicletypeindex[jjj].intValue(),
						ic.initial_density[jjj] * ic.link.getLengthInMiles()));
			}
		}
		return tuples;		
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

		public LinkDestinationIC(com.relteq.sirius.jaxb.Density jaxbD,Scenario myScenario){
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
