package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsMath;
import edu.berkeley.path.beats.simulator.InitialDensitySet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;

public class InitialDensitySetTest {

	private static Scenario scenario;
	private static InitialDensitySet ids;
	private static String config_folder = "data/config.quarantine/";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		scenario = ObjectFactory.createAndLoadScenario(config_folder+"complete_twotypes.xml");
		if(scenario==null)
			fail("scenario did not load");
		ids = (InitialDensitySet) scenario.getInitialDensitySet();
	}

	@Test
	public void test_getDensityForLinkIdInVeh() {
		double link_length_in_miles = scenario.getLinkWithId("1").getLengthInMeters()*0.621371/1000d;
		Double [] X = BeatsMath.times( ids.getDensityForLinkIdInVeh("1","1"),1/link_length_in_miles);
		Double [] expected = {2d,1d};
		assertTrue(X.length==expected.length);
		for(int i=0;i<expected.length;i++)
			assertEquals(X[i],expected[i],1E-4);
	}

	@Test
	public void test_get_initial_density_in_vehpermeter() {
		Double [][] expected = {{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d}};
		Link [] links = ids.getLink();
		Double [][] X = ids.get_initial_density_in_vehpermeter();
		int i,j;
		for(i=0;i<links.length;i++)
			for(j=0;j<scenario.getNumVehicleTypes();j++)
				assertEquals(X[i][j]/(0.621371/1000d),expected[i][j],1E-4);
	}

	@Test
	public void test_get_initial_density_in_veh() {
		Double [][] expected = {{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d}};
		Link [] links = ids.getLink();
		Double [][] X = ids.get_initial_density_in_veh();
		int i,j;
		double linklength;
		for(i=0;i<links.length;i++){
			linklength = links[i].getLengthInMeters()*(0.621371/1000d);
			for(j=0;j<scenario.getNumVehicleTypes();j++)
				assertEquals(X[i][j]/linklength,expected[i][j],1E-4);
		}
	}

	@Test
	public void test_getVehicletypeindex() {
		Integer[] ind = ids.getVehicletypeindex();
		Integer[] expected = {0,1};
		assertTrue(Arrays.equals(ind,expected));
	}
	
}
