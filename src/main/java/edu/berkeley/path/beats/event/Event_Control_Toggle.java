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
import com.relteq.sirius.simulator.Controller;
import com.relteq.sirius.simulator.Event;
import com.relteq.sirius.simulator.Scenario;
import com.relteq.sirius.simulator.ScenarioElement;

public class Event_Control_Toggle extends Event {

	protected boolean ison; 
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////
	
	public Event_Control_Toggle(){
	}
		
	public Event_Control_Toggle(Scenario myScenario,float timestampinseconds,List <Controller> controllers,boolean ison) {
		this.myScenario = myScenario;
		this.ison = ison;
		this.myType = Event.Type.control_toggle;
		this.timestampstep = (int) Math.round(timestampinseconds/myScenario.getSimDtInSeconds());
		this.targets = new ArrayList<ScenarioElement>();
			for(Controller controller : controllers )
				this.targets.add(ObjectFactory.createScenarioElement(controller));	
	}

	/////////////////////////////////////////////////////////////////////
	// InterfaceEvent
	/////////////////////////////////////////////////////////////////////

	@Override
	public void populate(Object jaxbobject) {
		com.relteq.sirius.jaxb.Event jaxbe = (com.relteq.sirius.jaxb.Event) jaxbobject;
		com.relteq.sirius.simulator.Parameters params = (com.relteq.sirius.simulator.Parameters) jaxbe.getParameters();
		// on_off_switch
		if (null != params && params.has("on_off_switch"))
			this.ison = params.get("on_off_switch").equalsIgnoreCase("on");
		else
			this.ison = true;
	}
	
	@Override
	public void validate() {
		
		super.validate();
		
		// check each target is valid
		if(targets!=null)
			for(ScenarioElement s : targets)
				if(s.getMyType().compareTo(ScenarioElement.Type.controller)!=0)
					SiriusErrorLog.addError("Wrong target type for event id=" +getId() +".");

	}

	@Override
	public void activate() throws SiriusException{
		for(ScenarioElement s : targets){
			Controller c = myScenario.getControllerWithId(s.getId());
			setControllerIsOn(c, ison);
		}			
	}
	
}