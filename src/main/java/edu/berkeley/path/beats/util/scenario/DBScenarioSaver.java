package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;

class DBScenarioSaver implements ScenarioSaverIF {

	Long id = null;

	@Override
	public void save(Scenario scenario) throws SiriusException {
		id = edu.berkeley.path.beats.db.ScenarioImporter.doImport(scenario);
	}

	public Long getID() {
		return id;
	}

}
