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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import edu.berkeley.path.beats.control.*;
import edu.berkeley.path.beats.event.*;
import edu.berkeley.path.beats.sensor.*;

/** Factory methods for creating scenarios, controllers, events, sensors, and scenario elements. 
 * <p>
 * Use the static methods in this class to load a scenario and to programmatically generate events, controllers, sensors, and scenario elements.
 * 
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
final public class ObjectFactory {

	private static Logger logger = Logger.getLogger(ObjectFactory.class);
	
	/////////////////////////////////////////////////////////////////////
	// private default constructor
	/////////////////////////////////////////////////////////////////////

	private ObjectFactory(){}
							  
	/////////////////////////////////////////////////////////////////////
	// protected create from Jaxb
	/////////////////////////////////////////////////////////////////////
	
	/** @y.exclude */
	protected static Controller createControllerFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Controller jaxbC,Controller.Type myType) {		
		if(myScenario==null)
			return null;
		Controller C;
		switch(myType){
			case IRM_ALINEA:
				C = new Controller_IRM_Alinea(myScenario, jaxbC, myType);
				break;
				
			case IRM_TOD:
				C = new Controller_IRM_Time_of_Day(myScenario, jaxbC, myType);
				break;
				
			case IRM_TOS:
				C = new Controller_IRM_Traffic_Responsive(myScenario, jaxbC, myType);
				break;
				
			case CRM_HERO:
				C = new Controller_CRM_HERO(myScenario, jaxbC, myType);
				break;

			case CRM_MPC:
				C = new Controller_CRM_MPC(myScenario, jaxbC, myType);
				break;
				
			case SIG_Pretimed:
				C = new Controller_SIG_Pretimed(myScenario, jaxbC, myType);
				break;
				
			default:
				C = null;
				break;
		}
		C.populate(jaxbC);
		return C;

	}

	/** @y.exclude */
	protected static Event createEventFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Event jaxbE,Event.Type myType) {	
		if(myScenario==null)
			return null;
		Event E;
		switch(myType){
			case fundamental_diagram:
				E = new Event_Fundamental_Diagram(myScenario, jaxbE, myType);
				break;

			case link_demand_knob:
				E = new Event_Link_Demand_Knob(myScenario, jaxbE, myType);
				break;

			case link_lanes:
				E = new Event_Link_Lanes(myScenario, jaxbE, myType);
				break;

			case node_split_ratio:
				E = new Event_Node_Split_Ratio(myScenario, jaxbE, myType);
				break;

			case control_toggle:
				E = new Event_Control_Toggle(myScenario, jaxbE, myType);
				break;

			case global_control_toggle:
				E = new Event_Global_Control_Toggle(myScenario, jaxbE, myType);
				break;

			case global_demand_knob:
				E = new Event_Global_Demand_Knob(myScenario, jaxbE, myType);
				break;
				
			default:
				E = null;
				break;
		}
		E.populate(jaxbE);
		return E;
	}

	/** @y.exclude */
	protected static Sensor createSensorFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Sensor jaxbS,Sensor.Type myType) {	
		if(myScenario==null)
			return null;
		Sensor S;
		switch(myType){
			case loop:
				S = new SensorLoopStation(myScenario, jaxbS, myType);
				break;

			default:
				S = null;
				break;
		}
		S.populate(jaxbS);
		return S;
	}
	
	/** @y.exclude */
	protected static ScenarioElement createScenarioElementFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.ScenarioElement jaxbS){
		if(myScenario==null)
			return null;
		ScenarioElement S = new ScenarioElement();
		S.myScenario = myScenario;
		S.setId(jaxbS.getId().trim());
		S.myType = ScenarioElement.Type.valueOf(jaxbS.getType());
		switch(S.myType){
		case link:
			S.reference = myScenario.getLinkWithId(S.getId());
			break;
		case node:
			S.reference = myScenario.getNodeWithId(S.getId());
			break;
		case sensor:
			S.reference = myScenario.getSensorWithId(S.getId());
			break;
		case signal:
			S.reference = myScenario.getSignalWithId(S.getId());
			break;
		case controller:
			S.reference = myScenario.getControllerWithId(S.getId());
			break;
			
		default:
			S.reference = null;	
		}
		return S;
	}
	  
	/////////////////////////////////////////////////////////////////////
	// Scenario 
	/////////////////////////////////////////////////////////////////////

	/** Loads and validates scenarios from XML. 
	 * <p>
	 * This method does the following,
	 * <ol>
	 * <li> Unmarshalls the configuration file to populate JAXB objects, </li>
	 * <li> Determines the simulation mode (see below),</li>
	 * <li> Registers controllers with their targets (calls to InterfaceController.register()), </li>
	 * <li> Validates the scenario and all its components (calls to validate() on all scenario components). </li>
	 * </ol>
	 * <p>
	 * The simulation mode can be <i>normal</i>, <i>warmup from initial condition</i>, or <i> warmup from zero density</i>, 
	 * depending on the values of <code>timestart</code>, <code>timeend</code>, and the time stamp on the initial density profile (time_ic). In the <i>normal</i> mode,
	 * the simulator initializes the network with densities provided in the initial density profile, and produces as output the evolution of the density
	 * state from <code>timestart</code> to <code>timeend</code>. The warmup modes are executed whenever <code>timestart</code> does not match the timestamp of the initial density profile. 
	 * In these modes the objective is to generate a configuration file with an initial density profile corresponding to <code>timestart</code>. If <code>timestart</code>&gt time_ic, 
	 * the network is initialized with the given initial density profile and run from time_ic to <code>timestart</code>. If <code>timestart</code>&lt time_ic, the simulation is
	 * is initialized with zero density and run from the earliest timestamp of all demand profiles (timestart_demand) to <code>timestart</code>, assuming timestart_demand&lt<code>timestart</code>.
	 * If <code>timestart</code>&lt time_ic and timestart_demand&gt<code>timestart</code>, an error is produced.
	 * <p>
	 * <table border="1">
	 * <tr> <th>Simulation mode</th>   <th>Condition</th> 			 		 		<th>Initial condition</th>			<th>Start time</th> 			<th>End time</th> 				<th>Output</th>	</tr>
	 * <tr> <td>normal</td>			   <td><code>timestart</code>==time_ic</td>	 	<td>initial density profile</td>	<td><code>timestart</code></td>	<td><i>timeend</i></td>			<td>state</td>	</tr>
	 * <tr> <td> warmup from ic	</td>  <td><code>timestart</code>&gt time_ic</td>	<td>initial density profile</td>	<td>time_ic</td>				<td><code>timestart</code></td>	<td>configuration file</td>	</tr>
	 * <tr> <td> warmup from zero</td> <td><code>timestart</code>&lt time_ic</td>  	<td>zero density</td>				<td>timestart_demand</td>		<td><code>timestart</code></td>	<td>configuration file</td>	</tr>
	 * </table> 
	 * 
	 * @param configfilename		The name of the XML configuration file.
	 * @return scenario				Scenario object.
	 * @throws BeatsException
	 */
	public static Scenario createAndLoadScenario(String configfilename) throws BeatsException {

		JAXBContext context;
		Unmarshaller u;

		BeatsErrorLog.clearErrorMessage();
		
    	// create unmarshaller .......................................................
        try {
		//Reset the classloader for main thread; need this if I want to run properly
  		//with JAXB within MATLAB. (luis)
		Thread.currentThread().setContextClassLoader(ObjectFactory.class.getClassLoader());
        	context = JAXBContext.newInstance("edu.berkeley.path.beats.jaxb");
	        u = context.createUnmarshaller();
        } catch( JAXBException je ) {
        	throw new BeatsException("Failed to create context for JAXB unmarshaller", je);
        }
        
        // schema assignment ..........................................................
        try{
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            ClassLoader classLoader = ObjectFactory.class.getClassLoader();            
        	Schema schema = factory.newSchema(classLoader.getResource("sirius.xsd"));
        	u.setSchema(schema);
        } catch(SAXException e){
        	throw new BeatsException("Schema not found", e);
        }
        
        // process configuration file name ...........................................
		if(!configfilename.endsWith(".xml"))
			configfilename += ".xml";

        // read and return ...........................................................
        Scenario S = new Scenario();
        try {
        	setObjectFactory(u, new JaxbObjectFactory());
        	S = (Scenario) u.unmarshal( new FileInputStream(configfilename) );
        } catch( JAXBException je ) {
        	throw new BeatsException("JAXB threw an exception when loading the configuration file", je);
        } catch (FileNotFoundException e) {
        	throw new BeatsException("Configuration file not found", e);
		}
        
        if(S==null){
        	throw new BeatsException("Unknown load error");
		}

		// check the scenario schema version
		edu.berkeley.path.beats.util.ScenarioUtil.checkSchemaVersion(S);

        // copy in input parameters ..................................................
        S.configfilename = configfilename;

		return process(S);
	}

	/**
	 * Updates a scenario loaded by JAXB.
	 * Converts units to SI, populates the scenario,
	 * registers signals and controllers,
	 * and validates the scenario.
	 * @param S a scenario
	 * @return the updated scenario or null if an error occurred
	 * @throws BeatsException
	 */
	public static Scenario process(Scenario S) throws BeatsException {
		
		if (null == S.getSettings() || null == S.getSettings().getUnits())
			logger.warn("Scenario units not specified. Assuming SI");
		else if (!"SI".equalsIgnoreCase(S.getSettings().getUnits())) {
			logger.info("Converting scenario units from " + S.getSettings().getUnits() + " to SI");
			edu.berkeley.path.beats.util.UnitConverter.process(S);
		}

	    // copy data to static variables ..............................................
	    S.global_control_on = true;
	    S.global_demand_knob = 1d;
	    S.simdtinseconds = computeCommonSimulationTimeInSeconds(S);
	    S.uncertaintyModel = Scenario.UncertaintyType.uniform;
	    S.numVehicleTypes = 1;
	    S.has_flow_unceratinty = BeatsMath.greaterthan(S.std_dev_flow,0.0);
	    
	    if(S.getSettings()!=null)
	        if(S.getSettings().getVehicleTypes()!=null)
	            if(S.getSettings().getVehicleTypes().getVehicleType()!=null) 
	        		S.numVehicleTypes = S.getSettings().getVehicleTypes().getVehicleType().size();

	    // populate the scenario ....................................................
	    S.populate();

	    // register signals with their targets ..................................
	    boolean registersuccess = true;
		if(S.getSignalList()!=null)
	    	for(edu.berkeley.path.beats.jaxb.Signal signal:S.getSignalList().getSignal())
	    		registersuccess &= ((Signal)signal).register();
	    if(!registersuccess){
	    	throw new BeatsException("Signal registration failure");
	    }

	    if(S.controllerset!=null)
	    	if(!S.controllerset.register()){
	    		throw new BeatsException("Controller registration failure");
		    }

	    // print messages and clear before validation
		if (BeatsErrorLog.hasmessage()) {
			BeatsErrorLog.print();
			BeatsErrorLog.clearErrorMessage();
		}

		// validate scenario ......................................
	    Scenario.validate(S);
	    	    
		if(BeatsErrorLog.haserror())
			throw new ScenarioValidationError();
		
		if(BeatsErrorLog.haswarning()) {
			BeatsErrorLog.print();
			BeatsErrorLog.clearErrorMessage();
		}

		return S;
	}

	public static void setObjectFactory(Unmarshaller unmrsh, Object factory) throws PropertyException {
		final String classname = unmrsh.getClass().getName();
		String propnam = classname.startsWith("com.sun.xml.internal") ?//
				"com.sun.xml.internal.bind.ObjectFactory" ://
				"com.sun.xml.bind.ObjectFactory";
		unmrsh.setProperty(propnam, factory);
	}

	// returns greatest common divisor among network time steps.
	// The time steps are rounded to the nearest decisecond.
	private static double computeCommonSimulationTimeInSeconds(Scenario scenario){
		
		if(scenario.getNetworkList()==null)
			return Double.NaN;
		
		if(scenario.getNetworkList().getNetwork().size()==0)
			return Double.NaN;
			
		// loop through networks calling gcd
		double dt;
		List<edu.berkeley.path.beats.jaxb.Network> networkList = scenario.getNetworkList().getNetwork();
		int tengcd = 0;		// in deciseconds
		for(int i=0;i<networkList.size();i++){
			dt = networkList.get(i).getDt().doubleValue();	// in seconds
	        if( BeatsMath.lessthan( Math.abs(dt) ,0.1) ){
	        	BeatsErrorLog.addError("Warning: Network dt given in hours. Changing to seconds.");
				dt *= 3600;
	        }
			tengcd = BeatsMath.gcd( BeatsMath.round(dt*10.0) , tengcd );
		}
    	return ((double)tengcd)/10.0;
	}
	
	/////////////////////////////////////////////////////////////////////
	// public: controller
	/////////////////////////////////////////////////////////////////////
	
