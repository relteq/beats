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

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/** Class for holding five minute flow and speed data associated with a VDS.
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class FiveMinuteData {

	protected boolean isaggregate;	// true if object holds only averages over all lanes
	protected int vds;
	protected int lanes;
	protected ArrayList<Long> time = new ArrayList<Long>();
	protected ArrayList<ArrayList<Float>> flw = new ArrayList<ArrayList<Float>>();		// [veh/sec/lane]
	protected ArrayList<ArrayList<Float>> spd = new ArrayList<ArrayList<Float>>();		// [meters/sec]

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public FiveMinuteData(int vds,boolean isaggregate) {
		this.vds=vds;
		this.isaggregate = isaggregate;
	}
	
	/////////////////////////////////////////////////////////////////////
	// getters
	/////////////////////////////////////////////////////////////////////
	
	public int getNumDataPoints(){
		return time.size();
	}

	public ArrayList<Long> getTime(){
		return time;
	}
	
	/** get aggregate flow value in [veh/sec/lane]
	 * @param time index
	 * @return a float, or <code>NaN</code> if something goes wrong.
	 * */
	public float getAggFlwInVPSPL(int i){
		try{
			if(isaggregate)
				return flw.get(0).get(i);
			else
				return Float.NaN;
		}
		catch(Exception e){
			return Float.NaN;
		}
	}
	
	public float getAggFlwInVPS(int i){
		return getAggFlwInVPSPL(i)*lanes;
	}

	/** get aggregate speed value in [m/s]
	 * @param time index
	 * @return a float, or <code>NaN</code> if something goes wrong.
	 * */
	public float getAggSpd(int i){
		try{
			if(isaggregate)
				return spd.get(0).get(i);
			else
				return Float.NaN;
		}
		catch(Exception e){
			return Float.NaN;
		}
	}	

	/** get aggregate density value in [veh/meter/lane]
	 * @param time index
	 * @return a float, or <code>NaN</code> if something goes wrong.
	 * */
	public float getAggDtyInVPMPL(int i){
		try{
			if(isaggregate)
				return flw.get(0).get(i)/spd.get(0).get(i);
			else
				return Float.NaN;
		}
		catch(Exception e){
			return Float.NaN;
		}
	}
	
	public float getAggDtyInVPM(int i){
		return getAggDtyInVPMPL(i)*lanes;
	}
	
	public int getLanes(){
		return lanes;
	}

	/////////////////////////////////////////////////////////////////////
	// putters
	/////////////////////////////////////////////////////////////////////
	
	/** add aggregate flow value in [veh/sec/lane]
	 * @param value of flow
	 * */
	protected void addAggFlwInVPSPL(float val){
		if(flw.isEmpty())
			flw.add(new ArrayList<Float>());
		if(isaggregate)
			flw.get(0).add(val);
	}
	
	/** add aggregate speed value in [m/s]
	 * @param value of speed
	 * */
	protected void addAggSpd(float val){
		if(spd.isEmpty())
			spd.add(new ArrayList<Float>());
		if(isaggregate)
			spd.get(0).add(val);
	}	
	
	protected void setLanes(int lanes){
		this.lanes = lanes;
	}
	
	/** add array of per lane flow values in [veh/sec/lane]
	 * @param array of flow values.
	 * @param index to begining of sub-array.
	 * @param index to end of sub-array.
	 * */
	protected void addPerLaneFlw(ArrayList<Float> row,int start,int end){
		ArrayList<Float> x = new ArrayList<Float>();
		for(int i=start;i<end;i++)
			x.add(row.get(i));
		flw.add(x);
	}

	/** add array of per lane speed values in [m/s]
	 * @param array of speed values.
	 * @param index to begining of sub-array.
	 * @param index to end of sub-array.
	 * */
	protected void addPerLaneSpd(ArrayList<Float> row,int start,int end){
		ArrayList<Float> x = new ArrayList<Float>();
		for(int i=start;i<end;i++)
			x.add(row.get(i));
		spd.add(x);
	}
	
	/////////////////////////////////////////////////////////////////////
	// file I/O
	/////////////////////////////////////////////////////////////////////
	
	/** Write aggregate values to a text file.
	 * @param File name.
	 * */
	public void writeAggregateToFile(String filename) throws Exception{
		Writer out = new OutputStreamWriter(new FileOutputStream(filename+"_"+vds+".txt"));
		for(int i=0;i<time.size();i++)
			out.write(time.get(i)+"\t"+getAggFlwInVPSPL(i)+"\t"+getAggDtyInVPMPL(i)+"\t"+getAggSpd(i)+"\n");
		out.close();
	}

}
