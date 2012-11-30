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

package edu.berkeley.path.beats.simulator.output;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.torque.NoRowsException;
import org.apache.torque.TooManyRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import edu.berkeley.path.beats.om.*;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.LinkCumulativeData;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;
import edu.berkeley.path.beats.simulator.SiriusMath;

import com.workingdogs.village.DataSetException;

/**
 * Database output writer
 */
public class DBOutputWriter extends OutputWriterBase {

	public DBOutputWriter(Scenario scenario) {
		super(scenario);
		try {
			db_scenario = ScenariosPeer.retrieveByPK(str2id(scenario.getId()));
		} catch (NoRowsException exc) {
			logger.error("Scenario " + str2id(scenario.getId()) + " was not found in the database");
		} catch (TooManyRowsException exc) {
			logger.error("Data integrity violation", exc);
		} catch (TorqueException exc) {
			logger.error("Could not load scenario " + str2id(scenario.getId()), exc);
		}
		db_vehicle_type = new VehicleTypes[scenario.getNumVehicleTypes()];
		if (null != db_scenario) {
			logger.info("Loading vehicle types");
			Criteria crit = new Criteria();
			crit.addJoin(VehicleTypesPeer.ID, VehicleTypesInSetsPeer.VEH_TYPE_ID);
			crit.add(VehicleTypesInSetsPeer.VEH_TYPE_SET_ID, db_scenario.getVehTypeSetId());
			try {
				@SuppressWarnings("unchecked")
				List<VehicleTypes> db_vt_l = VehicleTypesPeer.doSelect(crit);
				for (VehicleTypes db_vt : db_vt_l)
					for (int i = 0; i < scenario.getNumVehicleTypes(); ++i)
						if (db_vt.getDescription().equals(scenario.getVehicleTypeNames()[i]))
							db_vehicle_type[i] = db_vt;
			} catch (TorqueException exc) {
				logger.error("Failed to load vehicle types for scenario " + db_scenario.getId(), exc);
			}
		}
		scenario.requestLinkCumulatives();
	}

	private static Logger logger = Logger.getLogger(DBOutputWriter.class);

	private Scenarios db_scenario = null;
	VehicleTypes[] db_vehicle_type;
	private SimulationRuns db_simulation_run = null;
	private ApplicationTypes db_application_type = null;
	private AggregationTypes db_aggregation_type_raw = null;
	private QuantityTypes db_quantity_type_mean = null;

	private Long str2id(String id) {
		return Long.parseLong(id, 10);
	}

	boolean success = false;

	private Calendar ts = null;

	public static ApplicationTypes getApplicationTypes(String application_type) throws Exception {
		Criteria crit = new Criteria();
		crit.add(ApplicationTypesPeer.DESCRIPTION, application_type);
		@SuppressWarnings("unchecked")
		List<ApplicationTypes> db_at_l = ApplicationTypesPeer.doSelect(crit);
		if (db_at_l.isEmpty()) {
			ApplicationTypes db_at = new ApplicationTypes();
			db_at.setDescription(application_type);
			db_at.setInUse(Boolean.TRUE);
			db_at.save();
			return db_at;
		} else {
			if (1 < db_at_l.size())
				logger.warn("Found " + db_at_l.size() + " application types '" + application_type + "'");
			return db_at_l.get(0);
		}
	}

	public static AggregationTypes getAggregationTypes(String aggregation_type) throws Exception {
		Criteria crit = new Criteria();
		crit.add(AggregationTypesPeer.DESCRIPTION, aggregation_type);
		@SuppressWarnings("unchecked")
		List<AggregationTypes> db_at_l = AggregationTypesPeer.doSelect(crit);
		if (db_at_l.isEmpty()) {
			AggregationTypes db_at = new AggregationTypes();
			db_at.setDescription(aggregation_type);
			db_at.setInUse(Boolean.TRUE);
			db_at.save();
			return db_at;
		} else {
			if (1 < db_at_l.size())
				logger.warn("Found " + db_at_l.size() + " aggregation types '" + aggregation_type + "'");
			return db_at_l.get(0);
		}
	}

	public static QuantityTypes getQuantityTypes(String quantity_type) throws Exception {
		Criteria crit = new Criteria();
		crit.add(QuantityTypesPeer.DESCRIPTION, quantity_type);
		@SuppressWarnings("unchecked")
		List<QuantityTypes> db_qt_l = QuantityTypesPeer.doSelect(crit);
		if (db_qt_l.isEmpty()) {
			QuantityTypes db_qt = new QuantityTypes();
			db_qt.setDescription(quantity_type);
			db_qt.setInUse(Boolean.TRUE);
			db_qt.save();
			return db_qt;
		} else {
			if (1 < db_qt_l.size())
				logger.warn("Found " + db_qt_l.size() + " quantity types '" + quantity_type + "'");
			return db_qt_l.get(0);
		}
	}