//	/** [NOT IMPLEMENTED]. HERO coordinated ramp metering..
//	 * 
//	 * @return			_Controller object
//	 */
//	public static Controller createController_CRM_HERO(Scenario myScenario){
//		return  new edu.berkeley.path.beats.control.Controller_CRM_HERO(myScenario);
//	}
//
//	/** [NOT IMPLEMENTED] SWARM coordinated ramp metering.
//	 * 
//	 * @return			_Controller object
//	 */
//	public static Controller createController_CRM_SWARM(Scenario myScenario){
//		return  new edu.berkeley.path.beats.control.Controller_CRM_SWARM(myScenario);
//	}
//
//	/** Alinea isolated ramp metering.
//	 * 
//	 * <p> Generates a controller executing the Alinea algorithm. Feedback for the controller is taken
//	 * either from <code>mainlinelink</code> or <code>mainlinesensor</code>, depending on which is 
//	 * specified. Hence exactly one of the two must be non-null. A queue override algorithm will be 
//	 * employed if the <code>queuesensor</code> is non-null. The gain, defined in meters/sec units, is
//	 * normalized within the algorithm by dividing by the length a the mainline link (or by the link where the 
//	 * sensor resides in the case of sensor feedback).
//	 * 
//	 * @param myScenario		The scenario that contains the controller and its referenced links.
//	 * @param onramplink		The onramp link being controlled.
//	 * @param mainlinelink		The mainline link used for feedback (optional, use <code>null</code> to omit).
//	 * @param mainlinesensor	The onramp sensor used for feedback (optional, use <code>null</code> to omit).
//	 * @param queuesensor		The sensor on the onramp used to detect queue spillover optional, use <code>null</code> to omit).
//	 * @param gain				The gain for the integral controller in meters/sec.
//	 * @return					Controller object
//	 */
//	public static Controller createController_IRM_Alinea(Scenario myScenario,Link onramplink, Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,double gain){
//		return  new edu.berkeley.path.beats.control.Controller_IRM_Alinea(myScenario,onramplink,mainlinelink,mainlinesensor,queuesensor,gain);
//	}
//	
//	/** TOD isolated ramp metering.
//	 * 
//	 * <p> Generates a controller executing the TOD algorithm. A queue override algorithm will be 
//	 * employed if the <code>queuesensor</code> is non-null. The time of day profile is provided in a table.
//	 * Each row of the table contains a TOD entry, denoting the start time and the metering rates. This rate applies 
//	 * until the next entry becomes active/ the simulation ends. 
//	 * 
//	 * @param myScenario		The scenario that contains the controller and its referenced links.
//	 * @param onramplink		The onramp link being controlled.
//	 * @param queuesensor		The sensor on the onramp used to detect queue spillover optional, use <code>null</code> to omit).
//	 * @param todtable			The time of day profile is provided in a table. It contains two columns - StartTime and MeteringRates. 
//	 * @return					_Controller object
//	 */
//	public static Controller createController_IRM_Time_of_Day(Scenario myScenario,Link onramplink,Sensor queuesensor,Table todtable){
//		return  new edu.berkeley.path.beats.control.Controller_IRM_Time_of_Day(myScenario,onramplink,queuesensor,todtable);
//	}
//	
//	/** Traffic responsive isolated ramp metering.
//	 * 
//	 * <p> Generates a controller executing the Traffic responsive algorithm. Feedback for the controller is taken
//	 * either from <code>mainlinelink</code> or <code>mainlinesensor</code>, depending on which is 
//	 * specified. Hence exactly one of the two must be non-null. A queue override algorithm will be 
//	 * employed if the <code>queuesensor</code> is non-null. trtable specifies the occupancy, flow and speed thresholds.
//	 * Unused threshold types can be omitted from the table definition, or set to 0, for all entries. 
//	 * At least one of these thresholds should be available.  
//	 * 
//	 * @param myScenario		The scenario that contains the controller and its referenced links.
//	 * @param onramplink		The onramp link being controlled.
//	 * @param mainlinelink		The mainline link used for feedback (optional, use <code>null</code> to omit).
//	 * @param mainlinesensor	The onramp sensor used for feedback (optional, use <code>null</code> to omit).
//	 * @param queuesensor		The sensor on the onramp used to detect queue spillover optional, use <code>null</code> to omit).
//	 * @param trtable			The traffic responsive levels are provided in a table. It can contain four columns - MeteringRates, OccupancyThresholds, SpeedThresholds, FlowThresholds. 
//	 * @return					_Controller object
//	 */
//	public static Controller createController_IRM_Traffic_Responsive(Scenario myScenario,Link onramplink, Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,Table trtable){
//		return  new edu.berkeley.path.beats.control.Controller_IRM_Traffic_Responsive(myScenario,onramplink,mainlinelink,mainlinesensor,queuesensor,trtable);
//	}
//	
//	/** [NOT IMPLEMENTED] Actuated signal control.
//	 * 
//	 * @return			_Controller object
//	 */
//	public static Controller createController_SIG_Actuated(Scenario myScenario){
//		return  new edu.berkeley.path.beats.control.Controller_SIG_Actuated(myScenario);
//	}
//
//	/** [NOT IMPLEMENTED] Pretimed signal control.
//	 * 
//	 * @return			_Controller object
//	 */
//	public static Controller createController_SIG_Pretimed(Scenario myScenario){
//		return  new edu.berkeley.path.beats.control.Controller_SIG_Pretimed(myScenario);
//	}
//
//	/** [NOT IMPLEMENTED] Time of day variable speed limits.
//	 * 
//	 * @return			_Controller object
//	 */
//	public static Controller createController_VSL_Time_of_Day(Scenario myScenario){
//		return  new edu.berkeley.path.beats.control.Controller_VSL_Time_of_Day(myScenario);
//	}

	/////////////////////////////////////////////////////////////////////
	// public: event
	/////////////////////////////////////////////////////////////////////
	
