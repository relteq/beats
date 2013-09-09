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
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.calibrator.FDCalibrator;
import edu.berkeley.path.beats.data.DataFileReader;
import edu.berkeley.path.beats.data.FiveMinuteData;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import edu.berkeley.path.beats.jaxb.VehicleType;
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
@SuppressWarnings("restriction")
public final class Scenario extends edu.berkeley.path.beats.jaxb.Scenario {

	public static enum UncertaintyType { uniform, gaussian }
	public static enum ModeType { on_init_dens,left_of_init_dens,right_of_init_dens}
	public static enum NodeFlowSolver { proportional , symmetric }
	public static enum NodeSRSolver { A , B , C }
	
	private static Logger logger = Logger.getLogger(Scenario.class);
	private Cumulatives cumulatives;
	private Clock clock;
	private int numVehicleTypes;			// number of vehicle types
	private boolean global_control_on;	// global control switch
	private double global_demand_knob;	// scale factor for all demands
	private edu.berkeley.path.beats.simulator.ControllerSet controllerset = new edu.berkeley.path.beats.simulator.ControllerSet();
	private EventSet eventset = new EventSet();	// holds time sorted list of events	
	private SensorList sensorlist = new SensorList();
	private boolean started_writing;

	private String configfilename;
	private NodeFlowSolver nodeflowsolver = NodeFlowSolver.proportional;
	private NodeSRSolver nodesrsolver = NodeSRSolver.A;

	// Model uncertainty
	private UncertaintyType uncertaintyModel;
	private double std_dev_flow = 0.0d;	// [veh]
	private boolean has_flow_unceratinty;
	
	// data
	private boolean sensor_data_loaded = false;
	
	// run parameters
	private RunParameters runParam;
	private boolean scenario_initialized = false;
	private boolean scenario_locked=false;				// true when the simulation is running
	
	/////////////////////////////////////////////////////////////////////
	// protected constructor
	/////////////////////////////////////////////////////////////////////

	protected Scenario(){}
		
	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////

	protected void populate() throws BeatsException {
		
	    // initialize scenario attributes ..............................................
		this.global_control_on = true;
		this.global_demand_knob = 1d;
		this.uncertaintyModel = UncertaintyType.uniform;
		this.has_flow_unceratinty = BeatsMath.greaterthan(getStd_dev_flow(),0.0);

		this.numVehicleTypes = 1;
		if(getVehicleTypeSet()!=null && getVehicleTypeSet().getVehicleType()!=null)
			numVehicleTypes = getVehicleTypeSet().getVehicleType().size();

		// network list
		if(networkSet!=null)
			for( edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork() )
				((Network) network).populate(this);

		// sensors
		sensorlist.populate(this);
		
		// signals
		if(signalSet!=null)
			for(edu.berkeley.path.beats.jaxb.Signal signal : signalSet.getSignal())
				((Signal) signal).populate(this);
		
		// split ratio profile set (must follow network)
		if(splitRatioSet!=null)
			((SplitRatioSet) splitRatioSet).populate(this);
		
		// boundary capacities (must follow network)
		if(downstreamBoundaryCapacitySet!=null)
			for( edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfile capacityProfile : downstreamBoundaryCapacitySet.getDownstreamBoundaryCapacityProfile() )
				((CapacityProfile) capacityProfile).populate(this);

		if(demandSet!=null)
			((DemandSet) demandSet).populate(this);
		
		// fundamental diagram profiles 
		if(fundamentalDiagramSet!=null)
			for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fd : fundamentalDiagramSet.getFundamentalDiagramProfile())
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

	public static void validate(Scenario S) {
				
		// validate network
		if( S.networkSet!=null)
			for(edu.berkeley.path.beats.jaxb.Network network : S.networkSet.getNetwork())
				((Network)network).validate();

		// sensor list
		S.sensorlist.validate();
		
		// signal list
		if(S.signalSet!=null)
			for (edu.berkeley.path.beats.jaxb.Signal signal : S.signalSet.getSignal())
				((Signal) signal).validate();
		
		// NOTE: DO THIS ONLY IF IT IS USED. IE DO IT IN THE RUN WITH CORRECT FUNDAMENTAL DIAGRAMS
		// validate initial density profile
//		if(getInitialDensityProfile()!=null)
//			((_InitialDensityProfile) getInitialDensityProfile()).validate();

		// validate capacity profiles	
		if(S.downstreamBoundaryCapacitySet!=null)
			for(edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfile capacityProfile : S.downstreamBoundaryCapacitySet.getDownstreamBoundaryCapacityProfile())
				((CapacityProfile)capacityProfile).validate();
		
		// validate demand profiles
		if(S.demandSet!=null)
			((DemandSet)S.demandSet).validate();

		// validate split ratio profiles
		if(S.splitRatioSet!=null)
			((SplitRatioSet)S.splitRatioSet).validate();
		
		// validate fundamental diagram profiles
		if(S.fundamentalDiagramSet!=null)
			for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fd : S.fundamentalDiagramSet.getFundamentalDiagramProfile())
				((FundamentalDiagramProfile)fd).validate();
		
