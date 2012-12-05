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

/** Base class for controllers. 
 * Provides a default implementation of <code>InterfaceController</code>.
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class Controller implements InterfaceComponent,InterfaceController {
	
	/** Scenario that contains this controller */
	protected Scenario myScenario;										       								       
	
	/** Id of the controller. */
	protected String id;
										
	/** Controller type. */
	protected Controller.Type myType;
	
	/** List of scenario elements affected by this controller */
	protected ArrayList<ScenarioElement> targets;
	
	/** List of scenario elements that provide input to this controller */
	protected ArrayList<ScenarioElement> feedbacks;
	
	/** Maximum flow for link targets, in vehicles per simulation time period. Indexed by target.  */
	protected Double [] control_maxflow;
	
	/** Maximum flow for link targets, in normalized units. Indexed by target.  */
	protected Double [] control_maxspeed;
	
	/** Controller update period in seconds */
	protected double dtinseconds;
	
	/** Controller update period in number of simulation steps */
	protected int samplesteps;
	
	/** On/off switch for this controller */
	protected boolean ison;
	
	/** Activation times for this controller */
	protected ArrayList<ActivationTimes> activationTimes;
	
	/** Table of parameters. */
	protected Table table;
	
	/** Controller algorithm. The three-letter prefix indicates the broad class of the 
	 * controller.  
	 * <ul>
	 * <li> IRM, isolated ramp metering </li>
	 * <li> CRM, coordinated ramp metering </li>
	 * <li> VSL, variable speed limits </li>
	 * <li> SIG, signal control (intersections) </li>
	 * </ul>
	 */
	protected static enum Type {  
	  /** see {@link ObjectFactory#createController_IRM_Alinea} 			*/ 	IRM_ALINEA,
	  /** see {@link ObjectFactory#createController_IRM_Time_of_Day} 		*/ 	IRM_TOD,
	  /** see {@link ObjectFactory#createController_IRM_Traffic_Responsive}	*/ 	IRM_TOS,
	  /** see {@link ObjectFactory#createController_CRM_SWARM}				*/ 	CRM_SWARM,
      /** see {@link ObjectFactory#createController_CRM_HERO}				*/ 	CRM_HERO,
      /** see {@link ObjectFactory#createController_VSL_Time_of_Day}		*/ 	VSL_TOD,
      /** see {@link ObjectFactory#createController_SIG_Pretimed}			*/ 	SIG_TOD,
      /** see {@link ObjectFactory#createController_SIG_Actuated}			*/ 	SIG_Actuated };
	
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	 protected Controller(){}

	 /** @y.exclude */
	 protected Controller(ArrayList<ScenarioElement> targets){
		 this.targets = targets;
		 this.control_maxflow  = new Double [targets.size()];
		 this.control_maxspeed = new Double [targets.size()];
	 }
	
	/////////////////////////////////////////////////////////////////////
	// registration
	/////////////////////////////////////////////////////////////////////

   	/** Use this method within {@link InterfaceController#register} to register
   	 * flow control with a target link. The return value is <code>true</code> if
   	 * the registration is successful, and <code>false</code> otherwise. 
   	 * @param link The target link for flow control.
   	 * @param index The index of the link in the controller's list of targets.
   	 * @return A boolean indicating success of the registration. 
   	 */
	protected boolean registerFlowController(Link link,int index){
		if(link==null)
			return true;
		else
			return link.registerFlowController(this,index);
	}

   	/** Use this method within {@link InterfaceController#deregister} to deregister
   	 * speed control with a target link. The return value is <code>true</code> if
   	 * the deregistration is successful, and <code>false</code> otherwise. 
   	 * @param link The target link for speed control.
   	 * @return A boolean indicating success of the deregistration. 
   	 */
	protected boolean deregisterSpeedController(Link link){
		if(link!=null)			
			return link.deregisterSpeedController(this);
		else
			return false;
	}
	
	/** Use this method within {@link InterfaceController#deregister} to deregister
   	 * flow control with a target link. The return value is <code>true</code> if
   	 * the deregistration is successful, and <code>false</code> otherwise. 
   	 * @param link The target link for flow control.
   	 * @return A boolean indicating success of the deregistration. 
   	 */
	protected boolean deregisterFlowController(Link link){
		if(link==null)
			return false;
		else
			return link.deregisterFlowController(this);
	}

   	/** Use this method within {@link InterfaceController#register} to register
   	 * speed control with a target link. The return value is <code>true</code> if
   	 * the registration is successful, and <code>false</code> otherwise. 
   	 * @param link The target link for speed control.
   	 * @param index The index of the link in the controller's list of targets.
   	 * @return A boolean indicating success of the registration. 
   	 */
	protected boolean registerSpeedController(Link link,int index){
		if(link==null)
			return true;
		else
			return link.registerSpeedController(this,index);
	}
	