//	/** On/Off switch for controllers.
//	 * 
//	 * Turns all controllers included in the <code>controllers</code> array on or off,
//	 * depending on the value of <code>ison</code>, at time <code>timestampinseconds</code>.
//	 * Here "off" means that the control commands are ignored by their targets, and that the 
//	 * controller's update function is not called. 
//	 * 
//	 * @param myScenario			The scenario.
//	 * @param timestampinseconds	Activation time for the event.
//	 * @param controllers			List of target Controller objects.
//	 * @param ison					<code>true</code> turns controllers on, <code>false</code> turns controllers off.
//	 * @return						Event object
//	 */
//	public static Event createEvent_Control_Toggle(Scenario myScenario,float timestampinseconds,List <Controller> controllers,boolean ison) {
//		return  new edu.berkeley.path.beats.event.Event_Control_Toggle(myScenario,timestampinseconds,controllers,ison);
//	}	
//
//	/** Change the model parameters of a list of links.
//	 * 
//	 * <p> Use this event to modify any subset of the fundamental diagram parameters of a list of links.
//	 * The new parameters should be expressed in per-lane units. Use <code>null</code> in place of any
//	 * of the input parameters to indicate that the current value of the parameter should be kept. The
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param links				List of Link objects.
//	 * @param freeflowSpeed		Freeflow speed in [meters/sec]
//	 * @param congestionSpeed	Congestion wave speed in [meters/sec]
//	 * @param capacity			Capacity in [veh/sec/lane]
//	 * @param densityJam		Jam density in [veh/meter/lane]
//	 * @param capacityDrop		Capacity drop in [veh/sec/lane]
//	 * @param stdDevCapacity	Standard deviation for the capacity in [veh/sec/lane]
//	 * @return					Event object
//	 */
//	public static Event createEvent_Fundamental_Diagram(Scenario myScenario,List <Link> links,double freeflowSpeed,double congestionSpeed,double capacity,double densityJam,double capacityDrop,double stdDevCapacity) {		
//		return  new edu.berkeley.path.beats.event.Event_Fundamental_Diagram(myScenario,links,freeflowSpeed,congestionSpeed,capacity,densityJam,capacityDrop,stdDevCapacity);
//	}
//	
//	/** Revert to original parameters for a list of links.
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param links				List of Link objects.
//	 * @return					Event object
//	 */
//	public static Event createEvent_Fundamental_Diagram_Revert(Scenario myScenario,List <Link> links) {		
//		return  new edu.berkeley.path.beats.event.Event_Fundamental_Diagram(myScenario,links);
//	}
//	
//	/** On/Off switch for <i>all</i> controllers. 
//	 * <p> This is equivalent to passing the full set of controllers to {@link ObjectFactory#createEvent_Control_Toggle}.
//	 *
//	 * @param myScenario		The scenario.
//	 * @return					Event object
//	 */
//	public static Event createEvent_Global_Control_Toggle(Scenario myScenario,boolean ison){
//		return  new edu.berkeley.path.beats.event.Event_Global_Control_Toggle(myScenario,ison);
//	}	
//	
//	/** Adjust the global demand knob.
//	 * 
//	 * <p>The amount of traffic entering the network at a given source equals the nominal profile value 
//	 * multiplied by both the knob for the profile and the global knob. Use this event to make 
//	 * changes to the global demand knob.
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param newknob 			New value of the knob.
//	 * @return			Event object
//	 */
//	public static Event createEvent_Global_Demand_Knob(Scenario myScenario,double newknob){
//		return  new edu.berkeley.path.beats.event.Event_Global_Demand_Knob(myScenario,newknob);
//	}	
//	
//	/** Adjust the knob for the demand profile applied to a particular link.
//	 * 
//	 * <p>Use this event to scale the demand profile applied to a given link.
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param newknob 			New value of the knob.
//	 * @return					Event object
//	 */
//	public static Event createEvent_Link_Demand_Knob(Scenario myScenario,double newknob){
//		return  new edu.berkeley.path.beats.event.Event_Link_Demand_Knob(myScenario,newknob);
//	}	
//	
//	/** Change the number of lanes on a particular link.
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param links 			List of links to change.
//	 * @param deltalanes		Number of lanes to add to each link in the list
//	 * @return					Event object
//	 */
//	public static Event createEvent_Link_Lanes(Scenario myScenario,List<Link> links,boolean isrevert,double deltalanes){
//		return  new edu.berkeley.path.beats.event.Event_Link_Lanes(myScenario,links,isrevert,deltalanes);
//	}	
//	
//	/** Change the split ratio matrix on a node.
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param node				The node
//	 * @param inlink			String id of the input link 
//	 * @param vehicleType		String name of the vehicle type
//	 * @param splits			An array of splits for every link exiting the node.
//	 * @return					Event object
//	 */		
//	public static Event createEvent_Node_Split_Ratio(Scenario myScenario,Node node,String inlink,String vehicleType,ArrayList<Double>splits){
//		return  new edu.berkeley.path.beats.event.Event_Node_Split_Ratio(myScenario,node,inlink,vehicleType,splits);
//	}	
	
	/////////////////////////////////////////////////////////////////////
	// public: sensor
	/////////////////////////////////////////////////////////////////////

