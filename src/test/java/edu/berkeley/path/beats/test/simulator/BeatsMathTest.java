package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsFormatter;
import edu.berkeley.path.beats.simulator.BeatsMath;

public class BeatsMathTest {

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
	public void test_zeros_1D() {
		Double [] x = {0d,0d,0d};
		assertTrue(Arrays.equals(BeatsMath.zeros(3),x));
		
		// edge cases
		assertNull(BeatsMath.zeros(-1));
		assertTrue(BeatsMath.zeros(0).length==0);
	}
		
	@Test
	public void test_zeros_2D() {

		Double [][] x = {{0d,0d},{0d,0d},{0d,0d}};
		Double [][] y = BeatsMath.zeros(3,2);
		
		assertEquals(x.length,y.length);
		for(int i=0;i<x.length;i++)
			assertTrue(Arrays.equals(y[i],x[i]));
		
		// edge cases
		assertNull(BeatsMath.zeros(-1,1));
		assertNull(BeatsMath.zeros(1,-1));
		assertTrue(BeatsMath.zeros(0,1).length==0);
		assertTrue(BeatsMath.zeros(1,0)[0].length==0);
	}

	@Test
	public void test_sum_1D_array() {
		Double [] V = {1d,2d,3d};
		assertEquals(BeatsMath.sum(V),6d,1E-6);
		
		// edge cases
		Double [] Y = null;
		assertNull(BeatsMath.sum(Y));
		Double [] Z = {};
		assertEquals(BeatsMath.sum(Z),0d,1E-6);		
	}

	@Test
	public void test_sum_1D_collection() {
		
		ArrayList<Double> V = new ArrayList<Double>();
		V.add(1d);
		V.add(2d);
		V.add(3d);
		assertEquals(BeatsMath.sum(V),6d,1E-6);
		
		// edge cases
		ArrayList<Double> Y = null;
		assertNull(BeatsMath.sum(Y));
		ArrayList<Double> Z = new ArrayList<Double>();
		assertEquals(BeatsMath.sum(Z),0d,1E-6);	
		
	}

	@Test
	public void test_sum_2D_array() {
		
		Double [][] V = {{1d,2d,3d},{4d,5d,6d}};
		Double [] s1 = {5d,7d,9d};
		Double [] s2 = {6d,15d};
				
		assertTrue(Arrays.equals(BeatsMath.sum(V,1),s1));
		assertTrue(Arrays.equals(BeatsMath.sum(V,2),s2));
				
		// edge cases
		Double [][] Y = null;
		assertNull(BeatsMath.sum(Y,1));
		assertNull(BeatsMath.sum(Y,2));
		assertNull(BeatsMath.sum(V,0));
		Double [][] Z = {};
		assertNull(BeatsMath.sum(Z,1));
		assertNull(BeatsMath.sum(Z,2));
	}

	@Test
	public void test_times_scalararray() {
		Double [] V = {1d,2d,3d};
		double a = 4;
		Double [] S = {4d,8d,12d};
		assertTrue(Arrays.equals(BeatsMath.times(V, a), S));
		
		// edge cases
		Double [] Y = null;
		assertNull(BeatsMath.times(Y,2d));
	}

	@Test
	public void test_ceil() {
		assertEquals(BeatsMath.ceil(1.2d),2);
		assertEquals(BeatsMath.ceil(1d),1);
		assertEquals(BeatsMath.ceil(-1.5d),-1);
	}

	@Test
	public void test_floor() {
		assertEquals(BeatsMath.floor(1.2d),1);
		assertEquals(BeatsMath.floor(1d),1);
		assertEquals(BeatsMath.floor(-1.5d),-2);
	}

	@Test
	public void test_round() {
		assertEquals(BeatsMath.round(1.2d),1);
		assertEquals(BeatsMath.round(1d),1);
		assertEquals(BeatsMath.round(-1.6d),-2);
		assertEquals(BeatsMath.round(-1.5d),-1);
		assertEquals(BeatsMath.round(-1.1d),-1);
	}

	@Test
	public void test_any() {
		boolean [] a = {true,true};
		boolean [] b = {true,false};
		boolean [] c = {false,false};
		assertEquals(BeatsMath.any(a),true);
		assertEquals(BeatsMath.any(b),true);
		assertEquals(BeatsMath.any(c),false);
		
		// edge cases
		assertEquals(BeatsMath.any(null),false);
		boolean [] d = {};
		assertEquals(BeatsMath.any(d),false);
	}

	@Test
	public void test_all() {
		boolean [] a = {true,true};
		boolean [] b = {true,false};
		boolean [] c = {false,false};
		assertEquals(BeatsMath.all(a),true);
		assertEquals(BeatsMath.all(b),false);
		assertEquals(BeatsMath.all(c),false);
		
		// edge cases
		assertEquals(BeatsMath.all(null),false);
		boolean [] d = {};
		assertEquals(BeatsMath.all(d),false);
	}

	@Test
	public void test_not() {

		boolean [] a = {true,true};
		boolean [] na = {false,false};
		boolean [] b = {true,false};
		boolean [] nb = {false,true};
		boolean [] c = {false,false};
		boolean [] nc = {true,true};
		assertTrue(Arrays.equals(BeatsMath.not(a), na));
		assertTrue(Arrays.equals(BeatsMath.not(b), nb));
		assertTrue(Arrays.equals(BeatsMath.not(c), nc));
		
		// edge cases
		assertNull(BeatsMath.not(null));
		boolean [] d = {};
		assertNull(BeatsMath.not(d));
	}

	@Test
	public void test_count() {
		boolean [] a = {true,true};
		assertEquals(BeatsMath.count(a),2);

		// edge cases
		assertEquals(BeatsMath.count(null),0);
		boolean [] d = {};
		assertEquals(BeatsMath.count(d),0);
	}

