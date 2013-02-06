package edu.berkeley.path.beats.util.polyline;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import edu.berkeley.path.beats.jaxb.ObjectFactory;
import edu.berkeley.path.beats.jaxb.Point;
import edu.berkeley.path.beats.simulator.BeatsException;

//** Decodes a path from a string */
public class GoogleDecoder extends DecoderBase implements DecoderIF {
	public GoogleDecoder() {}

	@Override
	public List<Point> decode(String str) throws BeatsException {
		if (null == factory) factory = new ObjectFactory();
		List<Point> result = new java.util.ArrayList<Point>();
		Vector<BigDecimal> coords = decodeSequence(str);
		if (0 != coords.size() % 2)
			throw new BeatsException("Not an even number of coordinates");
		for (int nc = 0; nc < coords.size(); ++nc) {
			if (0 == nc % 2) result.add(factory.createPoint());
			final int np = nc / 2; // point index
			Point prev = np > 0 ? result.get(np - 1) : null;
			if (0 == nc % 2)
				result.get(np).setLat(null == prev ? coords.get(nc) : coords.get(nc).add(prev.getLat()));
			else
				result.get(np).setLng(null == prev ? coords.get(nc) : coords.get(nc).add(prev.getLng()));
		}
		return result;
	}

	private Vector<BigDecimal> decodeSequence(String str) throws BeatsException {
		Vector<BigDecimal> coords = new Vector<BigDecimal>();
		int coord = 0;
		int shift = 0;
		for (int i = 0; i < str.length(); ++i) {
			// Inverse 10. Subtract 63
			int value = (int) str.charAt(i) - 63;
			final boolean is_last = 0 == (value & 0x20);
			// Inverse 6. Assemble the binary value from 5-bit chunks (starting from the right hand side)
			coord |= (value & 0x1F) << shift;
			if (is_last) {
				// From 2 and 5. If the original decimal value was negative, the last bit would be 1
				final boolean is_negative = 1 == (coord & 1);
				// Inverse 5. If the original decimal value is negative, invert this encoding
				if (is_negative) coord = ~coord;
				// Inverse 4. Right-shift the binary value one bit
				coord >>>= 1;
				if (is_negative) coord |= Integer.rotateRight(1, 1);
				// Inverse 2. Take the decimal value and divide it by 1e5
				coords.add(BigDecimal.valueOf(coord).divide(BigDecimal.valueOf((long) 1E5)));
				coord = 0;
				shift = 0;
			} else
				shift += 5;
		}
		if (0 != shift)
			throw new BeatsException("Incorect string format");
		return coords;
	}

	@Override
	public void reset() {}

}
