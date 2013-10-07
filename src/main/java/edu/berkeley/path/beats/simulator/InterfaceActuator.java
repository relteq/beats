package edu.berkeley.path.beats.simulator;

import java.util.List;

public interface InterfaceActuator {
	public void deploy_metering_rate(Double metering_rate);
	public void deploy_green_times(List<Double> green_times);
	public void deploy_cms_split();
	public void deploy_vsl_speed();
}
