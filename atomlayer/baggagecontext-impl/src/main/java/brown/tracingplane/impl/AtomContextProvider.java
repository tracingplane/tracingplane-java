package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.List;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.atomlayer.AtomLayerSerialization;

/**
 * <p>
 * {@link BaggageProvider} for {@link AtomContext}, a minimal {@link BaggageContext} based on atoms and lexicographic merge.
 * </p>
 */
public class AtomContextProvider implements BaggageProvider<AtomContext> {

    @Override
    public boolean isValid(BaggageContext baggageContext) {
        return baggageContext == null || baggageContext instanceof AtomContext;
    }

    @Override
    public AtomContext newInstance() {
        return null;
    }

    @Override
    public void discard(AtomContext baggageContext) {
        if (baggageContext != null) {
            baggageContext.discard();
        }
    }

    @Override
    public AtomContext branch(AtomContext from) {
        return from == null ? null : from.branch();
    }

    @Override
    public AtomContext join(AtomContext left, AtomContext right) {
        return left == null ? right : left.merge(right);
    }

    @Override
    public AtomContext deserialize(byte[] serialized, int offset, int length) {
        return wrap(AtomLayerSerialization.deserialize(serialized, offset, length));
    }

    @Override
    public AtomContext deserialize(ByteBuffer buf) {
        return wrap(AtomLayerSerialization.deserialize(buf));
    }

    @Override
    public byte[] serialize(AtomContext baggageContext) {
        return AtomLayerSerialization.serialize(atoms(baggageContext));
    }

    @Override
    public byte[] serialize(AtomContext baggageContext, int maximumSerializedSize) {
        return AtomLayerSerialization.serialize(atoms(baggageContext), maximumSerializedSize);
    }

    AtomContext wrap(List<ByteBuffer> atoms) {
        if (atoms == null || atoms.size() == 0) {
            return null;
        } else {
            return new AtomContext(atoms);
        }
    }

    List<ByteBuffer> atoms(AtomContext baggageContext) {
        return baggageContext == null ? null : baggageContext.atoms();
    }

}
