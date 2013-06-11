/**
 * Copyright (c) 2012, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **/

package edu.berkeley.path.beats.simulator;

import edu.berkeley.path.beats.simulator.Scenario.SignalPhases;

public abstract class OutputWriterBase implements InterfaceOutputWriter{
	
	protected Scenario scenario;
	protected double outDt;			// output frequency in seconds
	protected int outSteps;			// output frequency in simulation steps

	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////
	
	public OutputWriterBase(Scenario scenario,double outDt,int outsteps) {
		this.scenario = scenario;
		this.outDt = outDt;
		this.outSteps = outsteps;
	}

	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////
	
	public int getOutSteps() {
		return outSteps;
	}
	
	public double getOutDtInSeconds() {
		return outDt;
	}

	/**
	 * @return the scenario
	 */
	public Scenario getScenario() {
		return scenario;
	}

	/////////////////////////////////////////////////////////////////////
	// protected
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Initializes a link cumulative data storage,
	 * if that has not yet been done.
	 * Calling this method multiple times is safe
	 */
	protected void requestLinkCumulatives() {
		scenario.getCumulatives().storeLinks();
	}
	
	/**
	 * Initializes a signal phase storage,
	 * if that has not yet been done.
	 * Calling this method multiple times is safe
	 */
	protected void requestSignalPhases() {
		scenario.getCumulatives().storeSignalPhases();
	}
	
	/**
	 * Retrieves completed phases for the given signal
	 * @param signal
	 * @return completed signal phases
	 * @throws BeatsException if the signal phase storage has not been initialized
	 */
	protected SignalPhases getCompletedPhases(edu.berkeley.path.beats.jaxb.Signal signal) throws BeatsException {
		return scenario.getCumulatives().get(signal);
	}
	
	/**
	 * Retrieves link cumulative data for the given link
	 * @param link
	 * @return link cumulative data
	 * @throws BeatsException if the link cumulative data storage has not been initialized
	 */
	protected LinkCumulativeData getCumulatives(edu.berkeley.path.beats.jaxb.Link link) throws BeatsException {
		return scenario.getCumulatives().get(link);
	}

}
