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
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;
import edu.berkeley.path.beats.simulator.SiriusFormatter;
import edu.berkeley.path.beats.simulator.SiriusMath;

public final class TextOutputWriter extends OutputWriterBase {
	protected Writer out_time = null;
	protected Writer [][] out_density;
	protected Writer [][] out_outflow;
	protected Writer [][] out_inflow;
	protected static String delim = "\t";
	private String prefix;

	public TextOutputWriter(Scenario scenario, Properties props){
		super(scenario);
		if (null != props) prefix = props.getProperty("prefix");
		if (null == prefix) prefix = "output";
	}

	@Override
	public void open(int run_id) throws SiriusException {
		try {
			String suffix;
			int numDN = scenario.getNumDestinationNetworks();
			int numVT = scenario.getNumVehicleTypes();
			out_time = new OutputStreamWriter(new FileOutputStream(prefix+"_time"+String.format("_%d.txt",run_id)));
			
			out_density = new OutputStreamWriter[numDN][numVT];
			out_outflow = new OutputStreamWriter[numDN][numVT];
			out_inflow = new OutputStreamWriter[numDN][numVT];
			
			int i,j;
			String [] dnnames = scenario.getDestinationNetworkNames();
			String [] vtnames = scenario.getVehicleTypeNames();
			for(i=0;i<numDN;i++)
				for(j=0;j<numVT;j++){
					suffix = String.format("_%s_%s_%d.txt",dnnames[i],vtnames[j],run_id);
					out_density[i][j] = new OutputStreamWriter(new FileOutputStream(prefix+"_density"+suffix));
					out_outflow[i][j] = new OutputStreamWriter(new FileOutputStream(prefix+"_outflow"+suffix));
					out_inflow[i][j] = new OutputStreamWriter(new FileOutputStream(prefix+"_inflow"+suffix));
				}
		} catch (FileNotFoundException exc) {
			throw new SiriusException(exc);
		}
	}

	@Override
	public void recordstate(double time, boolean exportflows, int outsteps) throws SiriusException {
//		
//		if(scenario==null)
//			return;
//		
//		double invsteps;
//		
//		if(scenario.getCurrentTimeStep()==1)
//			invsteps = 1f;
//		else
//			invsteps = 1f/((double)outsteps);
//			
//		try {
//			int dn,vt;
//			double [] numbers;
//			out_time.write(String.format("%f\n",time));
//			for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()){
//				List<edu.berkeley.path.beats.jaxb.Link> links = network.getLinkList().getLink();
//
//				int n = links.size();
//				Link link;
//				for(int i=0;i<n-1;i++){
//					link = (Link) links.get(i);
//					numbers = SiriusMath.times(link.getCumulativeDensityPerVtInVeh(0),invsteps);
//					out_density.write(SiriusFormatter.csv(numbers,":")+TextOutputWriter.delim);
//					if(exportflows){
//						numbers = SiriusMath.times(scenario.getCumulativeOutflowInVeh(0,dn,vt),invsteps);
//						out_outflow[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
//						numbers = SiriusMath.times(scenario.getCumulativeInflowInVeh(0,dn,vt),invsteps);
//						out_inflow[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
//					}
//					link.resetCumulative();
//				}
//				
//				link = (Link) links.get(n-1);
//				numbers = SiriusMath.times(link.getCumulativeDensityPerVtInVeh(0),invsteps);
//				out_density.write(SiriusFormatter.csv(numbers,":")+"\n");
//				if(exportflows){
//					numbers = SiriusMath.times(link.getCumulativeOutFlowPerVtInVeh(0),invsteps);
//					out_outflow.write(SiriusFormatter.csv(numbers,":")+"\n");
//					numbers = SiriusMath.times(link.getCumulativeInFlowPerVtInVeh(0),invsteps);
//					out_inflow.write(SiriusFormatter.csv(numbers,":")+"\n");
//				}
//				link.resetCumulative();	
//			}
//			scenario.reset_cumulative();
//			
//		} catch (IOException e) {
//			throw new SiriusException(e);
//		}
	}

	public void close(){
		try {
			int dn,vt;
			
			if(out_time!=null)
				out_time.close();
			
			if(out_density!=null)
				for(dn=0;dn<scenario.getNumDestinationNetworks();dn++)
					for(vt=0;vt<scenario.getNumVehicleTypes();vt++)
						if(out_density[dn][vt]!=null)
							out_density[dn][vt].close();
			
			if(out_outflow!=null)
				for(dn=0;dn<scenario.getNumDestinationNetworks();dn++)
					for(vt=0;vt<scenario.getNumVehicleTypes();vt++)
						if(out_outflow[dn][vt]!=null)
							out_outflow[dn][vt].close();
			
			if(out_inflow!=null)
				for(dn=0;dn<scenario.getNumDestinationNetworks();dn++)
					for(vt=0;vt<scenario.getNumVehicleTypes();vt++)
						if(out_inflow[dn][vt]!=null)
							out_inflow[dn][vt].close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
