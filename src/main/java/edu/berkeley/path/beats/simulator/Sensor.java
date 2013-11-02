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

/** Base class for sensors. 
 * Provides a default implementation of <code>InterfaceSensor</code>.
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class Sensor extends edu.berkeley.path.beats.jaxb.Sensor implements InterfaceSensor {
	
	/** The scenario that contains this sensor. */
	private Scenario myScenario;	

	private edu.berkeley.path.beats.jaxb.Sensor jaxbSensor;
	
	/** Sensor type. */
	private Sensor.Type myType;
	
	/** Current link where the sensor is located. */
	private Link myLink = null;

	/** Type of sensor. */
	public static enum Type	{  
	/** see {@link ObjectFactory#createSensor_LoopStation} 	*/	loop }
				   	   	       
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	protected Sensor(){
	}		  

	protected Sensor(Scenario myScenario,edu.berkeley.path.beats.jaxb.Sensor jaxbS,Sensor.Type myType){
		this.myScenario = myScenario;
		this.jaxbSensor = jaxbS;
		this.myType = myType;
		this.id = jaxbS.getId();
		if(jaxbS.getLinkId()!=null)
			myLink = myScenario.getLinkWithId(jaxbS.getLinkId());
	}
	
	/////////////////////////////////////////////////////////////////////
	// InterfaceSensor
	/////////////////////////////////////////////////////////////////////

	@Override
	public double[] getDensityInVPM(int ensemble) {
		return null;
	}

	@Override
	public double getOccupancy(int ensemble) {
		return Double.NaN;
	}

	@Override
	public double getTotalDensityInVPM(int ensemble) {
		return Double.NaN;
	}

	@Override
	public double[] getFlowInVPS(int ensemble) {
		return null;
	}

	@Override
	public double getTotalFlowInVPS(int ensemble) {
		return Double.NaN;
	}

	@Override
	public double getSpeedInMPS(int ensemble) {
		return Double.NaN;
	}

	@Override
	public double getTotalDensityInVeh(int ensemble) {
		return Double.NaN;
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

	public Scenario getMyScenario() {
		return myScenario;
	}	
	
	/** Sensor type. */
	public Sensor.Type getMyType() {
		return myType;
	}

	/** Link where the sensor is located. */
	public Link getMyLink() {
		return myLink;
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate / validate / reset / update
	/////////////////////////////////////////////////////////////////////
	
	protected void populate(Object jaxbobject) {
		return;
	}

	protected void validate() {
	}

	protected void reset() throws BeatsException {
		return;
	}

	protected void update() throws BeatsException {
		return;
	}

}
