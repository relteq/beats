package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjectFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test_createAndLoadScenario() {
//		public static Scenario createAndLoadScenario(String configfilename) throws BeatsException {
		fail("Not yet implemented");
	}

	@Test
	public void test_process() {
//		public static Scenario process(Scenario S) throws BeatsException {
		fail("Not yet implemented");
	}

	@Test
	public void test_setObjectFactory() {
//		public static void setObjectFactory(Unmarshaller unmrsh, Object factory) throws PropertyException {
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_CRM_HERO() {
//		public static Controller createController_CRM_HERO(Scenario myScenario){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_CRM_SWARM() {
//		public static Controller createController_CRM_SWARM(Scenario myScenario){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_IRM_Alinea() {
//		public static Controller createController_IRM_Alinea(Scenario myScenario,Link onramplink, Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,double gain){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_IRM_Time_of_Day() {
//		public static Controller createController_IRM_Time_of_Day(Scenario myScenario,Link onramplink,Sensor queuesensor,Table todtable){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_IRM_Traffic_Responsive() {
//		public static Controller createController_IRM_Traffic_Responsive(Scenario myScenario,Link onramplink, Link mainlinelink,Sensor mainlinesensor,Sensor queuesensor,Table trtable){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_SIG_Actuated() {
//		public static Controller createController_SIG_Actuated(Scenario myScenario){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_SIG_Pretimed() {
//		public static Controller createController_SIG_Pretimed(Scenario myScenario){
		fail("Not yet implemented");
	}

	@Test
	public void test_createController_VSL_Time_of_Day() {
//		public static Controller createController_VSL_Time_of_Day(Scenario myScenario){
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Control_Toggle() {
//		public static Event createEvent_Control_Toggle(Scenario myScenario,float timestampinseconds,List <Controller> controllers,boolean ison) {
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Fundamental_Diagram() {
//		public static Event createEvent_Fundamental_Diagram(Scenario myScenario,List <Link> links,double freeflowSpeed,double congestionSpeed,double capacity,double densityJam,double capacityDrop,double stdDevCapacity) {		
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Fundamental_Diagram_Revert() {
//		public static Event createEvent_Fundamental_Diagram_Revert(Scenario myScenario,List <Link> links) {		
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Global_Control_Toggle() {
//		public static Event createEvent_Global_Control_Toggle(Scenario myScenario,boolean ison){
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Global_Demand_Knob() {
//		public static Event createEvent_Global_Demand_Knob(Scenario myScenario,double newknob){
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Link_Demand_Knob() {
//		public static Event createEvent_Link_Demand_Knob(Scenario myScenario,double newknob){
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Link_Lanes() {
//		public static Event createEvent_Link_Lanes(Scenario myScenario,List<Link> links,boolean isrevert,double deltalanes){	
		fail("Not yet implemented");
	}

	@Test
	public void test_createEvent_Node_Split_Ratio() {
//		public static Event createEvent_Node_Split_Ratio(Scenario myScenario,Node node,String inlink,String vehicleType,ArrayList<Double>splits){
		fail("Not yet implemented");
	}

	@Test
	public void test_createSensor_LoopStation() {
//		public static Sensor createSensor_LoopStation(Scenario myScenario,String linkId){
		fail("Not yet implemented");
	}

	@Test
	public void test_createScenarioElement_node() {
//		public static ScenarioElement createScenarioElement(Node node){
		fail("Not yet implemented");
	}

	@Test
	public void test_createScenarioElement_link() {
//		public static ScenarioElement createScenarioElement(Link link){
		fail("Not yet implemented");
	}

	@Test
	public void test_createScenarioElement_sensor() {
//		public static ScenarioElement createScenarioElement(Sensor sensor){
		fail("Not yet implemented");
	}

	@Test
	public void test_createScenarioElement_controller() {
//		public static ScenarioElement createScenarioElement(Controller controller){
		fail("Not yet implemented");
	}

	@Test
	public void test_createScenarioElement_event() {
//		public static ScenarioElement createScenarioElement(Event event){
		fail("Not yet implemented");
	}

	@Test
	public void test_createInitialDensitySet() {
//		public static InitialDensitySet createInitialDensitySet(Scenario scenario,double tstamp,String [] link_id,String [] vehtype,Double [][] init_density) throws BeatsException{
		fail("Not yet implemented");
	}

	@Test
	public void test_createDemandProfile() {
//		public static DemandProfile createDemandProfile(Scenario scenario,String linkid,Double [][] dem,float starttime,float dt,float knob,float StdDevAdd,float StdDevMult){
		fail("Not yet implemented");
	}

}