	private static BigDecimal double2decimal(double arg) {
		return Double.isNaN(arg) ? null : BigDecimal.valueOf(arg);
	}

	@Override
	public void open(int run_id) throws SiriusException {
		success = false;
		if (1 != scenario.getNumEnsemble())
			logger.warn("scenario.numEnsembles != 1");
		if (null == db_scenario)
			throw new SiriusException("Scenario was not loaded from the database");

		logger.info("Initializing simulation run");
		try {
			Criteria crit = new Criteria();
			crit.add(ScenariosPeer.ID, db_scenario.getId());
			com.workingdogs.village.Value max_runnum = SimulationRunsPeer.maxColumnValue(SimulationRunsPeer.RUN_NUMBER, crit, null);
			final long run_number = null == max_runnum ? 1 : max_runnum.asLong() + 1;
			logger.info("Run number: " + run_number);

			db_simulation_run = new edu.berkeley.path.beats.om.SimulationRuns();
			db_simulation_run.setScenarios(db_scenario);
			db_simulation_run.setRunNumber(run_number);
			db_simulation_run.setVersion(edu.berkeley.path.beats.Version.get().getEngineVersion());
			if (null == db_simulation_run.getVersion()) db_simulation_run.setVersion("");
			db_simulation_run.setSimStartTime(double2decimal(scenario.getTimeStart()));
			db_simulation_run.setSimDuration(double2decimal(scenario.getTimeEnd() - scenario.getTimeStart()));
			db_simulation_run.setSimDt(double2decimal(scenario.getSimDtInSeconds()));
			db_simulation_run.setOutputDt(double2decimal(scenario.getOutputDt()));
			db_simulation_run.setExecutionStartTime(Calendar.getInstance().getTime());
			db_simulation_run.setStatus(-1);
			db_simulation_run.save();

			db_application_type = getApplicationTypes("simulation");
			db_aggregation_type_raw = getAggregationTypes("raw");
			db_quantity_type_mean = getQuantityTypes("mean");

			success = true;
		} catch (TorqueException exc) {
			throw new SiriusException(exc);
		} catch (DataSetException exc) {
			throw new SiriusException(exc);
		} catch (Exception exc) {
			throw new SiriusException(exc);
		}
		ts = Calendar.getInstance();
		ts.set(Calendar.MILLISECOND, 0);
	}

	@Override
	public void recordstate(double time, boolean exportflows, int outsteps) throws SiriusException {
		success = false;
		double min = Math.floor(time / 60);
		double hrs = Math.floor(min / 60);
		ts.set(Calendar.HOUR_OF_DAY, (int) hrs);
		ts.set(Calendar.MINUTE, (int) (min - hrs * 60));
		ts.set(Calendar.SECOND, (int) (time - min * 60));

		for (edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()) {
			for (edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink()) {
				Link _link = (Link) link;
				try {
					LinkDataTotal db_ldt = fill_total(_link, exportflows);
					fill_detailed(_link, exportflows, db_ldt.getSpeed());
				} catch (Exception exc) {
					throw new SiriusException(exc);
				}
			}
		}
		success = true;
	}

