package com.relteq.sirius.processor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.relteq.sirius.om.LinkDataTotal;
import com.relteq.sirius.om.LinkDataTotalPeer;
import com.workingdogs.village.Record;

public class LinkDataTotalChild extends LinkDataTotal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

			//Populate aggregated data
			super.setByPeerName(table+"."+"in_flow", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(1).asBigDecimal()));
			super.setByPeerName(table+"."+"out_flow", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(2).asBigDecimal()));
			super.setByPeerName(table+"."+"density", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(3).asBigDecimal()));
			super.setByPeerName(table+"."+"occupancy", 	(BigDecimal)(((Record)aggregatedData.get(0)).getValue(4).asBigDecimal()));
			super.setByPeerName(table+"."+"speed", 		(BigDecimal)(((Record)aggregatedData.get(0)).getValue(5).asBigDecimal()));
			super.setByPeerName(table+"."+"aggregation",aggregation);
			super.setByPeerName(table+"."+"ts",			ts);
			
			super.setNew(true);
			super.save();
			
			
		} catch (Exception e) {

			return 0;
		}

		return 1;
		
	}
}
