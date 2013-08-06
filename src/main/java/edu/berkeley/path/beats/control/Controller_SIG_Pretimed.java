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

import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsMath;
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

	private static Logger logger = Logger.getLogger(Controller_SIG_Pretimed_Plan.class);
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_SIG_Pretimed(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c,Controller.Type myType) {
		super(myScenario,c,myType);
	}
	
//	public Controller_SIG_Pretimed(Scenario myScenario) {
//		// TODO Auto-generated constructor stub
//	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset  / update
	/////////////////////////////////////////////////////////////////////

	/** Implementation of {@link InterfaceComponent#populate}.
	 * @param jaxbobject Object
	 */
	@Override
	protected void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;

		// must have these
		if(jaxbc.getTargetElements()==null)
			return;
		if(jaxbc.getTargetElements().getScenarioElement()==null)
			return;

		// check all tables
		for (String table_name : new String[] {"Cycle Length", "Offsets", "Plan List", "Plan Sequence"})
			if (null == getTables().get(table_name)) {
				BeatsErrorLog.addError("Controller " + jaxbc.getId() + ": no '" + table_name + "' table");
				return;
			}

		// restoring plan list
		PlanList planlist = new PlanList(getTables().get("Cycle Length"), getTables().get("Offsets"), getTables().get("Plan List"));
		// processing plan list
		plan = new java.util.HashMap<String, Controller_SIG_Pretimed_Plan>();
		for (Plan plan_raw : planlist.getPlanList()) {
			Controller_SIG_Pretimed_Plan pretimed_plan = new Controller_SIG_Pretimed_Plan();
			pretimed_plan.populate(this,getMyScenario(), plan_raw);
			plan.put(plan_raw.getId(), pretimed_plan);
		}

		// restoring plan sequence
		PlanSequence plan_seq = new PlanSequence(getTables().get("Plan Sequence"));
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

	@Override
	protected void update() {

		double simtime = getMyScenario().getCurrentTimeInSeconds();

		// time to switch plans .....................................
		if( cperiod < planstarttime.length-1 ){
			if( BeatsMath.greaterorequalthan( simtime , planstarttime[cperiod+1] + transdelay ) ){
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
	protected void validate() {
		
		super.validate();
		
		int i;
		
		// transdelay>=0
		if(transdelay<0)
			BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		// first planstarttime=0
		if(planstarttime[0]!=0)
			BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		// planstarttime is increasing
		for(i=1;i<planstarttime.length;i++)
			if(planstarttime[i]<=planstarttime[i-1])
				BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		// all plansequence ids found
		for(i=0;i<plansequence.length;i++)
			if (null == plansequence[i])
				BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");

		// all targets are signals
		for(ScenarioElement se: getTargets())
			if(se.getMyType().compareTo(ScenarioElement.Type.signal)!=0)
				BeatsErrorLog.addError("UNDEFINED ERROR MESSAGE.");
		
		for (Controller_SIG_Pretimed_Plan pretimed_plan : plan.values())
			pretimed_plan.validate();
		
	}

	@Override
	protected void reset() {
		super.reset();
		cperiod = 0;

		for (Controller_SIG_Pretimed_Plan pretimed_plan : plan.values())
			pretimed_plan.reset();
	}

	// auxiliary classes: plan list, plan sequence, etc

	static class PlanList {
		private List<Plan> plan_l;
		/**
		 * Constructs a plan list from "Cycle Length", "Offsets", "Plan List" tables
		 * @param plan_tbl the "Cycle Length" table
		 * @param intersection_tbl the "Offsets" table
		 * @param stage_tbl the "Plan List" table
		 */
		public PlanList(Table plan_tbl, Table intersection_tbl, Table stage_tbl) {
			plan_l = new ArrayList<Controller_SIG_Pretimed.Plan>(plan_tbl.getNoRows());
			process_plans(plan_tbl);
			process_intersections(intersection_tbl);
			process_stages(stage_tbl);
		}
		private void process_plans(Table tbl) {
			for (int row = 0; row < tbl.getNoRows(); ++row)
				plan_l.add(new Plan(tbl.getTableElement(row, "Plan ID"),
						Double.parseDouble(tbl.getTableElement(row, "Cycle Length"))));
		}
		private void process_intersections(Table tbl) {
			for (int row = 0; row < tbl.getNoRows(); ++row) {
				String plan_id = tbl.getTableElement(row, "Plan ID");
				Plan plan = getPlan(plan_id);
				if (null == plan)
					logger.error("Plan '" + plan_id + "' not found");
				else{
					long int_id = Long.parseLong(tbl.getTableElement(row, "Intersection"));
					plan.addIntersection(new Intersection(int_id,Double.parseDouble(tbl.getTableElement(row, "Offset"))));
				}
			}
		}
		private void process_stages(Table tbl) {
			for (int row = 0; row < tbl.getNoRows(); ++row) {
				String plan_id = tbl.getTableElement(row, "Plan ID");
				String node_id = tbl.getTableElement(row, "Intersection");
				Plan plan = getPlan(plan_id);
				if (null == plan)
					logger.error("Plan '" + plan_id + "' not found");
				else {
					Intersection intersection = plan.getIntersection(node_id);
					if (null == intersection)
						logger.error("Plan '" + plan_id + "': Intersection '" + node_id + "' not found");
					else
						intersection.addStage(new Stage(
								tbl.getTableElement(row, "Movement A"),
								tbl.getTableElement(row, "Movement B"),
								Double.parseDouble(tbl.getTableElement(row, "Green Time"))));
				}
			}
		}
		private Plan getPlan(String id) {
			for (Plan plan : plan_l)
				if (plan.getId().equals(id))
					return plan;
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
		protected Plan() {}
		/**
		 * Constructs a plan for the given ID and cycle length
		 * @param id plan ID
		 * @param cycle_length cycle length [sec]
		 */
		public Plan(String id, Double cycle_length) {
			this.id = id;
			this.cycle_length = cycle_length;
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
		protected void setId(String id) {
			this.id = id;
		}
		/**
		 * @return the cycle length [sec]
		 */
		public Double getCycleLength() {
			return cycle_length;
		}
		/**
		 * @return the intersection plan list
		 */
		public List<Intersection> getIntersection() {
			return ip_l;
		}
		/**
		 * Adds an intersection to the intersection list
		 * @param intersection the intersection to add
		 */
		void addIntersection(Intersection intersection) {
			ip_l.add(intersection);
		}
		/**
		 * Searches for an intersection with the given node ID
		 * @param node_id the node ID
		 * @return null, if an intersection was not found
		 */
		Intersection getIntersection(String node_id) {
			for (Intersection ip : ip_l)
				if (node_id.equals(ip.getNodeId()))
					return ip;
			return null;
		}
	}

	static class Intersection {
		private long node_id;
		private Double offset;
		private List<Stage> stage_l;
		/**
		 * Constructs an intersection for the given node ID and offset
		 * @param node_id
		 * @param offset
		 */
		public Intersection(long node_id, Double offset) {
			this.node_id = node_id;
			this.offset = offset;
			stage_l = new ArrayList<Controller_SIG_Pretimed.Stage>();
		}
		/**
		 * @return the node id
		 */
		public long getNodeId() {
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
		/**
		 * Adds a stage to the stage list
		 * @param stage the stage to add
		 */
		void addStage(Stage stage) {
			stage_l.add(stage);
		}
	}

	static class Stage {
		String movA;
		String movB;
		Double green_time;
		/**
		 * Constructs a stage for the given movements and green time
		 * @param movA
		 * @param movB
		 * @param green_time the green time [sec]
		 */
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
		/**
		 * Constructs a plan reference for the given plan ID and start time
		 * @param plan_id the plan ID
		 * @param start_time the start time [sec]
		 */
		public PlanRun(String plan_id, Double start_time) {
			this.start_time = start_time;
			this.plan_id = plan_id;
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
	}

	private static class PlanSequence {
		List<PlanRun> pr_l;
		/**
		 * Constructs a plan sequence from "Plan Sequence" table
		 * @param tbl the "Plan Sequence" table
		 */
		public PlanSequence(Table tbl) {
			pr_l = new ArrayList<Controller_SIG_Pretimed.PlanRun>();
			for (int i = 0; i < tbl.getNoRows(); ++i)
				pr_l.add(new PlanRun(tbl.getTableElement(i, "Plan ID"),
						Double.parseDouble(tbl.getTableElement(i, "Start Time"))));
			java.util.Collections.sort(pr_l);
		}
		/**
		 * @return the plan reference list
		 */
		public List<PlanRun> getPlanReference() {
			return pr_l;
		}
	}
}
