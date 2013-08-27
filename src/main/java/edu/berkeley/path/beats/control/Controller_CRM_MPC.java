package edu.berkeley.path.beats.control;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.BeatsFormatter;
import edu.berkeley.path.beats.simulator.BeatsMath;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.DemandProfile;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.ControlPolicyMaker;
import edu.berkeley.path.beats.simulator.Scenario;

public class Controller_CRM_MPC extends Controller {
	
	// constants
	private double opt_period;		// [sec] period for calling the optimizer
	private double opt_horizon;		// [sec] optimization horizon
	private double opt_dt;			// [sec] time step for the optimizer
	private int opt_period_int;		// [-] optimization period as multiple of controller dt
	private int opt_horizon_int;	// [-] optimization period as multiple of controller dt

	// data for control algorithm
	private Map<Long,Double> initialDensity;	private Map<Long,Double[]> splitRatios;
	private Map<Long,Double[]> rampDemands;	
	private Network network;
	
	// variables
	private double time_last_opt;			// [sec] time of last optimization call
	private Map<Long,Double[]> metering_rate;	// target link id to control profile in [veh/hr]
	
	private ControlPolicyMaker control_algorithm;
	//private Scenario opt_scenario;
	
	private static enum Type { adjoint , actm_lp , NULL}

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
		
		//this.opt_scenario = myScenario;
				
