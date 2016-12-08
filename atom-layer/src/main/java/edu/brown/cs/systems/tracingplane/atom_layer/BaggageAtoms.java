package edu.brown.cs.systems.tracingplane.atom_layer;

import java.nio.ByteBuffer;
import java.util.List;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.ThreadLocalBaggage;

public interface BaggageAtoms extends Baggage {

    public static final ByteBuffer OVERFLOW_MARKER = ByteBuffer.allocate(0);

    public static final AtomLayer<?> atomLayer = AtomLayerFactory.createDefaultAtomLayer();

    /** Create a BaggageAtoms object by wrapping the raw bytes provided */
    public static BaggageAtoms wrap(List<ByteBuffer> atoms) {
        return AtomLayerCompatibility.wrap(atomLayer, atoms);
    }

    /** Serialize the provided BaggageAtoms object into the byte-array representation. */
    public static List<ByteBuffer> atoms(BaggageAtoms atoms) {
        return AtomLayerCompatibility.atoms(atomLayer, atoms);
    }

    /** Gets the serialized byte array atoms for the thread's current context, if there is one Serialize the provided
     * BaggageAtoms object into the byte-array representation. */
    public static List<ByteBuffer> atoms() {
        return AtomLayerCompatibility.atoms(atomLayer, ThreadLocalBaggage.get());
    }

}