		// validate controllers
		S.controllerset.validate();

	}
	
	/** Prepare scenario for simulation:
	 * set the state of the scenario to the initial condition
	 * sample profiles
	 * open output files
	 * @return success		A boolean indicating whether the scenario was successfuly reset.
	 */
	private void reset() throws BeatsException {
		
		started_writing = false;
		global_control_on = true;
	    global_demand_knob = 1d;
		
		// reset the clock
		clock.reset();
		
		// reset network
		for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork())
			((Network)network).reset();
		
		// sensor list
		sensorlist.reset();
		
		// signal list
		if(signalSet!=null)
			for (edu.berkeley.path.beats.jaxb.Signal signal : signalSet.getSignal())
				((Signal) signal).reset();
						
		// reset demand profiles
		if(demandSet!=null)
			((DemandSet)demandSet).reset();

		// reset fundamental diagrams
		if(fundamentalDiagramSet!=null)
			for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fd : fundamentalDiagramSet.getFundamentalDiagramProfile())
				((FundamentalDiagramProfile)fd).reset();
		
		// reset controllers
		controllerset.reset();

		// reset events
		eventset.reset();

		cumulatives.reset();
		
	}	

	private void update() throws BeatsException {	

        // sample profiles .............................	
    	if(downstreamBoundaryCapacitySet!=null)
        	for(edu.berkeley.path.beats.jaxb.DownstreamBoundaryCapacityProfile capacityProfile : downstreamBoundaryCapacitySet.getDownstreamBoundaryCapacityProfile())
        		((CapacityProfile) capacityProfile).update();

    	if(demandSet!=null)
    		((DemandSet)demandSet).update();

    	if(splitRatioSet!=null)
    		((SplitRatioSet) splitRatioSet).update();        		

    	if(fundamentalDiagramSet!=null)
        	for(edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile fdProfile : fundamentalDiagramSet.getFundamentalDiagramProfile())
        		((FundamentalDiagramProfile) fdProfile).update();
    	
        // update sensor readings .......................
    	sensorlist.update();
		
        // update signals ...............................
		// NOTE: ensembles have not been implemented for signals. They do not apply
		// to pretimed control, but would make a differnece for feedback control. 
		if(signalSet!=null)
			for(edu.berkeley.path.beats.jaxb.Signal signal : signalSet.getSignal())
				((Signal)signal).update();

        // update controllers
    	if(global_control_on)
    		controllerset.update();

    	// update events
    	eventset.update();
    	
        // update the network state......................
		for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork())
			((Network) network).update();

		cumulatives.update();
		
		// advance the clock
    	clock.advance();

	}

	/////////////////////////////////////////////////////////////////////
	// initialization
	/////////////////////////////////////////////////////////////////////
	
	public void initialize(double timestep,double starttime,double endtime, double outdt, String outtype,String outprefix, int numReps, int numEnsemble) throws BeatsException {
		
		// create run parameters object
		boolean writeoutput = true;		
		runParam = new RunParameters( timestep, 
									  starttime,
									  endtime,
									  outdt,
									  writeoutput,
									  outtype,
									  outprefix,
									  numReps,
									  numEnsemble );
		
		// validate the run parameters
		runParam.validate();
		
		// lock the scenario
		scenario_locked = true;	
		
		// populate and validate the scenario
		populate_validate();
		
		// compute the initial state by running the simulator to the start time
		assign_initial_state();

		// it's initialized
        scenario_initialized = true;
        

	}
	
	/**
	 * Processes a scenario loaded by JAXB.
	 * Converts units to SI, populates the scenario,
	 * registers signals and controllers,
	 * and validates the scenario.
	 * @param S a scenario
	 * @return the updated scenario or null if an error occurred
	 * @throws BeatsException
	 */
	protected void populate_validate() throws BeatsException {
		
		if (null == getSettings() || null == getSettings().getUnits())
			logger.warn("Scenario units not specified. Assuming SI");
		else if (!"SI".equalsIgnoreCase(getSettings().getUnits())) {
			logger.info("Converting scenario units from " + getSettings().getUnits() + " to SI");
			edu.berkeley.path.beats.util.UnitConverter.process(this);
		}

	    // populate the scenario ....................................................
	    populate();

	    // register signals with their targets ..................................
	    boolean registersuccess = true;
		if(getSignalSet()!=null)
	    	for(edu.berkeley.path.beats.jaxb.Signal signal: getSignalSet().getSignal())
	    		registersuccess &= ((Signal)signal).register();
	    if(!registersuccess){
	    	throw new BeatsException("Signal registration failure");
	    }

	    if(getControllerset()!=null)
	    	if(!getControllerset().register()){
	    		throw new BeatsException("Controller registration failure");
		    }

	    // print messages and clear before validation
		if (BeatsErrorLog.hasmessage()) {
			BeatsErrorLog.print();
			BeatsErrorLog.clearErrorMessage();
		}

		// validate scenario ......................................
	    Scenario.validate(this);
	    	    
		if(BeatsErrorLog.haserror()){
			BeatsErrorLog.print();
			throw new ScenarioValidationError();
		}
		
		if(BeatsErrorLog.haswarning()) {
			BeatsErrorLog.print();
			BeatsErrorLog.clearErrorMessage();
		}

	}
	
	/////////////////////////////////////////////////////////////////////
	// start-to-end run
	/////////////////////////////////////////////////////////////////////

	public void run() throws BeatsException{

		logger.info("Simulation period: [" + runParam.t_start_output + ":" + runParam.dt_sim + ":" + runParam.t_end_output + "]");
		logger.info("Output period: [" + runParam.t_start_output + ":" + runParam.dt_output + ":" + runParam.t_end_output + "]");
		
		// output writer properties
		Properties owr_props = new Properties();
		if (null != runParam.outprefix) 
			owr_props.setProperty("prefix", runParam.outprefix);
		if (null != runParam.outtype) 
			owr_props.setProperty("type",runParam.outtype);
		
		// loop through simulation runs ............................
		for(int i=0;i<runParam.numReps;i++){
			
			OutputWriterBase outputwriter = null;
			if (runParam.writefiles){
				outputwriter = OutputWriterFactory.getWriter(this, owr_props, runParam.dt_output,runParam.outsteps);
				outputwriter.open(i);
			}
			
			try{
				// reset the simulation
				reset();
				
				// advance to end of simulation
				while( advanceNSteps_internal(1,runParam.writefiles,outputwriter,runParam.t_start_output) ){					
				}
			} finally {
				if (null != outputwriter) outputwriter.close();
			}
		}
        scenario_locked = false;		
		
	}

	/////////////////////////////////////////////////////////////////////
	// step-by-step run
	/////////////////////////////////////////////////////////////////////

	/** Advance the simulation <i>nsec</i> seconds.
	 * 
	 * <p> Move the simulation forward <i>nsec</i> seconds and stops.
	 * Returns <code>true</code> if the operation completes succesfully. Returns <code>false</code>
	 * if the end of the simulation is reached.
	 * @param nsec Number of seconds to advance.
	 * @throws BeatsException 
	 */
	public boolean advanceNSeconds(double nsec) throws BeatsException{	
		
		if(!scenario_locked)
			throw new BeatsException("Run not initialized. Use initialize_run() first.");
		
		if(!BeatsMath.isintegermultipleof(nsec,runParam.dt_sim))
			throw new BeatsException("nsec (" + nsec + ") must be an interger multiple of simulation dt (" + runParam.dt_sim + ").");
		int nsteps = BeatsMath.round(nsec/runParam.dt_sim);				
		return advanceNSteps_internal(nsteps,false,null,-1d);
	}

	/////////////////////////////////////////////////////////////////////
	// protected simple getters and setters
	/////////////////////////////////////////////////////////////////////

	protected edu.berkeley.path.beats.simulator.ControllerSet getControllerset() {
		return controllerset;
	}

	protected void setConfigfilename(String configfilename) {
		this.configfilename = configfilename;
	}

	protected void setNodeFlowSolver(String nodeflowsolver) {
		this.nodeflowsolver = NodeFlowSolver.valueOf(nodeflowsolver);
	}

	protected void setNodeSRSolver(String nodesrsolver) {
		this.nodesrsolver = NodeSRSolver.valueOf(nodesrsolver);
	}
	
	protected void setGlobal_control_on(boolean global_control_on) {
		this.global_control_on = global_control_on;
	}

	protected void setGlobal_demand_knob(double global_demand_knob) {
		this.global_demand_knob = global_demand_knob;
	}

	/////////////////////////////////////////////////////////////////////
	// protected complex getters
	/////////////////////////////////////////////////////////////////////

	/** Retrieve a network with a given id.
	 * @param id The string id of the network
	 * @return The corresponding network if it exists, <code>null</code> otherwise.
	 * 
	 */
	protected Network getNetworkWithId(long id){
		if(networkSet==null)
			return null;
		if(networkSet.getNetwork()==null)
			return null;
		if(networkSet.getNetwork().size()>1)
			return null;
		for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork()){
			if(network.getId()==id)
				return (Network) network;
		}
		return null;
	}

