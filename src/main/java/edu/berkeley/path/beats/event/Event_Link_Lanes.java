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

import java.util.ArrayList;
import java.util.List;

import com.relteq.sirius.simulator.ObjectFactory;
import com.relteq.sirius.simulator.SiriusErrorLog;
import com.relteq.sirius.simulator.SiriusException;
import com.relteq.sirius.simulator.Event;
import com.relteq.sirius.simulator.Link;
import com.relteq.sirius.simulator.Scenario;
import com.relteq.sirius.simulator.ScenarioElement;

public class Event_Link_Lanes extends Event {

	protected boolean resetToNominal;
	protected double deltalanes;
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Event_Link_Lanes(){
	}
			
	public Event_Link_Lanes(Scenario myScenario,List<Link> links,boolean isrevert,double deltalanes) {
		this.targets = new ArrayList<ScenarioElement>();
		this.resetToNominal = isrevert;
		for(Link link : links)
			this.targets.add(ObjectFactory.createScenarioElement(link));
		this.deltalanes = deltalanes;
	}

	/////////////////////////////////////////////////////////////////////
	// InterfaceEvent
	/////////////////////////////////////////////////////////////////////

	@Override
	public void populate(Object jaxbobject) {
		com.relteq.sirius.jaxb.Event jaxbe = (com.relteq.sirius.jaxb.Event) jaxbobject;
		com.relteq.sirius.simulator.Parameters params = (com.relteq.sirius.simulator.Parameters) jaxbe.getParameters();
		// reset_to_nominal
		if (null != params && params.has("reset_to_nominal"))
			this.resetToNominal = params.get("reset_to_nominal").equalsIgnoreCase("true");
		else
			this.resetToNominal = false;
		// lane_count_change
		if (null != params && params.has("lane_count_change"))
			this.deltalanes = Double.valueOf(params.get("lane_count_change"));
		else
			this.deltalanes = 0.0;
	}

	@Override
	public void validate() {
		
		super.validate();
		
		// check each target is valid
		for(ScenarioElement s : targets){
			if(s.getMyType().compareTo(ScenarioElement.Type.link)!=0)
				SiriusErrorLog.addError("wrong target type for event id=" +getId() +".");
		}
	}

	@Override
	public void activate() throws SiriusException{
		double newlanes;
		for(ScenarioElement s : targets){
			Link targetlink = (Link) s.getReference();
			if(resetToNominal)
				newlanes = ((com.relteq.sirius.jaxb.Link)targetlink).getLanes().doubleValue();
			else
				newlanes =  targetlink.get_Lanes();
			newlanes += deltalanes;
			setLinkLanes(targetlink,newlanes);
		}		
	}


}
