package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerFactory;

/** TODO: documentation and description */
public class GenericBaggageLayerFactory implements BaggageLayerFactory {

    @Override
    public BaggageLayer<?> newBaggageLayer() {
        return new GenericBaggageLayer();
    }

}
