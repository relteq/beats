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

package edu.berkeley.path.beats.control;

import java.util.Vector;

import edu.berkeley.path.beats.simulator.InterfaceComponent;
import edu.berkeley.path.beats.simulator.SiriusErrorLog;
import edu.berkeley.path.beats.simulator.SiriusMath;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;

public class Controller_SIG_Pretimed extends Controller {

	// input parameters
	private int [] plansequence;		  // Ordered list of plans to implement 
	private float [] planstarttime;		  // [sec] Implementation times (first should be 0, should be increasing)
	private float transdelay;					   // transition time between plans.
	private int numplans;						   // total number of defined plans
	private Controller_SIG_Pretimed_Plan [] plan;  // array of plans
	
	// state
	//private int cplan;							  // current plan id
	private int cperiod;						  // current index to planstarttime and plansequence
	
	// coordination
	//private ControllerCoordinated coordcont;
	private boolean coordmode = false;					  // true if this is used for coordination (softforceoff only)

	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_SIG_Pretimed() {
		// TODO Auto-generated constructor stub
	}
	
	public Controller_SIG_Pretimed(Scenario myScenario) {
		// TODO Auto-generated constructor stub
	}

	/////////////////////////////////////////////////////////////////////
	// InterfaceController
	/////////////////////////////////////////////////////////////////////

	/** Implementation of {@link InterfaceComponent#populate}.
	 * @param jaxbobject Object
	 */
	@Override
	public void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;

		// must have these
		if(jaxbc.getTargetElements()==null)
			return;
		if(jaxbc.getTargetElements().getScenarioElement()==null)
			return;
		if(jaxbc.getPlanList()==null)
			return;
		if(jaxbc.getPlanList().getPlan()==null)
			return;
		if(jaxbc.getPlanList().getPlan().isEmpty())
			return;

		// plan list
		numplans = jaxbc.getPlanList().getPlan().size();
		Vector<String> planId2Index = new Vector<String>();
		plan = new Controller_SIG_Pretimed_Plan[numplans];
		for(int i=0;i<numplans;i++){
			plan[i] = new Controller_SIG_Pretimed_Plan();
			plan[i].populate(this,myScenario,jaxbc.getPlanList().getPlan().get(i));
			planId2Index.add(plan[i].getId());
		}
		
		// plan sequence
		if(jaxbc.getPlanSequence()==null){	// if no plan sequence, assume 0,0
			transdelay = 0;
			plansequence = new int[1];
			plansequence[0] = 0;
			planstarttime = new float[1];
			planstarttime[0] = 0f;
		}
		else{
			if(jaxbc.getPlanSequence().getTransitionDelay()!=null)
				transdelay = jaxbc.getPlanSequence().getTransitionDelay().floatValue();
			else
				transdelay = 0f;
			
			if(jaxbc.getPlanSequence().getPlanReference()!=null){
				
				int numPlanReference = jaxbc.getPlanSequence().getPlanReference().size();

				plansequence = new int[numPlanReference];
				planstarttime = new float[numPlanReference];
				
				for(int i=0;i<numPlanReference;i++){
					edu.berkeley.path.beats.jaxb.PlanReference ref = jaxbc.getPlanSequence().getPlanReference().get(i);
					plansequence[i] = planId2Index.indexOf(ref.getPlanId());
					planstarttime[i] = ref.getStartTime().floatValue();
				}
			}
			
		}

	}

	@Override
	public void update() {

		double simtime = myScenario.getTimeInSeconds();

		// time to switch plans .....................................
		if( cperiod < planstarttime.length-1 ){
			if( SiriusMath.greaterorequalthan( simtime , planstarttime[cperiod+1] + transdelay ) ){
				cperiod++;
				if(plansequence[cperiod]==0){
					// GCG asc.ResetSignals();  GG FIX THIS
				}
//				if(coordmode)
//					coordcont.SetSyncPoints();
					
			}
		}

//		if( plansequence[cperiod]==0 )
//			ImplementASC();
//		else
			plan[plansequence[cperiod]].implementPlan(simtime,coordmode);		
		
	}
	
	@Override
	public void validate() {
		
		super.validate();
		
		int i;
		
		// transdelay>=0
		if(transdelay<0)
			SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		// first planstarttime=0
		if(planstarttime[0]!=0)
			SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		// planstarttime is increasing
		for(i=1;i<planstarttime.length;i++)
			if(planstarttime[i]<=planstarttime[i-1])
				SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		// all plansequence ids found
		for(i=0;i<plansequence.length;i++)
			if(plansequence[i]<0)
				SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");

		// all targets are signals
		for(ScenarioElement se: targets)
			if(se.getMyType().compareTo(ScenarioElement.Type.signal)!=0)
				SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		for(i=0;i<plan.length;i++)
			plan[i].validate();
		
	}

	@Override
	public void reset() {
		super.reset();
		cperiod = 0;

		for(int i=0;i<plan.length;i++)
			plan[i].reset();
	}
	
}
