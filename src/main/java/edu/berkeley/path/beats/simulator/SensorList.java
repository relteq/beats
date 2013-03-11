/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;

final class SensorList extends edu.berkeley.path.beats.jaxb.SensorList  {

	protected Scenario myScenario;
	protected ArrayList<Sensor> sensors = new ArrayList<Sensor>();
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {

		this.myScenario = myScenario;
		
		// replace jaxb.Sensor with simulator.Sensor
		if(myScenario.getSensorList()!=null){
			for(edu.berkeley.path.beats.jaxb.Sensor sensorjaxb : myScenario.getSensorList().getSensor()) {
				
				// assign type
				Sensor.Type myType;
		    	try {
					myType = Sensor.Type.valueOf(sensorjaxb.getType());
				} catch (IllegalArgumentException e) {
					continue;
				}
				
				// generate sensor
				if(myType!=null){
					Sensor S = ObjectFactory.createSensorFromJaxb(myScenario,sensorjaxb,myType);
					if(S!=null)
						sensors.add(S);
				}		    	
			}
		}
	
	}

	protected void validate() {
		for(Sensor sensor : sensors)
			sensor.validate();
	}
	
	protected void reset() throws BeatsException {
		for(Sensor sensor : sensors)
			sensor.reset();
	}

	protected void update() throws BeatsException {

        // NOTE: ensembles have not been implemented for sensors. They do not apply
        // to the loop sensor, but would make a difference for floating sensors.
		for(Sensor sensor : sensors)
			sensor.update();
	}

}
