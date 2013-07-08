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

package edu.berkeley.path.beats.sensor;

import java.util.ArrayList;

public class DataSource {
	private String urlname;
	private DataSource.Format format;
	private ArrayList<Integer> for_vds = new ArrayList<Integer>();

	public static enum Format { NULL, 
								PeMSDataClearinghouse,
								CaltransDBX,
								BHL }
	
	public DataSource(String urlname,String formatstr) {
		this.urlname = urlname;
		if(formatstr.compareTo("PeMS Data Clearinghouse")==0)
			format = DataSource.Format.PeMSDataClearinghouse;

		if(formatstr.compareTo("Caltrans DBX")==0)
			format = DataSource.Format.CaltransDBX;

		if(formatstr.compareTo("BHL")==0)
			format = DataSource.Format.BHL;		
	}

	public DataSource(edu.berkeley.path.beats.sensor.DataSource d) {
		this.urlname = d.getUrl();
		this.format = d.getFormat();
	}
	
	public String getUrl() {
		return urlname;
	}

	public DataSource.Format getFormat() {
		return format;
	}
	
	public ArrayList<Integer> getFor_vds() {
		return for_vds;
	}
	
	public void add_to_for_vds(int vds){
		for_vds.add(vds);
	}
	
}
