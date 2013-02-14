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

/** @y.exclude */
public final class JaxbObjectFactory extends edu.berkeley.path.beats.jaxb.ObjectFactory {
	
	@Override
	public edu.berkeley.path.beats.jaxb.CapacityProfile createCapacityProfile() {
		return new CapacityProfile();
	}
	
	@Override
	public edu.berkeley.path.beats.jaxb.ControllerSet createControllerSet() {
		return new ControllerSet();
	}
	
	@Override
	public edu.berkeley.path.beats.jaxb.DemandProfile createDemandProfile() {
		return new DemandProfile();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.DemandProfileSet createDemandProfileSet() {
		return new DemandProfileSet();
	}
	
	@Override
	public edu.berkeley.path.beats.jaxb.FundamentalDiagram createFundamentalDiagram() {
		return new FundamentalDiagram();
	}
	
	@Override
	public edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile createFundamentalDiagramProfile() {
		return new FundamentalDiagramProfile();
	}	
	
	@Override
	public edu.berkeley.path.beats.jaxb.InitialDensitySet createInitialDensitySet() {
		return new InitialDensitySet();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Link createLink() {
		return new Link();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Network createNetwork() {
		return new Network();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Node createNode() {
		return new Node_LNCTM_UnknownSR_A();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Scenario createScenario() {
		return new Scenario();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.ScenarioElement createScenarioElement() {
		return new ScenarioElement();
	}
	
	@Override
	public edu.berkeley.path.beats.jaxb.Signal createSignal() {
		return new Signal();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.SplitratioProfile createSplitratioProfile() {
		return new SplitRatioProfile();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.SplitRatioProfileSet createSplitRatioProfileSet() {
		return new SplitRatioProfileSet();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Sensor createSensor() {
		return new Sensor();
	}

	@Override
	public edu.berkeley.path.beats.jaxb.Parameters createParameters() {
		return new Parameters();
	}

}
