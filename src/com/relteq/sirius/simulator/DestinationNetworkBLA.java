package com.relteq.sirius.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class DestinationNetworkBLA {
	
	protected com.relteq.sirius.jaxb.DestinationNetwork dnetwork;
	protected Scenario myScenario;
	protected int myIndex;
	
	// these are used to verify the topology of the destination network
	// the setdiff of these two should be empty
	protected ArrayList<Link> links = new ArrayList<Link>();
	protected Set<Node> myInNodes = new HashSet<Node>();
	protected Set<Node> myOutNodes = new HashSet<Node>();

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
    
	public DestinationNetworkBLA(com.relteq.sirius.jaxb.DestinationNetwork dnetwork,Scenario myScenario,int myIndex){
		this.dnetwork = dnetwork;
		this.myScenario = myScenario;
		this.myIndex = myIndex;
	}

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
    
	/** @y.exclude */ 
	protected void populate() {
		if(dnetwork.getLinkReferences()!=null){
			for(com.relteq.sirius.jaxb.LinkReference linkref : dnetwork.getLinkReferences().getLinkReference()){
				Link link = myScenario.getLinkWithId(linkref.getId());
				links.add(link);
				if(link!=null){
					link.addDestination(myIndex);
					if(!link.issource)
						myOutNodes.add(link.begin_node);
					if(!link.issink)
						myInNodes.add(link.end_node);
				}
			}
		}
	}

	/** @y.exclude */ 	
	protected void validate() {
		
		// all non-terminal nodes must have inputs and outputs
		if(!SiriusMath.setEquals(myOutNodes,myInNodes))
			SiriusErrorLog.addError("Not all nodes in destination network " + dnetwork.getId() + " have inputs and outputs");

		// no bad link references
		boolean foundit = false;
		for(Link link : links){
			if(link==null)
				SiriusErrorLog.addError("Bad link id in destination network " + dnetwork.getId());
			else{
				if(link.issink)
					if(!foundit)
						foundit = true;
					else
						SiriusErrorLog.addError("Multiple destinations in destination network " + dnetwork.getId());
			}
		}
		
		if(!foundit)
			SiriusErrorLog.addError("No destinations in destination network " + dnetwork.getId());

	}
}
