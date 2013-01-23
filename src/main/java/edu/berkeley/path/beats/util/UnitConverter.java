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

package edu.berkeley.path.beats.util;

import java.math.BigDecimal;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.jaxb.*;
import edu.berkeley.path.beats.simulator.SiriusException;
import edu.berkeley.path.beats.util.scenario.ScenarioLoader;
import edu.berkeley.path.beats.util.scenario.ScenarioSaver;

/**
 * Converts scenario units
 * from US (mile, hour) or Metric (kilometer, hour)
 * to SI (meter, second).
 */
public class UnitConverter {
	/**
	 * Loads a scenario, performs unit conversion, saves the resulting scenario
	 * @param iconfig input scenario file
	 * @param oconfig output file
	 * @throws SiriusException
	 */
	public static void convertUnits(String iconfig, String oconfig) throws SiriusException {
		Scenario scenario = ScenarioLoader.loadRaw(iconfig);
		process(scenario);
		ScenarioSaver.save(scenario, oconfig);
	}

	private static Logger logger = Logger.getLogger(UnitConverter.class);

	private enum UnitSystem {SI, US, METRIC};

	private static UnitSystem getUnitSystem(String units) throws SiriusException {
		if (units.equalsIgnoreCase("SI"))
			return UnitSystem.SI;
		else if (units.equalsIgnoreCase("US"))
			return UnitSystem.US;
		else if (units.equalsIgnoreCase("Metric"))
			return UnitSystem.METRIC;
		else
			throw new SiriusException("Unknown units '" + units + "'");
	}

	private static Unit<Length> getLengthUnit(UnitSystem usystem) {
		switch (usystem) {
		case SI:
			return SI.METER;
		case US:
			return NonSI.MILE;
		case METRIC:
			return SI.KILOMETER;
		default:
			return null;
		}
	}

	private static Unit<? extends Quantity> getDensityUnit(UnitSystem usystem) {
		return getLengthUnit(usystem).inverse();
	}

	private static Unit<Duration> getDurationUnit(UnitSystem usystem) {
		switch (usystem) {
		case SI:
			return SI.SECOND;
		case US:
		case METRIC:
			return NonSI.HOUR;
		default:
			return null;
		}
	}

	private static Unit<? extends Quantity> getFlowUnit(UnitSystem usystem) {
		return getDurationUnit(usystem).inverse();
	}

	private static Unit<? extends Quantity> getFlowUnit(String units) throws SiriusException {
		return getFlowUnit(getUnitSystem(units));
	}

	private static Unit<Velocity> getSpeedUnit(UnitSystem usystem) {
		switch (usystem) {
		case SI:
			return SI.METERS_PER_SECOND;
		case US:
			return NonSI.MILES_PER_HOUR;
		case METRIC:
			return NonSI.KILOMETERS_PER_HOUR;
		default:
			return null;
		}
	}

	private static Unit<Velocity> getSpeedUnit(String units) throws SiriusException {
		return getSpeedUnit(getUnitSystem(units));
	}

	/**
	 * Creates a to-SI flow converter
	 * @param iunits the units of the input data
	 * @return a converter
	 * @throws SiriusException
	 */
	public static javax.measure.converter.UnitConverter getFlowConverter(String iunits) throws SiriusException {
		return getFlowConverter(iunits, "SI");
	}

	/**
	 * Creates a flow converter
	 * @param iunits the units of the input data
	 * @param ounits the units of the output data
	 * @return a converter
	 * @throws SiriusException
	 */
	public static javax.measure.converter.UnitConverter getFlowConverter(String iunits, String ounits) throws SiriusException {
		return getFlowUnit(iunits).getConverterTo(getFlowUnit(ounits));
	}

	/**
	 * Creates a to-SI unit converter
	 * @param iunits the units of the input data
	 * @return a converter
	 * @throws SiriusException
	 */
	public static javax.measure.converter.UnitConverter getSpeedConverter(String iunits) throws SiriusException {
		return getSpeedConverter(iunits, "SI");
	}

	/**
	 * Creates a speed converter
	 * @param iunits the units of the input data
	 * @param ounits the units of the output data
	 * @return a converter
	 * @throws SiriusException
	 */
	public static javax.measure.converter.UnitConverter getSpeedConverter(String iunits, String ounits) throws SiriusException {
		return getSpeedUnit(iunits).getConverterTo(getSpeedUnit(ounits));
	}

