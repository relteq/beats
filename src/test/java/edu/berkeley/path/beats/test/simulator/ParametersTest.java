package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.Parameters;

public class ParametersTest {

	private static Parameters parameters;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parameters = new Parameters();
		parameters.addParameter("n1","v1");
		parameters.addParameter("n2","v2");
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
	public void test_has() {
		assertTrue(parameters.has("n1"));
		assertFalse(parameters.has("n3"));
		
		// edge case
		assertFalse(parameters.has(null));
	}

	@Test
	public void test_get() {
		
		assertEquals(parameters.get("n1"),"v1");
		assertNull(parameters.get("n3"));
		
		// edge cases
		assertNull(parameters.get(null));
	}
}
