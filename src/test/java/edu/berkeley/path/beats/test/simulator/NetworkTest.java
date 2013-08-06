package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;

public class NetworkTest {

	private static Network network;
	private static String config_folder = "data/config/";
	private static String config_file = "_smalltest.xml";
		
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println(config_folder+config_file);
		Scenario scenario = ObjectFactory.createAndLoadScenario(config_folder+config_file);
		if(scenario==null)
			fail("scenario did not load");
		network = (Network) scenario.getNetworkList().getNetwork().get(0);
	}

	@Test
	public void test_getLinkWithId() {
		assertEquals(network.getLinkWithId(-2).getLengthInMeters(),976.0244598117758,1E-4);
	}

	@Test
	public void test_getNodeWithId() {
		Node node = network.getNodeWithId(-2);
		assertEquals(node.getPosition().getPoint().get(0).getLat().doubleValue(),37.8437831193107,1E-4);
	}

	@Test
	public void test_getListOfNodes() {
		List<edu.berkeley.path.beats.jaxb.Node> nodelist = network.getListOfNodes();
		assertEquals(nodelist.size(),8);
	}

	@Test
	public void test_getListOfLinks() {
		List<edu.berkeley.path.beats.jaxb.Link> linklist = network.getListOfLinks();
		assertEquals(linklist.size(),7);
	}

	@Test
	public void test_getListOfSignals() {
		List<edu.berkeley.path.beats.jaxb.Signal> signallist = network.getListOfSignals();
		assertNull(signallist);
	}

}
