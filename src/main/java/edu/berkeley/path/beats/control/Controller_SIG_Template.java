package edu.berkeley.path.beats.control;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Sensor;

public class Controller_SIG_Template extends Controller {

	// controller specific variables defined here
	
	
	
	
	
	// assign values to your controller-specific variables
	@Override
	protected void populate(Object jaxbobject) {
		super.populate(jaxbobject);
	}

	// validate the controller parameters.
	// use BeatsErrorLog.addError() or BeatsErrorLog.addWarning() to register problems
	@Override
	protected void validate() {
		super.validate();
	}
	
	// called before simulation starts. Set controller state to initial values. 
	@Override
	protected void reset() {
		super.reset();
	}
	
	// main controller update function, called every controller dt.
	// use this.sensors to get information, and this.actuators to apply the control command.
	@Override
	protected void update() throws BeatsException {
		super.update();
		
		
		ArrayList<Sensor> x = getSensorByUsage("queue_2");
		AccumulationSensor bla = (AccumulationSensor) x.get(0);
		ArrayList< time,queue > = bla.getQueueHistory()
				bla.resetQueueHistory();
		
		.
		.
		.
		.
		
		ActuatorSignal act = (ActuatorSignal) = this.actuators.get(0);
		act.setGreenTimes(List<Double> green_times);
		
		
		
	}


}
