/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.util.ArrayList;

import com.relteq.sirius.jaxb.DestinationNetwork;

import com.relteq.sirius.calibrator.FDParameters;

/** Link class.
* 
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public final class Link extends com.relteq.sirius.jaxb.Link {

	/** @y.exclude */ 	protected int my_global_index;						// used to write matrix output
	/** @y.exclude */ 	protected Network myNetwork;
	/** @y.exclude */ 	protected Node begin_node;
	/** @y.exclude */ 	protected Node end_node;

	// link type
	protected Link.Type myType;
	/** Type of link. */
	public static enum Type	{freeway,HOV,HOT,onramp,offramp,freeway_connector,street,intersection_approach,heavy_vehicle,electric_toll};
	
	/** @y.exclude */ 	protected double _length;							// [miles]
	/** @y.exclude */ 	protected double _lanes;							// [-]
	/** @y.exclude */ 	protected FundamentalDiagram [] FDfromProfile;		// profile fundamental diagram
	/** @y.exclude */ 	protected FundamentalDiagram FDfromEvent;			// event fundamental diagram
	/** @y.exclude */ 	protected FundamentalDiagramProfile myFDprofile;	// reference to fundamental diagram profile (used to rescale future FDs upon lane change event)
	/** @y.exclude */ 	protected boolean activeFDevent;					// true if an FD event is active on this link,
																			// true  means FD points to FDfromEvent 
																			// false means FD points to FDfromprofile
    
	// destinations used by this link (populated by DestinationNetwork.populate)
	/** @y.exclude */ 	protected int numDNetworks = 0;	// number of destinations that use this link
	/** @y.exclude */ 	protected ArrayList<Integer> myDNindex = new  ArrayList<Integer>();	// map to global destination index
	/** @y.exclude */ 	protected ArrayList<Integer> dn_endNodeMap = new  ArrayList<Integer>();	// map to channel in the end node
	/** @y.exclude */ 	protected ArrayList<Integer> dn_beginNodeMap = new  ArrayList<Integer>();	// map to channel in the begin node
	/** @y.exclude */ 	protected boolean link_used;
	
	
	// flow into the link
	/** @y.exclude */ 	protected double [][][] inflow;    		// [veh]	numEnsemble x numDNetworks x numVehTypes
	/** @y.exclude */ 	protected double [][] sourcedemand;		// [veh] 	numDNetworks x numVehTypes
    
    // demand and actual flow out of the link   
	/** @y.exclude */ 	protected double [][][] outflowDemand;  // [veh] 	numEnsemble x numDNetworks x numVehTypes
	/** @y.exclude */ 	protected double [][][] outflow;    	// [veh]	numEnsemble x numDNetworks x numVehTypes
    
    // contoller
	/** @y.exclude */ 	protected int control_maxflow_index;
	/** @y.exclude */ 	protected int control_maxspeed_index;
	/** @y.exclude */ 	protected Controller myFlowController;
	/** @y.exclude */ 	protected Controller mySpeedController;
   
	/** @y.exclude */ 	private double [][][] density;    	// [veh]	numEnsemble x numDNetworks x numVehTypes
	/** @y.exclude */ 	private double [] totaldensity;    	// [veh]	numEnsemble
	
	/** @y.exclude */ 	protected double [] spaceSupply;    // [veh]	numEnsemble
	/** @y.exclude */ 	protected boolean issource; 		// [boolean]
	/** @y.exclude */ 	protected boolean issink;     		// [boolean]
	/** @y.exclude */ 	protected double [][][] cumulative_density;	// [veh] 	numEnsemble x numDNetworks x numVehTypes
	/** @y.exclude */ 	protected double [][][] cumulative_inflow;	// [veh] 	numEnsemble x numDNetworks x numVehTypes
	/** @y.exclude */ 	protected double [][][] cumulative_outflow;	// [veh] 	numEnsemble x numDNetworks x numVehTypes
	       
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Link(){}
	
	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////

