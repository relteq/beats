package edu.berkeley.path.beats.simulator;

import java.util.List;

public class ActuatorImplementation implements InterfaceActuator {

	private Link myLink;
	
	public ActuatorImplementation(Link myLink){
		this.myLink = myLink;
	}
	
	public Link getLink(){
		return myLink;
	}
	
	@Override
	public void deploy_metering_rate_in_vph(Double metering_rate_in_vph) {
		myLink.set_external_max_flow(metering_rate_in_vph);
	}

	@Override
	public void deploy_green_times(List<Double> green_times) {
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
