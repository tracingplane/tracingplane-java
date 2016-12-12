package edu.brown.cs.systems.tracingplane.atom_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import edu.brown.cs.systems.tracingplane.atom_layer.protocol.AtomLayerSerialization;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

/**
 * <p>
 * The AtomLayer is an implementation of the {@link TransitLayer} that specifies some default behavior for how to
 * branch, join, and serialize {@link Baggage}. The AtomLayer provides the minimal implementation necessary for an
 * application to participate in the tracing plane. The AtomLayer enables an application to propagate {@link Baggage}
 * without needing to know about what data it contains or the semantics of that data.
 * </p>
 * 
 * <p>
 * The AtomLayer implementation of {@link Baggage} is {@link BaggageAtoms}, which is simply a list of <b>atoms</b>. At
 * atom is an arbitrary-length array of bytes, and most of the APIs for dealing with atoms just use {@link ByteBuffer}
 * objects for atoms. The AtomLayer is not responsible for understanding the contents of each individual atom. It simply
 * sees baggage as a list of atoms. For example, it might receive baggage with the following atoms:
 * </p>
 * 
 * <pre>
 * {@code
 * []
 * [F8, 00]
 * [00, 00, 00, 00, 00, 00, 00, 00, 07]
 * [F8, 01]
 * [00, 00, 00, 00, 00, 00, 00, 00, 0A]
 * [00, 00, 00, 00, 00, 00, 00, 00, 14]
 * }
 * </pre>
 * 
 * <p>
 * The AtomLayer does not attempt to interpret the meaning of these bytes. However, it does specify logic for how to
 * merge the atoms for multiple baggage instances, how to drop atoms if a baggage instance is too large, and how to
 * serialize atoms:
 * </p>
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
 * 
 * <p>
 * For a system to participate in the tracing plane, it must be capable of propagating at least two bytes of baggage.
 * </p>
 * 
 * <p>
 * The methods in this class are similar to the methods defined by {@link TransitLayer}, with
 * {@link #atoms(BaggageAtoms)} and {@link #wrap(List)} replacing {@link TransitLayer#serialize(Baggage)} and
 * {@link TransitLayer#deserialize(byte[], int, int)}.
 * </p>
 * 
 * @param <B> Some implementation of {@link Baggage} used by this transit layer.
 */
public interface AtomLayer<B extends BaggageAtoms> extends TransitLayer<B> {

    /**
     * <p>
     * Turn a list of atoms into a {@link BaggageAtoms} instance.
     * </p>
     * 
     * @param atoms The atom representation of a BaggageAtoms instance
     * @return a parsed BaggageAtoms instance
     */
    public B wrap(List<ByteBuffer> atoms);

    /**
     * <p>
     * Get the atoms that comprise the provided baggage instance.
     * </p>
     * 
     * @param baggage a {@link BaggageAtoms} instance
     * @return the atoms of this {@link BaggageAtoms} instance
     */
    public List<ByteBuffer> atoms(B baggage);

    @Override
    public default B deserialize(byte[] serialized, int offset, int length) {
        return wrap(AtomLayerSerialization.deserialize(serialized, offset, length));
    }

    @Override
    public default B readFrom(InputStream in) throws IOException {
        return wrap(AtomLayerSerialization.readFrom(in));
    }

    @Override
    public default byte[] serialize(B baggage) {
        return AtomLayerSerialization.serialize(atoms(baggage));
    }

    @Override
    public default void writeTo(OutputStream out, B baggage) throws IOException {
        AtomLayerSerialization.write(out, atoms(baggage));
    }

    @Override
    public default byte[] serialize(B baggage, int maximumSerializedSize) {
        return AtomLayerSerialization.serialize(atoms(baggage), maximumSerializedSize);
    }

    @Override
    public default void writeTo(OutputStream out, B baggage, int maximumSerializedSize) throws IOException {
        AtomLayerSerialization.write(out, atoms(baggage), maximumSerializedSize);
    }

}