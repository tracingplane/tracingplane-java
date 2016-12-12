package edu.brown.cs.systems.tracingplane.atom_layer;

import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

/**
 * <p>
 * {@link AtomLayer} implementations should provide an {@link AtomLayerFactory} class for creating a default instance.
 * </p>
 */
public interface AtomLayerFactory {

    AtomLayer<?> newAtomLayer();

    public static class TransitLayerFactoryImpl implements TransitLayerFactory {

        @Override
        public TransitLayer<?> newTransitLayer() {
            return AtomLayerConfig.defaultAtomLayer();
        }

    }

}
