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

package edu.berkeley.path.beats.event;

import java.math.BigDecimal;

import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Event;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;

public class Event_Fundamental_Diagram extends Event {

	protected boolean resetToNominal;
	protected edu.berkeley.path.beats.jaxb.FundamentalDiagram FD;
	  
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////
	
	public Event_Fundamental_Diagram(){
	}

	public Event_Fundamental_Diagram(Scenario myScenario,edu.berkeley.path.beats.jaxb.Event jaxbE,Event.Type myType){
		super(myScenario, jaxbE, myType);
	}
	
//	public Event_Fundamental_Diagram(Scenario myScenario,List <Link> links,double freeflowSpeed,double congestionSpeed,double capacity,double densityJam,double capacityDrop,double stdDevCapacity) {		
//		this.FD = new edu.berkeley.path.beats.jaxb.FundamentalDiagram();
//		this.FD.setFreeFlowSpeed(new BigDecimal(freeflowSpeed));
//		this.FD.setCongestionSpeed(new BigDecimal(congestionSpeed));
//		this.FD.setCapacity(new BigDecimal(capacity));
//		this.FD.setJamDensity(new BigDecimal(densityJam));
//		this.FD.setCapacityDrop(new BigDecimal(capacityDrop));
//		this.FD.setStdDevCapacity(new BigDecimal(stdDevCapacity));
//		this.resetToNominal = false;
//		this.targets = new ArrayList<ScenarioElement>();
//		for(Link link : links)
//			this.targets.add(ObjectFactory.createScenarioElement(link));
//	}
//	
//	public Event_Fundamental_Diagram(Scenario myScenario,List <Link> links) {		
//		this.resetToNominal = true;
//		for(Link link : links)
//			this.targets.add(ObjectFactory.createScenarioElement(link));
//	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / activate
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		edu.berkeley.path.beats.jaxb.Event jaxbe = (edu.berkeley.path.beats.jaxb.Event) jaxbobject;
		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) jaxbe.getParameters();
		// reset_to_nominal
		if (null != params && params.has("reset_to_nominal"))
			this.resetToNominal = params.get("reset_to_nominal").equalsIgnoreCase("true");
		else
			this.resetToNominal = false;
		// FD
		if (null != params) {
			this.FD = new edu.berkeley.path.beats.jaxb.FundamentalDiagram();
			
			if (params.has("capacity")) 
				this.FD.setCapacity(Double.parseDouble(params.get("capacity")));
			else
				this.FD.setCapacity(Double.NaN);
			
			if (params.has("capacity_drop")) 
				this.FD.setCapacityDrop(Double.parseDouble(params.get("capacity_drop")));
			else
				this.FD.setCapacityDrop(Double.NaN);
				
			if (params.has("congestion_speed")) 
				this.FD.setCongestionSpeed(new BigDecimal(params.get("congestion_speed")));
			if (params.has("jam_density")) 
				this.FD.setJamDensity(new BigDecimal(params.get("jam_density")));
			if (params.has("free_flow_speed")) 
				this.FD.setFreeFlowSpeed(new BigDecimal(params.get("free_flow_speed")));
		} else
			this.FD = null;
	}
	
	@Override
	protected void validate() {
		
		super.validate();
		
		if(FD==null)
			BeatsErrorLog.addError("Event id=" +getId() +" does not define a fundamental diagram.");
		
		// check each target is valid
		for(ScenarioElement s : getTargets())
			if(s.getMyType().compareTo(ScenarioElement.Type.link)!=0)
				BeatsErrorLog.addError("Wrong target type for event id=" +getId() +".");
		
		// check that new fundamental diagram does not invalidate current state
		
	}

	@Override
	protected void activate() throws BeatsException{
		for(ScenarioElement s : getTargets()){
			Link targetlink = (Link) s.getReference();
			if(resetToNominal)
				revertLinkFundamentalDiagram(targetlink);
			else
				setLinkFundamentalDiagram(targetlink,FD);
		}
		
	}
}
