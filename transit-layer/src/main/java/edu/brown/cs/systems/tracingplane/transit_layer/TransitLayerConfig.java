package edu.brown.cs.systems.tracingplane.transit_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TransitLayerConfig {

	private static final Logger log = LoggerFactory.getLogger(TransitLayerConfig.class);

	private static final String TRANSIT_LAYER_IMPLEMENTATION_KEY = "tracingplane.transit-layer.factory";

	public String transitLayerFactory;

	public TransitLayerConfig() {
		Config config = ConfigFactory.load();

		transitLayerFactory = config.getString(TRANSIT_LAYER_IMPLEMENTATION_KEY);
		try {
			Class.forName(transitLayerFactory);
		} catch (ClassNotFoundException e) {
			log.warn("The configured transit layer class {}=\"{}\" was not found; baggage will not be propagated");
		}
	}

}
