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
	private static String config_folder = "data/config/";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		scenario = ObjectFactory.createAndLoadScenario(config_folder+"complete_twotypes.xml");
		if(scenario==null)
			fail("scenario did not load");
		ids = (InitialDensitySet) scenario.getInitialDensitySet();
	}

	@Test
	public void test_getDensityForLinkIdInVeh() {
		
		int [] link_id = {1,2,3,4,5,6,7};
		int i,j;
		double [] X;
		double [] expected = {2d,1d};		
		for(i=0;i<link_id.length;i++){
			Link link = scenario.getLinkWithId(link_id[i]);
			double link_length_in_miles = link.getLengthInMeters()*0.621371/1000d;
			X = BeatsMath.times( ids.getDensityForLinkIdInVeh(1,link_id[i]),1/link_length_in_miles);
			for(j=0;j<expected.length;j++)
				assertEquals(X[j],expected[j],1E-4);
		}
		
		// edge case
		X = ids.getDensityForLinkIdInVeh(-1,1);
		assertNull(X);

		// edge case
		X = ids.getDensityForLinkIdInVeh(1,-1);
		assertNull(X);
		
	}

//	@Test
//	public void test_get_initial_density_in_vehpermeter() {
//		Double [] expected = {2d,1d,2d,1d,2d,1d,2d,1d,2d,1d,2d,1d,2d,1d,0d,0d};
//		Link [] links = ids.getLink();
//		double [] X = ids.get_initial_density_in_vehpermeter();
//		int i;
//		for(i=0;i<links.length;i++)
//			assertEquals(X[i]/(0.621371/1000d),expected[i],1E-4);
//	}
//
//	@Test
//	public void test_get_initial_density_in_veh() {
//		Double [][] expected = {{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d},{2d,1d}};
//		Link [] links = ids.getLink();
//		Double [][] X = ids.get_initial_density_in_veh();
//		int i,j;
//		double linklength;
//		for(i=0;i<links.length;i++){
//			linklength = links[i].getLengthInMeters()*(0.621371/1000d);
//			for(j=0;j<scenario.getNumVehicleTypes();j++){
//				System.out.println(X[i][j]/linklength);
//				assertEquals(X[i][j]/linklength,expected[i][j],1E-4);
//			}
//		}
//	}
	
}
