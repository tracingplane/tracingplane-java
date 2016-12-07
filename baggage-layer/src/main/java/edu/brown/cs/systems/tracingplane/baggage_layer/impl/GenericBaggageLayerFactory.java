package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

/**
 * TODO: documentation and description
 * 
 */
public class GenericBaggageLayerFactory implements BaggageLayerFactory {

	@Override
	public BaggageLayer<?> newBaggageLayer() {
		return new GenericBaggageLayer();
	}

}
