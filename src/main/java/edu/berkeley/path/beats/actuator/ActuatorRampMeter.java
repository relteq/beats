package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.jaxb.Parameter;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;

public class ActuatorRampMeter extends Actuator {
	
	private Link myLink;
	private double max_rate_in_veh;
	private double min_rate_in_veh;
	
	public void setMeteringRateInVeh(Double rate){
		this.command = rate;
System.out.println(this.getLink().getMyNetwork().getMyScenario().getCurrentTimeInSeconds() + "\tActuator: " + command.toString());
		


	}

	public void setMeteringRateInVPH(Double rate){
		this.command = rate;
	}
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public ActuatorRampMeter(){}
		
	public ActuatorRampMeter(Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
		super(myScenario,jaxbA);
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		
		max_rate_in_veh = Double.POSITIVE_INFINITY;
		min_rate_in_veh = 0d;
		myLink = myScenario.getLinkWithId(jaxbA.getScenarioElement().getId());

		if(myLink!=null && jaxbA.getParameters()!=null){
			double dt = myScenario.getSimdtinseconds()/3600d;
			double lanes = myLink.get_Lanes();
			for(Parameter p : jaxbA.getParameters().getParameter()){
				if(p.getName().compareTo("max_rate_in_vphpl")==0)
					max_rate_in_veh = Double.parseDouble(p.getValue())*dt*lanes;
				if(p.getName().compareTo("max_rate_in_vphpl")==0)
					min_rate_in_veh = Double.parseDouble(p.getValue())*dt*lanes;
			}	
		}
	}

	@Override
	protected void validate() {
		if(myLink==null)
			BeatsErrorLog.addError("Bad link id in ramp metering actuator id="+getId());
		if(max_rate_in_veh<0)
			BeatsErrorLog.addError("Negative max rate in ramp metering actuator id="+getId());

		if(min_rate_in_veh<0)
			BeatsErrorLog.addError("Negative min rate in ramp metering actuator id="+getId());
		
		if(max_rate_in_veh<min_rate_in_veh)
			BeatsErrorLog.addError("max rate less than min rate in actuator id="+getId());
	}

	@Override
	public void deploy() {
		this.implementor.deploy_metering_rate((Double) command);
	}
	
	public Link getLink(){
		return myLink;
	}

}
