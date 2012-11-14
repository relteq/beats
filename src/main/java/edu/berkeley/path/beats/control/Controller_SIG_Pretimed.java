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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.simulator.SiriusErrorLog;
import edu.berkeley.path.beats.simulator.SiriusMath;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.ScenarioElement;
import edu.berkeley.path.beats.simulator.Table;

public class Controller_SIG_Pretimed extends Controller {

	// input parameters
	private String [] plansequence;		  // Ordered list of plans to implement
	private float [] planstarttime;		  // [sec] Implementation times (first should be 0, should be increasing)
	private float transdelay;					   // transition time between plans.
	private java.util.Map<String, Controller_SIG_Pretimed_Plan> plan;  // array of plans
	
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
	
	@Override
	public void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;

		// must have these
		if(jaxbc.getTargetElements()==null)
			return;
		if(jaxbc.getTargetElements().getScenarioElement()==null)
			return;

		// plan list table
		Table tbl_pl = tables.get("Plan List");
		if (null == tbl_pl) {
			SiriusErrorLog.addError("Controller " + jaxbc.getId() + ": no 'Plan List' table");
			return;
		}

		// plan sequence table
		Table tbl_ps = tables.get("Plan Sequence");
		if (null == tbl_ps) {
			SiriusErrorLog.addError("Controller " + jaxbc.getId() + ": no 'Plan Sequence' table");
			return;
		}

		// restoring plan sequence
		PlanSequence plan_seq = new PlanSequence(tbl_ps);

		// restoring plan list
		PlanList planlist = new PlanList(tbl_pl);
		// processing plan list
		plan = new java.util.HashMap<String, Controller_SIG_Pretimed_Plan>();
		for (Plan plan_raw : planlist.getPlanList()) {
			// cycle length - from the sequence
			for (PlanRun plan_run : plan_seq.getPlanReference())
				if (plan_run.getPlanId().equals(plan_raw.getId())) {
					if (null == plan_raw.getCycleLength())
						plan_raw.setCycleLength(Double.valueOf(plan_run.getCycleLength()));
					else if (!plan_raw.getCycleLength().equals(plan_run.getCycleLength()))
						logger.warn("Found a different cycle length: controller=" + jaxbc.getId() + ", plan=" + plan_raw.getId());
				}
			if (null == plan_raw.getCycleLength())
				logger.warn("Plan " + plan_raw.getId() + " not found in the plan sequence (controller=" + jaxbc.getId() + ")");

			// populate
			Controller_SIG_Pretimed_Plan pretimed_plan = new Controller_SIG_Pretimed_Plan();
			pretimed_plan.populate(this, myScenario, plan_raw);
			plan.put(plan_raw.getId(), pretimed_plan);
		}

		// processing plan sequence
		final int seq_size = plan_seq.getPlanReference().size();
		plansequence = new String[seq_size];
		planstarttime = new float[seq_size];
		int i = 0;
		for (PlanRun plan_run : plan_seq.getPlanReference()) {
			plansequence[i] = plan_run.getPlanId();
			planstarttime[i] = plan_run.getStartTime().floatValue();
			++i;
		}

