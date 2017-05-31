package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.List;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.atomlayer.AtomLayerSerialization;
import brown.tracingplane.baggageprotocol.BaggageReader;
import brown.tracingplane.baggageprotocol.BaggageWriter;

/**
 * <p>
 * {@link BaggageProvider} for {@link NestedBaggageContext}, which extends {@link AtomContext} to interpret atoms as
 * nested data structures.
 * </p>
 */
public class NestedBaggageContextProvider implements BaggageProvider<NestedBaggageContext> {

    @Override
    public boolean isValid(BaggageContext baggage) {
        return baggage == null || baggage instanceof NestedBaggageContext;
    }

    @Override
    public NestedBaggageContext newInstance() {
        return null;
    }

    @Override
    public void discard(NestedBaggageContext baggage) {}

    @Override
    public NestedBaggageContext branch(NestedBaggageContext from) {
        return from == null ? null : from.branch();
    }

    @Override
    public NestedBaggageContext join(NestedBaggageContext left, NestedBaggageContext right) {
        if (left == null) {
            return right;
        } else {
            left.mergeWith(right);
            return left;
        }
    }

    @Override
    public NestedBaggageContext deserialize(byte[] serialized, int offset, int length) {
        List<ByteBuffer> atoms = AtomLayerSerialization.deserialize(serialized, offset, length);
        return NestedBaggageContext.parse(BaggageReader.create(atoms));
    }

    @Override
    public NestedBaggageContext deserialize(ByteBuffer buf) {
        List<ByteBuffer> atoms = AtomLayerSerialization.deserialize(buf);
        return NestedBaggageContext.parse(BaggageReader.create(atoms));
    }

    @Override
    public byte[] serialize(NestedBaggageContext baggage) {
        if (baggage == null) {
            return null;
        }
        BaggageWriter writer = BaggageWriter.create();
        baggage.serialize(writer);
        List<ByteBuffer> atoms = writer.atoms();
        return AtomLayerSerialization.serialize(atoms);
    }

    @Override
    public byte[] serialize(NestedBaggageContext baggage, int maximumSerializedSize) {
        if (baggage == null) {
            return null;
        }
        BaggageWriter writer = BaggageWriter.create();
        baggage.serialize(writer);
        List<ByteBuffer> atoms = writer.atoms();
        return AtomLayerSerialization.serialize(atoms, maximumSerializedSize);
    }

}
