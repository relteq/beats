package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.control.predictive.RampMeteringControlSet;
import edu.berkeley.path.beats.control.predictive.RampMeteringPolicyMaker;
import edu.berkeley.path.beats.control.predictive.RampMeteringPolicySet;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.*;

import java.util.Map;

public class PolicyMaker_CRM_ACTM_LP implements RampMeteringPolicyMaker {

	public PolicyMaker_CRM_ACTM_LP() {
		super();
	}

    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringControlSet control, Double dt) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
