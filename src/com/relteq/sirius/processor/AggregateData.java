package com.relteq.sirius.processor;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;


import com.relteq.sirius.db.OutputToCSV;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.workingdogs.village.Value;

public class AggregateData extends OutputToCSV {

	
	public static void reportToStandard(String s) {
		
		java.util.Date today = new java.util.Date();
			
		System.out.println(new java.sql.Timestamp(today.getTime()) + " " + s);
	}

	/**
	 * aggregate a single table for all intervals
	 * @param table
	 * @param arguments
	 * @throws IOException
	 * @throws TorqueException
	 * @throws DataSetException
	 */
	public static void doAggregateAllIntervals(String table, String[] arguments) throws IOException, TorqueException, DataSetException {
		
		doAggregate(table, arguments, "1min");
		doAggregate(table, arguments, "5min");
		doAggregate(table, arguments, "15min");
		doAggregate(table, arguments, "1hour");
		doAggregate(table, arguments, "1day");
		doAggregate(table, arguments, "total");
		
	}
	
	public static void doAggregateAllTables(String[] arguments) throws IOException, TorqueException, DataSetException {
		
		doAggregateAllIntervals("link_data_total", arguments);
		doAggregateAllIntervals("link_data_detailed", arguments);
		doAggregateAllIntervals("link_performance_detailed", arguments);
		doAggregateAllIntervals("link_performance_total", arguments);
		
	}
	
	/**
  * execute data aggregation
  * @param table name, array of arguments
  * @return string
	 * @throws DataSetException 
  */	
	public static void doAggregate(String table, String[] arguments, String aggregation) throws IOException, TorqueException, DataSetException {

		reportToStandard("Table: " +table + " Aggregation: " + aggregation);
		
		
		// get a list of keys for the selection
		String query = getScenarioAndRunSelection("select distinct " + getListOfKeys(table) + " from "  + table ,arguments, "raw" );

		
		List listOfKeys = BasePeer.executeQuery(query);		
		
		// define main query
		query = getScenarioAndRunSelection(" FROM " +table ,arguments, "raw" );
		int numOfAggregatedRows = 0;
		
		// perform aggregation
		
		reportToStandard("Unique key combinations: " + listOfKeys.size());
		
		// Loop by key combinations 
		// need to estimate performance for large tables and decide whether the loop by time should be outer or inner
			
		for (int i=0; i < listOfKeys.size(); i++ ) {
		
			if ( getValueFromDB("COUNT(ts)", table, arguments, (Record)listOfKeys.get(i), aggregation).asInt()  > 0 ) {
				
				reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + " has been previously aggregated");
			} 
			else {	
				
				int nAggregated=0;
				
				// Loop by time increment
				// Get min and max time stamp
				Timestamp minTimestamp = getValueFromDB("MIN(ts)", table, arguments, (Record)listOfKeys.get(i), "raw").asTimestamp();
				Timestamp maxTimestamp = getValueFromDB("MAX(ts)", table, arguments, (Record)listOfKeys.get(i), "raw").asTimestamp();

				
				Long delta = getAggregationInMilliseconds(aggregation);
				Long start =  (long)(Math.floor(minTimestamp.getTime()/delta) + .1)*delta;
				Long stop = maxTimestamp.getTime();
				
				if ( aggregation.equals("total") )	{
					
					start -= 1;
					delta  = stop - start;
			
				}

				
				for (Long time = start; time < stop; time += delta) {
				
					List originalData;
					List aggregatedData;	
					
					String currentQuery = setTimeInterval(setKeys(query,table,(Record)listOfKeys.get(i)), time, time+delta);
					
					try {
						
						// Get aggregated data for aggregation
						originalData = BasePeer.executeQuery("SELECT * " + currentQuery + " FETCH FIRST ROW ONLY");
						aggregatedData = BasePeer.executeQuery("SELECT" + getAggregationColumns(table ) + currentQuery);
						
						if (originalData.size() > 0 && aggregatedData.size() > 0 ) {
							
							// Save aggregated
							nAggregated += saveAggregated(table, aggregation, time+delta ,originalData, aggregatedData );
							
						}
				
					} catch (TorqueException e) {
						
						e.printStackTrace();
					
					}				
	
				}
				
				numOfAggregatedRows += nAggregated;
				reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + ": added " +nAggregated + " aggregated records" + " delta " + delta);
			}

		}
		
		reportToStandard("Aggregated records: " + numOfAggregatedRows+ "\n");
		
	}

	/**
	 * return aggregation interval in ms
	 * @param aggregation
	 * @return
	 */
