package edu.brown.cs.systems.tracingplane.atom_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayerFactory;

public interface AtomLayerFactory extends TransitLayerFactory {

	AtomLayer<?> newAtomLayer();

	static final Logger log = LoggerFactory.getLogger(AtomLayerFactory.class);

	public static AtomLayer<?> createDefaultAtomLayer() {
		AtomLayerConfig config = new AtomLayerConfig();
		try {
			return createAtomLayer(config);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format("Unable to instantiate default atom layer factory %s, defaulting to %s",
					config.atomLayerFactory, RawAtomLayerFactory.class.getName()));
			return new RawAtomLayerFactory().newAtomLayer();
		}
	}

	public static AtomLayer<?> createAtomLayer(AtomLayerConfig config)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return ((AtomLayerFactory) Class.forName(config.atomLayerFactory).newInstance()).newAtomLayer();
	}

	@Override
	public default TransitLayer<?> newTransitLayer() {
		return newTransitLayer(BaggageAtoms.atomLayer);
	}

	public static <T extends BaggageAtoms> TransitLayer<T> newTransitLayer(AtomLayer<T> atomLayer) {
		return new AtomTransitLayerImpl<T>(atomLayer);
	}

}
