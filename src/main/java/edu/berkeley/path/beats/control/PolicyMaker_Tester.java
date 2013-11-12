package edu.berkeley.path.beats.control;

import edu.berkeley.path.beats.control.predictive.RampMeteringControlSet;
import edu.berkeley.path.beats.control.predictive.RampMeteringPolicyMaker;
import edu.berkeley.path.beats.control.predictive.RampMeteringPolicyProfile;
import edu.berkeley.path.beats.control.predictive.RampMeteringPolicySet;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.*;

public class PolicyMaker_Tester implements RampMeteringPolicyMaker {

    @Override
    public RampMeteringPolicySet givePolicy(Network net, FundamentalDiagramSet fd, DemandSet demand, SplitRatioSet splitRatios, InitialDensitySet ics, RampMeteringControlSet control, Double dt) {

        RampMeteringPolicySet policy = new RampMeteringPolicySet();

        double [] sample_data = BeatsFormatter.readCSVstring(demand.getDemandProfile().get(0).getDemand().get(0).getContent(),",");
        double num_data = sample_data.length;

        for(edu.berkeley.path.beats.jaxb.Link jaxbL : net.getListOfLinks()){
            Link L = (Link) jaxbL;
            if(L.isSource()){
                RampMeteringPolicyProfile profile = new RampMeteringPolicyProfile();
                profile.sensorLink = L;
                for(int i=0;i<num_data;i++)
                    profile.rampMeteringPolicy.add(100d);
                policy.profiles.add(profile);
            }
        }
        return policy;
    }
}
