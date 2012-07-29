package com.relteq.sirius.processor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.SimpleKey;

import com.relteq.sirius.om.LinkDataDetailedPeer;
import com.relteq.sirius.om.LinkDataTotalPeer;
import com.relteq.sirius.om.LinkPerformanceTotalPeer;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

public class LinkPerformanceTotal extends com.relteq.sirius.om.LinkPerformanceTotal {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1435985890503127384L;


	/**
	 * calculates and saves performance measures
	 * @param originalData
	 * @param linkLength
	 * @param previousTs
	 * @return current time stamp
	 * @throws Exception 
	 */
	public long savePerformanceData(int recordNumber, List data, double linkLength, long previousTs, int lanes) throws Exception {
		
		LinkDataTotal obj = new LinkDataTotal();
		
		long timeDelta =0;
		double timeDeltaInHours =0.0;
				
			LinkDataTotalPeer.populateObject((Record)data.get(recordNumber), 1, obj);
			
			setPrimaryKey(obj.getPrimaryKey());
			
			/* 
			 
			Source LinkDataTotal
	        pks[0] = SimpleKey.keyFor(getLinkId());
	        pks[1] = SimpleKey.keyFor(getNetworkId());
	        pks[2] = SimpleKey.keyFor(getDataSourceId());
	        pks[3] = SimpleKey.keyFor(getTs());
	        pks[4] = SimpleKey.keyFor(getAggregation());
	        
	        Destination LinkPerformanceTotal
	        pks[0] = SimpleKey.keyFor(getLinkId());
	        pks[1] = SimpleKey.keyFor(getNetworkId());
	        pks[2] = SimpleKey.keyFor(getDataSourceId());
	        pks[3] = SimpleKey.keyFor(getTs());
	        pks[4] = SimpleKey.keyFor(getAggregation());
 
	        
	        */
			
			if ( obj.getOutFlow() != null )
				setVmt(obj.getOutFlow().multiply(BigDecimal.valueOf(linkLength)));
			
	//		AggregateData.reportToStandard("Flow worst: " + obj.getOutFlowWorst());
			
			if ( obj.getOutFlowWorst() != null )
				setVmtWorst(obj.getOutFlowWorst().multiply(BigDecimal.valueOf(linkLength)));
			
			if ( previousTs > 0 ) timeDelta = getTs().getTime() - previousTs;
			timeDeltaInHours = timeDelta / 1000.0/60.0/60.0;
			
			if ( obj.getDensity() != null )
				setVht(obj.getDensity().multiply(BigDecimal.valueOf(timeDeltaInHours))); 
			
			if ( obj.getDensityWorst() != null )
				setVhtWorst(obj.getDensityWorst().multiply(BigDecimal.valueOf(timeDeltaInHours))); 
			
			if ( obj.getSpeed() != null && getVht() !=null && getVmt() !=null )
				setDelay(PerformanceData.delay(getVht(), getVmt(), obj.getSpeed()));
			
			if ( obj.getSpeedWorst() != null && getVhtWorst() !=null && getVmtWorst() !=null )
				setDelayWorst(PerformanceData.delay(getVhtWorst(), getVmtWorst(), obj.getSpeedWorst()));
			
			if ( obj.getOutFlow() != null && obj.getCapacity() != null)
				setProductivityLoss( PerformanceData.productivityLoss(obj.getOutFlow(), obj.getCapacity(), lanes, linkLength, timeDelta) ); 
			
			if ( obj.getOutFlowWorst() != null && obj.getCapacity() != null)
				setProductivityLoss( PerformanceData.productivityLoss(obj.getOutFlowWorst(), obj.getCapacity(), lanes, linkLength, timeDelta) ); 
				
			setTravelTime(PerformanceData.actualTravelTime(recordNumber, data, linkLength, getColumnNumber("speed"), getColumnNumber("ts") ));
			setTravelTimeWorst(PerformanceData.actualTravelTime(recordNumber, data, linkLength, getColumnNumber("speed_worst"), getColumnNumber("ts") ));
			
			
			setNew(true);
			save();	
	
		return getTs().getTime();
	}
	
	/**
	 * populates aggregated object, sets calculate data and insert
	 * @param table
	 * @param aggregation
	 * @param time
	 * @param originalData
	 * @param aggregatedData
	 * @return number of rows processed
	 */
    
