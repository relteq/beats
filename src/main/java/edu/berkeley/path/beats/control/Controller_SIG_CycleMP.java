package edu.berkeley.path.beats.control;


import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Sensor;

public class Controller_SIG_CycleMP extends Controller {

	// controller specific variables defined here

    /////////////////////////////////////////////////////////////////////
    // Construction
    /////////////////////////////////////////////////////////////////////

    public Controller_SIG_CycleMP(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c,Controller.Algorithm myType) {
        super(myScenario,c,myType);
    }

    /////////////////////////////////////////////////////////////////////
    // populate / validate / reset  / update
    /////////////////////////////////////////////////////////////////////

    // assign values to your controller-specific variables
	@Override
	protected void populate(Object jaxbobject) {
		super.populate(jaxbobject);
	}

	// validate the controller parameters.
	// use BeatsErrorLog.addError() or BeatsErrorLog.addWarning() to register problems
	@Override
	protected void validate() {
		super.validate();
	}
	
	// called before simulation starts. Set controller state to initial values. 
	@Override
	protected void reset() {
		super.reset();
	}
	
	// main controller update function, called every controller dt.
	// use this.sensors to get information, and this.actuators to apply the control command.
	@Override
	protected void update() throws BeatsException {
		super.update();




        // replace these with calls to sensor
        int[] inputCounts = {6,7,3} ;
        int[] outputCounts = {6,3,8,7} ;
        float[][] splits = {{.5f,.5f,0f,0f},{.4f,.2f,.3f,.1f},{0f,.5f,.1f,.4f}};
        float[] satFlows = {2,3,3};
        int[][] controlMat = {{0,0,1},{1,1,0},{0,1,0},{1,0,1}};
        
        //these are internal, don't need to change
        int nInputs = inputCounts.length;
        int nOutputs = outputCounts.length;
        int nStages = controlMat.length;
        float[] weights;
        float[] pressures;
        int mpStage;
        
        //calculate SIMPLE weights
        //later this will be changed, to calculate max/min/average/etc weights. 
        weights = new float [nInputs];
        for (int i=0; i<nInputs; i++){
        	weights[i]=inputCounts[i];
        	for (int e=0; e<nOutputs; e++){
        		weights[i]-=splits[i][e]*outputCounts[e];
        	}
        	//System.out.println("weights for link "+i+": "+weights[i]);
        }
        	
        //calculate pressure for all stages
        pressures = new float [nStages];
        for (int s=0; s<nStages;s++){
        	pressures[s] = 0;
        	for (int i=0; i<nInputs; i++){
        		pressures[s] += controlMat[s][i]*weights[i]*satFlows[i];
        	}
        	//System.out.println("pressures for stage "+s+": "+pressures[s]);
        }
        
        //determine max pressure stage
        mpStage = 0;
        for (int s=1;s<nStages;s++){
        	if (pressures[s]>pressures[mpStage]){mpStage = s;}
        }
        //System.out.println("max pressure stage: "+ mpStage);
		
		
//		ArrayList<Sensor> x = getSensorByUsage("queue_2");
//		AccumulationSensor bla = (AccumulationSensor) x.get(0);
//		ArrayList< time,queue > = bla.getQueueHistory()
//				bla.resetQueueHistory();
//		
//		.
//		.
//		.
//		.
//		
//		ActuatorSignal act = (ActuatorSignal) = this.actuators.get(0);
//		act.setGreenTimes(List<Double> green_times);
		
		
		
	}


}