	/**
	 * Fills link_data_total table
	 * @param link
	 * @param param output parameters
	 * @return the stored row
	 * @throws Exception
	 */
	private LinkDataTotal fill_total(Link link, boolean exportflows) throws Exception {
		LinkDataTotal db_ldt = new LinkDataTotal();
		db_ldt.setLinkId(str2id(link.getId()));
		db_ldt.setNetworkId(str2id(link.getMyNetwork().getId()));
		db_ldt.setAppRunId(db_simulation_run.getId());
		db_ldt.setApplicationTypes(db_application_type);
		db_ldt.setTs(ts.getTime());
		db_ldt.setAggregationTypes(db_aggregation_type_raw);
		db_ldt.setQuantityTypes(db_quantity_type_mean);

		LinkCumulativeData link_cum_data = scenario.getCumulatives(link);
		// mean density, vehicles
		double density = exportflows ? link_cum_data.getMeanTotalDensity(0) : SiriusMath.sum(link.getDensityInVeh(0));
		db_ldt.setDensity(double2decimal(density));

		if (exportflows) {
			// input flow, vehicles
			db_ldt.setInFlow(double2decimal(link_cum_data.getCumulativeTotalInputFlow(0)));
			// output flow, vehicles
			db_ldt.setOutFlow(double2decimal(link_cum_data.getCumulativeTotalOutputFlow(0)));

			// free flow speed, m/s
			double ffspeed = link.getVfInMPS(0);
			// speed, m/s
			if (density <= 0)
				db_ldt.setSpeed(double2decimal(ffspeed));
			else {
				double speed = link_cum_data.getMeanTotalOutputFlow(0) * link.getLengthInMeters() / (scenario.getSimDtInSeconds() * density);
				if (!Double.isNaN(speed)) {
					if (!Double.isNaN(ffspeed) && speed > ffspeed)
						db_ldt.setSpeed(double2decimal(ffspeed));
					else
						db_ldt.setSpeed(double2decimal(speed));
				}
			}
		}
		// free flow speed, m/s
		db_ldt.setFreeFlowSpeed(double2decimal(link.getVfInMPS(0)));
		// critical speed, m/s
		db_ldt.setCriticalSpeed(double2decimal(link.getCriticalSpeedInMPS(0)));
		// congestion wave speed, m/s
		db_ldt.setCongestionWaveSpeed(double2decimal(link.getWInMPS(0)));
		// maximum flow, vehicles per second
		db_ldt.setCapacity(double2decimal(link.getCapacityInVPS(0)));
		// jam density, vehicles per meter
		db_ldt.setJamDensity(double2decimal(link.getDensityJamInVeh(0) / link.getLengthInMeters()));
		// capacity drop, vehicle per second
		db_ldt.setCapacityDrop(double2decimal(link.getCapacityDropInVeh(0) / scenario.getSimDtInSeconds()));

		db_ldt.save();
		return db_ldt;
	}

	/**
	 * Fills link_data_detailed table
	 * @param link
	 * @param params output parameters
	 * @param total_speed speed for the cell as a whole, m/s
	 * @throws Exception
	 */
	private void fill_detailed(Link link, boolean exportflows, BigDecimal total_speed) throws Exception {
		LinkCumulativeData link_cum_data = scenario.getCumulatives(link);
		for (int vt_ind = 0; vt_ind < db_vehicle_type.length; ++vt_ind) {
			LinkDataDetailed db_ldd = new LinkDataDetailed();
			db_ldd.setLinkId(str2id(link.getId()));
			db_ldd.setNetworkId(str2id(link.getMyNetwork().getId()));
			db_ldd.setAppRunId(db_simulation_run.getId());
			db_ldd.setApplicationTypes(db_application_type);
			// TODO db_ldd.setDestinationNetworks();
			db_ldd.setVehicleTypes(db_vehicle_type[vt_ind]);
			db_ldd.setTs(ts.getTime());
			db_ldd.setAggregationTypes(db_aggregation_type_raw);
			db_ldd.setQuantityTypes(db_quantity_type_mean);
			// mean density, vehicles
			double density = exportflows ? link_cum_data.getMeanDensity(0, vt_ind) : link.getDensityInVeh(0)[vt_ind];
			db_ldd.setDensity(double2decimal(density));
			if (exportflows) {
				// input flow, vehicles
				db_ldd.setInFlow(double2decimal(link_cum_data.getCumulativeInputFlow(0, vt_ind)));
				// output flow, vehicles
				db_ldd.setOutFlow(double2decimal(link_cum_data.getCumulativeOutputFlow(0, vt_ind)));
				if (density <= 0)
					db_ldd.setSpeed(total_speed);
				else {
					// speed, m/s
					double speed = link_cum_data.getMeanOutputFlow(0, vt_ind) * link.getLengthInMeters() / (scenario.getSimDtInSeconds() * density);
					if (!Double.isNaN(speed)) {
						// free flow speed, m/s
						double ffspeed = link.getVfInMPS(0);
						if (!Double.isNaN(ffspeed) && speed > ffspeed)
							db_ldd.setSpeed(double2decimal(ffspeed));
						else
							db_ldd.setSpeed(double2decimal(speed));
					}
				}
			}
			db_ldd.save();
		}
	}

	@Override
	public void close() {
		ts = null;
		if (null != db_simulation_run) {
			db_simulation_run.setExecutionEndTime(Calendar.getInstance().getTime());
			db_simulation_run.setStatus(success ? 0 : 1);
			try {
				db_simulation_run.save();
			} catch (Exception exc) {
				logger.error("Failed to update simulation run status", exc);
			} finally {
				db_simulation_run = null;
			}
		}
	}

}
