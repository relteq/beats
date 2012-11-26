package edu.berkeley.path.beats.util.polyline;

import edu.berkeley.path.beats.jaxb.Point;

/**
 * Encodes a polyline
 */
public interface EncoderIF {

	/**
	 * Adds a point to the polyline
	 * @param point the point to add
	 */
	public void add(Point point);

	/**
	 * Finalizes the encoding
	 * @return the encoding result
	 */
	public String getResult();

	/**
	 * Resets the encoder to the initial (empty) state
	 */
	public void reset();

}
