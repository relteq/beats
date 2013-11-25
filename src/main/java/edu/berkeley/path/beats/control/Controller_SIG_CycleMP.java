package edu.berkeley.path.beats.control;


import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import edu.berkeley.path.beats.jaxb.Phase;
import edu.berkeley.path.beats.simulator.Actuator;
import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Sensor;
import edu.berkeley.path.beats.simulator.Signal;
import edu.berkeley.path.beats.simulator.SignalPhase;

public class Controller_SIG_CycleMP extends Controller_SIG {

	private double cycle_length;
	
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
		
		//pull signal, node info
//		Signal mySignal = (Signal) getActuatorByUsage("signal").get(0);
		Node myNode = null;
		
		//get link index information from node
		Link [] inputLinks = myNode.getInput_link();
		Link [] outputLinks = myNode.getOutput_link();
		int nInputs = inputLinks.length;
        int nOutputs = outputLinks.length;
		
        //get sat flow information from links
		float[] satFlows = new float[nInputs];
		for(int i=0;i<nInputs;i++){satFlows[i] = (int) inputLinks[i].getCapacityInVeh(0);}
		//get counts from links (no sensor needed)
		int[] inputCounts = new int[nInputs];
        int[] outputCounts = new int[nOutputs];
		for(int i=0;i<nInputs;i++){inputCounts[i]=(int) Math.round(inputLinks[i].getTotalDensityInVeh(0));}
		for(int j=0;j<nOutputs;j++){inputCounts[j]=(int) Math.round(outputLinks[j].getTotalDensityInVeh(0));}

		// get splits from node
        double[][] splits = new double[nInputs][nOutputs];        
		for(int i=0;i<nInputs;i++){
			for(int j=0;j<nOutputs;j++){
				splits[i][j]=myNode.getSplitRatio(i, j, 0);
			}
		}
		
        // construct binary "control matrix" of size nStagesxnInputs
        List<Phase> sigPhases = mySignal.getPhase(); //i think this is not right! 
        
        int nStages = sigPhases.size();
        int[][] controlMat = new int [nStages][nInputs];
        
<<<<<<< HEAD
  
        mySignal.
        for(Stage aStage : stages){
        	SignalPhase aPhase = mySignal.getPhaseByNEMA(aStage.nema1);
        	Link [] targetlinks = aPhase.getTargetlinks();
        	int phaseLink = (int) targetlinks[0].getId();	
        }
        int nStages = controlMat.length;
=======
        
//        mySignal.
//        for(Stage aStage : stages){
//        	SignalPhase aPhase = mySignal.getPhaseByNEMA(aStage.nema1);
//        	Link [] targetlinks = aPhase.getTargetlinks();
//        	int phaseLink = (int) targetlinks[0].getId();
//        }
//        int nStages = controlMat.length;
>>>>>>> calpath/maxp

        
        
        //these are internal, don't need to change
        double[] weights;
        double[] pressures;
        int mpStage;
        
        //calculate SIMPLE weights
        //later this will be changed, to calculate max/min/average/etc weights, and to include minimum green time constraints. 
        weights = new double [nInputs];
        for (int i=0; i<nInputs; i++){
        	weights[i]=inputCounts[i];
        	for (int e=0; e<nOutputs; e++){
        		weights[i]-=splits[i][e]*outputCounts[e];
        	}
        	//System.out.println("weights for link "+i+": "+weights[i]);
        }
        	
        //calculate pressure for all stages
//        pressures = new float [nStages];
//        for (int s=0; s<nStages;s++){
//        	pressures[s] = 0;
//        	for (int i=0; i<nInputs; i++){
//        		pressures[s] += controlMat[s][i]*weights[i]*satFlows[i];
//        	}
//        	//System.out.println("pressures for stage "+s+": "+pressures[s]);
//        }
//
//        //determine max pressure stage
//        mpStage = 0;
//        for (int s=1;s<nStages;s++){
//        	if (pressures[s]>pressures[mpStage]){mpStage = s;}
//        }
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
