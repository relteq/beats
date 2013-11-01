package edu.berkeley.path.beats.control.predictive;

import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.simulator.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/25/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RampMeteringPolicyMaker {
    // dt should be same across all passed in objects (universal simulation dt)
    RampMeteringPolicySet givePolicy(Network net,
                                     FundamentalDiagramSet fd,
                                     DemandSet demand,
                                     SplitRatioSet splitRatios,
                                     InitialDensitySet ics,
                                     RampMeteringControlSet control,
                                     Double dt);
}
