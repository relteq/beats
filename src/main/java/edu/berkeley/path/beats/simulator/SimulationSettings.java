package edu.berkeley.path.beats.simulator;

import java.math.BigDecimal;

/**
 * The simulation settings
 */
final class SimulationSettings {
	
	public static enum ModeType {  normal, warmupFromZero , warmupFromIC };
	public static enum UncertaintyType { uniform, gaussian }

	private String configfilename;
	private String outputfileprefix;
	private String output_format;
	private Double startTime = null; // sec
	private Double duration = null; // sec
	private Double outputDt = null; // sec
	private Integer numReps = null;
	private UncertaintyType uncertaintyType;

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
	 * @param numReps number of runs, sec
	 */
	public SimulationSettings(String outputfileprefix,String output_format,Double startTime, Double duration, Double outputDt, Integer numReps) {
		this.outputfileprefix = outputfileprefix;
		this.output_format = output_format;		
		this.startTime = startTime;
		this.duration = duration;
		this.outputDt = outputDt;
		this.numReps = numReps;
	}

	
	public void setConfigfilename(String configfilename) {
		this.configfilename = configfilename;
	}

	public void setOutputfileprefix(String outputfileprefix) {
		this.outputfileprefix = outputfileprefix;
	}

	public void setOutput_format(String output_format) {
		this.output_format = output_format;
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
	 * @param numReps the number of repetitions to set
	 */
	public void setNumReps(Integer numReps) {
		this.numReps = numReps;
	}
	
	/**
	 * @param ss the parent simulation settings
	 */
	public void setParent(SimulationSettings ss) {
		this.parent = ss;
	}

	
	public String getConfigfilename() {
		if (null != configfilename) return configfilename;
		else if (null != parent) return parent.getConfigfilename();
		else return null;
	}
	

	public String getOutputfileprefix() {
		if (null != outputfileprefix) return outputfileprefix;
		else if (null != parent) return parent.getOutputfileprefix();
		else return null;
	}
	

	public String getOutput_format() {
		if (null != output_format) return output_format;
		else if (null != parent) return parent.getOutput_format();
		else return null;
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
	public Integer getNumReps() {
		if (null != numReps) return numReps;
		else if (null != parent) return parent.getNumReps();
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
		return BeatsMath.round(val * 10.0) / 10.0;
	}

	/**
	 * Parses command line arguments
	 * @param args the arguments array
	 * @param index an index to start from
	 */
	public void parseArgs(String[] args, int index) {
		if (index < args.length)
			this.configfilename = args[index];
		if (++index < args.length)
			this.outputfileprefix = args[index];
		if (++index < args.length)
			this.output_format = args[index];
		if (++index < args.length) 
			startTime = round(Double.parseDouble(args[index]));
		if (++index < args.length)
			duration = Double.parseDouble(args[index]);
		if (++index < args.length) 
			outputDt = round(Double.parseDouble(args[index]));
		if (++index < args.length) 
			numReps = Integer.parseInt(args[index]);
	}

	public String toString() {
		return "start time: " + getStartTime() + " sec, " + //
			"duration: " + getDuration() + " sec, " + //
			"output sample rate: " + getOutputDt() + " sec, " + //
			"number of runs: " + getNumReps();
	}

	/**
	 * @return the default simulation settings
	 */
	public static SimulationSettings defaults() {
		return new SimulationSettings("outputs", "xml", Double.valueOf(Defaults.TIME_INIT), Double.valueOf(Defaults.DURATION), Double.valueOf(Defaults.OUT_DT), 1);
	}

}
