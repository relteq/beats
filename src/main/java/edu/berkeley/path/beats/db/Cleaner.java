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

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;

import edu.berkeley.path.beats.om.*;
import edu.berkeley.path.beats.simulator.SiriusException;

public class Cleaner {

	private static Logger logger = Logger.getLogger(Cleaner.class);

	private static String select2delete(String query) {
		if (query.startsWith("SELECT")) return query.replaceFirst("SELECT", "DELETE");
		else {
			logger.warn("No SELECT in '" + query + "'");
			return query;
		}
	}

	private static void executeStatement(String statement, Connection conn) throws TorqueException {
		logger.debug(statement);
		BasePeer.executeStatement(statement, conn);
	}

	private static ApplicationTypes getApplicationTypes(String application_type, Connection conn) throws SiriusException {
		Criteria crit = new Criteria();
		crit.add(ApplicationTypesPeer.NAME, application_type);
		try{
			@SuppressWarnings("unchecked")
			List<ApplicationTypes> db_at_l = ApplicationTypesPeer.doSelect(crit, conn);
			if (db_at_l.isEmpty()) {
				throw new SiriusException("Application type '" + application_type + "' does not exist");
			} else {
				if (1 < db_at_l.size())
					logger.warn("Found " + db_at_l.size() + " application types '" + application_type + "'");
				return db_at_l.get(0);
			}
		} catch (TorqueException exc) {
			throw new SiriusException(exc);
		}
	}

	private static AggregationTypes getAggregationTypes(String aggregation_type, Connection conn) throws SiriusException {
		Criteria crit = new Criteria();
		crit.add(AggregationTypesPeer.NAME, aggregation_type);
		try {
			@SuppressWarnings("unchecked")
			List<AggregationTypes> db_at_l = AggregationTypesPeer.doSelect(crit, conn);
			if (db_at_l.isEmpty()) {
				throw new SiriusException("Aggregation type '" + aggregation_type + "' does not exist");
			} else {
				if (1 < db_at_l.size())
					logger.warn("Found " + db_at_l.size() + " aggregation types '" + aggregation_type + "'");
				return db_at_l.get(0);
			}
		} catch (TorqueException exc) {
			throw new SiriusException(exc);
		}
	}

	private static List<SimulationRuns> getSimulationRuns(long scenario_id, List<Long> run_number_l) throws SiriusException {
		Scenarios db_scenario = null;
		try {
			db_scenario = ScenariosPeer.retrieveByPK(scenario_id);
		} catch (NoRowsException exc) {
			throw new SiriusException("Scenario '" + scenario_id + "' does not exist", exc);
		} catch (TooManyRowsException exc) {
			throw new SiriusException("Table " + ScenariosPeer.TABLE_NAME + ": consistency broken", exc);
		} catch (TorqueException exc) {
			throw new SiriusException(exc);
		}
		try {
			if (null == run_number_l) {
				Criteria crit = new Criteria();
				crit.addAscendingOrderByColumn(SimulationRunsPeer.RUN_NUMBER);
				@SuppressWarnings("unchecked")
				List<SimulationRuns> db_sr_l = db_scenario.getSimulationRunss(crit);
				return db_sr_l;
			} else {
				List<SimulationRuns> db_sr_l = new java.util.ArrayList<SimulationRuns>(run_number_l.size());
				for (Long run_number : run_number_l) {
					Criteria crit = new Criteria();
					crit.add(SimulationRunsPeer.RUN_NUMBER, run_number);
					@SuppressWarnings("unchecked")
					List<SimulationRuns> db_sr_curnum_l = db_scenario.getSimulationRunss(crit);
					if (0 == db_sr_curnum_l.size())
						logger.warn("No simulation runs for scenario " + scenario_id + ", run number " + run_number);
					else if (1 < db_sr_curnum_l.size())
						logger.warn("UNIQUE constraint {" + SimulationRunsPeer.SCENARIO_ID + ", " + SimulationRunsPeer.RUN_NUMBER + "} violated");
					db_sr_l.addAll(db_sr_curnum_l);
				}
				return db_sr_l;
			}
		} catch (TorqueException exc) {
			throw new SiriusException(exc);
		}
	}

	/**
	 * Erases processing results for the given scenario
	 * @param scenario_id the scenario ID
	 * @throws SiriusException
	 */
	public static void clearProcessed(long scenario_id) throws SiriusException {
		clearProcessed(scenario_id, null);
	}

	/**
	 * Erases processing results (aggregated data and performance data)
	 * for the given scenario and run numbers
	 * @param scenario_id the scenario ID
	 * @param run_number_l a list of run numbers; if null, assume all run numbers
	 * @throws SiriusException
	 */
	public static void clearProcessed(long scenario_id, List<Long> run_number_l) throws SiriusException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		List<SimulationRuns> db_sr_l = getSimulationRuns(scenario_id, run_number_l);

