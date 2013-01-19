package edu.berkeley.path.beats.util.scenario;

import edu.berkeley.path.beats.jaxb.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;

public class ScenarioSaver {

	/**
	 * Saves a scenario to a file
	 * @param scenario
	 * @param filename
	 * @param format file format: "XML" or "JSON" (case-insensitive)
	 * @throws SiriusException
	 */
	public static void save(Scenario scenario, String filename, String format) throws SiriusException {
		ScenarioSaverIF saver = null;
		if (null == format)
			throw new SiriusException("Format is NULL");
		else if ("XML".equals(format.toUpperCase()))
			saver = new XMLScenarioSaver(filename);
		else if ("JSON".equals(format.toUpperCase()))
			saver = new JSONScenarioSaver(filename);
		else
			throw new SiriusException("Unsupported format " + format);
		saver.save(scenario);
	}

	/**
	 * Saves a scenario to a file.
	 * The output format is derived from the filename extension
	 * @param scenario
	 * @param filename
	 * @throws SiriusException
	 */
	public static void save(Scenario scenario, String filename) throws SiriusException {
		save(scenario, filename, ScenarioLoader.getFormat(filename));
	}

	/**
	 * Saves a scenario in the database
	 * @param scenario
	 * @return the scenario ID in the database
	 * @throws SiriusException
	 */
	public static Long save(Scenario scenario) throws SiriusException {
		DBScenarioSaver saver = new DBScenarioSaver();
		saver.save(scenario);
		return saver.getID();
	}

}
