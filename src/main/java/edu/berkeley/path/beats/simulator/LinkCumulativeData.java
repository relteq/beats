package edu.berkeley.path.beats.simulator;

/**
 * Link cumulative data storage
 */
final public class LinkCumulativeData {
	
	private edu.berkeley.path.beats.simulator.Link link;
	private int nensemble;
	private int nvehtype;
	private double[][] density;
	private double[][] iflow;
	private double[][] oflow;
	private int nsteps;

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	LinkCumulativeData(edu.berkeley.path.beats.simulator.Link link) {
		this.link = link;
		Scenario scenario = link.getMyNetwork().getMyScenario();
		nensemble = scenario.getNumEnsemble();
		nvehtype = scenario.getNumVehicleTypes();
		density = new double[nensemble][nvehtype];
		iflow = new double[nensemble][nvehtype];
		oflow = new double[nensemble][nvehtype];
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
	
	public double getMeanDensity(int ensemble, int vehtypenum) {
		return 0 == nsteps ? Double.NaN : density[ensemble][vehtypenum] / nsteps;
	}

	public double getMeanTotalDensity(int ensemble) {
		return 0 == nsteps ? Double.NaN : sum(density[ensemble]) / nsteps;
	}

	public double[] getMeanDensity(int ensemble) {
		if (0 == nsteps)
			return new double[nvehtype];
		return BeatsMath.times(density[ensemble], 1.0d / nsteps);
	}

	public double getCumulativeInputFlow(int ensemble, int vehtypenum) {
		return iflow[ensemble][vehtypenum];
	}

	public double[] getCumulativeInputFlow(int ensemble) {
		return iflow[ensemble];
	}

	public double getCumulativeTotalInputFlow(int ensemble) {
		return sum(iflow[ensemble]);
	}

	public double[] getMeanInputFlow(int ensemble) {
		if (0 == nsteps)
			return new double[nvehtype];
		return BeatsMath.times(iflow[ensemble], 1.0d / nsteps);
	}
	
	public double getMeanInputFlow(int ensemble, int vt_ind) {
		return 0 == nsteps ? Double.NaN : iflow[ensemble][vt_ind] / nsteps;
	}

	public double getCumulativeOutputFlow(int ensemble, int vehtypenum) {
		return oflow[ensemble][vehtypenum];
	}

	public double getCumulativeTotalOutputFlow(int ensemble) {
		return sum(oflow[ensemble]);
	}

	public double[] getCumulativeOutputFlow(int ensemble) {
		return oflow[ensemble];
	}

	public double getMeanOutputFlow(int ensemble, int vt_ind) {
		return 0 == nsteps ? Double.NaN : oflow[ensemble][vt_ind] / nsteps;
	}

	public double getMeanTotalOutputFlow(int ensemble) {
		return 0 == nsteps ? Double.NaN : sum(oflow[ensemble]) / nsteps;
	}

	public double[] getMeanOutputFlow(int ensemble) {
		if (0 == nsteps)
			return new double[nvehtype];
		return BeatsMath.times(oflow[ensemble], 1.0d / nsteps);
	}

	/////////////////////////////////////////////////////////////////////
	// private methods
	/////////////////////////////////////////////////////////////////////
	
	private static void reset(double[][] matrix) {
		for (int i = 0; i < matrix.length; ++i)
			for (int j = 0; j < matrix[i].length; ++j)
				matrix[i][j] = 0.0d;
	}

	private static double sum(double[] vector) {
		double sum = 0.0d;
		for (double val : vector)
			sum += val;
		return sum;
	}

}

