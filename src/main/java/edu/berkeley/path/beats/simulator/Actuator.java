package edu.berkeley.path.beats.simulator;

public class Actuator extends edu.berkeley.path.beats.jaxb.Actuator {

    public enum Implementation {beats,aimsun};
    protected InterfaceActuator implementor;

	protected Scenario myScenario;
	//protected edu.berkeley.path.beats.jaxb.Actuator jaxbA;

	public static enum Type	{ ramp_meter,
							  signalized_intersection,
							  vsl,
							  cms };
	

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Actuator (){
	}
	
	public Actuator (Scenario myScenario,edu.berkeley.path.beats.jaxb.Actuator jaxbA,InterfaceActuator act_implementor){

        this.myScenario = myScenario;
        //this.jaxbA = jaxbA;
        this.implementor = act_implementor;

        // copy jaxb data
        setId(jaxbA.getId());
        setScenarioElement(jaxbA.getScenarioElement());
        setParameters(jaxbA.getParameters());
        setActuatorType(jaxbA.getActuatorType());
        setTable(jaxbA.getTable());
	}

	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / deploy
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject) {
		return;
	}

	protected void validate() {
//		if(implementor.getLink()==null)
//			BeatsErrorLog.addError("Bad link reference in actuator id="+getId());
	}

	protected void reset() throws BeatsException {
		return;
	}
	
	protected void deploy(){};	

//    public long getId() {
//        return jaxbA.getId();
//    }

    public InterfaceActuator get_implementor(){
        return implementor;
    }
	
}
