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

final class ControllerSet extends edu.berkeley.path.beats.jaxb.ControllerSet {

	private enum OperationType {Deactivate,Activate}

	private Scenario myScenario;
	private ArrayList<Controller> controllers = new ArrayList<Controller>();
	private ArrayList<ActivationCommand> activations;
	private ArrayList<Integer> activeControllerIndex;
	private int activationindex;
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	protected ArrayList<Controller> get_Controllers(){
		return controllers;
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {

		this.myScenario = myScenario;
		this.activations = new ArrayList<ActivationCommand>();
		this.activeControllerIndex = new ArrayList<Integer>();
		int tempindex = 0;		
		if(myScenario.getControllerSet()!=null){
			for(edu.berkeley.path.beats.jaxb.Controller controller : myScenario.getControllerSet().getController()){
	
				// assign type
				Controller.Type myType;
		    	try {
					myType = Controller.Type.valueOf(controller.getType());
				} catch (IllegalArgumentException e) {
					continue;
				}
		    					
				// generate controller
				if(myType!=null){
					Controller C = ObjectFactory.createControllerFromJaxb(myScenario,controller,myType);
					if(C!=null){
						controllers.add(tempindex,C);									
						for (Controller.ActivationTimes acttimes : C.getActivationTimes()){
							if (acttimes!=null){								
								activations.add(new ActivationCommand(tempindex,acttimes.getBegintime(),OperationType.Activate));
								activations.add(new ActivationCommand(tempindex,acttimes.getEndtime(),OperationType.Deactivate));
							}
						}
						tempindex++;
					}
				}
			}			
		}
		
		Collections.sort(activations);
		activationindex=0;
	}

	protected void validate() {
		for(Controller controller : controllers)
			controller.validate();		
	}
	
	protected boolean register(){

		// For controllers that do not have activation times, validation does the initial registration,		
		// This is not added to the list of active controllers, because it is always active, thought we may need to change that.
		// We also validate each controller for its internal paramters.
		for(Controller controller : controllers){			
			if (controller.getActivationTimes().isEmpty())
				if (!controller.register()){
					BeatsErrorLog.addError("Controller registration failure, controller " + controller.getId());
					return false;
				}
		}

		// Check whether any two controllers are accessing the same link at any particular time.
		// An easy way to validate is to run through the sequence of activations and check registering/deregisterintg success.
		// We require all the controllers that are always active to be registered first!
		boolean validated = true;
		for (ActivationCommand activecmd : activations){
			if (activecmd!=null){
				if (activecmd.getOperation().equals(OperationType.Activate)){
					validated = controllers.get(activecmd.getIndex()).register();
					activeControllerIndex.add((Integer) activecmd.getIndex());				
				}
				else{
					validated = controllers.get(activecmd.getIndex()).deregister();
					controllers.get(activecmd.getIndex()).setIson(false);
					activeControllerIndex.remove(activeControllerIndex.indexOf((Integer) activecmd.getIndex()));					
				}
				if (!validated){
					BeatsErrorLog.addError("Multiple controllers accessing the same link at the same time. Controller registration failure, controller " + controllers.get(activecmd.getIndex()).getId());
					return false;
				}
			}	
		}
		
		// However, you need to deregister the last set of registered controllers
		for(Integer controllerindex : activeControllerIndex)
			if(controllerindex!=null)
				controllers.get(controllerindex).deregister();
		
		activeControllerIndex.clear();
		
		return true;
	}

	protected void reset() {
		//reset controllers
		for(Controller controller : controllers)
			controller.reset();
		
		// Deregister previous active controllers
		for(Integer controllerindex : activeControllerIndex)
			if(controllerindex!=null)
				controllers.get(controllerindex).deregister();
		
		// Set activation index to zero, and process all events upto the starttime.
		activationindex = 0;
		processActivations(myScenario.getClock().getStartTime());  	
		
	}
	
	// Process all events upto time t, starting from the activationindex
	protected void processActivations(double t){
		
		while (activationindex<activations.size() && activations.get(activationindex).getTime()<=t){
			ActivationCommand activecmd=activations.get(activationindex);
			
			if (activecmd!=null){
				if (activecmd.getOperation().equals(OperationType.Activate)){
					controllers.get(activecmd.getIndex()).register();
					controllers.get(activecmd.getIndex()).setIson(true);
					controllers.get(activecmd.getIndex()).reset();
					activeControllerIndex.add((Integer) activecmd.getIndex()); 
				}
				else{
					controllers.get(activecmd.getIndex()).deregister();
					controllers.get(activecmd.getIndex()).setIson(false);					
					activeControllerIndex.remove(activeControllerIndex.indexOf((Integer) activecmd.getIndex())); 
				}
			}
			activationindex++;
		}
	
	}
	
	protected void update() throws BeatsException {
		processActivations(myScenario.getClock().getT());			
		
    	for(Controller controller : controllers){
    		if(controller.isIson() && myScenario.getClock().istimetosample(controller.getSamplesteps(),0))
    			controller.update();
    	}
	}
	
	/////////////////////////////////////////////////////////////////////
	// Setup a class to keep track of Activation/Deactivation times
	/////////////////////////////////////////////////////////////////////
	
	protected class ActivationCommand implements Comparable<ActivationCommand> {
		protected int index;
		protected double time;		
		protected OperationType operation;
		
		public int getIndex() {
			return index;
		}
		
		public double getTime() {
			return time;
		}
		
		public OperationType getOperation() {
			return operation;
		}
		
		public ActivationCommand(int index, double time, OperationType operation) {
			super();
			this.index = index;
			this.time = time;
			this.operation = operation;
		}
		
		public int compareTo(ActivationCommand o) {
			//first compare by times
			int compare = ((Double) time).compareTo((Double)o.getTime());
			//then compare by operation type - deactivation takes precedence (as defined in the order before.
			if (compare==0){
				compare = operation.compareTo(o.getOperation());				
			}
			return compare;
		}	
		
	}
}
