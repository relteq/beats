package edu.berkeley.path.beats.processor;

public class Content {
	private String scenarioId;
	private String runs;
	
	public String getScenarioId() { return scenarioId; }
	public void setScenarioId(String id) { scenarioId = id; }
	public String getRuns() { return runs; }
	public void setRuns(String id) { runs = id; }	
	public Content() {
		runs = null; scenarioId = null;
	}
}