//	protected Integer [] getVehicleTypeIndices(edu.berkeley.path.beats.jaxb.VehicleTypeOrder vtypeorder){
//		
//		Integer [] vehicletypeindex;
//		
//		// single vehicle types in setting and no vtypeorder, return 0
//		if(vtypeorder==null && numVehicleTypes==1){
//			vehicletypeindex = new Integer[numVehicleTypes];
//			vehicletypeindex[0]=0;
//			return vehicletypeindex;
//		}
//		
//		// multiple vehicle types in setting and no vtypeorder, return 0...n
//		if(vtypeorder==null && numVehicleTypes>1){
//			vehicletypeindex = new Integer[numVehicleTypes];
//			for(int i=0;i<numVehicleTypes;i++)
//				vehicletypeindex[i] = i;	
//			return vehicletypeindex;	
//		}
//		
//		// vtypeorder is not null
//		int numTypesInOrder = vtypeorder.getVehicleTypeX().size();
//		int i,j;
//		vehicletypeindex = new Integer[numTypesInOrder];
//		for(i=0;i<numTypesInOrder;i++)
//			vehicletypeindex[i] = -1;			
//
//		if(getSettings()==null)
//			return vehicletypeindex;
//
//		if(getSettings().getVehicleTypes()==null)
//			return vehicletypeindex;
//		
//		for(i=0;i<numTypesInOrder;i++){
//			String vtordername = vtypeorder.getVehicleTypeX().get(i).getName();
//			List<edu.berkeley.path.beats.jaxb.VehicleTypeX> settingsname = getSettings().getVehicleTypes().getVehicleTypeX();
//			for(j=0;j<settingsname.size();j++){
//				if(settingsname.get(j).getName().equals(vtordername)){
//					vehicletypeindex[i] =  j;
//					break;
//				}
//			}			
//		}
//		return vehicletypeindex;
//	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

	// seriallization .................................................
	
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

	public UncertaintyType getUncertaintyModel() {
		return uncertaintyModel;
	}

	public boolean isGlobal_control_on() {
		return global_control_on;
	}

	public double getGlobal_demand_knob() {
		return global_demand_knob;
	}

	public double getStd_dev_flow() {
		return std_dev_flow;
	}
	
	public boolean isHas_flow_unceratinty() {
		return has_flow_unceratinty;
	}

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
		return runParam.numEnsemble;
	}

	/** Vehicle type index from name
	 * @return integer index of the vehicle type.
	 */
	public int getVehicleTypeIndexForName(String name){
		if(name==null)
			return -1;
		if(getVehicleTypeSet()==null)
			return 0;
		if(getVehicleTypeSet().getVehicleType()==null)
			return 0;
		for(int i=0;i<getVehicleTypeSet().getVehicleType().size();i++)
			if(getVehicleTypeSet().getVehicleType().get(i).getName().equals(name))
				return i;
		return -1;
	}

	/** Vehicle type index from id
	 * @return integer index of the vehicle type.
	 */
	public int getVehicleTypeIndexForId(long id){
		if(name==null)
			return -1;
		if(getVehicleTypeSet()==null)
			return 0;
		if(getVehicleTypeSet().getVehicleType()==null)
			return 0;
		for(int i=0;i<getVehicleTypeSet().getVehicleType().size();i++)
			if(getVehicleTypeSet().getVehicleType().get(i).getId()==id)
				return i;
		return -1;
	}
	
	/** Size of the simulation time step in seconds.
	 * @return Simulation time step in seconds. 
	 */
	public double getSimdtinseconds() {
		return runParam.dt_sim;
	}

	/** Start time of the simulation.
	 * @return Start time in seconds. 
	 */
	public double getTimeStart() {
		if(clock==null)
			return Double.NaN;
		return this.clock.getStartTime();
	}

	/** End time of the simulation.
	 * @return End time in seconds. 
	 * @return			XXX
	 */
	public double getTimeEnd() {
		if(clock==null)
			return Double.NaN;
		return this.clock.getEndTime();
	}
	
	/** Get configuration file name */
	public String getConfigFilename() {
		return configfilename;
	}
	
	public NodeFlowSolver getNodeFlowSolver(){
		return this.nodeflowsolver;
	}

	public NodeSRSolver getNodeSRSolver(){
		return this.nodesrsolver;
	}
	
	// array getters ........................................................
	
	/** Vehicle type names.
	 * @return	Array of strings with the names of the vehicles types.
	 */
	public String [] getVehicleTypeNames(){
		String [] vehtypenames = new String [numVehicleTypes];
		if(getVehicleTypeSet()==null || getVehicleTypeSet().getVehicleType()==null)
			vehtypenames[0] = Defaults.vehicleType;
		else
			for(int i=0;i<numVehicleTypes;i++)
				vehtypenames[i] = getVehicleTypeSet().getVehicleType().get(i).getName();
		return vehtypenames;
	}
	