		Connection conn = null;
		try {
			conn = Transaction.begin();

			final Long app_type_id = getApplicationTypes("simulator", conn).getId();
			final Long agg_type_id = getAggregationTypes("raw", conn).getId();

			Criteria crit = new Criteria();
			for (SimulationRuns db_sr : db_sr_l) {
				logger.info("Run number: " + db_sr.getRunNumber());

				// link_data_total
				crit.clear();
				crit.add(LinkDataTotalPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkDataTotalPeer.APP_RUN_ID, db_sr.getId());
				crit.add(LinkDataTotalPeer.AGG_TYPE_ID, agg_type_id, Criteria.NOT_EQUAL);
				executeStatement(select2delete(LinkDataTotalPeer.createQueryString(crit)), conn);

				// link_data_detailed
				crit.clear();
				crit.add(LinkDataDetailedPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkDataDetailedPeer.APP_RUN_ID, db_sr.getId());
				crit.add(LinkDataDetailedPeer.AGG_TYPE_ID, agg_type_id, Criteria.NOT_EQUAL);
				executeStatement(select2delete(LinkDataDetailedPeer.createQueryString(crit)), conn);

				// signal_data
				crit.clear();
				crit.add(SignalDataPeer.APP_TYPE_ID, app_type_id);
				crit.add(SignalDataPeer.APP_RUN_ID, db_sr.getId());
				crit.add(SignalDataPeer.AGG_TYPE_ID, agg_type_id, Criteria.NOT_EQUAL);
				executeStatement(select2delete(SignalDataPeer.createQueryString(crit)), conn);

				// link_performance_total
				crit.clear();
				crit.add(LinkPerformanceTotalPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkPerformanceTotalPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(LinkPerformanceTotalPeer.createQueryString(crit)), conn);

				// link_performance_detailed
				crit.clear();
				crit.add(LinkPerformanceDetailedPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkPerformanceDetailedPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(LinkPerformanceDetailedPeer.createQueryString(crit)), conn);

				// route_performance_total
				crit.clear();
				crit.add(RoutePerformanceTotalPeer.APP_TYPE_ID, app_type_id);
				crit.add(RoutePerformanceTotalPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(RoutePerformanceTotalPeer.createQueryString(crit)), conn);

				// signal_phase_performance
				crit.clear();
				crit.add(SignalPhasePerformancePeer.APP_TYPE_ID, app_type_id);
				crit.add(SignalPhasePerformancePeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(SignalPhasePerformancePeer.createQueryString(crit)), conn);
			}

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

	/**
	 * Erases scenario simulation results and processing results
	 * @param scenario_id the scenario ID
	 * @throws SiriusException
	 */
	public static void clearData(long scenario_id) throws SiriusException {
		clearData(scenario_id, null);
	}

	/**
	 * Erases scenario simulation results and processing results for the given run numbers
	 * @param scenario_id the scenario ID
	 * @param run_number_l a list of run numbers; if null, assume all run numbers
	 * @throws SiriusException
	 */
	public static void clearData(long scenario_id, List<Long> run_number_l) throws SiriusException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		List<SimulationRuns> db_sr_l = getSimulationRuns(scenario_id, run_number_l);

		Connection conn = null;
		try {
			conn = Transaction.begin();

			final Long app_type_id = getApplicationTypes("simulator", conn).getId();

			Criteria crit = new Criteria();
			for (SimulationRuns db_sr : db_sr_l) {
				logger.info("Run number: " + db_sr.getRunNumber());

				// link_data_total
				crit.clear();
				crit.add(LinkDataTotalPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkDataTotalPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(LinkDataTotalPeer.createQueryString(crit)), conn);

				// link_data_detailed
				crit.clear();
				crit.add(LinkDataDetailedPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkDataDetailedPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(LinkDataDetailedPeer.createQueryString(crit)), conn);

				// signal_data
				crit.clear();
				crit.add(SignalDataPeer.APP_TYPE_ID, app_type_id);
				crit.add(SignalDataPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(SignalDataPeer.createQueryString(crit)), conn);

				// link_performance_total
				crit.clear();
				crit.add(LinkPerformanceTotalPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkPerformanceTotalPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(LinkPerformanceTotalPeer.createQueryString(crit)), conn);

				// link_performance_detailed
				crit.clear();
				crit.add(LinkPerformanceDetailedPeer.APP_TYPE_ID, app_type_id);
				crit.add(LinkPerformanceDetailedPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(LinkPerformanceDetailedPeer.createQueryString(crit)), conn);

				// route_performance_total
				crit.clear();
				crit.add(RoutePerformanceTotalPeer.APP_TYPE_ID, app_type_id);
				crit.add(RoutePerformanceTotalPeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(RoutePerformanceTotalPeer.createQueryString(crit)), conn);

				// signal_phase_performance
				crit.clear();
				crit.add(SignalPhasePerformancePeer.APP_TYPE_ID, app_type_id);
				crit.add(SignalPhasePerformancePeer.APP_RUN_ID, db_sr.getId());
				executeStatement(select2delete(SignalPhasePerformancePeer.createQueryString(crit)), conn);
			}

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

}
