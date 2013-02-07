package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.Table;

public class TableTest {
	
	private static Table good_table;
	private static Table bad_table;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		ArrayList<String> columnNames = new ArrayList<String>();
		columnNames.add("c1");
		columnNames.add("c2");
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> row1 = new ArrayList<String>();
		row1.add("r11");
		row1.add("r12");

		ArrayList<String> row2 = new ArrayList<String>();
		row2.add("r21");
		row2.add("r22");

		ArrayList<String> row3 = new ArrayList<String>();
		row3.add("r31");
		row3.add("r32");
		
		rows.add(row1);
		rows.add(row2);
		rows.add(row3);
		
		good_table = new Table(columnNames,rows);
		
		ArrayList<ArrayList<String>> badrows = new ArrayList<ArrayList<String>>();
		ArrayList<String> badrow = new ArrayList<String>();
		badrow.add("xxx");
		badrows.add(badrow);
		bad_table = new Table(columnNames,badrows);
		
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
	public void test_checkTable() {
		assertTrue(good_table.checkTable());
		assertFalse(bad_table.checkTable());
	}

	@Test
	public void test_getNoRows() {
		assertEquals(good_table.getNoRows(),3);
		assertEquals(bad_table.getNoRows(),1);
	}

	@Test
	public void test_getNoColumns() {
		assertEquals(good_table.getNoColumns(),2);
		assertEquals(bad_table.getNoColumns(),2);
	}

	@Test
	public void test_getColumnNo() {
		assertEquals(good_table.getColumnNo("c1"),0);
		assertEquals(good_table.getColumnNo("xxx"),-1);
		
		// edge case
		assertEquals(good_table.getColumnNo(null),-1);
	}

	@Test
	public void test_getTableElement_a() {
		assertEquals(good_table.getTableElement(0,0),"r11");
		
		// edge cases
		assertNull(good_table.getTableElement(-1,0));
		assertNull(good_table.getTableElement(0,-1));
		assertNull(good_table.getTableElement(500,0));
		assertNull(good_table.getTableElement(0,500));
	}

	@Test
	public void test_getTableElement_b() {
		assertEquals(good_table.getTableElement(0,"c1"),"r11");

		// edge cases
		assertNull(good_table.getTableElement(-1,"c1"));
		assertNull(good_table.getTableElement(0,null));
		assertNull(good_table.getTableElement(500,"c1"));
	}

}
