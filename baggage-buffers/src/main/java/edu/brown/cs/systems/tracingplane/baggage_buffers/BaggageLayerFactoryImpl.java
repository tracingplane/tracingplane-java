package edu.brown.cs.systems.tracingplane.baggage_buffers;

import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayer;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerFactory;

public class BaggageLayerFactoryImpl implements BaggageLayerFactory {

    @Override
    public BaggageLayer<?> newBaggageLayer() {
        return new BaggageBuffers();
    }

}
