package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.jaxb.ScenarioElement;

import java.util.List;

public class BeatsActuatorImplementation implements InterfaceActuator {

	private Link myLink;

	public BeatsActuatorImplementation(edu.berkeley.path.beats.jaxb.Actuator parent,Object context){
        ScenarioElement se = parent.getScenarioElement();
        if(se.getType().compareTo("link")==0)
            myLink = ((Scenario) context).getLinkWithId(se.getId());
	}
	
	public Link getLink(){
		return myLink;
	}
	
	@Override
	public void deploy_metering_rate_in_vph(Double metering_rate_in_vph) {
		myLink.set_external_max_flow_in_vph(metering_rate_in_vph);
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
