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

package edu.berkeley.path.beats.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Transaction;

import edu.berkeley.path.beats.om.*;
import edu.berkeley.path.beats.simulator.BeatsErrorLog;
import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * Administers the Sirius Database
 */
public class Admin {

	/**
	 * Initializes the database.
	 * If the database exists, it is dropped and recreated.
	 * @throws SQLException
	 * @throws IOException
	 * @throws BeatsException
	 */
	public static void init() throws SQLException, IOException, BeatsException {
		init(Parameters.fromEnvironment());
	}

	/**
	 * Initializes the database
	 * @param params the database connection parameters
	 * @throws SQLException
	 * @throws IOException
	 * @throws BeatsException
	 */
	public static void init(Parameters params) throws SQLException, IOException, BeatsException {
		SQLExec exec = new SQLExec();
		drop(params);
		if (params.getDriver().equals("derby")) params.setCreate(true);
		else {
			String dbname = params.getDBName();
			try {
				params.setDBName("");
				Service.init(params);
				BasePeer.executeStatement("CREATE DATABASE " + dbname);
				logger.info("Database " + dbname + " created");
			} catch (TorqueException exc) {
				throw new BeatsException(exc);
			} finally {
				Service.shutdown();
				params.setDBName(dbname);
			}
		}
		Service.init(params);
		exec.runStatements(new java.io.InputStreamReader(Admin.class.getClassLoader().getResourceAsStream(
				"sql" + File.separator + params.getDriver() + File.separator + "sirius-db-schema.sql")));
		logger.info("Database tables created");

		new Initializer().run();
		logger.info("Database " + params.getDBName() + " initialized");
	}

	/**
	 * Executes SQL statements
	 */
	public static class SQLExec {
		public SQLExec() {}

		private String delimiter = ";";

		/**
		 * Derived from org.apache.torque.task.TorqueSQLExec.runStatements
		 * @param reader
		 * @throws IOException
		 */
		public void runStatements(java.io.Reader reader) throws IOException {
			StringBuilder sql = new StringBuilder();
			String line = null;

			BufferedReader bufreader = new BufferedReader(reader);

			while (null != (line = bufreader.readLine())) {
				line = line.trim();
				// ignore empty lines and comments
				if (line.isEmpty() || line.startsWith("//") || line.startsWith("--") || line.toUpperCase().startsWith("REM ")) continue;

				// SQL defines "--" as a comment to EOL
				// and in Oracle it may contain a hint
				// so we cannot just remove it, instead we must end it
				if (line.indexOf("--") >= 0) line += "\n";

				sql.append(" " + line);

				if (line.endsWith(delimiter)) {
					execSQL(sql.substring(0, sql.length() - delimiter.length()));
					sql.setLength(0);
				}
			}

			// Catch any statements not followed by ;
			if (0 < sql.length()) execSQL(sql.toString());
		}

		protected void execSQL(String sql) {
			logger.debug("SQL: " + sql);
			try {
				BasePeer.executeStatement(sql);
			} catch (TorqueException exc) {
				logger.error(exc.getMessage());
			}
		}

	}

	private static Logger logger = Logger.getLogger(Admin.class);

	/**
	 * Drops the database
	 * @param params DB connection parameters
	 */
	public static void drop(Parameters params) {
		if (params.getDriver().equals("derby"))
			try {
				org.apache.commons.io.FileUtils.deleteDirectory(new File(params.getDBName()));
			} catch (IOException exc) {
				BeatsErrorLog.addError(exc.getMessage());
			}
		else {
			String dbname = params.getDBName();
			try {
				params.setDBName("");
				Service.init(params);
				BasePeer.executeStatement("DROP DATABASE IF EXISTS " + dbname);
				logger.info("Database " + dbname + " dropped");
			} catch (TorqueException exc) {
				logger.error("Could not drop database " + dbname, exc);
			} catch (BeatsException exc) {
				logger.error(exc.getMessage(), exc);
			} finally {
				Service.shutdown();
				params.setDBName(dbname);
			}
		}
	}

