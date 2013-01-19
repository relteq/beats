package edu.berkeley.path.beats.util.scenario;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;

class XMLScenarioLoader extends ScenarioLoaderBase implements ScenarioLoaderIF {

	private String filename;

	public XMLScenarioLoader(String filename) {
		this.filename = filename;
	}

	@Override
	public Scenario loadRaw() throws SiriusException {
		try {
			return (edu.berkeley.path.beats.jaxb.Scenario) getUnmarshaller().unmarshal(new FileInputStream(filename));
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		} catch (FileNotFoundException exc) {
			throw new SiriusException(exc);
		}
	}

}
