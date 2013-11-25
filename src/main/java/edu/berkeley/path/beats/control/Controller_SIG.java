package edu.berkeley.path.beats.control;


import edu.berkeley.path.beats.jaxb.Column;
import edu.berkeley.path.beats.jaxb.Row;
import edu.berkeley.path.beats.simulator.*;

import java.util.List;

public class Controller_SIG extends Controller {

    protected Signal mySignal;
    protected double cycle_time;
    protected Stage [] stages;

    /////////////////////////////////////////////////////////////////////
    // Construction
    /////////////////////////////////////////////////////////////////////

    public Controller_SIG(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c,Controller.Algorithm myType) {
        super(myScenario,c,myType);
    }

    @Override
    protected void populate(Object jaxbobject) {

        // read cycle time
        cycle_time = jaxbController.getParameters()!=null ?
                        ((Parameters) jaxbController.getParameters()).readParameter("cycle_time",Double.NaN) :
                        Double.NaN;

        // read target actuator

        // hack: assume actuator id = signal id
        long act_id = jaxbController.getTargetActuators().getTargetActuator().get(0).getId();
        mySignal = myScenario.getSignalWithId(act_id);

        // read stages
        edu.berkeley.path.beats.jaxb.Table myTable = null;
        if(jaxbController.getTable()!=null)
            for (edu.berkeley.path.beats.jaxb.Table t : jaxbController.getTable())
                if (t.getName().compareToIgnoreCase("Intersection Stage Sequence") == 0) {
                    myTable = t;
                    break;
                }

        if(myTable!=null){
            int numStages = myTable.getRow().size();
            stages = new Stage[numStages];
            for(int i=0;i<numStages;i++){
                List<Column> c = myTable.getRow().get(i).getColumn();
                Signal.NEMA movA = Signal.NEMA.valueOf(c.get(0).getContent());
                Signal.NEMA movB = Signal.NEMA.valueOf(c.get(1).getContent());
                stages[i] = new Stage(mySignal,movA,movB);
            }
        }

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