//	/** get the cumulative density indexed by global destination network and 
//	 * vehicle types **/
//	public double[][] getCumulative_density(int ensemble) {
//		int numDestinationNetworks = myNetwork.myScenario.numDenstinationNetworks;
//		int numVehicleTypes = myNetwork.myScenario.numVehicleTypes;
//		double [][] X = SiriusMath.zeros(numDestinationNetworks, numVehicleTypes);
//		int i,j;
//		for(i=0;i<numDNetworks;i++)
//			for(j=0;j<numVehicleTypes;j++)
//				X[myDNindex.get(i)][j] = cumulative_density[ensemble][i][j];
//		return X;
//	}
//
//	/** get the cumulative inflow indexed by global destination network and 
//	 * vehicle types **/
//	public double[][] getCumulative_inflow(int ensemble) {
//		int numDestinationNetworks = myNetwork.myScenario.numDenstinationNetworks;
//		int numVehicleTypes = myNetwork.myScenario.numVehicleTypes;
//		double [][] X = SiriusMath.zeros(numDestinationNetworks, numVehicleTypes);
//		int i,j;
//		for(i=0;i<numDNetworks;i++)
//			for(j=0;j<numVehicleTypes;j++)
//				X[myDNindex.get(i)][j] = cumulative_inflow[ensemble][i][j];
//		return X;
//	}
//
//	/** get the cumulative outflow indexed by global destination network and 
//	 * vehicle types **/
//	public double[][] getCumulative_outflow(int ensemble) {
//		int numDestinationNetworks = myNetwork.myScenario.numDenstinationNetworks;
//		int numVehicleTypes = myNetwork.myScenario.numVehicleTypes;
//		double [][] X = SiriusMath.zeros(numDestinationNetworks, numVehicleTypes);
//		int i,j;
//		for(i=0;i<numDNetworks;i++)
//			for(j=0;j<numVehicleTypes;j++)
//				X[myDNindex.get(i)][j] = cumulative_outflow[ensemble][i][j];
//		return X;
//	}
	
	/** @y.exclude */
	protected void reset_cumulative(){
		int n1 = myNetwork.myScenario.numEnsemble;
		int n2 = numDNetworks;
		int n3 = myNetwork.myScenario.getNumVehicleTypes();
    	cumulative_density = SiriusMath.zeros(n1,n2,n3);
    	cumulative_inflow  = SiriusMath.zeros(n1,n2,n3);
    	cumulative_outflow = SiriusMath.zeros(n1,n2,n3);
	}

	/** @y.exclude */
	protected boolean registerFlowController(Controller c,int index){
		if(myFlowController!=null)
			return false;
		else{
			myFlowController = c;
			control_maxflow_index = index;
			return true;
		}
	}

	/** @y.exclude */
	protected boolean registerSpeedController(Controller c,int index){
		if(mySpeedController!=null)
			return false;
		else{
			mySpeedController = c;
			control_maxspeed_index = index;
			return true;
		}
	}

	/** @y.exclude */
	protected FundamentalDiagram currentFD(int ensemble){
		if(activeFDevent)
			return FDfromEvent;
		else
			return FDfromProfile[ensemble];
	}
	
	/** @y.exclude */
    protected void setFundamentalDiagramProfile(FundamentalDiagramProfile fdp){
    	if(fdp==null)
    		return;
    	myFDprofile = fdp;
    }

	/** @throws SiriusException 
	 * @y.exclude */
    protected void setFundamentalDiagramFromProfile(FundamentalDiagram fd) throws SiriusException{
    	if(fd==null)
    		return;
    	
    	// sample the fundamental digram
    	for(int e=0;e<myNetwork.myScenario.numEnsemble;e++)
    		FDfromProfile[e] = fd.perturb();
    }

	/** @throws SiriusException 
	 * @y.exclude */
    protected void activateFundamentalDiagramEvent(com.relteq.sirius.jaxb.FundamentalDiagram fd) throws SiriusException {
    	if(fd==null)
    		throw new SiriusException("Null parameter.");
    	
    	FDfromEvent = new FundamentalDiagram(this,currentFD(0));		// copy current FD 
    	// note: we are copying from the zeroth FD for simplicity. The alternative is to 
    	// carry numEnsemble event FDs.
    	FDfromEvent.copyfrom(fd);			// replace values with those defined in the event
    	
    	SiriusErrorLog.clearErrorMessage();
    	FDfromEvent.validate();
		if(SiriusErrorLog.haserror())
			throw new SiriusException("Fundamental diagram event could not be validated.");
		
		activeFDevent = true;
    }

	/** @throws SiriusException 
	 * @y.exclude */
    protected void revertFundamentalDiagramEvent() throws SiriusException{
    	if(!activeFDevent)
    		return;
    	activeFDevent = false;
    }

	/** @throws SiriusException 
	 * @y.exclude */
	protected void set_Lanes(double newlanes) throws SiriusException{
		for(int e=0;e<myNetwork.myScenario.numEnsemble;e++)
			if(getDensityJamInVeh(e)*newlanes/get_Lanes() < getTotalDensityInVeh(e))
				throw new SiriusException("ERROR: Lanes could not be set.");

		myFDprofile.set_Lanes(newlanes);	// adjust present and future fd's
		for(int e=0;e<myNetwork.myScenario.numEnsemble;e++)
			FDfromProfile[e].setLanes(newlanes);
		_lanes = newlanes;					// adjust local copy of lane count
	}

	/** @y.exclude */
	protected void setSourcedemandFromVeh(int destnetindex , double[] value) {
		if(destnetindex<0 || destnetindex>numDNetworks-1)
			return;
		sourcedemand[destnetindex] = value;		
	}
	
	// this is used by CapacityProfile only.
	// no FDs in this link may have capacities that exceed c.
	protected void setCapacityFromVeh(double c) {
		for(FundamentalDiagram fd : FDfromProfile)
			fd._capacity = fd._capacity<c ? fd._capacity : c;
		if(FDfromEvent!=null)
			FDfromEvent._capacity = FDfromEvent._capacity<c ? FDfromEvent._capacity : c;
	}
	
	protected void addDestinationNetwork(int dest_index){
		numDNetworks++;
		myDNindex.add(dest_index);
		link_used = true;
	}
	
	protected int getDestinationNetworkIdFor(String destnetid){
		if(destnetid==null && myNetwork.myScenario.has_background_flow)
			return 0;
		for(int i=0;i<numDNetworks;i++){
			DestinationNetwork destnet = myNetwork.myScenario.destination_networks.get(myDNindex.get(i)).dnetwork;
			if(destnet!=null)
				if(destnet.getId().equals(destnetid))
					return i;
		}
		return -1;
	}
	
	/////////////////////////////////////////////////////////////////////
	// supply and demand calculation
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected void updateOutflowDemand(){
        
		int numVehicleTypes = myNetwork.myScenario.getNumVehicleTypes();
		
        double totaloutflow;
        double control_maxspeed;
        double control_maxflow;
        
        FundamentalDiagram FD;
        
        for(int e=0;e<myNetwork.myScenario.numEnsemble;e++){

        	FD = currentFD(e);
        	
            // case empty link
            if( SiriusMath.lessorequalthan(totaldensity[e],0d) ){
            	outflowDemand[e] =  SiriusMath.zeros(numDNetworks,numVehicleTypes);        		
            	continue;
            }

            // compute total flow leaving the link in the absence of flow control
            if( totaldensity[e] < FD.getDensityCriticalInVeh() ){
            	if(mySpeedController!=null && mySpeedController.ison){
            		// speed control sets a bound on freeflow speed
                	control_maxspeed = mySpeedController.control_maxspeed[control_maxspeed_index];
            		totaloutflow = totaldensity[e] * Math.min(FD.getVfNormalized(),control_maxspeed);	
            	}
            	else
            		totaloutflow = totaldensity[e] * FD.getVfNormalized();
            }
            else{
            	totaloutflow = Math.max(FD._getCapacityInVeh()-FD._getCapacityDropInVeh(),0d);
                if(mySpeedController!=null && mySpeedController.ison){	// speed controller
                	control_maxspeed = mySpeedController.control_maxspeed[control_maxspeed_index];
                	totaloutflow = Math.min(totaloutflow,control_maxspeed*FD.getDensityCriticalInVeh());
                }
            }

            // flow controller
            if(myFlowController!=null && myFlowController.ison){
            	control_maxflow = myFlowController.control_maxflow[control_maxflow_index];
            	totaloutflow = Math.min( totaloutflow , control_maxflow );
            }    

            // flow uncertainty model
            if(myNetwork.myScenario.has_flow_unceratinty){

            	double delta_flow=0.0;
            	double std_dev_flow = myNetwork.myScenario.std_dev_flow;
	            
				switch(myNetwork.myScenario.uncertaintyModel){
				case uniform:
					delta_flow = SiriusMath.sampleZeroMeanUniform(std_dev_flow);
					break;
		
				case gaussian:
					delta_flow = SiriusMath.sampleZeroMeanGaussian(std_dev_flow);
					break;
				}
	            
				totaloutflow = Math.max( 0d , totaloutflow + delta_flow );
				totaloutflow = Math.min( totaloutflow , totaldensity[e] );
            }

            // split among types
            outflowDemand[e] = SiriusMath.times(density[e],totaloutflow/totaldensity[e]);
        }

        return;
    }

	/** @y.exclude */
    protected void updateSpaceSupply(){
		FundamentalDiagram FD;
    	for(int e=0;e<myNetwork.myScenario.numEnsemble;e++){
    		FD = currentFD(e);
            spaceSupply[e] = FD.getWNormalized()*(FD._getDensityJamInVeh() - totaldensity[e]);
            spaceSupply[e] = Math.min(spaceSupply[e],FD._getCapacityInVeh());
            
            // flow uncertainty model
            if(myNetwork.myScenario.has_flow_unceratinty){
            	double delta_flow=0.0;
            	double std_dev_flow = myNetwork.myScenario.std_dev_flow;
	            
				switch(myNetwork.myScenario.uncertaintyModel){
				case uniform:
					delta_flow = SiriusMath.sampleZeroMeanUniform(std_dev_flow);
					break;
		
				case gaussian:
					delta_flow = SiriusMath.sampleZeroMeanGaussian(std_dev_flow);
					break;
				}
				spaceSupply[e] = Math.max( 0d , spaceSupply[e] + delta_flow );
				spaceSupply[e] = Math.min( spaceSupply[e] , FD._getDensityJamInVeh() - totaldensity[e]);
            }
    	}
    }
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////    

	/** @y.exclude */
	protected void populate(Network myNetwork) {

        this.myNetwork = myNetwork;
        this.my_global_index = myNetwork.myScenario.numLinks;
        myNetwork.myScenario.numLinks++;
        this.link_used = false;

        
        // link type
        this.myType = Link.Type.valueOf(getType());
        
		// make network connections
		begin_node = myNetwork.getNodeWithId(getBegin().getNodeId());
		end_node = myNetwork.getNodeWithId(getEnd().getNodeId());
        
		// nodes must populate before links
		if(begin_node!=null)
			issource = begin_node.isTerminal;
		if(end_node!=null)
			issink = end_node.isTerminal;

		// lanes and length
		if(getLanes()!=null)
			_lanes = getLanes().doubleValue();
		if(getLength()!=null)
			_length = getLength().doubleValue();
	}

	/** @y.exclude */
	protected void validate() {
		
		if(!issource && begin_node==null)
			SiriusErrorLog.addError("Incorrect begin node id=" + getBegin().getNodeId() + " in link id=" + getId() + ".");

		if(!issink && end_node==null)
			SiriusErrorLog.addError("Incorrect e d node id=" + getEnd().getNodeId() + " in link id=" + getId() + ".");
		
		if(_length<=0)
			SiriusErrorLog.addError("Non-positive length in link id=" + getId() + ".");
		
		if(_lanes<=0)
			SiriusErrorLog.addError("Non-positive number of lanes in link id=" + getId() + ".");		
	}

	/** @y.exclude */
	protected void resetState(Scenario.ModeType simulationMode) {
		
		Scenario myScenario = myNetwork.myScenario;
		
		int numEnsemble = myScenario.numEnsemble;
		int numVehicleTypes = myScenario.getNumVehicleTypes();
		
		switch(simulationMode){
		
		case warmupFromZero:			// in warmupFromZero mode the simulation start with an empty network
			density = SiriusMath.zeros(numEnsemble,numDNetworks,numVehicleTypes);
			totaldensity = SiriusMath.zeros(numEnsemble);
			break;

		case warmupFromIC:				// in warmupFromIC and normal modes, the simulation starts 
		case normal:					// from the initial density profile 
			density = new double[numEnsemble][numDNetworks][numVehicleTypes];
			totaldensity = SiriusMath.zeros(numEnsemble);
			if(myScenario.getInitialDensitySet()!=null)
				density[0] = ((InitialDensitySet)myScenario.getInitialDensitySet()).getDensityForLinkIdInVeh(getId(),myDNindex);	
			else 
				density[0] = SiriusMath.zeros(numDNetworks,myScenario.getNumVehicleTypes());
			totaldensity[0] = SiriusMath.sumsum(density[0]);
			for(int e=1;e<numEnsemble;e++){
				density[e] = SiriusMath.makecopy(density[0]);
				totaldensity[e] = totaldensity[0];
			}
			break;
			
		default:
			break;
				
		}

		// reset other quantities
        inflow 				= SiriusMath.zeros(numEnsemble,numDNetworks,numVehicleTypes);
        outflow 			= SiriusMath.zeros(numEnsemble,numDNetworks,numVehicleTypes);
        sourcedemand 		= SiriusMath.zeros(numDNetworks,numVehicleTypes);
        outflowDemand 		= SiriusMath.zeros(numEnsemble,numDNetworks,numVehicleTypes);
        spaceSupply 		= SiriusMath.zeros(numEnsemble);
        
        // for correct export of initial condition
        cumulative_density 	= SiriusMath.makecopy(density);
        cumulative_inflow 	= SiriusMath.zeros(numEnsemble,numDNetworks,numVehicleTypes);
        cumulative_outflow 	= SiriusMath.zeros(numEnsemble,numDNetworks,numVehicleTypes);

		return;
	}

	/** @y.exclude */
	protected void resetLanes(){
		_lanes = getLanes().doubleValue();
	}

	/** @y.exclude */
	protected void resetFD(){
		FDfromProfile = new FundamentalDiagram [myNetwork.myScenario.numEnsemble];
		for(int i=0;i<FDfromProfile.length;i++){
			FDfromProfile[i] = new FundamentalDiagram(this);
			FDfromProfile[i].settoDefault();
		}
    	activeFDevent = false;
	}

	/** @y.exclude */
	protected void update() {
		
        if(issink)
            outflow = outflowDemand;
        
        if(issource)
        	for(int e=0;e<this.myNetwork.myScenario.numEnsemble;e++)
        		inflow[e] = SiriusMath.makecopy(sourcedemand);
                
        int e,p,j;
		for(e=0;e<myNetwork.myScenario.numEnsemble;e++){
			for(p=0;p<numDNetworks;p++){
				for(j=0;j<myNetwork.myScenario.getNumVehicleTypes();j++){
					density[e][p][j] += inflow[e][p][j] - outflow[e][p][j];
					cumulative_density[e][p][j] += density[e][p][j];
					cumulative_inflow[e][p][j]  += inflow[e][p][j];
					cumulative_outflow[e][p][j] += outflow[e][p][j];
				}
			}
			totaldensity[e] = SiriusMath.sumsum(density[e]);
		}

	}

	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public String toString() {
		return this.getId();
	}	

	// Link type ........................
	
	public Link.Type getMyType() {
		return myType;
	}
	
	public static boolean isFreewayType(Link link){
		
		if(link==null)
			return false;
		
		Link.Type linktype = link.getMyType();
		
		return  linktype.compareTo(Link.Type.intersection_approach)!=0 &&
				linktype.compareTo(Link.Type.offramp)!=0 &&
				linktype.compareTo(Link.Type.onramp)!=0 &&
				linktype.compareTo(Link.Type.street)!=0;		
	}
	
	
	// Link geometry ....................
	
	/** network that contains this link */
	public Network getMyNetwork() {
		return myNetwork;
	}

	/** upstream node of this link  */
	public Node getBegin_node() {
		return begin_node;
	}

	/** downstream node of this link */
	public Node getEnd_node() {
		return end_node;
	}

	/** Length of this link in miles */
	public double getLengthInMiles() {
		return _length;
	}

	/** Number of lanes in this link */
	public double get_Lanes() {
		return _lanes;
	}

	/** <code>true</code> if this link is a source of demand into the network */
	public boolean isSource() {
		return issource;
	}

	/** <code>true</code> if this link is a sink of demand from the network */
	public boolean isSink() {
		return issink;
	}
	
	// Link state .......................

	/** Density of vehicles per destination network and vehicle type in normalized units [vehicles]. 
	 * The return matrix is indexed first by destination network in the order given by Scenaroi.getDestinationNetworkNames(), and second
	 * by vehicle type in the order given by Scenario.getVehicleTypeNames(). 
	 * @param ensemble Ensemble number 
	 * @return number of vehicles of each type in the link. <code>null</code> if something goes wrong.
	 */
	public double[][] getDensityInVeh(int ensemble) {
		try{
			int numDN = myNetwork.myScenario.numDenstinationNetworks;
			int numVT = myNetwork.myScenario.numVehicleTypes;
			double [][] dens = SiriusMath.zeros(numDN,numVT);
			int d,k;
			for(d=0;d<numDNetworks;d++)
				for(k=0;k<numVT;k++)
					dens[myDNindex.get(d)][k] = density[ensemble][d][k];
			return dens;
		} catch(Exception e){
			return null;
		}
	}

	/** Total of vehicles in normalized units (vehicles/link). 
	 * The return value equals the sum of {@link Link#getDensityInVeh}.
	 * @return total number of vehicles in the link. 0 if something goes wrong.
	 */
	public double getTotalDensityInVeh(int ensemble) {
		try{
			return totaldensity[ensemble];
		} catch(Exception e){
			return 0d;
		}
	}
	
	/** Total of vehicles in (vehicles/mile). 
	 * @return total density of vehicles in the link. 0 if something goes wrong.
	 */
	public double getTotalDensityInVPM(int ensemble) {
		try{
			return getTotalDensityInVeh(ensemble)/_length;
		} catch(Exception e){
			return 0d;
		}
	}
	
	/** Number of vehicles per vehicle type exiting the link 
	 * during the current time step. The return array is indexed by 
	 * vehicle type in the order given in the <code>settings</code> 
	 * portion of the input file. 
	 * @return array of exiting flows per vehicle type. <code>null</code> if something goes wrong.
	 */
	public double [][] getOutflowInVeh(int ensemble) {
		try{
			int numDN = myNetwork.myScenario.numDenstinationNetworks;
			int numVT = myNetwork.myScenario.numVehicleTypes;
			double [][] f = SiriusMath.zeros(numDN,numVT);
			int d,k;
			for(d=0;d<numDNetworks;d++)
				for(k=0;k<numVT;k++)
					f[myDNindex.get(d)][k] = outflow[ensemble][d][k];
			return f;
		} catch(Exception e){
			return null;
		}
	}

	/** Total number of vehicles exiting the link during the current
	 * time step.  The return value equals the sum of 
	 * {@link Link#getOutflowInVeh}.
	 * @return total number of vehicles exiting the link in one time step.  0 if something goes wrong.
	 * 
	 */
	public double getTotalOutflowInVeh(int ensemble) {
		try{
			return SiriusMath.sumsum(outflow[ensemble]);
		} catch(Exception e){
			return 0d;
		}
	}

	/** Number of vehicles per vehicle type entering the link 
	 * during the current time step. The return array is indexed by 
	 * vehicle type in the order given in the <code>settings</code> 
	 * portion of the input file. 
	 * @return array of entering flows per vehicle type. <code>null</code> if something goes wrong.
	 */
	public double[][] getInflowInVeh(int ensemble) {
		try{
			int numDN = myNetwork.myScenario.numDenstinationNetworks;
			int numVT = myNetwork.myScenario.numVehicleTypes;
			double [][] f = SiriusMath.zeros(numDN,numVT);
			int d,k;
			for(d=0;d<numDNetworks;d++)
				for(k=0;k<numVT;k++)
					f[myDNindex.get(d)][k] = inflow[ensemble][d][k];
			return f;
		} catch(Exception e){
			return null;
		}	
	}

	/** Total number of vehicles entering the link during the current
	 * time step.  The return value equals the sum of 
	 * {@link Link#getInflowInVeh}.
	 * @return total number of vehicles entering the link in one time step. 0 if something goes wrong.
	 * 
	 */
	public double getTotalInlowInVeh(int ensemble) {
		try{
			return SiriusMath.sumsum(inflow[ensemble]);
		} catch(Exception e){
			return 0d;
		}
	}

	/** Average speed of traffic in the link in mile/hour. 
	 * The return value is computed by dividing the total outgoing 
	 * link flow by the total link density. 
	 * @return average link speed. 0  if something goes wrong.
	 */
	public double computeSpeedInMPH(int ensemble){
		try{
			double speed;
			if( SiriusMath.greaterthan(totaldensity[ensemble],0d) )
				speed = SiriusMath.sumsum(outflow[ensemble])/totaldensity[ensemble];
			else
				speed = currentFD(ensemble).getVfNormalized();
			return speed*_length/myNetwork.myScenario.getSimDtInHours();
		} catch(Exception e){
			return 0d;
		}
	}

	// Fundamental diagram ....................
	
	/** Jam density in vehicle/link. */
	public double getDensityJamInVeh(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getDensityJamInVeh();
	}

	/** Critical density in vehicle/link. */
	public double getDensityCriticalInVeh(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else		
			return FD.getDensityCriticalInVeh();
	}

	/** Capacity drop in vehicle/simulation time step */
	public double getCapacityDropInVeh(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getCapacityDropInVeh();
	}

	/** Capacity in vehicle/simulation time step */
	public double getCapacityInVeh(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getCapacityInVeh();
	}

	/** Jam density in vehicle/mile/lane. */
	public double getDensityJamInVPMPL(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getDensityJamInVeh()/getLengthInMiles()/_lanes;
	}

	/** Critical density in vehicle/mile/lane. */
	public double getDensityCriticalInVPMPL(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD.getDensityCriticalInVeh()/getLengthInMiles()/_lanes;
	}

	/** Capacity drop in vehicle/hr/lane. */
	public double getCapacityDropInVPHPL(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getCapacityDropInVeh()/myNetwork.myScenario.getSimDtInHours()/_lanes;
	}

	/** Capacity in vehicles per hour. */
	public double getCapacityInVPH(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getCapacityInVeh() / myNetwork.myScenario.getSimDtInHours();
	}

	/** Capacity in vehicle/hr/lane. */
	public double getCapacityInVPHPL(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD._getCapacityInVeh()/myNetwork.myScenario.getSimDtInHours()/_lanes;
	}

	/** Freeflow speed in normalized units (link/time step). */
	public double getNormalizedVf(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD.getVfNormalized();
	}

	/** Freeflow speed in mile/hr. */
	public double getVfInMPH(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD.getVfNormalized()*getLengthInMiles()/myNetwork.myScenario.getSimDtInHours();
	}

	/** Congestion wave speed in normalized units (link/time step). */
	public double getNormalizedW(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD.getWNormalized();
	}

	/** Congestion wave speed in mile/hr. */
	public double getWInMPH(int ensemble) {
		FundamentalDiagram FD = currentFD(ensemble);
		if(FD==null)
			return Double.NaN;
		else
			return FD.getWNormalized()*getLengthInMiles()/myNetwork.myScenario.getSimDtInHours();
	}
		
	/** Replace link density with given values.
	 *  [This is API call is being made available for implementation of particle filtering.
	 *  Use with caution.]
	 */
	public void overrideDensityWithVeh(Double[][] x,int ensemble){
		if(ensemble<0 || ensemble>=density.length)
			return;
		if(x.length!=density[0].length)
			return;
		if(x[0].length!=density[0][0].length)
			return;
		int i,j;
		for(i=0;i<x.length;i++)
			for(j=0;j<x[i].length;j++)
				density[ensemble][i][j] = x[i][j];
	}

}
