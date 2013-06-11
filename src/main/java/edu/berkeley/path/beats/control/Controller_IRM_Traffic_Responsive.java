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
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.Table;

public class Controller_IRM_Traffic_Responsive extends Controller {
	
	private Link onramplink = null;
	private Link mainlinelink = null;
	private Sensor mainlinesensor = null;
	private Sensor queuesensor = null;
	private boolean usesensor;
	
	boolean hasmainlinelink;		// true if config file contains entry for mainlinelink
	boolean hasmainlinesensor; 		// true if config file contains entry for mainlinesensor
	boolean hasqueuesensor; 		// true if config file contains entry for queuesensor

	private boolean istablevalid;   // true if a valid table is given

	boolean hasoccthres;
	boolean hasflowthres;
	boolean hasspeedthres;
	
	private double[] trFlowThresh;  // stores flow thresholds corresponding to the traffic responsive controllers.
	private double[] trOccThresh;  // stores occupancy thresholds corresponding to the traffic responsive controllers.
	private double[] trSpeedThresh;  // stores speed thresholds corresponding to the traffic responsive controllers.
	private double[] trMeteringRates_normalized; // normalized metering rates corresponding to the different levels of the traffic responsive controller.
	
	private int trlevelindex; // denotes the current level that is requested by the traffic responsive logic.

	private Table table;
	
	/////////////////////////////////////////////////////////////////////
	// Construction
	/////////////////////////////////////////////////////////////////////

	public Controller_IRM_Traffic_Responsive(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller c,Controller.Type myType) {
		super(myScenario,c,myType);
	}

//	public Controller_IRM_Traffic_Responsive(Scenario myScenario,Link onramplink,Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,Table trtable){
//
//		this.myScenario = myScenario;
//		this.onramplink 	= onramplink;
//		this.mainlinelink 	= mainlinelink;
//		this.mainlinesensor = mainlinesensor;
//		this.queuesensor 	= queuesensor;
//		
//		hasmainlinelink   = mainlinelink!=null;
//		hasmainlinesensor = mainlinesensor!=null;
//		hasqueuesensor    = queuesensor!=null;
//		
//		// abort unless there is either one mainline link or one mainline sensor
//		if(mainlinelink==null && mainlinesensor==null)
//			return;
//		if(mainlinelink!=null  && mainlinesensor!=null)
//			return;
//		
//		usesensor = mainlinesensor!=null;
//		
//		// need the sensor's link for target density
//		if(usesensor)
//			mainlinelink = mainlinesensor.getMyLink();
//		
//		// Traffic responsive table.
//		this.table = trtable;
//		
//		this.extractTable();
//		
//	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset  / update
	/////////////////////////////////////////////////////////////////////

	@Override
	protected void populate(Object jaxbobject) {

		edu.berkeley.path.beats.jaxb.Controller jaxbc = (edu.berkeley.path.beats.jaxb.Controller) jaxbobject;
		
		if(jaxbc.getTargetElements()==null)
			return;
		if(jaxbc.getTargetElements().getScenarioElement()==null)
			return;
		if(jaxbc.getFeedbackElements()==null)
			return;
		if(jaxbc.getFeedbackElements().getScenarioElement()==null)
			return;
		
		hasmainlinelink = false;
		hasmainlinesensor = false;
		hasqueuesensor = false;
		
		// There should be only one target element, and it is the onramp
		if(jaxbc.getTargetElements().getScenarioElement().size()==1){
			edu.berkeley.path.beats.jaxb.ScenarioElement s = jaxbc.getTargetElements().getScenarioElement().get(0);
			onramplink = getMyScenario().getLinkWithId(s.getId());	
		}
		
		// Feedback elements can be "mainlinesensor","mainlinelink", and "queuesensor"
		if(!jaxbc.getFeedbackElements().getScenarioElement().isEmpty()){
			
			for(edu.berkeley.path.beats.jaxb.ScenarioElement s:jaxbc.getFeedbackElements().getScenarioElement()){
				
				if(s.getUsage()==null)
					return;
				
				if( s.getUsage().equalsIgnoreCase("mainlinesensor") &&
				    s.getType().equalsIgnoreCase("sensor") && mainlinesensor==null){
					mainlinesensor=getMyScenario().getSensorWithId(s.getId());
					hasmainlinesensor = true;
				}

				if( s.getUsage().equalsIgnoreCase("mainlinelink") &&
					s.getType().equalsIgnoreCase("link") && mainlinelink==null){
					mainlinelink=getMyScenario().getLinkWithId(s.getId());
					hasmainlinelink = true;
				}

				if( s.getUsage().equalsIgnoreCase("queuesensor") &&
					s.getType().equalsIgnoreCase("sensor")  && queuesensor==null){
					queuesensor=getMyScenario().getSensorWithId(s.getId());
					hasqueuesensor = true;
				}				
			}
		}
		
		// abort unless there is either one mainline link or one mainline sensor
		if(mainlinelink==null && mainlinesensor==null)
			return;
		if(mainlinelink!=null  && mainlinesensor!=null)
			return;
		
		usesensor = mainlinesensor!=null;
		
		// need the sensor's link for target density
		if(usesensor)
			mainlinelink = mainlinesensor.getMyLink();
		
		if(mainlinelink==null)
			return;	

		table = findTable(jaxbc, "tod");
		this.extractTable();
	}
	
