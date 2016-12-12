package edu.brown.cs.systems.tracingplane.atom_layer;

import java.nio.ByteBuffer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

/**
 * This class just does type checking on {@link BaggageAtoms} instances to ensure they are compatible with the
 * {@link AtomLayer} installed in the current process. In general, BaggageAtoms instances should never be created by any
 * atom layer other than the one installed in the current process.
 */
public class AtomLayerCompatibility {

    static final Logger log = LoggerFactory.getLogger(AtomLayerCompatibility.class);

    public static <B extends BaggageAtoms> B wrap(AtomLayer<B> contextLayer, List<ByteBuffer> atoms) {
        return contextLayer.wrap(atoms);
    }

    public static <B extends BaggageAtoms> List<ByteBuffer> atoms(AtomLayer<B> contextLayer, Baggage maybeAtoms) {
        if (maybeAtoms instanceof BaggageAtoms) {
            return atoms(contextLayer, (BaggageAtoms) maybeAtoms);
        } else {
            log.warn("incompatible Baggage to {}.atoms; {} is not instance of BaggageAtoms",
                     contextLayer.getClass().getName(), maybeAtoms.getClass().getName());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <B extends BaggageAtoms> List<ByteBuffer> atoms(AtomLayer<B> contextLayer, BaggageAtoms atoms) {
        if (contextLayer.isInstance(atoms)) {
            return contextLayer.atoms((B) atoms);
        } else {
            log.warn("incompatible BaggageAtoms to {}.atoms; BaggageAtoms class is {}",
                     contextLayer.getClass().getName(), atoms.getClass().getName());
        }
        return null;
    }

}
