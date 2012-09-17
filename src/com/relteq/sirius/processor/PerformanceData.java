package com.relteq.sirius.processor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

import com.relteq.sirius.om.LinkPerformanceTotalPeer;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

public class PerformanceData extends AggregateData {
	

	
	public static void doPerformance(String[] arguments) throws TorqueException, IOException, DataSetException  {
		

		doPerformance("link_performance_detailed", arguments);
		doPerformance("link_performance_total", arguments);
		
	}
	
	/**
	  * execute data aggregation
	  * @param table name, array of arguments
	  * @return string
	  * @throws DataSetException 
	  */	
	public static void doPerformance(String table, String[] arguments) throws IOException, TorqueException, DataSetException {
		
		reportToStandard("Performance calculation: " + table);
		
		// get a list of keys for the selection
		String query = getScenarioAndRunSelection("select distinct " + getListOfKeys(getSourceTable(table)) + " from "  + getSourceTable(table) ,arguments, "raw" );
		
		List listOfKeys = BasePeer.executeQuery(query);		
		
		// define main query
		query = getScenarioAndRunSelection(" FROM " + getSourceTable(table) ,arguments, "raw" );
		
		int numOfProcessedRows = 0;
		int nProcessed=0;
		
		// perform aggregation
		
		reportToStandard("Unique key combinations: " + listOfKeys.size());
		
		// Loop by key combinations 
			
		for (int i=0; i < listOfKeys.size(); i++ ) {
			
			nProcessed=0;
			
		
			if ( getValueFromDB("COUNT(ts)", table, arguments, (Record)listOfKeys.get(i), "raw").asInt()  > 0 ) {
				
				reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + " has been previously processed");
			} 
			else {	
					List originalData;

					try {
						
						// Get data from the source table

						originalData = BasePeer.executeQuery( "SELECT * " + setKeys(query, getSourceTable(table), (Record)listOfKeys.get(i)) + " ORDER BY ts ASC" );

						if (originalData.size() > 0 ) {
							
							long 	timestamp = 0;
							double 	linkLength = 	getLinkLength(getSourceTable(table), (Record)originalData.get(0));;	
							int 	lanes = 		getNumberOfLanes(getSourceTable(table), (Record)originalData.get(0));
							
							for ( int ii=0; ii<originalData.size(); ii++) {
								
								timestamp = calculateAndSaved(table, ii, originalData, linkLength, timestamp, lanes);
								nProcessed++;
								
							}													
							
						}
				
					} catch (TorqueException e) {
						
						e.printStackTrace();
					
					}				
					
					numOfProcessedRows += nProcessed;
					
					reportToStandard("Key combination " + (i+1) + " of " + listOfKeys.size() + ": processed " +nProcessed + " records");	
				}

			}


		
		reportToStandard("Total records processed: " + numOfProcessedRows + "\n");
		
		return;
		
	}	


