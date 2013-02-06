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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.calibrator.FDCalibrator;
import edu.berkeley.path.beats.data.DataFileReader;
import edu.berkeley.path.beats.data.FiveMinuteData;
import edu.berkeley.path.beats.jaxb.ControllerSet;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import edu.berkeley.path.beats.jaxb.DestinationNetworks;
import edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfileSet;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramProfileSet;
import edu.berkeley.path.beats.jaxb.NetworkConnections;
import edu.berkeley.path.beats.jaxb.NetworkList;
import edu.berkeley.path.beats.jaxb.Routes;
import edu.berkeley.path.beats.jaxb.Settings;
import edu.berkeley.path.beats.jaxb.SignalList;
import edu.berkeley.path.beats.jaxb.WeavingFactorSet;
import edu.berkeley.path.beats.sensor.DataSource;
import edu.berkeley.path.beats.sensor.SensorLoopStation;

/** Load, manipulate, and run scenarios. 
 * <p>
 * A scenario is a collection of,
 * <ul>
 * <li> networks (nodes, links, sensors, and signals), </li>
 * <li> network connections, </li>
 * <li> initial conditions, </li>
 * <li> weaving factor profiles, </li>
 * <li> split ratio profiles, </li>
 * <li> downstream boundary conditions, </li> 
 * <li> events, </li>
 * <li> controllers, </li>
 * <li> fundamental diagram profiles, </li>
 * <li> destination networks, and </li>
 * <li> demand profiles. </li>
*  </ul>
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public final class Scenario extends edu.berkeley.path.beats.jaxb.Scenario {

	/** @y.exclude */	protected static enum ModeType {  normal, 
		  warmupFromZero , 
		  warmupFromIC };
	/** @y.exclude */	protected static enum UncertaintyType { uniform, 
		  gaussian }

	/** @y.exclude */	private static Logger logger = Logger.getLogger(Scenario.class);
	/** @y.exclude */	protected Clock clock;
	/** @y.exclude */	protected String configfilename;
	/** @y.exclude */	protected Scenario.UncertaintyType uncertaintyModel;
	/** @y.exclude */	protected int numVehicleTypes;			// number of vehicle types
	/** @y.exclude */	protected boolean global_control_on;	// global control switch
	/** @y.exclude */	protected double global_demand_knob;	// scale factor for all demands
	/** @y.exclude */	protected double simdtinseconds;		// [sec] simulation time step 
	/** @y.exclude */	protected boolean scenariolocked=false;	// true when the simulation is running
	/** @y.exclude */	protected edu.berkeley.path.beats.simulator.ControllerSet controllerset = new edu.berkeley.path.beats.simulator.ControllerSet();
	/** @y.exclude */	protected EventSet eventset = new EventSet();	// holds time sorted list of events	
	/** @y.exclude */	protected SensorList sensorlist = new SensorList();
	/** @y.exclude */	protected int numEnsemble;
	/** @y.exclude */	protected boolean started_writing;

	// Model uncertainty
	/** @y.exclude */	protected double std_dev_flow = 0.0d;	// [veh]
	/** @y.exclude */	protected boolean has_flow_unceratinty;
	
	// data
	private boolean sensor_data_loaded = false;

	protected Cumulatives cumulatives;
	
	/////////////////////////////////////////////////////////////////////
	// protected constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Scenario(){}
	
	/////////////////////////////////////////////////////////////////////
	// hide base class setters
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public void setDescription(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setControllerSet(ControllerSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSettings(Settings value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setNetworkList(NetworkList value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSignalList(SignalList value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSensorList(edu.berkeley.path.beats.jaxb.SensorList value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setInitialDensitySet(
			edu.berkeley.path.beats.jaxb.InitialDensitySet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setWeavingFactorSet(WeavingFactorSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSplitRatioProfileSet(
			edu.berkeley.path.beats.jaxb.SplitRatioProfileSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setDownstreamBoundaryCapacityProfileSet(
			DownstreamBoundaryCapacityProfileSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setEventSet(edu.berkeley.path.beats.jaxb.EventSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setDemandProfileSet(
			edu.berkeley.path.beats.jaxb.DemandProfileSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setFundamentalDiagramProfileSet(FundamentalDiagramProfileSet value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setNetworkConnections(NetworkConnections value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setDestinationNetworks(DestinationNetworks value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setRoutes(Routes value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setId(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setName(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSchemaVersion(String value) {
		System.out.println("This setter is hidden.");
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected void populate() throws BeatsException {
		
		// network list
		if(networkList!=null)
			for( edu.berkeley.path.beats.jaxb.Network network : networkList.getNetwork() )
				((Network) network).populate(this);

		// sensors
		sensorlist.populate(this);
		
		// signals
		if(signalList!=null)
			for(edu.berkeley.path.beats.jaxb.Signal signal : signalList.getSignal())
				((Signal) signal).populate(this);
		
		// split ratio profile set (must follow network)
		if(splitRatioProfileSet!=null)
			((SplitRatioProfileSet) splitRatioProfileSet).populate(this);
		
		// boundary capacities (must follow network)
		if(downstreamBoundaryCapacityProfileSet!=null)
			for( edu.berkeley.path.beats.jaxb.CapacityProfile capacityProfile : downstreamBoundaryCapacityProfileSet.getCapacityProfile() )
				((CapacityProfile) capacityProfile).populate(this);

		if(demandProfileSet!=null)
			((DemandProfileSet) demandProfileSet).populate(this);
		
		// fundamental diagram profiles 
		if(fundamentalDiagramProfileSet!=null)
			for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fd : fundamentalDiagramProfileSet.getFundamentalDiagramProfile())
				((FundamentalDiagramProfile) fd).populate(this);
		
		// initial density profile 
		if(initialDensitySet!=null)
			((InitialDensitySet) initialDensitySet).populate(this);
		
		// populate controllers 
		controllerset.populate(this);

		// populate events 
		eventset.populate(this);

		cumulatives = new Cumulatives(this);
	}

	/** @y.exclude */
	public static void validate(Scenario S) {
				
		// validate network
		if( S.networkList!=null)
			for(edu.berkeley.path.beats.jaxb.Network network : S.networkList.getNetwork())
				((Network)network).validate();

		// sensor list
		S.sensorlist.validate();
		
		// signal list
		if(S.signalList!=null)
			for (edu.berkeley.path.beats.jaxb.Signal signal : S.signalList.getSignal())
				((Signal) signal).validate();
		
		// NOTE: DO THIS ONLY IF IT IS USED. IE DO IT IN THE RUN WITH CORRECT FUNDAMENTAL DIAGRAMS
		// validate initial density profile
//		if(getInitialDensityProfile()!=null)
//			((_InitialDensityProfile) getInitialDensityProfile()).validate();

		// validate capacity profiles	
		if(S.downstreamBoundaryCapacityProfileSet!=null)
			for(edu.berkeley.path.beats.jaxb.CapacityProfile capacityProfile : S.downstreamBoundaryCapacityProfileSet.getCapacityProfile())
				((CapacityProfile)capacityProfile).validate();
		
		// validate demand profiles
		if(S.demandProfileSet!=null)
			((DemandProfileSet)S.demandProfileSet).validate();

		// validate split ratio profiles
		if(S.splitRatioProfileSet!=null)
			((SplitRatioProfileSet)S.splitRatioProfileSet).validate();
		
		// validate fundamental diagram profiles
		if(S.fundamentalDiagramProfileSet!=null)
			for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fd : S.fundamentalDiagramProfileSet.getFundamentalDiagramProfile())
				((FundamentalDiagramProfile)fd).validate();
		
		// validate controllers
		S.controllerset.validate();

	}
	
	/** Prepare scenario for simulation:
	 * set the state of the scenario to the initial condition
	 * sample profiles
	 * open output files
	 * @return success		A boolean indicating whether the scenario was successfuly reset.
	 * @throws BeatsException 
	 * @y.exclude
	 */
	protected boolean reset(Scenario.ModeType simulationMode) throws BeatsException {
		
		started_writing = false;
		global_control_on = true;
	    global_demand_knob = 1d;
		
		// reset the clock
		clock.reset();
		
		// reset network
		for(edu.berkeley.path.beats.jaxb.Network network : networkList.getNetwork())
			((Network)network).reset(simulationMode);
		
		// sensor list
		sensorlist.reset();
		
		// signal list
		if(signalList!=null)
			for (edu.berkeley.path.beats.jaxb.Signal signal : signalList.getSignal())
				((Signal) signal).reset();
						
		// reset demand profiles
		if(demandProfileSet!=null)
			((DemandProfileSet)demandProfileSet).reset();

		// reset fundamental diagrams
		if(fundamentalDiagramProfileSet!=null)
			for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fd : fundamentalDiagramProfileSet.getFundamentalDiagramProfile())
				((FundamentalDiagramProfile)fd).reset();
		
		// reset controllers
		controllerset.reset();

		// reset events
		eventset.reset();

		cumulatives.reset();

		return true;
		
	}	
	
	/** @y.exclude */
	protected void update() throws BeatsException {	

        // sample profiles .............................	
    	if(downstreamBoundaryCapacityProfileSet!=null)
        	for(edu.berkeley.path.beats.jaxb.CapacityProfile capacityProfile : downstreamBoundaryCapacityProfileSet.getCapacityProfile())
        		((CapacityProfile) capacityProfile).update();

    	if(demandProfileSet!=null)
    		((DemandProfileSet)demandProfileSet).update();

    	if(splitRatioProfileSet!=null)
    		((SplitRatioProfileSet) splitRatioProfileSet).update();        		

    	if(fundamentalDiagramProfileSet!=null)
        	for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fdProfile : fundamentalDiagramProfileSet.getFundamentalDiagramProfile())
        		((FundamentalDiagramProfile) fdProfile).update();
    	
        // update sensor readings .......................
    	sensorlist.update();
		
        // update signals ...............................
		// NOTE: ensembles have not been implemented for signals. They do not apply
		// to pretimed control, but would make a differnece for feedback control. 
		if(signalList!=null)
			for(edu.berkeley.path.beats.jaxb.Signal signal : signalList.getSignal())
				((Signal)signal).update();

        // update controllers
    	if(global_control_on)
    		controllerset.update();

    	// update events
    	eventset.update();
    	
        // update the network state......................
		for(edu.berkeley.path.beats.jaxb.Network network : networkList.getNetwork())
			((Network) network).update();

		cumulatives.update();
	}

	/////////////////////////////////////////////////////////////////////
	// protected interface
	/////////////////////////////////////////////////////////////////////
	
	protected void run(SimulationSettings simsettings,String outtype,String outprefix) throws BeatsException{
		this.numEnsemble = 1;
		RunParameters param = new RunParameters(simsettings.getStartTime(), simsettings.getEndTime(), simsettings.getOutputDt(), simdtinseconds);
		run_internal(param,simsettings.getNumReps(),true,outtype,outprefix);
	}
	
	/** Retrieve a network with a given id.
	 * @param id The string id of the network
	 * @return The corresponding network if it exists, <code>null</code> otherwise.
	 * 
	 */
	protected Network getNetworkWithId(String id){
		if(networkList==null)
			return null;
		if(networkList.getNetwork()==null)
			return null;
		if(id==null && networkList.getNetwork().size()>1)
			return null;
		if(id==null && networkList.getNetwork().size()==1)
			return (Network) networkList.getNetwork().get(0);
		id.replaceAll("\\s","");
		for(edu.berkeley.path.beats.jaxb.Network network : networkList.getNetwork()){
			if(network.getId().equals(id))
				return (Network) network;
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	// intialization and running ........................................
	
	/** Initialize the run before using {@link Scenario#advanceNSeconds(double)}
	 * 
	 * <p>This method performs certain necessary initialization tasks on the scenario. In particular
	 * it locks the scenario so that elements may not be added mid-run. It also resets the scenario
	 * rolling back all profiles and clocks. 
	 * @param numEnsemble Number of simulations to run in parallel
	 */
	public void initialize_run(int numEnsemble,double timestart) throws BeatsException{

		if(numEnsemble<=0)
			throw new BeatsException("Number of ensemble runs must be at least 1.");
		
		RunParameters param = new RunParameters(timestart,Double.POSITIVE_INFINITY,Double.NaN,simdtinseconds);

		this.scenariolocked = false;
		this.numEnsemble = numEnsemble;
        
		// create the clock
		clock = new Clock(param.timestart,param.timeend,simdtinseconds);

		// reset the simulation
		if(!reset(param.simulationMode))
			throw new BeatsException("Reset failed.");
		
		// lock the scenario
        scenariolocked = true;	
	}
	
	public void run(double timestart,double timeend,double outdt,String outputtype, String outputfileprefix,int numReps) throws BeatsException{
		this.numEnsemble = 1;
		RunParameters param = new RunParameters(timestart, timeend, outdt, simdtinseconds);
		run_internal(param,numReps,true,outputtype,outputfileprefix);
	}
		
	/** Advance the simulation <i>nsec</i> seconds.
	 * 
	 * <p> Move the simulation forward <i>nsec</i> seconds and stops.
	 * Returns <code>true</code> if the operation completes succesfully. Returns <code>false</code>
	 * if the end of the simulation is reached.
	 * @param nsec Number of seconds to advance.
	 * @throws BeatsException 
	 */
	public boolean advanceNSeconds(double nsec) throws BeatsException{	
		
		if(!scenariolocked)
			throw new BeatsException("Run not initialized. Use initialize_run() first.");
		
		if(!BeatsMath.isintegermultipleof(nsec,simdtinseconds))
			throw new BeatsException("nsec (" + nsec + ") must be an interger multiple of simulation dt (" + simdtinseconds + ").");
		int nsteps = BeatsMath.round(nsec/simdtinseconds);				
		return advanceNSteps_internal(ModeType.normal,nsteps,false,null,-1d);
	}

	/** Save the scenario to XML.
	 * 
	 * @param filename The name of the configuration file.
	 * @throws BeatsException 
	 */
	public void saveToXML(String filename) throws BeatsException{
        try {
        	
        	//Reset the classloader for main thread; need this if I want to run properly
            //with JAXB within MATLAB. (luis)
        	Thread.currentThread().setContextClassLoader(Scenario.class.getClassLoader());
	
        	JAXBContext context = JAXBContext.newInstance("edu.berkeley.path.beats.jaxb");
        	Marshaller m = context.createMarshaller();
        	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        	m.marshal(this,new FileOutputStream(filename));
        } catch( JAXBException je ) {
        	throw new BeatsException(je.getMessage());
        } catch (FileNotFoundException e) {
        	throw new BeatsException(e.getMessage());
        }
	}
	
	// scalar getters ........................................................
	
	/** Current simulation time in seconds.
	 * @return Simulation time in seconds after midnight.
	 */
	public double getCurrentTimeInSeconds() {
		if(clock==null)
			return Double.NaN;
		return clock.getT();
	}
	
	/** Time elapsed since the beginning of the simulation in seconds.
	 * @return Simulation time in seconds after start time.
	 */
	public double getTimeElapsedInSeconds() {
		if(clock==null)
			return Double.NaN;
		return clock.getTElapsed();
	}
	
	/** Current simulation time step.
	 * @return	Integer number of time steps since the start of the simulation. 
	 */
	public int getCurrentTimeStep() {
		if(clock==null)
			return 0;
		return clock.getCurrentstep();
	}

	/** Total number of time steps that will be simulated, regardless of the simulation mode.
	 * @return	Integer number of time steps to simulate.
	 */
	public int getTotalTimeStepsToSimulate(){
		if(clock==null)
			return -1;
		else
			return clock.getTotalSteps();
	}
	
	/** Number of vehicle types included in the scenario.
	 * @return Integer number of vehicle types
	 */
	public int getNumVehicleTypes() {
		return numVehicleTypes;
	}
	
	/** Number of ensembles in the run.
	 * @return Integer number of elements in the ensemble.
	 */
	public int getNumEnsemble() {
		return numEnsemble;
	}

	/** Vehicle type index from name
	 * @return integer index of the vehicle type.
	 */
	public int getVehicleTypeIndex(String name){
		String [] vehicleTypeNames = getVehicleTypeNames();
		if(vehicleTypeNames==null)
			return 0;
		if(vehicleTypeNames.length<=1)
			return 0;
		for(int i=0;i<vehicleTypeNames.length;i++)
			if(vehicleTypeNames[i].equals(name))
				return i;
		return -1;
	}
	
	/** Size of the simulation time step in seconds.
	 * @return Simulation time step in seconds. 
	 */
	public double getSimDtInSeconds() {
		return simdtinseconds;
	}

	/** Start time of the simulation.
	 * @return Start time in seconds. 
	 */
	public double getTimeStart() {
		if(clock==null)
			return Double.NaN;
		else
			return this.clock.getStartTime();
	}

	/** End time of the simulation.
	 * @return End time in seconds. 
	 * @return			XXX
	 */
	public double getTimeEnd() {
		if(clock==null)
			return Double.NaN;
		else
			return this.clock.getEndTime();
	}
	
	/** Get configuration file name */
	public String getConfigFilename() {
		return configfilename;
	}

	// array getters ........................................................

	/** @y.exclude */
	public Integer [] getVehicleTypeIndices(edu.berkeley.path.beats.jaxb.VehicleTypeOrder vtypeorder){
		
		Integer [] vehicletypeindex;
		
		// single vehicle types in setting and no vtypeorder, return 0
		if(vtypeorder==null && numVehicleTypes==1){
			vehicletypeindex = new Integer[numVehicleTypes];
			vehicletypeindex[0]=0;
			return vehicletypeindex;
		}
		
		// multiple vehicle types in setting and no vtypeorder, return 0...n
		if(vtypeorder==null && numVehicleTypes>1){
			vehicletypeindex = new Integer[numVehicleTypes];
			for(int i=0;i<numVehicleTypes;i++)
				vehicletypeindex[i] = i;	
			return vehicletypeindex;	
		}
		
		// vtypeorder is not null
		int numTypesInOrder = vtypeorder.getVehicleType().size();
		int i,j;
		vehicletypeindex = new Integer[numTypesInOrder];
		for(i=0;i<numTypesInOrder;i++)
			vehicletypeindex[i] = -1;			

		if(getSettings()==null)
			return vehicletypeindex;

		if(getSettings().getVehicleTypes()==null)
			return vehicletypeindex;
		
		for(i=0;i<numTypesInOrder;i++){
			String vtordername = vtypeorder.getVehicleType().get(i).getName();
			List<edu.berkeley.path.beats.jaxb.VehicleType> settingsname = getSettings().getVehicleTypes().getVehicleType();
			for(j=0;j<settingsname.size();j++){
				if(settingsname.get(j).getName().equals(vtordername)){
					vehicletypeindex[i] =  j;
					break;
				}
			}			
		}
		return vehicletypeindex;
	}
	
	/** Vehicle type names.
	 * @return	Array of strings with the names of the vehicles types.
	 */
	public String [] getVehicleTypeNames(){
		String [] vehtypenames = new String [numVehicleTypes];
		if(getSettings()==null || getSettings().getVehicleTypes()==null)
			vehtypenames[0] = Defaults.vehicleType;
		else
			for(int i=0;i<getSettings().getVehicleTypes().getVehicleType().size();i++)
				vehtypenames[i] = getSettings().getVehicleTypes().getVehicleType().get(i).getName();
		return vehtypenames;
	}
	
//	/** Vehicle type weights.
//	 * @return	Array of doubles with the weights of the vehicles types.
//	 */
//	public Double [] getVehicleTypeWeights(){
//		Double [] vehtypeweights = new Double [numVehicleTypes];
//		if(getSettings()==null || getSettings().getVehicleTypes()==null)
//			vehtypeweights[0] = 1d;
//		else
//			for(int i=0;i<getSettings().getVehicleTypes().getVehicleType().size();i++)
//				vehtypeweights[i] = getSettings().getVehicleTypes().getVehicleType().get(i).getWeight().doubleValue();
//		return vehtypeweights;
//	}
	

	/** Get the initial density state for the network with given id.
	 * @param network_id String id of the network
	 * @return A two-dimensional array of doubles where the first dimension is the
	 * link index (ordered as in {@link Network#getListOfLinks}) and the second is the vehicle type 
	 * (ordered as in {@link Scenario#getVehicleTypeNames})
	 */
	public double [][] getInitialDensityForNetwork(String network_id){
				
		Network network = getNetworkWithId(network_id);
		if(network==null)
			return null;
		
		double [][] density = new double [network.getLinkList().getLink().size()][getNumVehicleTypes()];
		InitialDensitySet initprofile = (InitialDensitySet) getInitialDensitySet();

		int i,j;
		for(i=0;i<network.getLinkList().getLink().size();i++){
			if(initprofile==null){
				for(j=0;j<numVehicleTypes;j++)
					density[i][j] = 0d;
			}
			else{
				edu.berkeley.path.beats.jaxb.Link link = network.getLinkList().getLink().get(i);
				Double [] init_density = initprofile.getDensityForLinkIdInVeh(link.getId(),network.getId());
				for(j=0;j<numVehicleTypes;j++)
					density[i][j] = init_density[j];
			}
		}
		return density;                         
	}

	/** Get the current density state for the network with given id.
	 * @param network_id String id of the network
	 * @return A two-dimensional array of doubles where the first dimension is the
	 * link index (ordered as in {@link Network#getListOfLinks}) and the second is the vehicle type 
	 * (ordered as in {@link Scenario#getVehicleTypeNames})
	 */
	public double [][] getDensityForNetwork(String network_id,int ensemble){
		
		Network network = getNetworkWithId(network_id);
		if(network==null)
			return null;
		
		double [][] density = new double [network.getLinkList().getLink().size()][getNumVehicleTypes()];

		int i,j;
		for(i=0;i<network.getLinkList().getLink().size();i++){
			Link link = (Link) network.getLinkList().getLink().get(i);
			Double [] linkdensity = link.getDensityInVeh(ensemble);
			if(linkdensity==null)
				for(j=0;j<numVehicleTypes;j++)
					density[i][j] = 0d;
			else
				for(j=0;j<numVehicleTypes;j++)
					density[i][j] = linkdensity[j];
		}
		return density;           
		
	}

	// object getters ........................................................

	/** Get a reference to a link by its composite id.
	 * 
	 * @param id String id of the link. 
	 * @return Reference to the link if it exists, <code>null</code> otherwise
	 */
	public Link getLinkWithId(String id){
		if(networkList==null)
			return null;
		for(edu.berkeley.path.beats.jaxb.Network network : networkList.getNetwork()){
			Link link = ((edu.berkeley.path.beats.simulator.Network) network).getLinkWithId(id);
			if(link!=null)
				return link;
		}
		return null;
	}
	
	/** Get a reference to a node by its id.
	 * 
	 * @param id String id of the node. 
	 * @return Reference to the node if it exists, <code>null</code> otherwise
	 */
	public Node getNodeWithId(String id){
		if(networkList==null)
			return null;
		for(edu.berkeley.path.beats.jaxb.Network network : networkList.getNetwork()){
			Node node = ((edu.berkeley.path.beats.simulator.Network) network).getNodeWithId(id);
			if(node!=null)
				return node;
		}
		return null;
	}

	/** Get a reference to a controller by its id.
	 * @param id Id of the controller.
	 * @return A reference to the controller if it exists, <code>null</code> otherwise.
	 */
	public Controller getControllerWithId(String id){
		if(controllerset==null)
			return null;
		for(Controller c : controllerset.get_Controllers()){
			if(c.id.equals(id))
				return c;
		}
		return null;
	}
	
	/** Get a reference to an event by its id.
	 * @param id Id of the event.
	 * @return A reference to the event if it exists, <code>null</code> otherwise.
	 */
	public Event getEventWithId(String id){
		if(eventset==null)
			return null;
		for(Event e : eventset.sortedevents){
			if(e.getId().equals(id))
				return e;
		}
		return null;
	}		

	/** Get sensor with given id.
	 * @param id String id of the sensor.
	 * @return Sensor object.
	 */
	public Sensor getSensorWithId(String id){
		if(sensorList==null)
			return null;
		id.replaceAll("\\s","");
		for(edu.berkeley.path.beats.jaxb.Sensor sensor : sensorList.getSensor()){
			if(sensor.getId().equals(id))
				return (Sensor) sensor;
		}
		return null;
	}
	
	/** Get signal with given id.
	 * @param id String id of the signal.
	 * @return Signal object.
	 */
	public Signal getSignalWithId(String id){
		if(signalList==null)
			return null;
		id.replaceAll("\\s","");
		for(edu.berkeley.path.beats.jaxb.Signal signal : signalList.getSignal()){
			if(signal.getId().equals(id))
				return (Signal) signal;
		}
		return null;
	}
	
	/** Get a reference to a signal by the id of its node.
	 * 
	 * @param node_id String id of the node. 
	 * @return Reference to the signal if it exists, <code>null</code> otherwise
	 */
	public Signal getSignalWithNodeId(String node_id){
		if(signalList==null)
			return null;
		id.replaceAll("\\s","");
		for(edu.berkeley.path.beats.jaxb.Signal signal : signalList.getSignal()){
			if(signal.getNodeId().equals(node_id))
				return (Signal)signal;
		}
		return null;
	}
	
	// add stuff ........................................................

	/** Add a controller to the scenario.
	 * 
	 * <p>Controllers can only be added if a) the scenario is not currently running, and
	 * b) the controller is valid. 
	 * @param C The controller
	 * @return <code>true</code> if the controller was successfully added, <code>false</code> otherwise. 
	 */
	public boolean addController(Controller C){
		if(scenariolocked)
			return false;
		if(C==null)
			return false;
		if(C.myType==null)
			return false;
		
		// validate
		BeatsErrorLog.clearErrorMessage();
		C.validate();
		BeatsErrorLog.print();
		if(BeatsErrorLog.haserror())
			return false;
		
		// add
		controllerset.controllers.add(C);
		
		return true;
	}

	/** Add an event to the scenario.
	 * 
	 * <p>Events are not added if the scenario is running. This method does not validate the event.
	 * @param E The event
	 * @return <code>true</code> if the event was successfully added, <code>false</code> otherwise. 
	 */
	public boolean addEvent(Event E){
		if(scenariolocked)
			return false;
		if(E==null)
			return false;
		if(E.myType==null)
			return false;
		
		// add event to list
		eventset.addEvent(E);
		
		return true;
	}
	
	// override a profile ...............................................	
	
	/** Add a demand profile to the scenario. If a profile already exists for the 
	 * origin link, then replace it.
	 * @throws BeatsException 
	 */
	public void addDemandProfile(edu.berkeley.path.beats.simulator.DemandProfile dem) throws BeatsException  {
		
		if(scenariolocked)
			throw new BeatsException("Cannot modify the scenario while it is locked.");

		if(demandProfileSet==null){
			demandProfileSet = new edu.berkeley.path.beats.jaxb.DemandProfileSet();
			@SuppressWarnings("unused")
			List<DemandProfile> temp = demandProfileSet.getDemandProfile(); // artifficially initialize the profile			
		}
		
		// validate the profile
		BeatsErrorLog.clearErrorMessage();
		dem.validate();
		if(BeatsErrorLog.haserror())
			throw new BeatsException(BeatsErrorLog.format());
		
		// replace an existing profile
		boolean foundit = false;
		for(int i=0;i<demandProfileSet.getDemandProfile().size();i++){
			edu.berkeley.path.beats.jaxb.DemandProfile d = demandProfileSet.getDemandProfile().get(i);
			if(d.getLinkIdOrigin().equals(dem.getLinkIdOrigin())){
				demandProfileSet.getDemandProfile().set(i,dem);
				foundit = true;
				break;
			}
		}
		
		// or add a new one
		if(!foundit)
			demandProfileSet.getDemandProfile().add(dem);

	}
	
	// data and calibration .............................................
	
	public void loadSensorData() throws BeatsException {

		if(sensorlist.sensors.isEmpty())
			return;
		
		if(sensor_data_loaded)
			return;

		HashMap <Integer,FiveMinuteData> data = new HashMap <Integer,FiveMinuteData> ();
		ArrayList<DataSource> datasources = new ArrayList<DataSource>();
		ArrayList<String> uniqueurls  = new ArrayList<String>();
		
		// construct list of stations to extract from datafile 
		for(Sensor sensor : sensorlist.sensors){
			if(sensor.getMyType().compareTo(Sensor.Type.loop)!=0)
				continue;
			SensorLoopStation S = (SensorLoopStation) sensor;
			int myVDS = S.getVDS();				
			data.put(myVDS, new FiveMinuteData(myVDS,true));	
			for(edu.berkeley.path.beats.sensor.DataSource d : S.get_datasources()){
				String myurl = d.getUrl();
				int indexOf = uniqueurls.indexOf(myurl);
				if( indexOf<0 ){
					DataSource newdatasource = new DataSource(d);
					newdatasource.add_to_for_vds(myVDS);
					datasources.add(newdatasource);
					uniqueurls.add(myurl);
				}
				else{
					datasources.get(indexOf).add_to_for_vds(myVDS);
				}
			}
		}
		
		// Read 5 minute data to "data"
		DataFileReader P = new DataFileReader();
		P.Read5minData(data,datasources);
		
		// distribute data to sensors
		for(Sensor sensor : sensorlist.sensors){
			
			if(sensor.getMyType().compareTo(Sensor.Type.loop)!=0)
				continue;

			SensorLoopStation S = (SensorLoopStation) sensor;
			
			// attach to sensor
			S.set5minData(data.get(S.getVDS()));
		}
		
		sensor_data_loaded = true;
		
	}

	public void calibrate_fundamental_diagrams() throws BeatsException {
		FDCalibrator.calibrate(this);
	}
	
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////	

	private void run_internal(RunParameters param,int numRepetitions,boolean writefiles,String outtype,String outprefix) throws BeatsException{
			
		logger.info("Simulation mode: " + param.simulationMode);
		logger.info("Simulation period: [" + param.timestart + ":" + simdtinseconds + ":" + param.timeend + "]");
		logger.info("Output period: [" + param.timestartOutput + ":" + param.outDt + ":" + param.timeend + "]");
		
		// output writer properties
		Properties owr_props = new Properties();
		if (null != outprefix) 
			owr_props.setProperty("prefix", outprefix);
		if (null != outtype) 
			owr_props.setProperty("type",outtype);
		
		// create the clock
		clock = new Clock(param.timestart,param.timeend,simdtinseconds);
		
		// lock the scenario
        scenariolocked = true;	
        
		// loop through simulation runs ............................
		for(int i=0;i<numRepetitions;i++){
			
			OutputWriterBase outputwriter = null;
			if (writefiles){
				outputwriter = OutputWriterFactory.getWriter(this, owr_props, param.outDt,param.outsteps);
				outputwriter.open(i);
			}
			
			try{
				// reset the simulation
				if(!reset(param.simulationMode))
					throw new BeatsException("Reset failed.");

				// advance to end of simulation
				while( advanceNSteps_internal(param.simulationMode,1,writefiles,outputwriter,param.timestartOutput) ){					
				}
			} finally {
				if (null != outputwriter) outputwriter.close();
			}
		}
        scenariolocked = false;
	}
	
	private boolean advanceNSteps_internal(Scenario.ModeType simulationMode,int n,boolean writefiles,OutputWriterBase outputwriter,double outStart) throws BeatsException{
		
		// advance n steps
		for(int k=0;k<n;k++){

			// export initial condition
	        if(!started_writing && BeatsMath.equals(clock.getT(),outStart) ){
	        	recordstate(writefiles,outputwriter,false);
	        	started_writing = true;
	        }
        	
        	// update scenario
        	update();

            // update time (before write to output)
        	clock.advance();

            if(started_writing && clock.getCurrentstep()%outputwriter.outSteps == 0 )
	        	recordstate(writefiles,outputwriter,true);
            
        	if(clock.expired())
        		return false;
		}
	      
		return true;
	}
	
	private void recordstate(boolean writefiles,OutputWriterBase outputwriter,boolean exportflows) throws BeatsException {
		if(writefiles)
			outputwriter.recordstate(clock.getT(),exportflows,outputwriter.outSteps);
		cumulatives.reset();
	}

	/////////////////////////////////////////////////////////////////////
	// private classes
	/////////////////////////////////////////////////////////////////////	
	
	private class RunParameters{
		public double timestart;			// [sec] start of the simulation
		public double timeend;				// [sec] end of the simulation
		public double timestartOutput;		// [sec] start outputing data
		public double outDt;				// [sec] output sampling time
		public int outsteps;				// [-] number of simulation steps per output step
		public Scenario.ModeType simulationMode;
		
		// input parameter outdt [sec] output sampling time
		public RunParameters(double tstart,double tend,double outdt,double simdtinseconds) throws BeatsException{
			
			// round to the nearest decisecond
			tstart = round(tstart);
			simdtinseconds = round(simdtinseconds);
			tend = round(tend);
			outdt = round(outdt);

			// check timestart < timeend
			if( BeatsMath.greaterorequalthan(tstart,tend))
				throw new BeatsException("Empty simulation period.");

			// check that outdt is a multiple of simdt
			if(!Double.isNaN(outdt) && !BeatsMath.isintegermultipleof(outdt,simdtinseconds))
				throw new BeatsException("outdt (" + outdt + ") must be an interger multiple of simulation dt (" + simdtinseconds + ").");
			
			this.timestart = tstart;
			this.timestartOutput = tstart;
			this.timeend = tend;
	        this.outsteps = BeatsMath.round(outdt/simdtinseconds);
			this.outDt = outsteps*simdtinseconds;

	        double time_ic = getInitialDensitySet()!=null ? getInitialDensitySet().getTstamp().doubleValue() : 0d;  // [sec]
	        
			// Simulation mode is normal <=> start time == initial profile time stamp
			simulationMode = null;
			if(BeatsMath.equals(timestart,time_ic)){
				simulationMode = Scenario.ModeType.normal;
			}
			else{
				// it is a warmup. we need to decide on start and end times
				if(time_ic<timestart){	// go from ic to timestart
					timestart = time_ic;
					simulationMode = Scenario.ModeType.warmupFromIC;
				}
				else{							// start at earliest demand profile
					timestart = Double.POSITIVE_INFINITY;
					if(demandProfileSet!=null)
						for(edu.berkeley.path.beats.jaxb.DemandProfile D : demandProfileSet.getDemandProfile())
							timestart = Math.min(timestart,D.getStartTime().doubleValue());					
					if(Double.isInfinite(timestart))
						timestart = 0d;
					timestart = Math.min(timestart, timestartOutput);
					simulationMode = Scenario.ModeType.warmupFromZero;
					
					if(BeatsMath.greaterthan(timeend,time_ic))
						throw new BeatsException("Simulation period stradles the initial condition time stamp.");
				}		
			}
						
		}

		/**
		 * Rounds the double value, precision: .1
		 * @param val
		 * @return the "rounded" value
		 */
		private double round(double val) {
			if(Double.isInfinite(val))
				return val;
			if(Double.isNaN(val))
				return val;
			return BeatsMath.round(val * 10.0) / 10.0;
		}
	}

	protected static class Cumulatives {
		private Scenario scenario;

		/** link id -> cumulative data */
		java.util.Map<String, LinkCumulativeData> links = null;

		/** signal id -> completed phases */
		java.util.Map<String, SignalPhases> phases = null;

		private static Logger logger = Logger.getLogger(Cumulatives.class);

		public Cumulatives(Scenario scenario) {
			this.scenario = scenario;
		}

		public void storeLinks() {
			if (null == links) {
				links = new java.util.HashMap<String, LinkCumulativeData>();
				for (edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()){
					if(((edu.berkeley.path.beats.simulator.Network) network).isempty)
						continue;
					for (edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink()) {
						if (links.containsKey(link.getId()))
							logger.warn("Duplicate link: id=" + link.getId());
						links.put(link.getId(), new LinkCumulativeData((edu.berkeley.path.beats.simulator.Link) link));
					}
				}
				logger.info("Link cumulative data have been requested");
			}
		}

		public void storeSignalPhases() {
			if (null == phases) {
				phases = new HashMap<String, SignalPhases>();
				if (null != scenario.getSignalList())
					for (edu.berkeley.path.beats.jaxb.Signal signal : scenario.getSignalList().getSignal()) {
						if (phases.containsKey(signal.getId()))
							logger.warn("Duplicate signal: id=" + signal.getId());
						phases.put(signal.getId(), new SignalPhases(signal));
					}
				logger.info("Signal phases have been requested");
			}
		}

		public void reset() {
			if (null != links) {
				Iterator<LinkCumulativeData> iter = links.values().iterator();
				while (iter.hasNext()) iter.next().reset();
			}
			if (null != phases) {
				Iterator<SignalPhases> iter = phases.values().iterator();
				while (iter.hasNext()) iter.next().reset();
			}
		}

		public void update() throws BeatsException {
			if (null != links) {
				java.util.Iterator<LinkCumulativeData> iter = links.values().iterator();
				while (iter.hasNext()) iter.next().update();
			}
			if (null != phases) {
				Iterator<SignalPhases> iter = phases.values().iterator();
				while (iter.hasNext()) iter.next().update();
			}
		}

		public LinkCumulativeData get(edu.berkeley.path.beats.jaxb.Link link) throws BeatsException {
			if (null == links) throw new BeatsException("Link cumulative data were not requested");
			return links.get(link.getId());
		}

		public SignalPhases get(edu.berkeley.path.beats.jaxb.Signal signal) throws BeatsException {
			if (null == phases) throw new BeatsException("Signal phases were not requested");
			return phases.get(signal.getId());
		}
	}

	/**
	 * Signal phase storage
	 */
	public static class SignalPhases {
		private edu.berkeley.path.beats.jaxb.Signal signal;
		private List<Signal.PhaseData> phases;

		SignalPhases(edu.berkeley.path.beats.jaxb.Signal signal) {
			this.signal = signal;
			phases = new java.util.ArrayList<Signal.PhaseData>();
		}

		public List<Signal.PhaseData> getPhaseList() {
			return phases;
		}

		void update() {
			phases.addAll(((Signal) signal).getCompletedPhases());
		}

		void reset() {
			phases.clear();
		}
	}
	
}
