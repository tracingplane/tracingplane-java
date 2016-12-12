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
 * The AtomLayer implementation of {@link Baggage} is {@link BaggageAtoms}. A BaggageAtoms instance is a list of atoms.
 * At atom is an arbitrary-length array of bytes. The AtomLayer makes no attempt to interpret the meaning of each atom
 * -- this is the job of higher layers such as the BaggageLayer. Atoms are the unit of division at the AtomLayer, so
 * they typically correspond to just one 'thing'.
 * </p>
 * 
 * <p>
 * The AtomLayer specifies the following:
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
 * For a system to participate in the tracing plane, it must be capable of propagating at least two bytes of baggage.
 * </p>
 * 
 * <p>
 * The methods in this class are similar to the methods defined by {@link TransitLayer}, with
 * {@link #atoms(BaggageAtoms)} and {@link #wrap(List)} replacing {@link TransitLayer#serialize(Baggage)} and
 * {@link TransitLayer#deserialize(byte[], int, int). </p>
 * 
 * @param <B> Some implementation of {@link Baggage} used by this transit layer.
 */
public interface AtomLayer<B extends BaggageAtoms> extends TransitLayer<B> {

    /**
     * Turn a list of atoms into a {@link BaggageAtoms} instance.
     * 
     * @param atoms
     * @return
     */
    public B wrap(List<ByteBuffer> atoms);

    /**
     * Get the {@link BaggageAtoms} representation of a baggage instance.
     * 
     * @param baggage
     * @return
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