package brown.tracingplane;

import java.nio.ByteBuffer;

/**
 * <p>
 * A straightforward {@link TransitLayer} implementation based on thread-local storage.
 * </p>
 */
public class ThreadLocalTransitLayer implements TransitLayer {

    private final BaggageProvider<BaggageContext> provider;
    private final ThreadLocal<BaggageContext> current = new ThreadLocal<BaggageContext>();

    /**
     * @param provider the implementation of {@link BaggageProvider} in use by this transit layer
     */
    public ThreadLocalTransitLayer(BaggageProvider<BaggageContext> provider) {
        this.provider = provider;
    }

    @Override
    public void discard() {
        current.remove();
    }

    @Override
    public BaggageContext branch() {
        return provider.branch(current.get());
    }

    @Override
    public byte[] branchBytes() {
        return provider.serialize(provider.branch(current.get()));
    }

    @Override
    public void join(BaggageContext otherContext) {
        current.set(provider.join(current.get(), otherContext));
    }

    @Override
    public void join(ByteBuffer serializedContext) {
        current.set(provider.join(current.get(), provider.deserialize(serializedContext)));
    }

    @Override
    public void join(byte[] serialized, int offset, int length) {
        current.set(provider.join(current.get(), provider.deserialize(serialized, offset, length)));
    }

    @Override
    public void set(BaggageContext baggage) {
        current.set(baggage);
    }

    @Override
    public void set(ByteBuffer serializedContext) {
        current.set(provider.deserialize(serializedContext));
    }

    @Override
    public void set(byte[] serialized, int offset, int length) {
        current.set(provider.deserialize(serialized, offset, length));
    }

    @Override
    public BaggageContext take() {
        try {
            return current.get();
        } finally {
            current.remove();
        }
    }

    @Override
    public byte[] takeBytes() {
        try {
            return provider.serialize(current.get());
        } finally {
            current.remove();
        }
    }

    @Override
    public BaggageContext peek() {
        return current.get();
    }

    @Override
    public void update(BaggageContext baggage) {
        current.set(baggage);
    }

}
