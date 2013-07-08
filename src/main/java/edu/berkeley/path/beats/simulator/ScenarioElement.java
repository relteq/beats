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

/** Container class for components used as targets or feedback in controllers and events. 
 * 
 * <p>This class provides a container for links, nodes, controllers, sensors, events, and signals
 * that appear in the target or feedback list of controllers and events.
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public final class ScenarioElement extends edu.berkeley.path.beats.jaxb.ScenarioElement {
	
	private Scenario myScenario;
	private ScenarioElement.Type myType;
	private Object reference;

	/** Type of scenario element. */
	public static enum Type {  
	/** see {@link Link} 		*/ link,
	/** see {@link Node} 		*/ node,
	/** see {@link Controller} */ controller,
	/** see {@link Sensor} 	*/ sensor,
	/** see {@link Event} 		*/ event,
	/** see {@link Signal} 	*/ signal }
			

	/////////////////////////////////////////////////////////////////////
	// public constructor
	/////////////////////////////////////////////////////////////////////


	protected ScenarioElement(Scenario myScenario,edu.berkeley.path.beats.jaxb.ScenarioElement jaxbS){
		this.myScenario = myScenario;
		this.setId(jaxbS.getId().trim());
		this.myType = ScenarioElement.Type.valueOf(jaxbS.getType());
		
		switch(myType){
		case link:
			this.reference = myScenario.getLinkWithId(id);
			break;
		case node:
			this.reference = myScenario.getNodeWithId(id);
			break;
		case sensor:
			this.reference = myScenario.getSensorWithId(id);
			break;
		case signal:
			this.reference = myScenario.getSignalWithId(id);
			break;
		case controller:
			this.reference = myScenario.getControllerWithId(id);
			break;
		default:
			this.reference = null;	
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// protected constructor
	/////////////////////////////////////////////////////////////////////

	protected ScenarioElement(){}
	
	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

	/** The scenario that contains the referenced component.
	 *  @return The scenario.
	 */
	public Scenario getMyScenario() {
		return myScenario;
	}

	/** The type of the referenced component.
	 * <p> i.e. link, node, controller, sensor, event, or signal.
	 * @return Component type. 
	 */
	public ScenarioElement.Type getMyType() {
		return myType;
	}

	/** Reference to the component.
	 * @return A java Object.
	 */
	public Object getReference() {
		return reference;
	}
	
}