	@Override
	protected void validate() {
		
		super.validate();
		
		// must have exactly one target
		if(getTargets().size()!=1)
			BeatsErrorLog.addError("Numnber of targets for traffic responsive controller id=" + getId()+ " does not equal one.");

		// bad mainline sensor id
		if(hasmainlinesensor && mainlinesensor==null)
			BeatsErrorLog.addError("Bad mainline sensor id in traffic responsive controller id=" + getId()+".");
		
		// bad queue sensor id
		if(hasqueuesensor && queuesensor==null)
			BeatsErrorLog.addError("Bad queue sensor id in traffic responsive controller id=" + getId()+".");
		
		// Target link id not found, or number of targets not 1.
		if(onramplink==null)
			BeatsErrorLog.addError("Invalid onramp link for traffic responsive controller id=" + getId()+ ".");

		// both link and sensor feedback
		if(hasmainlinelink && hasmainlinesensor)
			BeatsErrorLog.addError("Both mainline link and mainline sensor are not allowed in traffic responsive controller id=" + getId()+".");

		// sensor is disconnected
		if(usesensor && mainlinesensor.getMyLink()==null)
			BeatsErrorLog.addError("Mainline sensor is not connected to a link in traffic responsive controller id=" + getId()+ " ");

		// no feedback
		if(mainlinelink==null)
			BeatsErrorLog.addError("Invalid mainline link for traffic responsive controller id=" + getId()+ ".");

		// Target link id not found, or number of targets not 1.
		if(onramplink==null)
			BeatsErrorLog.addError("Invalid onramp link for traffic responsive controller id=" + getId()+ ".");
			
		// invalid table
		if(!istablevalid)
			BeatsErrorLog.addError("Controller has an invalid table.");			
	}
	
	@Override
	protected void update() {
		
		double mainlineocc=Double.POSITIVE_INFINITY;
		double mainlinespeed=Double.POSITIVE_INFINITY;
		double mainlineflow=Double.POSITIVE_INFINITY;
		// get mainline occ/spd/flow either from sensor or from link	
		if (hasoccthres)
			if(usesensor){
				mainlineocc = mainlinesensor.getOccupancy(0);			
			}
			else {
				mainlineocc = mainlinelink.getTotalDensityInVeh(0)/mainlinelink.getDensityJamInVeh(0);
			}
		
		if (hasspeedthres)
			if(usesensor){
				mainlinespeed = mainlinesensor.getSpeedInMPS(0);
			}
			else {
				mainlinespeed = mainlinelink.getTotalOutflowInVeh(0) / mainlinelink.getTotalDensityInVPMeter(0) / getMyScenario().getSimdtinseconds();
			}
		
		if (hasflowthres)
			if(usesensor){
				mainlineflow = mainlinesensor.getTotalFlowInVPS(0);
			}
			else {
				mainlineflow = mainlinelink.getTotalOutflowInVeh(0) / getMyScenario().getSimdtinseconds();
			}		
		
		// metering rate adjustments
		while (trlevelindex >0 && (hasoccthres && mainlineocc<=trOccThresh[trlevelindex]) 
				&& (hasspeedthres && mainlinespeed<=trSpeedThresh[trlevelindex])
				&& (hasflowthres && mainlineflow<=trFlowThresh[trlevelindex]))
			trlevelindex--;
		
		while (trlevelindex <trMeteringRates_normalized.length-1 &&
				((hasoccthres && mainlineocc>trOccThresh[trlevelindex+1]) || 
				(hasspeedthres && mainlinespeed>trSpeedThresh[trlevelindex])
				|| (hasflowthres && mainlineflow>trFlowThresh[trlevelindex])))
			trlevelindex++;
		
		setControl_maxflow(0, trMeteringRates_normalized[trlevelindex]);

	}

