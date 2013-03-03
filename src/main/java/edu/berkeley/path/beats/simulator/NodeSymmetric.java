package edu.berkeley.path.beats.simulator;

import java.util.Set;

import edu.berkeley.path.beats.util.ArraySet;

/**
 * The node model proposed in
 * C.M.J. Tampere et al.
 * A generic class of first order node models
 * for dynamic macroscopic simulation of traffic flows.
 * Transportation Research Part B 45 (2011) 289Ð309
 */
public class NodeSymmetric extends Node {

	private double [][] demand; // [nIn][nOut] S_{ij}
	double [] capacity; // [ensemble][nIn]
	private double [][] flow; // [ensemble][nIn][nOut] q_{ij}

	NodeModel model;

	@Override
	protected void reset() {
		super.reset();
		demand = new double[nIn][nOut];
		capacity = new double[nIn];
		flow = new double[nIn][nOut];

		model = new NodeModel(nIn, nOut);
	}

	@Override
	protected void validate() {
		super.validate();
		if (!istrivialsplit) {
			for (int iind = 0; iind < nIn; ++iind)
				for (int oind = 0; oind < nOut; ++oind)
					for (int vt = 0; vt < myNetwork.myScenario.numVehicleTypes; ++vt)
						if (splitratio.get(iind, oind, vt).isNaN())
							BeatsErrorLog.addWarning("NaN split ratios: node " + getId() + ", " +
									"input link " + input_link[iind].getId() + ", " +
									"output link " + output_link[oind].getId() + ", " +
									"vehicle type '" + myNetwork.myScenario.getVehicleTypeNames()[vt] + "'");
		}
	}

	@Override
	protected void computeInOutFlows() {
		if (isTerminal) return;

		if (!istrivialsplit && hasSRprofile && !controlleron && !hasactivesplitevent)
			splitratio.copydata(sampledSRprofile);

		for (int ens = 0; ens < myNetwork.myScenario.numEnsemble; ++ens) {
			for (int i = 0; i < nIn; ++i) {
				capacity[i] = input_link[i].getCapacityInVeh(ens);
				for (int j = 0; j < nOut; ++j) {
					demand[i][j] = 0;
					for (int vt = 0; vt < myNetwork.myScenario.numVehicleTypes; ++vt) {
						if (1 < nOut) {
							// S_{ij} = \sum_{vt} S_i^{vt} * sr_{ij}^{vt}
							Double sr = splitratio.get(i, j, vt);
							if (!sr.isNaN())
								demand[i][j] += inDemand[ens][i][vt] * sr;
						} else
							demand[i][j] += inDemand[ens][i][vt];
					}
				}
			}

			model.solve(demand, outSupply[ens], capacity, flow);

			for (int j = 0; j < nOut; ++j)
				for (int vt = 0; vt < myNetwork.myScenario.numVehicleTypes; ++vt)
					outFlow[ens][j][vt] = .0;

			for (int i = 0; i < nIn; ++i) {
				// S_i = \sum_j S_{ij}
				double demand_i = 0;
				for (int j = 0; j < nOut; ++j)
					demand_i += demand[i][j];

				if (0 >= demand_i) {
					for (int vt = 0; vt < myNetwork.myScenario.numVehicleTypes; ++vt)
						inFlow[ens][i][vt] = .0;
				} else {
					// q_i = \sum_j q_{ij}
					double flow_i = 0;
					for (int j = 0; j < nOut; ++j)
						flow_i += flow[i][j];
					final double reduction = flow_i / demand_i;
					for (int vt = 0; vt < myNetwork.myScenario.numVehicleTypes; ++vt) {
						inFlow[ens][i][vt] = inDemand[ens][i][vt] * reduction;
						for (int j = 0; j < nOut; ++j)
							outFlow[ens][j][vt] += inFlow[ens][i][vt] * splitratio.get(i, j, vt);
					}
				}
			}
		}

	}

	public static class NodeModel {
		private int nIn;
		private int nOut;

		double [][] priority; // [nIn][nOut] C_{ij}
		double [] demand_i; // [nIn] S_i
		private double [] supply_residual; // [nOut] \tilde R_j(k)
		private Set<Integer> j_set; // [nOut] J(k)
		private Set<Integer> [] uj_set; // [nOut][nIn] U_j(k)
		private double [] a_coef; // [nOut] a_j(k)

		public NodeModel(int nIn, int nOut) {
			this.nIn = nIn;
			this.nOut = nOut;

			priority = new double[nIn][nOut];
			demand_i = new double[nIn];
			supply_residual = new double[nOut];
			j_set = new ArraySet(nOut);
			uj_set = new ArraySet[nOut];
			for (int oind = 0; oind < nOut; ++oind) uj_set[oind] = new ArraySet(nIn);
			a_coef = new double[nOut];
		}

