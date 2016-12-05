package edu.brown.cs.systems.tracingplane.atom_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

public interface AtomLayerFactory extends TransitLayerFactory {

	AtomLayer<?> newContextLayer();

	static final Logger log = LoggerFactory.getLogger(AtomLayerFactory.class);

	public static AtomLayer<?> createDefaultContextLayer() {
		AtomLayerConfig config = new AtomLayerConfig();
		try {
			return createContextLayer(config);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format("Unable to instantiate default context layer factory %s, defaulting to %s",
					config.contextLayerFactory, RawAtomLayerFactory.class.getName()));
			return new RawAtomLayerFactory().newContextLayer();
		}
	}

	public static AtomLayer<?> createContextLayer(AtomLayerConfig config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return ((AtomLayerFactory) Class.forName(config.contextLayerFactory).newInstance()).newContextLayer();
	}

	@Override
	public default TransitLayer<?> newTransitLayer() {
		return newTransitLayer(BaggageAtoms.contextLayer);
	}

	public static <T extends BaggageAtoms> TransitLayer<T> newTransitLayer(AtomLayer<T> contextLayer) {
		return new AtomLayerImpl<T>(contextLayer);
	}

}