//   	/** DESCRIPTION
//   	 * 
//   	 */
//	protected boolean registerSplitRatioController(_Node node,int index){
//		if(node==null)
//			return true;
//		else
//			return node.registerSplitRatioController(this,index);
//	}
	
	// Returns the start and end times of the controller.
	
   	/** Returns the first start time of the controller. This is the minimum of the start
   	 * times of all activation periods of the controller. 
   	 * @return A double with the start time for the controller. 
   	 */
	protected double getFirstStartTime(){
		double starttime=myScenario.getTimeStart();
		for (int ActTimesIndex = 0; ActTimesIndex < activationTimes.size(); ActTimesIndex++ )
			if (ActTimesIndex == 0)
				starttime=activationTimes.get(ActTimesIndex).getBegintime();
			else
				starttime=Math.min(starttime,activationTimes.get(ActTimesIndex).getBegintime());
		return starttime;
	}
	
   	/** Returns the last end time of the controller. This is the maximum of the end
   	 * times of all activation periods of the controller. 
   	 * @return A double with the end time for the controller. 
   	 */
	protected double getlastEndTime(){
		double endtime=myScenario.getTimeEnd();
		for (int ActTimesIndex = 0; ActTimesIndex < activationTimes.size(); ActTimesIndex++ )
			if (ActTimesIndex == 0)
				endtime=activationTimes.get(ActTimesIndex).getEndtime();
			else
				endtime=Math.max(endtime,activationTimes.get(ActTimesIndex).getEndtime());
		return endtime;
	}
	
	/////////////////////////////////////////////////////////////////////
	// InterfaceComponent
	/////////////////////////////////////////////////////////////////////
		
	/** @y.exclude */
	protected final void populateFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c,Controller.Type myType){
		this.myScenario = myScenario;
		this.myType = myType;
		this.id = c.getId();
		this.ison = false; //c.isEnabled(); 
		this.activationTimes=new ArrayList<ActivationTimes>();
		dtinseconds = c.getDt().floatValue();		// assume given in seconds
		samplesteps = SiriusMath.round(dtinseconds/myScenario.getSimDtInSeconds());		
		
		// Copy table
		if (c.getTable()!=null)
			this.table = new Table(c.getTable());
		
		// Get activation times and sort	
		if (c.getActivationIntervals()!=null)
			for (edu.berkeley.path.beats.jaxb.Interval tinterval : c.getActivationIntervals().getInterval())		
				if(tinterval!=null)
					activationTimes.add(new ActivationTimes(tinterval.getStartTime().doubleValue(),tinterval.getEndTime().doubleValue()));
		Collections.sort(activationTimes);
		
		// store targets ......
		targets = new ArrayList<ScenarioElement>();
		if(c.getTargetElements()!=null)
			for(edu.berkeley.path.beats.jaxb.ScenarioElement s : c.getTargetElements().getScenarioElement() ){
				ScenarioElement se = ObjectFactory.createScenarioElementFromJaxb(myScenario,s);
				if(se!=null)
					targets.add(se);
			}
		
		control_maxflow  = new Double [targets.size()];
		control_maxspeed = new Double [targets.size()];

		// store feedbacks ......
		feedbacks = new ArrayList<ScenarioElement>();
		if(c.getFeedbackElements()!=null)
			for(edu.berkeley.path.beats.jaxb.ScenarioElement s : c.getFeedbackElements().getScenarioElement()){
				ScenarioElement se = ObjectFactory.createScenarioElementFromJaxb(myScenario,s);
				if(se!=null)
					feedbacks.add(se);	
			}
	}

	/** @y.exclude */
	@Override
	public void populate(Object jaxbobject) {
	}

	/** @y.exclude */
	@Override
	public void update() throws SiriusException {
	}

	/** @y.exclude */
	@Override
	/** @y.exclude */
	public void validate() {
		
		// check that type was read correctly
		if(myType==null)
			SiriusErrorLog.addError("Controller with id=" + getId() + " has the wrong type.");
		
		// check that the target is valid
		if(targets==null)
			SiriusErrorLog.addError("Invalid target for controller id=" + getId());
		
		// check that sample dt is an integer multiple of network dt
		if(!SiriusMath.isintegermultipleof(dtinseconds,myScenario.getSimDtInSeconds()))
			SiriusErrorLog.addError("Time step for controller id=" +getId() + " is not a multiple of the simulation time step.");

		// check that activation times are valid.
		for (int i=0; i<activationTimes.size(); i++ ){
			activationTimes.get(i).validate();
			if (i<activationTimes.size()-1)
				activationTimes.get(i).validateWith(activationTimes.get(i+1));
		}

	}

	/** @y.exclude */
	@Override
	public void reset() {
		//switch on conroller if it is always on by default.
		if (activationTimes==null)
			ison = true;
	}

	/** @y.exclude */
	@Override
	public boolean register() {
		return false;
	}

	/** @y.exclude */
	@Override
	public boolean deregister() {
		return false;
	}

	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

   	/** Get the ID of the controller  */
	public String getId() {
		return id;
	}	

   	/** Get the type of the controller.  */
	public Controller.Type getMyType() {
		return myType;
	}

   	/** Get list of controller targets  */
	public ArrayList<ScenarioElement> getTargets() {
		return targets;
	}

   	/** Get list of controller feedback elements  */
	public ArrayList<ScenarioElement> getFeedbacks() {
		return feedbacks;
	}

   	/** Get the controller update period in [seconds]  */
	public double getDtinseconds() {
		return dtinseconds;
	}

   	/** Get the on/off value of the controller 
   	 * @return <code>true</code> if the controller is currently on, <code>off</code> otherwise. 
   	 */
	public boolean isIson() {
		return ison;
	}

	/////////////////////////////////////////////////////////////////////
	// internal classes
	/////////////////////////////////////////////////////////////////////
	
	/** Creates a new class that stores begin and end times for each period of controller activation */
	protected class ActivationTimes implements Comparable<ActivationTimes>{
		
		/** Start time for each activation interval */
		protected double begintime; 
		
		/** End time for each activation interval */
		protected double endtime;
		
		protected ActivationTimes(double begintime, double endtime) {
			super();
			this.begintime = begintime;
			this.endtime = endtime;
		}
		
		public double getBegintime() {
			return begintime;
		}
		
		protected void setBegintime(double begintime) {
			this.begintime = begintime;
		}
		
		public double getEndtime() {
			return endtime;
		}
		
		protected void setEndtime(double endtime) {
			this.endtime = endtime;
		}		
		
		protected void validate(){			
			if (begintime-endtime>=0)
				SiriusErrorLog.addError("Begin time must be larger than end time.");		  
		}
		
		protected void validateWith(ActivationTimes that){			
			if (Math.max(this.begintime-that.getEndtime(), that.getBegintime()-this.endtime)<0)  // Assumption - activation times is sorted before this is invoked, should remove this assumption later.
				SiriusErrorLog.addError("Activation Periods of the controllers must not overlap.");
		}
		
		/////////////////////////////////////////////////////////////////////
		// Comparable
		/////////////////////////////////////////////////////////////////////		

		@Override
		public int compareTo(ActivationTimes that) {
			if(that==null)
				return 1;
			
			// Order first by begintimes.
			int compare = ((Double) this.getBegintime()).compareTo((Double) that.getBegintime());
		
			if (compare!=0)
				return compare;
				
		    // Order next by endtimes.
			compare = ((Double) this.getEndtime()).compareTo((Double) that.getEndtime());
				
			return compare;
				
				
		}
		
	}

}
