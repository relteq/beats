package edu.berkeley.path.beats.util.scenario;

import org.codehaus.jettison.mapped.MappedNamespaceConvention;

class JSONSettings {

	public static MappedNamespaceConvention getConvention() {
		org.codehaus.jettison.mapped.Configuration config = new org.codehaus.jettison.mapped.Configuration();
		return new MappedNamespaceConvention(config);
	}

}