    public  int saveAggregated(String table, String aggregation, Long time, List originalData, List aggregatedData )  {
			
		Timestamp ts = new Timestamp(time);

		try {
			
			LinkPerformanceTotalPeer.populateObject((Record)originalData.get(0), 1, this);

			//Populate aggregated data
			//  AVG(vmt), AVG(vht), AVG(delay), AVG(travel_time), AVG(productivity_loss), AVG(los), AVG(vc_ratio), AVG(vmt_worst), AVG(vht_worst), AVG(delay_worst), AVG(travel_time_worst), AVG(productivity_loss_worst), AVG(los_worst), AVG(vc_ratio_worst) 			

			setByPeerName(table+"."+"vmt", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(1).asBigDecimal()));
			setByPeerName(table+"."+"vht", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(2).asBigDecimal()));
			setByPeerName(table+"."+"delay", 			(BigDecimal)(((Record)aggregatedData.get(0)).getValue(3).asBigDecimal()));
			setByPeerName(table+"."+"travel_time", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(4).asBigDecimal()));
			setByPeerName(table+"."+"productivity_loss",(BigDecimal)(((Record)aggregatedData.get(0)).getValue(5).asBigDecimal()));
			setByPeerName(table+"."+"los", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(6).asBigDecimal()));
			setByPeerName(table+"."+"vc_ratio", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(7).asBigDecimal()));
			setByPeerName(table+"."+"vmt_worst", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(8).asBigDecimal()));
			setByPeerName(table+"."+"vht_worst", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(9).asBigDecimal()));
			setByPeerName(table+"."+"delay_worst", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(10).asBigDecimal()));
			setByPeerName(table+"."+"travel_time_worst", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(11).asBigDecimal()));
			setByPeerName(table+"."+"productivity_loss_worst",	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(12).asBigDecimal()));
			setByPeerName(table+"."+"los_worst", 				(BigDecimal)(((Record)aggregatedData.get(0)).getValue(13).asBigDecimal()));
			setByPeerName(table+"."+"vc_ratio_worst", 			(BigDecimal)(((Record)aggregatedData.get(0)).getValue(14).asBigDecimal()));
							
			setByPeerName(table+"."+"aggregation",	aggregation);
			setByPeerName(table+"."+"ts",			ts);
			
			setNew(true);
			save();
			
			
		} catch (Exception e) {

			return 0;
		}

		return 1;
		
	}
    /**
     * returns list of primary keys with values except time stamp and aggregation
     * @return string
     * @throws TorqueException
     * @throws DataSetException 
     */
    public String getListOfKeys(Record rec) throws TorqueException, DataSetException {
    	
    	String str = new String("");
    	int n=1;
    	
    	ColumnMap[] columns = getTableMap().getColumns();
    	
    	for (int i=0; i< columns.length; i++) {

    		if ( columns[i].isPrimaryKey() ) {
    			
    			if ( columns[i].getColumnName().equals("ts") || columns[i].getColumnName().equals("aggregation") ) {
    				// do not include time stamp
    			}
    			else  {
    				
    			
    				// include key name and value
    				
    				str  += " AND " + columns[i].getColumnName() + "=\'" + rec.getValue(n++).asString() + "\'";	
    			}

    		}
    	}
    		
    	return str;
    }
    
    /**
     * returns list of primary keys with values except time stamp and aggregation
     * @return string
     * @throws TorqueException
     * @throws DataSetException 
     */
    public String getLinkIdAndNetwokId(Record rec) throws TorqueException, DataSetException {
    	
    	String str = new String("");
    	int n=1;
    	
    	ColumnMap[] columns = getTableMap().getColumns();
    	
    	for (int i=0; i< columns.length; i++) {

    		if ( columns[i].isPrimaryKey() ) {
    			
    			if ( columns[i].getColumnName().equals("ts") || columns[i].getColumnName().equals("aggregation") ) {
    				// do not include time stamp
    			}
    			else  {
    				
    			
    				// include key name and value
    				if (columns[i].getColumnName().equals("link_id")) {
    					str  += " AND id=\'" + rec.getValue(n).asString() + "\'";	
    				} else 
    				if (columns[i].getColumnName().equals("network_id")) {
    					str  += " AND network_id=\'" + rec.getValue(n).asString() + "\'";	
    				}
    					
    				n++;	
    					
    			}

    		}
    	}
    		
    	return str;
    }
    	
    	
    	   /**
         * returns list of primary keys except time stamp and aggregation
         * @return string
         * @throws TorqueException
         */
        public String getListOfKeys() throws TorqueException {
        	
        	String str = new String("");
        	
        	ColumnMap[] columns = getTableMap().getColumns();
        	
        	for (int i=0; i< columns.length; i++) {

        		if ( columns[i].isPrimaryKey() ) {
        			
        			if ( columns[i].getColumnName().equals("ts") || columns[i].getColumnName().equals("aggregation") ) {
        				// do not include time stamp
        			}
        			else  {
        				// include key name
        				if (str.length() > 1 ) str += ", ";
        				
        				str  += columns[i].getColumnName();	
        			}

        		}
        		
        	}
        	  	
    	  	
    	
    	return str;
    }
        
	/**
	 * returns column number for given name
	 * @param name
	 * @return
	 */
     public int getColumnNumber(String name) {    	
     	
     	ColumnMap[] columns;
		try {			
				columns = getTableMap().getColumns();
			    	
		     	for (int i=0; i< columns.length; i++) {
		 			
		 			if ( columns[i].getColumnName().equals(name)  ) return i+1;	   		
	     	}
	     	
			} catch (TorqueException e) {
				
				return 0;
			}
     	    	 	
		return 0;
     }

}