//	/** Create a fixed loop detector station.
//	 * 
//	 * <p> This sensor models a loop detector station with loops covering all lanes at a particular
//	 * location on a link. 
//	 * 
//	 * @param myScenario		The scenario.
//	 * @param linkId			The id of the link where the sensor is placed.
//	 * @return					Sensor object
//	 */
//	public static Sensor createSensor_LoopStation(Scenario myScenario,String linkId){
//		return new edu.berkeley.path.beats.sensor.SensorLoopStation(myScenario,linkId);
//	}

	/////////////////////////////////////////////////////////////////////
	// public: scenario element
	/////////////////////////////////////////////////////////////////////
	
//	/** Container for a node.
//	 * 
//	 * @param node		The node.
//	 * @return			ScenarioElement object
//	 */
//	public static ScenarioElement createScenarioElement(Node node){
//		if(node==null)
//			return null;
//		ScenarioElement se = new ScenarioElement();
//		se.myScenario = node.getMyNetwork().myScenario;
//		se.myType = ScenarioElement.Type.node;
//		se.reference = node;
//		return se;
//	}
//	
//	/** Container for a link.
//	 * 
//	 * @param link		The link.
//	 * @return			ScenarioElement object
//	 */
//	public static ScenarioElement createScenarioElement(Link link){
//		if(link==null)
//			return null;
//		ScenarioElement se = new ScenarioElement();
//		se.myScenario = link.getMyNetwork().myScenario;
//		se.myType = ScenarioElement.Type.link;
//		se.reference = link;
//		return se;
//	}
//
//	/** Container for a sensor.
//	 * 
//	 * @param sensor	The sensor.
//	 * @return			ScenarioElement object
//	 */
//	public static ScenarioElement createScenarioElement(Sensor sensor){
//		if(sensor==null)
//			return null;
//		ScenarioElement se = new ScenarioElement();
//		se.myScenario = sensor.myScenario;
//		se.myType = ScenarioElement.Type.sensor;
//		se.reference = sensor;
//		return se;
//	}
//	
//	/** Container for a controller.
//	 * 
//	 * @param controller	The controller.
//	 * @return			ScenarioElement object
//	 */
//	public static ScenarioElement createScenarioElement(Controller controller){
//		if(controller==null)
//			return null;
//		ScenarioElement se = new ScenarioElement();
//		se.myType = ScenarioElement.Type.controller;
//		se.reference = controller;
//		return se;
//	}
//
//	/** Container for an event.
//	 * 
//	 * @param event	The event.
//	 * @return ScenarioElement object
//	 */
//	public static ScenarioElement createScenarioElement(Event event){
//		if(event==null)
//			return null;
//		ScenarioElement se = new ScenarioElement();
//		se.myType = ScenarioElement.Type.event;
//		se.reference = event;
//		return se;
//	}

	/////////////////////////////////////////////////////////////////////
	// public: sets and profiles
	/////////////////////////////////////////////////////////////////////