		/**
		 * Solve the node model
		 * @param demand directed demand
		 * @param supply
		 * @param capacity incoming links' capacity
		 * @param flow an array to store the resulting flow
		 */
		public void solve(double [][] demand, double [] supply, double [] capacity, double [][] flow) {
			for (int i = 0; i < nIn; ++i) {
				for (int j = 0; j < nOut; ++j)
					flow[i][j] = 0;

				// S_i = \sum_j S_{ij}
				demand_i[i] = 0;
				for (int j = 0; j < nOut; ++j)
					demand_i[i] += demand[i][j];

				if (nOut == 1) priority[i][0] = capacity[i];
				else
					for (int j = 0; j < nOut; ++j)
						// C_{ij} = C_i * (S_{ij} / S_i)
						priority[i][j] = 0 >= demand_i[i] ? capacity[i] / nOut :
							capacity[i] * demand[i][j] / demand_i[i];
			}
			// initialization
			j_set.clear();
			for (int j = 0; j < nOut; ++j) {
				uj_set[j].clear();
				// \tilde R_j(0) = R_j
				supply_residual[j] = supply[j];
				double demand_j = 0; // S_j
				for (int i = 0; i < nIn; ++i) {
					// S_j = \sum_i S_{ij}
					demand_j += demand[i][j];
					// U_j(0) = {i: S_{ij} > 0}
					if (demand[i][j] > 0) uj_set[j].add(i);
				}
				// J(0) = {j: S_j > 0}
				if (demand_j > 0) j_set.add(j);
			}
			// main loop
			while (!j_set.isEmpty()) {
				int min_a_ind = -1; // \hat j
				double min_a_val = Double.MAX_VALUE; // a_{\hat j}(k)
				for (int j = 0; j < nOut; ++j) // j \in J(k)
					if (j_set.contains(j)) {
						double sum_priority = 0; // \sum_{i \in U_j(k)} C_{ij}
						for (int i = 0; i < nIn; ++i)
							if (uj_set[j].contains(i))
								sum_priority += priority[i][j];
						// a_j(k) = \tilde R_j(k) / \sum_{i \in U_j(k)} C_{ij}
						a_coef[j] = supply_residual[j] / sum_priority;
						if (a_coef[j] < min_a_val) {
							min_a_val = a_coef[j];
							min_a_ind = j;
						}
					}
				if (-1 == min_a_ind)
					// TODO revise the exception type
					throw new RuntimeException("Internal node model error: min a_j is undefined");
				boolean demand_constrained = false;
				for (int i = 0; i < nIn; ++i)
					// i \in U_{\hat j}(k)
					// S_i <= a_{\hat k}(k) C_i
					if (uj_set[min_a_ind].contains(i) &&
							demand_i[i] <= min_a_val * capacity[i]) {
						demand_constrained = true;
						for (int j = 0; j < nOut; ++j) {
							// q_{ij} = S_{ij} for all j
							flow[i][j] = demand[i][j];
							// for all j \in J(k)
							if (j_set.contains(j)) {
								// \tilde R_j(k + 1) = \tilde R_j(k) - S_{ij}
								supply_residual[j] -= demand[i][j];
								// U_j(k + 1) = U_j(k) \ {i}
								uj_set[j].remove(i);
							}
						}
					}
				if (demand_constrained) {
					for (int j = 0; j < nOut; ++j)
						if (j_set.contains(j) && uj_set[j].isEmpty())
							j_set.remove(j);
				} else {
					for (int i = 0; i < nIn; ++i)
						// for all i \in U_{\hat j}(k)
						if (uj_set[min_a_ind].contains(i)) {
							for (int j = 0; j < nOut; ++j) {
								// q_{ij} = a_{\hat j}(k) C_{ij} for all j
								flow[i][j] = min_a_val * priority[i][j];
								// for all j \in J(k)
								if (j_set.contains(j)) {
									// \tilde R_j(k + 1) = \tilde R_j(k) - a_{\hat j}(k) C_{ok}
									supply_residual[j] -= flow[i][j];
									// if j != \hat j(k)
									if (j != min_a_ind)
										// U_j(k + 1) = U_j(k) \ U_{\hat j}(k)
										uj_set[j].remove(i);
								}
							}
						}
					for (int j = 0; j < nOut; ++j)
						if (j != min_a_ind && j_set.contains(j) && uj_set[j].isEmpty())
							j_set.remove(j);
					j_set.remove(min_a_ind);
				}
			}
		}
	}

}
