package edu.brown.cs.systems.tracingplane.transit_layer;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.impl.NullTransitLayerFactory;

public class TransitLayerConfig {

    private static final Logger log = LoggerFactory.getLogger(TransitLayerConfig.class);
    static {
        BasicConfigurator.configure();
    }

    private static final String TRANSIT_LAYER_IMPLEMENTATION_KEY = "tracingplane.transit-layer.factory";

    public String transitLayerFactory;

    public TransitLayerConfig() {
        Config config = ConfigFactory.load();

        transitLayerFactory = config.getString(TRANSIT_LAYER_IMPLEMENTATION_KEY);
        try {
            Class.forName(transitLayerFactory);
        } catch (ClassNotFoundException e) {
            log.warn("The configured transit layer class {}=\"{}\" was not found; baggage will not be propagated",
                     TRANSIT_LAYER_IMPLEMENTATION_KEY, transitLayerFactory);
        }
    }

    public TransitLayer<?> createTransitLayer() throws InstantiationException, IllegalAccessException,
                                                ClassNotFoundException {
        return ((TransitLayerFactory) Class.forName(transitLayerFactory).newInstance()).newTransitLayer();
    }

    private static TransitLayer<?> defaultTransitLayer = null;

    public static synchronized TransitLayer<?> defaultTransitLayer() {
        if (defaultTransitLayer == null) {
            TransitLayerConfig config = new TransitLayerConfig();
            try {
                defaultTransitLayer = config.createTransitLayer();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error(String.format("Unable to instantiate default transit layer factory %s, defaulting to %s",
                                        config.transitLayerFactory, NullTransitLayerFactory.class.getName()));
                defaultTransitLayer = new NullTransitLayerFactory().newTransitLayer();
            }
        }
        return defaultTransitLayer;
    }

}
