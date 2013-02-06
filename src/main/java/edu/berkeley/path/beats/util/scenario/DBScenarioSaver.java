package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.BeatsException;

class DBScenarioSaver implements ScenarioSaverIF {

	Long id = null;

	@Override
	public void save(Scenario scenario) throws BeatsException {
		id = edu.berkeley.path.beats.db.ScenarioImporter.doImport(scenario);
	}

	public Long getID() {
		return id;
	}

}
