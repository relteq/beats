package com.relteq.sirius.processor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.map.ColumnMap;


import com.relteq.sirius.om.LinkDataTotalPeer;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;

public class LinkDataTotal extends com.relteq.sirius.om.LinkDataTotal {


	private static final long serialVersionUID = 7572652113762860553L;

	/**
	 * populates aggregated object, sets calculated data and insert
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
			
			LinkDataTotalPeer.populateObject((Record)originalData.get(0), 1, this);
			getListOfKeys();
			
			//Populate aggregated data
			setByPeerName(table+"."+"in_flow", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(1).asBigDecimal()));
			setByPeerName(table+"."+"out_flow", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(2).asBigDecimal()));
			setByPeerName(table+"."+"density", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(3).asBigDecimal()));
			setByPeerName(table+"."+"occupancy", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(4).asBigDecimal()));
			setByPeerName(table+"."+"speed", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(5).asBigDecimal()));
			setByPeerName(table+"."+"in_flow_worst",(BigDecimal)(((Record)aggregatedData.get(0)).getValue(6).asBigDecimal()));
			setByPeerName(table+"."+"out_flow_worst",(BigDecimal)(((Record)aggregatedData.get(0)).getValue(7).asBigDecimal()));
			setByPeerName(table+"."+"density_worst",(BigDecimal)(((Record)aggregatedData.get(0)).getValue(8).asBigDecimal()));
			setByPeerName(table+"."+"occupancy_worst",(BigDecimal)(((Record)aggregatedData.get(0)).getValue(9).asBigDecimal()));
			setByPeerName(table+"."+"speed_worst", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(10).asBigDecimal()));
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
