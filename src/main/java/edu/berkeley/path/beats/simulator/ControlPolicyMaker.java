package edu.berkeley.path.beats.simulator;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 3/20/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */

public interface ControlPolicyMaker {
    public Map<Long,Double[]> compute(
            Map<Long, Double> initialDensity,
            Map<Long,Double[]> splitRatios,
            Map<Long,Double[]> rampDemands,
            Scenario scenario
    );
}

