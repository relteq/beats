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

import java.util.List;

/** Storage for a scenario state trajectory. 
 * <p>
* @author Gabriel Gomes
*/
public final class SiriusStateTrajectory {

	protected Scenario myScenario;
	protected int numNetworks;								// number of networks in the scenario
	protected NewtorkStateTrajectory [] networkState;		// array of states trajectories for networks
	protected int numVehicleTypes; 							// size of 2nd dimension of networkState
	protected int numTime; 									// size of 3rd dimension of networkState

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public SiriusStateTrajectory(Scenario myScenario,double outsteps) {
		if(myScenario==null)
			return;
		if(myScenario.getNetworkList()==null)
			return;
		if(myScenario.getNetworkList().getNetwork()==null)
			return;
		this.myScenario = myScenario;
		
		this.numNetworks = myScenario.getNetworkList().getNetwork().size();
		this.numVehicleTypes = myScenario.getNumVehicleTypes();
		this.numTime = (int) Math.ceil(myScenario.getTotalTimeStepsToSimulate()/outsteps);

		this.networkState = new NewtorkStateTrajectory[numNetworks];
		for(int i=0;i<numNetworks;i++){
			int numLinks = myScenario.getNetworkList().getNetwork().get(i).getLinkList().getLink().size();
			this.networkState[i] = new NewtorkStateTrajectory(numLinks);
		}
	}

	/////////////////////////////////////////////////////////////////////
	// API
	/////////////////////////////////////////////////////////////////////
	
	public Double getDensity(int netindex,int i,int j,int k) {
		if(netindex<0 || netindex>=numNetworks)
			return Double.NaN;
		NewtorkStateTrajectory  N = networkState[netindex];
		if(i<0 || i>=N.getNumLinks() || j<0 || j>=numVehicleTypes || k<0 || k>=numTime)
			return Double.NaN;
		else
			return N.density[i][j][k];
	}

	public Double getFlow(int netindex,int i,int j,int k) {
		if(netindex<0 || netindex>=numNetworks)
			return Double.NaN;
		NewtorkStateTrajectory  N = networkState[netindex];
		if(i<0 || i>=N.getNumLinks() || j<0 || j>=numVehicleTypes || k<0 || k>=numTime)
			return Double.NaN;
		else
			return N.flow[i][j][k];
	}

	protected void recordstate(int timestep,double time,boolean exportflows,int outsteps) {
		
		int i,j;
		double invsteps;
		
		if(timestep==1)
			invsteps = 1f;
		else
			invsteps = 1f/((double)outsteps);
	
		int timeindex = timestep/outsteps;

		for(int netindex=0;netindex<numNetworks;netindex++){
			edu.berkeley.path.beats.jaxb.Network network = myScenario.getNetworkList().getNetwork().get(netindex);
			List<edu.berkeley.path.beats.jaxb.Link> links = network.getLinkList().getLink();
			for(i=0;i<networkState[netindex].getNumLinks();i++){
				Link link = (Link) links.get(i);				
				for(j=0;j<numVehicleTypes;j++){
					networkState[netindex].density[i][j][timeindex] = link.cumulative_density[0][j]*invsteps;
					if(exportflows)
						networkState[netindex].flow[i][j][timeindex-1] = link.cumulative_outflow[0][j]*invsteps;
				}
			}
			netindex++;
		}
	}

	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////
	
	public class NewtorkStateTrajectory{

		protected int numLinks; 		// size of 1st dimension
		protected Double[][][] density; // [veh]
		protected Double[][][] flow; 	// [veh]

		public NewtorkStateTrajectory(int numLinks) {
			this.numLinks = numLinks;
			this.density = new Double[numLinks][numVehicleTypes][numTime+1];
			this.flow = new Double[numLinks][numVehicleTypes][numTime];
		}
		public int getNumLinks() {
			return numLinks;
		}
	}
	
}
