package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.jaxb.Parameter;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;

public class ActuatorRampMeter extends Actuator {
	
	private double max_rate_in_veh;
	private double min_rate_in_veh;
	
	public void setMeteringRateInVeh(Double rate){
		this.command = rate;
	}

	public void setMeteringRateInVPH(Double rate){
		this.command = rate;
	}
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public ActuatorRampMeter(Controller C,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
		
		super(C);
		
		max_rate_in_veh = Double.POSITIVE_INFINITY;
		min_rate_in_veh = Double.NEGATIVE_INFINITY;
		
		if(jaxbA.getParameters()!=null){
			Scenario myScenario = myController.getMyScenario();
			double dt = myScenario.getSimdtinseconds()/3600d;
			Link myLink = myScenario.getLinkWithId(getScenarioElement().getId());
			if(myLink!=null){
				double lanes = myLink.get_Lanes();
				for(Parameter p : jaxbA.getParameters().getParameter()){
					if(p.getName().compareTo("max_rate_in_vphpl")==0)
						max_rate_in_veh = Double.parseDouble(p.getValue())*dt*lanes;
					if(p.getName().compareTo("max_rate_in_vphpl")==0)
						min_rate_in_veh = Double.parseDouble(p.getValue())*dt*lanes;
				}
			}	
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		return;
	}

	@Override
	protected void validate() {
	}

	@Override
	protected void reset() throws BeatsException {
		return;
	}

	@Override
	public void deploy() {
		this.implementor.deploy_metering_rate((Double) command);
	}

}
