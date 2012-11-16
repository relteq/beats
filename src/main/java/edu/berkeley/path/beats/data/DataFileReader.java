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

package edu.berkeley.path.beats.data;

import java.net.*;
import java.io.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.berkeley.path.beats.sensor.DataSource;
import edu.berkeley.path.beats.simulator.SiriusException;

/** Use the Read5minData() method of this class to read five-minute data from data sources.
* @author Gabriel Gomes
* @version VERSION NUMBER
*/
public class DataFileReader {

	private ColumnFormat PeMSDataClearingHouse = new ColumnFormat(",",5,8,9,10,8,false,"US");
	private ColumnFormat CaltransDbx 		   = new ColumnFormat("\t",6,20,22,23,8,true,"US");

	/////////////////////////////////////////////////////////////////////
	// public methods
	/////////////////////////////////////////////////////////////////////
	
    public void Read5minData(HashMap <Integer,FiveMinuteData> data,ArrayList<DataSource> datasources) throws SiriusException {

    	// step through data file
    	int count = 0;
    	for(DataSource datasource : datasources){
    		
    		System.out.println(count);
    		DataSource.Format dataformat = datasource.getFormat();
    		count++;
    		switch(dataformat){
    		case PeMSDataClearinghouse:
    			ReadDataFile(data,datasource,PeMSDataClearingHouse);
    			break;
    		case CaltransDBX:
    			ReadDataFile(data,datasource,CaltransDbx);
    			break;
//    		case BHL:
//    			ReadDataSource(data,datasource,BHL);
//    			break;
			default:
				break;
    		}
    	}
    	 
    }
	       
	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
    
    private static Date ConvertTime(final String timestr) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        ParsePosition pp = new ParsePosition(0);
        return formatter.parse(timestr,pp);
    }

    private static void ReadDataFile(HashMap <Integer,FiveMinuteData> data,DataSource datasource, ColumnFormat format) throws SiriusException {
		int lane;
    	String line,str;
    	int indexof;
        Calendar calendar = Calendar.getInstance();
    	float totalflw,totalspd;
    	float val;
    	long time;
    	int actuallanes;
    	boolean hasflw,hasspd;

    	javax.measure.converter.UnitConverter flowConverter = edu.berkeley.path.beats.util.UnitConverter.getFlowConverter(format.units);
    	javax.measure.converter.UnitConverter speedConverter = edu.berkeley.path.beats.util.UnitConverter.getSpeedConverter(format.units);

    	try{
			URL url = new URL(datasource.getUrl());
			URLConnection uc = url.openConnection();
			BufferedReader fin = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			try {
				if(format.hasheader)
					line=fin.readLine(); 	// discard the header
		    	while ((line=fin.readLine()) != null) {
		            String f[] = line.split(format.delimiter,-1);
		            int vds = Integer.parseInt(f[1]);
		            indexof = datasource.getFor_vds().indexOf(vds);
		       
		            if(indexof<0)
		            	continue;
		            
		    		calendar.setTime(ConvertTime(f[0]));
		    		time = calendar.getTime().getTime()/1000;
		    		
		        	ArrayList<Float> laneflw = new ArrayList<Float>();
		        	ArrayList<Float> lanespd = new ArrayList<Float>();
		        
		        	// store in lane-wise ArrayList
		        	actuallanes = 0;
		            totalflw = 0;
		            totalspd = 0;
		            int index;
		            for (lane=0;lane<format.maxlanes;lane++) {
		            	
		            	index = format.laneblocksize*(lane+1)+format.flwoffset;
		            	str = f[index];
		            	hasflw = !str.isEmpty();
		            	if(hasflw){
		            		val = (float) flowConverter.convert(Float.parseFloat(str)*12f);
		            		laneflw.add(val);
		            		totalflw += val;
		            	}
		            	else
		                	laneflw.add(Float.NaN); 
		            	
		            	index = format.laneblocksize*(lane+1)+format.spdoffset;
		            	str = f[index];
		            	hasspd = !str.isEmpty();
		            	if(hasspd){
		            		val = (float) speedConverter.convert(Float.parseFloat(str));
		            		lanespd.add(val);
		            		totalspd += val;
		            	}
		            	else
		            		lanespd.add(Float.NaN); 
		            	if(hasflw || hasspd) // || hasocc
		            		actuallanes++;
		            }
		
		            // find the data structure and store. 
		            FiveMinuteData D = data.get(vds);
		            D.setLanes(actuallanes);
		            if(D.isaggregate && actuallanes>0){
		                totalspd /= actuallanes;
		                totalflw /= actuallanes;
		                D.addAggFlwInVPSPL(totalflw);
		                D.addAggSpd(totalspd);
		                D.time.add(time);	
		            }
		            else{
			            D.addPerLaneFlw(laneflw,0,actuallanes);
			            D.addPerLaneSpd(lanespd,0,actuallanes);
			            D.time.add(time);
		            }
		        } 
			}
			finally{
				if(fin!=null)
					fin.close();
			}
    	}
		catch(Exception e){
			throw new SiriusException(e);
		}
    }
    
    private class ColumnFormat {
    	public int laneblocksize;
    	public int flwoffset;
    	//public int occoffset;
    	public int spdoffset;
    	public int maxlanes;
    	public boolean hasheader;
    	public String delimiter;
    	public String units;

    	public ColumnFormat(String delimiter, int laneblocksize, int flwoffset, int occoffset, int spdoffset, int maxlanes, boolean hasheader, String units) {
    		super();
    		this.delimiter = delimiter;	
    		this.laneblocksize = laneblocksize;
    		this.flwoffset = flwoffset;
    		//this.occoffset = occoffset;
    		this.spdoffset = spdoffset;
    		this.maxlanes = maxlanes;
    		this.hasheader = hasheader;
    		this.units = units;
    	}
    }

}