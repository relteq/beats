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

import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.InterfaceComponent;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Sensor;
import edu.berkeley.path.beats.simulator.SiriusErrorLog;

public class Controller_IRM_Alinea extends Controller {

	private Link onramplink = null;
	private Link mainlinelink = null;
	private Sensor mainlinesensor = null;
	private Sensor queuesensor = null;
	private double gain_normalized;			// [-]
	
	private boolean targetdensity_given; 	// true if the user specifies the target density in the configuration file.
											// In this case the this value is used and kept constant
											// Otherwise it is assigned the critical density, which may change with fd profile.  
	
	private double targetvehicles;			// [veh/meter/lane]
	private boolean usesensor;
	
	boolean hasmainlinelink;		// true if config file contains entry for mainlinelink
	boolean hasmainlinesensor; 		// true if config file contains entry for mainlinesensor
	boolean hasqueuesensor; 		// true if config file contains entry for queuesensor

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_IRM_Alinea() {
	}

	public Controller_IRM_Alinea(Scenario myScenario,Link onramplink,Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,double gain_in_mps){

		this.myScenario = myScenario;
		this.onramplink 	= onramplink;
		this.mainlinelink 	= mainlinelink;
		this.mainlinesensor = mainlinesensor;
		this.queuesensor 	= queuesensor;
		
		hasmainlinelink   = mainlinelink!=null;
		hasmainlinesensor = mainlinesensor!=null;
		hasqueuesensor    = queuesensor!=null;
		
		// abort unless there is either one mainline link or one mainline sensor
		if(mainlinelink==null && mainlinesensor==null)
			return;
		if(mainlinelink!=null  && mainlinesensor!=null)
			return;
		
		usesensor = mainlinesensor!=null;
		
		// need the sensor's link for target density
		if(usesensor)
			mainlinelink = mainlinesensor.getMyLink();
		
		gain_normalized = gain_in_mps * myScenario.getSimDtInSeconds() / mainlinelink.getLengthInMeters();
		
	}

	/////////////////////////////////////////////////////////////////////
	// InterfaceController
	/////////////////////////////////////////////////////////////////////

	/** Implementation of {@link InterfaceComponent#populate}.
	 * @param jaxbobject Object
	 */
	@Override
	public void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;
		
		if(jaxbc.getTargetElements()==null)
			return;
		if(jaxbc.getTargetElements().getScenarioElement()==null)
			return;
		if(jaxbc.getFeedbackElements()==null)
			return;
		if(jaxbc.getFeedbackElements().getScenarioElement()==null)
			return;
		
		hasmainlinelink = false;
		hasmainlinesensor = false;
		hasqueuesensor = false;
		
		// There should be only one target element, and it is the onramp
		if(jaxbc.getTargetElements().getScenarioElement().size()==1){
			edu.berkeley.path.beats.jaxb.ScenarioElement s = jaxbc.getTargetElements().getScenarioElement().get(0);
			onramplink = myScenario.getLinkWithId(s.getId());	
		}
		
		// Feedback elements can be "mainlinesensor","mainlinelink", and "queuesensor"
		if(!jaxbc.getFeedbackElements().getScenarioElement().isEmpty()){
			
			for(edu.berkeley.path.beats.jaxb.ScenarioElement s:jaxbc.getFeedbackElements().getScenarioElement()){
				
				if(s.getUsage()==null)
					return;
				
				if( s.getUsage().equalsIgnoreCase("mainlinesensor") &&
				    s.getType().equalsIgnoreCase("sensor") && mainlinesensor==null){
					mainlinesensor=myScenario.getSensorWithId(s.getId());
					hasmainlinesensor = true;
				}

				if( s.getUsage().equalsIgnoreCase("mainlinelink") &&
					s.getType().equalsIgnoreCase("link") && mainlinelink==null){
					mainlinelink=myScenario.getLinkWithId(s.getId());
					hasmainlinelink = true;
				}

				if( s.getUsage().equalsIgnoreCase("queuesensor") &&
					s.getType().equalsIgnoreCase("sensor")  && queuesensor==null){
					queuesensor=myScenario.getSensorWithId(s.getId());
					hasqueuesensor = true;
				}				
			}
		}
		
//		// abort unless there is either one mainline link or one mainline sensor
//		if(mainlinelink==null && mainlinesensor==null)
//			return;
//		if(mainlinelink!=null  && mainlinesensor!=null)
//			return;
		