	@Test
	public void test_find() {
		
		boolean [] a = {false, false,false};
		boolean [] b = {true, false,false};
		boolean [] c = {false, false,true};
		boolean [] d = {true, true,false};
		
		ArrayList<Integer> fa = BeatsMath.find(a);
		assertTrue(fa.isEmpty());

		ArrayList<Integer> fb = BeatsMath.find(b);
		assertTrue(fb.size()==1);
		assertEquals((int) fb.get(0),0);

		ArrayList<Integer> fc = BeatsMath.find(c);
		assertTrue(fc.size()==1);
		assertEquals((int) fc.get(0),2);

		ArrayList<Integer> fd = BeatsMath.find(d);
		assertTrue(fd.size()==2);
		assertEquals((int) fd.get(0),0);
		assertEquals((int) fd.get(1),1);
		
		// edge cases
		assertNull(BeatsMath.find(null));
	}

	@Test
	public void test_isintegermultipleof() {
		assertTrue(BeatsMath.isintegermultipleof(10d,5d));
		assertTrue(!BeatsMath.isintegermultipleof(11d,5d));
		
		// edge cases
		assertTrue(BeatsMath.isintegermultipleof(Double.POSITIVE_INFINITY,5d));
		assertTrue(!BeatsMath.isintegermultipleof(5d,0d));
		assertTrue(BeatsMath.isintegermultipleof(0d,5d));
	}

	@Test
	public void test_equals() {
		assertTrue(BeatsMath.equals(1d,1d));
		assertTrue(!BeatsMath.equals(1d,2d));
	}

	@Test
	public void test_equals1D() {
		
		ArrayList<Double> A = new ArrayList<Double>();
		A.add(1d);
		A.add(2d);
		A.add(3d);

		ArrayList<Double> B = new ArrayList<Double>();
		B.add(1d);
		B.add(4d);
		B.add(3d);

		ArrayList<Double> C = new ArrayList<Double>();
		C.add(1d);
		C.add(4d);
		
		assertTrue(BeatsMath.equals1D(A,A));
		assertTrue(!BeatsMath.equals1D(A,B));
		assertTrue(!BeatsMath.equals1D(A,C));
		
		// edge cases
		assertTrue(!BeatsMath.equals1D(null,A));
		assertTrue(!BeatsMath.equals1D(A,null));
	}

	@Test
	public void test_equals2D() {
		String filename = fixture_folder+"twoDmatrix.txt";
		ArrayList<ArrayList<Double>> A = BeatsFormatter.readCSV(filename,"\t");
		ArrayList<ArrayList<Double>> B = BeatsFormatter.readCSV(filename,"\t");
		
		assertTrue(BeatsMath.equals2D(A,B));
		
		B.get(0).set(0,-1d);
		assertTrue(!BeatsMath.equals2D(A,B));
		
		// edge cases
		assertTrue(!BeatsMath.equals2D(null,B));
		assertTrue(!BeatsMath.equals2D(A,null));
	}

	@Test
	public void test_equals3D() {

		String filename = fixture_folder+"threeDmatrix.txt";
		ArrayList<ArrayList<ArrayList<Double>>> A = BeatsFormatter.readCSV(filename,",",";");
		ArrayList<ArrayList<ArrayList<Double>>> B = BeatsFormatter.readCSV(filename,",",";");
		
		assertTrue(BeatsMath.equals3D(A,B));
		
		B.get(0).get(0).set(0,-1d);
		assertTrue(!BeatsMath.equals3D(A,B));
		
		// edge cases
		assertTrue(!BeatsMath.equals3D(null,B));
		assertTrue(!BeatsMath.equals3D(A,null));	
	}	
	
	@Test
	public void test_greaterthan() {
		assertTrue(!BeatsMath.greaterthan(1d,2d));
		assertTrue(!BeatsMath.greaterthan(1d,1d));
		assertTrue(BeatsMath.greaterthan(2d,1d));
	}

	@Test
	public void test_greaterorequalthan() {
		assertTrue(!BeatsMath.greaterorequalthan(1d,2d));
		assertTrue(BeatsMath.greaterorequalthan(1d,1d));
		assertTrue(BeatsMath.greaterorequalthan(2d,1d));
	}

	@Test
	public void test_lessthan() {
		assertTrue(BeatsMath.lessthan(1d,2d));
		assertTrue(!BeatsMath.lessthan(1d,1d));
		assertTrue(!BeatsMath.lessthan(2d,1d));
	}

	@Test
	public void test_lessorequalthan() {
		assertTrue(BeatsMath.lessorequalthan(1d,2d));
		assertTrue(BeatsMath.lessorequalthan(1d,1d));
		assertTrue(!BeatsMath.lessorequalthan(2d,1d));
	}

	@Test
	public void test_gcd() {
		assertEquals(BeatsMath.gcd(20,15),5);
		assertEquals(BeatsMath.gcd(20,1),1);
		assertEquals(BeatsMath.gcd(20,11),1);
		assertEquals(BeatsMath.gcd(20,0),20);
		assertEquals(BeatsMath.gcd(-20,1),1);
		assertEquals(BeatsMath.gcd(-20,11),-1);
		assertEquals(BeatsMath.gcd(-20,0),-20);
	}

	@Test
	public void test_makecoy() {
		Double [][] x = {{1d,2d},{3d,4d},{5d,6d}};
		Double [][] y = BeatsMath.makecopy(x);

		assertEquals(x.length,y.length);
		for(int i=0;i<x.length;i++)
			assertTrue(Arrays.equals(x[i],y[i]));
		
		// edge cases
		assertNull(BeatsMath.makecopy(null));
	}

}