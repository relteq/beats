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

final class SplitRatioProfileSet extends edu.berkeley.path.beats.jaxb.SplitRatioProfileSet {

	protected Scenario myScenario;
	protected Integer [] vehicletypeindex; 	// index of vehicle types into global list

	/////////////////////////////////////////////////////////////////////
	// populate / reset / validate / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Scenario myScenario) {

		this.myScenario = myScenario;
		
		if(getSplitratioProfile().isEmpty())
			return;

		vehicletypeindex = myScenario.getVehicleTypeIndices(getVehicleTypeOrder());
		
		for(edu.berkeley.path.beats.jaxb.SplitratioProfile sr : getSplitratioProfile())
			((SplitRatioProfile) sr).populate(myScenario);
	}

	protected void validate() {

		if(getSplitratioProfile().isEmpty())
			return;

		// check that all vehicle types are accounted for
		if(vehicletypeindex.length!=myScenario.getNumVehicleTypes())
			SiriusErrorLog.addError("Vehicle types list in demand profile id=" +getId()+ " does not match that of settings.");
		
		for(edu.berkeley.path.beats.jaxb.SplitratioProfile sr : getSplitratioProfile())
			((SplitRatioProfile)sr).validate();		
	}

	protected void update() {
    	for(edu.berkeley.path.beats.jaxb.SplitratioProfile sr : getSplitratioProfile())
    		((SplitRatioProfile) sr).update();	
	}
	
}