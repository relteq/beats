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

import java.util.ArrayList;

import edu.berkeley.path.beats.jaxb.Parameter;

/** XXX. 
 * YYY
 *
*/
final public class Parameters extends edu.berkeley.path.beats.jaxb.Parameters {
	
	/////////////////////////////////////////////////////////////////////
	// construction
	/////////////////////////////////////////////////////////////////////

	public Parameters(){
        parameter = new ArrayList<Parameter>();
	}
	
	public void addParameter(String name,String value){
		
		// check if it exists
		for(Parameter p : parameter)
			if(p.getName().equals(name))
				return;
		
		// add to the list
		Parameter newp = new Parameter();
		newp.setName(name);
		newp.setValue(value);
		parameter.add(newp);
	}

	/////////////////////////////////////////////////////////////////////
	// public API
	/////////////////////////////////////////////////////////////////////

    public double readParameter(String name,double def){
        double val = def;
        if (has(name)){
            String str = get(name);
            val = str==null ? def : Double.parseDouble(str);
        }
        return val;
    }

	/**
	 * Tests whether a parameter with the given name exists
	 * @param name
	 * @return true, if such a parameter exists; false, otherwise
	 */
	public boolean has(String name) {
		if(name==null)
			return false;
		for (edu.berkeley.path.beats.jaxb.Parameter param : getParameter()) {
			if (name.equals(param.getName())) return true;
		}
		return false;
	}

	/**
	 * Retrieves a value of a parameter with the given name
	 * @param name
	 * @return null, if such a parameter does not exist
	 */
	public String get(String name) {
		if(name==null)
			return null;
		java.util.ListIterator<edu.berkeley.path.beats.jaxb.Parameter> iter = getParameter().listIterator(getParameter().size());
		while (iter.hasPrevious()) {
			edu.berkeley.path.beats.jaxb.Parameter param = iter.previous();
			if (name.equals(param.getName())) return param.getValue();
		}
		return null;
	}
	
}
