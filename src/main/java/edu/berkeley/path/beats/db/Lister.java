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

import java.text.DateFormat;
import java.util.List;

import org.apache.torque.NoRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import edu.berkeley.path.beats.om.Scenarios;
import edu.berkeley.path.beats.om.ScenariosPeer;
import edu.berkeley.path.beats.om.SimulationRuns;
import edu.berkeley.path.beats.om.SimulationRunsPeer;
import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * Implements "list" commands
 */
public class Lister {

	public static void listScenarios() throws BeatsException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		
		try {
			Criteria crit = new Criteria();
			crit.addAscendingOrderByColumn(ScenariosPeer.ID);
			@SuppressWarnings("unchecked")
			List<Scenarios> db_scenarios = ScenariosPeer.doSelect(crit);
			for (Scenarios db_scenario : db_scenarios) {
				StringBuilder sb = new StringBuilder(String.format("%2d", db_scenario.getId()));
				if (null != db_scenario.getName())
					sb.append(" ").append(db_scenario.getName());
				if (null != db_scenario.getDescription())
					sb.append(" ").append(db_scenario.getDescription());
				System.out.println(sb.toString());
			}
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

	public static void listRuns(long scenario_id) throws BeatsException {
		edu.berkeley.path.beats.db.Service.ensureInit();
		DateFormat date_format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		try {
			Scenarios db_scenario = ScenariosPeer.retrieveByPK(scenario_id);
			Criteria crit = new Criteria();
			crit.addAscendingOrderByColumn(SimulationRunsPeer.RUN_NUMBER);
			@SuppressWarnings("unchecked")
			List<SimulationRuns> db_run_l = db_scenario.getSimulationRunss(crit);
			for (SimulationRuns db_sr : db_run_l) {
				StringBuilder sb = new StringBuilder(String.format("%2d", db_sr.getRunNumber()));
				if (null != db_sr.getExecutionStartTime()) {
					sb.append("\t" + date_format.format(db_sr.getExecutionStartTime()));
					if (null != db_sr.getExecutionEndTime())
						sb.append(" -- " + date_format.format(db_sr.getExecutionEndTime()));
				}
				System.out.println(sb.toString());
			}
		} catch (NoRowsException exc) {
			throw new BeatsException("Scenario " + scenario_id + " does not exist");
		} catch (TorqueException exc) {
			throw new BeatsException(exc);
		}
	}

}