public static long getAggregationInMilliseconds(String aggregation) {
	
	if ( aggregation.equals("1min") ) 	return 60*1000;
	if ( aggregation.equals("5min") ) 	return 5*60*1000;
	if ( aggregation.equals("15min") ) 	return 15*60*1000;
	if ( aggregation.equals("1hour") ) 	return 60*60*1000;
	if ( aggregation.equals("1day") ) 	return 24*60*60*1000;
	
	return 1;
}
	
	/**
	 * Save aggregated data
	 * @param query
	 * @param table
	 * @return
	 * @throws Exception
	 */
			
	protected static int saveAggregated(String table, String aggregation, Long time, List originalData, List aggregatedData )  {
		
		if ( table.equals("link_data_total") ) {		

			LinkDataTotal row = new LinkDataTotal();
			return row.saveAggregated(table, aggregation, time, originalData, aggregatedData);	
			
		} else
		if ( table.equals("link_data_detailed") ) {

			LinkDataDetailed row = new LinkDataDetailed();
			return row.saveAggregated(table, aggregation, time, originalData, aggregatedData);
			
		} else
		if ( table.equals("link_performance_detailed") ) {

			LinkPerformanceDetailed row = new LinkPerformanceDetailed();
			return row.saveAggregated(table, aggregation, time, originalData, aggregatedData);			
			
		} else
		if ( table.equals("link_performance_total") ) {

			LinkPerformanceTotal row = new LinkPerformanceTotal();
			return row.saveAggregated(table, aggregation, time, originalData, aggregatedData);				
		} 
		
		return 0;
	}
	
	/**
	 * Save aggregated data
	 * @param query
	 * @param table
	 * @return
	 * @throws Exception
	 */
			
	public static String getAggregationColumns(String table )  {
		
		if ( table.equals("link_data_total") ) {		

			return " SUM(in_flow), SUM(out_flow), AVG(density), AVG(occupancy), AVG(speed), SUM(in_flow_worst), SUM(out_flow_worst), AVG(density_worst), AVG(occupancy_worst), AVG(speed_worst) ";
			
		} else
		if ( table.equals("link_data_detailed") ) {

			return " SUM(in_flow), SUM(out_flow), AVG(density), AVG(occupancy), AVG(speed), SUM(in_flow_worst), SUM(out_flow_worst), AVG(density_worst), AVG(occupancy_worst), AVG(speed_worst) ";
			
		} else
		if ( table.equals("link_performance_detailed") ) {

			return " AVG(vmt), AVG(vht), AVG(delay), AVG(vmt_worst), AVG(vht_worst), AVG(delay_worst) ";			
			
		} else
		if ( table.equals("link_performance_total") ) {

			
			return " AVG(vmt), AVG(vht), AVG(delay), AVG(travel_time), AVG(productivity_loss), AVG(los), AVG(vc_ratio), AVG(vmt_worst), AVG(vht_worst), AVG(delay_worst), AVG(travel_time_worst), AVG(productivity_loss_worst), AVG(los_worst), AVG(vc_ratio_worst) ";				
		} 
		
		return "*";
	}
	
	/**
	 * add time interval to the query condition
	 * @param query
	 * @param time1
	 * @param time2
	 * @return
	 */
public static String setTimeInterval(String query, long time1, long time2)	{
	
	Timestamp ts1= new Timestamp(time1);
	Timestamp ts2= new Timestamp(time2);
	

	
	if ( query.indexOf("WHERE") < 0 ) {
		
		return query + " WHERE TIMESTAMP(ts)>TIMESTAMP(\'" + ts1.toString() + "\')" + " AND TIMESTAMP(ts)<=TIMESTAMP(\'" + ts2.toString() + "\')";
		
	} else {			
	
		return query + " AND TIMESTAMP(ts)>TIMESTAMP(\'" + ts1.toString() + "\')" + " AND TIMESTAMP(ts)<=TIMESTAMP(\'" + ts2.toString() + "\')";
	}
	
}

	
	/**
	 * adds link_id and network_id to the selection where statement
	 * @param query
	 * @param table
	 * @param record
	 * @return
	 */
	public static String setKeys(String query,String table, Record rec) {
		
		if ( query.indexOf("WHERE") < 0 ) return query += " WHERE ";
			
			return query + getListOfKeys(table, rec);
		
	}
	
	/**
  * execute query returning the first value from the first record
  * @param commnad, table name, array of arguments, list of keys, aggregation
  * @return string
	 * @throws DataSetException 
  */
	protected static Value getValueFromDB(String cmd, String table, String[] arguments, Record rec, String aggregation) throws TorqueException, DataSetException {	
		
		String query = getScenarioAndRunSelection("SELECT " + cmd +" FROM " + table, arguments, aggregation);
		
		query = setKeys(query, table, rec);
		
    	return ((Record) BasePeer.executeQuery(query).get(0)).getValue(1);
	}
				
			


	
	/**
	 * returns list of keys for SELECT statement
	 * @param table
	 * @return String
	 */
	public static String getListOfKeys(String table)  {
		
		try {	
			
			if ( table.equals("link_data_total") ) {		
	
				LinkDataTotal row = new LinkDataTotal();
	
					return row.getListOfKeys();
	
				
			} else
			if ( table.equals("link_data_detailed") ) {
	
				LinkDataDetailed row = new LinkDataDetailed();
				return row.getListOfKeys();
				
			} else
			if ( table.equals("link_performance_detailed") ) {
	
				LinkPerformanceDetailed row = new LinkPerformanceDetailed();
				return row.getListOfKeys();
				
			} else
			if ( table.equals("link_performance_total") ) {
	
				LinkPerformanceTotal row = new LinkPerformanceTotal();
				return row.getListOfKeys();
			} 
		
		} catch (TorqueException e) {


			return "*";
		}
		
		return "*";
		
	}
	
	
	/**
	 * returns list of keys for WHERE statement
	 * @param table
	 * @return String
	 */
	public static String getListOfKeys(String table, Record rec)  {
		
		try {	
			
			if ( table.equals("link_data_total") ) {		
	
				LinkDataTotal row = new LinkDataTotal();
	
				
					return row.getListOfKeys(rec);
				
				
			} else
			if ( table.equals("link_data_detailed") ) {
	
				LinkDataDetailed row = new LinkDataDetailed();
				return row.getListOfKeys(rec);
				
			} else
			if ( table.equals("link_performance_detailed") ) {
	
				LinkPerformanceDetailed row = new LinkPerformanceDetailed();
				return row.getListOfKeys(rec);
				
			} else
			if ( table.equals("link_performance_total") ) {
	
				LinkPerformanceTotal row = new LinkPerformanceTotal();
				return row.getListOfKeys(rec);
			} 
		
		} catch (TorqueException e) {


			return " ";
		}
		catch (DataSetException e) {
			return " ";
		}

		
		return " ";
		
	}
	
		
	
}


