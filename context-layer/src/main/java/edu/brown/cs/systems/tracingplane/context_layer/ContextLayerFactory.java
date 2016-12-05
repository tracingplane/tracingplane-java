package edu.brown.cs.systems.tracingplane.context_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.impl.RawAtomsContextLayerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer2;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

public interface ContextLayerFactory extends TransitLayerFactory {

	ContextLayer<?> newContextLayer();

	static final Logger log = LoggerFactory.getLogger(ContextLayerFactory.class);

	public static ContextLayer<?> createDefaultContextLayer() {
		return createDefaultContextLayer(new ContextLayerConfig());
	}

	public static ContextLayer<?> createDefaultContextLayer(ContextLayerConfig config) {
		try {
			return createContextLayer(config);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format("Unable to instantiate default context layer factory %s, defaulting to %s",
					config.contextLayerFactory, RawAtomsContextLayerFactory.class.getName()));
			return new RawAtomsContextLayerFactory().newContextLayer();
		}
	}

	public static ContextLayer<?> createContextLayer(ContextLayerConfig config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return ((ContextLayerFactory) Class.forName(config.contextLayerFactory).newInstance()).newContextLayer();
	}

	@Override
	public default TransitLayer2<?> newTransitLayer() {
		ContextLayerConfig config = new ContextLayerConfig();
		ContextLayer<?> contextLayer = ContextLayerFactory.createDefaultContextLayer(config);
		return newTransitLayer(config, contextLayer);
	}
	
	public static TransitLayer2<?> newTransitLayer(ContextLayerConfig config) {
		return newTransitLayer(config, createDefaultContextLayer(config));
	}

	public static <T extends ContextBaggage> TransitLayer2<T> newTransitLayer(ContextLayerConfig config,
			ContextLayer<T> contextLayer) {
		return new TransitLayerImpl<T>(config, contextLayer);
	}

}
