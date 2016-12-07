package edu.brown.cs.systems.tracingplane.baggage_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayer;
import edu.brown.cs.systems.tracingplane.atom_layer.AtomLayerFactory;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.GenericBaggageLayerFactory;

public interface BaggageLayerFactory extends AtomLayerFactory {
	
	BaggageLayer<?> newBaggageLayer();

	static final Logger log = LoggerFactory.getLogger(BaggageLayerFactory.class);
	
	public static BaggageLayer<?> createDefaultBaggageLayer() {
		BaggageLayerConfig config = new BaggageLayerConfig();
		try {
			return createBaggageLayer(config);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format("Unable to instantiate default baggage layer factory %s, defaulting to %s",
					config.baggageLayerFactory, GenericBaggageLayerFactory.class));
			return new GenericBaggageLayerFactory().newBaggageLayer();
		}
	}

	public static BaggageLayer<?> createBaggageLayer(BaggageLayerConfig config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return ((BaggageLayerFactory) Class.forName(config.baggageLayerFactory).newInstance()).newBaggageLayer();
	}

	@Override
	public default AtomLayer<?> newAtomLayer() {
		return BaggageContents.baggageLayer;
	}

}
