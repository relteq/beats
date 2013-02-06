package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsFormatter;

public class BeatsFormatterTest {

//	public static ArrayList<ArrayList<Double>> readCSV(String filename,String delim) throws IOException{
//	public static ArrayList<ArrayList<ArrayList<Double>>> readCSV(String filename,String delim1,String delim2) throws IOException{
	
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

//	public static String csv(Double [][] V,String delim1,String delim2){
	
	@Test
	public void test_csv_2D() {
		Double [][] V = {{1d,2d,3d},{4d,5d},{7d,8d,9d}};		
		assertEquals(BeatsFormatter.csv(V, ",",";"),"1.0,2.0,3.0;4.0,5.0;7.0,8.0,9.0");
		
		// edge cases
		assertEquals(BeatsFormatter.csv(null, ",",";"),"");
		assertEquals(BeatsFormatter.csv(V, null,";"),"");
		assertEquals(BeatsFormatter.csv(V, ",",null),"");
	}
}
