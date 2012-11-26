package edu.berkeley.path.beats.db;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;


import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import edu.berkeley.path.beats.processor.AggregateData;
import edu.berkeley.path.beats.processor.LinkDataTotal;

public class OutputToCSV {


	/**
	 * Get aggregation selection for SELECTs
	 * @param query
	 * @param arguments
	 * @param aggregation
	 * @return
	 */
	public static String getAggregationSelection(String query, String aggregation) {
		
		if (aggregation == null) 	return query;
						
		if ( query.indexOf("WHERE") < 0 ) {
			query += " WHERE ";
		}
		else
		{
			query += " AND ";
		}
	
		
		query += "agg_type_id IN (SELECT id FROM aggregation_types WHERE name=" + "\'" + aggregation + "\') ";
		
		return query;
	}
	
	/**
	 * Get aggregation selection for SELECTs
	 * @param query
	 * @param arguments
	 * @param aggregation
	 * @return
	 */
	public static String getAggregationSelection(String query, Long aggregationId) {
				
						
		if ( query.indexOf("WHERE") < 0 ) {
			query += " WHERE ";
		}
		else
		{
			query += " AND ";
		}
			
		query += "agg_type_id=" + aggregationId; 
		
		return query;
	}
	

	/**
	 * Get Scenario and Run selection for SELECTs
	 * @param query
	 * @param arguments
	 * @return
	 */
	public static String getScenarioAndRunSelection(String query, String[] arguments) {
		
		if ( arguments.length == 0 ) return query;
		
		if ( query.indexOf("WHERE") < 0 ) {
			query += " WHERE ";
		}
		else
		{
			query += " AND ";
		}
				
		int runIDStart;
		int runIDStop;

			query += "app_run_id IN (SELECT id FROM simulation_runs WHERE scenario_id=" + arguments[0] ;
				
		
		if (arguments.length == 1) {
			
			query += ")";
		}
					

		if  (arguments.length == 2) {
			
			int index = -1;
			
			String temp1, temp2;
			
			if ( (index = arguments[1].indexOf('-')) > 0) {
				
				temp1 = arguments[1].substring(0,index);
				
				temp2 = arguments[1].substring(index+1);

				
				try
			    {
				 runIDStart = Integer.parseInt(temp1.trim());		 
				 runIDStop  = Integer.parseInt(temp2.trim());
				 
				 query += " AND run_number >=" + runIDStart;
				 query += " AND run_number <=" + runIDStop;
				 query += ")";
					
			    }
			    catch (NumberFormatException nfe)
			    {
			      
			    }
				
			} 
			else {				
				
				 try
				    {
					 
					 runIDStart = Integer.parseInt(arguments[1].trim());
					 
					 query += " AND run_number=" + runIDStart;
					 query += ")";
					
				    }
				    catch (NumberFormatException nfe)
				    {
				      
				    }
			}
			
		} else if (arguments.length == 3) {			
				
			try
		    {
				
			 runIDStart = Integer.parseInt(arguments[1].trim());
			 runIDStop = Integer.parseInt(arguments[2].trim());
			 
			 query += " AND app_run_id >= " + runIDStart;
			 query += " AND app_run_id <= " + runIDStop;
			 query += ")";

		    }
		    catch (NumberFormatException nfe)
		    {
		      
		    }

		} 
				
			return query;
	}
	
		
	   /**
     * form an SQL query to select rows for CSV output
     * @param table name, arguments
     * @return string
     */
public static String getSQLQueryForCSV(String table, String[] arguments) {
	
	// Form a query
	Long l = new Long(5);
	
	 return getScenarioAndRunSelection(getAggregationSelection("SELECT * FROM " + table, (String)null), arguments); //(String)null

	} 
	

/**
 * Outputs a single row to a csv file  
 *
 * @throws TorqueException Any exceptions caught during processing will be
 *         rethrown wrapped into a TorqueException.
 */
public static void populateObject(Record row, PrintWriter out)
    throws TorqueException
{
	int num = row.size();

    try
    {
        for(int i=0; i < num -1; i++) {
        	out.print(row.getValue(1 + i).asString());
			out.print(",");			

        }
        
        out.println(row.getValue(num).asString());
    	
    }
    catch (DataSetException e)
    {
        throw new TorqueException(e);
    }
}




/**
 * Calls populateObject for each row.
 *
 * @throws TorqueException Any exceptions caught during processing will be
 *         rethrown wrapped into a TorqueException.
 */
public static int populateObjects(List records, PrintWriter out )
    throws TorqueException
{
    // populate the object(s)
    for (int i = 0; i < records.size(); i++)
    {
        Record row = (Record) records.get(i);
        populateObject(row, out);
    }
    return records.size();
}

/**
 * Method to do selects and output csv data to the stream.
 *
 * @param  SELECT statement, stream.
 * @return 
 * @throws TorqueException Any exceptions caught during processing will be
 *         rethrown wrapped into a TorqueException.
 */
public static int doSelect(String query, PrintWriter out ) throws TorqueException
{
	 
	 return populateObjects(BasePeer.executeQuery(query), out);
	 
}   
	
/**
 * Outputs data to a csv file
 * @throws SQLException
 * @throws IOException
 * @throws TorqueException 
 */
public static void outputToCSV(String table, List columnNames, String[] arguments) throws IOException, TorqueException {
	
	
//	LinkDataTotal temp = new LinkDataTotal();
//	temp.getColumnNumber("a");
	
	String fileName = new String(table + ".csv");  

	
	FileWriter fw = new FileWriter(fileName);
	PrintWriter out = new PrintWriter(fw);
	
	
	// Output column names
	Iterator<String> name = columnNames.iterator();

	while (name.hasNext())
	{
		String columnName = (String)name.next();
		
		if (name.hasNext())
		{
			out.print(columnName);
			out.print(",");
		}
		else
		{
			out.println(columnName);
		}
	}
	
	
	// Execute SQLQuery and output to csv

	AggregateData.reportToStandard("Query: " + getSQLQueryForCSV(table, arguments) );	
	
	int numRecords = doSelect(getSQLQueryForCSV(table, arguments), out);
	
	AggregateData.reportToStandard("File name: " + fileName );
	AggregateData.reportToStandard("Total data records in the csv file: " + numRecords );
	
   //Flush the output to the file
   out.flush();
       
   //Close the Print Writer
   out.close();
       
   //Close the File Writer
   fw.close();       	
	
	
}

}
