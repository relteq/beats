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

package edu.berkeley.path.beats.control;

import java.util.ArrayList;

import edu.berkeley.path.beats.actuator.ActuatorRampMeter;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Sensor;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;

public class Controller_IRM_Alinea extends Controller {

	// sensors
	private Sensor mainline_sensor;
	private Link mainline_link;
	
	// actuator
	private ActuatorRampMeter ramp_meter;
	private Link onramp_link;
	
	// parameters
	private double gain_normalized;			// [-]
	private boolean target_density_given; 	// true if the user specifies the target density in the configuration file.
											// In this case the this value is used and kept constant
											// Otherwise it is assigned the critical density, which may change with fd profile.  
	private double target_vehicles;			// [veh/meter/lane]
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_IRM_Alinea(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c) {
		super(myScenario,c,Algorithm.IRM_ALINEA);
	}

//	public Controller_IRM_Alinea(Scenario myScenario,Link onramplink,Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,double gain_in_mps){
//
//		this.myScenario = myScenario;
//		this.onramplink 	= onramplink;
//		this.mainlinelink 	= mainlinelink;
//		this.mainlinesensor = mainlinesensor;
//		this.queuesensor 	= queuesensor;
//		
//		hasmainlinelink   = mainlinelink!=null;
//		hasmainlinesensor = mainlinesensor!=null;
//		hasqueuesensor    = queuesensor!=null;
//		
//		// abort unless there is either one mainline link or one mainline sensor
//		if(mainlinelink==null && mainlinesensor==null)
//			return;
//		if(mainlinelink!=null  && mainlinesensor!=null)
//			return;
//		
//		usesensor = mainlinesensor!=null;
//		
//		// need the sensor's link for target density
//		if(usesensor)
//			mainlinelink = mainlinesensor.getMyLink();
//		
//		gain_normalized = gain_in_mps * myScenario.getSimDtInSeconds() / mainlinelink.getLengthInMeters();
//		
//	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;
		
		// assign mainline sensor and link
		ArrayList<Sensor> mainlinesensor_list = getSensorByUsage("mainline");
		mainline_sensor = mainlinesensor_list.size()==1 ? mainlinesensor_list.get(0) : null;
		mainline_link = mainline_sensor!=null ? mainline_sensor.getMyLink() : null;

		// assign actuator
		ramp_meter = actuators.size()==1 ? (ActuatorRampMeter) actuators.get(0) : null;
		
		// get reference to onramp link
		onramp_link = ramp_meter!=null ? ramp_meter.getLink() : null;
		
		// read parameters
		target_density_given = false;
		double gain_in_mps = 50.0 * 1609.344 / 3600.0; // [meters/second]
		if(jaxbc.getParameters()!=null)
			for(edu.berkeley.path.beats.jaxb.Parameter p : jaxbc.getParameters().getParameter()){
				if(p.getName().equals("gain")){
					try {
						gain_in_mps = Double.parseDouble(p.getValue());
					} catch (NumberFormatException e) {
						gain_in_mps = Double.NaN;
					}
				}
				if(p.getName().equals("targetdensity")){
					if(mainline_link!=null){
						target_vehicles = Double.parseDouble(p.getValue());   // [in veh/meter/lane]
						target_vehicles *= mainline_link.get_Lanes() * mainline_link.getLengthInMeters();		// now in [veh]
						target_density_given = true;
					}
				}
			}	
		
		// normalize the gain
		if(mainline_link!=null)
			gain_normalized = gain_in_mps * getMyScenario().getSimdtinseconds() / mainline_link.getLengthInMeters();
	}
	
	@Override
	protected void validate() {
		
		super.validate();

		// null checks
		if(mainline_sensor==null)
			BeatsErrorLog.addError("Bad mainline sensor.");
		if(ramp_meter==null)
			BeatsErrorLog.addError("Bad actuator.");
		if(onramp_link==null)
			BeatsErrorLog.addError("Actuator link id is incorrect.");
	}

	@Override
	protected void update() {
		
		// get mainline density either from sensor or from link
		double mainlinevehicles = mainline_sensor.getTotalDensityInVeh(0);		// [veh]
				
		// need to read target density each time if not given
		if(!target_density_given)
			target_vehicles = mainline_link.getDensityCriticalInVeh(0);
		
		// metering rate
		ramp_meter.setMeteringRateInVeh(
				Math.max(onramp_link.getTotalOutflowInVeh(0) + gain_normalized*(target_vehicles-mainlinevehicles),0)
				);
				
	}

	/////////////////////////////////////////////////////////////////////
	// register / deregister
	/////////////////////////////////////////////////////////////////////

//	@Override
//	protected boolean register() {
//		return registerFlowController(onramplink,0);
//	}
//	
//	protected boolean deregister() {
//		return deregisterFlowController(onramplink);
//	}

}
