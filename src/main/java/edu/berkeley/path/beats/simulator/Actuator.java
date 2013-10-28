package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.jaxb.ActuatorType;
import edu.berkeley.path.beats.jaxb.DisplayPosition;
import edu.berkeley.path.beats.jaxb.Parameters;
import edu.berkeley.path.beats.jaxb.ScenarioElement;
import edu.berkeley.path.beats.jaxb.Table;



//public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {
public class Actuator {

	protected Scenario myScenario;
	protected edu.berkeley.path.beats.jaxb.Actuator jaxbA;
	protected InterfaceActuator implementor;
	protected Object command;
	
	public static enum Type	{ ramp_meter,
							  signalized_intersection,
							  vsl,
							  cms };
	

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Actuator (){
	}
	
	public Actuator (Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA){
		this.myScenario = myScenario;
		this.jaxbA = jaxbA;
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject) {
		return;
	}

	protected void validate() {
	}

	protected void reset() throws BeatsException {
		return;
	}
	
	protected void deploy(){};	

	/////////////////////////////////////////////////////////////////////
	// jaxb interface
	/////////////////////////////////////////////////////////////////////

    public ScenarioElement getScenarioElement() {
        return jaxbA.getScenarioElement();
    }

    public Parameters getParameters() {
        return jaxbA.getParameters();
    }

    public Table getTable() {
        return jaxbA.getTable();
    }

    public ActuatorType getActuatorType() {
        return jaxbA.getActuatorType();
    }

    public long getId() {
        return jaxbA.getId();
    }
	
}
