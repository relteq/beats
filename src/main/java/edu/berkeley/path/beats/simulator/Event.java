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

import java.math.BigDecimal;
import java.util.ArrayList;

import edu.berkeley.path.beats.jaxb.DisplayPosition;
import edu.berkeley.path.beats.jaxb.Parameters;
import edu.berkeley.path.beats.jaxb.SplitratioEvent;
import edu.berkeley.path.beats.jaxb.TargetElements;

/** Base class for events. 
 * Provides a default implementation of <code>InterfaceEvent</code>.
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
@SuppressWarnings("rawtypes")
public class Event extends edu.berkeley.path.beats.jaxb.Event implements Comparable {

	/** Scenario that contains this event */
	protected Scenario myScenario;
	
	/** Event type. */
	protected Event.Type myType;
	
	/** Activation time of the event, in number of simulation time steps. */
	protected int timestampstep;
	
	/** List of targets for the event. */
	protected ArrayList<ScenarioElement> targets;
	
	/** Type of event. */
	public static enum Type	{  
		/** see {@link ObjectFactory#createEvent_Fundamental_Diagram} 	*/ fundamental_diagram,
		/** see {@link ObjectFactory#createEvent_Link_Demand_Knob} 		*/ link_demand_knob,
		/** see {@link ObjectFactory#createEvent_Link_Lanes} 			*/ link_lanes, 
		/** see {@link ObjectFactory#createEvent_Node_Split_Ratio} 		*/ node_split_ratio,
		/** see {@link ObjectFactory#createEvent_Control_Toggle} 		*/ control_toggle,
		/** see {@link ObjectFactory#createEvent_Global_Control_Toggle} */ global_control_toggle,
		/** see {@link ObjectFactory#createEvent_Global_Demand_Knob} 	*/ global_demand_knob };
		   
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Event(){}

	/////////////////////////////////////////////////////////////////////
	// hide base class setters
	/////////////////////////////////////////////////////////////////////

	@Override
	public void setDescription(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setDisplayPosition(DisplayPosition value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setTargetElements(TargetElements value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setParameters(Parameters value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSplitratioEvent(SplitratioEvent value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setId(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setTstamp(BigDecimal value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setEnabled(boolean value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setType(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setJavaClass(String value) {
		System.out.println("This setter is hidden.");
	}

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	/** @y.exclude */
	protected void populateFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Event jaxbE,Event.Type myType){
		this.id = jaxbE.getId();
		this.myScenario = myScenario;
		this.myType = myType;
		this.timestampstep = BeatsMath.round(jaxbE.getTstamp().floatValue()/myScenario.getSimDtInSeconds());		// assume in seconds
		this.targets = new ArrayList<ScenarioElement>();
		if(jaxbE.getTargetElements()!=null)
			for(edu.berkeley.path.beats.jaxb.ScenarioElement s : jaxbE.getTargetElements().getScenarioElement() )
				this.targets.add(ObjectFactory.createScenarioElementFromJaxb(myScenario,s));
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / activate
	/////////////////////////////////////////////////////////////////////

	protected void populate(Object jaxbobject) {
	}

	protected void validate() {
		
		if(myType==null)
			BeatsErrorLog.addError("Event with id=" + getId() + " has bad type.");
			
		// check that there are targets assigned to non-global events
		if(myType!=null)
			if(myType.compareTo(Event.Type.global_control_toggle)!=0 && myType.compareTo(Event.Type.global_demand_knob)!=0)
				if(targets.isEmpty())
					BeatsErrorLog.addError("No targets assigned in event with id=" + getId() + ".");
		
		// check each target is valid
		for(ScenarioElement s : targets)
			if(s.reference==null)
				BeatsErrorLog.addError("Invalid target id=" + s.getId() + " in event id=" + getId() + ".");

	}
	
	protected void activate() throws BeatsException {		
	}

	/////////////////////////////////////////////////////////////////////
	// Comparable
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public int compareTo(Object arg0) {
		
		if(arg0==null)
			return 1;
		
		int compare;
		Event that = ((Event) arg0);
		
		// first ordering by time stamp
		Integer thiststamp = this.timestampstep;
		Integer thattstamp = that.timestampstep;
		compare = thiststamp.compareTo(thattstamp);
		if(compare!=0)
			return compare;

		// second ordering by event type
		Event.Type thiseventtype = this.myType;
		Event.Type thateventtype = that.myType;
		compare = thiseventtype.compareTo(thateventtype);
		if(compare!=0)
			return compare;
		
		// third ordering by number of targets
		Integer thisnumtargets = this.targets.size();
		Integer thatnumtargets = that.targets.size();
		compare = thisnumtargets.compareTo(thatnumtargets);
		if(compare!=0)
			return compare;
		
		// fourth ordering by target type
		for(int i=0;i<thisnumtargets;i++){
			ScenarioElement.Type thistargettype = this.targets.get(i).myType;
			ScenarioElement.Type thattargettype = that.targets.get(i).myType;
			compare = thistargettype.compareTo(thattargettype);
			if(compare!=0)
				return compare;		
		}

		// fifth ordering by target id
		for(int i=0;i<thisnumtargets;i++){
			String thistargetId = this.targets.get(i).getId();
			String thattargetId = that.targets.get(i).getId();
			compare = thistargetId.compareTo(thattargetId);
			if(compare!=0)
				return compare;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null)
			return false;
		else
			return this.compareTo((Event) obj)==0;
	}

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////	

	protected void setGlobalControlIsOn(boolean ison){
		myScenario.global_control_on = ison;
	}
	
	protected void setControllerIsOn(Controller c,boolean ison){
		if(c==null)
			return;
		c.ison = ison;
	}

    protected void setLinkLanes(Link link,double lanes) throws BeatsException{
		if(link==null)
			return;
    	link.set_Lanes(lanes);
    }
    	
	protected void setLinkFundamentalDiagram(Link link,edu.berkeley.path.beats.jaxb.FundamentalDiagram newFD) throws BeatsException{
		if(link==null)
			return;
		link.activateFundamentalDiagramEvent(newFD);
	}
	
    protected void revertLinkFundamentalDiagram(Link link) throws BeatsException{
    	if(link==null)
    		return;
    	link.revertFundamentalDiagramEvent();
    }    

	protected void setNodeEventSplitRatio(Node node, java.util.List<SplitRatio> splitratios) {
		if(node==null)
			return;
		Double3DMatrix X = new Double3DMatrix(node.getnIn(),node.getnOut(),myScenario.getNumVehicleTypes(),Double.NaN);
		X.copydata(node.splitratio);
		for (SplitRatio sr : splitratios)
			X.set(sr.getInputIndex(), sr.getOutputIndex(), sr.getVehicleTypeIndex(), sr.getValue());
		if(!node.validateSplitRatioMatrix(X))
			return;
		node.setSplitratio(X);
		node.hasactivesplitevent = true;
	}

	protected void revertNodeEventSplitRatio(Node node) {
		if(node==null)
			return;
		if(node.hasactivesplitevent){
			node.resetSplitRatio();
			node.hasactivesplitevent = false;
		}
	}
	
    protected void setDemandProfileEventKnob(edu.berkeley.path.beats.jaxb.DemandProfile profile,Double knob){
		if(profile==null)
			return;
		if(knob.isNaN())
			return;
		((DemandProfile) profile).set_knob(knob);
    }
    
    protected void setGlobalDemandEventKnob(Double knob){
    	myScenario.global_demand_knob = knob;
    }
	    
	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////	

	/** @y.exclude */
	protected static class SplitRatio {
		private int input_index;
		private int output_index;
		private int vehicle_type_index;
		private Double value;

		public SplitRatio(int input_index, int output_index, int vehicle_type_index, Double value) {
			this.input_index = input_index;
			this.output_index = output_index;
			this.vehicle_type_index = vehicle_type_index;
			this.value = value;
		}

		public int getInputIndex() {
			return input_index;
		}
		public int getOutputIndex() {
			return output_index;
		}
		public int getVehicleTypeIndex() {
			return vehicle_type_index;
		}
		public Double getValue() {
			return value;
		}
	}

}
