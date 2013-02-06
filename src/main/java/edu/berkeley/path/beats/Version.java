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

package edu.berkeley.path.beats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import edu.berkeley.path.beats.simulator.BeatsException;

/**
 * Retrieves and stores application schema and engine versions
 */
@SuppressWarnings("restriction")
public class Version {
	String schemaVersion;
	String engineVersion;

	private Version() {}

	/**
	 * @return the schemaVersion
	 */
	public String getSchemaVersion() {
		return schemaVersion;
	}

	/**
	 * @param schemaVersion the schemaVersion to set
	 */
	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	/**
	 * @return the engineVersion
	 */
	public String getEngineVersion() {
		return engineVersion;
	}

	/**
	 * @param engineVersion the engineVersion to set
	 */
	public void setEngineVersion(String engineVersion) {
		this.engineVersion = engineVersion;
	}

	/**
	 * @return java version
	 */
	public String getJavaVersion() {
		return System.getProperty("java.version");
	}

	private static Logger logger = Logger.getLogger(Version.class);

	public static Version get() {
		Version version = new Version();

		// schema version
		try {
			version.setSchemaVersion(edu.berkeley.path.beats.util.ScenarioUtil.getSchemaVersion());
		} catch (BeatsException exc) {
			logger.error("Failed to retrieve schema version", exc);
		}

		// engine version
		java.io.InputStream istream = Version.class.getClassLoader().getResourceAsStream("engine.version");
		if (null != istream) {
			BufferedReader br = new BufferedReader(new InputStreamReader(istream));
			try{
				version.setEngineVersion(br.readLine());
				br.close();
			} catch (IOException exc) {
				logger.error("Failed to retrieve engine version", exc);
			}
		}

		return version;
	}

	@Override
	public String toString() {
		final String linesep = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("schema: ").append(getSchemaVersion());
		sb.append(linesep);
		sb.append("engine: ").append(getEngineVersion());
		sb.append(linesep);
		sb.append("java:   ").append(getJavaVersion());
		return sb.toString();
	}
}
