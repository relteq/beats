package edu.berkeley.path.beats.actuator;


public class ActuatorRampMeter extends AbstractActuator {

	public void setRampMeteringRate(Double rate){
		this.command = rate;
	}

	@Override
	public void delpoy(Object command) {
		this.implmentor.deploy_metering_rate((Double) command);
	}
	
}
