package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.BeatsMath;
import edu.berkeley.path.beats.simulator.ControlAlgorithm;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Scenario;

public class Controller_CRM_MPC extends Controller {
	
	// constants
	
	//protected double dt_mod;
	//protected double dt_control;
	protected double dt_optimize;		// [sec] minimum period for calling the optimizer
	protected double opt_horizon;		// [sec] optimization horizon
	protected double opt_dt;			// [sec] time step for the optimizer
	
	
	protected int numTime;				// [-] length of the control profile

	// variables
	protected double time_last_opt;			// [sec] time of last optimization call
	protected double [][] metering_rate;	// [???] metering rate indexed by time and target
	
	protected ControlAlgorithm control_algorithm;
	protected Scenario opt_scenario;
	
	protected static enum Type { adjoint , actm_lp , fake, NULL};

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_CRM_MPC(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c,Controller.Type myType) {
		super(myScenario,c,myType);
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		
		this.opt_scenario = myScenario;
				
		// generate the controller algorithm
		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) jaxbController.getParameters();
		control_algorithm = null;
		if (null != params && params.has("policy")){
			Type myType;
	    	try {
				myType = Type.valueOf(params.get("policy").toLowerCase());
			} catch (IllegalArgumentException e) {
				myType = Type.NULL;
			}
			
			switch(myType){
				case adjoint:
					control_algorithm = new ControllerAlgorithm_CRM_Adjoint();
					break;
				case actm_lp:
					control_algorithm = new ControllerAlgorithm_CRM_ACTM_LP();
					break;
				case fake:
					control_algorithm = new ControllerAlgorithm_CRM_Fake();
					break;
				case NULL:
					break;
			}	
			if(control_algorithm!=null)
				control_algorithm.setName(control_algorithm.getClass().getSimpleName());
		}
		
		
		// read timing parameters
		if (null != params && params.has("dt_optimize")){
			String str = params.get("dt_optimize");
			if(str!=null)
				dt_optimize =Double.parseDouble(str);
			else
				dt_optimize = dtinseconds;
		}

		if (null != params && params.has("opt_timestep")){
			String str = params.get("opt_timestep");
			if(str!=null)
				opt_dt =Double.parseDouble(str);
			else
				opt_dt = myScenario.getSimDtInSeconds();
		}

		if (null != params && params.has("opt_horizon")){
			String str = params.get("opt_horizon");

			if(str!=null)
				opt_horizon = Double.parseDouble(str);
			else
				opt_horizon = Double.NaN;
		}
		
		// initialize metering rate
		int numTargets = this.getTargets().size();		
		this.numTime = BeatsMath.ceil(dt_optimize/dtinseconds);
		metering_rate = new double[numTime][numTargets];
	
	}

	@Override
	protected void validate() {

		super.validate();
		
		if(control_algorithm==null)
			BeatsErrorLog.addError("Control algorithm undefined.");
		
		if(Double.isNaN(opt_horizon))
			BeatsErrorLog.addError("Optimization horizon undefined.");
		
		// dt_optimize is a multiple of dtinseconds
		if(!BeatsMath.isintegermultipleof(dt_optimize,dtinseconds))
			BeatsErrorLog.addError("dt_optimize is not a a multiple of dtinseconds.");
		
		// dtinseconds is a multiple of opt_dt
		if(!BeatsMath.isintegermultipleof(dtinseconds,opt_dt))
			BeatsErrorLog.addError("dtinseconds is not a multiple of opt_dt.");

		// opt_horizon is a multiple of opt_dt
		if(!Double.isNaN(opt_horizon) && !BeatsMath.isintegermultipleof(opt_horizon,opt_dt))
			BeatsErrorLog.addError("opt_horizon is a multiple of opt_dt.");		
	}

	@Override
	protected void reset() {

		super.reset();

		// initialize to positive infinity
		int k,i;
		for(k=0;k<metering_rate.length;k++)
			for(i=0;i<metering_rate[k].length;i++)
				metering_rate[k][i] = Double.POSITIVE_INFINITY;
		
		// initialize t_lastupdate
		time_last_opt = Double.NEGATIVE_INFINITY;
	}

	/////////////////////////////////////////////////////////////////////
	// update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void update() throws BeatsException {
		
		double time_current = this.myScenario.getCurrentTimeInSeconds();
		double time_since_last_opt = time_current-time_last_opt;
		
		// if it is time to optimize, update metering rate profile
		if(BeatsMath.greaterorequalthan(time_since_last_opt,dt_optimize)){
			control_algorithm.compute(opt_scenario,opt_horizon,targets,metering_rate);
			time_last_opt = time_current;
			time_since_last_opt = 0;
		}

		// return the values corresponding to the current time
		int time_index = BeatsMath.floor(time_since_last_opt/dtinseconds);		
		for(int i=0;i<targets.size();i++)
			control_maxflow[i] = this.metering_rate[time_index][i];
		
	}
}
