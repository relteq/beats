package com.relteq.sirius.processor;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.relteq.sirius.db.OutputToCSV;

public class PerformanceDataTest {

	@Test
	public void testProductivityLoss() {
		
		BigDecimal res =new BigDecimal(0.0026);
		res = res.setScale(4, BigDecimal.ROUND_HALF_DOWN);
		
		BigDecimal out_flow = new BigDecimal(0.1);
		BigDecimal capacity = new BigDecimal(2.0);
		
		assertEquals("Result", res, PerformanceData.productivityLoss(out_flow, capacity, 1, 10.0, 1000).setScale(4, BigDecimal.ROUND_HALF_DOWN));
	}

}
