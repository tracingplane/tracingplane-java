package edu.brown.cs.systems.tracingplane.baggage_layer;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayerFactory;

public interface BaggageLayerFactory {

    BaggageLayer<?> newBaggageLayer();

    public static class AtomLayerFactoryImpl implements AtomLayerFactory {

        @Override
        public AtomLayer<?> newAtomLayer() {
            return BaggageLayerConfig.defaultBaggageLayer();
        }

    }

}
