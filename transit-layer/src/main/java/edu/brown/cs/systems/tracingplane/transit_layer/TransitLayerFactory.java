package edu.brown.cs.systems.tracingplane.transit_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.transit_layer.impl.TransitLayerNullImpl2;

public interface TransitLayerFactory {

	static final Logger log = LoggerFactory.getLogger(TransitLayerFactory.class);

	public static TransitLayer2<?> createDefaultTransitLayer() {
		TransitLayerConfig2 config = new TransitLayerConfig2();
		try {
			return createTransitLayer(config);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format("Unable to instantiate default transit layer factory %s, defaulting to %s",
					config.transitLayerFactory, TransitLayerNullImpl2.NullTransitLayerFactory.class.getName()));
			return new TransitLayerNullImpl2.NullTransitLayerFactory().newTransitLayer();
		}
	}

	public static TransitLayer2<?> createTransitLayer(TransitLayerConfig2 config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return ((TransitLayerFactory) Class.forName(config.transitLayerFactory).newInstance()).newTransitLayer();
	}

	TransitLayer2<?> newTransitLayer();

}
