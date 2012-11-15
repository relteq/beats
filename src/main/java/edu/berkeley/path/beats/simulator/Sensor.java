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

/** Base implementation of {@link InterfaceSensor}.
 * 
 * <p> This is the base class for all sensors contained in a scenario. 
 * It provides a full default implementation of <code>InterfaceSensor</code>
 * so that extended classes need only implement a portion of the interface.
 *
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public class Sensor extends edu.berkeley.path.beats.jaxb.Sensor implements InterfaceComponent,InterfaceSensor {
   			
	/** The scenario that contains this sensor. */
	protected Scenario myScenario;	

	/** Sensor type. */
	protected Sensor.Type myType;
	
	/** Current link where the sensor is located. */
	protected Link myLink = null;

	/** Type of sensor. */
	public static enum Type	{  
	/** see {@link ObjectFactory#createSensor_LoopStation} 	*/	static_point,
	                                                            static_area,
	/** see {@link ObjectFactory#createSensor_Floating} 	*/  moving_point };
				   	   	       
	/////////////////////////////////////////////////////////////////////
	// protected default constructor
	/////////////////////////////////////////////////////////////////////

	/** @y.exclude */
	protected Sensor(){
	}		  

	/////////////////////////////////////////////////////////////////////
	// InterfaceSensor
	/////////////////////////////////////////////////////////////////////

//	/** Default implementation of {@link InterfaceSensor#getDensityInVPM()} 
//	 * @return <code>null</code>
//	 * */
//	@Override
//	public double[] getDensityInVPM(int ensemble) {
//		return null;
//	}

	/** Default implementation of {@link InterfaceSensor#getOccupancy()} 
	 * @return <code>Double.NaN</code>
	 * */
	@Override
	public double getOccupancy(int ensemble) {
		return Double.NaN;
	}
	
	/** Default implementation of {@link InterfaceSensor#getTotalDensityInVPM()} 
	 * @return <code>Double.NaN</code>
	 * */
	@Override
	public double getTotalDensityInVPM(int ensemble) {
		return Double.NaN;
	}

	/** Default implementation of {@link InterfaceSensor#getFlowInVPS()}
	 * @return <code>null</code>
	 * */
	@Override
	public Double[] getFlowInVPS(int ensemble) {
		return null;
	}

	/** Default implementation of {@link InterfaceSensor#getTotalFlowInVPS()}
	 * @return <code>Double.NaN</code>
	 * */
	@Override
	public double getTotalFlowInVPS(int ensemble) {
		return Double.NaN;
	}

	/** Default implementation of {@link InterfaceSensor#getSpeedInMPS()}
	 * @return <code>Double.NaN</code>
	 * */
	@Override
	public double getSpeedInMPS(int ensemble) {
		return Double.NaN;
	}
	
	/** Default implementation of {@link InterfaceSensor#getTotalDensityInVeh()} 
	 * @return <code>Double.NaN</code>
	 * */
	@Override
	public double getTotalDensityInVeh(int ensemble) {
		return Double.NaN;
	}
	
	/////////////////////////////////////////////////////////////////////
	// API
	/////////////////////////////////////////////////////////////////////

	/** The scenario that contains this sensor.
	 * @return id String
	 * */
	public Scenario getMyScenario() {
		return myScenario;
	}

	/** Sensor type. 
	 * @return type _Sensor.Type
	 * */
	public Sensor.Type getMyType() {
		return myType;
	}

	/** Current link where the sensor is located. 
	 * <p> This value may change in time if the sensor is mobile.
	 * @return link  _Link
	 * */
	public Link getMyLink() {
		return myLink;
	}
	
	/////////////////////////////////////////////////////////////////////
	// populate
	/////////////////////////////////////////////////////////////////////
	
	/** @y.exclude */
	protected final void populateFromJaxb(Scenario myScenario,edu.berkeley.path.beats.jaxb.Sensor s,Sensor.Type myType){
		this.myScenario = myScenario;
		this.myType = myType;
		this.id = s.getId();
		if(s.getLinkReference()!=null)
			myLink = myScenario.getLinkWithId(s.getLinkReference().getId());
	}

	@Override
	public void populate(Object jaxbobject) {
		return;
	}

	@Override
	public void validate() {
	}

	@Override
	public void reset() throws SiriusException {
		return;
	}

	@Override
	public void update() throws SiriusException {
		return;
	}
	
}
