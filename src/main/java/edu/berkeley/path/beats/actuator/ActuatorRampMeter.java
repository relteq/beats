package edu.berkeley.path.beats.actuator;

import edu.berkeley.path.beats.jaxb.Parameter;
import edu.berkeley.path.beats.simulator.*;

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

    public ActuatorRampMeter(Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA,InterfaceActuator act_implementor){

        super(myScenario,jaxbA,act_implementor);

        int num_lanes_in_link = 1;      // WHAT TO DO WITH THIS???

        max_rate_in_vph = Double.POSITIVE_INFINITY;
        min_rate_in_vph = 0d;

        if(getParameters()!=null){
            for(Parameter p : getParameters().getParameter()){
                if(p.getName().compareTo("max_rate_in_vphpl")==0)
                    max_rate_in_vph = Double.parseDouble(p.getValue())*num_lanes_in_link;
                if(p.getName().compareTo("min_rate_in_vphpl")==0)
                    min_rate_in_vph = Double.parseDouble(p.getValue())*num_lanes_in_link;
            }
        }
    }

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {
		
		max_rate_in_vph = Double.POSITIVE_INFINITY;
		min_rate_in_vph = 0d;
		myLink = myScenario.getLinkWithId(getScenarioElement().getId());

		if(myLink!=null && getParameters()!=null){
			double lanes = myLink.get_Lanes();
			for(Parameter p : getParameters().getParameter()){
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