		// transition delay
		transdelay = 0f;
		if (null != jaxbc.getParameters()) {
			edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) jaxbc.getParameters();
			String param_name = "Transition Delay";
			if (params.has(param_name))
				transdelay = Float.parseFloat(params.get(param_name));
		}

	}

	private static Logger logger = Logger.getLogger(Controller_SIG_Pretimed_Plan.class);

	@Override
	public void update() {

		double simtime = myScenario.getTimeInSeconds();

		// time to switch plans .....................................
		if( cperiod < planstarttime.length-1 ){
			if( SiriusMath.greaterorequalthan( simtime , planstarttime[cperiod+1] + transdelay ) ){
				cperiod++;
				if(null == plansequence[cperiod]){
					// GCG asc.ResetSignals();  GG FIX THIS
				}
//				if(coordmode)
//					coordcont.SetSyncPoints();
					
			}
		}

//		if( plansequence[cperiod]==0 )
//			ImplementASC();
//		else
			plan.get(plansequence[cperiod]).implementPlan(simtime,coordmode);
		
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
			if (null == plansequence[i])
				SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");

		// all targets are signals
		for(ScenarioElement se: targets)
			if(se.getMyType().compareTo(ScenarioElement.Type.signal)!=0)
				SiriusErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		for (Controller_SIG_Pretimed_Plan pretimed_plan : plan.values())
			pretimed_plan.validate();
		
	}

	@Override
	public void reset() {
		super.reset();
		cperiod = 0;

		for (Controller_SIG_Pretimed_Plan pretimed_plan : plan.values())
			pretimed_plan.reset();
	}

	@Override
	public boolean register() {
		return true; // signal controllers don't have to register, because the signal does this for them.
	}

	@Override
	public boolean deregister() {		
		return false;  // signal controllers cannot deregister, because the signal does this for them.
	}

	// auxiliary classes: plan list, plan sequence, etc

	static class PlanList {
		private List<Plan> plan_l;
		public PlanList(Table tbl) {
			plan_l = new ArrayList<Controller_SIG_Pretimed.Plan>();
			for (int row = 0; row < tbl.getNoRows(); ++row)
				process_row(tbl, row);
		}
		private void process_row(Table tbl, int row) {
			final String id = tbl.getTableElement(row, "Plan ID");
			Plan plan = getPlan(id);
			if (null == plan) {
				plan = new Plan(id);
				plan_l.add(plan);
			}
			plan.process_row(tbl, row);
		}
		private Plan getPlan(String id) {
			for (Plan plan : plan_l)
				if (id.equals(plan.getId())) return plan;
			return null;
		}
		/**
		 * @return the plan list
		 */
		public List<Plan> getPlanList() {
			return plan_l;
		}
	}

	static class Plan {
		private String id;
		private Double cycle_length;
		private List<Intersection> ip_l;
		public Plan() {}
		public Plan(String id) {
			this.id = id;
			this.ip_l = new ArrayList<Controller_SIG_Pretimed.Intersection>();
		}
		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return the cycle length [sec]
		 */
		public Double getCycleLength() {
			return cycle_length;
		}
		/**
		 * @param cycle_length the cycle length [sec]
		 */
		public void setCycleLength(Double cycle_length) {
			this.cycle_length = cycle_length;
		}
		/**
		 * @return the intersection plan list
		 */
		public List<Intersection> getIntersection() {
			return ip_l;
		}
		void process_row(Table tbl, int row) {
			String id = tbl.getTableElement(row, "Intersection");
			Intersection ip = getIntersectionPlan(id);
			if (null == ip) {
				ip = new Intersection(id, Double.parseDouble(tbl.getTableElement(row, "Offset")));
				ip_l.add(ip);
			}
			ip.process_row(tbl, row);
		}
		private Intersection getIntersectionPlan(String node_id) {
			for (Intersection ip : ip_l)
				if (node_id.equals(ip.getNodeId()))
					return ip;
			return null;
		}
	}

	static class Intersection {
		private String node_id;
		private Double offset;
		private List<Stage> stage_l;
		public Intersection(String node_id, Double offset) {
			this.node_id = node_id;
			this.offset = offset;
			stage_l = new ArrayList<Controller_SIG_Pretimed.Stage>();
		}
		/**
		 * @return the node id
		 */
		public String getNodeId() {
			return node_id;
		}
		/**
		 * @return the offset [sec]
		 */
		public Double getOffset() {
			return offset;
		}
		/**
		 * @return the stage list
		 */
		public List<Stage> getStage() {
			return stage_l;
		}
		void process_row(Table tbl, int row) {
			stage_l.add(new Stage(
					tbl.getTableElement(row, "Movement A"),
					tbl.getTableElement(row, "Movement B"),
					Double.parseDouble(tbl.getTableElement(row, "Green Time"))));
		}
	}

	static class Stage {
		String movA;
		String movB;
		Double green_time;
		public Stage(String movA, String movB, Double green_time) {
			this.movA = movA;
			this.movB = movB;
			this.green_time = green_time;
		}
		/**
		 * @return the movA
		 */
		public String getMovA() {
			return movA;
		}
		/**
		 * @return the movB
		 */
		public String getMovB() {
			return movB;
		}
		/**
		 * @return the green time [sec]
		 */
		public Double getGreenTime() {
			return green_time;
		}
	}

	/**
	 * Plan sequence unit
	 */
	private static class PlanRun implements Comparable<PlanRun> {
		Double start_time;
		String plan_id;
		Double cycle_length;
		public PlanRun(String plan_id, Double start_time, Double cycle_length) {
			super();
			this.start_time = start_time;
			this.plan_id = plan_id;
			this.cycle_length = cycle_length;
		}
		@Override
		public int compareTo(PlanRun other) {
			return Double.compare(this.start_time, other.start_time);
		}
		/**
		 * @return the start time [seconds]
		 */
		public Double getStartTime() {
			return start_time;
		}
		/**
		 * @return the plan id
		 */
		public String getPlanId() {
			return plan_id;
		}
		/**
		 * @return the cycle length [sec]
		 */
		public Double getCycleLength() {
			return cycle_length;
		}
	}

	private static class PlanSequence {
		List<PlanRun> pr_l;
		public PlanSequence(Table tbl) {
			pr_l = new ArrayList<Controller_SIG_Pretimed.PlanRun>();
			for (int i = 0; i < tbl.getNoRows(); ++i)
				pr_l.add(new PlanRun(tbl.getTableElement(i, "Plan ID"),
						Double.parseDouble(tbl.getTableElement(i, "Start Time")),
						Double.parseDouble(tbl.getTableElement(i, "Cycle Length"))));
			java.util.Collections.sort(pr_l);
		}
		public List<PlanRun> getPlanReference() {
			return pr_l;
		}
	}
}
