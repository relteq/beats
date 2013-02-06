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

import java.math.BigDecimal;
import java.math.BigInteger;

import edu.berkeley.path.beats.jaxb.DisplayPosition;
import edu.berkeley.path.beats.jaxb.LinkReference;
import edu.berkeley.path.beats.jaxb.Parameters;
import edu.berkeley.path.beats.jaxb.Table;

/** Base class for sensors. 
 * Provides a default implementation of <code>InterfaceSensor</code>.
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class Sensor extends edu.berkeley.path.beats.jaxb.Sensor implements InterfaceSensor {
   			
	/** The scenario that contains this sensor. */
	protected Scenario myScenario;	

	/** Sensor type. */
	protected Sensor.Type myType;
	
	/** Current link where the sensor is located. */
	protected Link myLink = null;

	/** Type of sensor.
	 *
	 * TMC = Traffic Message Channel.
	 * This is a static way of reporting probe measurements
	 * employed by INRIX, Navteq, etc.
	 */
	public static enum Type	{  
	/** see {@link ObjectFactory#createSensor_LoopStation} 	*/	loop,
		magnetic,
		radar,
		camera,
		TMC
	};
				   	   	       
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Sensor(){
	}		  

	protected final void populateFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Sensor s,Sensor.Type myType){
		this.myScenario = myScenario;
		this.myType = myType;
		this.id = s.getId();
		if(s.getLinkReference()!=null)
			myLink = myScenario.getLinkWithId(s.getLinkReference().getId());
	}

	/////////////////////////////////////////////////////////////////////
	// hide base class setters
	/////////////////////////////////////////////////////////////////////

	@Override
	public void setDisplayPosition(DisplayPosition value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setLinkReference(LinkReference value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setParameters(Parameters value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setTable(Table value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setId(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setLinkPosition(BigDecimal value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setType(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setSensorIdOriginal(String value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setLaneNumber(BigInteger value) {
		System.out.println("This setter is hidden.");
	}

	@Override
	public void setHealthStatus(BigDecimal value) {
		System.out.println("This setter is hidden.");
	}	
	
	/////////////////////////////////////////////////////////////////////
	// InterfaceSensor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	@Override
	public Double[] getDensityInVPM(int ensemble) {
		return null;
	}

	/** @y.exclude */
	@Override
	public double getOccupancy(int ensemble) {
		return Double.NaN;
	}

	/** @y.exclude */
	@Override
	public double getTotalDensityInVPM(int ensemble) {
		return Double.NaN;
	}

	/** @y.exclude */
	@Override
	public Double[] getFlowInVPS(int ensemble) {
		return null;
	}

	/** @y.exclude */
	@Override
	public double getTotalFlowInVPS(int ensemble) {
		return Double.NaN;
	}

	/** @y.exclude */
	@Override
	public double getSpeedInMPS(int ensemble) {
		return Double.NaN;
	}

	/** @y.exclude */
	@Override
	public double getTotalDensityInVeh(int ensemble) {
		return Double.NaN;
	}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

	/** The scenario that contains this sensor. */
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
