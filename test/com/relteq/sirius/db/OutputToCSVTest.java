package com.relteq.sirius.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class OutputToCSVTest {

	@Test
	public void testGetSQLQueryForCSV() {
		
		String table = "link_data_total"; 
		String[] arguments = new String[2];;
		arguments[0] = "1";
		arguments[1] = "2-3";
				
		assertEquals("Result", "SELECT * FROM link_data_total WHERE data_source_id IN (SELECT data_source_id FROM simulation_runs WHERE scenario_id='1' AND run_number >= 2 AND run_number <=3)", 
								OutputToCSV.getSQLQueryForCSV(table, arguments));
	}

}
