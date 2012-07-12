package com.relteq.sirius.db;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

public class OutputToCSV {

	
	   /**
     * form an SQL query to select rows for CSV output
     * @param table name, arguments
     * @return string
     */
public static String getSQLQuery(String table, String[] arguments) {
		
	int runIDStart;
	int runIDStop;
	
		// Form a query
	 
	 String query = new String();
	 
	 query = "SELECT * FROM " + table + " ";
	 
		
		if (arguments.length > 0) {

			query += "WHERE data_source_id IN (SELECT data_source_id FROM simulation_runs WHERE scenario_id=";
			query += "\'" + arguments[0] + "\'";
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
				 
				 query += " AND run_number >= " + runIDStart;
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
					 
					query += " AND run_number = " + runIDStart;
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
			 
			 query += " AND run_number >= " + runIDStart;
			 query += " AND run_number <= " + runIDStop;
			 query += ")";

		    }
		    catch (NumberFormatException nfe)
		    {
		      
		    }

		} 
	
		
		return query;
		
	} 
	

/**
 * Outputs a single row to a csv file  
 *
 * @throws TorqueException Any exceptions caught during processing will be
 *         rethrown wrapped into a TorqueException.
 */
public static void populateObject(Record row, int numColumns, PrintWriter out)
    throws TorqueException
{
    try
    {
        for(int i=0; i < numColumns -1; i++) {
        	out.print(row.getValue(1 + i).asString());
			out.print(",");
        }
        
        out.println(row.getValue(numColumns).asString());
    	
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
public static int populateObjects(List records, int numColumns, PrintWriter out )
    throws TorqueException
{
    // populate the object(s)
    for (int i = 0; i < records.size(); i++)
    {
        Record row = (Record) records.get(i);
        populateObject(row, numColumns, out);
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
public static int doSelect(String query, int numColumns, PrintWriter out ) throws TorqueException
{

    //sample BasePeer.executeQuery("SELECT * FROM link_data_total WHERE link_data_total.data_source_id='764ba409-5255-494c-9aac-036dc970126c' ORDER BY link_data_total.ts ASC");
	 
	 return populateObjects(BasePeer.executeQuery(query), numColumns, out);
	 
}   
	
/**
 * Outputs data to a csv file
 * @throws SQLException
 * @throws IOException
 * @throws TorqueException 
 */
public static void outputToCSV(String table, List columnNames, String[] arguments) throws IOException, TorqueException {
	
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
	
	int numRecords = doSelect(getSQLQuery(table, arguments), columnNames.size(), out);
	
	System.out.println("File name: " + fileName );
	System.out.println("Total data records in the csv file: " + numRecords );
	
   //Flush the output to the file
   out.flush();
       
   //Close the Print Writer
   out.close();
       
   //Close the File Writer
   fw.close();       	
	
	
}

}
