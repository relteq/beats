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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class DestinationNetworkBLA {
	
	protected edu.berkeley.path.beats.jaxb.DestinationNetwork dnetwork;
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
    
	public DestinationNetworkBLA(edu.berkeley.path.beats.jaxb.DestinationNetwork dnetwork,Scenario myScenario,int myIndex){
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
			for(edu.berkeley.path.beats.jaxb.Network network : myScenario.getNetworkList().getNetwork()){
				for(edu.berkeley.path.beats.jaxb.Link jlink : network.getLinkList().getLink() )
					((Link)jlink).addDestinationNetwork(myIndex);
				for(edu.berkeley.path.beats.jaxb.Node jnode : network.getNodeList().getNode() )
					((Node)jnode).addDestinationNetwork(myIndex,null,null);
			}
			return;
		}
		
		if(dnetwork.getLinkReferences()!=null){

			HashMap<Node,ArrayList<Link>> node2outlinks = new HashMap<Node,ArrayList<Link>>();
			HashMap<Node,ArrayList<Link>> node2inlinks = new HashMap<Node,ArrayList<Link>>();
			
			// loop through link references
			for(edu.berkeley.path.beats.jaxb.LinkReference linkref : dnetwork.getLinkReferences().getLinkReference()){
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
