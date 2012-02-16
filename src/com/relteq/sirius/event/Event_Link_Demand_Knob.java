package com.relteq.sirius.event;

import com.relteq.sirius.jaxb.DemandProfileSet;
import com.relteq.sirius.jaxb.Event;
import com.relteq.sirius.simulator.DemandProfile;
import com.relteq.sirius.simulator._DemandProfile;
import com.relteq.sirius.simulator._Event;
import com.relteq.sirius.simulator._Scenario;
import com.relteq.sirius.simulator._ScenarioElement;

/** DESCRIPTION OF THE CLASS
* @author AUTHOR NAME
* @version VERSION NUMBER
*/
public class Event_Link_Demand_Knob extends _Event {

	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////
	
	public Event_Link_Demand_Knob(_Scenario myScenario, Event jaxbE) {
		super.populateFromJaxb(myScenario,jaxbE, _Event.Type.link_demand_knob);
	}
	
	public Event_Link_Demand_Knob(_Scenario myScenario) {
		// TODO Auto-generated constructor stub
	}

	/////////////////////////////////////////////////////////////////////
	// InterfaceEvent
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean validate() {

		if(!super.validate())
			return false;
		
		// check each target is valid
		for(_ScenarioElement s : targets){
			if(s.getMyType()!=_ScenarioElement.Type.link){
				System.out.println("wrong target type.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void activate() {
		for(_ScenarioElement s : targets){
	    	if(myScenario.getDemandProfileSet()!=null){
	        	for(DemandProfileSet profile : myScenario.getDemandProfileSet().getDemandProfile()){
	        		if(profile.getLinkIdOrigin().equals(s.id)){
	        			double newknob;
	        			if(isResetToNominal())
	        				newknob = profile.getKnob().doubleValue();
	        			else
	        				newknob = getKnob().getValue().doubleValue();
	        			((_DemandProfile) profile).set_knob( newknob );
	        			break;
	        		}
	        	}
	    	}
		}		
	}
}
