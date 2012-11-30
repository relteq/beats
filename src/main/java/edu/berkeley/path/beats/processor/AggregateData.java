package edu.berkeley.path.beats.processor;



import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;


import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.workingdogs.village.Value;

import edu.berkeley.path.beats.db.OutputToCSV;
import edu.berkeley.path.beats.om.AggregationTypes;
import edu.berkeley.path.beats.om.AggregationTypesPeer;
import edu.berkeley.path.beats.om.LinkDataDetailedPeer;
import edu.berkeley.path.beats.om.LinkDataTotalPeer;

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
	
	/**
	 * Aggregates all tables all intervals
	 * @param arguments
	 * @throws IOException
	 * @throws TorqueException
	 * @throws DataSetException
	 */
	public static void doAggregateAllTables(String[] arguments) throws IOException, TorqueException, DataSetException {
		
		
	Criteria crit = new Criteria();
	
	Long del = new Long(2);
	
	crit.add(LinkDataTotalPeer.AGG_TYPE_ID, del); 
	LinkDataTotalPeer.doDelete(crit);
		
		doAggregateAllIntervals("link_data_total", arguments);
		doAggregateAllIntervals("link_data_detailed", arguments);
		doAggregateAllIntervals("link_performance_detailed", arguments);
		doAggregateAllIntervals("link_performance_total", arguments);
		
	}
		
	
	/**
	 * Get aggregation ID, or create one if does not exit
	 * @param aggregation
	 * @return
	 */
	public static Long getAggregationID(String aggregation) {
				
		Long newId = new Long(0);
		
			List aggList;
			try {
				
				aggList = BasePeer.executeQuery("SELECT id FROM aggregation_types WHERE name=\'" + aggregation + "\'");
				
				
				try {
					if (aggList.size() > 0 ) {
					
						return ((Record)(aggList.get(0))).getValue(1).asLong();
					}
					else {
						
						//  Add new aggregation ID
						AggregationTypes obj = new AggregationTypes();						
						newId = ((Record)BasePeer.executeQuery("SELECT MAX(id) FROM aggregation_types ").get(0)).getValue(1).asLong() + 1;
						
						obj.setId(newId);
						obj.setName(aggregation);
						obj.setCreatedBy("Alexey");
						obj.setCreated(new java.util.Date());
						try {
							
							obj.save();
							reportToStandard("New Aggregation ID created: " + newId);
							return newId;
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return newId;
						}
						
					}
				} catch (DataSetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				
			} catch (TorqueException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			

		return newId;
		
	}
	
	/**
  * execute data aggregation
  * @param table name, array of arguments
  * @return string
	 * @throws DataSetException 
  */	
	public static void doAggregate(String table, String[] arguments, String aggregation) throws IOException, TorqueException, DataSetException {
		
		reportToStandard("Table: " +table + " Aggregation: " + aggregation);
		
		Long aggregationId = getAggregationID(aggregation);
		Long aggregationRaw = getAggregationID("raw");
		
		CleanPreviousAggregation(table, arguments,  aggregationId);
		
		String aggregationColumns = getAggregationColumns(table);
		
		long readStartTime=0, readAverage=0, numOfReads=0;
		 
		// get a list of keys for the selection
		String query = getScenarioAndRunSelection(getAggregationSelection("select distinct " + getListOfKeys(table) + " from "  + table, aggregationRaw) ,arguments );

		
		List listOfKeys = BasePeer.executeQuery(query);		
		
		// define main query
		query = getScenarioAndRunSelection(getAggregationSelection(" FROM " +table, aggregationRaw) ,arguments);
		
		int numOfAggregatedRows = 0;
		
		// perform aggregation

		
		// Loop by key combinations 
		// need to estimate performance for large tables and decide whether the loop by time should be outer or inner
			
		for (int i=0; i < listOfKeys.size(); i++ ) {
				
			int nAggregated=0;
			
			// Loop by time increment
			// Get min and max time stamp
			Timestamp minTimestamp = getValueFromDB("MIN(ts)", table, arguments, (Record)listOfKeys.get(i), "raw").asTimestamp();
			Timestamp maxTimestamp = getValueFromDB("MAX(ts)", table, arguments, (Record)listOfKeys.get(i), "raw").asTimestamp();

			
			Long delta = getAggregationInMilliseconds(aggregation);
			Long start =  (long)( Math.floor(minTimestamp.getTime()/delta) )*delta;
			Long stop = maxTimestamp.getTime();
			
			if ( aggregation.equals("1day") ) {
				start = beginningOfTheDay(minTimestamp).getTime();
			}
				
		
			
			if ( aggregation.equals("total") )	{
				
				start -= 1;
				delta  = stop - start;
		
			}
			
			List originalData = BasePeer.executeQuery("SELECT * " + setKeys(query,table,(Record)listOfKeys.get(i)) + " FETCH FIRST ROW ONLY");  	// typical execution time 4 ms
		
			for (Long time = start; time < stop; time += delta) {			
				
				List aggregatedData;	
				
				String currentQuery = setTimeInterval(setKeys(query,table,(Record)listOfKeys.get(i)), time, time+delta);
				
				try {
										
					readStartTime = System.currentTimeMillis();
					
					
					// reportToStandard("Main Query: " + "SELECT" + getAggregationColumns(table ) + currentQuery );
					//reportToStandard("Rows for aggregation: " + ((Record)BasePeer.executeQuery("SELECT COUNT(TS) " + currentQuery).get(0)).getValue(1).asInt());
					
					aggregatedData = BasePeer.executeQuery("SELECT" + aggregationColumns + currentQuery); // typical execution time 6 ms
					
					readAverage += (System.currentTimeMillis()- readStartTime);
					numOfReads++;
					
					if (originalData.size() > 0 && aggregatedData.size() > 0 ) {

						// Save aggregated
						nAggregated += saveAggregated(table, aggregationId, time+delta ,originalData, aggregatedData );
						
					}
			
				} catch (TorqueException e) {
					
					e.printStackTrace();
				
				}				

			}
			
			numOfAggregatedRows += nAggregated;
			reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + ": added " +nAggregated + " aggregated records" );
			

		}
		
		if ( numOfReads > 0  ) reportToStandard("Average read time: " + (double)(readAverage/numOfReads) + " ms");
		reportToStandard("Aggregated records: " + numOfAggregatedRows+ "\n");
		
	}
	
	
	public static Timestamp beginningOfTheDay(Timestamp t) {	
		
		return Timestamp.valueOf(t.toString().substring(0,t.toString().indexOf(' ')+1) + "00:00:00.000000");
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
			
	protected static int saveAggregated(String table, Long aggregationId, Long time, List originalData, List aggregatedData )  {
		
		if ( table.equals("link_data_total") ) {		

			LinkDataTotal row = new LinkDataTotal();
			return row.saveAggregated(table, aggregationId, time, originalData, aggregatedData);	
			
		} else
		if ( table.equals("link_data_detailed") ) {

			LinkDataDetailed row = new LinkDataDetailed();
			return row.saveAggregated(table, aggregationId, time, originalData, aggregatedData);	
			
		} else
		if ( table.equals("link_performance_detailed") ) {

			LinkPerformanceDetailed row = new LinkPerformanceDetailed();
			return row.saveAggregated(table, aggregationId, time, originalData, aggregatedData);				
			
		} else
		if ( table.equals("link_performance_total") ) {

			LinkPerformanceTotal row = new LinkPerformanceTotal();
			return row.saveAggregated(table, aggregationId, time, originalData, aggregatedData);				
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
	
			LinkDataTotal temp = new LinkDataTotal();
			
			return listToString(temp.getColumnsForAggreagtion());			
				
		} else
		if ( table.equals("link_data_detailed") ) {

			LinkDataDetailed temp = new LinkDataDetailed();
			
			return listToString(temp.getColumnsForAggreagtion());	
			
		} else
		if ( table.equals("link_performance_detailed") ) {

			LinkPerformanceDetailed temp = new LinkPerformanceDetailed();
			
			return listToString(temp.getColumnsForAggreagtion());			
			
		} else
		if ( table.equals("link_performance_total") ) {
			
			LinkPerformanceTotal temp = new LinkPerformanceTotal();
			
			return listToString(temp.getColumnsForAggreagtion());				} 
		
		return "*";
	}
	
	/**
	 * Delete previously aggregated values
	 * @param table
	 * @param arguments
	 * @param aggregationId
	 */
	public static void CleanPreviousAggregation(String table, String[] arguments, Long aggregationId)  {	

		try {
			
			LinkDataTotalPeer.executeStatement(getScenarioAndRunSelection(getAggregationSelection("DELETE" + " FROM "  + table, aggregationId) ,arguments ));
		} catch (TorqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/**
	 * Delete previously aggregated values
	 * @param table
	 * @param arguments
	 * @param aggregationId
	 */
	public static void CleanPreviousAggregation(String table, String[] arguments, String aggregation)  {	

		try {
			
			LinkDataTotalPeer.executeStatement(getScenarioAndRunSelection(getAggregationSelection("DELETE" + " FROM "  + table, aggregation) ,arguments ));
		} catch (TorqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
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
		
		if ( query.indexOf("WHERE") < 0 ) return query += " WHERE " + getListOfKeys(table, rec);
			
			return query + getListOfKeys(table, rec);
		
	}
	
	/**
  * execute query returning the first value from the first record
  * @param commnad, table name, array of arguments, list of keys, aggregation
  * @return string
	 * @throws DataSetException 
  */
	protected static Value getValueFromDB(String cmd, String table, String[] arguments, Record rec, String aggregation) throws TorqueException, DataSetException {	
		
		String query = getScenarioAndRunSelection(getAggregationSelection("SELECT " + cmd +" FROM " + table, aggregation), arguments);
		
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
	
	  
	   /**
	    * Convert list to a command usable in a select query
	    * @param colList
	    * @return String
	    */
	   public static String listToString(ArrayList<String> colList ) {
		   
		   String str = new String("");
		
		   for (int i=0; i<colList.size(); i++) {
			   
			   if ( i>0 ) str += ",";
			   
			   if ( colList.get(i).indexOf("in_flow") > 0 || colList.get(i).indexOf("out_flow") > 0) {
				   
				   // this column should be summed
				   str += " SUM(" + colList.get(i) + ")";
				   
			   } else {
				   
				   // this column should be averaged
				   str += " AVG(" + colList.get(i) + ")";
			   }
			   
		   }
	 		   	   
		   return str;	   
	   }
	   		
	
}


