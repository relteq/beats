package edu.berkeley.path.beats.util.polyline;

import java.math.BigDecimal;
import java.util.Vector;

import edu.berkeley.path.beats.jaxb.Point;

/** Converts a path into a string */

public class GoogleEncoder extends EncoderBase implements EncoderIF {
	StringBuilder sb;
	Point prev = null;

	public GoogleEncoder() {
		sb = new StringBuilder();
	}

	@Override
	public void add(Point point) {
		encode(point.getLat(), null == prev ? null : prev.getLat());
		encode(point.getLng(), null == prev ? null : prev.getLng());
		prev = point;
	}

	public String getResult() {
		return sb.toString();
	}

	private void encode(BigDecimal curr, BigDecimal prev) {
		curr = round(curr);
		if (null != prev) curr = curr.subtract(round(prev));
		encode(curr);
	}

	private BigDecimal round(BigDecimal value) {
		return value.setScale(5, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * See https://developers.google.com/maps/documentation/utilities/polylinealgorithm
	 * @param coord the value to encode
	 */
	private void encode(BigDecimal coord) {
		// 1. Take the initial signed value
		// 2. Take the decimal value and multiply it by 1e5, rounding the result
		// 3. Convert the decimal value to binary.
		//    Note that a negative value must be calculated using its two's complement
		//    by inverting the binary value and adding one to the result
		int value = coord.multiply(BigDecimal.valueOf((long) 1E5)).intValueExact();
		boolean is_negative = value < 0;
		// 4. Left-shift the binary value one bit
		value <<= 1;
		// 5. If the original decimal value is negative, invert this encoding
		if (is_negative) value = ~value;
		// 6. Break the binary value out into 5-bit chunks (starting from the right hand side)
		// 7. Place the 5-bit chunks into reverse order
		Vector<Integer> chunks = new Vector<Integer>();
		do {
			chunks.add(value & 0x1F);
			value >>>= 5;
		} while (0 != value);
		// 8. OR each value with 0x20 if another bit chunk follows
		for (int i = 0; i < chunks.size() - 1; ++i)
			chunks.set(i, chunks.get(i) | 0x20);
		// 9. Convert each value to decimal
		// 10. Add 63 to each value
		// 11. Convert each value to its ASCII equivalent
		for (Integer chunk : chunks)
			sb.append((char) (chunk.intValue() + 63));
	}

	@Override
	public void reset() {
		sb.setLength(0);
		prev = null;
	}

}