		usesensor = mainlinesensor!=null;
		
		// need the sensor's link for target density
		if(usesensor)
			mainlinelink = mainlinesensor.getMyLink();
		
//		if(mainlinelink==null)
//			return;
		
		// read parameters
		double gain_in_mps = 50.0 * 1609.344 / 3600.0; // [meters/second]
		targetdensity_given = false;
		if(jaxbc.getParameters()!=null)
			for(edu.berkeley.path.beats.jaxb.Parameter p : jaxbc.getParameters().getParameter()){
				if(p.getName().equals("gain")){
					try {
						gain_in_mps = Double.parseDouble(p.getValue());
					} catch (NumberFormatException e) {
						gain_in_mps = 0d;
					}
				}
				if(p.getName().equals("targetdensity")){
					if(mainlinelink!=null){
						targetvehicles = Double.parseDouble(p.getValue());   // [in veh/meter/lane]
						targetvehicles *= mainlinelink.get_Lanes() * mainlinelink.getLengthInMeters();		// now in [veh]
						targetdensity_given = true;
					}
				}
			}	
		
		// normalize the gain
		if(mainlinelink!=null)
			gain_normalized = gain_in_mps * myScenario.getSimDtInSeconds() / mainlinelink.getLengthInMeters();
	}
	
	@Override
	public void validate() {
		
		super.validate();

		// must have exactly one target
		if(targets.size()!=1)
			SiriusErrorLog.addError("Numnber of targets for Alinea controller id=" + getId()+ " does not equal one.");

		// bad mainline sensor id
		if(hasmainlinesensor && mainlinesensor==null)
			SiriusErrorLog.addError("Bad mainline sensor id in Alinea controller id=" + getId()+".");

		// bad queue sensor id
		if(hasqueuesensor && queuesensor==null)
			SiriusErrorLog.addError("Bad queue sensor id in Alinea controller id=" + getId()+".");
		
		// both link and sensor feedback
		if(hasmainlinelink && hasmainlinesensor)
			SiriusErrorLog.addError("Both mainline link and mainline sensor are not allowed in Alinea controller id=" + getId()+".");
		
		// sensor is disconnected
		if(usesensor && mainlinesensor.getMyLink()==null)
			SiriusErrorLog.addError("Mainline sensor is not connected to a link in Alinea controller id=" + getId()+ " ");
		
		// no feedback
		if(mainlinelink==null)
			SiriusErrorLog.addError("Invalid mainline link for Alinea controller id=" + getId()+ ".");
		
		// Target link id not found, or number of targets not 1.
		if(onramplink==null)
			SiriusErrorLog.addError("Invalid onramp link for Alinea controller id=" + getId()+ ".");
			
		// negative gain
		if(mainlinelink!=null && gain_normalized<=0f)
			SiriusErrorLog.addError("Non-positiva gain for Alinea controller id=" + getId()+ ".");
		
	}

	@Override
	public void update() {
		
		// get mainline density either from sensor or from link
		double mainlinevehicles;		// [veh]
		if(usesensor)
			mainlinevehicles = mainlinesensor.getTotalDensityInVeh(0);
		else
			mainlinevehicles = mainlinelink.getTotalDensityInVeh(0);
				
		// need to read target density each time if not given
		if(!targetdensity_given)
			targetvehicles = mainlinelink.getDensityCriticalInVeh(0);
		
		// metering rate
		control_maxflow[0] = Math.max(Math.min(onramplink.getTotalOutflowInVeh(0) + gain_normalized*(targetvehicles-mainlinevehicles), 1705),0);
	}

	@Override
	public boolean register() {
		return registerFlowController(onramplink,0);
	}
	
	public boolean deregister() {
		return deregisterFlowController(onramplink);
	}

}
