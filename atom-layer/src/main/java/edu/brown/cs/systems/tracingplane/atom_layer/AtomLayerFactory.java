package edu.brown.cs.systems.tracingplane.atom_layer;

import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

/**
 * <p>
 * All {@link AtomLayer} implementations should provide an {@link AtomLayerFactory} class for creating a default
 * instance.
 * </p>
 */
public interface AtomLayerFactory {

    AtomLayer<?> newAtomLayer();

    /**
     * An implementation of {@link TransitLayerFactory} that returns the default configured {@link AtomLayer}
     */
    public static class TransitLayerFactoryImpl implements TransitLayerFactory {

        @Override
        public TransitLayer<?> newTransitLayer() {
            return AtomLayerConfig.defaultAtomLayer();
        }

    }

}
