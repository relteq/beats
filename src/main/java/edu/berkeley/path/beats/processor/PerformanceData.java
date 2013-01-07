/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

/****************************************************************************/
/************        Author: Alexey Goder alexey@goder.com  *****************/
/************                    Dec 10, 2012               *****************/
/****************************************************************************/

package edu.berkeley.path.beats.processor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

import com.itextpdf.text.Paragraph;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import edu.berkeley.path.beats.om.Routes;

public class PerformanceData extends AggregateData {
	

	
	public static void doPerformance(String[] arguments) throws TorqueException, IOException, DataSetException  {
			
		doPerformance("link_performance_total", arguments);
		doPerformance("link_performance_detailed", arguments);
		doRoutePerformance(arguments);
		
	}
	
	/**
	 * Make sure the route ID does exist
	 * @param routeId
	 * @return
	 */
	public static Long checkRouteId(Long routeId) {
		
		try {
			
			if ( BasePeer.executeQuery("SELECT id FROM routes WHERE id=" + routeId).size() == 0 ) {
				
				Routes obj = new Routes();
				
				obj.setId(routeId);
				obj.setName("Default");
				obj.setCreatedBy("Alexey");
				obj.setCreated(new java.util.Date());
				obj.setProjectId((long)0);
				
				
				try {
					
					obj.save();
					reportToStandard("New Route ID created: " + routeId);
					return routeId;
					
				} catch (Exception e) {
					
					e.printStackTrace();
					return null;
				}
			}
		} catch (TorqueException e) {
			
			e.printStackTrace();
			return null;
		}
		
		return routeId;
	}
	
	
	/**
	 * Execute Route Performance
	 * @param arguments
	 * @throws IOException
	 * @throws TorqueException
	 * @throws DataSetException
	 */
	public static void doRoutePerformance(String[] arguments) throws IOException, TorqueException, DataSetException {
	
		
		int numOfProcessedRows = 0;
		int nProcessed=0;
		
		reportToStandard("Performance calculation: route_performance");
		List listOfRoutes;
		
		
		// get a list of routes for the selection
		//String query = getScenarioAndRunSelection(getAggregationSelection("select distinct " + getListOfKeys(getSourceTable(table)) + " from "  + getSourceTable(table), "raw") ,arguments );
		
		String query =  getScenarioAndRunSelection(getAggregationSelection("SELECT DISTINCT link_id FROM link_performance_total", "raw"), arguments);
		String routeQuery = "SELECT DISTINCT route_id FROM route_links WHERE link_id IN (" + query + ")";
					
		reportToStandard("Key query: " + routeQuery);
		listOfRoutes = BasePeer.executeQuery(routeQuery);	
		
		int numberOfRoutes = listOfRoutes.size();			
		reportToStandard("Number of routes: " + numberOfRoutes);
		
		if ( numberOfRoutes < 1 ) numberOfRoutes = 1; 
		
		// Clean previously processed data
		CleanPreviousAggregation("route_performance_total", arguments, (String)null);

		java.util.List<Record> originalData;
		
		for (int i=0; i < numberOfRoutes; i++ ) {
		
			Long routeId;		// Just in case the route table is empty
			routeId = (long) 0;
			nProcessed=0;
			
			if ( listOfRoutes.size() > 0 )
				routeId = ((Record)listOfRoutes.get(i)).getValue(1).asLong(); 
			
			checkRouteId(routeId);
			
			// Get data set
			
			String linkQuery = "SELECT link_id FROM route_links WHERE route_id=" + routeId;
			query = "SELECT app_run_id, app_type_id, ts, agg_type_id, value_type_id, SUM(travel_time) FROM link_performance_total ";
			query = getScenarioAndRunSelection(getAggregationSelection(query, "raw"), arguments);
			if ( listOfRoutes.size() > 0 ) 
				query += " AND link_id IN (" + linkQuery + ")";
			query += "  GROUP BY app_run_id, app_type_id, ts, agg_type_id, value_type_id";
			
			reportToStandard("Link query: " + query);
			
			originalData = BasePeer.executeQuery(query);
			
			reportToStandard("Route #"+routeId + "  Data Size: " + originalData.size());
			
			for (int j=0; j<originalData.size(); j++) {
			
				RoutePerformanceTotal obj = new RoutePerformanceTotal();
		
				nProcessed += obj.savePerformanceData(routeId, originalData.get(j));
				
			}	
			
			reportToStandard("Key combination " + (i+1) + " of " + numberOfRoutes + ": processed " +nProcessed + " records");
			numOfProcessedRows += nProcessed;
		}
		
		reportToStandard("Total records processed: " + numOfProcessedRows + "\n");
	}
	
	
	/**
	  * compute performance measures
	  * @param table name, array of arguments
	  * @return string
	  * @throws DataSetException 
	  */	
	public static void doPerformance(String table, String[] arguments) throws IOException, TorqueException, DataSetException {
		
		reportToStandard("Performance calculation: " + table);
		
		// get a list of keys for the selection
		String query = getScenarioAndRunSelection(getAggregationSelection("select distinct " + getListOfKeys(getSourceTable(table)) + " from "  + getSourceTable(table), "raw") ,arguments );
		
		List listOfKeys = BasePeer.executeQuery(query);		
		
		// define main query
		query = getScenarioAndRunSelection(getAggregationSelection(" FROM " + getSourceTable(table), "raw") ,arguments);
		
		int numOfProcessedRows = 0;
		int nProcessed=0;

		
		reportToStandard("Unique key combinations: " + listOfKeys.size());
		
		
		// Clean previously processed data
		CleanPreviousAggregation(table, arguments,(String)null);
		
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
						String mainQuery = setKeys(query, getSourceTable(table), (Record)listOfKeys.get(i));
						
						originalData = BasePeer.executeQuery( "SELECT * " + mainQuery + " ORDER BY ts ASC" );

						reportToStandard("Data size: " + originalData.size() + setKeys("", getSourceTable(table), (Record)listOfKeys.get(i)) );
/*						for ( int j=1; j<=((Record)originalData.get(0)).size(); j++) {
							AggregateData.reportToStandard("Raw data: j=" + j + " " +   ((Record)originalData.get(0)).getValue(j).asString() );
						}
						
*/						
						if (originalData.size() > 1 ) {
							
							long 	timestamp = 0;
							double 	linkLength = 	getLinkLength(getSourceTable(table), (Record)originalData.get(0));;	
							int 	lanes = 		getNumberOfLanes(getSourceTable(table), (Record)originalData.get(0));
							
							for ( int ii=0; ii<originalData.size(); ii++) {
								
								timestamp = calculateAndSave(table, ii, originalData, linkLength, timestamp, lanes);
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


protected static long calculateAndSave(String table, int recordNumber, List data, double linkLength, long previousTs, int lanes)  {

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
	public static BigDecimal delay(BigDecimal vht, BigDecimal vmt, BigDecimal freeFlowSpeed) {
		
		if ( freeFlowSpeed == null || freeFlowSpeed.doubleValue() <= 1E-10 )  return new BigDecimal(0);
		else {

			double x = vht.doubleValue() - vmt.doubleValue()/freeFlowSpeed.doubleValue();
			
			if ( x <1E-3 ) x = 0.0; // assuming that delay less than 1 ms does not make sense 
			
			return new BigDecimal(x);
		}
		
	}
	
	
	/**
	 * calculates equation 3.8
	 * @param density
	 * @param length
	 * @param aggregation
	 * @return Productivity Loss
	 */
	public static BigDecimal productivityLoss(BigDecimal out_flow, BigDecimal capacity, int lanes, double length, long timeDelta) {
		

		if ( capacity.doubleValue() <= 1E-10 )  return null;
		else {

			// According to Alex K, out_flow is per output time delta not per sec
			
			return  (  BigDecimal.valueOf(( 1.0 -  (out_flow.doubleValue()/timeDelta / capacity.doubleValue()) ) * lanes * length * timeDelta /1000.0 ) );
			
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
			
			twoKeys= twoKeys.replaceFirst("AND id", "AND link_id"); // link_lanes table uses link_id as a key while links table uses id for same thing.  Isn't it stupid?
			
			return ((Record) BasePeer.executeQuery("SELECT lanes FROM link_lanes WHERE lanes > 0 "+ twoKeys).get(0)).getValue(1).asInt();
					
		} catch (Exception e) {
			
			e.printStackTrace();
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
				
				e.printStackTrace();
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
		 * calculates Actual Travel Time and Worst Travel Time.  Equation 3.4
		 * @param speedData
		 * @param linkLength
		 * @return
		 */			
		public static BigDecimal actualTravelTime(int recordNumber, List speedData, double linkLength, int speedColumn, int tsColumn ) {
			
			double distance =0.0;
			BigDecimal v;
			double remainingTravelTime;
		
			try {
				
				for (int i=recordNumber; i < speedData.size(); i++) {
					
								
					v = ((Record)speedData.get(i)).getValue(speedColumn).asBigDecimal() ;
					 
					 if ( v != null  ) distance += v.doubleValue() * getRawTimeDeltaInSeconds(speedData , i, tsColumn);								
					
					if ( distance >= linkLength ) {						
						
						if ( v.doubleValue()>1E-10 ) {			
							
							remainingTravelTime = (distance - linkLength)/v.doubleValue();
						}
						else 
							remainingTravelTime = 0.0;
						
						// stop calculation.  
						return BigDecimal.valueOf(getRawTimeDeltaInSeconds(speedData, recordNumber, i, tsColumn) + remainingTravelTime);
						
					}
				}
				
				return new BigDecimal(0);
				
			} catch (DataSetException e) {
				
				return new BigDecimal(0);
			}
		}
		
		
		
		/**
		 * get raw time interval for a specific record number (it may change from record to record)
		 * @param speedData
		 * @param recordNumber
		 * @return time delta in hours
		 */
		public static double getRawTimeDeltaInSeconds(List speedData, int recordNumber, int tsColumn ) {

			if ( recordNumber < speedData.size() - 1 ) {

				return getRawTimeDeltaInSeconds(speedData, recordNumber, recordNumber+1, tsColumn );
			}
			else {
				
				return getRawTimeDeltaInSeconds(speedData, recordNumber-1, recordNumber, tsColumn );	
			}

		}
		
		
		/**
		 * get raw time interval for a specific record number (it may change from record to record)
		 * @param speedData
		 * @param recordNumber
		 * @return time delta in seconds
		 */
		public static double getRawTimeDeltaInSeconds(List speedData, int recordNumber1, int recordNumber2, int tsColumn ) {
					
			try {		

				return ((double)( ((Record)speedData.get(recordNumber2)).getValue(tsColumn).asTimestamp().getTime() 
						- ((Record)speedData.get(recordNumber1)).getValue(tsColumn).asTimestamp().getTime()) )
						/1000.0;
		
			} catch (DataSetException e) {
				
				return 0.0;
			}			

		}
	
}
