package com.relteq.sirius.db;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;


import com.relteq.sirius.processor.*;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.workingdogs.village.Value;

public class AggregateData {

	
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
		
		
		// get a list of LinkID and NetworkID pairs for the selection
		String query = OutputToCSV.getScenarioAndRunSelection("select distinct link_id, network_id, data_source_id from "  + table ,arguments, "raw" );
		List listOfKeys = BasePeer.executeQuery(query);		
		
		// define main query
		query = OutputToCSV.getScenarioAndRunSelection(" FROM " +table ,arguments, "raw" );
		int numOfAggregatedRows = 0;
		
		// perform aggregation
		
		reportToStandard("Unique key combinations: " + listOfKeys.size());
		
		// Loop by key combinations LinkID,NetworkID, SourceID
		// need to estimate performance for large tables and decide whether the loop by time should be outer or inner
			
		for (int i=0; i < listOfKeys.size(); i++ ) {
			
			String linkId = 	((Record)listOfKeys.get(i)).getValue(1).asString();
			String networkId = 	((Record)listOfKeys.get(i)).getValue(2).asString();
			String sourceId = 	((Record)listOfKeys.get(i)).getValue(3).asString();
			
			if ( getValueFromDB("COUNT(ts)", table, arguments, linkId, networkId, sourceId, aggregation).asInt() > 0 ) {
				
				reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + " has been previously aggregated");
			} 
			else {	
				
				int nAggregated=0;
				
				// Loop by time increment
				// Get min and max time stamp
				Timestamp minTimestamp = getValueFromDB("MIN(ts)", table, arguments, linkId, networkId,sourceId).asTimestamp();
				Timestamp maxTimestamp = getValueFromDB("MAX(ts)", table, arguments, linkId, networkId,sourceId).asTimestamp();

				
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
					
					String currentQuery = setTimeInterval(setKeys(query,linkId,networkId), time, time+delta);
					
					try {
						
						// Get aggregated data for aggregation
						originalData = BasePeer.executeQuery("SELECT * " + currentQuery + " FETCH FIRST ROW ONLY");
						aggregatedData = BasePeer.executeQuery("SELECT SUM(in_flow),SUM(out_flow),AVG(density),AVG(occupancy),AVG(speed) " + currentQuery);
						
						if (originalData.size() > 0 && aggregatedData.size() > 0 ) {
							
							// Save aggregated
							nAggregated += saveAggregated(table, aggregation, time+delta ,originalData, aggregatedData );
							
						}
				
					} catch (TorqueException e) {
						
						e.printStackTrace();
					
					}				
	
				}
				
				numOfAggregatedRows += nAggregated;
				reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + ": added " +nAggregated + " aggregated records");
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
			
	private static int saveAggregated(String table, String aggregation, Long time, List originalData, List aggregatedData )  {
		
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
	 * adds link_id, network_id, and data_source_id to the selection statement
	 * @param query
	 * @param linkId
	 * @param networkId
	 * @return
	 */
	public static String setKeys(String query,String linkId, String networkId) {
		
		if ( query.indexOf("WHERE") < 0 ) {
			
			return query + " WHERE link_id=\'" + linkId + "\'" + " AND network_id=\'" + networkId + "\'";
			
		} else {			
		
			return query + " AND link_id=\'" + linkId + "\'" + " AND network_id=\'" + networkId + "\'";
		}
	}
	
	/**
	 * adds link_id and network_id to the selection where statement
	 * @param query
	 * @param linkId
	 * @param networkId
	 * @return
	 */
	public static String setKeys(String query,String linkId, String networkId, String sourceId) {
		
		if ( query.indexOf("WHERE") < 0 ) {
			
			return query + " WHERE link_id=\'" + linkId + "\' AND network_id=\'" + networkId + "\' AND data_source_id=\'" +sourceId + "\'";
			
		} else {			
		
			return query + " AND link_id=\'" + linkId + "\' AND network_id=\'" + networkId + "\' AND data_source_id=\'" +sourceId + "\'";
		}
	}
	
	/**
  * execute query returning the first value from the first record
  * @param commnad, table name, array of arguments. link_id, network_id, data_source_id, aggregation
  * @return string
	 * @throws DataSetException 
  */
	private static Value getValueFromDB(String cmd, String table, String[] arguments, String linkId, String networkId, String sourceId, String aggregation) throws TorqueException, DataSetException {	
		
		String query = OutputToCSV.getScenarioAndRunSelection("SELECT " + cmd +" FROM " + table, arguments, aggregation);
		
		query = setKeys(query, linkId, networkId);
		
    	return ((Record) BasePeer.executeQuery(query).get(0)).getValue(1);
	}
	
	
	
	/**
	  * execute query returning the first value from the first record
	  * @param commnad, table name, array of arguments. link_id, network_id, data_source_id
	  * @return string
		 * @throws DataSetException 
	  */
		private static Value getValueFromDB(String cmd, String table, String[] arguments, String linkId, String networkId, String sourceId ) throws TorqueException, DataSetException {	
			
			return getValueFromDB(cmd, table, arguments, linkId, networkId, sourceId, "raw");

		}
			

}


