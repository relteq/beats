/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.ArrayList;
import java.util.Collections;
//import java.util.List;

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
			if(!current.isbackground && current.destination_link==null)
				break;
			if(previous!=null)
				if(current.equals(previous))
					SiriusErrorLog.addError("Initial density set contains two entries for link id="+ current.link.getId() + " and destination id=" + current.getDestinationId() ); 
			previous = current;
		}

		// validate each one
		for(LinkDestinationIC d : data)
			d.validate();
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	public Double [][] getDensityForLinkIdInVeh(String linkid,ArrayList<Integer> destination){
		int numDestination = destination.size();
		Double [][] d = SiriusMath.zeros(numDestination,myScenario.getNumVehicleTypes());
		for(LinkDestinationIC ld : data){
			if(ld.hasvalidrefs){				
				if(ld.link.getId().equals(linkid)){
					// find ld's destination in the array of destinations
					int dest_index = destination.indexOf(ld.destination_network_index);
					if(dest_index>=0)
						for(int j=0;j<vehicletypeindex.length;j++)
							d[dest_index][vehicletypeindex[j]] = ld.initial_density[j]*ld.link.getLengthInMiles();
				}
			}
		}
		return d;
	}

	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////
	
	private class LinkDestinationIC implements Comparable<LinkDestinationIC> {
		Double [] initial_density; 	// [veh/mile] by type
		Link link;
		boolean isbackground;
		Link destination_link;
		int destination_network_index;
		boolean hasvalidrefs;

		public LinkDestinationIC(com.relteq.sirius.jaxb.Density jaxbD,Scenario myScenario){
			// read destination link.
			// if not specified, this density applies to background flow.
			if(jaxbD.getLinkIdDestination()==null){
				isbackground = true;
				destination_link = null;
				destination_network_index = -1;
			}
			else{
				isbackground = false;
				destination_link = myScenario.getLinkWithId(jaxbD.getLinkIdDestination());
				destination_network_index = -1;
				if(destination_link!=null)
					for(DestinationNetworkBLA destnet : myScenario.destination_networks){
						if(destnet.dnetwork.getLinkIdDestination().compareTo(destination_link.getId())==0)
							destination_network_index = destnet.myIndex;
					}
				
			}
			link = myScenario.getLinkWithId(jaxbD.getLinkId());
			if(link!=null){
				Double1DVector D = new Double1DVector(jaxbD.getContent(),":");
				initial_density = D.getData();
			}

			// true if it has valid link reference and is either background or
			// has a valid destination reference
			hasvalidrefs = link!=null && (isbackground || destination_network_index>=0);
		}

//		public String getLinkId(){
//			return link==null ? "unknown link id" : link.getId();
//		}
		
		public String getDestinationId(){
			if(isbackground)
				return "background";
			return destination_link==null ? "unknown link id" : destination_link.getId();
		}
		
		protected void validate(){

			
			// check size of data
			if(link!=null)
				if(initial_density.length!=vehicletypeindex.length)
					SiriusErrorLog.addError("Number of density values does not match number of vehicle types in initial density profile id=" + getId());

			// check that values are between 0 and jam density			
			if(link==null)
				SiriusErrorLog.addWarning("Unknown link id in initial density set.");

			// bad destination link id
			if(!isbackground && destination_link==null)
				SiriusErrorLog.addWarning("Destination link not found in destination networks in initial density set.");
			
			if(link!=null){
				Double sum = 0.0;
				Double x;
				for(int j=0;j<vehicletypeindex.length;j++){
					x = initial_density[j];
					if(x<0)
						SiriusErrorLog.addError("Negative value found in initial density set for link id=" + link.getId());
					if( x.isNaN())
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
			String thisdestination = this.destination_link.getId();
			String thatdestination = that.destination_link.getId();
			compare = thisdestination.compareTo(thatdestination);
			if(compare!=0)
				return compare;
			
			return 0;
		}
	}	
	
//	public class Tuple {
//		private String link_id;
//		private String network_id;
//		private int vehicle_type_index;
//		private double density;
//		public Tuple(String link_id, String network_id, int vehicle_type_index,
//				double density) {
//			this.link_id = link_id;
//			this.network_id = network_id;
//			this.vehicle_type_index = vehicle_type_index;
//			this.density = density;
//		}
//		/**
//		 * @return the link id
//		 */
//		public String getLinkId() {
//			return link_id;
//		}
//		/**
//		 * @return the network id
//		 */
//		public String getNetworkId() {
//			return network_id;
//		}
//		/**
//		 * @return the vehicle type index
//		 */
//		public int getVehicleTypeIndex() {
//			return vehicle_type_index;
//		}
//		/**
//		 * @return the density, in vehicles
//		 */
//		public double getDensity() {
//			return density;
//		}
//	}
//
//	/**
//	 * Constructs a list of initial densities,
//	 * along with the corresponding link identifiers and vehicle types
//	 * @return a list of <code/><link id, network id, vehicle type index, density></code> tuples
//	 */
//	public List<Tuple> getData() {
//		List<Tuple> data = new ArrayList<Tuple>(link.length * vehicletypeindex.length);
//		for (int iii = 0; iii < link.length; ++iii)
//			for (int jjj = 0; jjj < vehicletypeindex.length; ++jjj)
//				data.add(new Tuple(link[iii].getId(), link[iii].myNetwork.getId(),
//						vehicletypeindex[jjj].intValue(),
//						initial_density[iii][jjj] * link[iii].getLengthInMiles()));
//		return data;
//	}

}
