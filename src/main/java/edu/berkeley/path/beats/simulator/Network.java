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

import java.util.List;

/** Network in a scenario. 
 * <p>
 * A network is a collection of links, nodes, sensors, and signals that is
 * a) connected and b) limited by terminal nodes on all source and sink links. 
 * All elements within the network can be referred to by element id at the 
 * network level, or by composite (network id,element id) at the scenario level.
 * This class provides access to individual elements (links, nodes,
 * sensors, and signals) and to lists of elements.
* @author Gabriel Gomes
* @version VERSION NUMBER
*/
public final class Network extends edu.berkeley.path.beats.jaxb.Network {

	protected Scenario myScenario;
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {
		
		this.myScenario = myScenario;
		
		if(getNodeList()!=null)
			for (edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
				((Node) node).populate(this);
		
		if(getLinkList()!=null)
			for (edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink())
				((Link) link).populate(this);
		
	}
	
	protected void constructLinkNodeMaps(){
		Node node;
		if(getLinkList()!=null)
			for (com.relteq.sirius.jaxb.Link jlink : getLinkList().getLink()){
				Link link = (Link) jlink;
				node = link.end_node;
				if(!node.isTerminal)
					for(Integer x : link.myDNindex)
						link.dn_endNodeMap.add(node.myDNGlobalIndex.indexOf(x));
				node = link.begin_node;
				if(!node.isTerminal)
					for(Integer x : link.myDNindex)
						link.dn_beginNodeMap.add(node.myDNGlobalIndex.indexOf(x));
			}
	}

	protected void validate() {

		if(myScenario.getSimDtInSeconds()<=0)
			SiriusErrorLog.addError("Non-positive simulation step size (" + myScenario.getSimDtInSeconds() +").");
		
		// node list
		if(getNodeList()!=null)
			for (edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
				((Node)node).validate();

		// link list
		if(getLinkList()!=null)
			for (edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink())
				((Link)link).validate();
	}

	protected void reset(Scenario.ModeType simulationMode) throws SiriusException {

		// node list
		if(getNodeList()!=null)
			for (edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
				((Node) node).reset();

		// link list
		if(getLinkList()!=null)
			for (edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink()){
				Link _link = (Link) link;
				_link.resetLanes();		
				_link.resetState(simulationMode);
				_link.resetFD();
			}
	}

	protected void update() throws SiriusException {
		
        // compute link demand and supply ...............
        for(edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink()){
        	((Link)link).updateOutflowDemand();
        	((Link)link).updateSpaceSupply();
        }
        
        // update nodes: compute flows on links .........
        for(edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
            ((Node)node).update();
        
        // update links: compute densities .............
        for(edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink())
        	((Link)link).update();
	}

	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

	/** Get link with given id.
	 * @param id String id of the link.
	 * @return Link object.
	 */
	public Link getLinkWithId(String id){
		id.replaceAll("\\s","");
		for(edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink()){
			if(link.getId().equals(id))
				return (Link) link;
		}
		return null;
	}

	/** Get node with given id.
	 * @param id String id of the node.
	 * @return Node object.
	 */
	public Node getNodeWithId(String id){
		id.replaceAll("\\s","");
		for(edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode()){
			if(node.getId().equals(id))
				return (Node) node;
		}
		return null;
	}

	/** Get the list of nodes in this network.
	 * @return List of all nodes as jaxb objects. 
	 * Each of these may be cast to a {@link Node}.
	 */
	public List<edu.berkeley.path.beats.jaxb.Node> getListOfNodes() {
		if(getNodeList()==null)
			return null;
		if(getNodeList().getNode()==null)
			return null;
		return getNodeList().getNode();
	}

	/** Get the list of links in this network.
	 * @return List of all links as jaxb objects. 
	 * Each of these may be cast to a {@link Link}.
	 */
	public List<edu.berkeley.path.beats.jaxb.Link> getListOfLinks() {
		if(getLinkList()==null)
			return null;
		if(getLinkList().getLink()==null)
			return null;
		return getLinkList().getLink();	
	}

//	/** Get the list of sensors in this network.
//	 * @return List of all sensors. 
//	 */
//	public List<edu.berkeley.path.beats.jaxb.Sensor> getListOfSensors() {
//		if(getSensorList()==null)
//			return null;
//		return getSensorList().getSensor();
//	}

	/**
	 * Retrieves a list of signals referencing nodes from this network
	 * @return a list of signals or null if the scenario's signal list is null
	 */
	public List<edu.berkeley.path.beats.jaxb.Signal> getListOfSignals() {
		if (null == myScenario.getSignalList()) return null;
		List<edu.berkeley.path.beats.jaxb.Signal> sigl = new java.util.ArrayList<edu.berkeley.path.beats.jaxb.Signal>();
		for (edu.berkeley.path.beats.jaxb.Signal sig : myScenario.getSignalList().getSignal()) {
			if (null != getNodeWithId(sig.getNodeId()))
				sigl.add(sig);
		}
		return sigl;
	}
}
