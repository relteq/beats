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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.*;

import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.LinkCumulativeData;
import edu.berkeley.path.beats.simulator.Network;
import edu.berkeley.path.beats.simulator.Node;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.Signal;
import edu.berkeley.path.beats.simulator.SiriusErrorLog;
import edu.berkeley.path.beats.simulator.SiriusException;

public final class XMLOutputWriter extends OutputWriterBase {
	protected XMLStreamWriter xmlsw = null;
	protected static final String SEC_FORMAT = "%.1f";
	protected static final String NUM_FORMAT = "%.4f";
	private String prefix;

	private Formatter dens_formatter; // density formatter
	private Formatter flow_formatter; // flow formatter
	private Formatter speed_formatter; // speed formatter
	private Formatter sr_formatter; // split ratio formatter

	private Marshaller marshaller;

	public XMLOutputWriter(Scenario scenario, Properties props) throws SiriusException {
		super(scenario);
		if (null != props) prefix = props.getProperty("prefix");
		if (null == prefix) prefix = "output";
		final String delim = ":";
		dens_formatter = new Formatter(delim, NUM_FORMAT);
		flow_formatter = new Formatter(delim, NUM_FORMAT);
		speed_formatter = new Formatter(delim, NUM_FORMAT);
		sr_formatter = new Formatter(delim, NUM_FORMAT);

		try {
			marshaller = JAXBContext.newInstance(edu.berkeley.path.beats.jaxb.ObjectFactory.class).createMarshaller();
			marshaller.setSchema(edu.berkeley.path.beats.util.ScenarioUtil.getOutputSchema());
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		}

		scenario.requestLinkCumulatives();
		scenario.requestSignalPhases();
	}

	@Override
	public void open(int run_id) throws SiriusException {
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		try {
			xmlsw = xmlof.createXMLStreamWriter(new FileOutputStream(prefix + "_" + String.format("%d", run_id) + ".xml"), "utf-8");
			xmlsw.writeStartDocument("utf-8", "1.0");
			xmlsw.writeStartElement("scenario_output");
			xmlsw.writeAttribute("schemaVersion", "XXX");
			// scenario
			marshaller.marshal(scenario, xmlsw);
			// report
			xmlsw.writeStartElement("report");
			marshaller.marshal(scenario.getSettings(), xmlsw);
			xmlsw.writeStartElement("link_report");
			xmlsw.writeAttribute("density_report", "true");
			xmlsw.writeAttribute("flow_report", "true");
			xmlsw.writeEndElement(); // link_report
			xmlsw.writeStartElement("node_report");
			xmlsw.writeAttribute("srm_report", "true");
			xmlsw.writeEndElement(); // node_report
			xmlsw.writeStartElement("signal_report");
			xmlsw.writeAttribute("cycle_report", "true");
			xmlsw.writeEndElement(); // signal_report
			xmlsw.writeEndElement(); // report
			// data
			xmlsw.writeStartElement("data");
		} catch (XMLStreamException exc) {
			SiriusErrorLog.addError(exc.toString());
		} catch (FileNotFoundException exc) {
			throw new SiriusException(exc);
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		}
	}

