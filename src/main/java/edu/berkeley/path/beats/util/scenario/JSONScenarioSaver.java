package edu.berkeley.path.beats.util.scenario;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;

class JSONScenarioSaver extends ScenarioSaverBase implements ScenarioSaverIF {

	Writer writer;

	public JSONScenarioSaver(String filename) throws SiriusException {
		try {
			this.writer = new FileWriter(filename);
		} catch (IOException exc) {
			throw new SiriusException(exc);
		}
	}

	@Override
	public void save(Scenario scenario) throws SiriusException {
		ensureSchemaVersion(scenario);
		XMLStreamWriter xmlsw = new MappedXMLStreamWriter(JSONSettings.getConvention(), writer);
		try {
			getMarshaller().marshal(scenario, xmlsw);
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		}
	}

}
