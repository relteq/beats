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

package edu.berkeley.path.beats.simulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final public class Defaults {
	public static double DURATION 	= 86400f;		// [sec]
	public static double TIME_INIT 	= 0f;			// [sec]
	public static double OUT_DT 	= 300f;			// [sec]
	public static double SIMDT	 	= 10f;			// [sec]
	
	public static String vehicleType = "vehicle";
	
	// fundamental diagram
	public static double vf					= 60.0 * 0.44704;		// [m/s]
	public static double w					= 20.0 * 0.44704;		// [m/s]
	public static double densityJam			= 160.0 / 1609.344;		// [veh/meter/lane]
	public static double capacityDrop 		= 0.0;					// [veh/sec/lane]
	public static double capacity 			= 2400.0 / 3600.0;		// [veh/sec/lane]
	
	// signal phase
	public static float mingreen			= 10f;		// [sec]
	public static float redcleartime		= 5f;		// [sec]
	public static float yellowtime			= 5f;		// [sec]
	
	// dt
	public static final Map<String, Double> dt_map;
	
	// initialization
    static {
        Map<String, Double> aMap = new HashMap<String, Double>();    	
        aMap.put("Albany-and-Berkeley.xml",1d);
    	aMap.put("complete.xml",5d);
    	aMap.put("complete_twotypes.xml",5d);
    	aMap.put("samitha1onramp.xml",5d);
    	aMap.put("scenario_twotypes.xml",5d);
    	aMap.put("testfwy2.xml",5d);
    	aMap.put("testfwy_w.xml",5d);
    	aMap.put("test_event.xml",5d);
    	aMap.put("_scenario_2009_02_12.xml",5d);
    	aMap.put("_scenario_constantsplits.xml",5d);
    	aMap.put("_smalltest.xml",5d);
    	aMap.put("_smalltest_MPC.xml",5d);
    	aMap.put("_smalltest_multipletypes.xml",5d);
    	aMap.put("_smalltest_nocontrol_broken.xml",5d);
        dt_map = Collections.unmodifiableMap(aMap);
    }
    
    public static double getTimestepFor(String configname){
    	try {
			return dt_map.get(configname);
		} catch (Exception e) {
			return Double.NaN;
		}
    }

    
}
