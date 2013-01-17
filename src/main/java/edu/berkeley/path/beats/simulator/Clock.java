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

final class Clock {
	protected double t;					// [sec]
	protected double to;				// [sec]
	protected double dt;				// [sec]
	protected double maxt;				// [sec]
	protected int currentstep;			// [sec]
	
	public Clock(double to,double tf,double dt){
		this.t = to;
		this.to = to;
		this.dt = dt;
		this.maxt = tf;
		this.currentstep = 0;
	}
	
	protected void reset(){
		t = to;
		currentstep = 0;
	}

	protected double getT() {
		return t;
	}
	
	protected double getTElapsed(){
		return t-to;
	}

	protected int getCurrentstep() {
		return currentstep;
	}

	protected int getTotalSteps(){
		return (int) Math.ceil((maxt-to)/dt);
	}
	
	protected void advance(){
		currentstep++;
		t = to + currentstep*dt;
	}
	
	protected boolean expired(){
		return t>maxt;
	}

	protected boolean istimetosample(int samplesteps,int stepinitial){	
		if(currentstep<=1)
			return true;
		if(currentstep<stepinitial)
			return false;
		return (currentstep-stepinitial) % samplesteps == 0;
	}
	
	protected int sampleindex(int stepinitial,int samplesteps){
		if(samplesteps>0){
			return SiriusMath.floor((currentstep-stepinitial)/((float)samplesteps));
		}
		else
			return 0;
	}
	
	public double getStartTime(){
		return to;
	}

	public double getEndTime(){
		return maxt;
	}
	
	public void print(){
		System.out.println("t=" + t + "\t\tstep=" + currentstep);
	}
}
