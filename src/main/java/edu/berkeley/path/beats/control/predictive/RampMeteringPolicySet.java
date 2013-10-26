package edu.berkeley.path.beats.control.predictive;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jdr
 * Date: 10/25/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class RampMeteringPolicySet {
    public List<RampMeteringPolicyProfile> profiles;

    public void print() {
        for (RampMeteringPolicyProfile profile : profiles) {
            profile.print();
        }
    }
}
