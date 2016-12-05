package edu.brown.cs.systems.tracingplane.context_layer.impl;

import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayerFactory;

public class RawAtomsContextLayerFactory implements ContextLayerFactory {

	@Override
	public ContextLayer<?> newContextLayer() {
		return new RawAtomsContextLayer();
	}

}
