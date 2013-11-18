package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.actuator.ActuatorRampMeter;
import edu.berkeley.path.beats.control.predictive.*;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.simulator.*;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.DemandProfile;
import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.FundamentalDiagram;
import edu.berkeley.path.beats.simulator.FundamentalDiagramProfile;
import edu.berkeley.path.beats.simulator.InitialDensitySet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SplitRatioProfile;
import edu.berkeley.path.beats.simulator.SplitRatioSet;

public class Controller_CRM_MPC extends Controller {

    // policy maker
    private RampMeteringPolicyMaker policy_maker;
    private RampMeteringPolicySet policy;
    private RampMeteringControlSet controller_parameters;
    private Network network;
    private FundamentalDiagramSet constant_fd_set; // used by scenario-less act 2 runner;

	// parameters
	private double pm_period;		  // [sec] period for calling the policy maker
	private double pm_horizon;		  // [sec] policy maker time horizon
    private double pm_dt;		 	  // [sec] internal time step for the policy maker

    // derived
    private int pm_horizon_steps;     // pm_horizon/pm_dt

	// variable
	private double time_last_opt;     // [sec] time of last policy maker call

	private static enum PolicyMakerType {tester,adjoint,actm_lp,NULL}

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

//	public Controller_CRM_MPC(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c, Controller.Algorithm myType) {
//		super(myScenario,c,myType);
//	}


	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

		// generate the policy maker
		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) getJaxbController().getParameters();
		policy_maker = null;
		if (null != params && params.has("policy")){
            PolicyMakerType myPMType;
	    	try {
				myPMType = PolicyMakerType.valueOf(params.get("policy").toLowerCase());
			} catch (IllegalArgumentException e) {
				myPMType = PolicyMakerType.NULL;
			}

			switch(myPMType){
                case tester:
                    policy_maker = new PolicyMaker_Tester();
                    break;
				case adjoint:
					policy_maker = new AdjointRampMeteringPolicyMaker();
					break;
//				case actm_lp:
//                    policy_maker = new PolicyMaker_CRM_ACTM_LP();
//					break;
				case NULL:
					break;
			}
		}

		// read timing parameters
        if(params!=null){
            pm_period = params.readParameter("dt_optimize",getDtinseconds());
            pm_dt = params.readParameter("policy_maker_timestep",getMyScenario().getSimdtinseconds());
            pm_horizon = params.readParameter("policy_maker_horizon",Double.NaN);
        }
        else{
            pm_period = getDtinseconds();
            pm_dt = getMyScenario().getSimdtinseconds();
            pm_horizon = Double.NaN;
        }

        pm_horizon_steps = BeatsMath.round(pm_horizon/pm_dt);

        // assign network (it will already be assigned if controller is scenario-less)
        if(network==null && myScenario!=null)
            network = (Network) myScenario.getNetworkSet().getNetwork().get(0);

        // controller parameters
        controller_parameters = new RampMeteringControlSet();
        for(edu.berkeley.path.beats.jaxb.Link jaxbL : network.getListOfLinks()){
            Link L = (Link) jaxbL;
            if(L.isSource()){
                RampMeteringControl con = new RampMeteringControl();
                con.link = L;
                con.max_rate = 1;    // veh/sec
                con.min_rate = 0;    // veh/sec
                controller_parameters.control.add(con);
            }
        }

    }

	@Override
	protected void validate() {

		super.validate();

		if(policy_maker==null)
			BeatsErrorLog.addError("Control algorithm undefined.");

		if(Double.isNaN(pm_horizon))
			BeatsErrorLog.addError("Optimization horizon undefined.");

        // opt_horizon is a multiple of pm_dt
        if(!Double.isNaN(pm_horizon) && !BeatsMath.isintegermultipleof(pm_horizon, pm_dt))
            BeatsErrorLog.addError("pm_horizon is a multiple of pm_dt.");

        // opt_horizon is greater than opt_period
        if(!Double.isNaN(pm_horizon) && !BeatsMath.greaterorequalthan(pm_horizon,pm_period) )
            BeatsErrorLog.addError("pm_horizon is less than pm_period.");

        // validations below this make sensor only in the context of a scenario
        if(getMyScenario()==null)
            return;

        // opt_period is a multiple of dtinseconds
		if(!BeatsMath.isintegermultipleof(pm_period,getDtinseconds()))
			BeatsErrorLog.addError("pm_period is not a a multiple of dtinseconds.");

		// dtinseconds is a multiple of pm_dt
		if(!BeatsMath.isintegermultipleof(getDtinseconds(), pm_dt))
			BeatsErrorLog.addError("dtinseconds ("+getDtinseconds()+") is not a multiple of pm_dt ("+pm_dt+").");

	}

	@Override
	protected void reset() {
		super.reset();
		time_last_opt = Double.NEGATIVE_INFINITY;
	}

	/////////////////////////////////////////////////////////////////////
	// update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void update() throws BeatsException {

		double time_current = getMyScenario().getCurrentTimeInSeconds();

		// if it is time to optimize, update metering rate profile
		if(BeatsMath.greaterorequalthan(time_current-time_last_opt, pm_period)){

			// call policy maker (everything in SI units)
            policy = policy_maker.givePolicy( network,
                                              myScenario.gather_current_fds(time_current),
                                              myScenario.predict_demands(time_current,pm_dt,pm_horizon_steps),
                                              myScenario.predict_split_ratios(time_current,pm_dt,pm_horizon_steps),
                                              myScenario.gather_current_densities(),
                                              controller_parameters,
                                              pm_dt);

            // update time keeper
			time_last_opt = time_current;
		}

        // .....
        send_policy_to_actuators(time_current);

	}

    /////////////////////////////////////////////////////////////////////
    // private
    /////////////////////////////////////////////////////////////////////

    public void send_policy_to_actuators(double time_current){
        if(policy==null)
            return;
        double time_since_last_pm_call = time_current-time_last_opt;
        int time_index = BeatsMath.floor(time_since_last_pm_call/pm_dt);
        for(RampMeteringPolicyProfile rmprofile : policy.profiles){
            for(Actuator act : actuators){
                ActuatorRampMeter this_actuator = (ActuatorRampMeter) act;
                if(this_actuator.getLink().getId()==rmprofile.sensorLink.getId())
                    this_actuator.setMeteringRateInVPH( rmprofile.rampMeteringPolicy.get(time_index)*3600d  );
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Act 2 api
    /////////////////////////////////////////////////////////////////////

    public Controller_CRM_MPC(edu.berkeley.path.beats.jaxb.Controller c,Network network,FundamentalDiagramSet fd_set){
        super(null,c,Algorithm.CRM_MPC);
        this.ison = true;
        this.network = network;
        this.constant_fd_set = fd_set;
        this.populate(null);
    }

    public void update_and_send_policy_to_actuators(double time_current,DemandSet demand_set,SplitRatioSet split_ratio_set,InitialDensitySet init_dens_set){

        policy = policy_maker.givePolicy( network,
                                          constant_fd_set,
                                          demand_set,
                                          split_ratio_set,
                                          init_dens_set,
                                          controller_parameters,
                                          pm_dt);

        // update time keeper
        time_last_opt = time_current;

        send_policy_to_actuators(time_current);
    }


}
