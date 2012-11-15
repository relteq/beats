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
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Sensor;
import edu.berkeley.path.beats.simulator.SiriusErrorLog;
import edu.berkeley.path.beats.simulator.Table;

public class Controller_IRM_Time_of_Day extends Controller {
	
	private Link onramplink = null;	
	private Sensor queuesensor = null;	
	private boolean hasqueuesensor;
	
	private double[] todMeteringRates_normalized;			
	private double[] todActivationTimes;
	private int todActivationIndx;	
	
	
	private boolean istablevalid;
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_IRM_Time_of_Day() {
		return;
	}
	
	public Controller_IRM_Time_of_Day(Scenario myScenario,Link onramplink,Sensor queuesensor,Table todtable){

		this.myScenario = myScenario;
		this.onramplink 	= onramplink;
		this.queuesensor 	= queuesensor;
		
		hasqueuesensor    = queuesensor!=null;		
			
		
		// Time of day table.
		this.table = todtable;
		
		this.extractTable();
	}
	
	/////////////////////////////////////////////////////////////////////
	// InterfaceController
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public void populate(Object jaxbobject) {
		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;
		
		if(jaxbc.getTargetElements()==null)
			return;
		if(jaxbc.getTargetElements().getScenarioElement()==null)
			return;
		if(jaxbc.getFeedbackElements()!=null)
			return;				
		
		hasqueuesensor = false;
		
		// There should be only one target element, and it is the onramp
		if(jaxbc.getTargetElements().getScenarioElement().size()==1){
			edu.berkeley.path.beats.jaxb.ScenarioElement s = jaxbc.getTargetElements().getScenarioElement().get(0);
			onramplink = myScenario.getLinkWithId(s.getId());	
		}
				
		this.extractTable();
		
	    
		
	}

	private void extractTable(){
		
		// read parameters from table, and also validate
		
		int timeIndx = table.getColumnNo("StartTime");
		int rateIndx = table.getColumnNo("MeteringRates");
		
		
		istablevalid=table.checkTable() && (table.getNoColumns()!=2? false:true) && (timeIndx!=-1) && (rateIndx!=-1);
		
		// need a valid table to parse
		if (!istablevalid) 
			return;
		
		
		// read table, initialize values. 
		todMeteringRates_normalized=new double[table.getNoRows()];
		todActivationTimes=new double[table.getNoRows()];		
		todActivationIndx=0;
		
		for (int i=0;i<table.getNoRows();i++){
			todMeteringRates_normalized[i] = Double.parseDouble(table.getTableElement(i,rateIndx)) * myScenario.getSimDtInSeconds(); // in veh per sim step
			todActivationTimes[i]=Double.parseDouble(table.getTableElement(i,timeIndx)); // in sec
			// check that table values are valid.			
			if ((i>0 && todActivationTimes[i]<=todActivationTimes[i-1])||(todMeteringRates_normalized[i]<0))
				istablevalid=false;					
		}			
		
		if (todActivationTimes[0]>this.myStartTime())
			istablevalid=false;
		}
	
	@Override
	public void validate() {

		super.validate();
		
		// must have exactly one target
		if(targets.size()!=1)
			SiriusErrorLog.addError("Numnber of targets for TOD controller id=" + getId()+ " does not equal one.");
		
		// bad queue sensor id
		if(hasqueuesensor && queuesensor==null)
			SiriusErrorLog.addError("Bad queue sensor id in TOD controller id=" + getId()+".");
		
		// Target link id not found, or number of targets not 1.
		if(onramplink==null)
			SiriusErrorLog.addError("Invalid onramp link for TOD controller id=" + getId()+ ".");
			
		// has valid tables	
		if(!istablevalid)
			SiriusErrorLog.addError("Controller has an invalid TOD table.");
	}
	
	@Override
	public void reset() {
		super.reset();
		todActivationIndx=0;
		while (todActivationIndx<todActivationTimes.length-1 && todActivationTimes[todActivationIndx+1] <=myScenario.getTimeStart())
			todActivationIndx++;
		control_maxflow[0]=todMeteringRates_normalized[todActivationIndx];
	}

	@Override
	public void update() {
		while (todActivationIndx<todActivationTimes.length-1 && todActivationTimes[todActivationIndx+1] <=myScenario.getTimeInSeconds())
			control_maxflow[0]=todMeteringRates_normalized[++todActivationIndx];		
	}
	
	@Override
	public boolean register() {
		return registerFlowController(onramplink,0);
	}

	@Override
	public boolean deregister() {
		return deregisterFlowController(onramplink);
	}
	
}
