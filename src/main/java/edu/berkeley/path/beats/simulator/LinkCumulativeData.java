package edu.berkeley.path.beats.simulator;

/**
 * Link cumulative data storage
 */
final public class LinkCumulativeData {
	
	private edu.berkeley.path.beats.simulator.Link link;
	private int nensemble;
	private int nvehtype;
	private double[][] density;		// [veh]
	private double[][] iflow;		// [veh]
	private double[][] oflow;		// [veh]
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
	
	// densities ........................................................
	
	// average density over the output period for a given ensemble and vehicle type
	public double getMeanDensityInVeh(int ensemble, int vehtypenum) {
		return 0 == nsteps ? Double.NaN : density[ensemble][vehtypenum] / nsteps;
	}

	// average density[vehicle_type] over the output period for a given ensemble
	public double[] getMeanDensityInVeh(int ensemble) {
		return 0 == nsteps ? new double[nvehtype] : BeatsMath.times(density[ensemble], 1.0d / nsteps);
	}
	
	// average density over the output period for a given ensemble and all vehicle types
	public double getMeanTotalDensityInVeh(int ensemble) {
		return 0 == nsteps ? Double.NaN : sum(density[ensemble]) / nsteps;
	}

	// inflow ........................................................

	// inflow accumulated over the output period for a given ensemble and vehicle type
	public double getCumulativeInputFlowInVeh(int ensemble, int vehtypenum) {
		return iflow[ensemble][vehtypenum];
	}

	// inflow[vehicle_type] accumulated over the output period for a given ensemble
	public double[] getCumulativeInputFlowInveh(int ensemble) {
		return iflow[ensemble];
	}

	// total inflow accumulated over the output period for a given ensemble
	public double getCumulativeTotalInputFlowInVeh(int ensemble) {
		return sum(iflow[ensemble]);
	}

	// outflow ........................................................

	// outflow accumulated over the output period for a given ensemble and vehicle type
	public double getCumulativeOutputFlowInVeh(int ensemble, int vehtypenum) {
		return oflow[ensemble][vehtypenum];
	}

	// outflow[vehicle_type] accumulated over the output period for a given ensemble
	public double[] getCumulativeOutputFlowInVeh(int ensemble) {
		return oflow[ensemble];
	}
	
	// total outflow accumulated over the output period for a given ensemble
	public double getCumulativeTotalOutputFlowInVeh(int ensemble) {
		return sum(oflow[ensemble]);
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

