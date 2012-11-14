package com.relteq.sirius.simulator;

import java.util.ArrayList;
import java.util.HashMap;
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
		
		// special case for background networks
		if(dnetwork==null){ 
			// add it to all links and nodes in the scenario
			for(com.relteq.sirius.jaxb.Network network : myScenario.getNetworkList().getNetwork()){
				for(com.relteq.sirius.jaxb.Link jlink : network.getLinkList().getLink() )
					((Link)jlink).addDestinationNetwork(myIndex);
				for(com.relteq.sirius.jaxb.Node jnode : network.getNodeList().getNode() )
					((Node)jnode).addDestinationNetwork(myIndex,null,null);
			}
			return;
		}
		
		if(dnetwork.getLinkReferences()!=null){

			HashMap<Node,ArrayList<Link>> node2outlinks = new HashMap<Node,ArrayList<Link>>();
			HashMap<Node,ArrayList<Link>> node2inlinks = new HashMap<Node,ArrayList<Link>>();
			
			// loop through link references
			for(com.relteq.sirius.jaxb.LinkReference linkref : dnetwork.getLinkReferences().getLinkReference()){
				Link link = myScenario.getLinkWithId(linkref.getId());
				links.add(link);
				
				// register valid links and record their nodes
				if(link!=null){
					link.addDestinationNetwork(myIndex);
					if(!link.issource){
						Node node = link.begin_node;
						myOutNodes.add(node);
						if(!node2outlinks.containsKey(node))
							node2outlinks.put(node, new ArrayList<Link>());
						node2outlinks.get(node).add(link);
					}
					if(!link.issink){
						Node node = link.end_node;
						myInNodes.add(node);
						if(!node2inlinks.containsKey(node))
							node2inlinks.put(node, new ArrayList<Link>());
						node2inlinks.get(node).add(link);
					}
				}
			}
			
			// register the nodes
			// (validation will check that myOutNodes==myInNodes, so we can just register myOutNodes)
			for(Node node : myOutNodes)
				node.addDestinationNetwork(myIndex,node2inlinks.get(node),node2outlinks.get(node));
			
		}
	}

	/** @y.exclude */ 	
	protected void validate() {
		
		// dont validate background network
		if(dnetwork==null)
			return;
		
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