	/**
	 * Performs in-line unit conversion from the given units (settings/units) to SI
	 * @param scenario
	 * @throws SiriusException
	 */
	public static void process(Scenario scenario) throws SiriusException {
		String units = null;
		if (null != scenario.getSettings())
			units = scenario.getSettings().getUnits();
		if (null == units)
			throw new SiriusException("no units");
		process(scenario, units, "SI");
	}

	/** Performs in-line unit conversion
	 * @param scenario
	 * @param iunits input units
	 * @param ounits output units
	 * @throws SiriusException
	 */
	public static void process(Scenario scenario, String iunits, String ounits) throws SiriusException {
		UnitSystem iusystem = getUnitSystem(iunits);
		UnitSystem ousystem = getUnitSystem(ounits);
		if (iusystem == ousystem) {
			logger.info("Units are equal. Skipping unit conversion");
			return;
		}
		new UnitConverter(scenario, iusystem, ousystem).process();

		if (null == scenario.getSettings())
			scenario.setSettings(new edu.berkeley.path.beats.simulator.JaxbObjectFactory().createSettings());
		scenario.getSettings().setUnits(ounits);
	}

	private Scenario scenario = null;

	private javax.measure.converter.UnitConverter lconv = null; // length converter
	private javax.measure.converter.UnitConverter dconv = null; // density converter
	private javax.measure.converter.UnitConverter fconv = null; // flow converter
	private javax.measure.converter.UnitConverter sconv = null; // speed converter

	private UnitConverter(Scenario scenario, UnitSystem iusystem, UnitSystem ousystem) {
		this.scenario = scenario;

		lconv = getLengthUnit(iusystem).getConverterTo(getLengthUnit(ousystem));
		dconv = getDensityUnit(iusystem).getConverterTo(getDensityUnit(ousystem));
		fconv = getFlowUnit(iusystem).getConverterTo(getFlowUnit(ousystem));
		sconv = getSpeedUnit(iusystem).getConverterTo(getSpeedUnit(ousystem));
	}

	private BigDecimal convert(BigDecimal value, javax.measure.converter.UnitConverter converter) {
		return null == value ? null : BigDecimal.valueOf(converter.convert(value.doubleValue()));
	}

	private BigDecimal convertLength(BigDecimal value) {
		return convert(value, lconv);
	}

	private BigDecimal convertDensity(BigDecimal value) {
		return convert(value, dconv);
	}

	private BigDecimal convertFlow(BigDecimal value) {
		return convert(value, fconv);
	}

	private BigDecimal convertSpeed(BigDecimal value) {
		return convert(value, sconv);
	}

	private void process() throws SiriusException {
		// settings: nothing to process
		process(scenario.getNetworkList());
		// signal list, sensor list: nothing to process
		process(scenario.getInitialDensitySet());
		// weaving factors, split ratios: nothing to process
		process(scenario.getDownstreamBoundaryCapacityProfileSet());
		process(scenario.getEventSet());
		process(scenario.getDemandProfileSet());
		process(scenario.getControllerSet());
		process(scenario.getFundamentalDiagramProfileSet());
		// network connections, destination networks, routes: nothing to process
	}

	private void process(NetworkList netlist) {
		if (null == netlist) return;
		for (Network network : netlist.getNetwork()) {
			// node list: nothing to process
			if (null != network.getLinkList())
				for (Link link : network.getLinkList().getLink()) {
					link.setLength(convertLength(link.getLength()));
				}
		}
	}

	private void process(InitialDensitySet idset) {
		if (null == idset) return;
		for (edu.berkeley.path.beats.jaxb.Density density : idset.getDensity()) {
			Data1D data1d = new Data1D(density.getContent(), ":");
			if (!data1d.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (BigDecimal val : data1d.getData()) {
					if (0 < sb.length()) sb.append(':');
					sb.append(convertDensity(val).toPlainString());
				}
				density.setContent(sb.toString());
			}
		}
	}

	private void process(DownstreamBoundaryCapacityProfileSet dbcpset) {
		if (null == dbcpset) return;
		for (CapacityProfile cp : dbcpset.getCapacityProfile()) {
			// TODO delimiter = ':' or ','?
			Data1D data1d = new Data1D(cp.getContent(), ",");
			if (!data1d.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (BigDecimal val : data1d.getData()) {
					if (0 < sb.length()) sb.append(',');
					sb.append(convertFlow(val).toPlainString());
				}
				cp.setContent(sb.toString());
			}
		}
	}

	private void process(EventSet eset) {
		if (null == eset) return;
		for (Event event : eset.getEvent())
			process(event.getParameters());
	}

