package edu.berkeley.path.beats.control.predictive;

import edu.berkeley.path.beats.simulator.Link;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/25/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class RampMeteringPolicyProfile {
    public Link sensorLink;
    public List<Double> rampMeteringPolicy;

    public void print() {
        System.out.println(sensorLink.getLinkName());
        for (Double d : rampMeteringPolicy) {
            System.out.print(d.toString() + ",");
        }
        System.out.println();
    }
}