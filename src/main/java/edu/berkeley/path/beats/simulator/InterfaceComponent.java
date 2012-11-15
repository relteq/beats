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

/** Common interface for controllers, sensors, and events.
*
* @author Gabriel Gomes (gomes@path.berkeley.edu)
*/
public interface InterfaceComponent {

	/** Populate the component with configuration data. 
	 * 
	 * <p> Called once by {@link ObjectFactory#createAndLoadScenario}.
	 * It is passed a JAXB object with data loaded from the configuration file. 
	 * Use this function to populate and initialize all fields in the
	 * component. 
	 * 
	 * @param jaxbobject Object
	 */
	public void populate(Object jaxbobject);
	
	/** Validate the component.
	 * 
	 * <p> Called once by {@link ObjectFactory#createAndLoadScenario}.
	 * It checks the validity of the configuration parameters.
	 * Events are validated at their activation time. All other components
	 * are validated when the scenario is loaded. 
	 * 
	 * @return <code>true</code> if the data is valid, <code>false</code> otherwise. 
	 */
	public void validate();

	/** Prepare the component for simulation.
	 * 
	 * <p> Called by {@link Scenario#run} each time a new simulation run is started.
	 * It is used to initialize the internal state of the component.
	 * <p> Because events are state-less, the {@link Event} class provides a default 
	 * implementation of this method, so it need not be implemented by other event classes.
	 */
	public void reset() throws SiriusException;

	/** Update the state of the component.
	 * 
	 * <p> Called by {@link Scenario#run} at each simulation time step.
	 * This function updates the internal state of the component.
	 * <p> Because events are state-less, the {@link Event} class provides a default 
	 * implementation of this method, so it need not be implemented by other event classes.
	 */
	public void update() throws SiriusException;
	
}
