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

import java.util.Arrays;
import java.math.BigDecimal;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;

import edu.berkeley.path.beats.om.DefSimSettings;
import edu.berkeley.path.beats.om.DefSimSettingsPeer;

public final class Runner {

	private static String outputtype = "text";
	private static String configfilename;
	private static String outputfileprefix;

	private static Logger logger = Logger.getLogger(Runner.class);

	public static void main(String[] args) {

		long time = System.currentTimeMillis();

		try {
			// process input parameters
			SimulationSettings simsettings = parseInput(args);
			if (null == simsettings) return;
		
			// load configuration file
			Scenario scenario = ObjectFactory.createAndLoadScenario(configfilename);
			if (null == scenario)
				throw new SiriusException("UNEXPECTED! Scenario was not loaded");


		
////////////////////////////////////
//System.out.println("Destination networks:");
//for(DestinationNetworkBLA d : scenario.destination_networks){
//
//System.out.println("\t" + d.myIndex + "\t" + (d.dnetwork==null?"background":d.dnetwork.getId()) );
//System.out.print("\t\tLinks: [");
//for(Link link : d.links)
//System.out.print(link.getId() + " ");
//System.out.println("]");
//
//
//System.out.print("\t\tNodes: [");
//for(Node nodes : d.myInNodes)
//System.out.print(nodes.getId() + " ");
//System.out.println("]");
//}

//System.out.print("\nNodes:");
//for(com.relteq.sirius.jaxb.Node jnode : scenario.getNetworkList().getNetwork().get(0).getNodeList().getNode()){
//Node node = (Node) jnode;
//System.out.println("\n\tid=" + node.getId());
//System.out.println("\toutlinks: " + Arrays.asList(node.output_link).toString());
//System.out.println("\tinlinks: " + Arrays.asList(node.input_link).toString());		
//System.out.println("\tnumDNetworks=" + node.numDNetworks);
//System.out.println("\tmyDNGlobalIndex=" + node.myDNGlobalIndex.toString());
//System.out.println("\tdn2outlinkindex=" + node.dn2outlinkindex.toString());
//System.out.println("\tdn2inlinkindex=" + node.dn2inlinkindex.toString());
//System.out.println("\tistrivialsplit=" + node.dn_isSingleOut.toString());
//}
//
//System.out.print("\nLinks:");
//for(com.relteq.sirius.jaxb.Link jlink : scenario.getNetworkList().getNetwork().get(0).getLinkList().getLink()){
//Link link = (Link) jlink;
//System.out.println("\n\tid=" + link.getId());
//System.out.println("\tnumDNetworks=" + link.numDNetworks);
//System.out.println("\tmyDNindex=" + link.myDNindex.toString());
//
//System.out.println("\tdn_beginNodeMap=" + link.dn_beginNodeMap.toString());
//System.out.println("\tdn_endNodeMap=" + link.dn_endNodeMap.toString());
//}

////////////////////////////////////


		try {
			Properties owr_props = new Properties();
			if (null != outputfileprefix) owr_props.setProperty("prefix", outputfileprefix);
			owr_props.setProperty("type", outputtype);
			scenario.run(simsettings, owr_props);
			System.out.println("done in " + (System.currentTimeMillis()-time));
		} catch (SiriusException exc) {
			exc.printStackTrace();
		} finally {
			if (SiriusErrorLog.hasmessage()) {
				SiriusErrorLog.print();
				SiriusErrorLog.clearErrorMessage();
			}
		}
		
	}

	public static void simulate_output(String[] args) {
		outputtype = "xml";
		main(args);
	}

	public static void debug(String[] args) {
		outputtype = "text";
		main(args);
	}

	private static SimulationSettings parseInput(String[] args){

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
			return null;
		}
		
		// Configuration file name	
		configfilename = args[0];

		// Output file name
		if(args.length>1)
			outputfileprefix = args[1];	
		else
			outputfileprefix = "output";

		SimulationSettings simsettings = new SimulationSettings(SimulationSettings.defaults());
		simsettings.parseArgs(args, 2);
		return simsettings;
	}

	public static void run_db(String [] args) throws SiriusException, edu.berkeley.path.beats.Runner.InvalidUsageException {
		logger.info("Parsing arguments");
		long scenario_id;
		SimulationSettings simsettings = new SimulationSettings(SimulationSettings.defaults());
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
			simsettings.parseArgs(args, 1);
		}

		edu.berkeley.path.beats.db.Service.init();

		logger.info("Loading scenario");
		Scenario scenario = edu.berkeley.path.beats.db.exporter.ScenarioRestorer.getScenario(scenario_id);

		if (args.length < 4) {
			logger.info("Loading default simulation settings");
			try {
				DefSimSettings db_defss = DefSimSettingsPeer.retrieveByPK(Long.valueOf(scenario_id));
				SimulationSettings defss = new SimulationSettings(simsettings.getParent());
				defss.setStartTime(db_defss.getSimStartTime());
				defss.setDuration(db_defss.getSimDuration());
				defss.setOutputDt(db_defss.getOutputDt());
				simsettings.setParent(defss);
			} catch (NoRowsException exc) {
				logger.warn("Found no default simulation settings for scenario " + scenario_id, exc);
			} catch (TooManyRowsException exc) {
				logger.error("Too many default simulation settings for scenario " + scenario_id, exc);
			} catch (TorqueException exc) {
				throw new SiriusException(exc);
			}
		}

		logger.info("Simulation parameters: " + simsettings);

		logger.info("Simulation");
		Properties owr_props = new Properties();
		owr_props.setProperty("type", "db");
		scenario.run(simsettings, owr_props);

		edu.berkeley.path.beats.db.Service.shutdown();
		logger.info("Done");
	}

}
