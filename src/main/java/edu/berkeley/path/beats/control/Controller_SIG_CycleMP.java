package edu.berkeley.path.beats.control;

import java.lang.Math;

import edu.berkeley.path.beats.simulator.BeatsException;
import edu.berkeley.path.beats.simulator.Controller;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Scenario;

public class Controller_SIG_CycleMP extends Controller_SIG {

    private Node myNode;
    public double [] cycle_splits;          // [sums to 1] in the order of stages.
	    

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
        myNode = myScenario.getNodeWithId(mySignal.getNodeId());
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

		//get link index information from node
		Link [] inputLinks = myNode.getInput_link();
		Link [] outputLinks = myNode.getOutput_link();
		int nInputs = inputLinks.length;
        int nOutputs = outputLinks.length;
		
        //construct control matrices
        int nStages = stages.length;
        int[][] controlMat = new int [nStages][nInputs]; // initializes to filled with 0
        for(int s=0; s<nStages; s++){
        	for (Link a: stages[s].phaseA.getTargetlinks()){
        		for (int i=0;i<nInputs;i++){
        			if (inputLinks[i].getId()==a.getId()){
        				controlMat[s][i]=1;
        				break;
        			}
        		}
        	}
        	for (Link b: stages[s].phaseB.getTargetlinks()){
        		for (int i=0;i<nInputs;i++){
        			if (inputLinks[i].getId()==b.getId()){
        				controlMat[s][i]=1;
        				break;
        			}
        		}
        	}
        }
        
        //get sat flow information from links
		float[] satFlows = new float[nInputs];
		for(int i=0;i<nInputs;i++){
            satFlows[i] = (int) inputLinks[i].getCapacityInVeh(0);
        }

		//get counts from links (no sensor needed)
		int[] inputCounts = new int[nInputs];
        int[] outputCounts = new int[nOutputs];
		for(int i=0;i<nInputs;i++){
            inputCounts[i]=(int) Math.round(inputLinks[i].getTotalDensityInVeh(0));
        }
		for(int j=0;j<nOutputs;j++){
            inputCounts[j]=(int) Math.round(outputLinks[j].getTotalDensityInVeh(0));
        }

		// get splits from node
        double[][] splits = new double[nInputs][nOutputs];        
		for(int i=0;i<nInputs;i++){
			for(int j=0;j<nOutputs;j++){
				splits[i][j]=myNode.getSplitRatio(i, j, 0);
			}
		}
		
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
        }
        	
        //calculate pressure for all stages
        pressures = new double [nStages];
        for (int s=0; s<nStages;s++){
        	pressures[s] = 0;
        	for (int i=0; i<nInputs; i++){
        		pressures[s] += controlMat[s][i]*weights[i]*satFlows[i];
        	}
        }

        //determine max pressure stage
        mpStage = 0;
        for (int s=1;s<nStages;s++){
        	if (pressures[s]>pressures[mpStage]){mpStage = s;}
        }
		
	}

}
