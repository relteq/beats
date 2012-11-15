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

package edu.berkeley.path.beats.simulator.output;

import java.util.Properties;

import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.simulator.SiriusException;

/**
 *
 */
public class OutputWriterFactory {
	/**
	 * Constructs an output writer of a given type
	 * @param scenario
	 * @param props output writer properties (type, prefix)
	 * @return an output writer
	 * @throws SiriusException
	 */
	public static OutputWriterIF getWriter(Scenario scenario, Properties props) throws SiriusException {
		final String type = props.getProperty("type");
		if (type.equals("xml")) return new XMLOutputWriter(scenario, props);
		else if (type.equals("db")) return new DBOutputWriter(scenario);
		else if (type.equals("text") || type.equals("plaintext")) return new TextOutputWriter(scenario, props);
		else throw new SiriusException("Unknown output writer type '" + type + "'");
	}
}
