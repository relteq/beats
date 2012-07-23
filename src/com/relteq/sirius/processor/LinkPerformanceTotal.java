package com.relteq.sirius.processor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.relteq.sirius.om.LinkPerformanceTotalPeer;
import com.workingdogs.village.Record;

public class LinkPerformanceTotal extends com.relteq.sirius.om.LinkPerformanceTotal {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1435985890503127384L;
	
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
			setByPeerName(table+"."+"in_flow", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(1).asBigDecimal()));
			setByPeerName(table+"."+"out_flow", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(2).asBigDecimal()));
			setByPeerName(table+"."+"density", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(3).asBigDecimal()));
			setByPeerName(table+"."+"occupancy", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(4).asBigDecimal()));
			setByPeerName(table+"."+"speed", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(5).asBigDecimal()));
			setByPeerName(table+"."+"aggregation",	aggregation);
			setByPeerName(table+"."+"ts",			ts);
			
			setNew(true);
			save();
			
			
		} catch (Exception e) {

			return 0;
		}

		return 1;
		
	}


}
