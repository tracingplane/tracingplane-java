package edu.brown.cs.systems.tracingplane.transit_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayerFactory;

public interface TransitLayerFactory {

    static final Logger log = LoggerFactory.getLogger(TransitLayerFactory.class);

    public static TransitLayer<?> createDefaultTransitLayer() {
        TransitLayerConfig config = new TransitLayerConfig();
        try {
            return createTransitLayer(config);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error(String.format("Unable to instantiate default transit layer factory %s, defaulting to %s",
                                    config.transitLayerFactory, NullTransitLayerFactory.class.getName()));
            return new NullTransitLayerFactory().newTransitLayer();
        }
    }

    public static TransitLayer<?> createTransitLayer(TransitLayerConfig config) throws InstantiationException,
                                                                                IllegalAccessException,
                                                                                ClassNotFoundException {
        return ((TransitLayerFactory) Class.forName(config.transitLayerFactory).newInstance()).newTransitLayer();
    }

    TransitLayer<?> newTransitLayer();

}
