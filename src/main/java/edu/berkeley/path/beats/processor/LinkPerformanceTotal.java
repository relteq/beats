package edu.berkeley.path.beats.processor;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.map.ColumnMap;
import org.apache.torque.om.SimpleKey;

import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

import edu.berkeley.path.beats.om.LinkDataDetailedPeer;
import edu.berkeley.path.beats.om.LinkDataTotalPeer;
import edu.berkeley.path.beats.om.LinkPerformanceTotalPeer;

public class LinkPerformanceTotal extends edu.berkeley.path.beats.om.LinkPerformanceTotal {

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
		double timeDeltaInSeconds =0.0;
		
		LinkDataTotalPeer.populateObject((Record)data.get(recordNumber), 1, obj);
		
		setPrimaryKey(obj.getPrimaryKey());	
		
		
		if ( previousTs > 0 ) timeDelta = getTs().getTime() - previousTs;
		timeDeltaInSeconds = timeDelta / 1000.0;	
		
		// Equation 3.5 
		if ( obj.getDensity() != null && obj.getSpeed() != null)
			// According to Alex K, the Density value in the database is already multiplied by the length of the link
			setVmt(new BigDecimal(obj.getDensity().doubleValue() * obj.getSpeed().doubleValue() * /* linkLength */ timeDeltaInSeconds) );	
		else
			setVmt(new BigDecimal(0));
		
		// Equation 3.6
		if ( obj.getDensity() != null )
			// According to Alex K, the Density value in the database is already multiplied by the length of the link
			setVht( new BigDecimal(obj.getDensity().doubleValue() * /* linkLength */ timeDeltaInSeconds) ); 
		else
			setVht(new BigDecimal(0));
		
		// Equation 3.7
		if ( obj.getFreeFlowSpeed() != null && getVht() !=null && getVmt() !=null )
			setDelay(PerformanceData.delay(getVht(), getVmt(), obj.getFreeFlowSpeed()));
		else
			setDelay(new BigDecimal(0));
		
		// Equation 3.8
		if (obj.getSpeed() != null && obj.getSpeed().compareTo(obj.getFreeFlowSpeed()) == 0)
			setProductivityLoss(BigDecimal.valueOf(0.0));
		else {
				
			if ( obj.getOutFlow() != null && obj.getCapacity() != null && obj.getSpeed().compareTo(obj.getFreeFlowSpeed()) != 0  )
				setProductivityLoss( PerformanceData.productivityLoss(obj.getOutFlow(), obj.getCapacity(), lanes, linkLength, timeDelta) );
			else
				setProductivityLoss(new BigDecimal(0));
		}	
			
		
		if ( obj.getCapacity().doubleValue() > 0 && obj.getSpeed() != null)
			setVcRatio(new BigDecimal(obj.getSpeed().doubleValue() / obj.getCapacity().doubleValue()) );
		else
			setVcRatio(new BigDecimal(0) );
/*

    The value for the los field: need equation

 */
						
		setTravelTime(PerformanceData.actualTravelTime(recordNumber, data, linkLength, obj.getColumnNumber("speed"), obj.getColumnNumber("ts") ));			

///		AggregateData.reportToStandard("Performance: " + getTs() + " #" + recordNumber + " Vmt=" + getVmt() + " Vht=" + getVht() + " Delay="+getDelay() + " travelTime=" + getTravelTime() + " freeFlowSpeed=" + obj.getFreeFlowSpeed() );
		
		setNew(true);
		save();	
	
		return getTs().getTime();
	}
	

	
	/**
	 * populates aggregated object, sets calculated data and insert
	 * @param table
	 * @param aggregation ID
	 * @param time
	 * @param originalData
	 * @param aggregatedData
	 * @return number of rows processed
	 */
    
    public  int saveAggregated(String table, long aggregationId, Long time, List originalData, List aggregatedData )  {
			
		Timestamp ts = new Timestamp(time);
		
		ArrayList<String> colList = getColumnsForAggreagtion(); 

		try {
			
			// Use the originalData record to populate non-aggregated values of the this row.
			LinkPerformanceTotalPeer.populateObject((Record)originalData.get(0), 1, this);
			
			//Populate aggregated data
				
			for (int i=0; i < colList.size(); i++ ) {
				
				setByPeerName(table+"."+colList.get(i), (BigDecimal)(((Record)aggregatedData.get(0)).getValue(i+1).asBigDecimal()));
				
			}		
			
			setTs(ts);
			setAggTypeId(aggregationId);
			
			setNew(true);
			save();
			
			
		} catch (Exception e) {

			e.printStackTrace();
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
    			
    			if ( columns[i].getColumnName().equals("ts") || columns[i].getColumnName().equals("agg_type_id") ) {
    				// do not include time stamp or aggregation
    			}
    			else  {
    				    			
    				// include key name and value
    				
    				str  += " AND " + columns[i].getColumnName() + "=" + rec.getValue(n++).asString() ;	
    			}

    		}
    	}
    		
    	return str;
    }
   
    
    public static void removeNulls(edu.berkeley.path.beats.om.LinkPerformanceTotal obj) {
    	
    	
    	BigDecimal zero = new BigDecimal(0);
    	ColumnMap[] columns;
    	

    	try {
    		
    		columns = obj.getTableMap().getColumns();
    		
			for (int i=0; i< obj.getTableMap().getColumns().length; i++) {

	    		if ( columns[i].isPrimaryKey() ||  columns[i].isForeignKey() ) {
	    			
	    		} else {
					if ( obj.getByPosition(i) == null ) {
						
						try {
							obj.setByPosition(i, zero);
						} catch (TorqueException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	    		}
			}
			
		} catch (TorqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
	
    }
    
    
    /**
     * Creates a list of columns that should be aggregated
     * @return List of strings
     * @throws TorqueException
     * @throws DataSetException
     */
   public ArrayList<String> getColumnsForAggreagtion()  {
    	
	   ArrayList<String> colList = new ArrayList<String>();    	
    	
    	ColumnMap[] columns;
		try {
			columns = getTableMap().getColumns();
		
    	
    	for (int i=0; i< columns.length; i++) {

    		if ( columns[i].isPrimaryKey() ) {
    			
    		} else {
    			
    			if ( columns[i].getTorqueType().equals("DECIMAL") ) {
    				
    				colList.add(columns[i].getColumnName());
    				
    			}
    		}
    	}
    		
    	
		} catch (TorqueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	return colList;
		
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
    			
    			if ( columns[i].getColumnName().equals("ts") || columns[i].getColumnName().equals("agg_type_id") ) {
    				// do not include time stamp
    			}
    			else  {
    				
    			
    				// include key name and value
    				if (columns[i].getColumnName().equals("link_id")) {
    					str  += " AND id=" + rec.getValue(n).asString() ;	
    				} else 
    				if (columns[i].getColumnName().equals("network_id")) {
    					str  += " AND network_id=" + rec.getValue(n).asString() ;	
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
        			
        			if ( columns[i].getColumnName().equals("ts") || columns[i].getColumnName().equals("agg_type_id") ) {
        				// do not include time stamp or aggregation
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
