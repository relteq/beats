/*  Copyright (c) 2012, Relteq Systems, Inc. All rights reserved.
	This source is subject to the following copyright notice:
	http://relteq.com/COPYRIGHT_RelteqSystemsInc.txt
*/

package com.relteq.sirius.simulator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

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
		
		if(scenario.clock.getCurrentstep()==1)
			invsteps = 1f;
		else
			invsteps = 1f/((double)outsteps);
			
		try {
			int dn,vt;
			double [] numbers;
			out_time.write(String.format("%f\n",time));
			for(dn=0;dn<scenario.numDenstinationNetworks;dn++){
				for(vt=0;vt<scenario.numVehicleTypes;vt++){
					numbers = SiriusMath.times(scenario.getCumulativeDensity(0,dn,vt),invsteps);
					out_density[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
					if(exportflows){
						numbers = SiriusMath.times(scenario.getCumulativeOutflow(0,dn,vt),invsteps);
						out_outflow[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
						numbers = SiriusMath.times(scenario.getCumulativeInflow(0,dn,vt),invsteps);
						out_inflow[dn][vt].write(SiriusFormatter.csv(numbers,TextOutputWriter.delim)+"\n");
					}
				}
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
