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
import java.util.Collections;

final class EventSet extends edu.berkeley.path.beats.jaxb.EventSet {

	protected Scenario myScenario;
	protected boolean isdone;			// true if we are done with events
	protected int currentevent;
	protected ArrayList<Event> sortedevents = new ArrayList<Event>();
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	protected void addEvent(Event E){
				
		// add event to list
		sortedevents.add(E);
		
		// re-sort
		Collections.sort(sortedevents);

	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	protected void populate(Scenario myScenario) {
		
		this.myScenario = myScenario;

		if(myScenario.getEventSet()!=null){
			for(edu.berkeley.path.beats.jaxb.Event event : myScenario.getEventSet().getEvent() ){
				
				// keep only enabled events
				if(event.isEnabled()){
	
					// assign type
					Event.Type myType;
			    	try {
						myType = Event.Type.valueOf(event.getType());
					} catch (IllegalArgumentException e) {
						continue;
					}	
					
					// generate event
					if(myType!=null){
						Event E = ObjectFactory.createEventFromJaxb(myScenario,event,myType);
						if(E!=null)
							sortedevents.add(E);
					}
				}
			}
		}
		
		// sort the events by timestamp, etc
		Collections.sort(sortedevents);
	}

	protected void validate() {
		Event previousevent = null;
		for(Event event : sortedevents){			
			// disallow pairs of events with equal time stamp, target, and type.
			if(previousevent!=null)
				if(event.equals(previousevent))
					SiriusErrorLog.addError("Events id=" + previousevent.getId() + " and id=" + event.getId() + " are identical.");
			previousevent = event;
		}
	}

	protected void reset() {
		currentevent = 0;
		isdone = sortedevents.isEmpty();
	}

	protected void update() throws SiriusException {

		if(isdone)
			return;
		
		if(sortedevents.isEmpty()){
			isdone=true;
			return;
		}

		// check whether next event is due
		while(myScenario.clock.getCurrentstep()>=sortedevents.get(currentevent).timestampstep){
			
			Event event =  sortedevents.get(currentevent);
			
			SiriusErrorLog.clearErrorMessage();
			event.validate();
			if(!SiriusErrorLog.haserror())
				event.activate(); 
			else
				throw new SiriusException("Event could not be validated.");
			
			currentevent++;
			
			// don't come back if done
			if(currentevent==sortedevents.size()){
				isdone=true;
				break;
			}
		}
	}

}
