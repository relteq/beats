package edu.berkeley.path.beats.util.scenario;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import edu.berkeley.path.beats.simulator.BeatsException;

abstract class ScenarioLoaderBase implements ScenarioLoaderIF {

	@Override
	public edu.berkeley.path.beats.simulator.Scenario load() throws BeatsException {
		edu.berkeley.path.beats.simulator.Scenario scenario = (edu.berkeley.path.beats.simulator.Scenario) loadRaw();
		edu.berkeley.path.beats.util.ScenarioUtil.checkSchemaVersion(scenario);
		return edu.berkeley.path.beats.simulator.ObjectFactory.process(scenario);
	}

	static JAXBContext getJAXBContext() throws JAXBException {
		return JAXBContext.newInstance(edu.berkeley.path.beats.jaxb.ObjectFactory.class);
	}

	protected static Object getJAXBObjectFactory() {
		return new edu.berkeley.path.beats.simulator.JaxbObjectFactory();
	}

	protected static Unmarshaller getUnmarshaller() throws JAXBException, BeatsException {
		Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
		unmarshaller.setSchema(edu.berkeley.path.beats.util.ScenarioUtil.getSchema());
		edu.berkeley.path.beats.simulator.ObjectFactory.setObjectFactory(unmarshaller, getJAXBObjectFactory());
		return unmarshaller;
	}

}