	private void process(Parameters params) {
		if (null == params) return;
		for (Parameter param : params.getParameter())
			process(param);
	}

	private void process(Parameter param) {
		if (null == param.getName() || null == param.getValue()) return;
		final String name = param.getName();
		javax.measure.converter.UnitConverter converter = null;
		if (name.equals("capacity") || name.equals("capacity_drop") || name.endsWith("Flow"))
			converter = fconv;
		else if (name.equals("free_flow_speed") || name.equals("congestion_speed") || name.startsWith("gain"))
			converter = sconv;
		else if (name.equals("jam_density") || name.equals("targetDensity"))
			converter = dconv;
		// TODO check if all the parameters are processed correctly
		if (null != converter)
			param.setValue(convert(new BigDecimal(param.getValue()), converter).toPlainString());
	}

	private void process(Table table) {
		if (null == table) return;
		javax.measure.converter.UnitConverter conv[] = new javax.measure.converter.UnitConverter[table.getColumnNames().getColumnName().size()];
		int colnum = 0;
		for (ColumnName colname : table.getColumnNames().getColumnName()) {
			if (colname.getName().equals("MeteringRates") || colname.getName().equals("FlowThresholds"))
				conv[colnum] = fconv;
			else if (colname.getName().equals("SpeedThresholds"))
				conv[colnum] = sconv;
			else
				conv[colnum] = null;
			++colnum;
		}
		for (Row row : table.getRow()) {
			java.util.ListIterator<String> citer = row.getColumn().listIterator();
			for (colnum = 0; citer.hasNext(); ++colnum) {
				String value = citer.next();
				if (null != conv[colnum])
					citer.set(convert(new BigDecimal(value), conv[colnum]).toPlainString());
			}
		}
	}

	private void process(DemandProfileSet dpset) {
		if (null == dpset) return;
		for (DemandProfile dp : dpset.getDemandProfile())
			process(dp);
	}

	private void process(DemandProfile dp) {
		dp.setStdDevAdd(convertFlow(dp.getStdDevAdd()));

		Data2D data2d = new Data2D(dp.getContent(), new String[] {",", ":"});
		if (!data2d.isEmpty()) {
			BigDecimal[][] data = data2d.getData();
			StringBuilder sb = new StringBuilder();
			for (int t = 0; t < data.length; ++t) {
				if (0 < t) sb.append(',');
				for (int vtn = 0; vtn < data[t].length; ++vtn) {
					if (0 < vtn) sb.append(':');
					sb.append(convertFlow(data[t][vtn]).toPlainString());
				}
			}
			dp.setContent(sb.toString());
		}
	}

	private void process(ControllerSet cset) {
		if (null == cset) return;
		for (Controller controller : cset.getController()) {
			process(controller.getParameters());
			process(controller.getQueueController());
			for (Table table : controller.getTable())
				process(table);
		}
	}

	private void process(QueueController qcontroller) {
		if (null == qcontroller) return;
		process(qcontroller.getParameters());
	}

	private void process(FundamentalDiagramProfileSet fdpset) {
		if (null == fdpset) return;
		for (FundamentalDiagramProfile fdprofile : fdpset.getFundamentalDiagramProfile())
			for (FundamentalDiagram fd : fdprofile.getFundamentalDiagram())
				process(fd);
	}

	private void process(FundamentalDiagram fd) {
		fd.setFreeFlowSpeed(convertSpeed(fd.getFreeFlowSpeed()));
		fd.setCriticalSpeed(convertSpeed(fd.getCriticalSpeed()));
		fd.setCongestionSpeed(convertSpeed(fd.getCongestionSpeed()));
		fd.setCapacity(convertFlow(fd.getCapacity()));
		if (null != fd.getJamDensity())
			fd.setJamDensity(convertDensity(fd.getJamDensity()));
		if (null != fd.getCapacityDrop())
			fd.setCapacityDrop(convertFlow(fd.getCapacityDrop()));
		if (null != fd.getStdDevCapacity())
			fd.setStdDevCapacity(convertFlow(fd.getStdDevCapacity()));
		if (null != fd.getStdDevFreeFlowSpeed())
			fd.setStdDevFreeFlowSpeed(convertSpeed(fd.getStdDevFreeFlowSpeed()));
		if (null != fd.getStdDevCongestionSpeed())
			fd.setStdDevCongestionSpeed(convertSpeed(fd.getStdDevCongestionSpeed()));
	}

}