//	/** Create an initial density.
//	 * 
//	 * @param scenario The scenario
//	 * @param tstamp A double with the time stamp in seconds after midnight
//	 * @param link_id The String id of the link
//	 * @param vehtype An array of String link type names
//	 * @param init_density 2-D matrix of doubles with densities per link and vehicle type.
//	 * @throws BeatsException
//	 * @return InitialDensitySet
//	 */
//	public static InitialDensitySet createInitialDensitySet(Scenario scenario,double tstamp,String [] link_id,String [] vehtype,Double [][] init_density) throws BeatsException{
//		
//		// check input
//		if(link_id.length!=init_density.length)
//			throw new BeatsException("1st dimension of the initial density matrix does not match the link array.");
//		
//		if(init_density.length==0)
//			throw new BeatsException("Empty initial density matrix.");
//
//		if(vehtype.length!=init_density[0].length)
//			throw new BeatsException("2nd dimension of the initial density matrix does not match the vehicle types array.");
//		
//		// new
//		InitialDensitySet ic = new InitialDensitySet();
//		
//		// populate base class
//		
//		// vehicle types
//		VehicleTypeOrder vto = new VehicleTypeOrder();
//		for(String str : vehtype){
//			VehicleType vt = new VehicleType();
//			vt.setName(str);
//			vto.getVehicleType().add(vt);
//		}
//		ic.setVehicleTypeOrder(vto);
//
//		// initial density
//		int i;
//		for(i=0;i<init_density.length;i++){
//			Density density = new Density();
//			density.setLinkId(link_id[i]);
//			density.setContent(BeatsFormatter.csv(init_density[i],":"));			
//			ic.getDensity().add(density);
//		}
//		
//		// populate extended properties
//		ic.populate(scenario);
//		return ic;
//	}
//	
//	/** Create a demand profile.
//	 * 
//	 * @param scenario The scenario
//	 * @param dem A list of list of demand values.
//	 * @param starttime start time float
//	 * @param dt time step float
//	 * @param knob scalar multiplier float
//	 * @param StdDevAdd additive uncertainty
//	 * @param StdDevMult multiplicative uncertainty
//	 * @return DemandProfile
//	 */
//	public static DemandProfile createDemandProfile(Scenario scenario,String linkid,Double [][] dem,float starttime,float dt,float knob,float StdDevAdd,float StdDevMult){
//
//		// check input parameters
//		int i,j;
//		for(i=0;i<dem.length;i++)
//			for(j=0;j<dem[i].length;j++){
//				dem[i][j] = dem[i][j]==null ? 0d : dem[i][j];
//				dem[i][j] = dem[i][j]<0d ? 0d : dem[i][j];
//			}
//		
//		// new
//		DemandProfile demandprofile = new DemandProfile();
//		
//		// copy to base class
//		demandprofile.setLinkIdOrigin(linkid);
//		demandprofile.setKnob(new BigDecimal(knob));
//		demandprofile.setStartTime(new BigDecimal(starttime));
//		demandprofile.setDt(new BigDecimal(dt));
//		demandprofile.setStdDevAdd(new BigDecimal(StdDevAdd));
//		demandprofile.setStdDevMult(new BigDecimal(StdDevMult));
//		demandprofile.setContent(BeatsFormatter.csv(dem,":",","));
//		
//		// populate extended class properties
//		demandprofile.populate(scenario);
//
//		return demandprofile;
//	}
	
}
