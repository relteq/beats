package edu.berkeley.path.beats.simulator;

public class Actuator {

	protected Scenario myScenario;
	protected edu.berkeley.path.beats.jaxb.Actuator jaxbA;
	protected ActuatorImplementation implementor;
//	protected Object command;
	
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
		if(jaxbA.getScenarioElement().getType().compareToIgnoreCase("link")==0){
			Link myLink = myScenario.getLinkWithId(jaxbA.getScenarioElement().getId());
			implementor = new ActuatorImplementation(myLink);
		}
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject) {
		return;
	}

	protected void validate() {
		if(implementor.getLink()==null)
			BeatsErrorLog.addError("Bad link reference in actuator id="+getId());
	}

	protected void reset() throws BeatsException {
		return;
	}
	
	protected void deploy(){};	

    public long getId() {
        return jaxbA.getId();
    }

//    public String getScenarioElementType() {
//        return jaxbA.getScenarioElement().getType();
//    }
//
//    public long getScenarioElementId() {
//        return jaxbA.getScenarioElement().getId();
//    }

    public Signal getSignal(){
        ScenarioElement se = (ScenarioElement) jaxbA.getScenarioElement();
        if(se.getMyType().compareTo(ScenarioElement.Type.signal)==0)
            return (Signal) se.getReference();
        else
            return null;
    }

}
