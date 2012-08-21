package com.relteq.sirius.simulator;

import java.util.List;

/** Storage for a scenario state trajectory. 
 * <p>
* @author Gabriel Gomes
*/
public final class SiriusStateTrajectory {

	protected Scenario myScenario;
	protected int numNetworks;								// number of networks in the scenario
	protected NewtorkStateTrajectory [] networkState;		// array of states trajectories for networks
	protected int numDestinationNetworks;					// size of 2nd dimension of networkState
	protected int numVehicleTypes; 							// size of 3rd dimension of networkState
	protected int numTime; 									// size of 4th dimension of networkState

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
		this.numDestinationNetworks = myScenario.numDenstinationNetworks;
		this.numVehicleTypes = myScenario.numVehicleTypes;
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
	
	public double getDensity(int netindex,int link_index,int dn_index,int vt_index,int time_index) {
		if(netindex<0 || netindex>=numNetworks)
			return Double.NaN;
		NewtorkStateTrajectory  N = networkState[netindex];
		if( link_index<0 || link_index>=N.getNumLinks() )
			return Double.NaN;
		if( dn_index<0 || dn_index>=numDestinationNetworks )
			return Double.NaN;
		if( vt_index<0 || vt_index>=numVehicleTypes  )
			return Double.NaN;
		if( time_index<0 || time_index>=numTime )
			return Double.NaN;
		return N.density[link_index][dn_index][vt_index][time_index];
	}

	public double getFlow(int netindex,int link_index,int dn_index,int vt_index,int time_index) {
		if(netindex<0 || netindex>=numNetworks)
			return Double.NaN;
		NewtorkStateTrajectory  N = networkState[netindex];
		if(link_index<0 || link_index>=N.getNumLinks() )
			return Double.NaN;
		if(dn_index<0 || dn_index>=numDestinationNetworks )
			return Double.NaN;
		if(vt_index<0 || vt_index>=numVehicleTypes )
			return Double.NaN;
		if(time_index<0 || time_index>=numTime)
			return Double.NaN;
		return N.flow[link_index][dn_index][vt_index][time_index];
	}

	protected void recordstate(int timestep,double time,boolean exportflows,int outsteps) {
		
		int i,d,k,dn_global_index;
		double invsteps;
		
		if(timestep==1)
			invsteps = 1f;
		else
			invsteps = 1f/((double)outsteps);
	
		int timeindex = timestep/outsteps;

		for(int netindex=0;netindex<numNetworks;netindex++){
			com.relteq.sirius.jaxb.Network network = myScenario.getNetworkList().getNetwork().get(netindex);
			List<com.relteq.sirius.jaxb.Link> links = network.getLinkList().getLink();
			for(i=0;i<networkState[netindex].getNumLinks();i++){
				Link link = (Link) links.get(i);
				for(d=0;d<link.numDNetworks;d++){
					dn_global_index = link.myDNindex.get(d);
					for(k=0;k<numVehicleTypes;k++){
						networkState[netindex].density[i][dn_global_index][k][timeindex] = link.cumulative_density[0][d][k]*invsteps;
						if(exportflows)
							networkState[netindex].flow[i][dn_global_index][k][timeindex-1] = link.cumulative_outflow[0][d][k]*invsteps;
					}
				}
			}
			netindex++;
		}
	}

	/////////////////////////////////////////////////////////////////////
	// internal class
	/////////////////////////////////////////////////////////////////////
	
	public class NewtorkStateTrajectory{

		protected int numLinks; 			// size of 1st dimension
		protected double[][][][] density; 	// unit: [veh] dimension: [link][dn][vt][time]
		protected double[][][][] flow; 		// unit: [veh] dimension: [link][dn][vt][time] 

		public NewtorkStateTrajectory(int numLinks) {
			this.numLinks = numLinks;
			this.density = SiriusMath.zeros(numLinks,numDestinationNetworks,numVehicleTypes,numTime+1);
			this.flow = SiriusMath.zeros(numLinks,numDestinationNetworks,numVehicleTypes,numTime);
		}
		public int getNumLinks() {
			return numLinks;
		}
	}
	
}
