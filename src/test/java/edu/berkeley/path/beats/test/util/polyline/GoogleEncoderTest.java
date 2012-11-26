package edu.berkeley.path.beats.test.util.polyline;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

import edu.berkeley.path.beats.jaxb.Point;
import edu.berkeley.path.beats.simulator.SiriusException;
import edu.berkeley.path.beats.util.polyline.GoogleDecoder;
import edu.berkeley.path.beats.util.polyline.GoogleEncoder;

public class GoogleEncoderTest {

	private static Point createPoint(BigDecimal lat, BigDecimal lng) {
		Point point = new Point();
		point.setLat(lat);
		point.setLng(lng);
		return point;
	}

	private static Logger logger = Logger.getLogger(GoogleEncoderTest.class);

	/**
	 * See https://developers.google.com/maps/documentation/utilities/polylinealgorithm
	 * Input: (38.5, -120.2), (40.7, -120.95), (43.252, -126.453).
	 * Output: "_p~iF~ps|U_ulLnnqC_mqNvxq`@".
	 */
	@Test
	public void testEncoding() {
		GoogleEncoder encoder = new GoogleEncoder();
		encoder.add(createPoint(new BigDecimal("38.5"), new BigDecimal("-120.2")));
		encoder.add(createPoint(new BigDecimal("40.7"), new BigDecimal("-120.95")));
		encoder.add(createPoint(new BigDecimal("43.252"), new BigDecimal("-126.453")));
		String result = encoder.getResult();
		logger.info("Encoded polyline: " + result);
		assertEquals(null, "_p~iF~ps|U_ulLnnqC_mqNvxq`@", result);
	}

	private final static int NPOINTS = 64;

	/**
	 * Checks if the encoding process is invertible for coordinates with precision 5 or less
	 * @throws SiriusException
	 */
	@Test
	public void testExact() throws SiriusException {
		GoogleEncoder encoder = new GoogleEncoder();
		BigDecimal[] ilat = new BigDecimal[NPOINTS];
		BigDecimal[] ilng = new BigDecimal[NPOINTS];
		Random rnd = new Random();
		for (int i = 0; i < NPOINTS; ++i) {
			Point point = createPoint(
					ilat[i] = generateExactCoordinate(rnd, -90d, 90d),
					ilng[i] = generateExactCoordinate(rnd, -180d, 180d));
			encoder.add(point);
		}
		String encoded = encoder.getResult();
		logger.info("Encoded polyline: " + encoded);

		GoogleDecoder decoder = new GoogleDecoder();
		List<Point> opl = decoder.decode(encoded);
		BigDecimal[] olat = new BigDecimal[opl.size()];
		BigDecimal[] olng = new BigDecimal[opl.size()];
		for (int i = 0; i < opl.size(); ++i) {
			Point point = opl.get(i);
			olat[i] = point.getLat();
			olng[i] = point.getLng();
		}

		checkEquals(ilat, olat);
		checkEquals(ilng, olng);
	}

	private static BigDecimal generateExactCoordinate(Random rnd, double min, double max) {
		return BigDecimal.valueOf(rnd.nextInt((int) ((max - min) * 1E5)) + (long) (min * 1E5)).divide(BigDecimal.valueOf((long) 1E5));
	}

	private void checkEquals(BigDecimal[] expected, BigDecimal[] actual) {
		if (expected.length != actual.length)
			fail("Array lengths differ");
		for (int i = 0; i < expected.length; ++i)
			if (expected[i].compareTo(actual[i]) != 0)
				fail("Arrays first differ at element " + i + "; expected:" + expected[i] + ", but was:" + actual[i]);
	}

	/**
	 * Checks if the encoding precision is 1E-5
	 * @throws SiriusException
	 */
	@Test
	public void testRounding() throws SiriusException {
		Random rnd = new Random();

		List<Point> ipoints = new java.util.ArrayList<Point>(NPOINTS);
		for (int i = 0; i < NPOINTS; ++i)
			ipoints.add(createPoint(
					generateCoordinate(rnd, -90d, 90d),
					generateCoordinate(rnd, -180d, 180d)));

		GoogleEncoder encoder = new GoogleEncoder();
		encoder.add(ipoints);
		String encoded = encoder.getResult();
		logger.info("Encoded polyline: " + encoded);

		GoogleDecoder decoder = new GoogleDecoder();
		List<Point> opoints = decoder.decode(encoded);
		if (ipoints.size() != opoints.size()) fail("List sizes do not match");

		BigDecimal epsilon = BigDecimal.valueOf(1E-5);
		for (int i = 0; i < NPOINTS; ++i)
			checkEquals(ipoints.get(i), opoints.get(i), epsilon);
	}

	private static BigDecimal generateCoordinate(Random rnd, double min, double max) {
		return BigDecimal.valueOf(rnd.nextDouble() * (max - min) + min);
	}

	private void checkEquals(Point expected, Point actual, BigDecimal epsilon) {
		checkEquals(expected.getLat(), actual.getLat(), epsilon);
		checkEquals(expected.getLng(), actual.getLng(), epsilon);
	}

	private void checkEquals(BigDecimal expected, BigDecimal actual, BigDecimal epsilon) {
		if (actual.subtract(expected).abs().subtract(epsilon).doubleValue() > 0)
			fail("| (" + expected + ") - (" + actual + ") | > " + epsilon);
	}

}
