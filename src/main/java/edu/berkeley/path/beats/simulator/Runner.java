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

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;

import edu.berkeley.path.beats.om.DefSimSettings;
import edu.berkeley.path.beats.om.DefSimSettingsPeer;
import edu.berkeley.path.beats.util.scenario.ScenarioLoader;

/** XXX. 
 * YYY
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public final class Runner {

	private static Logger logger = Logger.getLogger(Runner.class);

	public static void main(String[] args) {

		long time = System.currentTimeMillis();

		try {
			// process input parameters
			RunnerArguments runargs = parseInput(args);
			if (null == runargs)
				return;

			// load configuration file
			Scenario scenario = ObjectFactory.createAndLoadScenario(runargs.getConfigfilename(),runargs.getNodeFlowSolver(),runargs.getNodeSRSolver());
			
			if (null == scenario)
				throw new BeatsException("Scenario did not load");
			
			// initialize
			scenario.initialize( runargs.getDt(),
								 runargs.getStartTime(),
								 runargs.getEndTime(),
								 runargs.getOutputDt(),
								 runargs.getOutput_format(),
								 runargs.getOutputfileprefix(),
								 runargs.getNumReps(),
								 1 );
			
			// run the scenario
			scenario.run();
			
			System.out.println("done in " + (System.currentTimeMillis()-time));
			
		} catch (BeatsException exc) {
			exc.printStackTrace();
		} finally {
			if (BeatsErrorLog.hasmessage()) {
				BeatsErrorLog.print();
				BeatsErrorLog.clearErrorMessage();
			}
		}
		
	}

	private static RunnerArguments parseInput(String[] args){

		if(args.length<1){
			String str;
			str = "Usage:" + "\n";
			str += "-----\n" + "\n";
			str += "args[0]: Configuration file name. (required)\n";
			str += "args[1]: Time step [seconds]. (required)\n";
			str += "args[2]: Output file name.\n";
			str += "         Default: 'outputs'\n";
			str += "args[3]: Output file format.\n";
			str += "         Default: 'xml'.\n";
			str += "args[4]: Start time [seconds after midnight]." + "\n";
			str += "         Default: Minimum start time of all demand profiles." + "\n";
			str += "args[5]: Duration [seconds]." + "\n";
			str += "         Default: 86,400 seconds." + "\n";
			str += "args[6]: Output sampling time [seconds]." + "\n";
			str += "         Default: 300 seconds." + "\n";
			str += "args[7]: Number of simulations." + "\n";
			str += "         Default: 1." + "\n";
			str += "args[8]: noise model <gaussian,uniform>." + "\n";
			str += "         Default: uniform." + "\n";
			str += "args[9]: node flow solver <proportional,symmetric>." + "\n";
			str += "         Default: proportional." + "\n";
			str += "args[10]: node split ratio solver <A,B,C>." + "\n";
			str += "         Default: A." + "\n";
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
			BeatsErrorLog.addError(str);
			return null;
		}
		
		RunnerArguments simsettings = new RunnerArguments(RunnerArguments.defaults());
		simsettings.parseArgs(args, 0);
		return simsettings;
	}

//	public static void run_db(String [] args) throws BeatsException, edu.berkeley.path.beats.Runner.InvalidUsageException {
//		logger.info("Parsing arguments");
//		long scenario_id;
//		RunnerArguments runargs = new RunnerArguments(RunnerArguments.defaults());
//		if (0 == args.length || 5 < args.length) {
//			final String eol = System.getProperty("line.separator");
//			throw new edu.berkeley.path.beats.Runner.InvalidUsageException(
//					"Usage: simulate|s scenario_id [parameters]" + eol +
//					"Parameters:" + eol +
//					"\tstart time, sec" + eol +
//					"\tduration, sec" + eol +
//					"\toutput sampling time, sec" + eol +
//					"\tnumber of simulations");
//		} else {
//			scenario_id = Long.parseLong(args[0]);
//			runargs.parseArgs(args, 1);
//		}
//
//		edu.berkeley.path.beats.db.Service.init();
//
//		logger.info("Loading scenario");
//		Scenario scenario = ScenarioLoader.load(scenario_id);
//
//		if (args.length < 4) {
//			logger.info("Loading default simulation settings");
//			try {
//				DefSimSettings db_defss = DefSimSettingsPeer.retrieveByPK(Long.valueOf(scenario_id));
//				RunnerArguments defss = new RunnerArguments(runargs.getParent());
//				defss.setStartTime(db_defss.getSimStartTime());
//				defss.setDuration(db_defss.getSimDuration());
//				defss.setOutputDt(db_defss.getOutputDt());
//				runargs.setParent(defss);
//			} catch (NoRowsException exc) {
//				logger.warn("Found no default simulation settings for scenario " + scenario_id, exc);
//			} catch (TooManyRowsException exc) {
//				logger.error("Too many default simulation settings for scenario " + scenario_id, exc);
//			} catch (TorqueException exc) {
//				throw new BeatsException(exc);
//			}
//		}
//
//		logger.info("Simulation parameters: " + runargs);
//
//		logger.info("Simulation");
//		scenario.run(runargs.getDt(),
//				runargs.getStartTime(), 
//				runargs.getEndTime(), 
//				runargs.getOutputDt(),
//				"db",
//				runargs.getOutputfileprefix(),
//				runargs.getNumReps());
//
//		edu.berkeley.path.beats.db.Service.shutdown();
//		logger.info("Done");
//	}

}
