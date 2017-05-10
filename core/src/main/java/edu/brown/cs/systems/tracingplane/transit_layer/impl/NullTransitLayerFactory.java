package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

/**
 * The factory for {@link NullTransitLayer}, which is the default {@link TransitLayer} if no other is configured.
 */
public class NullTransitLayerFactory implements TransitLayerFactory {

    public TransitLayer<?> newTransitLayer() {
        return new NullTransitLayer();
    }

}