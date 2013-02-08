package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;

public class NodeTest {

	private static Node node;
	private static String config_folder = "data/config/";
	private static String config_file = "_smalltest_nocontrol.xml";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");
		node = scenario.getNodeWithId("-4");
	}

	@Test
	public void test_getMyNetwork() {
		assertEquals(node.getMyNetwork().getId(),"-1");
	}

	@Test
	public void test_getOutput_link() {
		Link[] links = node.getOutput_link();
		assertEquals(links[0].getId(),"-4");
		assertEquals(links[1].getId(),"-7");
	}

	@Test
	public void test_getInput_link() {
		Link[] links = node.getInput_link();
		System.out.println(links[0].getId());
		assertEquals(links[0].getId(),"-3");
	}

	@Test
	public void test_getInputLinkIndex() {
		assertEquals(node.getInputLinkIndex("-3"),0);
		assertEquals(node.getInputLinkIndex("xx"),-1);

		// edge case
		assertEquals(node.getInputLinkIndex(null),-1);
	}

	@Test
	public void test_getOutputLinkIndex() {
		assertEquals(node.getOutputLinkIndex("-4"),0);		
		assertEquals(node.getOutputLinkIndex("-7"),1);
		assertEquals(node.getOutputLinkIndex("xx"),-1);

		// edge case
		assertEquals(node.getOutputLinkIndex(null),-1);
	}

	@Test
	public void test_getnIn() {
		assertEquals(node.getnIn(),1);
	}

	@Test
	public void test_getnOut() {
		assertEquals(node.getnOut(),2);
	}

	@Test
	public void test_hasController() {
		assertFalse(node.hasController());
	}

	@Test
	public void test_getSplitRatio_a() {
		Double [][][] X = node.getSplitRatio();
		assertEquals(X[0][0][0],1d,1e-4);
		assertEquals(X[0][1][0],0d,1e-4);
	}

	@Test
	public void test_getSplitRatio_b() {
		assertEquals(node.getSplitRatio(0, 0, 0),1d,1e-4);
		assertEquals(node.getSplitRatio(0, 1, 0),0d,1e-4);

		// edge cases
		assertNull(node.getSplitRatio(-1, 1, 0));
		assertNull(node.getSplitRatio(100, 1, 0));
		assertNull(node.getSplitRatio(0, -1, 0));
		assertNull(node.getSplitRatio(0, 100, 0));
		assertNull(node.getSplitRatio(0, 0, -1));
		assertNull(node.getSplitRatio(0, 0, 100));
	}	

}
