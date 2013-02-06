package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.BeatsException;

class DBScenarioLoader implements ScenarioLoaderIF {

	private Long id;

	public DBScenarioLoader(Long id) {
		this.id = id;
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Scenario loadRaw() throws BeatsException {
		return edu.berkeley.path.beats.db.ScenarioExporter.doExport(id);
	}

	@Override
	public edu.berkeley.path.beats.simulator.Scenario load() throws BeatsException {
		return ObjectFactory.process((edu.berkeley.path.beats.simulator.Scenario) loadRaw());
	}

}
