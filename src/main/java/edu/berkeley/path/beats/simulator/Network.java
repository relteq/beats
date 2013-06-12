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

/** Network class
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public final class Network extends edu.berkeley.path.beats.jaxb.Network {

	private boolean isempty;
	private Scenario myScenario;
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {
		
		this.myScenario = myScenario;
		this.isempty = getNodeList()==null || getLinkList()==null;
		
		if(isempty)
			return;
	
		for (edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
			((Node) node).populate(this);
	
		for (edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink())
			((Link) link).populate(this);
		
	}
	
	public boolean isEmpty() {
		return isempty;
	}

	protected void validate() {

		if(isempty)
			return;
		
		if(myScenario.getSimdtinseconds()<=0)
			BeatsErrorLog.addError("Non-positive simulation step size (" + myScenario.getSimdtinseconds() +").");
		
		// node list
		for (edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
			((Node)node).validate();

		// link list
		for (edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink())
			((Link)link).validate();
	}

	protected void reset(Scenario.ModeType simulationMode) throws BeatsException {

		if(isempty)
			return;
		
		// node list
		for (edu.berkeley.path.beats.jaxb.Node node : getNodeList().getNode())
			((Node) node).reset();

		// link list
		for (edu.berkeley.path.beats.jaxb.Link link : getLinkList().getLink())
			((Link) link).reset(simulationMode);

	}

	protected void update() throws BeatsException {

		if(isempty)
			return;
		
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

	public boolean isIsempty() {
		return isempty;
	}

	public Scenario getMyScenario() {
		return myScenario;
	}
		
	/** Get link with given id.
	 * @param id String id of the link.
	 * @return Link object.
	 */
	public Link getLinkWithId(String id){
		if(isempty)
			return null;
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
		if(isempty)
			return null;
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
		if(isempty)
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
		if(isempty)
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
