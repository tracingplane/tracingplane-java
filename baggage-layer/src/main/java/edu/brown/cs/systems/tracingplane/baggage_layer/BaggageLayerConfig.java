package edu.brown.cs.systems.tracingplane.baggage_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayerConfig;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.GenericBaggageLayerFactory;

public class BaggageLayerConfig {

    private static final Logger log = LoggerFactory.getLogger(AtomLayerConfig.class);

    private static final String BAGGAGE_LAYER_IMPLEMENTATION_KEY = "tracingplane.baggage-layer.factory";

    public String baggageLayerFactory;

    public BaggageLayerConfig() {
        Config conf = ConfigFactory.load();

        baggageLayerFactory = conf.getString(BAGGAGE_LAYER_IMPLEMENTATION_KEY);
        try {
            Class.forName(baggageLayerFactory);
        } catch (ClassNotFoundException e) {
            log.error("The configured baggage layer class {}=\"{}\" was not found; defaulting to simple context layer",
                      BAGGAGE_LAYER_IMPLEMENTATION_KEY, baggageLayerFactory);
        }
    }

    public BaggageLayer<?> createBaggageLayer() throws InstantiationException, IllegalAccessException,
                                                ClassNotFoundException {
        return ((BaggageLayerFactory) Class.forName(baggageLayerFactory).newInstance()).newBaggageLayer();
    }

    private static BaggageLayer<?> defaultBaggageLayer = null;

    public static synchronized BaggageLayer<?> defaultBaggageLayer() {
        if (defaultBaggageLayer == null) {
            BaggageLayerConfig config = new BaggageLayerConfig();
            try {
                defaultBaggageLayer = config.createBaggageLayer();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error(String.format("Unable to instantiate default baggage layer factory %s, defaulting to %s",
                                        config.baggageLayerFactory, GenericBaggageLayerFactory.class));
                return new GenericBaggageLayerFactory().newBaggageLayer();
            }
        }
        return defaultBaggageLayer;
    }

}
