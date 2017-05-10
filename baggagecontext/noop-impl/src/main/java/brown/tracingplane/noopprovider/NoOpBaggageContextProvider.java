package brown.tracingplane.noopprovider;

import java.nio.ByteBuffer;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;

/**
 * A {@link BaggageProvider} implementation that always just returns null.
 */
public class NoOpBaggageContextProvider implements BaggageProvider<BaggageContext> {

    @Override
    public boolean isValid(BaggageContext baggage) {
        return baggage == null;
    }

    @Override
    public BaggageContext newInstance() {
        return null;
    }

    @Override
    public void discard(BaggageContext baggage) {}

    @Override
    public BaggageContext branch(BaggageContext from) {
        return null;
    }

    @Override
    public BaggageContext join(BaggageContext left, BaggageContext right) {
        return null;
    }

    @Override
    public BaggageContext deserialize(byte[] serialized, int offset, int length) {
        return null;
    }

    @Override
    public BaggageContext deserialize(ByteBuffer buf) {
        return null;
    }

    @Override
    public byte[] serialize(BaggageContext baggage) {
        return null;
    }

    @Override
    public byte[] serialize(BaggageContext baggage, int maximumSerializedSize) {
        return null;
    }

}
