package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.control.predictive.*;
import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.simulator.*;
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
    private RampMeteringControlSet controller_parameters;
    private Network network;

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

	public Controller_CRM_MPC(Scenario myScenario, edu.berkeley.path.beats.jaxb.Controller c, Controller.Algorithm myType) {
		super(myScenario,c,myType);
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

		// generate the policy maker
		edu.berkeley.path.beats.simulator.Parameters params = (edu.berkeley.path.beats.simulator.Parameters) getJaxbController().getParameters();
		policy_maker = null;
		if (null != params && params.has("policy")){
            PolicyMakerType myType;
	    	try {
				myType = PolicyMakerType.valueOf(params.get("policy").toLowerCase());
			} catch (IllegalArgumentException e) {
				myType = PolicyMakerType.NULL;
			}

			switch(myType){
                case tester:
                    policy_maker = new PolicyMaker_Tester();
                    break;
				case adjoint:
					//policy_maker = new RampMeteringAdjoint();
//					policy_maker = new ControllerAlgorithm_CRM_Adjoint();
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

        // auxiliary parameters
		//opt_period_int = BeatsMath.round(opt_period/getDtinseconds());
		//opt_horizon_int = BeatsMath.round(opt_horizon/getDtinseconds());

	}

    protected void assign_network(Network net){
        network = net;
    }

	@Override
	protected void validate() {

		super.validate();

		if(policy_maker==null)
			BeatsErrorLog.addError("Control algorithm undefined.");

		if(Double.isNaN(pm_horizon))
			BeatsErrorLog.addError("Optimization horizon undefined.");

		// opt_period is a multiple of dtinseconds
		if(!BeatsMath.isintegermultipleof(pm_period,getDtinseconds()))
			BeatsErrorLog.addError("pm_period is not a a multiple of dtinseconds.");

		// dtinseconds is a multiple of pm_dt
		if(!BeatsMath.isintegermultipleof(getDtinseconds(), pm_dt))
			BeatsErrorLog.addError("dtinseconds ("+getDtinseconds()+") is not a multiple of pm_dt ("+pm_dt+").");

		// opt_horizon is a multiple of pm_dt
		if(!Double.isNaN(pm_horizon) && !BeatsMath.isintegermultipleof(pm_horizon, pm_dt))
			BeatsErrorLog.addError("pm_horizon is a multiple of pm_dt.");

		// opt_horizon is greater than opt_period
		if(!Double.isNaN(pm_horizon) && !BeatsMath.greaterorequalthan(pm_horizon,pm_period) )
			BeatsErrorLog.addError("pm_horizon is less than pm_period.");
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
		double time_since_last_opt = time_current-time_last_opt;

		// if it is time to optimize, update metering rate profile
		if(BeatsMath.greaterorequalthan(time_since_last_opt, pm_period)){

            JaxbObjectFactory factory = new JaxbObjectFactory();

            // initial densities
            InitialDensitySet init_dens_set = (InitialDensitySet) factory.createInitialDensitySet();
            for(edu.berkeley.path.beats.jaxb.Link jaxbL : network.getListOfLinks()){
                Density den = factory.createDensity();
                den.setContent(String.format("%f",((Link) jaxbL).getTotalDensityInVeh(0)));
                init_dens_set.getDensity().add(den);
            }

            // demands
            DemandSet demand_set = (DemandSet) factory.createDemandSet();
            for(edu.berkeley.path.beats.jaxb.Link jaxbL : network.getListOfLinks()){
                Link L = (Link) jaxbL;
                if(L.isSource()){

                    // add demands to demand_set
                    DemandProfile dp = (DemandProfile) factory.createDemandProfile();
                    demand_set.getDemandProfile().add(dp);
                    Demand dem = factory.createDemand();
                    dp.getDemand().add(dem);

                    // set values
                    dp.setLinkIdOrg(L.getId());
                    dem.setContent(BeatsFormatter.csv(L.getDemandProfile().predictTotal(time_current, pm_dt, pm_horizon_steps), ","));
                }
            }

            // split ratios
            SplitRatioSet split_ratio_set = (SplitRatioSet) factory.createSplitRatioSet();
            for(edu.berkeley.path.beats.jaxb.Node jaxbN : network.getListOfNodes()){
                Node N = (Node) jaxbN;

                SplitRatioProfile srp = (SplitRatioProfile) factory.createSplitRatioProfile();
                split_ratio_set.getSplitRatioProfile().add(srp);

                for(Input in : N.getInputs().getInput()){
                    for(Output out : N.getOutputs().getOutput()){
                        for(int vt_index=0;vt_index<myScenario.getNumVehicleTypes();vt_index++)    {

                            Splitratio splitratio = factory.createSplitratio();
                            srp.getSplitratio().add(splitratio);

                            // set values
                            splitratio.setLinkIn(in.getLinkId());
                            splitratio.setLinkOut(out.getLinkId());
                            splitratio.setVehicleTypeId(myScenario.getVehicleTypeSet().getVehicleType().get(vt_index).getId());
                            double [] sr = N.getSplitRatioProfile().predict(
                                    in.getLinkId(),
                                    out.getLinkId(),
                                    vt_index,time_current, pm_dt, pm_horizon_steps);
                            splitratio.setContent(BeatsFormatter.csv(sr, ","));
                        }
                    }
                }
            }

            // fundamental diagrams
            FundamentalDiagramSet fd_set = factory.createFundamentalDiagramSet();
            for(edu.berkeley.path.beats.jaxb.Link jaxbL : network.getListOfLinks()){
                Link L = (Link) jaxbL;
                FundamentalDiagramProfile fdp = (FundamentalDiagramProfile) factory.createFundamentalDiagramProfile();
                fd_set.getFundamentalDiagramProfile().add(fdp);

                // set values
                fdp.setLinkId(L.getId());
                FundamentalDiagram fd = (FundamentalDiagram) factory.createFundamentalDiagram();
                fd.copyfrom(L.getFundamentalDiagramProfile().getFDforTime(time_current));
                fd.setOrder(0);
                fdp.getFundamentalDiagram().add(fd);
            }

			// call policy maker
            RampMeteringPolicySet policy = policy_maker.givePolicy(  network,
                                                                     fd_set,
                                                                     demand_set,
                                                                     split_ratio_set,
                                                                     init_dens_set,
                                                                     controller_parameters,
                                                                     pm_dt);

            // check policy is not null
            if(policy==null)
                throw new BeatsException("Control algorithm returned null.");

            // update time keeper
			time_last_opt = time_current;

		}

		// actuate
//		int time_index = BeatsMath.floor(time_since_last_opt/getDtinseconds());
//		for(int i=0;i<actuators.size();i++)
//			setControl_maxflow(i, metering_rate.get(actuators.get(i).getId())[time_index]);
	}

}
