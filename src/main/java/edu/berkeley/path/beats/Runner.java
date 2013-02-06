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

package edu.berkeley.path.beats;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import edu.berkeley.path.beats.db.OutputToCSV;
import edu.berkeley.path.beats.db.Service;
import edu.berkeley.path.beats.om.LinkDataDetailed;
import edu.berkeley.path.beats.om.LinkDataTotal;
import edu.berkeley.path.beats.om.LinkPerformanceDetailed;
import edu.berkeley.path.beats.om.LinkPerformanceTotal;
import edu.berkeley.path.beats.om.RoutePerformanceTotal;
import edu.berkeley.path.beats.om.SignalData;
import edu.berkeley.path.beats.om.SignalPhasePerformance;
import edu.berkeley.path.beats.processor.AggregateData;
import edu.berkeley.path.beats.processor.PdfReport;
import edu.berkeley.path.beats.processor.PerformanceData;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.ScenarioValidationError;
import edu.berkeley.path.beats.util.scenario.ScenarioLoader;
import edu.berkeley.path.beats.util.scenario.ScenarioSaver;

/**
 * Implements "Beats: Concept of Operations"
 */
public class Runner {

	private static Logger logger = Logger.getLogger(Runner.class);

	/**
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		
		try {
			if (0 == args.length) throw new InvalidUsageException();
			String cmd = args[0];
			String[] arguments = new String[args.length - 1];
			System.arraycopy(args, 1, arguments, 0, args.length - 1);
			
			
			// Run report
			if (cmd.equals("report") || cmd.equals("r")) 
			{
				Service.ensureInit();
				
				// Calculate performance measures
				PdfReport pdf = new PdfReport();
				pdf.outputPdf("link_data_total");
				
			} else
				
			// Aggregate data
			if (cmd.equals("process") || cmd.equals("p")) 
			{
				Service.ensureInit();
				
				// Calculate performance measures
				 PerformanceData.doPerformance(arguments);
				
				//Aggregate data
				AggregateData.doAggregateAllTables(arguments);
				
			} else
			
			// CSV output
			if (cmd.equals("link_data_total") || cmd.equals("ldt")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("link_data_total",LinkDataTotal.getFieldNames(), arguments);
				
			} else if (cmd.equals("link_data_detailed") || cmd.equals("ldd")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("link_data_detailed",LinkDataDetailed.getFieldNames(), arguments);
				
			} else if (cmd.equals("link_performance_total") || cmd.equals("lpt")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("link_performance_total", LinkPerformanceTotal.getFieldNames(), arguments);
				
			} else if (cmd.equals("link_performance_detailed") || cmd.equals("lpd")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("link_performance_detailed", LinkPerformanceDetailed.getFieldNames(), arguments);
				
			} else if (cmd.equals("signal_data") || cmd.equals("sd")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("signal_data", SignalData.getFieldNames(), arguments);
				
			} else if (cmd.equals("signal_phase_performance") || cmd.equals("spp")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("signal_phase_performance", SignalPhasePerformance.getFieldNames(), arguments);
				
			} else if (cmd.equals("route_performance_total") || cmd.equals("rpt")) 
			{
				Service.ensureInit();
				OutputToCSV.outputToCSV("route_performance_total", RoutePerformanceTotal.getFieldNames(), arguments);
				
			} else
			
			// End of CSV output
			

			if (cmd.equals("import") || cmd.equals("i")) {
				Options clOptions = new Options();
				clOptions.addOption("f", true, "input file format: xml or json");
				org.apache.commons.cli.Parser clParser = new org.apache.commons.cli.BasicParser();
				CommandLine cline = clParser.parse(clOptions, arguments);
				if (1 != cline.getArgs().length)
					throw new InvalidUsageException("Usage: import|i [-f file_format] scenario_file_name");

				final String filename = cline.getArgs()[0];

				edu.berkeley.path.beats.simulator.Scenario scenario = cline.hasOption("f") ?
						ScenarioLoader.load(filename, cline.getOptionValue("f")) :
						ScenarioLoader.load(filename);
				logger.info("Loaded configuration file '" + filename + "'");

				Long id = ScenarioSaver.save(scenario);
				logger.info("Scenario imported, ID=" + id);
			} else if (cmd.equals("update") || cmd.equals("u")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("export") || cmd.equals("e")) {
				Options clOptions = new Options();
				clOptions.addOption("f", true, "input file format: xml or json");
				org.apache.commons.cli.Parser clParser = new org.apache.commons.cli.BasicParser();
				CommandLine cline = clParser.parse(clOptions, arguments);
				arguments = cline.getArgs();
				if (0 == arguments.length || 2 < arguments.length)
					throw new InvalidUsageException("Usage: export|e [-f output_format] scenario_id [output_file_name]");

				edu.berkeley.path.beats.jaxb.Scenario scenario = ScenarioLoader.loadRaw(Long.parseLong(cline.getArgs()[0]));

				final String filename = 1 < arguments.length ? arguments[1] : scenario.getId() + "." + (cline.hasOption("f") ? cline.getOptionValue("f") : "xml");
				if (1 >= arguments.length) logger.info("Output file: " + filename);
				if (cline.hasOption("f"))
					ScenarioSaver.save(scenario, filename, cline.getOptionValue("f"));
				else
					ScenarioSaver.save(scenario, filename);
				logger.debug("Scenario " + scenario.getId() + " saved to file " + filename);
			} else if (cmd.equals("calibrate") || cmd.equals("c")) {
				edu.berkeley.path.beats.calibrator.FDCalibrator.main(arguments);
			} else if (cmd.equals("simulate") || cmd.equals("s")) {
				edu.berkeley.path.beats.simulator.Runner.run_db(arguments);
			} else if (cmd.equals("simulate_output") || cmd.equals("so")) {
				edu.berkeley.path.beats.simulator.Runner.main(arguments);
			} else if (cmd.equals("simulate_process") || cmd.equals("sp")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("list_scenarios") || cmd.equals("ls")) {
				edu.berkeley.path.beats.db.Lister.listScenarios();
			} else if (cmd.equals("list_runs") || cmd.equals("lr")) {
				if (1 == arguments.length)
					edu.berkeley.path.beats.db.Lister.listRuns(Long.parseLong(arguments[0], 10));
				else
					throw new InvalidUsageException("Usage: list_runs|lr scenario_id");
			} else if (cmd.equals("load") || cmd.equals("l")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("output") || cmd.equals("o")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("list_aggregations") || cmd.equals("la")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("node_data") || cmd.equals("nd")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("detection_data") || cmd.equals("dd")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("probe_data") || cmd.equals("pd")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("controller_data") || cmd.equals("cd")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("init")) {
				edu.berkeley.path.beats.db.Admin.init();
			} else if (cmd.equals("clear_data") || cmd.equals("cld")) {
				if (1 == arguments.length)
					edu.berkeley.path.beats.db.Cleaner.clearData(Long.parseLong(arguments[0], 10));
				else
					throw new InvalidUsageException("Usage: clear_data|cld scenario_id");
			} else if (cmd.equals("clear_processed") || cmd.equals("clp")) {
				if (1 == arguments.length)
					edu.berkeley.path.beats.db.Cleaner.clearProcessed(Long.parseLong(arguments[0], 10));
				else
					throw new InvalidUsageException("Usage: clear_processed|clp scenario_id");
			} else if (cmd.equals("clear_scenario") || cmd.equals("cls")) {
				if (1 == arguments.length)
					edu.berkeley.path.beats.db.Cleaner.clearScenario(Long.parseLong(arguments[0], 10));
				else throw new InvalidUsageException("Usage: clear_scenario|cls scenario_id");
			} else if (cmd.equals("clear_all") || cmd.equals("cla")) {
				throw new NotImplementedException(cmd);
			} else if (cmd.equals("version") || cmd.equals("v")) {
				printVersion();
			} else if (cmd.equals("convert_units") || cmd.equals("cu")) {
				if (2 == arguments.length)
					edu.berkeley.path.beats.util.UnitConverter.convertUnits(arguments[0], arguments[1]);
				else
					throw new InvalidUsageException("Usage: convert_units|cu input_file output_file");
			} else if ("convert".equals(cmd)) {
				Options clOptions = new Options();
				clOptions.addOption("if", true, "input file format: xml or json");
				clOptions.addOption("of", true, "output file format: xml or json");
				org.apache.commons.cli.Parser clParser = new org.apache.commons.cli.BasicParser();
				CommandLine cline = clParser.parse(clOptions, arguments);
				arguments = cline.getArgs();
				if (0 == arguments.length || 2 < arguments.length)
					throw new InvalidUsageException("convert [OPTIONS] input_file_name [output_file_name]", clOptions);

				String iformat = null;
				if (cline.hasOption("if")) iformat = cline.getOptionValue("if");
				else if (arguments[0].toLowerCase().endsWith(".json")) iformat = "json";
				else iformat = "xml";
				if (!"xml".equals(iformat) && !"json".equals(iformat))
					throw new InvalidUsageException("Invalid input format " + iformat);

				String oformat = null;
				if (cline.hasOption("of")) oformat = cline.getOptionValue("of");
				else if (1 < arguments.length && arguments[1].toLowerCase().endsWith(".json")) oformat = "json";
				else if (1 < arguments.length && arguments[1].toLowerCase().endsWith(".xml")) oformat = "xml";
				else if ("json".equals(iformat)) oformat = "xml";
				else if ("xml".equals(iformat)) oformat = "json";
				if (!"xml".equals(oformat) && !"json".equals(oformat))
					throw new InvalidUsageException("Invalid output format " + oformat);

				edu.berkeley.path.beats.jaxb.Scenario scenario = ScenarioLoader.loadRaw(arguments[0], iformat);

				String ofilename = null;
				if (1 < arguments.length) ofilename = arguments[1];
				else {
					ofilename = new File(arguments[0]).getName();
					final String iext = "." + iformat;
					if (ofilename.toLowerCase().endsWith(iext))
						ofilename = ofilename.substring(0, ofilename.length() - iext.length());
					ofilename += "." + oformat;
					logger.info("Output file: " + ofilename);
				}
				ScenarioSaver.save(scenario, ofilename, oformat);
			} else throw new InvalidCommandException(cmd);
		} catch (InvalidUsageException exc) {
			String msg = exc.getMessage();
			if (null == msg) msg = "Usage: command [parameters]";
			System.err.println(msg);
		} catch (NotImplementedException exc) {
			System.err.println(exc.getMessage());
		} catch (InvalidCommandException exc) {
			System.err.println(exc.getMessage());
		} catch (ScenarioValidationError exc) {
			logger.fatal(exc.getMessage());
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			if (BeatsErrorLog.hasmessage()) {
				BeatsErrorLog.print();
				BeatsErrorLog.clearErrorMessage();
			}
			if (edu.berkeley.path.beats.db.Service.isInit()) {
				logger.debug("Shutting down the DB service");
				edu.berkeley.path.beats.db.Service.shutdown();
			}
		}
	}

	@SuppressWarnings("serial")
	public static class NotImplementedException extends Exception {
		/**
		 * Constructs a <code>NotImplementedException</code> for the specified command
		 * @param cmd name of the command
		 */
		NotImplementedException(String cmd) {
			super("Command '" + cmd + "' is not implemented");
		}
	}

	@SuppressWarnings("serial")
	public static class InvalidCommandException extends Exception {
		/**
		 * Constructs an <code>InvalidCommandException</code> for the specified command
		 * @param cmd name of the command
		 */
		public InvalidCommandException(String cmd) {
			super("Invalid command '" + cmd + "'");
		}
	}

	@SuppressWarnings("serial")
	public static class InvalidUsageException extends Exception {
		public InvalidUsageException() {
			super();
		}
		public InvalidUsageException(String message) {
			super(message);
		}
		/**
		 * Constructs an <code>InvalidUsageException</code> for the given command line syntax and options
		 * @param clSyntax the command line syntax string
		 * @param clOptions the command line options
		 */
		public InvalidUsageException(String clSyntax, Options clOptions) {
			super(format(clSyntax, clOptions));
		}
		private static String format(String clSyntax, Options clOptions) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printUsage(pw, Integer.MAX_VALUE, clSyntax);
			pw.println("options:");
			formatter.printOptions(pw, Integer.MAX_VALUE, clOptions, formatter.getLeftPadding(), formatter.getDescPadding());
			pw.close();
			return sw.toString();
		}
	}

	private static void printVersion() {
		System.out.println(Version.get());
	}

}
