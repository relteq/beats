package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * Scenario loader interface.
 * Loads a scenario form a file
 */
interface ScenarioLoaderIF {

	public edu.berkeley.path.beats.jaxb.Scenario loadRaw() throws BeatsException;

	public edu.berkeley.path.beats.simulator.Scenario load() throws BeatsException;

}
