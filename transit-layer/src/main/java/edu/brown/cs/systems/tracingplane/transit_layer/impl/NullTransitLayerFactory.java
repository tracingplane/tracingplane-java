package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

public class NullTransitLayerFactory implements TransitLayerFactory {

	public TransitLayer<?> newTransitLayer() {
		return new NullTransitLayer();
	}

}