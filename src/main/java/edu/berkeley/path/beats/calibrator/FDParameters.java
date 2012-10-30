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

package edu.berkeley.path.beats.calibrator;

public class FDParameters {

	// nominal values
	private static float nom_vf = 65 * 0.44704f;			// [m/s]
	private static float nom_w = 15 * 0.44704f; 			// [m/s]
	private static float nom_q_max = 2000 / 3600.0f;		// [veh/sec/lane]

	private float vf;
	private float w;
	private float q_max;
	
	public FDParameters(){
		vf = FDParameters.nom_vf;
		w  = FDParameters.nom_w;
		q_max = FDParameters.nom_q_max;
	}
	
	public FDParameters(float vf,float w,float q_max){
		this.vf = vf;
		this.w  = w;
		this.q_max = q_max;
	}
	
	public void setFD(float vf,float w,float q_max){
	if(!Float.isNaN(vf))
		this.vf = vf;
	if(!Float.isNaN(w))
		this.w = w;
	if(!Float.isNaN(q_max))
		this.q_max = q_max;
	}
	
	public float getVf() {
		return vf;
	}
	
	public float getW() {
		return w;
	}
	
	public float getQ_max() {
		return q_max;
	}
	
	public float getRho_crit() {
		return q_max/vf;
	}
	
	public float getRho_jam() {
		return q_max*(1/vf+1/w);
	}
	
}
