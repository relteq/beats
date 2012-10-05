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

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Retrieves and stores application schema and engine versions
 */
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

	public static Version get() {
		Version version = new Version();
		ClassLoader classLoader = Version.class.getClassLoader();
		// schema version
		try {
			XMLStreamReader xmlsr = XMLInputFactory.newInstance().createXMLStreamReader(classLoader.getResourceAsStream("sirius.xsd"));
			while (xmlsr.hasNext()) {
				if (XMLStreamConstants.START_ELEMENT == xmlsr.getEventType()) {
					javax.xml.namespace.QName qname = xmlsr.getName();
					if ("schema" == qname.getLocalPart() && XMLConstants.W3C_XML_SCHEMA_NS_URI == qname.getNamespaceURI()) {
						version.setSchemaVersion(xmlsr.getAttributeValue(null, "version"));
						break;
					}
				}
				xmlsr.next();
			}
			xmlsr.close();
		} catch (XMLStreamException exc) {
			exc.printStackTrace();
		} catch (FactoryConfigurationError exc) {
			exc.printStackTrace();
		}

		// engine version
		BufferedReader br = new BufferedReader(new InputStreamReader(Version.class.getClassLoader().getResourceAsStream("engine.version")));
		try{
			version.setEngineVersion(br.readLine());
			br.close();
		} catch (IOException exc) {
			exc.printStackTrace();
		}

		return version;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("schema: ").append(getSchemaVersion());
		sb.append("    ");
		sb.append("engine: ").append(getEngineVersion());
		return sb.toString();
	}
}
