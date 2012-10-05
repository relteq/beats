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

final class Defaults {
	public static double DURATION 	= 86400f;		// [sec]
	public static double TIME_INIT 	= 0f;			// [sec]
	public static double OUT_DT 	= 300f;			// [sec]
	public static double SIMDT	 	= 10f;			// [sec]
	
	public static String vehicleType = "vehicle";
	
	// fundamental diagram
	public static Double vf					= 60.0;		// [mile/hr]
	public static Double w					= 20.0;		// [mile/hr]
	public static Double densityJam			= 160.0;	// [veh/mile/lane]
	public static Double capacityDrop 		= 0.0;		// [veh/hr/lane]
	public static Double capacity 			= 2400.0;	// [veh/hr/lane]
	
	// signal phase
	public static float mingreen			= 10f;		// [sec]
	public static float redcleartime		= 5f;		// [sec]
	public static float yellowtime			= 5f;		// [sec]
}
