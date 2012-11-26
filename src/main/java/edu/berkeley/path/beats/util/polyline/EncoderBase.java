package edu.berkeley.path.beats.util.polyline;

import edu.berkeley.path.beats.jaxb.Point;

public abstract class EncoderBase implements EncoderIF {

	/**
	 * Encodes a list of points
	 * @param pl the point list
	 */
	public void add(java.util.List<Point> pl) {
		for (Point point : pl)
			add(point);
	}

}