	@Override
	public void recordstate(double time, boolean exportflows, int outsteps) throws SiriusException {
		boolean firststep = 0 == scenario.getCurrentTimeStep();
		String dt = String.format(SEC_FORMAT, firststep ? .0d : scenario.getSimDtInSeconds() * outsteps);
		try {
			xmlsw.writeStartElement("ts");
			xmlsw.writeAttribute("sec", String.format(SEC_FORMAT, time));
			xmlsw.writeStartElement("netl");
			for (edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()) {
				xmlsw.writeStartElement("net");
				xmlsw.writeAttribute("id", network.getId());
				// dt = time interval of reporting, sec
				xmlsw.writeAttribute("dt", dt);
				// link list
				xmlsw.writeStartElement("ll");
				for (edu.berkeley.path.beats.jaxb.Link link : network.getLinkList().getLink()) {
					xmlsw.writeStartElement("l");
					xmlsw.writeAttribute("id", link.getId());
					Link _link = (Link) link;
					LinkCumulativeData link_cum_data = scenario.getCumulatives(link);
					// d = average number of vehicles during the interval of reporting dt
					xmlsw.writeAttribute("d", dens_formatter.format(exportflows ? link_cum_data.getMeanDensity(0) : _link.getDensityInVeh(0)));
					if (exportflows) {
						// f = flow per dt, vehicles
						xmlsw.writeAttribute("f", flow_formatter.format(link_cum_data.getCumulativeOutputFlow(0)));
						// computing speed
						speed_formatter.clear();
						double ffspeed = _link.getVfInMPS(0);
						double mean_total_density = link_cum_data.getMeanTotalDensity(0);
						if (0 >= mean_total_density) {
							if (!Double.isNaN(ffspeed))
								for (int vt_ind = 0; vt_ind < scenario.getNumVehicleTypes(); ++vt_ind)
									speed_formatter.add(ffspeed);
						} else {
							double mean_speed = link_cum_data.getMeanTotalOutputFlow(0) * _link.getLengthInMeters() / (mean_total_density * scenario.getSimDtInSeconds());
							if (!Double.isNaN(ffspeed) && ffspeed < mean_speed) mean_speed = ffspeed;
							for (int vt_ind = 0; vt_ind < scenario.getNumVehicleTypes(); ++vt_ind) {
								double density = link_cum_data.getMeanDensity(0, vt_ind);
								double speed = mean_speed;
								if (0 < density) {
									speed = link_cum_data.getMeanOutputFlow(0, vt_ind) * _link.getLengthInMeters() / (density * scenario.getSimDtInSeconds());
									if (!Double.isNaN(ffspeed) && speed > ffspeed) speed = ffspeed;
								}
								speed_formatter.add(speed);
							}
						}
						// v = speed, m/s
						if (!speed_formatter.isEmpty()) xmlsw.writeAttribute("v", speed_formatter.getResult());
					}
					// mf = capacity, vehicles per second
					double mf = _link.getCapacityInVPS(0);
					if (!Double.isNaN(mf)) xmlsw.writeAttribute("mf", String.format(NUM_FORMAT, mf));
					// fv = free flow speed, meters per second
					double fv = _link.getVfInMPS(0);
					if (!Double.isNaN(fv)) xmlsw.writeAttribute("fv", String.format(NUM_FORMAT, fv));
					xmlsw.writeEndElement(); // l
				}
				xmlsw.writeEndElement(); // ll
				// node list
				xmlsw.writeStartElement("nl");
				for (edu.berkeley.path.beats.jaxb.Node node : network.getNodeList().getNode()) {
					xmlsw.writeStartElement("n");
					xmlsw.writeAttribute("id", node.getId());
					Node _node = (Node) node;
					for (int ili = 0; ili < _node.getnIn(); ++ili)
						for (int oli = 0; oli < _node.getnOut(); ++oli) {
							xmlsw.writeStartElement("io");
							xmlsw.writeAttribute("il", _node.getInput_link()[ili].getId());
							xmlsw.writeAttribute("ol", _node.getOutput_link()[oli].getId());
							sr_formatter.clear();
							for (int vti = 0; vti < scenario.getNumVehicleTypes(); ++vti)
								sr_formatter.add(_node.getSplitRatio(ili, oli, vti));
							xmlsw.writeAttribute("r", sr_formatter.getResult());
							xmlsw.writeEndElement(); // io
						}
					xmlsw.writeEndElement(); // n
				}
				xmlsw.writeEndElement(); // nl
				// signal list
				List<edu.berkeley.path.beats.jaxb.Signal> sigl = ((Network) network).getListOfSignals();
				if (null != sigl && 0 < sigl.size()) {
					xmlsw.writeStartElement("sigl");
					for (edu.berkeley.path.beats.jaxb.Signal signal : sigl) {
						xmlsw.writeStartElement("sig");
						xmlsw.writeAttribute("id", signal.getId());
						List<Signal.PhaseData> phdata = scenario.getCompletedPhases(signal).getPhaseList();
						for (Signal.PhaseData ph : phdata) {
							xmlsw.writeStartElement("ph");
							xmlsw.writeAttribute("i", String.format("%d", ph.nema.ordinal()));
							xmlsw.writeAttribute("b", String.format(SEC_FORMAT, ph.starttime));
							xmlsw.writeAttribute("g", String.format(SEC_FORMAT, ph.greentime));
							xmlsw.writeEndElement(); // ph
						}
						xmlsw.writeEndElement(); // sig
					}
					xmlsw.writeEndElement(); // sigl
				}
				xmlsw.writeEndElement(); // net
			}
			xmlsw.writeEndElement(); // netl
			xmlsw.writeEndElement(); // ts
		} catch (XMLStreamException exc) {
			exc.printStackTrace();
		}
	}

	@Override
	public void close(){
		try {
			xmlsw.writeEndElement(); // data
			xmlsw.writeEndElement(); // scenario_output
			xmlsw.writeEndDocument();
			xmlsw.close();
		} catch (XMLStreamException exc) {
			SiriusErrorLog.addError(exc.toString());
		}
	}

	protected static class Formatter {
		private String delim;
		private String format;
		private StringBuilder sb;

		public Formatter(String delim, String format) {
			this.delim = delim;
			this.format = format;
			this.sb = new StringBuilder();
		}

		public void clear() {
			sb.setLength(0);
		}

		public void add(Double val) {
			if (0 < sb.length()) sb.append(delim);
			sb.append(String.format(format, val));
		}

		public String getResult() {
			return sb.toString();
		}

		public String format(Double[] vector) {
			clear();
			for (Double val : vector)
				add(val);
			return getResult();
		}

		public boolean isEmpty() {
			return 0 == sb.length();
		}
	}

}
