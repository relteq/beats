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

package edu.berkeley.path.beats.simulator;

import java.util.ArrayList;

public class _test {

	public static void main(String[] args) {
		
		String configfilename; 
		String outputfileprefix = "out";;
		double timestart = 0d;; 			// [sec] after midnight
		double timeend = 3600d;			// [sec] after midnight
		double outdt = 300d;;			// [sec]
		
		try {

			//configfilename = "C:\\Gabriel\\traffic-estimation\\particle_filtering\\scenario\\scenario_2.xml";
			configfilename = "C:\\Users\\gomes\\workspace\\sirius\\data\\config\\_smalltest_multipletypes.xml";
			
			// load the scenario
			Scenario scenario = ObjectFactory.createAndLoadScenario(configfilename);
									
			// check if it loaded
			if(scenario==null)
				return;

			// make list of all links and origin links 
			ArrayList<String> link_origin = new ArrayList<String> ();
			ArrayList<String> link_ids = new ArrayList<String> ();
			for(edu.berkeley.path.beats.jaxb.Link jlink : scenario.getNetworkList().getNetwork().get(0).getLinkList().getLink()){
				Link link = (Link) jlink;
				link_ids.add(jlink.getId());
				if(link.issource)
					link_origin.add(link.getId());
			}	
			
			int numLinks = link_ids.size();
			int numVehTypes = scenario.getNumVehicleTypes();
			
			// create demand profiles
			int numTimes = 10;
			ArrayList<DemandProfile> demands = new ArrayList<DemandProfile>();
			for(edu.berkeley.path.beats.jaxb.Link jlink : scenario.getNetworkList().getNetwork().get(0).getLinkList().getLink()){
				Link link = (Link) jlink;
				if(link.issource){
					Double [][] demand = new Double [numTimes][numVehTypes];  // number of time intervals X number of vehicle types
					DemandProfile d = ObjectFactory.createDemandProfile(scenario,link.getId(),demand,0f,30f,1f,0f,0f);
					demands.add(d);
					scenario.addDemandProfile(d);
				}
			}	
			
			// create my initial condition object
			String [] a_link_ids = link_ids.toArray(new String[link_ids.size()]);
			Double [][] init_density = new Double[numLinks][numVehTypes];
			int i,j;
			for(i=0;i<init_density.length;i++)
				for(j=0;j<init_density[0].length;j++)
					init_density[i][j]=0d;
			
			InitialDensitySet myIC = ObjectFactory.createInitialDensitySet(scenario,0, a_link_ids, scenario.getVehicleTypeNames(), init_density);
			
			// attach it to the scenario
			scenario.setInitialDensitySet(myIC);
			
			// zero initial condition
			scenario.run(timestart,timeend,outdt,outputfileprefix);


			// 10 initial condition
			init_density = myIC.getInitial_density();
			for(i=0;i<numLinks;i++)
				for(j=0;j<numVehTypes;j++)
					init_density[i][j] = 10d;
			
			scenario.run(timestart,timeend,outdt,outputfileprefix);
			
			// still need modifiers for fd parameters and sink capacities
			
			// set demands to 10 vph
			for(DemandProfile d : demands)
				d.demand_nominal.set(0, 0, 10 * scenario.getSimDtInSeconds());
			scenario.run(timestart,timeend,outdt,outputfileprefix);

		} catch (SiriusException e) {
			if(SiriusErrorLog.haserror())
				SiriusErrorLog.print();
			else
				e.printStackTrace();
		}	
		
		System.out.println("done");
	}
		
}
