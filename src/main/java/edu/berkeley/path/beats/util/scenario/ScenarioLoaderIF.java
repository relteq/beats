package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.simulator.SiriusException;

/**
 * Scenario loader interface.
 * Loads a scenario form a file
 */
interface ScenarioLoaderIF {

	public edu.berkeley.path.beats.jaxb.Scenario loadRaw() throws SiriusException;

	public edu.berkeley.path.beats.simulator.Scenario load() throws SiriusException;

}
