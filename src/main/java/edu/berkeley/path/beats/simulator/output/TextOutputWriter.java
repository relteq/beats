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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.LinkCumulativeData;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;
import edu.berkeley.path.beats.simulator.SiriusFormatter;

public final class TextOutputWriter extends OutputWriterBase {
	protected Writer out_time = null;
	protected Writer out_density = null;
	protected Writer out_outflow = null;
	protected Writer out_inflow = null;
	protected static String delim = "\t";
	private String prefix;

	public TextOutputWriter(Scenario scenario, Properties props){
		super(scenario);
		if (null != props) prefix = props.getProperty("prefix");
		if (null == prefix) prefix = "output";
		scenario.requestLinkCumulatives();
	}

	@Override
	public void open(int run_id) throws SiriusException {
		String suffix = String.format("_%d.txt", run_id);
		try {
			out_time = new OutputStreamWriter(new FileOutputStream(prefix+"_time"+suffix));
			out_density = new OutputStreamWriter(new FileOutputStream(prefix+"_density"+suffix));
			out_outflow = new OutputStreamWriter(new FileOutputStream(prefix+"_outflow"+suffix));
			out_inflow = new OutputStreamWriter(new FileOutputStream(prefix+"_inflow"+suffix));
		} catch (FileNotFoundException exc) {
			throw new SiriusException(exc);
		}
	}

	@Override
	public void recordstate(double time, boolean exportflows, int outsteps) throws SiriusException {
		
		if(scenario==null)
			return;

		try {
			out_time.write(String.format("%f\n",time));
			for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()){
				List<edu.berkeley.path.beats.jaxb.Link> links = network.getLinkList().getLink();

				for (int i = 0; i < links.size(); ++i){
					Link link = (Link) links.get(i);
					LinkCumulativeData link_cum_data = scenario.getCumulatives(link);
					if (0 < i) out_density.write(TextOutputWriter.delim);
					out_density.write(SiriusFormatter.csv(exportflows ? link_cum_data.getMeanDensity(0) : link.getDensityInVeh(0), ":"));
					if(exportflows){
						if (0 < i) {
							out_outflow.write(TextOutputWriter.delim);
							out_inflow.write(TextOutputWriter.delim);
						}
						out_outflow.write(SiriusFormatter.csv(link_cum_data.getMeanOutputFlow(0), ":"));
						out_inflow.write(SiriusFormatter.csv(link_cum_data.getMeanInputFlow(0), ":"));
					}
				}

				out_density.write("\n");
				if (exportflows) {
					out_outflow.write("\n");
					out_inflow.write("\n");
				}
			}
			
		} catch (IOException e) {
			throw new SiriusException(e);
		}
	}

	public void close(){
		try {
			if(out_time!=null)
				out_time.close();
			if(out_density!=null)
				out_density.close();
			if(out_outflow!=null)
				out_outflow.close();
			if(out_inflow!=null)
				out_inflow.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
