package edu.brown.cs.systems.tracingplane.atom_layer;

import java.nio.ByteBuffer;
import java.util.List;
import edu.brown.cs.systems.tracingplane.atom_layer.protocol.AtomLayerOverflow;
import edu.brown.cs.systems.tracingplane.atom_layer.protocol.AtomLayerSerialization;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

/**
 * <p>
 * BaggageAtoms is an extension of {@link Baggage} for use by the {@link AtomLayer}. A BaggageAtoms instance is a list
 * of <i>atoms</i>. At atom is an arbitrary-length array of bytes. The AtomLayer makes no attempt to interpret the
 * meaning of each atom -- this is the job of higher layers such as the BaggageLayer. Atoms are the unit of granularity
 * at the AtomLayer (e.g., an atom is indivisible).
 * </p>
 * 
 * <p>
 * BaggageAtoms are a simple default representation of data that enables consistent propagation of data while traversing
 * different thread, process, machine, and application boundaries. The AtomLayer specifies the following:
 * <ul>
 * <li>The underlying serialization format of atoms, which is to prefix the bytes of each atom with their length
 * (encoded as a protobuf-style varint). For example, with {@code byte[] a = new byte[10];} and
 * {@code byte[] b = new byte[20];}, the serialized representation would be
 * {@code [length=32][a.length=10][ a ][b.length=20][ b ]}. See {@link AtomLayerSerialization} for more information.
 * </li>
 * <li>The default merge behavior when two branches of an execution join. Two Baggage instances {@code a} and {@code b}
 * are merged by <i>lexicographically</i> merging their respective atoms and dropping duplicates that are encountered.
 * respective lists of atoms while dropping duplicates <i>as they are encountered</i>. See {@link Lexicographic} for
 * more information.</li>
 * <li>The default behavior for dropping atoms if a baggage instance is larger than permitted by a system. For example,
 * if a system wants to keep headers less than a certain size, it might mean baggage must be less than 100 bytes in
 * size. To trim baggage, atoms are dropped from the <b>end</b> of the list of atoms. If atoms are dropped, then an
 * {@link BaggageAtoms#OVERFLOW_MARKER OVERFLOW_MARKER} should be appended to the end of the list of atoms. The overflow
 * marker is the empty atom (e.g., zero-length atom) which is lexicographically smaller than all other atoms and
 * therefore tracks the position in the baggage where data was dropped.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Propagation of BaggageAtoms should still be done via the static methods in {@link Baggage}. However, there are
 * several additional static methods available in this class.
 * </p>
 *
 */
public interface BaggageAtoms extends Baggage {

    /**
     * The overflow marker is a zero-length atom, used to indicate that an application had to drop some of the atoms in
     * this baggage. The overflow marker is lexicographically smaller than all other atoms, so it 'holds' its position
     * when merging with other atoms. This enables end-users to determine where in their atoms data might have been
     * dropped.
     */
    public static final ByteBuffer OVERFLOW_MARKER = AtomLayerOverflow.OVERFLOW_MARKER;

    /**
     * The {@link AtomLayer} implementation installed in the process.
     */
    public static final AtomLayer<?> atomLayer = AtomLayerConfig.defaultAtomLayer();

    /** Create a BaggageAtoms object by wrapping the raw bytes provided */
    public static BaggageAtoms wrap(List<ByteBuffer> atoms) {
        return AtomLayerCompatibility.wrap(atomLayer, atoms);
    }

    /** Serialize the provided BaggageAtoms object into the byte-array representation. */
    public static List<ByteBuffer> atoms(BaggageAtoms atoms) {
        return AtomLayerCompatibility.atoms(atomLayer, atoms);
    }

    /**
     * Gets the serialized byte array atoms for the thread's current context, if there is one Serialize the provided
     * BaggageAtoms object into the byte-array representation.
     */
    public static List<ByteBuffer> atoms() {
        return AtomLayerCompatibility.atoms(atomLayer, Baggage.get());
    }

}