		// generate the controller algorithm
		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) getJaxbController().getParameters();
		control_algorithm = null;
		if (null != params && params.has("policy")){
			Type myType;
	    	try {
				myType = Type.valueOf(params.get("policy").toLowerCase());
			} catch (IllegalArgumentException e) {
				myType = Type.NULL;
			}
			
			switch(myType){
//				case adjoint:
//					control_algorithm = new ControllerAlgorithm_CRM_Adjoint();
//					break;
				case actm_lp:
					control_algorithm = new ControllerAlgorithm_CRM_ACTM_LP();
					break;
				case NULL:
					break;
			}
		}
		
		// read timing parameters
		if (null != params && params.has("dt_optimize")){
			String str = params.get("dt_optimize");
			if(str!=null)
				opt_period =Double.parseDouble(str);
			else
				opt_period = getDtinseconds();
		}

		if (null != params && params.has("opt_timestep")){
			String str = params.get("opt_timestep");
			if(str!=null)
				opt_dt =Double.parseDouble(str);
			else
				opt_dt = getMyScenario().getSimdtinseconds();
		}

		if (null != params && params.has("opt_horizon")){
			String str = params.get("opt_horizon");

			if(str!=null)
				opt_horizon = Double.parseDouble(str);
			else
				opt_horizon = Double.NaN;
		}
		
		opt_period_int = BeatsMath.round(opt_period/getDtinseconds());
		opt_horizon_int = BeatsMath.round(opt_horizon/getDtinseconds());
		
		// initialize data for control algorithm
		
		// TEMPORARY: the network is a reference to the scenario network (dangerous)
		network = (Network) getMyScenario().getNetworkSet().getNetwork().get(0);
		
		// populate initialDensity
		initialDensity = new HashMap<Long,Double>();
		for(edu.berkeley.path.beats.jaxb.Link link : network.getListOfLinks())
			initialDensity.put(link.getId(), null);

		// populate splitRatios
		splitRatios = new HashMap<Long,Double[]>();
		for(edu.berkeley.path.beats.jaxb.Node node : network.getListOfNodes())
			splitRatios.put(node.getId(), null);
		
		// populate rampDemands and metering_rate
		rampDemands = new HashMap<Long,Double[]>();
		metering_rate = new HashMap<Long,Double[]>();
		for(edu.berkeley.path.beats.jaxb.Link link : network.getListOfLinks())
			if( ((Link)link).getMyType().compareTo(Link.Type.onramp)==0) {
				rampDemands.put(link.getId(), null);
				metering_rate.put(link.getId(), null);
			}
	}

	@Override
	protected void validate() {

		super.validate();
		
		if(control_algorithm==null)
			BeatsErrorLog.addError("Control algorithm undefined.");
		
		if(Double.isNaN(opt_horizon))
			BeatsErrorLog.addError("Optimization horizon undefined.");
		
		// opt_period is a multiple of dtinseconds
		if(!BeatsMath.isintegermultipleof(opt_period,getDtinseconds()))
			BeatsErrorLog.addError("dt_optimize is not a a multiple of dtinseconds.");
		
		// dtinseconds is a multiple of opt_dt
		if(!BeatsMath.isintegermultipleof(getDtinseconds(),opt_dt))
			BeatsErrorLog.addError("dtinseconds is not a multiple of opt_dt.");

		// opt_horizon is a multiple of opt_dt
		if(!Double.isNaN(opt_horizon) && !BeatsMath.isintegermultipleof(opt_horizon,opt_dt))
			BeatsErrorLog.addError("opt_horizon is a multiple of opt_dt.");		
		
		// opt_horizon is greater than opt_period
		if(!Double.isNaN(opt_horizon) && !BeatsMath.greaterorequalthan(opt_horizon,opt_period) )
			BeatsErrorLog.addError("opt_horizon is less than opt_period.");		
	}

	@Override
	protected void reset() {

		super.reset();

		// initialize to positive infinity
		Iterator<Map.Entry<Long, Double[]>> it = metering_rate.entrySet().iterator();
		while(it.hasNext()) {
			Double [] val = new Double [opt_period_int];
			for(int i=0;i<opt_period_int;i++)
				val[i] = Double.POSITIVE_INFINITY;
			it.next().setValue(val);
		}
		
		// initialize t_lastupdate
		time_last_opt = Double.NEGATIVE_INFINITY;
	}

	/////////////////////////////////////////////////////////////////////
	// update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void update() throws BeatsException {
		
		double time_current = getMyScenario().getCurrentTimeInSeconds();
		double time_since_last_opt = time_current-time_last_opt;

		// if it is time to optimize, update metering rate profile
		if(BeatsMath.greaterorequalthan(time_since_last_opt,opt_period)){

			// copy initial density
			for(edu.berkeley.path.beats.jaxb.Link link : network.getListOfLinks())
				initialDensity.put(link.getId(), ((Link)link).getTotalDensityInVeh(0));
			
			// copy split ratios
			for(edu.berkeley.path.beats.jaxb.Node node : network.getListOfNodes())
				splitRatios.put(node.getId(),null);
	
			// copy ramp demands
			DemandSet demand_set = (DemandSet) getMyScenario().getDemandSet();
			for(edu.berkeley.path.beats.jaxb.Link link : network.getListOfLinks())
				if( ((Link)link).getMyType().compareTo(Link.Type.onramp)==0){					
					Double [] future_demands = BeatsFormatter.toDoubleArray(demand_set.getFutureTotalDemandInVeh_NoNoise(link.getId(),opt_dt,opt_horizon_int));
					rampDemands.put(link.getId(),future_demands);
				}
			
			// call control algorithm
			metering_rate = control_algorithm.compute(initialDensity,splitRatios, rampDemands, getMyScenario());
			time_last_opt = time_current;
			time_since_last_opt = 0;
			
			// check metering_rate is not null
			if(metering_rate==null)
				throw new BeatsException("Control algorithm returned null.");

			// check lengths are correct
			Iterator<Map.Entry<Long,Double[]>> it = metering_rate.entrySet().iterator();
			while(it.hasNext())
				if(it.next().getValue().length<opt_period_int)
					throw new BeatsException("Control algorithm returned policy with incorrect number of targets.");
			
		}

		// return the values corresponding to the current time
		int time_index = BeatsMath.floor(time_since_last_opt/getDtinseconds());		
		for(int i=0;i<getTargets().size();i++)
			setControl_maxflow(i, metering_rate.get(getTargets().get(i).getId())[time_index]);
	}
	
}
