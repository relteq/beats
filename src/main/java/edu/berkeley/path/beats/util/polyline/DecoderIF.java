package edu.berkeley.path.beats.util.polyline;

import edu.berkeley.path.beats.jaxb.ObjectFactory;
import edu.berkeley.path.beats.jaxb.Point;
import edu.berkeley.path.beats.simulator.SiriusException;

/**
 * Decodes a polyline from a string
 */
public interface DecoderIF {

	/**
	 * Specifies an object factory to create points
	 * @param factory the object factory. If null, a new factory is created
	 */
	public void setObjectFactory(ObjectFactory factory);

	/**
	 * Restores a polyline
	 * @param encoded String
	 * @return the list of polyline points
	 */
	public java.util.List<Point> decode(String encoded) throws SiriusException;

	/**
	 * Restores the initial state.
	 * The factory is not reset
	 */
	public void reset();

}
