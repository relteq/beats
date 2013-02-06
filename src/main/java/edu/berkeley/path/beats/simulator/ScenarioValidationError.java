package edu.berkeley.path.beats.simulator;

@SuppressWarnings("serial")
public class ScenarioValidationError extends BeatsException {
	public ScenarioValidationError() {
		super("Scenario validation failed. See error log for details");
	}
}
