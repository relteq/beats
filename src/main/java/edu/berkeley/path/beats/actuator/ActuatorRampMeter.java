package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.jaxb.Parameter;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;

public class ActuatorRampMeter extends Actuator {

    private double metering_rate_in_vph;
    private Link myLink;
	private double max_rate_in_vph;
	private double min_rate_in_vph;
	
	public void setMeteringRateInVeh(Double rate){
        metering_rate_in_vph = rate/(myScenario.getSimdtinseconds()/3600d);
        metering_rate_in_vph = Math.max(metering_rate_in_vph,min_rate_in_vph);
        metering_rate_in_vph = Math.min(metering_rate_in_vph,max_rate_in_vph);
	}

	public void setMeteringRateInVPH(Double rate){
        metering_rate_in_vph = rate;
        metering_rate_in_vph = Math.max(metering_rate_in_vph,min_rate_in_vph);
        metering_rate_in_vph = Math.min(metering_rate_in_vph,max_rate_in_vph);
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
		
		max_rate_in_vph = Double.POSITIVE_INFINITY;
		min_rate_in_vph = 0d;
		myLink = myScenario.getLinkWithId(jaxbA.getScenarioElement().getId());

		if(myLink!=null && jaxbA.getParameters()!=null){
			double lanes = myLink.get_Lanes();
			for(Parameter p : jaxbA.getParameters().getParameter()){
				if(p.getName().compareTo("max_rate_in_vphpl")==0)
					max_rate_in_vph = Double.parseDouble(p.getValue())*lanes;
				if(p.getName().compareTo("max_rate_in_vphpl")==0)
					min_rate_in_vph = Double.parseDouble(p.getValue())*lanes;
			}	
		}
	}

	@Override
	protected void validate() {
		if(myLink==null)
			BeatsErrorLog.addError("Bad link id in ramp metering actuator id="+getId());
		if(max_rate_in_vph<0)
			BeatsErrorLog.addError("Negative max rate in ramp metering actuator id="+getId());
		if(min_rate_in_vph<0)
			BeatsErrorLog.addError("Negative min rate in ramp metering actuator id="+getId());
		if(max_rate_in_vph<min_rate_in_vph)
			BeatsErrorLog.addError("max rate less than min rate in actuator id="+getId());
	}

	@Override
	public void deploy() {
		this.implementor.deploy_metering_rate_in_vph(metering_rate_in_vph);
	}
	
	public Link getLink(){
		return myLink;
	}

}