//	/** Get the initial density state for the network with given id.
//	 * @param network_id String id of the network
//	 * @return A two-dimensional array of doubles where the first dimension is the
//	 * link index (ordered as in {@link Network#getListOfLinks}) and the second is the vehicle type 
//	 * (ordered as in {@link Scenario#getVehicleTypeNames})
//	 */
//	public double [][] getInitialDensityForNetwork(long network_id){
//
//		Network network = getNetworkWithId(network_id);
//		if(network==null)
//			return null;
//		
//		double [][] density = new double [network.getLinkList().getLink().size()][getNumVehicleTypes()];
//		InitialDensitySet initprofile = (InitialDensitySet) getInitialDensitySet();
//
//		int i,j;
//		for(i=0;i<network.getLinkList().getLink().size();i++){
//			if(initprofile==null){
//				for(j=0;j<numVehicleTypes;j++)
//					density[i][j] = 0d;
//			}
//			else{
//				edu.berkeley.path.beats.jaxb.Link link = network.getLinkList().getLink().get(i);
//				Double [] init_density = initprofile.getDensityForLinkIdInVeh(network.getId(),link.getId());
//				for(j=0;j<numVehicleTypes;j++)
//					density[i][j] = init_density[j];
//			}
//		}
//		return density;                         
//	}

	/** Get the current density state for the network with given id.
	 * @param network_id String id of the network
	 * @return A two-dimensional array of doubles where the first dimension is the
	 * link index (ordered as in {@link Network#getListOfLinks}) and the second is the vehicle type 
	 * (ordered as in {@link Scenario#getVehicleTypeNames})
	 */
	public double [][] getDensityForNetwork(long network_id,int ensemble){
		
		if(ensemble<0 || ensemble>=runParam.numEnsemble)
			return null;
		Network network = getNetworkWithId(network_id);
		if(network==null)
			return null;
		
		double [][] density = new double [network.getLinkList().getLink().size()][getNumVehicleTypes()];

		int i,j;
		for(i=0;i<network.getLinkList().getLink().size();i++){
			Link link = (Link) network.getLinkList().getLink().get(i);
			double [] linkdensity = link.getDensityInVeh(ensemble);
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

	public Cumulatives getCumulatives() {
		return cumulatives;
	}

	public Clock getClock() {
		return clock;
	}
	
	/** Get a reference to a link by its composite id.
	 * 
	 * @param id String id of the link. 
	 * @return Reference to the link if it exists, <code>null</code> otherwise
	 */
	public Link getLinkWithId(long id){
		if(networkSet==null)
			return null;
		for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork()){
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
	public Node getNodeWithId(long id){
		if(networkSet==null)
			return null;
		for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork()){
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
	public Controller getControllerWithId(long id){
		if(controllerset==null)
			return null;
		for(Controller c : controllerset.get_Controllers()){
			if(c.getId()==id)
				return c;
		}
		return null;
	}
	
	/** Get a reference to an event by its id.
	 * @param id Id of the event.
	 * @return A reference to the event if it exists, <code>null</code> otherwise.
	 */
	public Event getEventWithId(long id){
		if(eventset==null)
			return null;
		for(Event e : eventset.getSortedevents()){
			if(e.getId()==id)
				return e;
		}
		return null;
	}		

	/** Get sensor with given id.
	 * @param id String id of the sensor.
	 * @return Sensor object.
	 */
	public Sensor getSensorWithId(long id){
		if(sensorSet==null)
			return null;
		for(edu.berkeley.path.beats.simulator.Sensor sensor :sensorlist.getSensors()){
			if(sensor.getId()==id)
				return sensor;
		}
		return null;
	}
	
	/** Get signal with given id.
	 * @param id String id of the signal.
	 * @return Signal object.
	 */
	public Signal getSignalWithId(long id){
		if(signalSet==null)
			return null;
		for(edu.berkeley.path.beats.jaxb.Signal signal : signalSet.getSignal()){
			if(signal.getId()==id)
				return (Signal) signal;
		}
		return null;
	}
	
	/** Get a reference to a signal by the id of its node.
	 * 
	 * @param node_id String id of the node. 
	 * @return Reference to the signal if it exists, <code>null</code> otherwise
	 */
	public Signal getSignalWithNodeId(long node_id){
		if(signalSet==null)
			return null;
		for(edu.berkeley.path.beats.jaxb.Signal signal : signalSet.getSignal()){
			if(signal.getNodeId()==node_id)
				return (Signal)signal;
		}
		return null;
	}
		
	/////////////////////////////////////////////////////////////////////
	// scenario modification
	/////////////////////////////////////////////////////////////////////

	/** Add a controller to the scenario.
	 * 
	 * <p>Controllers can only be added if a) the scenario is not currently running, and
	 * b) the controller is valid. 
	 * @param C The controller
	 * @return <code>true</code> if the controller was successfully added, <code>false</code> otherwise. 

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
		 */

	/** Add an event to the scenario.
	 * 
	 * <p>Events are not added if the scenario is running. This method does not validate the event.
	 * @param E The event
	 * @return <code>true</code> if the event was successfully added, <code>false</code> otherwise. 
	 */
	public boolean addEvent(Event E){
		if(scenario_locked)
			return false;
		if(E==null)
			return false;
		if(E.getMyType()==null)
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
		
		if(scenario_locked)
			throw new BeatsException("Cannot modify the scenario while it is locked.");

		if(demandSet==null){
			demandSet = new edu.berkeley.path.beats.jaxb.DemandSet();
			@SuppressWarnings("unused")
			List<DemandProfile> temp = demandSet.getDemandProfile(); // artifficially initialize the profile			
		}
		
		// validate the profile
		BeatsErrorLog.clearErrorMessage();
		dem.validate();
		if(BeatsErrorLog.haserror())
			throw new BeatsException(BeatsErrorLog.format());
		
		// replace an existing profile
		boolean foundit = false;
		for(int i=0;i<demandSet.getDemandProfile().size();i++){
			edu.berkeley.path.beats.jaxb.DemandProfile d = demandSet.getDemandProfile().get(i);
			if(d.getLinkIdOrg()==dem.getLinkIdOrg()){
				demandSet.getDemandProfile().set(i,dem);
				foundit = true;
				break;
			}
		}
		
		// or add a new one
		if(!foundit)
			demandSet.getDemandProfile().add(dem);

	}
	
	/////////////////////////////////////////////////////////////////////
	// calibration
	/////////////////////////////////////////////////////////////////////
	
	public void loadSensorData() throws BeatsException {

		if(sensorlist.getSensors().isEmpty())
			return;
		
		if(sensor_data_loaded)
			return;

		HashMap <Integer,FiveMinuteData> data = new HashMap <Integer,FiveMinuteData> ();
		ArrayList<DataSource> datasources = new ArrayList<DataSource>();
		ArrayList<String> uniqueurls  = new ArrayList<String>();
		
		// construct list of stations to extract from datafile 
		for(Sensor sensor : sensorlist.getSensors()){
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
		for(Sensor sensor : sensorlist.getSensors()){
			
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
	
	private void assign_initial_state() throws BeatsException {
		
		// initial density set time stamp
        double time_ic = getInitialDensitySet()!=null ? getInitialDensitySet().getTstamp() : Double.POSITIVE_INFINITY;  // [sec]        
		
		// determine the simulation mode and sim_start time
        double sim_start; 
        ModeType simulationMode = null;
		if(BeatsMath.equals(runParam.t_start_output,time_ic)){
			sim_start = runParam.t_start_output;
			simulationMode = ModeType.on_init_dens;
		}
		else{
			// it is a warmup. we need to decide on start and end times
			if(BeatsMath.lessthan(time_ic, runParam.t_start_output) ){	// go from ic to timestart
				sim_start = time_ic;
				simulationMode = ModeType.right_of_init_dens;
			}
			else{							
				
				// find earliest demand profile ...
				double demand_start = Double.POSITIVE_INFINITY;
				if(demandSet!=null)
					for(edu.berkeley.path.beats.jaxb.DemandProfile D : demandSet.getDemandProfile())
						demand_start = Math.min(demand_start,D.getStartTime());					
				if(Double.isInfinite(demand_start))
					demand_start = 0d;
				
				// ... start simulation there or at output start time
				sim_start = Math.min(runParam.t_start_output,demand_start);
				simulationMode = ModeType.left_of_init_dens;
				
			}		
		}		
		
		// what to do in each case
		boolean use_initial_density_profile;
		boolean run_singleton_to_start_output;
		switch(simulationMode){
		case left_of_init_dens:
			use_initial_density_profile = false;
			run_singleton_to_start_output = true;
			break;
		case on_init_dens:
			use_initial_density_profile = true;
			run_singleton_to_start_output = false;
			break;
		case right_of_init_dens:
			use_initial_density_profile = true;
			run_singleton_to_start_output = true;
			break;
		default:
			throw new BeatsException("bad case.");
		}
				
		// initial density
		if(use_initial_density_profile && getInitialDensitySet()!=null){
			for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork())
				for(edu.berkeley.path.beats.jaxb.Link jlink:network.getLinkList().getLink()){
					double [] density = ((InitialDensitySet)getInitialDensitySet()).getDensityForLinkIdInVeh(network.getId(),jlink.getId());
					if(density!=null)
						((Link) jlink).set_initial_state(density);
					else
						((Link) jlink).set_initial_state(BeatsMath.zeros(numVehicleTypes));
				}
		}
		else {
			for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork())
				for(edu.berkeley.path.beats.jaxb.Link jlink:network.getLinkList().getLink())
					((Link) jlink).set_initial_state(BeatsMath.zeros(numVehicleTypes));
		}
			
		// run the density forward
		clock = new Clock(sim_start,runParam.t_end_output,runParam.dt_sim);		
		if(run_singleton_to_start_output){
			
	        // advance a point ensemble to start_output time
	        int original_numEnsemble = runParam.numEnsemble;
	        runParam.numEnsemble = 1;
	        
			// reset the simulation (copy initial_density to density)
			reset();
	        
	        // advance to start of output time  		
	        while( BeatsMath.lessthan(getCurrentTimeInSeconds(),runParam.t_start_output) )
	        	update();

			// copy the result to the initial density
			for(edu.berkeley.path.beats.jaxb.Network network : networkSet.getNetwork())
				for(edu.berkeley.path.beats.jaxb.Link link:network.getLinkList().getLink())
					((Link) link).copy_state_to_initial_state();
			
	        // revert numEnsemble
	        runParam.numEnsemble = original_numEnsemble;
		}
		
        
	}
	
	private boolean advanceNSteps_internal(int n,boolean writefiles,OutputWriterBase outputwriter,double outStart) throws BeatsException{
		
		// advance n steps
		for(int k=0;k<n;k++){

			// export initial condition
	        if(!started_writing && BeatsMath.equals(clock.getT(),outStart) ){
	        	recordstate(writefiles,outputwriter,false);
	        	started_writing = true;
	        }
        	
        	// update scenario
        	update();

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
	// public static
	/////////////////////////////////////////////////////////////////////	
	
	// returns greatest common divisor among network time steps.
	// The time steps are rounded to the nearest decisecond.
//	private static double computeCommonSimulationTimeInSeconds(Scenario scenario){
//		
//		if(scenario.getNetworkSet()==null)
//			return Double.NaN;
//		
//		if(scenario.getNetworkSet().getNetwork().size()==0)
//			return Double.NaN;
//			
//		// loop through networks calling gcd
//		double dt;
//		List<edu.berkeley.path.beats.jaxb.Network> networkSet = scenario.getNetworkSet().getNetwork();
//		int tengcd = 0;		// in deciseconds
//		for(int i=0;i<networkSet.size();i++){
//			dt = networkSet.get(i).getDt().doubleValue();	// in seconds
//	        if( BeatsMath.lessthan( Math.abs(dt) ,0.1) ){
//	        	BeatsErrorLog.addError("Warning: Network dt given in hours. Changing to seconds.");
//				dt *= 3600;
//	        }
//			tengcd = BeatsMath.gcd( BeatsMath.round(dt*10.0) , tengcd );
//		}
//    	return ((double)tengcd)/10.0;
//	}
	
	/////////////////////////////////////////////////////////////////////
	// private classes
	/////////////////////////////////////////////////////////////////////	
	
	private class RunParameters{
		
		// prescribed
		public double dt_sim;				// [sec] simulation time step
		//public double sim_start;			// [sec] start of the simulation
		public double t_start_output;		// [sec] start outputing data
		public double t_end_output;			// [sec] end of the simulation
		public double dt_output;				// [sec] output sampling time
		
		public boolean writefiles;
		public String outtype;
		public String outprefix;
		public int numReps;
		public int numEnsemble;

		// derived
		public int outsteps;				// [-] number of simulation steps per output step
//		public ModeType simulationMode;
		  
		// input parameter outdt [sec] output sampling time
		public RunParameters(double simdt,double tstart,double tend,double outdt, boolean writefiles,String outtype,String outprefix,int numReps,int numEnsemble) throws BeatsException{
			
			// round to the nearest decisecond
			simdt = round(simdt);
			tstart = round(tstart);
			tend = round(tend);
			outdt = round(outdt);
			
			this.dt_sim = simdt;
			this.t_start_output = tstart;
			this.t_end_output = tend;
	        this.outsteps = BeatsMath.round(outdt/simdt);
			this.dt_output = outsteps*simdt;

			this.writefiles = writefiles;
			this.outtype = outtype;
			this.outprefix = outprefix;
			this.numReps = numReps;
			this.numEnsemble = numEnsemble;
						
		}
		
		public void validate() throws BeatsException{

			// check simdt non-negative
			if( BeatsMath.lessthan(dt_sim,0d))
				throw new BeatsException("Negative time step.");
			
			// check tstart non-negative
			if( BeatsMath.lessthan(t_start_output,0d))
				throw new BeatsException("Negative start time.");

			// check timestart < timeend
			if( BeatsMath.greaterorequalthan(t_start_output,t_end_output))
				throw new BeatsException("Empty simulation period.");

			// check that outdt is a multiple of simdt
			if(!Double.isNaN(dt_output) && !BeatsMath.isintegermultipleof(dt_output,dt_sim))
				throw new BeatsException("outdt (" + dt_output + ") must be an interger multiple of simulation dt (" + dt_sim + ").");
					
			// initial density set time stamp
	        double time_ic = getInitialDensitySet()!=null ? getInitialDensitySet().getTstamp() : Double.POSITIVE_INFINITY;  // [sec]        
	        
	        // check values
	        if(BeatsMath.lessthan(t_start_output,time_ic) && BeatsMath.lessthan(time_ic,t_end_output))
				throw new BeatsException("Illegal start/end time: start<i.c.<end is not allowed");
	        
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
		java.util.Map<Long, LinkCumulativeData> links = null;

		/** signal id -> completed phases */
		java.util.Map<Long, SignalPhases> phases = null;

		private static Logger logger = Logger.getLogger(Cumulatives.class);

		public Cumulatives(Scenario scenario) {
			this.scenario = scenario;
		}

		public void storeLinks() {
			if (null == links) {
				links = new java.util.HashMap<Long, LinkCumulativeData>();
				for (edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkSet().getNetwork()){
					if(((edu.berkeley.path.beats.simulator.Network) network).isIsempty())
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
				phases = new HashMap<Long, SignalPhases>();
				if (null != scenario.getSignalSet())
					for (edu.berkeley.path.beats.jaxb.Signal signal : scenario.getSignalSet().getSignal()) {
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

	public void setUncertaintyModel(Scenario.UncertaintyType uncertaintyModel) {
		this.uncertaintyModel = uncertaintyModel;
	}
	
}