protected static long calculateAndSaved(String table, int recordNumber, List data, double linkLength, long previousTs, int lanes)  {

	try {
	if ( table.equals("link_data_total") ) {	
		
		return 0;
		
	} else
	if ( table.equals("link_data_detailed") ) {

		return 0;
		
	} else
	if ( table.equals("link_performance_detailed") ) {

		LinkPerformanceDetailed row = new LinkPerformanceDetailed();

			return row.savePerformanceData(recordNumber, data, linkLength, previousTs);
			
		
	} else
	if ( table.equals("link_performance_total") ) {

		LinkPerformanceTotal row = new LinkPerformanceTotal();
		return row.savePerformanceData(recordNumber, data, linkLength, previousTs, lanes);				
	} 
	

	
	} catch (Throwable e) {
		
		e.printStackTrace();
		return 0;
	}
	
	return 0;
}

	/**
	 * calculates equation 3.7
	 * @param density
	 * @param length
	 * @param aggregation
	 * @return delay
	 */
	public static BigDecimal delay(BigDecimal vht, BigDecimal vmt, BigDecimal speed) {
		
		
		if ( speed.doubleValue() <= 0 )  return null;
		else 
		return vht.subtract(vmt.divide(speed));
		
	}
	
	
	/**
	 * calculates equation 3.8
	 * @param density
	 * @param length
	 * @param aggregation
	 * @return Productivity Loss
	 */
	public static BigDecimal productivityLoss(BigDecimal out_flow, BigDecimal capacity, int lanes, double length, long timeDelta) {
		

		if ( capacity.doubleValue() <= 0 )  return null;
		else {
			// need to ad logic for  v and V
			
			return  (  BigDecimal.valueOf(
											( 1.0 -  (out_flow.doubleValue() / capacity.doubleValue()) ) * lanes * length * timeDelta /1000.0/60.0/60.0 
										 ) 
					);
			
			}
		}

	/**
	 * gets length of the link
	 * @param table name
	 * @param Record
	 * @return length
	 */

	public static int getNumberOfLanes(String table, Record rec)	{
		
		String twoKeys ="";
		
		try {			
			if ( table.equals("link_data_total") ) {		
	
				LinkDataTotal row = new LinkDataTotal();				

					twoKeys = row.getLinkIdAndNetwokId(rec);
					
			} else
			if ( table.equals("link_data_detailed") ) {
	
				LinkDataDetailed row = new LinkDataDetailed();
				twoKeys = row.getLinkIdAndNetwokId(rec);
				
			} else
			if ( table.equals("link_performance_detailed") ) {
	
				LinkPerformanceDetailed row = new LinkPerformanceDetailed();
				twoKeys = row.getLinkIdAndNetwokId(rec);
				
			} else
			if ( table.equals("link_performance_total") ) {
	
				LinkPerformanceTotal row = new LinkPerformanceTotal();
				twoKeys = row.getLinkIdAndNetwokId(rec);
			} 
			else {
				return 0;
			}
		
		
		} catch (Exception e) {
			
			return 0;
		}		
		
		try {
			
			return ((Record) BasePeer.executeQuery("SELECT lanes FROM links WHERE length>0 "+ twoKeys).get(0)).getValue(1).asInt();
					
		} catch (Exception e) {
			
			return 0;
		}
		

	}
			
		/**
		 * gets length of the link
		 * @param table name
		 * @param Record
		 * @return length
		 */
	
		public static double getLinkLength(String table, Record rec)	{
			
			String twoKeys ="";
			
			try {			
				if ( table.equals("link_data_total") ) {		
		
					LinkDataTotal row = new LinkDataTotal();	
					twoKeys = row.getLinkIdAndNetwokId(rec);
						
				} else
				if ( table.equals("link_data_detailed") ) {
		
					LinkDataDetailed row = new LinkDataDetailed();
					twoKeys = row.getLinkIdAndNetwokId(rec);
					
				} else
				if ( table.equals("link_performance_detailed") ) {
		
					LinkPerformanceDetailed row = new LinkPerformanceDetailed();
					twoKeys = row.getLinkIdAndNetwokId(rec);
					
				} else
				if ( table.equals("link_performance_total") ) {
		
					LinkPerformanceTotal row = new LinkPerformanceTotal();
					twoKeys = row.getLinkIdAndNetwokId(rec);
				} 
				else {
					return 0.0;
				}
					
			} catch (Exception e) {
				
				return 0.0;
			}
						
			try {

				return ((Record) BasePeer.executeQuery("SELECT length FROM links WHERE length>0"+ twoKeys).get(0)).getValue(1).asDouble();
				
				
			} catch (Exception e) {
				
				return 0.0;
			}
			
	
		}
		
		/**
		 * gets table name for source for data
		 * @param table name
		 * @return table name
		 */
	
		public static String getSourceTable(String table)	{
			
						
				if ( table.equals("link_data_total") ) {		
		
					return "link_data_total";
						
				} else
				if ( table.equals("link_data_detailed") ) {
		
					return "link_data_detailed";
					
				} else
				if ( table.equals("link_performance_detailed") ) {
		
					return "link_data_detailed";
					
				} else
				if ( table.equals("link_performance_total") ) {
		
					return "link_data_total";
				} 
				else {
					return "link_data_total";
				}
			
	
		}
		
		
	public static int getColumnNumber(String table, String name)	{
		
				
			if ( table.equals("link_data_total") ) {		
	
				LinkDataTotal row = new LinkDataTotal();				
				return row.getColumnNumber(name);
					
			} else
			if ( table.equals("link_data_detailed") ) {
	
				LinkDataDetailed row = new LinkDataDetailed();
				return row.getColumnNumber(name);
				
			} else
			if ( table.equals("link_performance_detailed") ) {
	
				LinkPerformanceDetailed row = new LinkPerformanceDetailed();
				return row.getColumnNumber(name);
				
			} else
			if ( table.equals("link_performance_total") ) {
	
				LinkPerformanceTotal row = new LinkPerformanceTotal();
				return row.getColumnNumber(name);
			} 
			else {
				return 0;
			}
			

	}
			
		
		/**
		 * calculates Actual Travel Time and Worst Travel Time
		 * @param speedData
		 * @param linkLength
		 * @param worst = 1 for regular speed and worst = 2 for worst speed
		 * @return
		 */			
		public static BigDecimal actualTravelTime(int recordNumber, List speedData, double linkLength, int speedColumn, int tsColumn ) {
			
			double distance =0.0;
			BigDecimal v;		
		
			try {
				
				for (int i=recordNumber; i < speedData.size(); i++) {
					
								
					v = ((Record)speedData.get(i)).getValue(speedColumn).asBigDecimal() ;
					 
					 if ( v != null  ) distance += v.doubleValue() * getRawTimeDeltaInHours(speedData , i, tsColumn);								
					
					if ( distance >= linkLength ) {
						
						// stop calculation
						return BigDecimal.valueOf(getRawTimeDeltaInHours(speedData, recordNumber, i, tsColumn));
						
					}
				}
				
				return null;
				
			} catch (DataSetException e) {
				
				return null;
			}
		}
		
		
		/**
		 * get raw time interval for a specific record number (it may change from record to record)
		 * @param speedData
		 * @param recordNumber
		 * @return time delta in hours
		 */
		public static double getRawTimeDeltaInHours(List speedData, int recordNumber, int tsColumn ) {

			if ( recordNumber < speedData.size() - 1 ) {

				return getRawTimeDeltaInHours(speedData, recordNumber, recordNumber+1, tsColumn );
			}
			else {
				
				return getRawTimeDeltaInHours(speedData, recordNumber-1, recordNumber, tsColumn );	
			}

		}
		
		/**
		 * get raw time interval for a specific record number (it may change from record to record)
		 * @param speedData
		 * @param recordNumber
		 * @return time delta in hours
		 */
		public static double getRawTimeDeltaInHours(List speedData, int recordNumber1, int recordNumber2, int tsColumn ) {
					
			try {		

				return ((double)( ((Record)speedData.get(recordNumber2)).getValue(tsColumn).asTimestamp().getTime() 
						- ((Record)speedData.get(recordNumber1)).getValue(tsColumn).asTimestamp().getTime()) )
						/1000.0/60.0/60.0;
		
			} catch (DataSetException e) {
				
				return 0.0;
			}			

		}
}

