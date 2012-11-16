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
import edu.berkeley.path.beats.simulator.SiriusErrorLog;
import edu.berkeley.path.beats.simulator.SiriusException;

/**
 * Administers the Sirius Database
 */
public class Admin {

	/**
	 * Initializes the database.
	 * If the database exists, it is dropped and recreated.
	 * @throws SQLException
	 * @throws IOException
	 * @throws SiriusException
	 */
	public static void init() throws SQLException, IOException, SiriusException {
		init(Parameters.fromEnvironment());
	}

	/**
	 * Initializes the database
	 * @param params the database connection parameters
	 * @throws SQLException
	 * @throws IOException
	 * @throws SiriusException
	 */
	public static void init(Parameters params) throws SQLException, IOException, SiriusException {
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
				throw new SiriusException(exc);
			} finally {
				Service.shutdown();
				params.setDBName(dbname);
			}
		}
		Service.init(params);
		exec.runStatements(new java.io.InputStreamReader(Admin.class.getClassLoader().getResourceAsStream(
				"sql" + File.separator + params.getDriver() + File.separator + "sirius-db-schema.sql")),
				System.err);
		logger.info("Database tables created");

		new Initializer().run();
		logger.info("Database " + params.getDBName() + " initialized");
	}

	/**
	 * Executes SQL statements
	 */
	public static class SQLExec extends org.apache.torque.task.TorqueSQLExec {
		public SQLExec() {
			org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
			project.init();
			setProject(project);
		}

		@Override
		public void runStatements(java.io.Reader reader, java.io.PrintStream out) throws IOException, SQLException {
			super.runStatements(reader, out);
		}

		@Override
		protected void execSQL(String sql, java.io.PrintStream out) {
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
				SiriusErrorLog.addError(exc.getMessage());
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
			} catch (SiriusException exc) {
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
		public void run() throws SiriusException {
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
				throw new SiriusException(exc);
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
		private void addType(BaseTypes db_type, String name) throws TorqueException {
			db_type.setDescription(name);
			db_type.setInUse(Boolean.TRUE);
			db_type.save(conn);
		}
		private void addNodeTypes() throws TorqueException {
			for (String name : new String[] {"freeway", "highway", "signalized_intersection", "stop_intersection", "simple", "terminal", "other"})
				addType(new NodeTypes(), name);
			logger.info("Added node types");
		}
		private void addLinkTypes() throws TorqueException {
			for (String name : new String[] {"freeway", "highway", "onramp", "offramp", "interconnect", "HOV", "HOT", "toll", "heavy_vehicle", "bus", "street", "intersection_approach", "left_turn_pocket", "right_turn_pocket"})
				addType(new LinkTypes(), name);
			logger.info("Added link types");
		}
		private void addFDTypes() throws TorqueException {
			for (String name : new String[] {"triangular", "trapezoidal", "linear-hyperbolic", "greenshields"})
				addType(new FundamentalDiagramTypes(), name);
			logger.info("Added fundamental diagram types");
		}
		private void addSensorTypes() throws TorqueException {
			for (String name : new String[] {"static_point", "static_area", "moving_point"})
				addType(new SensorTypes(), name);
			logger.info("Added sensor types");
		}
		private void addControllerTypes() throws TorqueException {
			for (String name : new String[] {"IRM_alinea", "IRM_time_of_day", "IRM_traffic_responsive", "CRM_swarm", "CRM_hero", "VSL_time_of_day", "SIG_pretimed", "SIG_actuated"})
				addType(new ControllerTypes(), name);
			logger.info("Added controller types");
		}
		private void addQueueControllerTypes() throws TorqueException {
			for (String name : new String[] {"none", "queue_override", "proportional", "proportional_integral"})
				addType(new QueueControllerTypes(), name);
			logger.info("Added queue controller types");
		}
		private void addEventTypes() throws TorqueException {
			for (String name : new String[] {"fundamental_diagram", "link_demand_knob", "link_lanes", "node_split_ratio", "control_toggle", "global_control_toggle", "global_demand_knob"})
				addType(new EventTypes(), name);
			logger.info("Added event types");
		}
		private void addScenarioElementTypes() throws TorqueException {
			for (String name : new String[] {"link", "node", "network", "signal", "sensor", "controller", "event"})
				addType(new ScenarioElementTypes(), name);
			logger.info("Added scenario element types");
		}
		private void addApplicationTypes() throws TorqueException {
			for (String name : new String[] {"estimator", "simulator", "basic_calibrator", "calibrator"})
				addType(new ApplicationTypes(), name);
			logger.info("Added application types");
		}
		private void addQuantityTypes() throws TorqueException {
			for (String name : new String[] {"standard", "mean", "median", "STD", "1Q", "3Q", "95percentile", "min", "max", "best_case", "worst_case"})
				addType(new QuantityTypes(), name);
			logger.info("Added quantity types");
		}
		private void addAggregationTypes() throws TorqueException {
			for (String name : new String[] {"raw", "total", "1min", "5min", "15min", "1hour", "1day"})
				addType(new AggregationTypes(), name);
			logger.info("Added aggregation types");
		}
		private void addVehicleTypes() throws TorqueException {
			addVehicleType("general", 1.0);
			addVehicleType("SOV", 1.0);
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
			VehicleTypes db_vt = new VehicleTypes();
			db_vt.setDescription(name);
			db_vt.setWeight(BigDecimal.valueOf(weight));
			db_vt.setIsStandard(Boolean.TRUE);
			db_vt.save(conn);
		}
	}

}
