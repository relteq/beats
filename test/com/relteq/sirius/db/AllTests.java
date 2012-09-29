package com.relteq.sirius.db;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.relteq.sirius.processor.*;

@RunWith(Suite.class)
@SuiteClasses({ OutputToCSVTest.class, PerformanceDataTest.class })
public class AllTests {
	

}
