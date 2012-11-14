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
import org.apache.torque.TorqueException;
import org.apache.torque.util.BasePeer;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;

import edu.berkeley.path.beats.om.LinkDataDetailedPeer;
import edu.berkeley.path.beats.om.LinkDataTotalPeer;
import edu.berkeley.path.beats.om.LinkPerformanceDetailedPeer;
import edu.berkeley.path.beats.om.LinkPerformanceTotalPeer;
import edu.berkeley.path.beats.om.ScenariosPeer;
import edu.berkeley.path.beats.om.SimulationRuns;
import edu.berkeley.path.beats.om.SimulationRunsPeer;
import edu.berkeley.path.beats.simulator.SiriusException;

public class Cleaner {

	/**
	 * Initialize the DB service if it hasn't been initialized yet
	 * @throws SiriusException
	 */
	private static void initDB() throws SiriusException {
		if (!edu.berkeley.path.beats.db.Service.isInit()) edu.berkeley.path.beats.db.Service.init();
	}

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

	public static void clearProcessed(long scenario_id) throws SiriusException {
		initDB();
		Connection conn = null;
		try {
			conn = Transaction.begin();
			try {
				ScenariosPeer.retrieveByPK(scenario_id, conn);
			} catch (NoRowsException exc) {
				throw new SiriusException("Scenario '" + scenario_id + "\' does not exist", exc);
			}

			Criteria crit = new Criteria();
			crit.add(SimulationRunsPeer.SCENARIO_ID, scenario_id);
			crit.addAscendingOrderByColumn(SimulationRunsPeer.RUN_NUMBER);
			@SuppressWarnings("unchecked")
			List<SimulationRuns> db_sr_l = SimulationRunsPeer.doSelect(crit, conn);

			for (SimulationRuns db_sr : db_sr_l) {
				logger.info("Run number: " + db_sr.getRunNumber());

				crit.clear();
				crit.add(LinkDataTotalPeer.DATA_SOURCE_ID, db_sr.getDataSourceId());
				crit.add(LinkDataTotalPeer.AGGREGATION, (Object) "raw", Criteria.NOT_EQUAL);
				executeStatement(select2delete(LinkDataTotalPeer.createQueryString(crit)), conn);

				crit.clear();
				crit.add(LinkDataDetailedPeer.DATA_SOURCE_ID, db_sr.getDataSourceId());
				crit.add(LinkDataDetailedPeer.AGGREGATION, (Object) "raw", Criteria.NOT_EQUAL);
				executeStatement(select2delete(LinkDataDetailedPeer.createQueryString(crit)), conn);

				crit.clear();
				crit.add(LinkPerformanceTotalPeer.DATA_SOURCE_ID, db_sr.getDataSourceId());
				executeStatement(select2delete(LinkPerformanceTotalPeer.createQueryString(crit)), conn);

				crit.clear();
				crit.add(LinkPerformanceDetailedPeer.DATA_SOURCE_ID, db_sr.getDataSourceId());
				executeStatement(select2delete(LinkPerformanceDetailedPeer.createQueryString(crit)), conn);
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
