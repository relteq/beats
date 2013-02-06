package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * Scenario saving interface.
 * Saves a scenario to a file or to a stream
 */
interface ScenarioSaverIF {

	public void save(edu.berkeley.path.beats.jaxb.Scenario scenario) throws BeatsException;

}