	private static class Initializer {
		public Initializer() {}
		Connection conn = null;
		public void run() throws BeatsException {
			try {
				conn = Transaction.begin();

				createDefaultProject();
				addNodeTypes();
				addLinkTypes();
				addFDTypes();
				addSensorTypes();
				addControllerTypes();
				addQueueControllerTypes();
				addEventTypes();
				addScenarioElementTypes();
				addApplicationTypes();
				addQuantityTypes();
				addAggregationTypes();
				addVehicleTypes();

				Transaction.commit(conn);
				conn = null;
			} catch (TorqueException exc) {
				throw new BeatsException(exc);
			} finally {
				if (null != conn) {
					Transaction.safeRollback(conn);
					conn = null;
				}
			}
		}
		private void createDefaultProject() throws TorqueException {
			Projects db_project = new Projects();
			db_project.setId(Long.valueOf(0));
			db_project.setName("default");
			db_project.save(conn);
			logger.info("Project 'default' [id=" + db_project.getId() + "] created");
		}
		private void addType(BaseTypes db_type, String name, String description) throws TorqueException {
			db_type.setName(name);
			db_type.setDescription(description);
			db_type.setInUse(Boolean.TRUE);
			db_type.save(conn);
		}
		private void addNodeTypes() throws TorqueException {
			addNodeType("freeway", "Nodes at on-ramp/interconnect merges, off-ramp/interconnect splits, HOV/HOT gates on freeways.");
			addNodeType("highway", "Same as above for highways.");
			addNodeType("signalized_intersection", "Signals can be placed on nodes only of this type.");
			addNodeType("stop_intersection", null);
			addNodeType("simple", "Single-Input-Single-Output (SISO) node, where the input and the output links have the same properties.");
			addNodeType("terminal", "Node that has only 1 input or only 1 output link � it is either the begin node for an origin link, or the end node for the destination link.");
			addNodeType("other", null);
			logger.info("Added node types");
		}
		private void addNodeType(String name, String description) throws TorqueException {
			addType(new NodeTypes(), name, description);
		}

