package edu.brown.cs.systems.tracingplane.baggage_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class BaggageLayerConfig {

	private static final Logger log = LoggerFactory.getLogger(BaggageLayerConfig.class);

	private static final String BAGGAGE_LAYER_IMPLEMENTATION_KEY = "tracingplane.baggage-layer.implementation";

	public String bagggageLayerImplementationClassName;

	public BaggageLayerConfig() {
		Config conf = ConfigFactory.load();

		bagggageLayerImplementationClassName = conf.getString(BAGGAGE_LAYER_IMPLEMENTATION_KEY);
		try {
			Class.forName(bagggageLayerImplementationClassName);
		} catch (ClassNotFoundException e) {
			log.error(
					"The configured baggage layer class {}=\"{}\" was not found; defaulting to generic baggage layer");
		}
	}

	public BaggageLayer createBaggageLayerInstance()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (BaggageLayer) Class.forName(bagggageLayerImplementationClassName).newInstance();
	}

	public BaggageLayer createBaggageLayerInstanceOrDefault() {
		try {
			return createBaggageLayerInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format(
					"Unable to create instance of baggage layer class %s=\"%s\", defaulting to generic baggage layer",
					BAGGAGE_LAYER_IMPLEMENTATION_KEY, bagggageLayerImplementationClassName), e);
			// return new GenericBaggageLayerImpl();
			return null;
		}
	}

}
