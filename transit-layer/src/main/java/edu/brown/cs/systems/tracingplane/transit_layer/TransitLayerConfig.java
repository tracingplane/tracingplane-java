package edu.brown.cs.systems.tracingplane.transit_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TransitLayerConfig {

	private static final Logger log = LoggerFactory.getLogger(TransitLayer.class);

	private static final String TRANSIT_LAYER_IMPLEMENTATION_KEY = "tracingplane.transit-layer.implementation";

	public String transitLayerImplementationClassName;

	private TransitLayerConfig() {
		Config config = ConfigFactory.load();

		transitLayerImplementationClassName = config.getString(TRANSIT_LAYER_IMPLEMENTATION_KEY);
		try {
			Class.forName(transitLayerImplementationClassName);
		} catch (ClassNotFoundException e) {
			log.error("The configured transit layer class {}=\"{}\" was not found; baggage will not be propagated");
		}
	}

	public TransitLayer createTransitLayerInstance()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (TransitLayer) Class.forName(transitLayerImplementationClassName).newInstance();
	}

	private static TransitLayerConfig DEFAULT_CONFIG;

	private static TransitLayerConfig defaultConfig() {
		if (DEFAULT_CONFIG == null) {
			synchronized (TransitLayerConfig.class) {
				if (DEFAULT_CONFIG == null) {
					DEFAULT_CONFIG = new TransitLayerConfig();
				}
			}
		}
		return DEFAULT_CONFIG;
	}

	public static TransitLayer newTransitLayer() {
		try {
			return defaultConfig().createTransitLayerInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format(
					"Unable to create instance of configured default transit layer class %s=\"%s\"; baggage will not be propagated",
					TRANSIT_LAYER_IMPLEMENTATION_KEY, defaultConfig().transitLayerImplementationClassName), e);
			return new TransitLayerNullImpl.NullTransitLayer();
		}
	}

}
