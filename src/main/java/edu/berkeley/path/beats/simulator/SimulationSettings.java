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

package edu.berkeley.path.beats.simulator;

import java.math.BigDecimal;

/**
 * The simulation settings
 */
public class SimulationSettings {
	private Double startTime = null; // sec
	private Double duration = null; // sec
	private Double outputDt = null; // sec
	private Integer numRuns = null;

	private SimulationSettings parent = null;

	/**
	 * Constructor from default settings
	 * @param parent
	 */
	public SimulationSettings(SimulationSettings parent) {
		this.parent = parent;
	}

	/**
	 * Constructor from the given values
	 * @param startTime simulation start time, sec
	 * @param duration simulation duration, sec
	 * @param outputDt output sample rate, sec
	 * @param numRuns number of runs, sec
	 */
	public SimulationSettings(Double startTime, Double duration, Double outputDt, Integer numRuns) {
		this.startTime = startTime;
		this.duration = duration;
		this.outputDt = outputDt;
		this.numRuns = numRuns;
	}

	/**
	 * @param startTime the start time to set, sec
	 */
	public void setStartTime(Double startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * @param startTime decimal start time, sec
	 */
	public void setStartTime(BigDecimal startTime) {
		this.startTime = null == startTime ? null : startTime.doubleValue();
	}
	
	/**
	 * @param duration the duration to set, sec
	 */
	public void setDuration(Double duration) {
		this.duration = duration;
	}
	
	/**
	 * @param duration decimal duration, sec
	 */
	public void setDuration(BigDecimal duration) {
		this.duration = null == duration ? null : duration.doubleValue();
	}
	
	/**
	 * @param outputDt the output sample rate to set, sec
	 */
	public void setOutputDt(Double outputDt) {
		this.outputDt = outputDt;
	}
	
	/**
	 * @param outputDt decimal output sample rate, sec
	 */
	public void setOutputDt(BigDecimal outputDt) {
		this.outputDt = null == outputDt ? null : outputDt.doubleValue();
	}
	
	/**
	 * @param numRuns the number of repetitions to set
	 */
	public void setNumRuns(Integer numRuns) {
		this.numRuns = numRuns;
	}
	
	/**
	 * @param ss the parent simulation settings
	 */
	public void setParent(SimulationSettings ss) {
		this.parent = ss;
	}

	/**
	 * @return start time, sec
	 */
	public Double getStartTime() {
		if (null != startTime) return startTime;
		else if (null != parent) return parent.getStartTime();
		else return null;
	}

	/**
	 * @return duration, sec
	 */
	public Double getDuration() {
		if (null != duration) return duration;
		else if (null != parent) return parent.getDuration();
		else return null;
	}

	/**
	 * @return output sample rate, sec
	 */
	public Double getOutputDt() {
		if (null != outputDt) return outputDt;
		else if (null != parent) return parent.getOutputDt();
		else return null;
	}

	/**
	 * @return the number of runs
	 */
	public Integer getNumRuns() {
		if (null != numRuns) return numRuns;
		else if (null != parent) return parent.getNumRuns();
		else return null;
	}

	/**
	 * @return the parent simulation settings
	 */
	public SimulationSettings getParent() {
		return parent;
	}

	/**
	 * @return the simulation end time, sec
	 */
	public double getEndTime() {
		return getStartTime().doubleValue() + getDuration().doubleValue();
	}

	/**
	 * Rounds the double value, precision: .1
	 * @param val
	 * @return the "rounded" value
	 */
	private double round(double val) {
		return SiriusMath.round(val * 10.0) / 10.0;
	}

	/**
	 * Parses command line arguments
	 * @param args the arguments array
	 * @param index an index to start from
	 */
	public void parseArgs(String[] args, int index) {
		if (index < args.length) startTime = round(Double.parseDouble(args[index]));
		if (++index < args.length) duration = Double.parseDouble(args[index]);
		if (++index < args.length) outputDt = round(Double.parseDouble(args[index]));
		if (++index < args.length) numRuns = Integer.parseInt(args[index]);
	}

	public String toString() {
		return "start time: " + getStartTime() + " sec, " + //
			"duration: " + getDuration() + " sec, " + //
			"output sample rate: " + getOutputDt() + " sec, " + //
			"number of runs: " + getNumRuns();
	}

	/**
	 * @return the default simulation settings
	 */
	public static SimulationSettings defaults() {
		return new SimulationSettings(Double.valueOf(Defaults.TIME_INIT), Double.valueOf(Defaults.DURATION), Double.valueOf(Defaults.OUT_DT), 1);
	}

}
