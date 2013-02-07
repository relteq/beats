package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsFormatter;

public class BeatsFormatterTest {

	private String fixture_folder = "data/test/fixture/";
		
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
	public void test_csv_1D() {
		Double [] V = {1d,2d,3d};
		assertEquals(BeatsFormatter.csv(V, "z"),"1.0z2.0z3.0");
		
		// edge cases
		assertEquals(BeatsFormatter.csv(null, "z"),"");
		assertEquals(BeatsFormatter.csv(V,null),"");
	}

	@Test
	public void test_csv_2D() {
		Double [][] V = {{1d,2d,3d},{4d,5d},{7d,8d,9d}};		
		assertEquals(BeatsFormatter.csv(V, ",",";"),"1.0,2.0,3.0;4.0,5.0;7.0,8.0,9.0");
		
		// edge cases
		assertEquals(BeatsFormatter.csv(null, ",",";"),"");
		assertEquals(BeatsFormatter.csv(V, null,";"),"");
		assertEquals(BeatsFormatter.csv(V, ",",null),"");
	}

	@Test
	public void test_read_2D() {
		Double [][] V  = {{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},{0.149364, 0.080467, 0.273645, 0.030807, 4.87E-4, 0.680465, 0.0},{0.21458, 0.290479, 1.242697, 0.409284, 0.017176, 1.118014, 0.0}};
		String filename = fixture_folder+"twoDmatrix.txt";
		ArrayList<ArrayList<Double>> A = BeatsFormatter.readCSV(filename,"\t");
		assertNotNull(A);
		int i,j;
		for(i=0;i<A.size();i++)
			for(j=0;j<A.get(i).size();j++)
				assertEquals(V[i][j],A.get(i).get(j));
		
		// edge cases
		assertNull(BeatsFormatter.readCSV(null,"\t"));
		assertNull(BeatsFormatter.readCSV(filename,null));
	}

	@Test
	public void test_read_3D() {
		Double [][][] V  = {{{0.0}, {0.0}, {0.0}, {0.0}, {0.0}, {0.0}, {0.0, 1.0}, {1.0}, {1.0}, {1.0}, {1.0}, {1.0}, {1.0}}, 
				{{0.149364}, {0.080467}, {0.273645}, {0.030807}, {4.87E-4}, {0.680465}, {0.0, -0.149364}, {-0.080467}, {-0.273645}, {-0.030807}, {-4.87E-4}, {-0.680465}, {-0.0}}, 
				{{0.21458}, {0.290479}, {1.242697}, {0.409284}, {0.017176}, {1.118014}, {0.0, -0.21458}, {-0.290479}, {-1.242697}, {-0.409284}, {-0.017176}, {-1.118014}, {-0.0}}};						
						
		String filename = fixture_folder+"threeDmatrix.txt";
		ArrayList<ArrayList<ArrayList<Double>>> A = BeatsFormatter.readCSV(filename,",",";");
		assertNotNull(A);
		int i,j,k;
		for(i=0;i<A.size();i++)
			for(j=0;j<A.get(i).size();j++)
				for(k=0;k<A.get(i).get(j).size();k++)
					assertEquals(V[i][j][k],A.get(i).get(j).get(k));
		
		// edge cases
		assertNull(BeatsFormatter.readCSV(null,",",";"));
		assertNull(BeatsFormatter.readCSV(filename,null,";"));
		assertNull(BeatsFormatter.readCSV(filename,",",null));
	}
	
	
}
