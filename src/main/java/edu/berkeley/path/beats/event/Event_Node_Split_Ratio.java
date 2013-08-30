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

import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Event;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;
import edu.berkeley.path.beats.util.Data1D;

public class Event_Node_Split_Ratio extends Event {

	protected boolean resetToNominal;			// if true, go back to nominal before applying changes
	protected Node myNode;
	protected java.util.List<SplitRatio> splitratios;
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////
	
	public Event_Node_Split_Ratio(){
	}
	
	public Event_Node_Split_Ratio(Scenario myScenario,edu.berkeley.path.beats.jaxb.Event jaxbE,Event.Type myType){
		super(myScenario, jaxbE, myType);
	}
	
//	// constructor for change event with single node target, single input, single vehicle type
//	public Event_Node_Split_Ratio(Scenario myScenario,Node node,String inlink,String vehicletype,ArrayList<Double>splits) {
//		if(node==null)
//			return;
//		if(myScenario==null)
//			return;
//		this.resetToNominal = false;
//		splitratios = new ArrayList<SplitRatio>(splits.size());
//		int input_index = node.getInputLinkIndex(inlink);
//		int vt_index = myScenario.getVehicleTypeIndex(vehicletype);
//		int output_index = 0;
//		for (Double split : splits)
//			splitratios.add(new SplitRatio(input_index, output_index++, vt_index, split));
//		this.targets = new ArrayList<ScenarioElement>();
//		this.targets.add(ObjectFactory.createScenarioElement(node));		
//	}

//	// constructor for reset event with single node target
//	public Event_Node_Split_Ratio(Scenario myScenario,Node node) {
//		this.resetToNominal = true;
//		this.splitratios = null;
//		this.myType = Event.Type.node_split_ratio;
//		this.targets = new ArrayList<ScenarioElement>();
//		this.targets.add(ObjectFactory.createScenarioElement(node));	
//	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / activate
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

//		edu.berkeley.path.beats.jaxb.Event jaxbe = (edu.berkeley.path.beats.jaxb.Event) jaxbobject;
//		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) jaxbe.getParameters();
//
//		// reset_to_nominal
//		boolean reset_to_nominal = false;
//		if (null != params && params.has("reset_to_nominal"))
//			reset_to_nominal = params.get("reset_to_nominal").equalsIgnoreCase("true");
//
//		if(!reset_to_nominal && jaxbe.getSplitratioEvent()==null)
//			return;
//
//		// only accepts single target
//		if(getTargets().size()!=1)
//			return;
//
//		this.resetToNominal = reset_to_nominal;
//		this.myNode = (Node) getTargets().get(0).getReference();
//		
//		if(myNode==null)
//			return;
//		
//		if(resetToNominal)		// nothing else to populate in this case
//			return;
//		
//		edu.berkeley.path.beats.jaxb.SplitratioEvent srevent = jaxbe.getSplitratioEvent();
//		if (srevent != null) {
//			int[] vt_index = null;
//			if (null == srevent.getVehicleTypeOrder()) {
//				vt_index = new int[getMyScenario().getNumVehicleTypes()];
//				for (int i = 0; i < vt_index.length; ++i)
//					vt_index[i] = i;
//			} else {
//				vt_index = new int[srevent.getVehicleTypeOrder().getVehicleTypeX().size()];
//				int i = 0;
//				for (edu.berkeley.path.beats.jaxb.VehicleTypeX vt : srevent.getVehicleTypeOrder().getVehicleTypeX())
//					vt_index[i++] = getMyScenario().getVehicleTypeIndexForName(vt.getName());
//			}
//			splitratios = new ArrayList<SplitRatio>(vt_index.length * srevent.getSplitratio().size());
//			for (edu.berkeley.path.beats.jaxb.Splitratio sr : srevent.getSplitratio()) {
//				Data1D data1d = new Data1D(sr.getContent(), ":");
//				if (!data1d.isEmpty()) {
//					java.math.BigDecimal[] data = data1d.getData();
//					int input_index = myNode.getInputLinkIndex(sr.getLinkIn());
//					int output_index = myNode.getOutputLinkIndex(sr.getLinkOut());
//					for (int i = 0; i < data.length; ++i)
//						splitratios.add(new SplitRatio(input_index, output_index, vt_index[i], data[i].doubleValue()));
//				}
//			}
//		}

	}
	
	@Override
	protected void validate() {
		
//		super.validate();
//		
//		if(getTargets().size()!=1)
//			BeatsErrorLog.addError("Multiple targets assigned to split ratio event id="+this.getId()+".");
//		
//		// check each target is valid
//		if(getTargets().get(0).getMyType().compareTo(ScenarioElement.Type.node)!=0)
//			BeatsErrorLog.addError("Wrong target type for event id="+getId()+".");
//		
//		if(myNode==null)
//			BeatsErrorLog.addWarning("Invalid node id for event id="+getId()+".");
//		
//		// check split ratio matrix
//		if(!resetToNominal){
//			for (SplitRatio sr : splitratios) {
//				if (sr.getInputIndex() < 0 || sr.getInputIndex() >= myNode.getnIn())
//					BeatsErrorLog.addWarning("Invalid input link index for event id="+getId()+".");
//				if (sr.getOutputIndex() < 0 || sr.getOutputIndex() >= myNode.getnOut())
//					BeatsErrorLog.addWarning("Invalid output link index for event id="+getId()+".");
//				if (sr.getVehicleTypeIndex() < 0 || sr.getVehicleTypeIndex() >= getMyScenario().getNumVehicleTypes())
//					BeatsErrorLog.addWarning("Invalid vehicle type index for event id="+getId()+".");
//			}
//		}
	}
	
	@Override
	protected void activate() throws BeatsException{
//		if(myNode==null)
//			return;
//		if(resetToNominal)
//			revertNodeEventSplitRatio(myNode);
//		else
//			setNodeEventSplitRatio(myNode, splitratios);
	}

}