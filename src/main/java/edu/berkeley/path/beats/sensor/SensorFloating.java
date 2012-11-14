
package com.relteq.sirius.sensor;

import com.relteq.sirius.simulator.Scenario;
import com.relteq.sirius.simulator.Sensor;

public class SensorFloating extends Sensor {

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////
	
	public SensorFloating(){
	}
	
	public SensorFloating(Scenario myScenario,String linkId){
		if(myScenario==null)
			return;
		this.myScenario  = myScenario;
		// this.id = GENERATE AN ID;
	    this.myType = Sensor.Type.moving_point;
	    this.myLink = myScenario.getLinkWithId(linkId);
	}
		
	/////////////////////////////////////////////////////////////////////
	// InterfaceSensor
	/////////////////////////////////////////////////////////////////////	

	@Override
	public void populate(Object jaxbobject) {
	}
	
	@Override
	public void validate() {
	}

	@Override
	public void reset() {
		return;
	}

	@Override
	public void update() {
		return;
	}

}
