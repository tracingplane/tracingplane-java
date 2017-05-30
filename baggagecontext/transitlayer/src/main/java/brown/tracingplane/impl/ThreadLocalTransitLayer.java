package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;
import brown.tracingplane.TransitLayer;

/**
 * <p>
 * A straightforward {@link TransitLayer} implementation based on thread-local storage. This is the out-of-the-box
 * transit layer implementation provided by the Tracing Plane.
 * </p>
 * 
 * <p>
 * It is unlikely that you will need to manually configure the Tracing Plane to use this class -- depending on the
 * distribution you are using, it is likely to be configured by default.
 * </p>
 * 
 * <p>
 * Otherwise, to manually configure the {@link ThreadLocalTransitLayer}, you must configure it using the
 * <code>baggage.transit</code> property to use {@link ThreadLocalTransitLayerFactory}, e.g.:
 * 
 * <pre>
 *      -Dbaggage.transit=brown.tracingplane.impl.ThreadLocalTransitLayerFactory
 * </pre>
 * 
 * It can alternatively be configured in your typesafe config's <code>application.conf</code>, e.g.:
 * 
 * <pre>
 * baggage.transit = "brown.tracingplane.impl.ThreadLocalTransitLayerFactory"
 * </pre>
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
