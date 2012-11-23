package edu.berkeley.path.beats.util.polyline;

import edu.berkeley.path.beats.jaxb.ObjectFactory;

public abstract class DecoderBase implements DecoderIF {

	protected ObjectFactory factory = null;

	@Override
	public void setObjectFactory(ObjectFactory factory) {
		this.factory = factory;
	}

}
