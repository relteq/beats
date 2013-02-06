package edu.berkeley.path.beats.util.scenario;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.BeatsException;

class XMLScenarioLoader extends ScenarioLoaderBase implements ScenarioLoaderIF {

	private String filename;

	public XMLScenarioLoader(String filename) {
		this.filename = filename;
	}

	@Override
	public Scenario loadRaw() throws BeatsException {
		try {
			return (edu.berkeley.path.beats.jaxb.Scenario) getUnmarshaller().unmarshal(new FileInputStream(filename));
		} catch (JAXBException exc) {
			throw new BeatsException(exc);
		} catch (FileNotFoundException exc) {
			throw new BeatsException(exc);
		}
	}

}
