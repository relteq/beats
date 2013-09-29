package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.simulator.Link;

public class BeatsActuator implements InterfaceActuator {

	private Link myLink;
	
	@Override
	public void deploy_metering_rate(Double metering_rate) {
		myLink.set_external_max_flow(metering_rate);
	}

	@Override
	public void deploy_green_splits() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deploy_cms_split() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deploy_vsl_speed() {
		// TODO Auto-generated method stub
		
	}

}
