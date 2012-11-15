/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package edu.berkeley.path.beats.simulator.output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
			int numDN = scenario.numDenstinationNetworks;
			int numVT = scenario.numVehicleTypes;
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
		
		if(scenario==null)
			return;
		
		double invsteps;
		
		if(scenario.getCurrentTimeStep()==1)
			invsteps = 1f;
		else
			invsteps = 1f/((double)outsteps);
			
		try {
			int dn,vt;
			double [] numbers;
			out_time.write(String.format("%f\n",time));

/*
			for(dn=0;dn<scenario.numDenstinationNetworks;dn++){
				for(vt=0;vt<scenario.numVehicleTypes;vt++){
					numbers = SiriusMath.times(scenario.getCumulativeDensity(0,dn,vt),invsteps);
					out_density[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
*/
			for(edu.berkeley.path.beats.jaxb.Network network : scenario.getNetworkList().getNetwork()){
				List<edu.berkeley.path.beats.jaxb.Link> links = network.getLinkList().getLink();

				int n = links.size();
				Link link;
				for(int i=0;i<n-1;i++){
					link = (Link) links.get(i);
					numbers = SiriusMath.times(link.getCumulativeDensity(0),invsteps);
					out_density.write(SiriusFormatter.csv(numbers,":")+TextOutputWriter.delim);
					if(exportflows){
						numbers = SiriusMath.times(scenario.getCumulativeOutflow(0,dn,vt),invsteps);
						out_outflow[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
						numbers = SiriusMath.times(scenario.getCumulativeInflow(0,dn,vt),invsteps);
						out_inflow[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
					}
					link.resetCumulative();
				}
				
				link = (Link) links.get(n-1);
				numbers = SiriusMath.times(link.getCumulativeDensity(0),invsteps);
				out_density.write(SiriusFormatter.csv(numbers,":")+"\n");
				if(exportflows){
					numbers = SiriusMath.times(link.getCumulativeOutFlow(0),invsteps);
					out_outflow.write(SiriusFormatter.csv(numbers,":")+"\n");
					numbers = SiriusMath.times(link.getCumulativeInFlow(0),invsteps);
					out_inflow.write(SiriusFormatter.csv(numbers,":")+"\n");
				}
				link.resetCumulative();	
			}
			scenario.reset_cumulative();
			
		} catch (IOException e) {
			throw new SiriusException(e);
		}
	}

	public void close(){
		try {
			int dn,vt;
			
			if(out_time!=null)
				out_time.close();
			
			if(out_density!=null)
				for(dn=0;dn<scenario.numDenstinationNetworks;dn++)
					for(vt=0;vt<scenario.numVehicleTypes;vt++)
						if(out_density[dn][vt]!=null)
							out_density[dn][vt].close();
			
			if(out_outflow!=null)
				for(dn=0;dn<scenario.numDenstinationNetworks;dn++)
					for(vt=0;vt<scenario.numVehicleTypes;vt++)
						if(out_outflow[dn][vt]!=null)
							out_outflow[dn][vt].close();
			
			if(out_inflow!=null)
				for(dn=0;dn<scenario.numDenstinationNetworks;dn++)
					for(vt=0;vt<scenario.numVehicleTypes;vt++)
						if(out_inflow[dn][vt]!=null)
							out_inflow[dn][vt].close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
