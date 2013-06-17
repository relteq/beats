package edu.berkeley.path.beats.simulator;

/**
 * Link cumulative data storage
 */
final public class LinkCumulativeData {
	
	private edu.berkeley.path.beats.simulator.Link link;
	private int nensemble;
	private int nvehtype;
	private Double[][] density;
	private Double[][] iflow;
	private Double[][] oflow;
	private int nsteps;

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	LinkCumulativeData(edu.berkeley.path.beats.simulator.Link link) {
		this.link = link;
		Scenario scenario = link.getMyNetwork().getMyScenario();
		nensemble = scenario.getNumEnsemble();
		nvehtype = scenario.getNumVehicleTypes();
		density = new Double[nensemble][nvehtype];
		iflow = new Double[nensemble][nvehtype];
		oflow = new Double[nensemble][nvehtype];
		reset();
	}

	/////////////////////////////////////////////////////////////////////
	// update / reset
	/////////////////////////////////////////////////////////////////////
	
	void update() throws BeatsException {
		for (int i = 0; i < nensemble; ++i)
			for (int j = 0; j < nvehtype; ++j) {
				density[i][j] += link.getDensity(i, j);
				iflow[i][j] += link.getInputFlow(i, j);
				oflow[i][j] += link.getOutputFlow(i, j);
			}
		++nsteps;
	}

	void reset() {
		reset(density);
		reset(iflow);
		reset(oflow);
		nsteps = 0;
	}

	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	public Double getMeanDensity(int ensemble, int vehtypenum) {
		return 0 == nsteps ? Double.NaN : density[ensemble][vehtypenum] / nsteps;
	}

	public Double getMeanTotalDensity(int ensemble) {
		return 0 == nsteps ? Double.NaN : sum(density[ensemble]) / nsteps;
	}

	public Double[] getMeanDensity(int ensemble) {
		if (0 == nsteps)
			return new Double[nvehtype];
		else
			return BeatsMath.times(density[ensemble], 1.0d / nsteps);
	}

	public Double getCumulativeInputFlow(int ensemble, int vehtypenum) {
		return iflow[ensemble][vehtypenum];
	}

	public Double[] getCumulativeInputFlow(int ensemble) {
		return iflow[ensemble];
	}

	public Double getCumulativeTotalInputFlow(int ensemble) {
		return sum(iflow[ensemble]);
	}

	public Double[] getMeanInputFlow(int ensemble) {
		if (0 == nsteps)
			return new Double[nvehtype];
		else
			return BeatsMath.times(iflow[ensemble], 1.0d / nsteps);
	}
	
	public Double getMeanInputFlow(int ensemble, int vt_ind) {
		return 0 == nsteps ? Double.NaN : iflow[ensemble][vt_ind] / nsteps;
	}

	public Double getCumulativeOutputFlow(int ensemble, int vehtypenum) {
		return oflow[ensemble][vehtypenum];
	}

	public Double getCumulativeTotalOutputFlow(int ensemble) {
		return sum(oflow[ensemble]);
	}

	public Double[] getCumulativeOutputFlow(int ensemble) {
		return oflow[ensemble];
	}

	public Double getMeanOutputFlow(int ensemble, int vt_ind) {
		return 0 == nsteps ? Double.NaN : oflow[ensemble][vt_ind] / nsteps;
	}

	public Double getMeanTotalOutputFlow(int ensemble) {
		return 0 == nsteps ? Double.NaN : sum(oflow[ensemble]) / nsteps;
	}

	public Double[] getMeanOutputFlow(int ensemble) {
		if (0 == nsteps)
			return new Double[nvehtype];
		else
			return BeatsMath.times(oflow[ensemble], 1.0d / nsteps);
	}

	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	private static void reset(Double[][] matrix) {
		for (int i = 0; i < matrix.length; ++i)
			for (int j = 0; j < matrix[i].length; ++j)
				matrix[i][j] = 0.0d;
	}

	private static Double sum(Double[] vector) {
		double sum = 0.0d;
		for (double val : vector)
			sum += val;
		return sum;
	}

}

