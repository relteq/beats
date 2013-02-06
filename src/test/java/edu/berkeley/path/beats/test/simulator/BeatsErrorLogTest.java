package edu.berkeley.path.beats.test.simulator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.berkeley.path.beats.simulator.BeatsErrorLog;

public class BeatsErrorLogTest {
	
	@Before
	public void setUp() throws Exception {
		BeatsErrorLog.clearErrorMessage();
	}

	@Test
	public void test_clearErrorMessage() {
		BeatsErrorLog.addError("an error");
		BeatsErrorLog.addError("another error");
		BeatsErrorLog.addWarning("a warning");
		BeatsErrorLog.addWarning("another warning");
		BeatsErrorLog.clearErrorMessage();
		assertTrue(!BeatsErrorLog.haserror());
		assertTrue(!BeatsErrorLog.haswarning());
	}

	@Test
	public void test_adderror_haserror() {
		BeatsErrorLog.addError("an error");
		assertTrue(BeatsErrorLog.haserror());
	}

	@Test
	public void test_addwarning_haswarning() {
		BeatsErrorLog.addWarning("a warning");
		assertTrue(BeatsErrorLog.haswarning());
	}

	@Test
	public void test_hasmessage() {
		BeatsErrorLog.addWarning("a warning");
		assertTrue(BeatsErrorLog.hasmessage());
		BeatsErrorLog.clearErrorMessage();
	}

}
