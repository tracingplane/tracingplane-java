package edu.brown.cs.systems.tracingplane.transit_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayerFactory;

/**
 * {@link TransitLayer} implementations should provide a {@link TransitLayerFactory} class for creating a default
 * instance.
 */
public interface TransitLayerFactory {

    TransitLayer<?> newTransitLayer();

}
