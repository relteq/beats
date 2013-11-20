package edu.berkeley.path.beats.control;


import edu.berkeley.path.beats.jaxb.TargetActuator;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Signal;

public class Controller_SIG extends Controller {

    protected Signal mySignal;

    @Override
    protected void populate(Object jaxbobject) {



        Actuator act = myScenario.getActuatorWithId(jaxbController.getTargetActuators().getTargetActuator().get(0).getId());


        mySignal = act.getSignal();

    }

   @Override
    protected void validate() {
        super.validate();
    }

    @Override
    protected void reset() {
        super.reset();
    }

    @Override
    protected void update() throws BeatsException {
        super.update();
    }

}
