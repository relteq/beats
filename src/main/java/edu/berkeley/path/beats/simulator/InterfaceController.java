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

/** Controller interface.
 * 
 * @author Gabriel Gomes (gomes@path.berkeley.edu)
 */
public interface InterfaceController {
		
	/** Register the controller with its targets. 
	 * 
	 * <p> All controllers must register with their targets in order to be allowed to
	 * manipulate them. This is to prevent clashes, in which two or 
	 * more controllers access the same variable. Use 
	 * {@link Controller#registerFlowController} {@link Controller#registerSpeedController} to register. 
	 * The return value of these methods indicates whether the registration was successful.
	 * 
	 * @return <code>true</code> if the controller successfully registered with all of its targets; 
	 * <code>false</code> otherwise.
	 */
	public boolean register();
	
	/** deRegister the controller with its targets. 
	 * 
	 * <p> All controllers must deregister with their targets when they are no longer active
	 *  This is to prevent clashes, in which two or more controllers access the same variable at different simulation periods 
	 * . Use {@link Controller#deregisterFlowController} {@link Controller#deregisterSpeedController} to register. 
	 * The return value of these methods indicates whether the deregistration was successful.
	 * 
	 * @return <code>true</code> if the controller successfully registered with all of its targets; 
	 * <code>false</code> otherwise.
	 */
	public boolean deregister();
}
