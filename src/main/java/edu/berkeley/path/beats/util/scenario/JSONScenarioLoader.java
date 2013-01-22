package edu.berkeley.path.beats.util.scenario;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;

class JSONScenarioLoader extends ScenarioLoaderBase implements ScenarioLoaderIF {

	private String filename;

	public JSONScenarioLoader(String filename) {
		this.filename = filename;
	}

	@Override
	public Scenario loadRaw() throws SiriusException {
		try {
			XMLStreamReader xmlsr = new MappedXMLStreamReader(new JSONObject(org.apache.commons.io.FileUtils.readFileToString(new File(filename))), JSONSettings.getConvention());
			return (edu.berkeley.path.beats.jaxb.Scenario) getUnmarshaller().unmarshal(xmlsr);
		} catch (JAXBException exc) {
			throw new SiriusException(exc);
		} catch (JSONException exc) {
			throw new SiriusException(exc);
		} catch (XMLStreamException exc) {
			throw new SiriusException(exc);
		} catch (IOException exc) {
			throw new SiriusException(exc);
		}
	}

}