	/////////////////////////////////////////////////////////////////////
	// register / deregister
	/////////////////////////////////////////////////////////////////////

	@Override
	protected boolean register() {
		return registerFlowController(onramplink,0);
	}
	
	protected boolean deregister() {
		return deregisterFlowController(onramplink);
	}

	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////

	private void extractTable(){
		if (null == table) {
			istablevalid = false;
			return;
		}

		// read parameters from table, and also validate
		
		
		int rateIndx = table.getColumnNo("MeteringRates");
		int occIndx = table.getColumnNo("OccupancyThresholds");
		int spdIndx = table.getColumnNo("SpeedThresholds");
		int flwIndx = table.getColumnNo("FlowThresholds");
		
		hasflowthres=(flwIndx!=-1);
		hasspeedthres=(spdIndx!=-1);
		hasoccthres=(occIndx!=-1);		
		
		istablevalid=table.checkTable() && (rateIndx!=-1) && (hasflowthres || hasoccthres || hasspeedthres);
		
		// need a valid table to parse
		if (!istablevalid) 
			return;
		
		
		// read table, initialize values. 
		if (hasflowthres)
			trFlowThresh=new double[table.getNoRows()];
		
		if (hasoccthres)
			trOccThresh=new double[table.getNoRows()];
		
		if (hasspeedthres)
			trSpeedThresh=new double[table.getNoRows()];
		
		
		trMeteringRates_normalized=new double[table.getNoRows()];			
		trlevelindex = 0;
		// extract data from the table and populate
		for (int i=0;i<table.getNoRows();i++){
			trMeteringRates_normalized[i] = Double.parseDouble(table.getTableElement(i,rateIndx)) * getMyScenario().getSimdtinseconds(); // in veh per sim step
			if (hasflowthres){
				trFlowThresh[i]=Double.parseDouble(table.getTableElement(i,flwIndx));			// flow in veh/sec
			}
			if (hasoccthres){
				trOccThresh[i]=Double.parseDouble(table.getTableElement(i,occIndx));  			// occupancy in %
			}
			if (hasspeedthres){
				trSpeedThresh[i]=Double.parseDouble(table.getTableElement(i,spdIndx)); 			// speed in m/s
			}

			if (i==0 && ((hasflowthres && trFlowThresh[i]<0) || (hasoccthres && trOccThresh[i]<0) ||
					(hasspeedthres && trSpeedThresh[i]<0)))
					istablevalid=false;
			// decreasing metering rates, and increasing thresholds, where applicable.		
			if ((trMeteringRates_normalized[i]<0) || (i>0 && (trMeteringRates_normalized[i]>trMeteringRates_normalized[i-1])) || 
			(i>0 && !((hasflowthres && trFlowThresh[i]>trFlowThresh[i-1]) || (hasoccthres && trOccThresh[i]>trOccThresh[i-1]) ||
					(hasspeedthres && trSpeedThresh[i]>trSpeedThresh[i-1]))))				
				istablevalid=false;					
		}
		
		// occupancy thresholds should be between 0 and 100.
		if (hasoccthres && trOccThresh[0]<=0 && trOccThresh[trOccThresh.length-1]>100)
			istablevalid=false;
	}
	
}
