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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;

import edu.berkeley.path.beats.om.*;
import edu.berkeley.path.beats.simulator.BeatsException;

public class Cleaner {

	private static Logger logger = Logger.getLogger(Cleaner.class);

	private static String select2delete(String query) {
		if (query.startsWith("SELECT")) return query.replaceFirst("SELECT", "DELETE");
		else {
			logger.warn("No SELECT in '" + query + "'");
			return query;
		}
	}

	private static void executeStatement(String statement) throws TorqueException {
		logger.debug(statement);
		BasePeer.executeStatement(statement);
	}

	private static ApplicationTypes getApplicationTypes(String application_type) throws BeatsException {
		Criteria crit = new Criteria();
		crit.add(ApplicationTypesPeer.NAME, application_type);
		try{
			@SuppressWarnings("unchecked")
			List<ApplicationTypes> db_at_l = ApplicationTypesPeer.doSelect(crit);
			if (db_at_l.isEmpty()) {
				throw new BeatsException("Application type '" + application_type + "' does not exist");
			} else {
				if (1 < db_at_l.size())
					logger.warn("Found " + db_at_l.size() + " application types '" + application_type + "'");
				return db_at_l.get(0);
			}
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	private static AggregationTypes getAggregationTypes(String aggregation_type) throws BeatsException {
		Criteria crit = new Criteria();
		crit.add(AggregationTypesPeer.NAME, aggregation_type);
		try {
			@SuppressWarnings("unchecked")
			List<AggregationTypes> db_at_l = AggregationTypesPeer.doSelect(crit);
			if (db_at_l.isEmpty()) {
				throw new BeatsException("Aggregation type '" + aggregation_type + "' does not exist");
			} else {
				if (1 < db_at_l.size())
					logger.warn("Found " + db_at_l.size() + " aggregation types '" + aggregation_type + "'");
				return db_at_l.get(0);
			}
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	private static List<AggregationTypes> getAggregationTypes(List<String> aggregation_l) throws BeatsException {
		Criteria crit = new Criteria();
		crit.addIn(AggregationTypesPeer.NAME, aggregation_l);
		try {
			@SuppressWarnings("unchecked")
			List<AggregationTypes> db_agg_type_l = AggregationTypesPeer.doSelect(crit);
			return db_agg_type_l;
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	private static List<Long> getAggregationTypeIds(List<String> aggregation_l) throws BeatsException {
		List<AggregationTypes> db_agg_type_l = getAggregationTypes(aggregation_l);
		List<Long> agg_type_id_l = new java.util.ArrayList<Long>(db_agg_type_l.size());
		for (AggregationTypes db_agg_type : db_agg_type_l)
			agg_type_id_l.add(db_agg_type.getId());
		return agg_type_id_l;
	}

	private static List<SimulationRuns> getSimulationRuns(long scenario_id, List<Long> run_number_l) throws BeatsException {
		Scenarios db_scenario = null;
		try {
			db_scenario = ScenariosPeer.retrieveByPK(scenario_id);
		} catch (NoRowsException exc) {
			throw new BeatsException("Scenario '" + scenario_id + "' does not exist", exc);
		} catch (TooManyRowsException exc) {
			throw new BeatsException("Table " + ScenariosPeer.TABLE_NAME + ": consistency broken", exc);
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
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
			throw new BeatsException(exc);
		}
	}

	private static List<Long> getSimulationRunIds(long scenario_id, List<Long> run_number_l) throws BeatsException {
		List<SimulationRuns> db_sr_l = getSimulationRuns(scenario_id, run_number_l);
		List<Long> sr_id_l = new java.util.ArrayList<Long>(db_sr_l.size());
		StringBuilder sb = new StringBuilder();
		for (SimulationRuns db_sr : db_sr_l) {
			sr_id_l.add(db_sr.getId());
			if (0 < sb.length()) sb.append(", ");
			sb.append(db_sr.getRunNumber());
		}
		logger.info("Run numbers: " + sb.toString());
		return sr_id_l;
	}

	/**
	 * Erases processing results for the given scenario
	 * @param scenario_id the scenario ID
	 * @throws BeatsException
	 */
	public static void clearProcessed(long scenario_id) throws BeatsException {
		clearProcessed(scenario_id, null);
	}

	/**
	 * Erases processing results (aggregated data and performance data)
	 * for the given scenario and run numbers
	 * @param scenario_id the scenario ID
	 * @param run_number_l a list of run numbers; if null, assume all run numbers
	 * @throws BeatsException
	 */
	public static void clearProcessed(long scenario_id, List<Long> run_number_l) throws BeatsException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		final List<Long> sr_id_l = getSimulationRunIds(scenario_id, run_number_l);
		if (0 == sr_id_l.size()) {
			logger.warn("No simulation runs. Aborting");
			return;
		}
		final Long app_type_id = getApplicationTypes("simulator").getId();
		final Long agg_type_id = getAggregationTypes("raw").getId();

		Criteria crit = new Criteria();
		try{
			logger.info("Clearing " + LinkDataTotalPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkDataTotalPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkDataTotalPeer.APP_RUN_ID, sr_id_l);
			crit.add(LinkDataTotalPeer.AGG_TYPE_ID, agg_type_id, Criteria.NOT_EQUAL);
			executeStatement(select2delete(LinkDataTotalPeer.createQueryString(crit)));

			logger.info("Clearing " + LinkDataDetailedPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkDataDetailedPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkDataDetailedPeer.APP_RUN_ID, sr_id_l);
			crit.add(LinkDataDetailedPeer.AGG_TYPE_ID, agg_type_id, Criteria.NOT_EQUAL);
			executeStatement(select2delete(LinkDataDetailedPeer.createQueryString(crit)));

			logger.info("Clearing " + SignalDataPeer.TABLE_NAME);
			crit.clear();
			crit.add(SignalDataPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(SignalDataPeer.APP_RUN_ID, sr_id_l);
			crit.add(SignalDataPeer.AGG_TYPE_ID, agg_type_id, Criteria.NOT_EQUAL);
			executeStatement(select2delete(SignalDataPeer.createQueryString(crit)));

			logger.info("Clearing " + LinkPerformanceTotalPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkPerformanceTotalPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkPerformanceTotalPeer.APP_RUN_ID, sr_id_l);
			executeStatement(select2delete(LinkPerformanceTotalPeer.createQueryString(crit)));

			logger.info("Clearing " + LinkPerformanceDetailedPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkPerformanceDetailedPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkPerformanceDetailedPeer.APP_RUN_ID, sr_id_l);
			executeStatement(select2delete(LinkPerformanceDetailedPeer.createQueryString(crit)));

			logger.info("Clearing " + RoutePerformanceTotalPeer.TABLE_NAME);
			crit.clear();
			crit.add(RoutePerformanceTotalPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(RoutePerformanceTotalPeer.APP_RUN_ID, sr_id_l);
			executeStatement(select2delete(RoutePerformanceTotalPeer.createQueryString(crit)));

			logger.info("Clearing " + SignalPhasePerformancePeer.TABLE_NAME);
			crit.clear();
			crit.add(SignalPhasePerformancePeer.APP_TYPE_ID, app_type_id);
			crit.addIn(SignalPhasePerformancePeer.APP_RUN_ID, sr_id_l);
			executeStatement(select2delete(SignalPhasePerformancePeer.createQueryString(crit)));
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	/**
	 * Erases scenario simulation results and processing results
	 * @param scenario_id the scenario ID
	 * @throws BeatsException
	 */
	public static void clearData(long scenario_id) throws BeatsException {
		clearData(scenario_id, null, null);
	}

	/**
	 * Erases scenario simulation results and processing results for the given run numbers
	 * @param scenario_id the scenario ID
	 * @param run_number_l a list of run numbers; if null, assume all run numbers
	 * @param aggregation_l a list of aggregation types; if null, assume all types
	 * @throws BeatsException
	 */
	public static void clearData(long scenario_id, List<Long> run_number_l, List<String> aggregation_l) throws BeatsException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		final List<Long> sr_id_l = getSimulationRunIds(scenario_id, run_number_l);
		if (0 == sr_id_l.size()) {
			logger.warn("No simulation runs. Aborting");
			return;
		}
		final Long app_type_id = getApplicationTypes("simulator").getId();
		List<Long> agg_type_id_l = null;
		if (null != aggregation_l) {
			agg_type_id_l = getAggregationTypeIds(aggregation_l);
			if (0 == agg_type_id_l.size()) {
				logger.warn("No aggregation types. Aborting");
				return;
			}
		}

		try {
			Criteria crit = new Criteria();

			logger.info("Clearing " + LinkDataTotalPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkDataTotalPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkDataTotalPeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(LinkDataTotalPeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(LinkDataTotalPeer.createQueryString(crit)));

			logger.info("Clearing " + LinkDataDetailedPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkDataDetailedPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkDataDetailedPeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(LinkDataDetailedPeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(LinkDataDetailedPeer.createQueryString(crit)));

			logger.info("Clearing " + SignalDataPeer.TABLE_NAME);
			crit.clear();
			crit.add(SignalDataPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(SignalDataPeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(SignalDataPeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(SignalDataPeer.createQueryString(crit)));

			logger.info("Clearing " + LinkPerformanceTotalPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkPerformanceTotalPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkPerformanceTotalPeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(LinkPerformanceTotalPeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(LinkPerformanceTotalPeer.createQueryString(crit)));

			logger.info("Clearing " + LinkPerformanceDetailedPeer.TABLE_NAME);
			crit.clear();
			crit.add(LinkPerformanceDetailedPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(LinkPerformanceDetailedPeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(LinkPerformanceDetailedPeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(LinkPerformanceDetailedPeer.createQueryString(crit)));

			logger.info("Clearing " + RoutePerformanceTotalPeer.TABLE_NAME);
			crit.clear();
			crit.add(RoutePerformanceTotalPeer.APP_TYPE_ID, app_type_id);
			crit.addIn(RoutePerformanceTotalPeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(RoutePerformanceTotalPeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(RoutePerformanceTotalPeer.createQueryString(crit)));

			logger.info("Clearing " + SignalPhasePerformancePeer.TABLE_NAME);
			crit.clear();
			crit.add(SignalPhasePerformancePeer.APP_TYPE_ID, app_type_id);
			crit.addIn(SignalPhasePerformancePeer.APP_RUN_ID, sr_id_l);
			if (null != agg_type_id_l) crit.addIn(SignalPhasePerformancePeer.AGG_TYPE_ID, agg_type_id_l);
			executeStatement(select2delete(SignalPhasePerformancePeer.createQueryString(crit)));
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	/**
	 * Erases a scenario and all related data
	 * @param scenario_id the scenario ID
	 * @throws BeatsException
	 */
	public static void clearScenario(long scenario_id) throws BeatsException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		logger.info("Deleting scenario simulation results, aggregated data and performance data");
		clearData(scenario_id);

		Criteria crit = new Criteria();
		crit.add(ScenariosPeer.ID, scenario_id);
		try {
			ScenariosPeer.doDelete(crit);
			logger.info("Scenario " + scenario_id + " deleted");
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

}
