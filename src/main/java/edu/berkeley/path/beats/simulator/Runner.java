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

import java.math.BigDecimal;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;

import edu.berkeley.path.beats.om.DefSimSettings;
import edu.berkeley.path.beats.om.DefSimSettingsPeer;

public final class Runner {
	
	private static Scenario scenario;

	private static String outputtype = "text";
	private static String configfilename;
	private static String outputfileprefix;
	private static double timestart;
	private static double timeend;
	private static double outdt;
	private static int numRepetitions;

	private static Logger logger = Logger.getLogger(Runner.class);

	public static void main(String[] args) {

		long time = System.currentTimeMillis();

		// process input parameters
		if(!parseInput(args)){
			SiriusErrorLog.print();
			return;
		}

		// load configuration file ................................
		try {
			scenario = ObjectFactory.createAndLoadScenario(configfilename);
		} catch (SiriusException exc) {
			exc.printStackTrace();
			return;
		}

		// check if it loaded
		if(scenario==null)
			return;

		try {
			Properties owr_props = new Properties();
			if (null != outputfileprefix) owr_props.setProperty("prefix", outputfileprefix);
			owr_props.setProperty("type", outputtype);
			scenario.run(timestart,timeend,outdt,numRepetitions,owr_props);
			System.out.println("done in " + (System.currentTimeMillis()-time));
		} catch (SiriusException e) {
			if(SiriusErrorLog.haserror())
				SiriusErrorLog.print();
			else
				e.printStackTrace();
		}	
		
	}

	public static void debug(String [] args) {
		outputtype = "text";
		main(args);
	}

	private static boolean parseInput(String[] args){

		if(args.length<1){
			String str;
			str = "Usage:" + "\n";
			str += "-----\n" + "\n";
			str += "args[0]: Configuration file name. (required)\n";
			str += "args[1]: Output file name.\n";
			str += "args[2]: Start time [seconds after midnight]." + "\n";
			str += "         Defailt: Minimum start time of all demand profiles." + "\n";
			str += "args[3]: Duration [seconds]." + "\n";
			str += "         Defailt: 86,400 seconds." + "\n";
			str += "args[4]: Output sampling time [seconds]." + "\n";
			str += "         Default: 300 seconds." + "\n";
			str += "args[5]: Number of simulations." + "\n";
			str += "         Default: 1." + "\n";
			str += "\nSimulation modes:" + "\n";
			str += "----------------\n" + "\n";
			str += "Normal mode: Simulation runs in normal mode when the start time equals " +
					"the time stamp of the initial density profile. In this mode, the initial density state" +
					" is taken from the initial density profile, and the simulated state is written to the output file.\n" + "\n";
			str += "Warmup mode: Warmup is executed whenever the start time (st) does not equal the time stamp " +
					"of the initial density profile (tsidp). The purpose of a warmup simulation is to compute the state of the scenario " +
					"at st. If st<tsidp, then the warmup run will start with zero density at the earliest times stamp of all " +
					"demand profiles and run to st. If st>tsidn, then the warmup will start at tsidn with the given initial " +
					"density profile and run to st. The simulation state is not written in warmup mode. The output is a configuration " +
					"file with the state at st contained in the initial density profile." + "\n";
			SiriusErrorLog.addError(str);
			return false;
		}
		
		// Configuration file name	
		configfilename = args[0];

		// Output file name
		if(args.length>1)
			outputfileprefix = args[1];	
		else
			outputfileprefix = "output";
			
		// Start time [seconds after midnight]
		if(args.length>2){
			timestart = Double.parseDouble(args[2]);
			timestart = SiriusMath.round(timestart*10.0)/10.0;	// round to the nearest decisecond
		}
		else
			timestart = Defaults.TIME_INIT;

		// Duration [seconds]	
		if(args.length>3)
			timeend = timestart + Double.parseDouble(args[3]);
		else
			timeend = timestart + Defaults.DURATION;
		
		// Output sampling time [seconds]
		if(args.length>4){
			outdt = Double.parseDouble(args[4]);
			outdt = SiriusMath.round(outdt*10.0)/10.0;		// round to the nearest decisecond	
		}
		else
			outdt = Defaults.OUT_DT;

		// Number of simulations
		if(args.length>5){
			numRepetitions = Integer.parseInt(args[5]);
		}
		else
			numRepetitions = 1;

		return true;
	}

	public static void run_db(String [] args) throws SiriusException, edu.berkeley.path.beats.Runner.InvalidUsageException {
		logger.info("Parsing arguments");
		long scenario_id;
		BigDecimal startTime = null;
		BigDecimal duration = null;
		BigDecimal outputDt = null;
		Integer numSim = null;
		if (0 == args.length || 5 < args.length) {
			final String eol = System.getProperty("line.separator");
			throw new edu.berkeley.path.beats.Runner.InvalidUsageException(
					"Usage: simulate|s scenario_id [parameters]" + eol +
					"Parameters:" + eol +
					"\tstart time, sec" + eol +
					"\tduration, sec" + eol +
					"\toutput sampling time, sec" + eol +
					"\tnumber of simulations");
		} else {
			scenario_id = Long.parseLong(args[0]);
			if (1 < args.length) startTime = new BigDecimal(args[1]);
			if (2 < args.length) duration = new BigDecimal(args[2]);
			if (3 < args.length) outputDt = new BigDecimal(args[3]);
			if (4 < args.length) numSim = new Integer(args[4]);
		}

		edu.berkeley.path.beats.db.Service.init();

		logger.info("Loading scenario");
		scenario = edu.berkeley.path.beats.db.exporter.ScenarioRestorer.getScenario(scenario_id);
		if (SiriusErrorLog.haserror()) {
			SiriusErrorLog.print();
			return;
		}

		if (null == startTime || null == duration || null == outputDt) {
			logger.info("Loading default simulation settings");
			try {
				DefSimSettings db_defss = DefSimSettingsPeer.retrieveByPK(Long.valueOf(scenario_id));
				if (null == startTime) startTime = db_defss.getSimStartTime();
				if (null == duration) duration = db_defss.getSimDuration();
				if (null == outputDt) outputDt = db_defss.getOutputDt();
			} catch (NoRowsException exc) {
				logger.warn("Found no default simulation settings for scenario " + scenario_id, exc);
			} catch (TooManyRowsException exc) {
				logger.error("Too many default simulation settings for scenario " + scenario_id, exc);
			} catch (TorqueException exc) {
				throw new SiriusException(exc);
			}
		}

		logger.info("Simulation");
		Properties owr_props = new Properties();
		owr_props.setProperty("type", "db");
		if (null == startTime) startTime = BigDecimal.valueOf(0);
		if (null == duration) duration = BigDecimal.valueOf(60 * 60 * 24);
		if (null == outputDt) outputDt = BigDecimal.valueOf(60);
		if (null == numSim) numSim = Integer.valueOf(1);
		logger.info("Simulation parameters: start time: " + startTime + " sec, duration: " + duration + " sec, output sampling time: " + outputDt + " sec");
		scenario.run(startTime.doubleValue(), startTime.add(duration).doubleValue(), outputDt.doubleValue(), numSim.intValue(), owr_props);

		edu.berkeley.path.beats.db.Service.shutdown();
		logger.info("Done");
	}

}