		private void addLinkTypes() throws TorqueException {
			addLinkType("freeway", null);
			addLinkType("highway", null);
			addLinkType("onramp", null);
			addLinkType("offramp", null);
			addLinkType("freeway_connector", "Link between two freeways (highways) or a freeway and a highway.");
			addLinkType("HOV", "Lane for high occupancy vehicles.");
			addLinkType("HOT", "Lane for high occupancy vehicles or those who pay toll.");
			addLinkType("toll", "Toll Lane.");
			addLinkType("heavy_vehicle", "Heavy Vehicle Lane.");
			addLinkType("bus", "Bus Lane.");
			addLinkType("street", null);
			addLinkType("intersection_approach", "An input link for Signalized Intersection node, parallel to left and right pockets, that is there for the through movement.");
			addLinkType("left_turn_pocket", null);
			addLinkType("right_turn_pocket", null);
			logger.info("Added link types");
		}
		private void addLinkType(String name, String description) throws TorqueException {
			addType(new LinkTypes(), name, description);
		}
		private void addFDTypes() throws TorqueException {
			addFDType("triangular", null);
			addFDType("trapezoidal", null);
			addFDType("linear-hyperbolic", "Invertible fundamental diagram whose right side is linear.");
			addFDType("greenshields", "Fundamental diagram in the form of parabola.");
			logger.info("Added fundamental diagram types");
		}
		private void addFDType(String name, String description) throws TorqueException {
			addType(new FundamentalDiagramTypes(), name, description);
		}
		private void addSensorTypes() throws TorqueException {
			addSensorType("loop", null);
			addSensorType("magnetic", null);
			addSensorType("radar", null);
			addSensorType("camera", null);
			addSensorType("TMC", "TMC stands for Traffic Message Channel. This is a static way of reporting probe measurements employed by INRIX, Navteq, etc.");
			logger.info("Added sensor types");
		}
		private void addSensorType(String name, String description) throws TorqueException {
			addType(new SensorTypes(), name, description);
		}
		private void addControllerTypes() throws TorqueException {
			addControllerType("IRM_ALINEA", "IRM stands for Isolated Ramp Metering � ramp metering on individual on-ramps. ALINEA is the name of RM algorithm.");
			addControllerType("IRM_TOD", "TOD stands for Time Of Day � fixed rates for given times of the day.");
			addControllerType("IRM_TOS", "Traffic responsive controller based on lookup tables.");
			addControllerType("CRM_HERO", "CRM stands for Coordinated Ramp Metering. HERO is the name of CRM algorithm.");
			addControllerType("CRM_SWARM", "SWARM is the name of another CRM algorithm, developed by Delcan.");
			addControllerType("VSL_TOD", "VSL stands for Variable Speed Limit, TOD � for Time Of Day.");
			addControllerType("VSL_ALINEA", "VSL with ALINEA algorithm.");
			addControllerType("ML_TOLL_Reaction", "ML � Managed Lanes. Models drivers� reaction to tolls.");
			addControllerType("ML_TOLL_Pricing", "Controller that computes toll pricing.");
			addControllerType("ML_Shoulder", "Controller that opens a shoulder as an extra lane based on traffic condition.");
			addControllerType("SIG_Pretimed", "Pre-timed signal control.");
			addControllerType("SIG_Actuated", "Actuated signal control.");
			addControllerType("SIG_Synchronized", "Actuated signal control synchronized over multiple intersections.");
			addControllerType("SIG_TUC", "One of the adaptive signal control algorithms. TUC stands for Traffic-responsive Urban Control.");
			addControllerType("FAC_1", "Freeway Arterial Coordination � scenario 1.");
			addControllerType("FAC_2", "Scenario 2.");
			addControllerType("FAC_3", "Scenario 3.");
			addControllerType("FAC_4", "Scenario 4.");
			logger.info("Added controller types");
		}
		private void addControllerType(String name, String description) throws TorqueException {
			addType(new ControllerTypes(), name, description);
		}
		private void addQueueControllerTypes() throws TorqueException {
			addQueueContorllerType("queue_override", "Queue controllers are there to override RM controller rates to avoid queue spillbacks. Queue Override is the simplest algorithm doing just that.");
			addQueueContorllerType("proportional", "Proportional algorithm.");
			addQueueContorllerType("proportional_integral", "Proportional-Integral algorithm.");
			logger.info("Added queue controller types");
		}
		private void addQueueContorllerType(String name, String description) throws TorqueException {
			addType(new QueueControllerTypes(), name, description);
		}
		private void addEventTypes() throws TorqueException {
			addEventType("link_lanes", "Changes number of lanes in a link.");
			addEventType("fundamental_diagram", "Changes fundamental diagram assigned to a link.");
			addEventType("link_demand_knob", "Changes coefficient by which demand is multiplied at given origin link.");
			addEventType("node_split_ratio", "Changes split ratios at a node.");
			addEventType("control_toggle", null);
			addEventType("global_control_toggle", "Turns all controllers on and off.");
			addEventType("global_demand_knob", "Changes demand coefficients at all origin links of scenario networks.");
			logger.info("Added event types");
		}
		private void addEventType(String name, String description) throws TorqueException {
			addType(new EventTypes(), name, description);
		}
		private void addScenarioElementTypes() throws TorqueException {
			for (String name : new String[] {"link", "node", "network", "signal", "sensor", "controller", "event"})
				addType(new ScenarioElementTypes(), name, null);
			logger.info("Added scenario element types");
		}
		private void addApplicationTypes() throws TorqueException {
			addApplicationType("estimator", null);
			addApplicationType("simulator", null);
			addApplicationType("basic_calibrator", "Calibrates only fundamental diagrams. Currently implemented in BeATS.");
			addApplicationType("calibrator", null);
			logger.info("Added application types");
		}
		private void addApplicationType(String name, String description) throws TorqueException {
			addType(new ApplicationTypes(), name, description);
		}
		private void addQuantityTypes() throws TorqueException {
			addQuantityType("standard", "Non-stochastic standard output.");
			addQuantityType("mean", null);
			addQuantityType("median", null);
			addQuantityType("STD", "Standard deviation.");
			addQuantityType("1Q", null);
			addQuantityType("3Q", null);
			addQuantityType("95percentile", null);
			addQuantityType("min", null);
			addQuantityType("max", null);
			addQuantityType("best_case", "Best case.");
			addQuantityType("worst_case", "Worst case.");
			logger.info("Added quantity types");
		}
		private void addQuantityType(String name, String description) throws TorqueException {
			addType(new QuantityTypes(), name, description);
		}
		private void addAggregationTypes() throws TorqueException {
			addAggregationType("raw", "Output of simulator, estimator, other programs. Other aggregations result from processing �raw� data.");
			addAggregationType("total", null);
			addAggregationType("1min", null);
			addAggregationType("5min", null);
			addAggregationType("15min", null);
			addAggregationType("1hour", null);
			addAggregationType("1day", null);
			logger.info("Added aggregation types");
		}
		private void addAggregationType(String name, String description) throws TorqueException {
			addType(new AggregationTypes(), name, description);
		}
		private void addVehicleTypes() throws TorqueException {
			addVehicleType("general", 1.0);
			addVehicleType("SOV", "Single Occupancy Vehicle.", 1.0);
			addVehicleType("HOV", 1.0);
			addVehicleType("hybrid", 1.0);
			addVehicleType("electric", 1.0);
			addVehicleType("truck2", 1.5);
			addVehicleType("truck3", 2.0);
			addVehicleType("truck4", 3.0);
			addVehicleType("truck5", 4.0);
			addVehicleType("truck6", 5.0);
			addVehicleType("bus", 1.5);
			addVehicleType("bus2", 2.5);
			addVehicleType("motorcycle", 0.8);
		}
		private void addVehicleType(String name, double weight) throws TorqueException {
			addVehicleType(name, null, weight);
		}
		private void addVehicleType(String name, String description, double weight) throws TorqueException {
			VehicleTypes db_vt = new VehicleTypes();
			db_vt.setName(name);
			db_vt.setDescription(description);
			db_vt.setWeight(BigDecimal.valueOf(weight));
			db_vt.setIsStandard(Boolean.TRUE);
			db_vt.save(conn);
		}
	}

}
